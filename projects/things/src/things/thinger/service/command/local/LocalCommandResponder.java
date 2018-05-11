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
package things.thinger.service.command.local;

import things.common.WhoAmI;
import things.data.AttributeReader;
import things.data.Entity;
import things.data.NV;
import things.data.Receipt;
import things.data.Data.Priority;
import things.data.Data.Type;
import things.data.impl.ReadWriteableAttributes;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.Injector;
import things.thinger.io.conduits.Conduit.InjectorType;
import things.thinger.service.command.CommandResponder;

/**
 * The Local implementation Command Responder.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 APR 06
 * </pre> 
 */
public class LocalCommandResponder implements CommandResponder {

	// =======================================================================================================
	// == DATA 
	ReadWriteableAttributes responseAttributes;
	Entity<LocalCommandInfo> myOriginalCommand;
	WhoAmI	myServiceId;
	LocalCommandInfo	info;
	Injector myResponseConduitInjector;			// This will be disposed by the finalizer.
	Conduit myResponseConduit;
	SystemInterface	mySi;
	
	/**
	 * Note when we've considered it all done--either happy or errored.
	 */
	private boolean iAmDone;
	
	// =======================================================================================================
	// == CONSTRUCTION
	/**
	 * Construct.
	 * @param originalCommand
	 * @param responseConduit
	 * @param serviceId
	 * @param si
	 * @throws Throwable
	 */
	public LocalCommandResponder(Entity<LocalCommandInfo> originalCommand, Conduit responseConduit, WhoAmI serviceId, SystemInterface si) throws Throwable {
		myOriginalCommand = originalCommand;
		myServiceId = serviceId;
		mySi = si;
		iAmDone = false;
		
		// Wrap any exceptions
		try { 
			info = originalCommand.getTypedThing();
			myResponseConduit = responseConduit;
			myResponseConduitInjector = responseConduit.getInjector(InjectorType.BROADCAST);
		} catch (Throwable t) {
			throw new SystemException("Could not attach response conduit.", SystemException.SYSTEM_COMMAND_FAULT_COULD_NOT_BUILD_RESPONSE, t);
		}
	}
	
	// =======================================================================================================
	// == IMPLEMENT CommandResponder
	
	/**
	 * Get the command attributes.
	 * @throws SystemException
	 */
	public AttributeReader getCommandAttributes() throws SystemException {
		return myOriginalCommand.getAttributes();
	}
	
	/**
	 * Add a response.
	 * @param item the item
	 * @throws SystemException
	 */
	public void add(NV	item) throws SystemException {
		
		// Do we need an attribute set?
		if (responseAttributes==null) responseAttributes = new ReadWriteableAttributes();
		
		// Add it
		try {
			responseAttributes.addAttribute(item);
		}	catch (Throwable t) {
			throw new SystemException("Failed to add response due to infrastructure problem.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t);
		}
	}
	
	/**
	 * Get a system interface.
	 * @return A system interface.
	 * @throws SystemException
	 */
	public SystemInterface getSystemInterface() throws SystemException {
		return mySi;
	}
	
	/**
	 * Remove a response for replacement, if possible.  If the reponder has been flushed, the response may have already been transmitted, and then it is too late.
	 * So be careful with this.
	 * @param name the item to replace
	 * @throws SystemException
	 */
	public void remove(String name) throws SystemException {
		
		try {
			if (responseAttributes!=null) responseAttributes.removeAttribute(name);
		}	catch (Throwable t) {
			throw new SystemException("Failed to add response due to infrastructure problem.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t);
		}
	}
	
	/**
	 * Force the responder to flush.  This should make it transmit.
	 * @throws SystemException
	 */
	public void flush() throws SystemException {
		postMessage(Receipt.Type.UNDERWAY, "partial");
	}
	
	/**
	 * Make the response done.  This is a TERMINAL reciept.    This will flush.  Any further adds or replaces will result in an exception.  It'll throw an exception if we are done--either through happiness or error.
	 * @throws SystemException
	 */
	public void done() throws SystemException {
		postMessage(Receipt.Type.ETERNAL_HAPPINESS, "completed");
		iAmDone = true;		// TERMINAL
	}
	
	// =======================================================================================================
	// == METHODS
	
	/**
	 * Report a failure.  This is a TERMINAL reciept.  It'll throw an exception if we are done (TERMINAL reciept)--either through happiness or error.  
	 * @param message the test of the failure.
	 * @throws SystemException
	 */
	public void reportFailure(String message) throws SystemException {					
		postMessage(Receipt.Type.ERRORED, message);
		iAmDone = true;		// TERMINAL
	}
	
	/**
	 * Report a failure with attributes.  This is a TERMINAL reciept.    It'll throw an exception if we are done (TERMINAL reciept)--either through happiness or error.
	 * @param message the test of the failure.
	 * @throws SystemException
	 * @param attrib Name/value pairs that will make us the attributes.
	 */
	public void reportFailure(String message, String... attrib) throws SystemException {	
		postMessage(Receipt.Type.ERRORED, message, attrib);
		iAmDone = true;		// TERMINAL
	}
	
	// ================================================================================================================
	// == HELPERS
	
	/**
	 * Post a response.  It'll throw an exception if we are done (TERMINAL reciept)--either through happiness or error.
	 * @param type the type of response.
	 * @param message A text message to go with the response.   Msotly irrelevent.
	 * @throws SystemException
	 */
	private void postMessage(Receipt.Type type, String message) throws SystemException {
		try {
			
			// Are we done?.
			if (iAmDone) throw new SystemException("Responder is already done(), so cannot post.", SystemException.SYSTEM_COMMAND_ERROR_COMMAND_ALREADY_DONE);
			
			// Do we need an attribute set?
			if (responseAttributes==null) responseAttributes = new ReadWriteableAttributes();
			
			// Prepare response
			Receipt resultReciept = new Receipt(myServiceId, myOriginalCommand.getCreatorID().toString(), type, message);
			Entity<Receipt> responseEntity = new Entity<Receipt>(myOriginalCommand.getNumeric(), Type.COMMAND_RESPONSE, Priority.ROUTINE, resultReciept,
					myOriginalCommand.getID(), myServiceId, responseAttributes);
			
			// Send it and ignore the receipts.
			myResponseConduitInjector.post(responseEntity);
			
			// Clear the response attributes.
			responseAttributes = null;
			
		} catch (Throwable t) {
			throw new SystemException("Failed to transmit " + type.toString() + " response due to infrustructure problems.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t);			
		}
	}
	
	/**
	 * Post a response.
	 * @param type the type of response.
	 * @param message A text message to go with the response.   Mostly irrelevent.
	 * @throws SystemException
	 */
	private void postMessage(Receipt.Type type, String message, String... attribs) throws SystemException {
		
		// Attrivute management.
		try {
			// Make sure it exists.  Then add the new ones.
			if (responseAttributes==null) responseAttributes = new ReadWriteableAttributes();		
			responseAttributes.addMultiAttributes(attribs);
			
		} catch (Throwable t) {
			throw new SystemException("Failed to manage attributes.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t);			
		}
		
		// Process it.   Let exceptions out.
		postMessage(type, message);
		
	}
	
	// ================================================================================================================
	// == FINALIZER
	
	/**
	 * finalizer.
	 */
	protected void finalize() throws Throwable {
		if (finalizeproc == true)
			return;

		try {	
			
			// Destruction process.  Only run done if we are not done.  The catch will protect against any race conditions around iAmDone.
			if (iAmDone == false) this.done();
			
			// Detach injector
			myResponseConduit.disposeInjector(myResponseConduitInjector);
			
		} catch (Throwable t) {
			// MEans it is already gone, so don't worry.
		}
	}
	private boolean finalizeproc = false;
}
