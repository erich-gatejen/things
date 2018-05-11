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
package things.thinger.service.proxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.common.WhoAmI;
import things.common.impl.WhoAmISimple;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.service.Service;
import things.thinger.service.ServiceConstants;
import things.universe.Universe;
import things.universe.UniverseAddress;

/**
 * The proxy service.  Connections will be dispatched to helper 
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public class ProxyService extends Service {
	
	// ===================================================================================================
	// EXPOSED DATA
	
	// ===================================================================================================
	// INTERNAL DATA
	
	// Settings
	final private static int MAX_POOL = 35;
	final private static int ACCEPT_TIMEOUT = 2000;
	final private static int CONNECTION_TIMEOUT = 900000;
	final private static int CULL_TIME = 900000; 	// 15 minutes.
	private static WhoAmI stockUnknown = new WhoAmISimple("Unknown");
	
	// Data
	private ServerSocket listen;
	private LinkedBlockingQueue<ProxyServiceThread> availableThreads;
	private HashSet<ProxyServiceThread> activeThreads;
	private HashMap<String,ProxySession> sessions;
	
	private String uplinkAddress;
	private int uplinkPort;
	
	private String proxyPath;
	private Universe proxyUniverse;
	private Class<ProxyProcessor> proxyProcessorClass;
	
	private int currentNumber;
	private long lastCull;
	
	// ===================================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 */
	public ProxyService() {
		super();
		availableThreads = new LinkedBlockingQueue<ProxyServiceThread>();
		activeThreads = new HashSet<ProxyServiceThread>();
		sessions = new HashMap<String,ProxySession>();
	}
	
	/**
	 * Tell the service that a context is complete.
	 * @param context the context.
	 */
	public synchronized void complete(ProxyServiceContext context) {
		// return the session to the available pool.
		activeThreads.remove(context.thread);
		availableThreads.add(context.thread);
		context.dispose();
	}

	
	// ===================================================================================================
	// SERVICE IMPLEMENTATION
	
	/**
	 * Called to turn the service on.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOn() throws SystemException {
		// Always on
	}
	
	/**
	 * Called to turn the service off.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOff() throws SystemException {
		// Always on		
	}
	
	/**
	 * This is the entry point for the actual processing.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException {
		String stamp;
		Socket accepted;
		Socket uplink;
		ProxyServiceContext context;
		ProxyServiceThread ethread;
		ProxySession session;
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
					
					// Connect to downlink
					uplink = new Socket(uplinkAddress, uplinkPort);
					
					// Get or make the session
					String dlAddy = accepted.getInetAddress().getCanonicalHostName().replace(':', '_');
					session = sessions.get(dlAddy);
					if (session==null) {
						session = new ProxySession(stamp + "_" + dlAddy, proxyUniverse, proxyPath);
						sessions.put(dlAddy, session);
					}
					session.lastAccess = System.currentTimeMillis();
					
					// Build context and dispatch
					ethread = getThread();
					context = new ProxyServiceContext(id, session, ethread, this, uplink, accepted, proxyProcessorClass.newInstance(), myLogger); 
					ethread.handoff.meet(context);
					
					// dispatch
					myLogger.info("Proxy accepted.", ThingsCodes.SERVICE_PROXY_ACCEPT, ThingsNamespace.ATTR_TRANSPORT_ADDRESS, accepted.getInetAddress().toString(), ThingsNamespace.ATTR_ID, id.toString());
					
				} catch (InterruptedException ie) {
					throw ie;
				} catch (Throwable t) {
					myLogger.error("Proxy connection failed to uplink.", ThingsCodes.ACCESS_ERROR_CANNOT_OPEN, ThingsNamespace.ATTR_TRANSPORT_ADDRESS, uplinkAddress, ThingsNamespace.ATTR_ID, id.toString(), ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
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
	@SuppressWarnings("unchecked")
	public void constructThingsProcess() throws SystemException {
		
		try {
			
			// Get the processing info
			ThingsPropertyView localProperties = ssi.getLocalProperties();
			String fullProxyPath = localProperties.getProperty(ServiceConstants.PROXY_SESSION_PATH);
			if (fullProxyPath==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.PROXY_SESSION_PATH);
			if (fullProxyPath.trim().length()<1) throw new ThingsException("Required property empty.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.PROXY_SESSION_PATH);
			try {
				UniverseAddress ua = new UniverseAddress(fullProxyPath);
				proxyUniverse = ssi.getUniverse(ua.universeName);
				proxyPath = ua.path;
			} catch (Throwable t) {
				throw new ThingsException("Specified path is bad.  It must be a valid universe address.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, t, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.PROXY_SESSION_PATH, ThingsNamespace.ATTR_PROPERTY_VALUE, fullProxyPath);
			}
					
			String proxyProcessorClassName = localProperties.getProperty(ServiceConstants.PROXY_PROCESSOR);
			if (proxyPath==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.PROXY_PROCESSOR);
			try {
				proxyProcessorClass = (Class<ProxyProcessor>)Class.forName(proxyProcessorClassName);
			} catch (Throwable t) {
				throw new ThingsException("Specified processor does not exist.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.PROXY_PROCESSOR, ThingsNamespace.ATTR_PROPERTY_VALUE, proxyProcessorClassName);
			}
			
			// Get the uplink info.
			uplinkAddress = localProperties.getProperty(ServiceConstants.RELAY_CONNECT_ADDRESS);
			if (uplinkAddress==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.RELAY_CONNECT_ADDRESS);
			if (uplinkAddress.trim().length()<1) throw new ThingsException("Required property empty.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.RELAY_CONNECT_ADDRESS);

			String uplinkPortText = localProperties.getProperty(ServiceConstants.RELAY_CONNECT_PORT);
			if (uplinkPortText==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.RELAY_CONNECT_PORT);
			try {
				uplinkPort = Integer.parseInt(uplinkPortText);
				if ((uplinkPort<1)||(uplinkPort>65334)) throw new Exception();
			} catch (Throwable t) {
				throw new ThingsException("Property value bad.  Expecting a valie socket port.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.RELAY_CONNECT_PORT, ThingsNamespace.ATTR_PROPERTY_VALUE, uplinkPortText);
			}
			
			// Get the listen port.
			int listenPortValue = 0;
			String listenPort = localProperties.getProperty(ServiceConstants.LISTEN_PORT);
			if (listenPort==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT);
			try {
				listenPortValue = Integer.parseInt(listenPort);
				if ((listenPortValue<1)||(listenPortValue>65334)) throw new Exception();
			} catch (Throwable t) {
				throw new ThingsException("Property value bad.  Expecting a valid socket port.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT, ThingsNamespace.ATTR_PROPERTY_VALUE, listenPort);
			}
			
			listen = new ServerSocket(listenPortValue);
			listen.setSoTimeout(ACCEPT_TIMEOUT);
			
		} catch (Throwable t) {
			throw new SystemException("Failed to construct ProxyService.", SystemException.SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());
		}
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
		
		// Kill the sessions  Go ahead and pull them from the collections as they will finalize at different times.
		ProxyServiceThread session = availableThreads.poll();
		while (session!=null) {
			smackSession(session);
			session = availableThreads.poll();
		}
		
		Iterator<ProxyServiceThread> sessions = activeThreads.iterator();
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
	private void smackSession(ProxyServiceThread session) {
		session.interrupt();
	}
		
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "ProxyService";
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
	// == PRIVATE AND INTERNAL
	
	/**
	 * Get a thread.
	 * @return the session.
	 * @throws Throwable
	 */
	private synchronized ProxyServiceThread getThread() throws Throwable {
		ProxyServiceThread result = null;
		
		// Get it
		if (availableThreads.size()<1) {
			
			if (activeThreads.size()<MAX_POOL) {
				// We have room to build one.
				result = new ProxyServiceThread();
				result.start();
				
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
	 * Check to see if we are due to cull sessions.  If so, then do a cull.
	 */
	private void checkCull() {
		ProxySession session;
		long now = System.currentTimeMillis();
		if ((lastCull + CULL_TIME) < now) {
			// We are due.
			for ( String key : sessions.keySet()) {
				session = sessions.get(key);
				if ((session.lastAccess + CULL_TIME) < now) {
					// Ok, it has expired
					session.dispose();
					sessions.remove(key);
				}
			}
			lastCull = System.currentTimeMillis(); 
		}
	}
	
	// ==========================================================================================================
	// == TOOLS

}
