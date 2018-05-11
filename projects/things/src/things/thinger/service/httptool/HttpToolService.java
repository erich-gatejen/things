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
package things.thinger.service.httptool;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.common.WhoAmI;
import things.common.impl.WhoAmISimple;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyReaderToolkit.Validations;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger.LEVEL;
import things.thinger.kernel.Clearance;
import things.thinger.service.Service;
import things.thinger.service.ServiceConstants;

/**
 * The service implementation.  It must be told what PageManager and ActionManager to use.  These are passed by the 
 * properties:  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * EPG - Modified to be multi-threaded - 13 DEC 08
 * </pre>  */
public class HttpToolService extends Service {

	// ============================================================================================================
	// CONFIGURATION
	final private static int ACCEPT_TIMEOUT = 2000;
	
	// ============================================================================================================
	// FIELDS

	// ============================================================================================================
	// INTERNAL DATA
	private Boolean serviceIsOn;
	
	final private static int MAX_POOL = 10;
	final private static int CONNECTION_TIMEOUT = 900000;
	final private static int CULL_TIME = 900000; 	// 15 minutes.
	private static WhoAmI stockUnknown = new WhoAmISimple("Unknown");
	
	private int poolMax = MAX_POOL;		// Default is 10
	
	// Data
	private ServerSocket listen;
	private LinkedBlockingQueue<HttpToolServiceThread> availableThreads;
	private HashSet<HttpToolServiceThread> activeThreads;
	
	private int currentNumber;
	private long lastCull;
	
	private PageManager pageManager;
	private ActionManager actionManager;
	private ServeManager serveManager;
	private String serveRoot;
	
	private Clearance clearance;

	// =====================================================================================================================================
	// =====================================================================================================================================
	// SERVICE IMPLEMENTATION
	
	/**
	 * Called to turn the service on.  This may be called by another thread.
	 */
	public void serviceOn() throws SystemException {
		synchronized(serviceIsOn) {
			serviceIsOn = true;
		}
	}
	
	/**
	 * Called to turn the service off.  This may be called by another thread.
	 */
	public void serviceOff() throws SystemException {
		synchronized(serviceIsOn) {
			serviceIsOn = false;
		}
	}
	
	/**
	 * This is the entry point for the actual processing.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException {
		String stamp;
		Socket accepted;
		HttpToolServiceContext context;
		HttpToolServiceThread ethread;
		WhoAmI id = stockUnknown;
			
		// The current processing is for test purposes only.
		try {
			
			lastCull = System.currentTimeMillis();
			listen.setSoTimeout(CULL_TIME);
			myLogger.info(getName() + " is alive.");
			
			// Loop it.  If the consumer wants to stop they should throw a SystemException with SYSTEM_SERVICE_DONE as a numeric.
			while (true) {
				
				try {
					accepted = listen.accept();
					accepted.setSoTimeout(CONNECTION_TIMEOUT);
				} catch (SocketTimeoutException  ste) {
					// Just the cull timer.
					checkCull();
					continue;	// Iterate on the listen.
				} catch (Throwable t) {
					throw t;
				}
					
				myLogger.debug("Accepted a connection.");
				try {
					stamp = ThingsUtilityBelt.timestampFormatterYYYYDDDHHMMSSmmmm().replace(':', '_');
					id = new WhoAmISimple(stamp + "_" + getNumber());
					
					// Build context and dispatch
					ethread = getThread();
					context = new HttpToolServiceContext(id, this, accepted, ssi, serveRoot); 
					ethread.handoff.meet(context);
										
				} catch (InterruptedException ie) {
					throw ie;
				} catch (Throwable t) {
					myLogger.error("Dispatch of http connection failed.", ThingsCodes.SERVICE_HTTPTOOL_ERROR, ThingsNamespace.ATTR_ID, id.toString(), ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
					try {
						accepted.close();
					} catch (Throwable tt) {
					}
				}
				
				// Run a cull, if it is time.
				checkCull();
				
			} // End while working OK
			
		} catch (ThingsException te) {
			
			// Ignore it if it is just a DONE exception
			if (te.numeric != ThingsException.SYSTEM_SERVICE_DONE) {
				throw new SystemException("Unrecoverable exception in the ProxyService.", SystemException.SYSTEM_FAULT_SERVICE_PROBLEM, te, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName(), ThingsNamespace.ATTR_ID, id.toString());						
			}
			
		} catch (Throwable t) {
			if (!this.getCurrentState().isHalting())  // Are we just quitting?
				throw new SystemException("Unrecoverable exception in the ProxyService.", SystemException.PANIC_SYSTEM_SERVICE_UNRECOVERABLE, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName(), ThingsNamespace.ATTR_ID, id.toString());			

		} finally {
			myLogger.info(getName() + " stopping.");
		}	
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * <p>
	 * Set up the listening port.
	 */
	public void constructThingsProcess() throws SystemException {
		
		// It always starts as on.
		serviceIsOn = true;
		
		// Handle any other server stuff.
		try {
			
			availableThreads = new LinkedBlockingQueue<HttpToolServiceThread>();
			activeThreads = new HashSet<HttpToolServiceThread>();
			
			// -- PROPERTY CONFIGURATION ------------------------------------
			ThingsPropertyReaderToolkit propToolkit = new ThingsPropertyReaderToolkit(ssi.getLocalProperties());
			
			// Get the listen port.
			int listenPortValue = 0;
			String listenPort = propToolkit.getRequired(ServiceConstants.LISTEN_PORT, Validations.IS_NUMERIC);
			listenPortValue = Integer.parseInt(listenPort);
			if ((listenPortValue<1)||(listenPortValue>65334)) throw new ThingsException("Property value bad.  Expecting a valid socket port.", ThingsException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT, ThingsNamespace.ATTR_PROPERTY_VALUE, listenPort);
			
			// Get the listen port.
			String poolMaxText = propToolkit.getRequired(ServiceConstants.POOL_MAX, Validations.IS_NUMERIC);
			poolMax = Integer.parseInt(poolMaxText);
			if (poolMax<1) throw new ThingsException("Property value bad.  Expecting a positive number for pool size.", ThingsException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.POOL_MAX, ThingsNamespace.ATTR_PROPERTY_VALUE, poolMaxText);
			if (poolMax>100) myLogger.warning(ServiceConstants.POOL_MAX + " is LARGE.  This could spawn a crazy number of Threads.  " + ServiceConstants.POOL_MAX + "=" + poolMax);
			
			// Serve root.
			serveRoot = propToolkit.getOptional(ServiceConstants.SERVE_ROOT);
			
			// Clearance
			clearance = Clearance.PUBLIC;
			String clearanceString = propToolkit.getOptional(ServiceConstants.CLEARANCE);
			if ((clearanceString!=null)&&(clearanceString.trim().length()>1)) {
				try {
					clearance = Clearance.valueOf(clearanceString.trim().toUpperCase());
				} catch (Throwable t) {
					throw new SystemException("Unknown CLEARANCE configuration value.",SystemException.PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION,t, ThingsNamespace.ATTR_DATA_ARGUMENT, clearanceString);
				}
			}			
			
			// Managers
			String pageManagerClassName = propToolkit.getRequired(ServiceConstants.PAGE_MANAGER, Validations.NOT_EMPTY, Validations.NOT_NULL);
			String actionManagerClassName = propToolkit.getRequired(ServiceConstants.ACTION_MANAGER, Validations.NOT_EMPTY, Validations.NOT_NULL);

			// -- BUILD MANAGERS  ---------------------------------------------------------------
			try {
				Class<?> tempClass = Class.forName(pageManagerClassName);
				pageManager = (PageManager)tempClass.newInstance();
				pageManager.init(ssi);
			} catch (Throwable t) {
				throw new SystemException("Could not create page manager.", SystemException.KERNEL_FAULT_CLASS_ISSUE, t);
			}
			try {
				Class<?> tempClass = Class.forName(actionManagerClassName);
				actionManager = (ActionManager)tempClass.newInstance();
				actionManager.init(ssi);
			} catch (Throwable t) {
				throw new SystemException("Could not create action manager.", SystemException.KERNEL_FAULT_CLASS_ISSUE, t);
			}
			try {
				serveManager = new ServeManager();
				serveManager.init(ssi);
			} catch (Throwable t) {
				throw new SystemException("Could not create server manager.", SystemException.KERNEL_FAULT_CLASS_ISSUE, t);
			}
			
			// -- SET UP  ---------------------------------------------------------------
			listen = new ServerSocket(listenPortValue);
			listen.setSoTimeout(ACCEPT_TIMEOUT);	
			
			// Get my logger.
			myLogger = ssi.getSystemLogger();
			myLogger.info("HTTPToolService started.");
				
		} catch (Throwable t)  {
			throw new SystemException("Failed to complete HTTPToolService construction.",SystemException.PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION,t);
		}			
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
		
		// Kill the sessions  Go ahead and pull them from the collections as they will finalize at different times.
		HttpToolServiceThread session = availableThreads.poll();
		while (session!=null) {
			smackSession(session);
			session = availableThreads.poll();
		}
		
		Iterator<HttpToolServiceThread> sessions = activeThreads.iterator();
		while (sessions.hasNext()) {
			session = sessions.next();
			activeThreads.remove(session);
			smackSession(session);
		}
	}
	
	/**
	 * Smack a session for good.
	 * @param session
	 */
	private void smackSession(HttpToolServiceThread session) {
		try {
			session.forceHalt();
		} catch (Throwable t) {
			myLogger.shout("Ghosted a HttpToolServiceThread due to an exception.", ThingsCodes.PROCESSOR_HTTP_GHOSTED_PROCESS, LEVEL.WARNING, ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
		}
	}
	
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "HTTPToolService";
	}
		
	// ==========================================================================================================
	// == METHODS
	
	/**
	 * Tell the service that a thread is complete.
	 * @param theThread the thread that is compelte.
	 */
	public synchronized void complete(HttpToolServiceThread theThread) {
		// return the session to the available pool.
		activeThreads.remove(theThread);
		availableThreads.add(theThread);
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL
	
	/**
	 * Get a thread.
	 * @return the session.
	 * @throws Throwable
	 */
	private synchronized HttpToolServiceThread getThread() throws Throwable {
		HttpToolServiceThread result = null;
		
		// Get it
		if (availableThreads.size()<1) {
			
			if (activeThreads.size()<poolMax) {
				// We have room to build one.
				result = new HttpToolServiceThread(pageManager, actionManager, serveManager);
				ssi.startProcess(result, ssi.getLocalProperties(), clearance);
				
			} else {
				// We have to wait for one.
				result = availableThreads.take();
			}
			
		} else {
			result = availableThreads.take();
		}
		
		// Make active
		activeThreads.add(result);
		
		return result;
	}
	
	/**
	 * Get the session number.
	 * @return the number
	 */
	private int getNumber() {
		int result = currentNumber;
		currentNumber++;
		return result;
	}
	
	/**
	 * Check to see if we are due to cull.  If so, then do a cull.
	 */
	private void checkCull() {
		long now = System.currentTimeMillis();
		if ((lastCull + CULL_TIME) < now) {
			//  NOTHING TO CULL RIGHT NOW 
			
			
			lastCull = System.currentTimeMillis(); 
		}
	}
	
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS - mostly unused.
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public WhoAmI getListenerId() {
		return getProcessId();
	}
	
}
