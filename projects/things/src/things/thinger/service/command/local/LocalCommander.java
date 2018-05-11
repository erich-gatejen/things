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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.Verbose;
import things.common.WhoAmI;
import things.common.impl.WhoAmISimple;
import things.data.AttributeReader;
import things.data.Data;
import things.data.Entity;
import things.data.Receipt;
import things.data.ReceiptList;
import things.data.ThingsProperty;
import things.data.Data.Priority;
import things.data.Data.Type;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.Injector;
import things.thinger.io.conduits.basic.BasicPullDrainContainer;
import things.thinger.kernel.ResourceInterface;
import things.thinger.service.command.Command;
import things.thinger.service.command.CommandResponse;
import things.thinger.service.command.Commander;
import things.thinger.service.command.CommandResponse.ResponseState;

/**
 * A Commander implementation intended for a local environment.
 * <p>
 * Command semantics:<br><code>
 * Command <br>
 *  		- creates-[ Entity&lt;LocalCommandInfo&gt;(idNumber,COMMAND,ROUTINE,ConduitId.responseConduit,WhoAmI(commandIdString),id, attributeData ]<br>
 *                                      Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a))<br>
 * 			- sends Entity&lt;LocalCommandInfo&gt; ---} myCommandConduit<br>
 * 			[--- returns Receipt(myId, commandIdString, Receipt.Type.ACCEPTANCE, commandIdString)<br>
 *                       Receipt(WhoAmI callerID, String theToken, Receipt.Type theType, String note)<br>
 * <br>
 *      commandIdString is the command transaction id and can be used to associate commands with responses.
 * <p>                      
 * <br>
 * CommandResponse<br>
 * 			- gets-[ Entity&lt;Receipt&gt;(idNumber,COMMAND_RESPONSE,ROUTINE,Receipt,echo.Command.WhoAmI(commandIdString),id, attributeData ]<br>
 *                   Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a))<br>
 * <p></code>
 * Some of the methods are synchronized to keep the pendingCommands safe.  It might be possible to trim this some later.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 06
 * </pre> 
 */
public class LocalCommander implements Commander, ResourceInterface, Verbose  {
	
	// ========================================================================================================
	// FIELDS
	
	/**
	 * Command tag.
	 */
	public final static String COMMAND_ID_TAG = "CM";
	
	// ========================================================================================================
	// DATA
	
	/**
	 * This protects against collecting too many disposed but not freed command responses.  After draining this many responses,
	 * it will allow the disposal to run.
	 */
	private static final int 	DEFAULT_DISPOSAL_WAIT_COUNT = 25;
	
	// DATA
	private Conduit		myCommandConduit;
	private Conduit		myResponseConduit;
	private ConduitID	myResponseConduitID;	// speed up
	private BasicPullDrainContainer	responseDrain;
	private Injector	commandInjector;
	private WhoAmI		myId;
	private HashMap<String, LocalCommandResponse>		pendingCommands;
	private int			disposalWaitCount;
	
	// STATIC
	private static AtomicInteger	uniqueId = new AtomicInteger();

	
	// ===================================================================================================
	// IMPLEMENTATION
	
	/**
	 * Default constructor is not allowed.
	 */
	//public LocalCommander() throws SystemException {
	//	SystemException.softwareProblem("Cannot call default constructor in Commander.");
	//}
	
	/**
	 * Construct a commander.
	 */
	public LocalCommander(Conduit commandConduit, Conduit responseConduit, WhoAmI id) throws SystemException {
		
		// Validate
		if (id == null) SystemException.softwareProblem("Commander(commandConduit) instantiated with a null id.  Bad thing.");
		if (commandConduit == null) SystemException.softwareProblem("Commander(commandConduit) instantiated with a null conduit.  Bad thing.");
		myCommandConduit = commandConduit;
		if (responseConduit == null) SystemException.softwareProblem("Commander(responseConduit) instantiated with a null conduit.  Bad thing.");
		myResponseConduit = responseConduit;
		myResponseConduitID = responseConduit.getId();
		
		// Data
		myId = id;
		pendingCommands = new HashMap<String, LocalCommandResponse>();
		disposalWaitCount = 0;
		
		// Build channels
		try {
			//  Build a pull drain on the response channel.
			responseDrain = new BasicPullDrainContainer();
			responseConduit.registerPullDrain(responseDrain);

			// Get my injector.
			commandInjector = commandConduit.getInjector(Conduit.InjectorType.REQUIRE_FIRST_DRAIN, myId.toString());
		
		} catch (Exception e) {
			throw new SystemException("Could not build the LocalCommander", SystemException.SYSTEM_COMMAND_FAULT_COULD_NOT_BUILD_COMMANDER, e);
		}
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS
	// == For now these will be synchonized to keep thread issues at bay.  This is probibly ok, since each thread should create its own
	// == Commander anyway.
	
	/**
	 * This will issue a command.  It returns a receipt for the issuance, not for the other all interaction.  If there was something
	 * wrong with the issuance process, it will throw an exception.  It will not issue a BAD receipt.  
	 * @param commandToIssue The command to issue.
	 * @throws things.thinger.SystemException
	 */
	public synchronized Receipt issueCommand(Command		commandToIssue) throws SystemException {
		
		AttributeReader attributeData;
		Receipt result = null;
		
		// Validate
		if (commandToIssue==null) throw new SystemException("Cannot issue a null as a command", SystemException.SYSTEM_COMMAND_ERROR_NULL_COMMAND);
		screech("Command arrived: " + commandToIssue.named());
		
		try {

			// Formulate the command ID.  This must always be unique.  We will not even check to see if it is.
			int idNumber = uniqueId.getAndIncrement();
			result = new Receipt(myId, new String(myId.toString() + ThingsProperty.PROPERTY_PATH_SEPARATOR + commandToIssue.named() + ThingsProperty.PROPERTY_PATH_SEPARATOR + idNumber), Receipt.Type.ACCEPTANCE);
			
			// Get the command instance.
			attributeData = commandToIssue.getInstanceData();
		
			// Assemble the Data.  The commandID is how we will correlate commands with response Data.
			LocalCommandInfo info = new LocalCommandInfo();
			info.commandName = commandToIssue.named();
			info.responseConduit = myResponseConduitID;
			Entity<LocalCommandInfo> commandEntity = new Entity<LocalCommandInfo>(idNumber, Type.COMMAND, Priority.ROUTINE, info,
					                                                new WhoAmISimple(result.toString(),COMMAND_ID_TAG), myId, attributeData);
			
			// Construct the reponse here so any exceptions will prevent a bad injection.
			LocalCommandResponse response = new LocalCommandResponse(commandToIssue.getDefinition(), this);
			
			// Submit it.
			ReceiptList injectionReceipts =  commandInjector.post(commandEntity);
			Receipt firstOk = injectionReceipts.firstOk();
			if (firstOk == null) {
				
				// It was not ok!  The command will have been rejected.
				throw new SystemException("Command rejected during issuance due to no receipts.", SystemException.SYSTEM_COMMAND_ERROR_ISSUANCE_NO_RECEIPTS);

			} else {
				
				// Issue receipt, wrap with command response, and remember it.
				pendingCommands.put(result.toString(), response);
				screech("Command issued:" + result.toString());
			}
			
		} catch (SystemException se) {
			
			// Is it just that no one is listening?
			if (se.numeric == SystemException.IO_CONDUIT_ERROR_POSTED_TO_NO_DRAINS) {
				
				// This is kinda ok, so just warn.
				throw new SystemException("No one listening for the command.", SystemException.SYSTEM_COMMAND_WARNING_NO_ONE_LISTENING, se, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
				
			} else {
				
				// Actual problem
				if (se.isWorseThanError()) throw new SystemException("Command issuance FAULT.", SystemException.SYSTEM_COMMAND_FAULT_DURING_ISSUANCE, se, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
				throw new SystemException("Command issuance FAILED.", SystemException.SYSTEM_COMMAND_ERROR_ISSUANCE_FAILED, se, SystemNamespace.ATTR_PLATFORM_MESSAGE, se.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
			}

		} catch (Throwable t) {
			 throw new SystemException("Command issuance FAULT due to infrastructure.", SystemException.SYSTEM_COMMAND_FAULT_DURING_ISSUANCE, t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
		}	
		
		return result;
	}
	
	/**
	 * Query the reponse progress a command.  It is up to the implementation as to when responses are handled.  If they
	 * are not handled in a separate thread, it is possible this call with take time to handle responses.  The implementation will
	 * make a best-effort to keep InterruptedExceptions from fouling the process.  If it successful, the interruption will propagate
	 * as a simple WARNING level SystemException.  If not, you could see anything up to a PANIC.
	 * <br>
	 * If the resposne is not found, it will throw an exception.
	 * @param commandReceipt The issuance receipt. 
	 * @return A response object.
	 * @throws things.thinger.SystemException
	 */
	public synchronized CommandResponse queryResponse(Receipt	commandReceipt) throws SystemException {
		
		// Validate
		if (commandReceipt==null) throw new SystemException("Cannot ask for a null response.", SystemException.SYSTEM_COMMAND_ERROR_NULL_RESPONSE_RECEIPT);

		// Drain
		drainResponses();
		
		// Give him a response
		CommandResponse result = null;
		if (pendingCommands.containsKey(commandReceipt.toString())) {
			result = pendingCommands.get(commandReceipt.toString());
		} else {
			throw new SystemException("Command not outstanding for this commander.", SystemException.SYSTEM_COMMAND_ERROR_COMMAND_NOT_FOUND, SystemNamespace.ATTR_SYSTEM_COMMAND_ID, commandReceipt.getNote());
		}
		return result;
	}
	
	/**
	 * Allow the disposal procecure to run.  It is safe to call this any time.  It will sweep any DISPOSED responses
	 * from the system, which may build up over time.  The CommandResponse implementations may or may not call it.
	 * <p>
	 * This implementation will remove any references to it to it can go away for good.  Any delayed command responses will get dropped
	 * and a WARNING will be logged.
	 * @throws things.thinger.SystemException
	 */
	public synchronized void disposal() throws SystemException {
		
		LocalCommandResponse current;
		for (Entry<String,LocalCommandResponse> item : pendingCommands.entrySet() ) {
			current = item.getValue();
			if (current.getState() == ResponseState.DISPOSED) {
				pendingCommands.remove(item.getKey());
			}
		}
	}
		
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Spend some time draining responses.  A call to queryResponse will always do this.  The LocalCommandResponse should do this.  Anyone else
	 * not using LocalCommandResponse should definately do this from time to time.
	 * @throws things.thinger.SystemException
	 */
	@SuppressWarnings("unchecked")
	public void drainResponses() throws SystemException {
		
		// Give some time to queue drains
		try {
			Data outputItem = responseDrain.poll();
			Entity<Receipt> item;
			String commandIdEcho = null;
			LocalCommandResponse cachedResponse = null;
			while (outputItem != null) {
			
				// Validate the item.
				item = (Entity<Receipt>)outputItem;
				
				// Find a home.
				commandIdEcho = item.getID().toString();
				if (pendingCommands.containsKey(commandIdEcho)) {
					
					cachedResponse = pendingCommands.get(commandIdEcho);
					cachedResponse.add(item);
					
				} else {
					screech("Unmatched response.  Discarding.  id="  + pendingCommands + " info=" + outputItem.toString());
				}
				
				// Next
				outputItem = responseDrain.poll();
			}
			
		// Check the disposal wait count.
			if (disposalWaitCount >= DEFAULT_DISPOSAL_WAIT_COUNT) {
				disposal();
				disposalWaitCount = 0;
			} else {
				disposalWaitCount++;
			}
			
		//} catch (InterruptedException ie) {
			
		} catch (ClassCastException ie) {
			// Something other than (Entity<Receipt>) got into the drain.  BAD!
			throw new SystemException("Command response FAULT.  Something other than a response object got into the response conduit.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_OBJECT_BAD, ie, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
			
		} catch (SystemException se) {
			
			// Actual problem
			if (se.isWorseThanError()) throw new SystemException("Command response processing FAULT.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, se, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
			throw new SystemException("Command response processing FAILED.", SystemException.SYSTEM_COMMAND_ERROR_COMMAND_RESPONSE_PROCESSING, se, SystemNamespace.ATTR_PLATFORM_MESSAGE, se.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
			
		} catch (Throwable t) {
			if (t instanceof InterruptedException) throw new SystemException("Interrupted.  This commander is now unreliable", SystemException.PANIC_SYSTEM_COMMAND_INTERRUPTED_AND_UNRELIABLE, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
			throw new SystemException("Command response FAULT due to spurious exception.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t, SystemNamespace.ATTR_SYSTEM_COMMAND_COMMANDER_ID, myId.toString());
		}
		
	}
	
	// ===============================================================================
	// VERBOSE INTERFACE
	StringPoster debugPoster = null;
	
	/**
	 * VERBOSE INTERFACE.  DON'T TOUCH.
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
    	debugPoster = poster;
    }
	/**
	 * VERBOSE INTERFACE.  DON'T TOUCH.
	 */
	public void verboseOff() {
		debugPoster = null;
	}
	/**
	 * VERBOSE INTERFACE.  DON'T TOUCH.
	 */
	public void screech(String	message){
		if (debugPoster != null) debugPoster.postit(message);
	}
	/**
	 * VERBOSE INTERFACE.  DON'T TOUCH.
	 */
	public boolean isVerbose() {
		 if (debugPoster==null) return false;
		 return true;
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// RESOURCE INTERFACE
	WhoAmI resourceId;
	private State		state = State.NEW;
	
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public void initResource(WhoAmI  id) throws SystemException {
		resourceId = id;
		state = State.RUNNING;
	}	
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public void disposeResource() throws SystemException {
		state = State.DISPOSING;		
		try {
			myCommandConduit.disposeInjector(commandInjector);
		} catch (Throwable t) {
			// Don't care.
		}
		try {
			myResponseConduit.deRegisterPullDrain(responseDrain);
		} catch (Throwable t) {
			// Don't care.
		}	
		state = State.DEAD;	
	}	
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public boolean lock() throws SystemException {
		// Not supported.
		return false;
	}
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public void unlock() throws SystemException {
		// Not supported.
	}	
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public WhoAmI getId() throws SystemException {
		return resourceId;
	}
	/**
	 * RESOURCE INTERFACE IMPLEMENTATION.  LEAVE ALONE.
	 */
	public State getState() throws SystemException {
		return state;
	}
	
}
