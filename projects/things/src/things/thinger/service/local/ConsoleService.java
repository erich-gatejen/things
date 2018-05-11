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
package things.thinger.service.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.common.impl.StringPosterConsole;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;

/**
 * A console service.  It will take a direct stdin and give it to the tender. 
 *<p>
 * While you can choose to run it as a server, you might not want too.  STDIN doesn't respond well to 
 * kernel interrupts.  Sometimes it behaves.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial (moved this out of the bootstrap) - 06 NOV 07
 * </pre> 
 */
public class ConsoleService extends CLIBackbone {
	
	// ===================================================================================================
	// EXPOSED DATA

	// ===================================================================================================
	// INTERNAL DATA
		
	// ===================================================================================================
	// CONSTRUCTOR
	public ConsoleService() {
		super();
	}

	
	// ===================================================================================================
	// METHODS	

	/**
	 * This will run the console.  It does not rely on any service infrastructure, so you can call it without
	 * starting the object as a service.
	 * @throws SystemException
	 */
	public void enterConsole() throws SystemException {
		boolean running = true;
		
		// The current processing is for test purposes only.
		try {
			
			// Give the kernel a chance to complete start-up.  We can optimize this later.
			try {
				sleep(110);
			} catch (Throwable t) {
				// Don't care.
			}
			
			// Our console
			// There will be no end of trouble because of this.  A lot of the time, it just refuses to interrupt the System.in stream.  Underneath,
			// it's a FileInputStream, so it shouldn't be a problem.  I've tried all kinds of things, but nothing is sure fire.  SO, instead I'll
			// give the kernel the ability to just exit the whole fucking show.
		    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			StringPoster consoleLogger = new StringPosterConsole();
			
			myLogger.info(getName() + " is alive.");
			
			// Loop through lines
			String currentLine;
			String result;
			while(running) {
				
				// Get a line and qualify it.
				try {
					
					currentLine = stdin.readLine();
					if ((currentLine!=null)&&(currentLine.length() > 0)) {
						
						try {
							result = transactionInterface.tender(currentLine);
							if ((result!=null)&&(result.trim().length()>0)) {
								consoleLogger.post( result );
							}
							
//						} catch (NullPointerException npe) {
							// There is a race condition where, but I don't want to deal with it now.  This will just dump that one line and move on.
						} catch (ThingsException te) {
							
							// Is this just the system dying?
							if (this.getCurrentState().isHalting()) {
								running = false;
								consoleLogger.postit("System shutting down so console is quiting.");
								break;
							}
							
							// No, really an error.
							consoleLogger.postit("ERROR while issuing command.");
							consoleLogger.postit(te.toStringComplex());
						}
						
					} // End if there was something on the line to process.
					
				} catch (IOException ioe) {
					// Don't care about these.
					throw ioe;
					
				} catch (Throwable t) {
					// We could be dying.  Otherwise just gobble it.
					if (t instanceof InterruptedException) {
						if (this.getCurrentState().isHalting()) {
							consoleLogger.postit("System shutting down so console is quiting.");
							running = false;
							break;
						}
					} else {
						throw t;
					}
				
				} // end outer exception (for read from console).
				
			} // end while running
			
		} catch (Throwable t) {
			myLogger.info(getName() + " stopping.");
			throw new SystemException("Unrecoverable exception in the CLIService.", SystemException.PANIC_SYSTEM_SERVICE_UNRECOVERABLE, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());			

		} 
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
		enterConsole();
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 */
	public void constructThingsProcess() throws SystemException {
		
		try {
			// Nothing to do.
			
		} catch (Throwable t) {
			throw new SystemException("Failed to construct CLIService.", SystemException.SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());
		}
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
	}
		
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "ConsoleService";
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
	
	// ==========================================================================================================
	// == PRIVATE COMMAND METHODS

	// ==========================================================================================================
	// == TOOLS
	
}
