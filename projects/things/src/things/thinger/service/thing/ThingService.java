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
package things.thinger.service.thing;

import java.util.concurrent.atomic.AtomicInteger;

import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.data.Data;
import things.data.Receipt;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitController;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.PushDrain;
import things.thinger.service.Service;
import things.thinger.service.ServiceConstants;

/**
 * Thing execution service.  THIS IS NOT DONE.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 FEB 06
 * </pre> 
 */
public class ThingService extends Service implements PushDrain {
	
	// CONSTANTS
	private static final int STARTING_SERVED 	= 1;
	
	// DATA
	@SuppressWarnings("unused")
	private ConduitID  		myConduitId = null;
	private Conduit			myConduit;
	private Boolean	  		serviceIsOn;
	private AtomicInteger	served;
	private Logger			myLogger;
	private ThingsPropertyView localProperties;
	private ConduitController systemConduitController;
	
	// ===================================================================================================
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
	 * This is the entry point for the actual processing
	 */
	public void executeThingsProcess() throws SystemException {
	
		// Run until an exception
		try {
			
			while(true) {

			}
			
	//	} catch (InterruptedException ie) {
			// This is likely the 
	//		screech("CommandService process execute() was interrupted.  Quiting.");
			
	//	} catch (SystemException se) {
	//		screech("SystemException while processing command.  Quitting with error.");
	//		throw new SystemException("SystemException exception caused fault for CommandService process execute().  Quitting with error.",SystemException.SYSTEM_COMMAND_FAULT_SERVICE_ABORTED, se, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
			
		} catch (Throwable t) {
			screech("General exception while processing command.  Quitting with error.");
			throw new SystemException("General exception caused fault for CommandService process execute().  Quitting with error.",SystemException.SYSTEM_COMMAND_FAULT_SERVICE_ABORTED, t, SystemNamespace.ATTR_PLATFORM_MESSAGE,t.getMessage());
		} finally {
			myLogger.info("LocalCommandService stopping.");
		}
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 */
	public void constructThingsProcess() throws SystemException {
		
		// State is off
		serviceIsOn = false;
		
		// The served value ID's each run.
		served = new AtomicInteger();
		served.set(STARTING_SERVED);
		
		// Build my channel and attach the conduit.
		try {
			
			// Get local command info
			localProperties = ssi.getLocalProperties();
			String commandChannelName = localProperties.getProperty(ServiceConstants.CHANNEL_COMMAND);
			if (commandChannelName == null) throw new SystemException("Command channel not set in properties.", SystemException.SYSTEM_ERROR_BAD_PROPERTY_NOT_DEFINED, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.CHANNEL_COMMAND);
			
			// set it up
			systemConduitController = ssi.getSystemConduits();
			myConduit = systemConduitController.tune(new ConduitID(commandChannelName),this.getProcessId());
			myConduit.registerPushDrain(this);
			
			
		} catch (Throwable t)  {
			throw new SystemException("Failed to attach CommandService to its conduit.",SystemException.PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION,t);
		}
		
		// Handle any other server stuff.
		try {
			
			// Get my logger.
			myLogger = ssi.getSystemLogger();
			myLogger.info("LocalCommandService started.");
			
		} catch (Throwable t)  {
			throw new SystemException("Failed to complete CommandService construction.",SystemException.PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION,t);
		}			
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
		
		// Deregister
		myConduit.deRegisterPushDrain(this);	
	}

	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "LocalCommandService";
	}
	
	// ==========================================================================================================
	// == PUSH DRAIN
	
    /**
     * Initialize the PushDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PushDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException {
    	myConduitId = yourId;
    }
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The Data to post.
	 * @return a receipt
	 * @throws things.thinger.SystemException
	 */
	public Receipt postListener(Data		n) throws SystemException {
		
		Receipt result = null;
//		Entity<ThingSpecification>	item;
		
		// On or off.  Since this is a post, it should be synchronized already anyway.
		synchronized(serviceIsOn) {
			
			try {
	/*			
				// Get it and qualify it (this may cause a classcast exception) by trying to read the tag.  This wouldn't be neccessary if Java generics didn't suck.
				item = (Entity<ThingSpecification>)n;
				ThingSpecification info = item.getTypedThing();
				if ((info.responseConduit.toTag()==null)&&this.isVerbose()) screech("CommandService got an Entry with a null tag.  LocalCommandService can onlyt handle LocalCommand commands.");	// See if it fails on class issues
				
				// Has this service been constructed?
				if (inboundCommands == null) throw new SystemException("CommandService was not construct()'ed before use.");
			
				// Is the service on?
				if (serviceIsOn == true) {
					
					// Accept delivery and enqueue it for the service
					result = new Receipt(commandTagging(Integer.toString(n.getNumeric())), Receipt.Type.DELIVERY);
					inboundCommands.add(item);

				} else {
							
					// Dead reciept
					result = new Receipt(commandTagging("Service is off"), Receipt.Type.UNWANTED);
				}	
				*/
			
			} catch (ClassCastException cce) {
				throw new SystemException("CommandService can only handle Entity<ConduitID>.",SystemException.SYSTEM_SERVICE_ERROR_COMMAND_MALFORMED,SystemNamespace.ATTR_PLATFORM_CLASS_ACTUAL,n.getClass().getName());				
//			} catch (SystemException se) {
	//			throw new SystemException("Failed CommandServices due to error.",SystemException.SYSTEM_SERVICE_ERROR_COMMAND_GENERAL,se);
			} catch (Throwable t) { 
				throw new SystemException("Failed CommandService due to unexpected exception.",SystemException.SYSTEM_FAULT_SERVICE_GERERAL,t);
			}
		}
		
		// done
		return result;
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

	// ===================================================================================================================
	// == HELPERS
	
	
	// ==========================================================================================================
	// == IMPLEMENTATION
	

}
