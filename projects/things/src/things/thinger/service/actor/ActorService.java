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
package things.thinger.service.actor;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyReaderToolkit.Validations;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.service.Service;
import things.thinger.service.ServiceConstants;

/**
 * The actor service.  This was never really finished.
 * <p>
 * LISTEN_PORT paramter gives the connect port.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 MAY 07
 * </pre> 
 */
public class ActorService  extends Service {

	// ============================================================================================================
	// CONFIGURATION
	final private static int ACCEPT_TIMEOUT = 2000;
	
	// ============================================================================================================
	// FIELDS

	// ============================================================================================================
	// INTERNAL DATA
	private Boolean serviceIsOn;
	private ServerSocket listen;
	
	/**
	 *  active ports 
	 */
	HashSet<ActorServiceThread> active;
	
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
	 * Complete construction. This will be called when the process is initialized.
	 * @throws SystemException
	 */
	public void constructThingsProcess() throws SystemException {
		
		// It always starts as on.
		serviceIsOn = true;
		
		// Handle any other server stuff.
		try {
			
			// -- PROPERTY CONFIGURATION ------------------------------------
			ThingsPropertyReaderToolkit propToolkit = new ThingsPropertyReaderToolkit(ssi.getLocalProperties());
			
			// Get the listen port.
			int listenPortValue = 0;
			String listenPort = propToolkit.getRequired(ServiceConstants.LISTEN_PORT, Validations.IS_NUMERIC);
			listenPortValue = Integer.parseInt(listenPort);
			if ((listenPortValue<1)||(listenPortValue>65334)) throw new ThingsException("Property value bad.  Expecting a valie socket port.", ThingsException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT, ThingsNamespace.ATTR_PROPERTY_VALUE, listenPort);

			// -- SET UP  ---------------------------------------------------------------
			listen = new ServerSocket(listenPortValue);
			listen.setSoTimeout(ACCEPT_TIMEOUT);	
			
			// Get my logger.
			myLogger = ssi.getSystemLogger();
			myLogger.info("HTTPToolService started.");
			
			// Data
			active = new HashSet<ActorServiceThread>();
			
		} catch (Throwable t)  {
			throw new SystemException("Failed to complete ActorService construction.",SystemException.PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION,t);
		}	
	}
	
	/**
	 * This is the entry point for the actual processing
	 */
	public void executeThingsProcess() throws SystemException {
		
		// Run until an exception
		try {
			
			Socket accepted = null;
			
			// -- Run ----------------------------------------------
			while (serviceIsOn) {
				
				try {
					
					// Get the next connection
					accepted = listen.accept();

					// Create new thread
					ActorServiceThread ast = new ActorServiceThread();
					ast.initialize(ssi, accepted);
					active.add(ast);
					
					// run it
					ast.start();
							
					// Done.
					myLogger.info("Connection accepted", ThingsCodes.SERVICE_ACTOR_CONNECTION_ACCEPTED, SystemNamespace.ATTR_ADDRESS_NETWORK, accepted.getInetAddress().toString());
					
				} catch (InterruptedException ie) {
					throw ie;
					
				} catch (SocketTimeoutException stm) {
					// NOP for now
							
				} catch (Throwable t) {
					throw new SystemException("Fault while accepting a connection.",SystemException.ACTORSERVICE_FAULT, SystemNamespace.ATTR_ADDRESS_NETWORK, accepted.getInetAddress().toString());
									
				}
				
			} // end while running		
			
		} catch (InterruptedException ie) {
			// This is likely the 
			screech("ActorService process execute() was interrupted.  Quiting.");
		
		} finally {
			myLogger.info("ActorService stopping.");
		}
		
		
	}
	
	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
		Iterator<ActorServiceThread> i = active.iterator();

		while (i.hasNext()) {
			ActorServiceThread ast = (ActorServiceThread) i.next();
			active.remove(ast);
			if (ast.isAlive()) {
				try {
					ast.interrupt();
				} catch (Exception eee) { // dont care
				}
			}
		} // end while
	}

	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "ActorService";
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