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
 * limitations und-er the License.
 */
package things.thinger.kernel.basic;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.data.Data.Type;
import things.thing.Metrics;
import things.thing.RESULT;
import things.thing.THING;
import things.thing.UserException;
import things.thinger.ExpressionInterface;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.kernel.ThingsProcess;

/**
 * Things Process wrapper for THINGs.  Act as the default expression interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 07
 * </pre> 
 */
public class KernalBasic_THINGProcessWrapper extends ThingsProcess implements ExpressionInterface {

	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * DATA
	
	/**
	 * THING class. 
	 */
	Class<THING> myThingClass;
	
	/**
	 * The actual thing.
	 * @see things.thinger.Thing
	 */
	THING	myThing;
	
	/**
	 * The final result.
	 * @see things.thinger.RESULT
	 */
	RESULT	finalResult;
	
	
	/**
	 * The system logger for this process.
	 * @see things.thinger.io.Logger
	 */
	Logger	myLogger;
	
	/**
	 * Expression interface.
	 */
	ExpressionInterface parentEi;
	
	
	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * METHODS
	
	/**
	 * Set the THING name.  It must have a name.  This implementation will use the class name as the name until the
	 * underlying THING object is created. 
	 * @param thingClass the loaded class to use.
	 * @param parentEi the expression interface.  It can be a parent.  If set to null, the wrapper will provide a expression into the system log.
	 */
	public KernalBasic_THINGProcessWrapper(Class<THING> thingClass, ExpressionInterface  parentEi) {
		myThingClass = thingClass;
		this.parentEi = parentEi;
	}
	
	/**
	 * Get the latest result.  Overrides the basic implementation in the ThingsProcess.
	 * @return the latest or the last result for the thread.  It is completely up to the implementation how to implement this.
	 * @throws ThingsException for whatever reason. It may come from the THING itself.
	 */
	public RESULT getResult() throws ThingsException {
		if (myThing==null) {
			if (finalResult==null) return new RESULT(ThingsCodes.KERNEL_PROCESS_THING_WAITING_START, Metrics.STOCK_Inconclusive, Type.INCONCLUSIVE, SystemNamespace.ATTR_THING_NAME_ACTUAL, myThingClass.getCanonicalName());
			return finalResult;
		}
		return myThing.getInterimResult();
	}
	
	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * ABSTRACT METHODS

	/**
	 * This is the entry point for the actual processing
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException, InterruptedException {
		RESULT theResult = null;
		try {
			
			myLogger.info("Starting as root " + myThing.GET_NAME(), ThingsCodes.USER_THING_MANAGEMENT, SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString(), SystemNamespace.ATTR_THING_NAME, myThing.GET_NAME(), SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName());
			theResult = myThing.call_chain();
			myLogger.shout("Root completed.", ThingsCodes.USER_RESULT_COMPLETE, Logger.LEVEL.DATA,
					 theResult.getAllAttributes(SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString(), SystemNamespace.ATTR_THING_NAME, myThing.GET_NAME()));
			
		} catch (ThingsException te) {
			throw new SystemException("THING died.", SystemException.SYSTEM_FAULT_THING_DIED, te, SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString());
		} catch (InterruptedException ie) {
			throw ie;
		} catch (Throwable t) {
			throw new SystemException("THING died to spurious exception.", SystemException.SYSTEM_FAULT_THING_SPURIOUS_EXCEPTION, t, SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString());			
		} finally {
			
			// If there is no result, we must have hit a problem.  See if we can get from from the THING.
			if (theResult == null) {
				theResult = myThing.getResult();
				if (theResult==null) {
					// Still no result.  Die.
					myLogger.info("Root THING died without any results.", ThingsCodes.USER_RESULT_ERRORED);
				} else {
					try {
						myLogger.info("Root THING died with from results.", ThingsCodes.USER_RESULT_ERRORED, theResult.getAllAttributes());
					} catch (Throwable t) {
						this.screech("Exception while trying to post results from a dying THING.  message=" + t.getMessage());
					}
				}
				
			} // end if no results
			
			// Remember it as the latest.
			super.internalResult = theResult;  // paranoia!
			finalResult = theResult;
			
		} // end try
			
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * @throws things.thinger.SystemException
	 */
	public void constructThingsProcess() throws SystemException {

		// Qualify
		if (myThingClass == null) SystemException.softwareProblem("BUG: Instantiated with a null thingsClass.");
		screech("Constructing a THING: "  + myThingClass.getName() + " processName: " + this.getProcessName() + " processId: " + this.getProcessId().toString());
		if (myThingClass == null) SystemException.softwareProblem("BUG: myThingName is null during constructThingsProcess call.  Did you forget to call instantiate() before constructThingsProcess()?");
		
		// Create
		Object thang = null;
		try {
			// Make instance
			thang = myThingClass.newInstance();
		} catch (Throwable t) {
			throw new SystemException("THING construction failed to spurious exception.", SystemException.SYSTEM_FAULT_THING_CONSTRUCTION_SPURIOUS_EXCEPTION, t, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, myThingClass.getName());	
		}
	
		// Validate.  The null check is because there will be a different class loading mechanism in the future.
		if (thang==null) throw new SystemException("THING construction failed because it yielded a null object.", SystemException.SYSTEM_FAULT_THING_CONSTRUCTION_NULLED, SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName());	
		if (!(thang instanceof THING))  throw new SystemException("THING construction failed because it didn't create a THING object.", SystemException.SYSTEM_FAULT_THING_CONSTRUCTION_BAD_CLASS, SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName(), SystemNamespace.ATTR_SYSTEM_OBJECT_NAME_ACTUAL, thang.getClass().getName());	
		
		// Use it
		try {
			myThing = (THING)thang;
			myLogger = ssi.getSystemLogger();
			myThing.init(ssi, this, this, parentEi);			// Give it the ssi.  We'll let the class protect it for now.
			myLogger.debug("Root THING for this process constructed.", ThingsCodes.USER_THING_MANAGEMENT, SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString(), SystemNamespace.ATTR_THING_NAME, myThing.GET_NAME(), SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName());
			
		} catch (UserException ue) {
			throw new SystemException("THING construction failed because it could not be initialized.", SystemException.SYSTEM_FAULT_THING_CONSTRUCTION_NULLED, ue, SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName());	
		} catch (Throwable t) {
			throw new SystemException("THING construction failed to spurious exception during initialization.", SystemException.SYSTEM_FAULT_THING_CONSTRUCTION_SPURIOUS_EXCEPTION, t, SystemNamespace.ATTR_PLATFORM_CLASS, myThingClass.getName());	
		}

	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 * @throws things.thinger.SystemException
	 */
	public void destructThingsProcess() throws SystemException {
		myThing = null;		// Kill the reference.  Maybe we'll speed up finalization--maybe not.
	}
	
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		if (myThing != null)
			return myThing.GET_NAME();
		else 
			return myThingClass.getName();
	}
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS
	
	/**
	 * The identified resource is in the process of being revoked.  It is still possible for the resource listener to call the resource within the context
	 * of this thread and call.
	 * <p>
	 * @param resourceID the ID of the resource that is being revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * The identified resource bas been revoked.  It is gone.  Attempting to call it would be a very bad thing.  The listener should remove the resource 
	 * from it's internal lists..
	 * <p>
	 * @param resourceID the ID of the resource that has been revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * Get the ID of the listener.
	 * <p>
	 * @return The listener's ID.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getListenerId() {
		return getProcessId();
	}

	// ==========================================================================================================
	// == DEFAULT EXPRESSION INTERFACE IMPLEMENTATION
	
	/**
	 * Express a RESULT.  Reliablility is up to the kernel; you will not get a receipt.
	 * @param theResult The result to express.
	 * @throws SystemException
	 * @see things.thing.RESULT
	 */
	public void expressResult(RESULT theResult) throws SystemException {
		try {
			myLogger.shout("RESULT", theResult.getNumeric(), Logger.LEVEL.DATA,
				 theResult.getAllAttributes(SystemNamespace.ATTR_PROCESS_ID, this.getProcessId().toString(), SystemNamespace.ATTR_THING_NAME, myThing.GET_NAME()));		
			myLogger.flush();

		} catch (ThingsException te) {
			throw new SystemException("Failed THING expression.", ThingsException.ERROR_THING_EXPRESSION_DEFAULT_ERROR, te);
		}
	}
	
}
