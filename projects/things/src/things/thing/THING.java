/**
 * THINGS/THINGER 2009
 * Copyright Erich P Gatejen (c) 2001 through 2009  ALL RIGHTS RESERVED
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package things.thing;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsExceptionBundle;
import things.common.ThingsNamespace;
import things.common.tools.StopWatch;
import things.data.Data;
import things.data.ThingsPropertyView;
import things.thinger.ExpressionInterface;
import things.thinger.SystemInterface;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.kernel.ControlInterface;

/**
 * A thing.<br>
 * <p><pre>
 * The life cycle of a thing (with default behavior:
 * definition -> instance(check parms) -> call -> <normal exit> -> RESULTS -> return
 *                                             -> ERROR	    	-> trap    -> RESULTS -> return
 *                                             -> FAULT     	-> trap    -> RESULTS -> propagate          
 * </pre>
 * <p>
 * NOTES:<br>
 * Logging is currently done with a system logger.  May want to change that.<br>
 * Ultimately, RESULT should be reported as Level.DATA.  Only the root THING will do so automatically, rolling up the registered results of
 * any calls, depending on how the THINGS were implements..  A simple INFO logging of each completed THING will be done.  Naturally, any THING
 * can explicitly report results at any time. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public abstract class THING extends BASE implements THINGInterface {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	// private String myName = null;
	private ExpressionInterface myExpressionInterface;
	private ExpressionInterface parentInterface;
	private ControlInterface myControlInterface;
	private StopWatch runTime;
	
	/**
	 * The result.
	 */
	private RESULT	myResult;
	
	/**
	 * Reportable exceptions.
	 */
	private ThingsExceptionBundle<ThingsException> exceptions;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * Definition step.
	 */
	public abstract void DEFINITION() throws UserException;
	
	/**
	 * The process can only be called once per instance!
	 * @throws any Throwable.  It's important to let InterruptedException's to escape or the system may pound your THING with interruptions.
	 */
	public abstract void PROCESS() throws Throwable;
	
	/**
	 * Optional result step.  The default will report results based on defined tests.  Overload it if you want to manipulate the results.
	 * If you return null, it will use the system defined results.  The default will return null;
	 * @param defaultReport the result the system calculated based on defined tests.
	 * @throws ThingException
	 * @return the actual result you want to report.
	 */
	public RESULT RESULTS(RESULT defaultReport) throws ThingsException {
		return defaultReport;
	}

	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  May be overridden by the implementation class. 
	
	/** 
	 * Default FAULT trap.  It will just propagate it as a UserException FAULT.
	 * @throws UserException
	 * @param te the offending exception
	 */
	public void CATCH_FAULT(ThingsException te) throws UserException {
		if (te instanceof UserException) throw (UserException)te;
		throw new UserException("THING Fault from exception.", ThingsException.SYSTEM_FAULT_THING_FAULT, te, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
	}
	
	/**
	 * Default ERROR trap.  It won't do anything with it.
	 * @throws UserException
	 * @param te the offending exception
	 */
	public void CATCH_ERROR(ThingsException te) throws UserException {
		if (te instanceof UserException) throw (UserException)te;
		throw new UserException("THING Error from exception.", ThingsException.ERROR_THING_ERROR, te, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
	}
	
	/** 
	 * The THING is about to pause.  This gives it a chance to prepare.  Do not linger here!
	 * @throws UserException
	 */
	public void PAUSING() throws UserException {
		//NOP
	}
	
	/** 
	 * The THING is about to unpause.  This gives it a chance to prepare.  Do not linger here!
	 * @throws UserException
	 */
	public void UNPAUSING() throws UserException {
		//NOP
	}
	
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == SERVICE AND INTERFACE METHODS.  These are exposed in THINGInterface.
	
	/**
	 * Instantiate a module.  It will seek and create the one as named and init it.  You'll have to cast it to the specific class if
	 * you want to access subclass methods.  
	 * @param moduleName the name of the module.  This must match the class name for the MODULE or it will not be found and run.
	 * @return the MODULE 
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public MODULE INSTANCE(String moduleName) throws UserException, InterruptedException {
		
		MODULE theModule;
		try {
			theModule = mySystemInterface.loadModule(moduleName);
			theModule.init(mySystemInterface);
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Instantiated a MODULE for INSTANCE()", ThingsCodes.DEBUG_MODULE_LOADED, ThingsNamespace.ATTR_MODULE_NAME, moduleName);
		} catch (ThingsException cc) {
			throw new UserException("MODULE implementation class not found.", UserException.ERROR_MODULE_IMPLEMENTATION_NOT_FOUND, cc, ThingsNamespace.ATTR_MODULE_NAME, moduleName);
		} catch (Throwable t) {
			throw new UserException("MODULE creation failed to spurious exception.", UserException.ERROR_MODULE_INSTANTIATION_FAILED, t, ThingsNamespace.ATTR_MODULE_NAME, moduleName);		
		}
		return theModule;
	}
	
	/**
	 * Call a THING.  A new instance of it will be created.
	 * @param thingName the name of the thing.  This must match the class name for the THING or it will not be found and run.
	 * @return the RESULT of the call.
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public RESULT CALL(String thingName) throws UserException, InterruptedException {

		// Build our instance
		THING theThing;
		try {
			theThing = mySystemInterface.loadThing(thingName);
			theThing.init(mySystemInterface, myControlInterface, myExpressionInterface, parentInterface);
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Instantiated a THING for CALL()", ThingsCodes.DEBUG_THING_CALL_SETUP, ThingsNamespace.ATTR_THING_NAME, thingName);
		} catch (ThingsException cc) {
			throw new UserException("CALL implementation class not found.", UserException.ERROR_THING_CALL_IMPLEMENTATION_NOT_FOUND, cc, ThingsNamespace.ATTR_THING_NAME, thingName);
		} catch (Throwable t) {
			throw new UserException("CALL creation failed to spurious exception.", UserException.ERROR_THING_CALL_FAILED, t, ThingsNamespace.ATTR_THING_NAME, thingName);		
		}
		
		// Run it.
		RESULT ourResult = null;
		try {
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Start CALL()", ThingsCodes.DEBUG_THING_CALL_ENTER, ThingsNamespace.ATTR_THING_NAME, thingName);
			ourResult = theThing.call_chain();
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Done CALL()", ThingsCodes.DEBUG_THING_CALL_DONE, ThingsNamespace.ATTR_THING_NAME, thingName);
		} catch (InterruptedException ie) {
			if (localSystemLogger.debuggingState()) localSystemLogger.shout("Done CALL() due to INTERRUPTION.", ThingsCodes.DEBUG_THING_CALL_DONE_INTERRUPTION, Logger.LEVEL.DEBUG, ThingsNamespace.ATTR_THING_NAME, thingName);		
			throw ie;
		} catch (ThingsException cc) {
			if (localSystemLogger.debuggingState()) localSystemLogger.shout("Failed CALL() due to exception.", ThingsCodes.DEBUG_THING_CALL_DONE_EXCEPTION, Logger.LEVEL.DEBUG, ThingsNamespace.ATTR_THING_NAME, thingName);		
			throw new UserException("CALL process failed.", UserException.ERROR_THING_CALL_FAILED, cc, ThingsNamespace.ATTR_THING_NAME, thingName);
		} catch (Throwable t) {
			if (localSystemLogger.debuggingState()) localSystemLogger.shout("Failed CALL() due to spurious exception.", ThingsCodes.DEBUG_THING_CALL_DONE_EXCEPTION, Logger.LEVEL.DEBUG, ThingsNamespace.ATTR_THING_NAME, thingName);		
			throw new UserException("CALL process failed to spurious exception.", UserException.ERROR_THING_CALL_FAILED, t, ThingsNamespace.ATTR_THING_NAME, thingName);		
		}
				
		// Done
		return ourResult;
	}
	
	/**
	 * Run a THING.  A new instance of it will be created.  It will be run in a separate process, but it will inherit a common expression interface.
	 * @param thingName the name of the thing.  This must match the class name for the THING or it will not be found and run.
	 * @return the RESULT of the call.  The result will have the attribute ATTR_THING_PROCESS_ID identifying the kernal process id of the running thing.
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public RESULT RUN(String thingName) throws UserException, InterruptedException {
		RESULT myResult = null;

		try {
			String name = mySystemInterface.runThing(thingName, parentInterface);
			myResult = new RESULT( new Metrics(1, 0, 0, 0, 1, 1, 0, 0, 0), Data.Type.PASS, ThingsNamespace.ATTR_THING_PROCESS_ID, name);
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Instantiated a THING for RUN()", ThingsCodes.DEBUG_THING_CALL_SETUP, ThingsNamespace.ATTR_THING_NAME, thingName, ThingsNamespace.ATTR_THING_NAME_ACTUAL, name);
		} catch (ThingsException cc) {
			if (localSystemLogger.debuggingState()) localSystemLogger.shout("Failed RUN() due to exception.", ThingsCodes.DEBUG_THING_RUN_DONE_EXCEPTION, Logger.LEVEL.DEBUG, ThingsNamespace.ATTR_THING_NAME, thingName);		
			throw new UserException("RUN process failed.", UserException.ERROR_THING_RUN_FAILED, cc, ThingsNamespace.ATTR_THING_NAME, thingName);
		} catch (Throwable t) {
			if (localSystemLogger.debuggingState()) localSystemLogger.shout("Failed RUN() due to spurious exception.", ThingsCodes.DEBUG_THING_RUN_DONE_EXCEPTION, Logger.LEVEL.DEBUG, ThingsNamespace.ATTR_THING_NAME, thingName);		
			throw new UserException("RUN process failed to spurious exception.", UserException.ERROR_THING_RUN_FAILED, t, ThingsNamespace.ATTR_THING_NAME, thingName);		
		}
		
		return myResult;
	}
	
	/**
	 * Express a result.  It is up to the environment as to where it will be expressed, be it to the local log or across the wire.
	 * It is up to the kernel to make this reliable and there will be no receipt for it.  If you absolutely must be certain that your
	 * receipt made it somewhere, use a channel.
	 */
	public void EXPRESS(RESULT  theResult) throws UserException {
		try {
			if (myExpressionInterface!=null) myExpressionInterface.expressResult(theResult);
			if (parentInterface!=null) parentInterface.expressResult(theResult);
			
		} catch (Throwable te) {
			throw new UserException("Failed to express.", UserException.USER_EXPRESSION_FAILED, te);
		}
	}
	
	/**
	 * Set an expressor.  This will become the parent expression interface and all child THINGS will use it (unless they call SET_EXPRESSOR too).
	 * @param destination the destination for the expressions.  This is up to the implementation.  It might be a universe address or a channel.
	 * @throws UserException
	 */
	public void SET_EXPRESSOR(String  destination) throws UserException {
		try {
			ExpressionInterface namedInterface =  mySystemInterface.getNamedExpressor(destination);		
			if (parentInterface != null) localSystemLogger.debug("Call to SET_EXPRESSOR will eclipse a parent expressor.", ThingsCodes.DEBUG_THING_ECLIPSE_PARENT_EXPRESSOR);
			parentInterface = namedInterface;
			
		} catch (Throwable te) {
			throw new UserException("Failed to set expressor.", UserException.ERROR_THING_EXPRESSION_COULD_NOT_SET, te);
		}
	}
	
	/**
	 * Get a module.  Each call will yield a unique instance.  It will be initialized in the same context as the THING.
	 * @param moduleName the name of the module.  This must match the class name for the MODULE or it will not be found and run.
	 * @return the MODULE itself.
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public MODULE MODULE(String moduleName) throws UserException, InterruptedException {

		// Build our instance
		MODULE theModule;
		try {
			theModule = mySystemInterface.loadModule(moduleName);
			if (localSystemLogger.debuggingState()) localSystemLogger.debug("Instantiated a MODULE for MODULE()", ThingsCodes.DEBUG_THING_MODULE_INSTANCE, ThingsNamespace.ATTR_MODULE_NAME, moduleName);
		} catch (ThingsException cc) {
			throw new UserException("MODULE implementation class not found.", UserException.ERROR_MODULE_INSTANTIATION_FAILED, cc, ThingsNamespace.ATTR_MODULE_NAME, moduleName);
		} catch (Throwable t) {
			throw new UserException("MODULE creation failed to spurious exception.", UserException.ERROR_MODULE_IMPLEMENTATION_NOT_FOUND, t, ThingsNamespace.ATTR_MODULE_NAME, moduleName);		
		}

		// Done
		return theModule;
	}
	
	/**
	 * Get how long the thing has been running in milliseconds.  This is not how much processing time has accumulated,
	 * but from the moment it started running until it stopped, pauses and whatever included.
	 * @return milliseconds.
	 */
	public long RUN_TIME() throws UserException {
		return runTime.time();
	}

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == THING SERVICE METHODS.
	
	/**
	 * Accept a pause.  If the processes has a pause request, allow the pause.  This is for NICE things that will
	 * allow the system to pause them.
	 */
	public void ACCEPT_PAUSE() {
		try {
			myControlInterface.acceptPause();
		} catch (ThingsException se) {
			exceptions.add(se);
		}
	}
	
	/**
	 * Accept a halt.  If the processes has a halt request, allow the halt.  It will result in a exception that should be allowed out.
	 */
	public void ACCEPT_HALT() throws UserException {
		try {
			myControlInterface.acceptHalt();
		} catch (ThingsException se) {
			throw new UserException("Halt accepted.  Quitting.", UserException.PANIC_PROCESS_RESPONDING_TO_HALT_OK, se);
		}
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DIRECTIVE DATA AND METHODS.  Will be provided by the system.
	
	
	// == RESULT MANAGEMENT ==============================================================================================================

	/**
	 * All the result definitions by name.
	 */
	private HashMap<String, ResultDefinition> resultDefinitions = new HashMap<String, ResultDefinition>();	
		
	/**
	 * Define a result.  It'll overwrite any result definition of the same name.
	 * @param name
	 * @param description
	 * @param expectation
	 * @throws UserException
	 */
	public void DEFINE_RESULT(String name, String description, ResultExpectation expectation) throws UserException {
		
		// Qualify.  Dont' allow nulls and must be a result type.
		if ((name==null)||(description==null)) throw new UserException("Cannot DEFINE_RESULT with a null parameter.", ThingsCodes.THING_FAULT_DEFINITION_BAD, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name, ThingsNamespace.ATTR_THING_RESULT_DESCRIPTION, description);
		
		// Add
		resultDefinitions.put(name, new ResultDefinition(name, description, expectation));
	}
		
	/**
	 * Set result based on a result type only.  A complete result will not be available for expression.  IF the result is alreadt set, it'll be replaced with this result.
	 * @param name the name of the result
	 * @param resultType the result type as defined in Data
	 * @see things.data.Data
	 * @throws a UserException, usually only if the result was not defined.
	 */
	public void SET_RESULT(String name, Data.Type	resultType) throws UserException {
		if (name==null) throw new UserException("Cannot SET_RESULT with a null name.", ThingsCodes.ERROR_THING_RESULT_NOT_DEFINED, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
		if (!resultDefinitions.containsKey(name)) throw new UserException("RESULT not defined.", ThingsCodes.ERROR_THING_RESULT_NOT_DEFINED, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);
		
		ResultDefinition item = resultDefinitions.get(name);
		try {
			item.actual = new RESULT(resultType);
		} catch (Throwable t) {
			throw new UserException("Could not create a result to SET_RESULT.", UserException.THING_FAULT_RESULT_FUNDIMENTAL, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);
		}
	}
	
	/**
	 * Set result based on a complete RESULT. IF the result is already set, it'll be replaced with this result.
	 * @param name the name of the result
	 * @param result the actual result
	 * @throws a UserException, usually only if the result was not defined. 
	 */
	public void SET_RESULT(String name, RESULT	result) throws UserException { 
		if (name==null) throw new UserException("Cannot SET_RESULT with a null name.", ThingsCodes.ERROR_THING_RESULT_NOT_DEFINED, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);
		if (!resultDefinitions.containsKey(name)) throw new UserException("RESULT not defined.", ThingsCodes.ERROR_THING_RESULT_NOT_DEFINED, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);

		ResultDefinition item = resultDefinitions.get(name);
		item.actual = result; 
	}
	
	/**
	 * Calculate the result based on the defined results and their settings.  There are the following rules:<br>
	 * All results must be PASS for the final result to PASS.<br>
	 * A single ABORT will make the final result an ABORT, otherwise:<br>
	 * A single EXCEPTION will make the final result an EXCEPTION, otherwise:<br>
	 * a single FAIL will make the final result a FAIL, otherwise:<br>
	 * a single INCONCLUSIVE will make the final result a FAIL.<p>
	 * @return the overall result.
	 * @throws UserException
	 */
	public RESULT CALCULATE_RESULT() throws UserException {
		return ResultManager.calculateResult(resultDefinitions.values(), exceptions);
	}

	
	// == PARAMETER MANAGEMENT ==============================================================================================================

	private HashSet<String> requiredProperties = new HashSet<String>();
	
	/**
	 * Require a property.
	 * @param name
	 * @throws UserException
	 */
	public void REQUIRE_PROPERTY(String name) throws UserException {
		
		// Qualify.  Dont' allow nulls and must be a result type.
		if ((name==null)||(name.length()<1)) throw new UserException("Cannot REQUIRE_PROPERTY with a null or empty name.", ThingsCodes.THING_FAULT_DEFINITION_BAD, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);
		
		// Prevent bufoonery.
		if (name.trim().length() != name.length()) throw new UserException("REQUIRE_PROPERTY name has leading or trailing whitespace.", ThingsCodes.THING_FAULT_DEFINITION_BAD, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_THING_RESULT_NAME, name);
		
		// Add
		requiredProperties.add(name);		
	}
	
	/**
	 * Make sure all required properties exist.  If they don't, it'll throw an error exception.
	 * @throws UserException
	 */
	public void CHECK_REQUIRED() throws UserException {
		for (String item : requiredProperties) {
			
			try {
				if (localProperties.getProperty(item)==null) throw new UserException("Required property does not exist.", UserException.ERROR_THING_REQUIRED_PROPERTY_MISSING, ThingsNamespace.ATTR_THING_NAME, GET_NAME(), ThingsNamespace.ATTR_PROPERTY_NAME, item);

			} catch (UserException ue) {
				throw ue;
			} catch (Throwable t) {
				// Serious problem!
				UserException.softwareProblem("localProperties failed in CHECK_REQUIRED.  This is a bug.", t);
			}
			
		} //end for
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == Class Methods
		
	/**
	 * Initialize this THING.
	 * @param si A system interface.  
	 * @param ci Control interface. 
	 * @param ei The parent expression interface.  If null, nothing will express to it.
	 * @throws UserException which will always be a FAULT.
	 */
	public void init(SystemInterface	si, ControlInterface ci, ExpressionInterface	ei, ExpressionInterface parent) throws UserException {
		
		// Any init failure is a fault
		try {
			systemInit(si);
			myExpressionInterface = ei;
			myControlInterface = ci;
			parentInterface = parent;
			runTime = new StopWatch();
			
		} catch (ThingsException te) {
			throw new UserException("Failure to initialize the THING caused a FAULT.", ThingsCodes.SYSTEM_FAULT_THING_FAILED_INIT, te, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
		} catch (Throwable t) {
			throw new UserException("Spurious exception to initialize the THING caused a FAULT.", ThingsCodes.SYSTEM_FAULT_THING_FAILED_INIT, t, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
			
		}
	}
	
	/**
	 * This is the class call point.  This will be invoked by the kernel if it is a root process or another THING through the CALL.  Don't call it directly.
	 * It is not reentrant.
	 * @return the result
	 * @throws UserException
	 */
	public RESULT call_chain() throws UserException, ThingsException, InterruptedException {
		exceptions =  new ThingsExceptionBundle<ThingsException>();
		
		// If debugging, dump the properties
		if (localSystemLogger.debuggingState()) {
			try {
				
				ThingsPropertyView propView = mySystemInterface.getLocalProperties();
				Collection<String> keys = propView.sub(null);
				for (String item : keys) {
					localSystemLogger.debug("Local property.", ThingsCodes.DEBUG_PROPERTY_VALUE, propView.getPropertyNV(item));
				}
				
			} catch (Throwable t) {
				throw new UserException("THING failed in dumping properties.", UserException.SYSTEM_FAULT_THING_FAILED_DEFINITION, t, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
			}
		}
		
		// The definition.  Problems here cannot be handled by the traps and are immeadiately terminal--a single fatal RESULT.
		try {
			DEFINITION();
	
		} catch (Throwable t) {
			
			// Stock result.
			Metrics	myMetrics = new Metrics();
			myMetrics.abort++;
			myResult = new RESULT(myMetrics, Data.Type.ABORT);
			
			throw new UserException("THING failed in definition.", UserException.SYSTEM_FAULT_THING_FAILED_DEFINITION, t, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
		}
		
		// This is so we can catch or propagate ERROR v. FAULT
		try {
			
			// This is for the traps
			try {
				CHECK_REQUIRED();
				runTime.start();
				PROCESS();
				runTime.stop();

			} catch (ThingsException te) {
				
				// Eat orders to halt.  We will definatedly quit at this point.
				if (te.numeric != UserException.PANIC_PROCESS_RESPONDING_TO_HALT_OK) {
					if (te.isWorseThanError()) CATCH_FAULT(te);
					else CATCH_ERROR(te);
				}
				
			} catch (Throwable t) {
				// Allow InterruptedExceptions out.
				if (t instanceof InterruptedException) { 
					throw t;
				} else {	
					CATCH_FAULT(new UserException("Spurious exception during THINGS call.", UserException.SYSTEM_FAULT_THING_SPURIOUS_EXCEPTION, t));
				}
			} 
	
		} catch (ThingsException tte) {
 			exceptions.add(tte);	
		} catch (Throwable t) {
			if (t instanceof InterruptedException) { 
				// This may have been forced by the System, so let it out!
				if (localSystemLogger.debuggingState()) localSystemLogger.debug("Thing was interrupted.");
				
				// We should still try to roll up the result.
				try {
					// Remember, if it is null, we use the system calculated results.
					myResult = CALCULATE_RESULT();
					RESULT stageResult = RESULTS(myResult);
					if (stageResult != null) myResult = stageResult;
					
				} catch (UserException ue) {
					// We really can't say anything about an exception here, since the InterruptedException will tear us down, but we can 
					// at least log something.
					localSystemLogger.shout("Ate a UserException while calculating results during an InterruptedException", ThingsCodes.USER_DEFAULT_INFO, Logger.LEVEL.INFO);				
				}	
				throw (InterruptedException)t;	
			
			} else {	
				UserException.softwareProblem("This should have never happened.", t);
			}
		}
		
		// Always do results
		try  {	

			// Remember, if it is null, we use the system calculated results.
			myResult = CALCULATE_RESULT();
			RESULT stageResult = RESULTS(myResult);
			if (stageResult != null) myResult = stageResult;
			
			// Report results as information.  (remember, only root things automatically report as DATA, unless explicitly done.
			localSystemLogger.info("CALL completed.", ThingsCodes.USER_RESULT_DEFAULT, myResult.getAllAttributes(SystemNamespace.ATTR_THING_NAME, GET_NAME()));
			
		} catch (ThingsException ue) {
			exceptions.add(ue);
			
		} catch (Throwable tt) {
			exceptions.add(new ThingsException("Spurious exception while calculating results.", ThingsException.ERROR_THING_RESULT_SPURIOUS_ERROR, tt));
		}
		
		// Only propagate faults or worse.
		if (exceptions.size() > 0) {
			ThingsException resolvedException = exceptions.resolve();
			if (resolvedException.isWorseThanError()) {
				if (resolvedException instanceof UserException) throw (UserException)resolvedException;
				throw new UserException("Fault in THING.", UserException.SYSTEM_FAULT_THING_FAULT, resolvedException);
			}
			
			// Debug log the whole thing if debugging is on.  Otherwise, just warn.
			try {
				if (localSystemLogger.debuggingState()) localSystemLogger.debug(resolvedException.toStringComplex(), resolvedException.numeric);
				else localSystemLogger.warning(resolvedException.getMessage(),resolvedException.numeric);
			} catch (Throwable t) {
				UserException.softwareProblem("Logging failed in THING while resolving an exception.  This is a bug.", t, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
			}
		}
		
		return myResult;
	}
	
	/**
	 * Get the final result, if available.  Usually, you'd only need to see this if there was a fatal exception.
	 * @return the last result or null if it hasn't been calculated yet.  The method getInterimResult will force them to calculate.
	 */
	public RESULT getResult() {
		return myResult;
	}
	
	/**
	 * Get the interim result, if available.  Usually, you'd only need to see this if there was a fatal exception.
	 * @return the most recent results.
	 * @throws ThingsException for whatever reason.  It may come from the THING's  RESULTS implementation.
	 */
	public RESULT getInterimResult() throws ThingsException {
		try {
			myResult = CALCULATE_RESULT();
			RESULT stageResult = RESULTS(myResult);
			if (stageResult != null) myResult = stageResult;
		} catch (Throwable t) {
			throw new ThingsException("Spurious exception while calculating results.", ThingsException.ERROR_THING_RESULT_SPURIOUS_ERROR, t);
		}
		return myResult;
	}
	
	/**
	 * Get a reference tot he parent expression interface.  This exists because java doesn't have friends.  Gah.  Don't use it.  Only the kernel and result managers will.
	 * @return the parent expression interface.
	 */
	public ExpressionInterface getParentExpressionInterface() {
		return parentInterface;
	}
	
}
