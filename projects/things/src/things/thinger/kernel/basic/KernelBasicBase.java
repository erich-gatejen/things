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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import things.common.StringPoster;
import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.common.impl.WhoAmISimple;
import things.common.tools.DeadDropRendezvous;
import things.common.tools.Plato;
import things.common.tools.StoplightMonitor;
import things.data.NV;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTreeRAM;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.SystemSuperInterface;
import things.thinger.io.AFileSystem;
import things.thinger.io.FileSystemLocator;
import things.thinger.io.Logger;
import things.thinger.io.Logger.LEVEL;
import things.thinger.io.conduits.basic.BasicConduitController;
import things.thinger.io.fs.FSFileSystem;
import things.thinger.kernel.Clearance;
import things.thinger.kernel.Loader;
import things.thinger.kernel.PCB;
import things.thinger.kernel.ProcessCoordinator;
import things.thinger.kernel.ResourceCoordinator;
import things.thinger.kernel.ResourceManager;
import things.thinger.kernel.ThingsProcess;
import things.thinger.service.Service;
import things.universe.Universe;
import things.universe.UniverseAddress;
import things.universe.UniverseRegistry;
import things.universe.server.UniverseRegistry_Simple;

/**
 * Implements underlying Kernel functionality.<p>
 * - Bootstrap a property set for global properties.<br>
 * - Start a list of services.<br>
 * <p>
 * There are a set of required properties during bootstrap.<br><pre>
 * USER_FILESYSTEM_ROOT
 * SYSTEM_FILESYSTEM_ROOT
 * LOGGING_LEVE
 * <p>
 * User property prunes are separated by their processId.toTag() in their paths.
 * <p>
 * It does not implement the System Interfaces.
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
public abstract class KernelBasicBase extends ThingsProcess implements SystemSuperInterface, KernelBasic_Constants {

	// ==========================================================================================
	// PRIVATE DATA
	
	// Properties
	protected ThingsPropertyTree					myGlobalPropertiesTree;
	protected ThingsPropertyView					myGlobalPropertiesViewRoot;
	protected ThingsPropertyReaderToolkit			myGlobalPropertiesKit;
	protected ThingsPropertyView					userGlobalPropertiesView;	
	protected ThingsPropertyView					userGlobalConfigView;		// Get the local configuration properties.
	protected ThingsPropertyView					sharedGlobalPropertiesView;	// Get the local configuration properties.
	 
	// Services
	protected HashMap<String,Service> 				loadedServiceList;
	//private Tender<String,String>					cliTransactionInterface;
	
	// Process stuff
	protected ProcessCoordinator					processes;
	protected AtomicInteger							pcbNumber;
	protected boolean								verbose = false;
	protected List<WhoAmI>							deathList;				// List of process ids that are dying.  Only the runtime() is allowed to pull anything off the list.
	protected HashMap<WhoAmI, String>				cullPropsOnDeathMap;	// Map of process ids that should have their local properties culled on death.  ID and the root to cull.  Only happen to processes and not services.

	// Kernel internal
	protected StringPoster							bootstrapLogger;
	protected KernelBasic_WriterLogger				kernelLogger;
	protected WhoAmI 								kernelSpaceID;
	protected WhoAmI 								userSpaceID;
	protected DeadDropRendezvous<Throwable> 		startupRendezvous;
	protected StoplightMonitor 						kernelStoplight;
	protected Loader								loader;
	
	// Configuration
	protected String								config_USER_FILESYSTEM_ROOT;
	protected String								config_SYSTEM_FILESYSTEM_ROOT;
	protected LEVEL									config_LOGGING_LEVEL;
	protected KernelBasic_WriterLogger_Factory		config_LOGGING_FACTORY_IMPLEMENTATION;
	
	// General stuff
	protected ResourceCoordinator					resources;
	protected BasicConduitController				systemConduits;
	protected Object								deathWatchMonitor;			// Used for things that want to know when the death watch process is run by the kernel thread.
	
	// The user and system filesystems
	protected AFileSystem							userFilesystem;
	protected AFileSystem							systemFilesystem;
	
	// Universe
	protected UniverseRegistry 						universeRegistry;
	protected Universe								systemUniverse;			// Default system universe
	protected Universe								userUniverse;			// Default user universe

	// ==========================================================================================
	// SYSTEM THINGS TO REMEMBER
	protected HashMap<String,Logger> 				loggerCache;
	protected HashMap<String,FileSystemLocator> 	loggerFileMap;		// Remember what loggers map to files.	
	protected HashMap<WhoAmI,Logger>				localPropertyCache;
	
	// ==========================================================================================
	// STATIC CONFIGURATION
	
	// VALUES
	protected final static int START_PROCESSED_ID  = 1;
	
	// ==========================================================================================
	// CONSTRUCTION
	public KernelBasicBase() throws SystemException {
		super();
		try {	
			loadedServiceList = new HashMap<String,Service>();
			kernelSpaceID = new WhoAmISimple(KERNEL_ID_STRING,KERNEL_ID_TAG);
			userSpaceID = new WhoAmISimple(USERSPACE_ID_STRING,USERSPACE_ID_TAG);
			pcbNumber = new AtomicInteger(START_PROCESSED_ID);
			loggerCache = new HashMap<String,Logger>();
			loggerFileMap = new HashMap<String,FileSystemLocator>();
			startupRendezvous = new DeadDropRendezvous<Throwable>();
			kernelStoplight  = new StoplightMonitor();
			deathList = new LinkedList<WhoAmI>();
			cullPropsOnDeathMap = new HashMap<WhoAmI, String>();				
			universeRegistry = new UniverseRegistry_Simple();
			deathWatchMonitor = new Object();
		} catch (Throwable t) {
			throw new SystemException("Fatal spurious exception while constructing KernelBasicBase.", ThingsException.PANIC_SYSTEM_STARTUP_KERNEL_CONSTRUCTION_FAILURE, t);
		}
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// EXPOSED KERNEL PROCESSES
	
	/**
	 * BOOTSTRAP.  This will start the kernel process.  It will not return until the Kernel quits.
	 * @param verboseMode the verbosity mode for system internal messages.  The default is false.
	 * @param bootstrapLoggerPoster String poster for low-level system logging.
	 * @param globalPropertiesTree A view to global properties.
	 */
	public void bootstrap(boolean verboseMode, StringPoster	bootstrapLoggerPoster, ThingsPropertyTree globalPropertiesTree) throws SystemException {

		// Logging faculties for the bootstrap.
		bootstrapLogger = bootstrapLoggerPoster;
		verbose = verboseMode;
		
		// ----SET UP DATA AND PROCESS ---------------------------------------------------------------
		try {
			bootstrapLogger.post("Begin bootstrap.");						
			
			// Prime it the properties
			myGlobalPropertiesTree = globalPropertiesTree;
			myGlobalPropertiesViewRoot = myGlobalPropertiesTree.getRoot();
			userGlobalPropertiesView = myGlobalPropertiesViewRoot.cutting(USER_ROOT_GLOBAL_SPACE);
			sharedGlobalPropertiesView = myGlobalPropertiesViewRoot.cutting(SHARED_ROOT_GLOBAL_SPACE);
			
			// Get the local configuration properties.
			userGlobalConfigView = myGlobalPropertiesTree.getRoot().cutting(USER_ROOT_CONFIGURED_SPACE);
			
			// Managers and coordinators
			resources = new ResourceCoordinator(kernelSpaceID);
			processes = new ProcessCoordinator(kernelSpaceID);
			
			// MAKE THE KERNEL A FIRSTCLASS PROCESS
			this.fix(kernelSpaceID);	
			processes.registerProcess(this, Clearance.EXTREME,  myGlobalPropertiesViewRoot, myGlobalPropertiesViewRoot);
			this.init(this);
			
			// And start it and wait for the rendezvous.  Any throwable will be propagated as a SystemException.
			bootstrapLogger.post("Starting multiprocessing kernel.");
			this.release();
			Throwable kernelResult = startupRendezvous.enter();
			if (kernelResult != null) throw kernelResult;
			
			// Wait for the kernel to exit
			kernelResult = startupRendezvous.enter();
			if (kernelResult != null) throw kernelResult;

		} catch (SystemException se) {
			throw new SystemException("PANIC in Kernel.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, se);
		} catch (Throwable t) {
			throw new SystemException("PANIC in Kernel.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, t);		
		} finally {
			bootstrapLogger.postit("The kernel has stopped.");		
		}
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// INTERNAL KERNEL PROCESSES
	
	/**
	 * The Kernel thread runtime.  It should let a exception out only if something real bad happened.  It could get a thread-interrupted
	 * exception at any time and that should me considered normal.
	 * @throws things.thinger.SystemException
	 */
	private void runtime() throws SystemException {

		// What we will pass back through the rendezvous if there is a problem.  It starts as null, which assumes all is A-OK.
		Throwable resultThrowable = null;
		
		// Loop until something makes us leave.
		while(!getCurrentState().isHalting()) {
		
			try {
				
				// Pause during each iteration
				sleep(KERNEL_RUNTIME_SLEEP_INTERVAL);
				
				//  ---- Death watch -------------------------------------------------------------------------
				if (deathList.size() > 0) {

					PCB candidateProcess;
					WhoAmI candidate = deathList.remove(0);
					kernelLogger.debug("Process dead and being culled from the system.", ThingsCodes.KERNEL_PROCESS_DONE, SystemNamespace.ATTR_PROCESS_ID, candidate.toString());
					
					// Process it - we'll log issues if they occur.  
					try {
						candidateProcess = processes.getProcess(candidate);
						candidateProcess.getProcess().forceFinalize();
						
						// Cull local properties.
						if (cullPropsOnDeathMap.containsKey(candidate)) {
							myGlobalPropertiesViewRoot.prune(cullPropsOnDeathMap.get(candidate));
						}
						
					} catch (SystemException se) {
						kernelLogger.info("Exception while finalizing a dying thread.", ThingsCodes.KERNEL_PROCESS_FINALIZATION, se.getAttributesNVMulti(new NV(SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage()), new NV(SystemNamespace.ATTR_PROCESS_ID, candidate.toString()) ));
					} catch (Throwable t) {
						kernelLogger.info("Exception while finalizing a dying thread.", ThingsCodes.KERNEL_PROCESS_FINALIZATION, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage(), SystemNamespace.ATTR_PROCESS_ID, candidate.toString());						
					}
					
					// Thwack the process logger
					Logger candidateLogger = loggerCache.remove(candidate);
					candidateLogger.flush();
					
					// Let the watchers know.
					synchronized(deathWatchMonitor) {
						deathWatchMonitor.notifyAll();
					}	
				}
				
			} catch (InterruptedException ie) {
				// Ignore.  just let it have a cycle.  We may be halting.
				Thread.interrupted();
			} catch (SystemException se) {
				resultThrowable = se;
			} catch (Throwable t) {
				resultThrowable = new SystemException("PANIC in Kernel during runtime.", SystemException.PANIC_SYSTEM_KERNEL_FAILURE, t);
			}	
			
		} // end while
		
		// In case the request is still pending.
		try {
			acceptHalt();
		} catch (Throwable t) {
			// Make sure the interrupted doesn't spoil our fun.  
		}
		
		// Shutdown
		shutdownKernel();
		
		// Done
		startupRendezvous.meet(resultThrowable);			// Send back a throwable or a null if nothing happened.
	}
	
	/**
	 * Start the Kernel.  This will be called by the executeThingsProcess when the thread is start()'ed.
	 * @throws things.thinger.SystemException
	 */
	private void startKernel() throws SystemException {
		
		// What we will pass back through the rondezvous if there is a problem.  It starts as null, which assumes all is A-OK.
		Throwable resultThrowable = null;
		
		// Outer try.
		try {
		
			// ----MANAGE PROPERTIES ---------------------------------------------------------------
			try {	
				//	 Try to catch any vicious problems here
				bootstrapLogger.post("The kernel process has started.");
				myGlobalPropertiesKit = new ThingsPropertyReaderToolkit(myGlobalPropertiesTree);
			} catch (Throwable t) {
				throw new SystemException("PANIC in bootstrap during begin.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());			
			}		
					
			// ----MANAGE PROPERTIES ---------------------------------------------------------------
			try {
					
				// Process filesystems.  It'll let us know if it is bad.
				config_USER_FILESYSTEM_ROOT = myGlobalPropertiesKit.getRequired(KernelBasic_Constants.USER_FILESYSTEM_ROOT);
				userFilesystem = new FSFileSystem(config_USER_FILESYSTEM_ROOT);
				config_SYSTEM_FILESYSTEM_ROOT = myGlobalPropertiesKit.getRequired(KernelBasic_Constants.SYSTEM_FILESYSTEM_ROOT);
				systemFilesystem = new FSFileSystem(config_SYSTEM_FILESYSTEM_ROOT);
				
				// Set the level
				String levelName = myGlobalPropertiesKit.getRequired(KernelBasic_Constants.LOGGING_LEVEL);
				LEVEL candidateLevel = LEVEL.getLevelByName(levelName);
				if (candidateLevel == null) throw new SystemException("Logging level not valid value.", SystemException.SYSTEM_ERROR_LOGGING_LOG_LEVEL_INVALID, SystemNamespace.ATTR_PROPERTY_NAME, KernelBasic_Constants.LOGGING_LEVEL, SystemNamespace.ATTR_PROPERTY_VALUE, levelName);
				config_LOGGING_LEVEL = candidateLevel;
				bootstrapLogger.post("Logging level set as " + config_LOGGING_LEVEL.name());
				
				// Ask for standard logger implementation
				String loggerImpementation = myGlobalPropertiesKit.getOptional(LOGGING_FACTORY_IMPLEMENTATION);
				if (loggerImpementation==null) {
					loggerImpementation = DEFAULT_LOGGING_FACTORY_IMPLEMENTATION;  // Use default if not set.
				}
				config_LOGGING_FACTORY_IMPLEMENTATION = (KernelBasic_WriterLogger_Factory)Class.forName(loggerImpementation).newInstance();
	
				bootstrapLogger.post("Loaded properties.");
				
			} catch (Throwable t) {
				throw new SystemException("PANIC in bootstrap during property setup.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, t,SystemNamespace.ATTR_PLATFORM_MESSAGE,t.getMessage());			
			}	
			
			// ---- START INTERNAL KERNEL LOGGER.  This is a special logger.  ---------------------------------
			try {
			
				// We are not going to manage this as a resource, since it will last the length of the Kernel.
				kernelLogger = config_LOGGING_FACTORY_IMPLEMENTATION.forgeFileLogger(kernelSpaceID, KERNEL_LOG_PATH, systemFilesystem, config_LOGGING_LEVEL);
				kernelLogger.initResource(kernelSpaceID.birthMyChild(RESOURCENAME_LOGGER_PREFIX + KERNEL_ID_STRING));
				bootstrapLogger.post("System logger started.");
				if (verbose) {
					kernelLogger.debuggingOn();
				}
				
			} catch (Throwable t) {
				throw new SystemException("PANIC in bootstrap during kernel logger setup.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, t,SystemNamespace.ATTR_PLATFORM_MESSAGE,t.getMessage());			
			}
			
			// ----SET UP DATA ---------------------------------------------------------------
			try {
				
				
				// Set up main conduits.
				systemConduits	= new BasicConduitController();
				
				// ---- Set up universe    ----------------
				
				// Universe Registry
				String defaultUniverseConfig = myGlobalPropertiesKit.getRequired(DEFAULT_UNIVERSE_CONFIG);
				ThingsPropertyView universeRegistryView = ThingsPropertyTreeBASIC.getExpedientFromFile(config_SYSTEM_FILESYSTEM_ROOT + "/" + defaultUniverseConfig);	
				universeRegistry.loadRegistry(universeRegistryView);		
				
				// Set up default universes.
				systemUniverse = universeRegistry.getAccessor(myGlobalPropertiesKit.getRequired(UNIVERSE_SYSTEM_DEFAULT_NAME_CONFIG));
				userUniverse = universeRegistry.getAccessor(myGlobalPropertiesKit.getRequired(UNIVERSE_USER_DEFAULT_NAME_CONFIG));
					
				// ---- Set up loader   ----------------	
				
				// Which loader?
				if (Plato.decideTruth(myGlobalPropertiesKit.getOptional(DEBUGGING_IDE), false)) {
					
					// The IDE one.  This one will leave it up to an IDE to do loading into classpath.  ALL sources need
					// to be in the path!
					loader = new KernelBasic_LoaderIDE();
					
				} else {
					
					// Our usual one.
					loader = new KernelBasic_Loader();
					
					// System cache first
					UniverseAddress cacheUniverseUAddy = new UniverseAddress(myGlobalPropertiesKit.getRequired(KernelBasic_Constants.THINGS_CACHE_LOCATION));
					Universe workingUniverse = universeRegistry.getAccessor(cacheUniverseUAddy.universeName);
					loader.init(workingUniverse, cacheUniverseUAddy.path);
					
					// Sources
					String[] sourceUniverseList = myGlobalPropertiesKit.getOrderedNumberedPly(KernelBasic_Constants.THINGS_UNIVERSE_LOCATION_PLY);
					String sourceUniverseName = null;
					UniverseAddress uaddySource; 
					for (int index = 0 ; index < sourceUniverseList.length ; index++ ) {
						
						try {
							// Get configuration and validate
							sourceUniverseName = myGlobalPropertiesKit.getRequired(KernelBasic_Constants.THINGS_UNIVERSE_LOCATION_PLY, sourceUniverseList[index]);
							uaddySource = new UniverseAddress(sourceUniverseName);
							workingUniverse = universeRegistry.getAccessor(uaddySource.universeName);
							if (workingUniverse == null) throw new Exception("Universe not registered.");
							
							// Register
							loader.addSource(workingUniverse, uaddySource.path);
							
						} catch (Throwable t) {
							throw new SystemException("Failed to register source.", SystemException.UNIVERSE_FAULT_NOT_REGISTERED, t, SystemNamespace.ATTR_UNIVERSE_NAME, sourceUniverseName, ThingsNamespace.ATTR_PROPERTY_NAME, KernelBasic_Constants.THINGS_UNIVERSE_LOCATION_PLY);
						}
						
					} // End for sourceUniverseList
					
				} // end if which loader
	
				bootstrapLogger.post("Data prepared.");
				
			} catch (Throwable t) {
				throw new SystemException("PANIC in bootstrap during data and universe setup.", SystemException.PANIC_SYSTEM_STARTUP_KERNEL_FAILURE, t,SystemNamespace.ATTR_PLATFORM_MESSAGE,t.getMessage());			
			}	
			
			// ---- START SERVICES -------------------------------------------------------------------------
			String candidateName = "none";
			try {
				
				// Get the list of services listed in global.
				Collection<String> serviceList = myGlobalPropertiesViewRoot.ply(KernelBasic_Constants.CORE_SERVICES);
				if (serviceList.isEmpty()) kernelLogger.post("No services specified.");
				
				// Run 'em.
				for (String t : serviceList) {
					
					// Discover a service name
					candidateName = "UNKNOWN";
					candidateName =  myGlobalPropertiesKit.getRequiredSingle(CORE_SERVICES, t, CORE_SERVICES_suffix_NAME);
					String className =  myGlobalPropertiesKit.getRequiredSingle(CORE_SERVICES, t, CORE_SERVICES_suffix_CLASS);				
					
					// Load
					kernelLogger.post("Start service " + candidateName);
					Class<?> candidate = Class.forName(className);
					Object thang = candidate.newInstance();
					if ( !(thang instanceof Service) ) {
						throw new SystemException("Service class not actually a service.", SystemException.KERNEL_FAULT_CLASS_ISSUE, SystemNamespace.ATTR_PLATFORM_CLASS, className, SystemNamespace.ATTR_PLATFORM_CLASS_ACTUAL, candidate.getCanonicalName());
					}
					
					// Instantiate the service
					Service validService = (Service)thang;
					int	pin = pcbNumber.getAndIncrement();
					WhoAmI thisProcessId = kernelSpaceID.birthMyChild(SERVICE_ID_STRING_PREFIX + pin + ID_SEPERATOR + t, SERVICE_ID_TAG_PREFIX + pin);
					
					// Get a logger first, so we can use it during process init.
					// The log forger already put it into the ResourceCoordinator for us.
					// Be sure to add it to the cache so calls to getSystemLogger will yield it.
					ResourceManager rmLogger = forgeLogger(thisProcessId,systemFilesystem, KernelBasic_NamingReactors.rLOGGER_LOCATION(thisProcessId));
					Logger theLogger = (Logger)rmLogger.getResource();
					if (verbose) {
						// Force debugging if this is verbose
						theLogger.debuggingOn();
					}
					validService.setLogger(theLogger);
					loggerCache.put(thisProcessId.toString(), theLogger);
					
					// Get the local properties.  If not available, give it a private, empty tree.
					ThingsPropertyView localView = myGlobalPropertiesViewRoot.cutting(ThingsPropertyReaderToolkit.path(CORE_SERVICES, t, CORE_SERVICES_suffix_LOCAL));
					if (localView==null) localView = new ThingsPropertyTreeRAM();
	
					// Fix the process
					validService.fix(thisProcessId);
					
					// ID it as a process, this must happen before the init or the kernel may not let it have access to resources.
					processes.registerProcess(validService,DEFAULT_SERVICE_CLEARANCE, localView, userGlobalConfigView);
					
					// Init the service through the process
					validService.init(this);
					validService.release();
					
					// Add the process as a listener to the logger
					rmLogger.addListener(validService);
									
					// Add to list and start it
					validService.serviceOn();
					loadedServiceList.put(t,validService);
					bootstrapLogger.post("      service " + candidateName + " started.");
					
				}
				
			} catch (ThingsException tp) {		
				throw new SystemException("PANIC in bootstrap during service start.", SystemException.PANIC_SYSTEM_STARTUP_SERVICE_FAILURE, tp, SystemNamespace.ATTR_SYSTEM_SERVICE_NAME, candidateName);			
			} catch (NullPointerException np) {		
				throw  new SystemException("PANIC in bootstrap during service start.  Service specification null.  Could be a bug.", SystemException.PANIC_SYSTEM_STARTUP_SERVICE_FAILURE, np, SystemNamespace.ATTR_SYSTEM_SERVICE_NAME, candidateName);
			} catch (Throwable t) {
				throw new SystemException("PANIC in bootstrap during service start.  General error.", SystemException.PANIC_SYSTEM_STARTUP_SERVICE_FAILURE, t,SystemNamespace.ATTR_SYSTEM_SERVICE_NAME, candidateName, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());			
			} finally {
				// Make sure our kernel logger is flushed, if possible.
				try {
					kernelLogger.flush();
				} catch (Throwable t) {
					// If we can't, then we cant...
				}
			}
			
		} catch (SystemException see) {
			
			// Remember if we had an exception
			resultThrowable = see;
		}
		
		// ---- SHUTDOWN IF WE HAD A PROBLEM    -----------------------------------------------------------
		// If we blew up, undo stuff.  Start killing the services.
		if (resultThrowable != null) {
			bootstrapLogger.postit("Boostrap failed.  Stopping services.");		
			bootstrapLogger.postit(ThingsException.toStringComplex(resultThrowable));
			shutdownKernel();
		}
		
		// We're done.
		bootstrapLogger.postit("Boostrap complete.");
		startupRendezvous.meet(resultThrowable);			// Send back an throwable or a null if nothing happened.

	}
	
	/**
	 * Shutdown the Kernel.  This can be called during bootstrap or later.  We cannot let any exceptions out of this.
	 * Anything after a call to this will be best effort, since the shutdown is allowed to abruptly halt the VM.
	 */
	@SuppressWarnings("deprecation")
	private void shutdownKernel() throws SystemException {

		int runningNumber = 0;
		
		// Don't let anything out!
		try {
			bootstrapLogger.postit("Start shutdown.");
			
			// ---- STOP SERVICES -------------------------------------------------------------------------
			// Sweep through twice.  Try once nicely.  Try twice meanly.  And if anything remains, the kernel
			// will just EXIT() when it is done.  Blame SUN and their interrupted() crap with streams.
			runningNumber = 0;
			for (Service s : loadedServiceList.values()) {
				try {
					if (s.getState() != Thread.State.TERMINATED) {
						s.forceHalt();		
						runningNumber++;
					}
				} catch (Throwable t) {
					bootstrapLogger.postit("Problem while forcing service to halt.  This probably doesn't matter.  service=" + s.getName() + " message=" + t.getMessage());
				}
			}
			if (runningNumber>0) { 
				sleep(200);
				for (Service s : loadedServiceList.values()) {
					try {
						if (s.getState() != Thread.State.TERMINATED) {
							s.stop();			
						}
					} catch (ThreadDeath td) {
						// GOOD!
					} catch (Throwable t) {
						bootstrapLogger.postit("Problem while forcing service to stop.  This probably doesn't matter.  service=" + s.getName() + " message=" + t.getMessage());
					}
				}			
			}			

			// ---- STOP OTHER PROCESSES -------------------------------------------------------------------------
			// Sweep through until we KNOW they are dead, even if I have to resort to evil deprecated functions. 
			// The services should already be gone.
			
			runningNumber = 0;
			for (PCB s : processes.getProcesses()) {
				try {
					if (s.getProcess().getState() != Thread.State.TERMINATED) {
						s.getProcess().forceHalt();		
						runningNumber++;
					}
				} catch (Throwable t) {
					bootstrapLogger.postit("Problem while forcing process to halt.  This probably doesn't matter.  service=" + s.getProcess().getName() + " message=" + t.getMessage());
				}
			}
			if (runningNumber>0) { 
				try {
					sleep(200);
				} catch (Throwable t) {
					// The kernel might be interrupted already.
				}
				for (PCB s : processes.getProcesses()) {
					try {
						if (s.getProcess().getState() != Thread.State.TERMINATED) {
							s.getProcess().stop();			
						}
					} catch (ThreadDeath td) {
						// GOOD!
					} catch (Throwable t) {
						bootstrapLogger.postit("Problem while forcing process to stop.  This probably doesn't matter.  service=" + s.getProcess().getName() + " message=" + t.getMessage());
					}
				}			
			}	

			// In case we are still bootstrapping.
			try {
				forceHalt();
			} catch (Throwable t) {
				// Make sure the interrupted doesn't spoil our fun.  
			}
			
			bootstrapLogger.postit("Shutdown complete.");
			
		} catch (Throwable t) {
			try {
				bootstrapLogger.postit("Problem while shutting down the kernel.  This probably doesn't matter.  message=" + t.getMessage());				
			} catch (Throwable tt) {
				// FARG...
			}
		}
		
		// Dispose runtime resources
		kernelLogger.disposeResource();
		
		// OK, exit this damn thing if anything is left alive.
		if (runningNumber > 0) {
			bootstrapLogger.postit("There are ghosted processes, so kernel will abruptly stop.");
			System.exit(0);	
		}

	}
	

	// =====================================================================================================================
	// =====================================================================================================================
	// = THINGS PROCESS ABSTRACTS

	/**
	 * This is the entry point for the actual processing
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException {
		kernelStoplight.turnRed();
		startKernel();
		runtime();
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * @throws things.thinger.SystemException
	 */
	public void constructThingsProcess() throws SystemException {
		// Don't care?
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 * @throws things.thinger.SystemException
	 */
	public void destructThingsProcess() throws SystemException {
		// Don't care?
	}
	
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return kernelSpaceID.toString();
	}
	
	// Resourse interface.  Don't care.
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public WhoAmI getListenerId() {
		return getProcessId();
	}

	// =====================================================================================================================
	// =====================================================================================================================
	// PROTECTED METHODS
	
	/**
	 * Create a file logger from the current factory and put it in a resource manager.
	 */
	protected ResourceManager forgeLogger(WhoAmI owner, AFileSystem fileSystem, String path)throws SystemException {
		ResourceManager rm = null;
		
		// These are always named the same way.
		String loggerId = KernelBasic_NamingReactors.rLOGGER_RESOURCE_ID(owner);
		
		// Make sure we can't asked for this logger already.
		if (resources.isRegistered(loggerId)) throw new SystemException("Logger already issued for that id and it is active.", ThingsException.SYSTEM_ERROR_LOGGING_LOGGER_ALREADY_ISSUED, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, owner.toString() );
		
		try {

			// Is this a startup problem???
			if (config_LOGGING_FACTORY_IMPLEMENTATION==null) {
				System.out.println("Shit self.");
			}
			
			// Create it.
			KernelBasic_WriterLogger candidate = config_LOGGING_FACTORY_IMPLEMENTATION.forgeFileLogger(owner, path, fileSystem, config_LOGGING_LEVEL);

			// Put it under management
			rm = resources.registerResource(candidate, loggerId);
			
			
		} catch (SystemException se) {
			throw new SystemException("Could not forge resource managed Logger.", SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER, se, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, owner.toString());			
		} catch (Throwable t) {
			throw new SystemException("Could not forge resource managed Logger due to spurious exception.", SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER, t, SystemNamespace.ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER, owner.toString());
		}
		
		// Done
		return rm;
	}

}
