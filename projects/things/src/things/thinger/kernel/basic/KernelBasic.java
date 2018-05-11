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
package things.thinger.kernel.basic;

import java.io.PrintWriter;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.common.impl.WhoAmISimple;
import things.data.NV;
import things.data.NVImmutable;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;
import things.data.impl.ThingsPropertyTreeRAM;
import things.data.tables.Table;
import things.thing.MODULE;
import things.thing.THING;
import things.thinger.ExpressionInterface;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.SystemSuperInterface;
import things.thinger.io.FileSystemLocator;
import things.thinger.io.Logger;
import things.thinger.io.Logger.TYPE;
import things.thinger.io.conduits.ConduitController;
import things.thinger.kernel.Clearance;
import things.thinger.kernel.PCB;
import things.thinger.kernel.ProcessInterface;
import things.thinger.kernel.ResourceManager;
import things.thinger.kernel.ThingsProcess;
import things.thinger.kernel.ThingsState;
import things.universe.Universe;
import things.universe.UniverseAddress;

/**
 * Implements a system interface for the basic Kernel.<p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 12 JUN 05
 * EPG - Split base and main - 6 FEB 06
 * </pre> 
 */
public class KernelBasic extends KernelBasicBase {

	// ==========================================================================================
	// PRIVATE DATA
	
	// ==========================================================================================
	// STATIC CONFIGURATION
	
	// ==========================================================================================
	// CONSTRUCTION
	public KernelBasic() throws SystemException {
		super();
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// SUPER SYSTEM INTERFACE
	
	/**
	 * Get system global property view
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getGlobalProperties() throws SystemException {
		return myGlobalPropertiesViewRoot;
	}
	
	/**
	 * Get user global property view. 
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getUserGlobalProperties() throws SystemException {
		return userGlobalPropertiesView;
	}
	
	/**
	 * Get shared property view for this server.  Anyone can read and write to them.  
	 * @return a property view
	 * @throws things.thinger.ThingsException
	 */
	public ThingsPropertyView getSharedProperties() throws ThingsException {
		return sharedGlobalPropertiesView;
	}
	
	/**
	 * Get the configuration properties that are writable.  Anything that has access to the SSI can touch these.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getConfigPropertiesWritable() throws SystemException {
		return userGlobalConfigView;
	}
	
	/**
	 * Get local property view for the given  id.  
	 * @param id String id of the process.
	 * @return a property view or null if the id doesn't identify any known process.
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties(String id) throws SystemException {
		try {
			PCB process = processes.getProcess(id);
			return process.getMyLocalProperties();
		} catch (Throwable t) {
			if (kernelLogger.debuggingState()) kernelLogger.debug("Someone asked for getLocalProperties(id) for an unknown process.", SystemException.PROCESS_ERROR_NOT_FOUND, SystemNamespace.ATTR_PROCESS_ID, id);
			return null;
		}
	}
	
	/**
	* Ask the server to quit.  This version will cause a rapid forced halt of the kernel.  It will try to stop the services.
	 * It will not let any SystemException
	 *  escape.
	 */
	public void requestQuit() {
		try {
			this.forceHalt();
		} catch (SystemException se) {
			bootstrapLogger.postit("Bad thing happened while a requestQuit() called.");
			bootstrapLogger.postit(se.toStringComplex());
		}
	}
	
	/**
	 * Typically, this is how a process will tell the kernel it
	 * is dying, so that the kernel can clear resources.  This really should be the LAST thing a process
	 * does before exiting run().  If can be used instead of flingException.
	 * @param te a Things exception that indicates the reason for the death.  It may be null if it was normal termination.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void deathNotice(ThingsException te) {
		
		try {

			// Add to the deathlist.
			WhoAmI candidate = processes.callerId();
			deathList.add(processes.callerId());
			
			// Log the exceptions.  If we have kernel logger, use it.  Otherwise try the bootstrap.
			if (te != null) {
				if (kernelLogger==null) {
					bootstrapLogger.post("Exception while boot failed.\n"  + te.toStringComplex());
					bootstrapLogger.flush();		
				} else {	
					kernelLogger.info("Exception while finalizing a dying thread.", ThingsCodes.KERNEL_PROCESS_FINALIZATION, (NVImmutable[])te.getAttributesNVMulti( new NV(SystemNamespace.ATTR_PLATFORM_MESSAGE,te.getMessage()), new NV(SystemNamespace.ATTR_PROCESS_ID ,candidate.toString()) ));
					kernelLogger.flush();
				}
				flingException(te);
			}
			
		} catch (Throwable t) {
			try {
				kernelLogger.error("Exception while processing a death notice.", SystemException.KERNEL_ERROR_DEATH_NOTICE_FAILED, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
			} catch (SystemException ssee) {
				bootstrapLogger.postit("Failed to log an exception in deathNotice.  This must be bad.  " + ssee.toString());
			}
		}
	}
	
	/**
	 * Typically, this is a last ditch way for a process or
	 * module to pass info to the kernel when something very
	 * bad is happening.  There is no feedback loop.
	 * @param te a Things exception
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void flingException(ThingsException te) {
		
		// ID the caller
		WhoAmI candidate = null;
		
		// Manage the exception
		try {
			candidate = processes.callerId();
			
			// Use kernellogger if the kernel is actually up, otherwise use the bootstraplogger.
			if (kernelLogger==null) {
				bootstrapLogger.post("Process is reporting an exception during Kernel startup.\n"  + te.toStringComplex());
				
			} else {
				kernelLogger.error("Process is reporting an exception.", te.numeric, 
						(NVImmutable[])te.getAttributesNVMulti( new NV(SystemNamespace.ATTR_PLATFORM_MESSAGE, te.getMessage()), new NV(SystemNamespace.ATTR_PROCESS_ID ,candidate.toString()) ));
				kernelLogger.debug("....... dead process exception as follows.",te.numeric,
								   new NV(SystemNamespace.ATTR_PROCESS_ID, candidate.toString()),
						           new NV(SystemNamespace.ATTR_PLATFORM_TRACE, te.toString())  );
			}
		} catch (Throwable t) {
			if (kernelLogger == null) {
				System.out.println("PANIC: VERY BAD THING.  Kernel died while logging during startup.  Printing trace:\n");
				t.printStackTrace();
			} else {
				kernelLogger.postit("PANIC: VERY BAD THING.  Kernel encountered an exception while trying to handle process terminal exception.  te=" + te + " id=" + candidate);
				kernelLogger.postit("PANIC: Thread info.  " + Thread.currentThread().toString());
				if (candidate != null) kernelLogger.postit("PANIC: Process id=" + candidate.toString());
				if (te != null) kernelLogger.postit("PANIC: Message=" + te.getMessage());
			}
		}
	}
	
	/** 
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, constructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * <br>
	 * All processes started with this will have DEFAULT_USER_CLEARANCE.
	 * <p>
	 * @param processObject This will be a ThingsProcess or subclass.
	 * @param properties These are properties to add (or supplant) to the processes specific view before starting the process.  It is ok
	 * to pass null if there are none.
	 * @throws Throwable
	 * @return the ID of the started process.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties) throws Throwable {
		return startProcess(processObject, properties, DEFAULT_USER_CLEARANCE);
	}
		
	/**
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, constructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * <p>
	 * @param processObject This will be a ThingsProcess or subclass.
	 * @param properties These are properties to add (or supplant) to the processes specific view before starting the process.  It is ok
	 * to pass null if there are none.
	 * @param processClearance specify the process clearance level.  This must be at or lower than the calling process's clearance.
	 * @throws Throwable
	 * @return the ID of the started process.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties, Clearance	processClearance) throws Throwable {

		// Make sure the calling process has the clearance to create another process of the same priv.
		processes.requireClearance(processClearance, "startProcess_clearance");
		
		// Instantiate the service
		int	pin = pcbNumber.getAndIncrement();
		WhoAmI thisProcessId = userSpaceID.birthMyChild(PROCESS_ID_STRING_PREFIX + pin + ID_SEPERATOR + processObject.getProcessName(), PROCESS_ID_TAG_PREFIX + pin);
		
		// Get a logger first, so we can use it during process init.
		// The log forger already put it into the ResourceCoordinator for us.
		// Be sure to add it to the cache so calls to getSystemLogger will yield it.
		String path =  KernelBasic_NamingReactors.rLOGGER_LOCATION(thisProcessId);
		ResourceManager rmLogger = forgeLogger(thisProcessId,systemFilesystem, path);
		Logger theLogger = (Logger)rmLogger.getResource();
		theLogger.setPostLevel(config_LOGGING_LEVEL);
		if (verbose) {
			processObject.verboseOn(theLogger);
		}
		loggerCache.put(thisProcessId.toString(), theLogger);
		loggerFileMap.put(thisProcessId.toString(), new FileSystemLocator(systemFilesystem, path));
		
		// Get the local properties.  It will be a snapshot of the user branch.  It will be destroyed when the process dies by the kernel.
		String desinationBranch = ThingsPropertyReaderToolkit.path(USER_ROOT_PROCESS_SPACE, thisProcessId.toTag());
		myGlobalPropertiesTree.copyAndGraftBranch(USER_ROOT_GLOBAL_SPACE, desinationBranch);
		ThingsPropertyView localView = myGlobalPropertiesTree.getRoot().cutting(desinationBranch);
		cullPropsOnDeathMap.put(thisProcessId,desinationBranch);
		if (properties!=null) localView.graft(properties);			// Graft on any new props.
		
		// Fix the process
		processObject.fix(thisProcessId);
		
		// ID it as a process, this must happen before the init or the kernel may not let it have access to resources.
		processes.registerProcess(processObject, processClearance, localView, userGlobalConfigView);
		
		// Init the service through the process
		processObject.init(this);
		processObject.release();
		
		// Add the process as a listener to the logger
		rmLogger.addListener(processObject);
						
		// DONE
		bootstrapLogger.post("Process " + processObject.getName() + " started with ID:" + thisProcessId.toString());		
		return thisProcessId;
	}
	
	/**
	 * Register a ready-made PCB.  It'll allows you to set the clearance level.  The
	 * kernel may choose to deny the operation.  So normally, use startProcess instead.
	 * This exists mostly for debugging and testing.
	 * <br>
	 * @param processPCB This will be a ready-made PCB.
	 * @param processClearance The clearance level.  This will be immutable.
	 * @throws things.common.ThingsException
	 */
	public void registerProcess(PCB processPCB, Clearance		processClearance) throws ThingsException {
		
		// Get a logger first, so we can use it during process init.
		// The log forger already put it into the ResourceCoordinator for us.
		// Be sure to add it to the cache so calls to getSystemLogger will yield it.
		WhoAmI thisProcessId = processPCB.getProcess().getProcessId();
		ResourceManager rmLogger = forgeLogger(thisProcessId,systemFilesystem, KernelBasic_NamingReactors.rLOGGER_LOCATION(thisProcessId));
		Logger theLogger = (Logger)rmLogger.getResource();
		theLogger.setPostLevel(config_LOGGING_LEVEL);
		if (verbose) {
			processPCB.getProcess().verboseOn(theLogger);
		}
		loggerCache.put(thisProcessId.toString(), theLogger);
		
		// Register it.  DO not init or release it.
		processes.registerProcess(processPCB, processClearance);
		
		// Add the process as a listener to the logger
		rmLogger.addListener(processPCB.getProcess());
		
		// DONE
		bootstrapLogger.post("Process " + processPCB.getProcess().getName() + " started with ID:" + thisProcessId.toString());	
		
	}
	
	/**
	 * Get the system conduit controller.  These are for conduits between privileged services.
	 * @return a ConduitController
	 * @throws things.thinger.SystemException
	 * @see things.thinger.io.conduits.ConduitController
	 */
	public ConduitController getSystemConduits() throws SystemException {
		return systemConduits;
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// SYSTEM INTERFACE
	
	/**
	 * Get a system logger for the process.<br>
	 * This is implemented with the numbered call GET_SYSTEM_LOGGER.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.SystemException
	 */
	public Logger getSystemLogger() throws SystemException {
		Logger result = null;
		
		// Get ID and validate.
		WhoAmI caller = processes.requireClearance(Clearance.PUBLIC,"getSystemLogger");
		try {	
			result = loggerCache.get(caller.toString());
		} catch (Throwable t) {
			throw new SystemException("Failed to getSystemLogger.",SystemException.SYSTEM_CALL_ERROR_GET_SYSTEM_LOGGER,t);
		}
		
		// If the result is null, that means there is no logger for this process.  That is VERY bad.
		if (result == null) throw new SystemException("No logger available for this process.", ThingsException.PANIC_SYSTEM_KERNEL_NO_LOGGER_FOR_PROCESS, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, caller.toString());
		
		return result;
	}
	
	/**
	 * Forge a new named logger.  If a logger already exists for that name, it may cause an exception, depending on how it resolves.
	 * <br>
	 * KernelBasic treats the name as a universe address and will log to a file at that location.
	 * <br>
	 * @param name the name.  It will be unique.  It's up to kernel on how the name is resolved.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.SystemException
	 */
	public Logger getNamedLogger(String name) throws SystemException {
		Logger result = null;
		
		// Get ID and validate.
		WhoAmI caller = processes.requireClearance(Clearance.PUBLIC,"getNamedLogger");
		WhoAmI proposedId = new WhoAmISimple(name);
		
		try {	
			if (name==null) throw new Exception("Name is null.");
			if (loggerCache.containsKey(proposedId)) throw new Exception("Named logger already exists.");
			
			UniverseAddress uAddy = new UniverseAddress(name);
			Universe pageUniverse = getUniverse(uAddy.universeName);
			String realId = KernelBasic_NamingReactors.rLOGGER_NAMED_RESOURCE_ID(name);
			
			// Build it
			KernelBasic_WriterLogger candidate =  new KernelBasic_WriterLogger();
			PrintWriter pow = new PrintWriter( pageUniverse.putStream(uAddy.path)  );	
			candidate.init(pow, caller, config_LOGGING_LEVEL);
			candidate.init(TYPE.BROADCAST);
			
			// Put it under management.
			resources.registerResource(candidate, realId);		
			loggerCache.put(proposedId.toString(), null);
			
			// Last thing -- make the candidate it
			result = candidate;
			
		} catch (Throwable t) {
			throw new SystemException("Failed to getNamedLogger.",ThingsException.SYSTEM_CALL_ERROR_GET_SYSTEM_LOGGER,t);
		}
		
		// If the result is null, that means there is no logger for this process.  That is VERY bad.
		if (result == null) throw new SystemException("No logger available for the getNamedLogger request.", ThingsException.PANIC_SYSTEM_KERNEL_NO_LOGGER_FOR_PROCESS, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, caller.toString());
		
		return result;
		
	}
	
	/**
	 * Forge a new named expressor.
	 * <br>
	 * @param name the name.  Generally, it should be unique.  It's up to kernel on how the name is resolved and if name reuse is allowed.
	 * @return an expression interface.
	 * @see things.thinger.ExpressionInterface
	 * @throws things.thinger.SystemException
	 */
	public ExpressionInterface getNamedExpressor(String name) throws SystemException {
		ExpressionInterface result = null;
		try {	
			result = new KernalBasic_LoggingExpressor(getNamedLogger(name), this);
			
		} catch (Throwable t) {
			throw new SystemException("Failed to getExpressionInterface.",ThingsException.SYSTEM_CALL_ERROR_GET_EXPRESSOR,t);
		}
		return result;	
	}
	
	/**
	 * Get a local reference to the log if possible.  This is totally up to the implementation.  It may be the whole log, a snippet, or null (no log at all).
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @param id String id of the process.
	 * @return log file locator or null
	 * @see things.thinger.kernel.ProcessInterface
	 * @see things.thinger.io.FileSystemLocator
	 * @throws things.thinger.SystemException for general errors or InterruptedException for thread control.  Always let the InterruptedException out.
	 */
	public FileSystemLocator getLogLocal(String id) throws SystemException, InterruptedException {
		FileSystemLocator result = null;
		try {	
			
			// Right now, only system loggers per process can be found.  We only get local for WriterLoggers for KB.  They are file based.
			if (loggerFileMap.containsKey(id)) {
				result = loggerFileMap.get(id);
			}
			// end return null because it isn't there.
			
		} catch (Throwable t) {
			throw new SystemException("Failed to getLogLocal.",ThingsException.SYSTEM_CALL_ERROR_GET_LOCAL_LOG,t);
		}

		if (result == null) throw new SystemException("No logger available for this process.", SystemException.PANIC_SYSTEM_KERNEL_NO_LOGGER_FOR_PROCESS, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, id);
		
		return result;
	}
	
	/**
	 * Load a thing but don't run it.  It will only construct.  It's up to you initialize and call it.   Typically, the user should
	 * call .init() and then .call_chain().  This is mostly so that THINGs can calll other THINGs, so perhaps it is best to 
	 * just use THING.CALL instead--if you can.
	 * @return The constructed thing.
	 * @throws things.thinger.SystemException
	 */
	public THING loadThing(String name) throws SystemException {
		THING result = null;
		
		try {	
			if (kernelLogger.debuggingState()) kernelLogger.info("Attempting to load thing " + name);
			
			// Get ID and validate.
			processes.requireClearance(Clearance.PUBLIC,"loadThing");
			Class<THING> classToUse = loader.loadThing(name);
			result = classToUse.newInstance();
			
			kernelLogger.debug("Loading a THING successful.", ThingsCodes.DEBUG_THING_LOADED, ThingsNamespace.ATTR_THING_NAME, name, ThingsNamespace.ATTR_PLATFORM_CLASS, name);
		} catch (Throwable t) {
			if (kernelLogger.debuggingState()) kernelLogger.info("Loading a THING FAILED.", ThingsCodes.SYSTEM_CALL_ERROR_LOAD_THING, ThingsNamespace.ATTR_THING_NAME, name, ThingsNamespace.ATTR_PLATFORM_CLASS, name);
			throw new SystemException("Failed to loadThing.",SystemException.SYSTEM_CALL_ERROR_LOAD_THING,t);
		}
		return result;
	}
	
	/**
	 * Load a module but don't do anything with it.  It will only construct.  It's up to the user to initialize it. 
	 * @return The constructed MODULE
	 * @param name the name that the loader can use to find it.  Typically, the full class name.
	 * @throws things.thinger.SystemException
	 */
	public MODULE loadModule(String name) throws SystemException {
		MODULE result = null;
		
		try {	
			if (kernelLogger.debuggingState()) kernelLogger.info("Attempting to load module " + name);
			
			// Get ID and validate.
			processes.requireClearance(Clearance.PUBLIC,"loadModule");
			Class<MODULE> classToUse = loader.loadModule(name);
			result = classToUse.newInstance();
			result.init(this);
			
			kernelLogger.debug("Loading a MODULE successful.", ThingsCodes.DEBUG_THING_LOADED, ThingsNamespace.ATTR_THING_NAME, name, ThingsNamespace.ATTR_PLATFORM_CLASS, name);
		} catch (Throwable t) {
			throw new SystemException("Failed to loadModule.",SystemException.SYSTEM_CALL_ERROR_LOAD_MODULE,t);
		}
		return result;
	}
	
	/**
	 * Load and run a thing in a new process.
	 * @param name The name of the thing.  The KernalBasic implementation will assume the name is the same as the 
	 * full class name.  If it cannot find the class in the path, it will fail.
	 * @return The process id of the THING.
	 * @throws things.thinger.SystemException
	 */
	public String runThing(String name) throws SystemException {
		return runThing(name, null);
	}
	
	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.
	 * @return The process id of the THING.. 
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.
	 * @throws things.thinger.SystemException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor) throws SystemException {
		return runThing(name, parentExpressor, null);
	}
	
	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.  This will let you add properties to the THING's view before it starts.
	 * @return The process id of the THING.
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.  Set to null if there is no parent.
	 * @param properties properties to add to the THING processes specific view.
	 * @throws things.thinger.SystemException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor, ThingsPropertyView	properties) throws SystemException {

		String result = null;
				
		try {	
			if (kernelLogger.debuggingState()) kernelLogger.info("Attempting to run thing " + name);
			
			// Get ID and validate.
			processes.requireClearance(Clearance.PUBLIC,"runThing");
			Class<THING> classToUse = loader.loadThing(name);
			KernalBasic_THINGProcessWrapper thingWrapper = new KernalBasic_THINGProcessWrapper(classToUse, parentExpressor);
			String thingName = thingWrapper.getProcessName();
			WhoAmI resultId = this.startProcess(thingWrapper, properties);
			result = resultId.toString();
			
			kernelLogger.info("Running a THING successful.", ThingsCodes.KERNEL_PROCESS_THING_STARTED, SystemNamespace.ATTR_PROCESS_ID, result, ThingsNamespace.ATTR_THING_NAME, thingName, ThingsNamespace.ATTR_PLATFORM_CLASS, name);
		} catch (Throwable t) {
			throw new SystemException("Failed to runThing.",SystemException.SYSTEM_CALL_ERROR_RUN_THING,t);
		}
		return result;
	}
	
	/**
	 * Get local property view for the caller only.  
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties() throws SystemException {
	
		ThingsPropertyView result = null;
		
		try {
			// Get ID and validate.
			WhoAmI caller = processes.requireClearance(Clearance.PUBLIC, "getLocalProperties");
			
			// Get the properties.
			PCB process = processes.getProcess(caller);
			result = process.getMyLocalProperties();
		
		} catch (Throwable t) {
			throw new SystemException("Failed to getLocalProperties.",SystemException.SYSTEM_CALL_ERROR_GET_LOCAL_PROPERTIES,t);
		}
		
		return result;
	}
	
	/**
	 * Get the read only properties for this for the caller only.  
	 * @return a property view reader
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyViewReader getConfigProperties() throws SystemException {
	
		ThingsPropertyViewReader result = null;
		
		try {
			// Get ID and validate.
			WhoAmI caller = processes.requireClearance(Clearance.PUBLIC, "getConfigProperties");
			
			// Get the properties.
			PCB process = processes.getProcess(caller);
			result = process.getMyConfigProperties();
		
		} catch (Throwable t) {
			throw new SystemException("Failed to getConfigProperties.",SystemException.SYSTEM_CALL_ERROR_GET_CONFIG_PROPERTIES,t);
		}
		
		return result;
	}
	
	/**
	 * Get the process list.<p>
	 * The process list will be a Table.
	 * @return A table representing the process list.
	 * @see things.data.tables.Table
	 * @throws things.thinger.SystemException
	 */
	public Table<String> getProcessList() throws SystemException {
		Table<String> theTable = new Table<String>();
		processes.dumpProcessTable(theTable);
		return theTable;
	}
	
	/**
	 * Get the state of a specific process.<p>
	 * If the process is not found, the state is ProcessInterface.ThingsState.STATE_INVALID.
	 * @param id String id of the process.
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @see things.thinger.kernel.ProcessInterface
	 * @return The state.
	 * @throws things.thinger.SystemException
	 */
	public ThingsState getProcessState(String id) throws SystemException {
		ThingsState result = ThingsState.STATE_INVALID;
		try {
			PCB processContainter = processes.getProcess(id);
			result = processContainter.getProcess().getCurrentState();
			
		} catch (SystemException se) {
			// Trap the process not found.  It'll let the STATE_INVALID fall through.
			if (se.numeric!= SystemException.PROCESS_ERROR_NOT_FOUND) throw se;
		}
		return result;
	}
	
	/**
	 * Get a process interface.  You can only get a process of equal or less clearance.
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @return The interface
	 * @throws things.thinger.SystemException
	 */
	public ProcessInterface getProcessInterface(String id) throws SystemException {
		
		ProcessInterface result = null;
		
		try {
			
			// Get the properties.
			PCB process = processes.getProcess(id);
			
			// Get ID and validate.  I have a feeling there is a big fat security hole here.  
			processes.requireClearance(process.getClearance(), "access process interface");
			
			// Get the properties.
			result = process.getProcess();
		
		} catch (Throwable t) {
			throw new SystemException("Failed to getConfigProperties.",SystemException.SYSTEM_CALL_ERROR_GET_CONFIG_PROPERTIES,t);
		}
		
		return result;
	}
	
	/**
	 * Wait until the named process if done (meaning any state that satisfies ProcessInterface.ThingsState.isDeadOrDying()==true).<p>
	 * If the process is not found, it will quietly return.
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @throws things.thinger.SystemException for general errors or InterruptedException for thread control.  Always let the InterruptedException out.
	 */
	public void waitProcessDone(String id) throws SystemException, InterruptedException {
		
		ThingsState state = ThingsState.STATE_INVALID;
		try {
			PCB processContainter = processes.getProcess(id);
			state = processContainter.getProcess().getCurrentState();
			while (!state.isDeadOrDying()) {
				synchronized(deathWatchMonitor) {
					deathWatchMonitor.wait();
				}		
			}
			
		} catch (SystemException se) {
			// Quietly let it go.
			if (se.numeric == SystemException.PROCESS_ERROR_NOT_FOUND) return;
			else throw se;
		}
	}
	
	/**
	 * Ask the kernel for a SuperSystemInterface.  If you can't have it, you'll get a SystemException.  Generally, only services are allowed to have it.
	 * <p>
	 * For KernelBasic, anything at or above CLEARANCE.PRIVILEGED can have the supersystem interface, which is most services.
	 * @return The super system interface.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.SystemSuperInterface
	 */
	public SystemSuperInterface requestSuperSystemInterface() throws SystemException {
		processes.requireClearance(Clearance.PRIVILEGED, "requestSuperSystemInterface");
		return this;
	}

	/**
	 * Get a universe by the local name.  (This is the local name in the registry.)  It will make sure the requestor has sufficient 
	 * clearance to get it. 
	 * <p>
	 * @param name the local name for the universe
	 * @return The universe.  
	 * @throws things.thinger.SystemException
	 * @see things.universe.Universe
	 */
	public Universe getUniverse(String name) throws SystemException {
		// Qualify
		if (name==null) throw new SystemException("Local name cannot be null for getUniverse(name).", SystemException.UNIVERSE_FAULT_NAMING_FAILED);
		
		// Get it
		Universe result = null;
		Clearance requiredClearance = Clearance.UNKNOWN;
		try {
			result	= universeRegistry.getAccessor(name);
			requiredClearance = universeRegistry.getClearance(name);
		} catch (Throwable t) {
			throw new SystemException("Fault while getting Universe.", SystemException.UNIVERSE_FAULT_COULD_NOT_ACCESS, t, SystemNamespace.ATTR_UNIVERSE_NAME, name);
		}
		
		// Check
		if (result==null) throw new SystemException("Universe is not registered.", SystemException.UNIVERSE_FAULT_NAMING_FAILED, SystemNamespace.ATTR_UNIVERSE_NAME, name);
		processes.requireClearance(requiredClearance, "Access Universe.");
		
		return result;
	}
	
	/**
	 * Get process ID for the calling prosess. 
	 * <p>
	 * @return The ID.
	 * @throws things.thinger.SystemException
	 */
	public WhoAmI getCallingProcessId() throws SystemException {
		WhoAmI caller = processes.requireClearance(Clearance.PUBLIC, "getId"); 
		return caller;
	}

	/**
	 * Get an empty tree using the preferred, non-persistent implementation for the local host.
	 * @return a new property tree.  
	 */	
	public ThingsPropertyTree getLocalPropertiesImplementation() throws ThingsException {
		return new ThingsPropertyTreeRAM();
	}
	
}
