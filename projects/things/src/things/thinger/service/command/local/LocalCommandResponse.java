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

import java.util.Collection;
import java.util.Vector;

import things.data.AttributeReader;
import things.data.Entity;
import things.data.NVImmutable;
import things.data.Receipt;
import things.data.ReceiptList;
import things.data.impl.ReadWriteableAttributes;
import things.thinger.SystemException;
import things.thinger.service.command.Command;
import things.thinger.service.command.CommandDefinition;
import things.thinger.service.command.CommandResponse;

/**
 * A local command response implementation.  This is <b>NOT</b> thread safe!!!  This will work only with the other Local implementations.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 APR 06
 * </pre> 
 */
public class LocalCommandResponse implements CommandResponse {

	final static long serialVersionUID = 1;
	final static int WAIT_SLICE = 100;	// Bad polling hack.  It's this or actually put a thread on managing command responses.  No thank you.
	
	// =======================================================================================================
	// == DATA
	private ResponseState state;
	private int rover = 0;
	private Vector<Entity<Receipt>> responses;
	private CommandDefinition commandDefinition;
	private ReadWriteableAttributes	rollupAttributes;
	private LocalCommander myCommander;
	
	// =======================================================================================================
	// == CONSTRUCTOR
	
	/**
	 * The Local command response.  This will only work with a LocalCommander since it needs to help drain it.
	 */
	public LocalCommandResponse(CommandDefinition	originalCommandDefinition, LocalCommander commander) {
		state = ResponseState.OPEN;
		commandDefinition = originalCommandDefinition;
		responses = new Vector<Entity<Receipt>>();
		myCommander = commander;
		
		// Rollups
		rollupAttributes = new ReadWriteableAttributes();
		rollupAttributes.allowMulti();
	}
	
	// =======================================================================================================
	// == INTERFACE METHODS
	
	/**
	 * Get state of the command response.
	 * @return the State
	 */
	public ResponseState getState() {
		return state;
	}
	
	/**
	 * Assuming the response is done, was the reponse well formed per the scheme?
	 * @param throwReason If true and the disposition is BAD, it will throw a SystemException for the reason that makes it BAD.
	 * @throws things.thinger.SystemException
	 */
	public synchronized CompletionDisposition validateCompletion(boolean throwReason) throws SystemException {
		CompletionDisposition result = CompletionDisposition.OPEN;

		// Yield to drain responses.  Let the exception propagate as is.
		myCommander.drainResponses();
		
		// Check required
		try {
			Command.checkRequiredData(rollupAttributes, Command.CheckType.RESPONSE, commandDefinition);
			Command.checkDataForm(rollupAttributes, Command.CheckType.RESPONSE, commandDefinition);
			result = CompletionDisposition.GOOD;
			
		} catch (SystemException se) {
			if (throwReason) throw se;
			else result = CompletionDisposition.BAD;
			
		} catch (Throwable t) {
			throw new SystemException("Spurious exception during validation.", SystemException.SYSTEM_COMMAND_FAULT_SPURIOUS, t); 
		}

		
		// Report
		return result;	
	}

	/**
	 * Get the final receipt.
	 * @return the terminal receipt for the interaction, or null if it isn't DONE.
	 * @see things.data.Receipt
	 */
	public Receipt getFinalReceipt() {
		Receipt result;
		
		if (state != ResponseState.DONE) {
			// Not done.
			result = null;
			
		} else {
			// Done
			result = responses.lastElement().getTypedThing();
		}
		return result;		
	}
	
	/**
	 * Get the receipt list.
	 * @return the complete receipt list for the interaction, or null if it isn't DONE.
	 * @see things.data.Receipt
	 */
	public ReceiptList getReceiptList() {
		ReceiptList result;
		
		if (state != ResponseState.DONE) {
			// Not done.
			result = null;
			
		} else {
			// Done, so build the list.
			result = new ReceiptList();
			for (Entity<Receipt> item : responses) {
				result.add(item.getTypedThing());
			}
		}
		return result;	
	}

	/**
	 * Get next available submission. 
	 * @return submission, or null if nothing is available.
	 * @see things.data.Receipt
	 * @see things.data.Entity	 
	 * @throws things.thinger.SystemException
	 */
	public Entity<Receipt> next() throws SystemException {
		Entity<Receipt> result;
		
		// Yield to drain responses.  Let the exception propagate as is.
		myCommander.drainResponses();
		
		// Now check.
		if ( (state == ResponseState.OPEN) || (rover >= responses.size()) ) {
			// Not started or rover is already at the end, so return null.
			result = null;
			
		} else {
			// Give it something.
			result = responses.get(rover);
			// There is a chance we have a race here.  Check for a null before inc the rover.
			if (result != null)	rover++;
		}
		return result;
	}
	
	/**
	 * Wait for a submission and then get it.  If there is an exception, it'll be from the underlying implementation and not something specific to API usage.
	 * @return submission, or null if nothing is available.
	 * @param timeout a timeout period in milliseconds.  It'll throw a ThingsCodes.SYSTEM_SERVICE_RESPONSE_TIMEOUT SystemException at the end of the timeout.  This is a harmless exception and can be safely thrown away.  The timeout resolution isn't exact, so don't base any critical timing on it.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public Entity<Receipt> waitResponse(int timeout) throws SystemException {
		long timeLeft = timeout;
		long startTime = System.currentTimeMillis();
		Entity<Receipt> result = next();
		
		// Loop if result is null.
		while (result == null) {
			
			// If we exceeeded the timeout, throw the exception
			if (timeLeft <= 0) throw new SystemException("Timeout.", SystemException.SYSTEM_SERVICE_RESPONSE_TIMEOUT);
			
			try {
							
				// Wait
				synchronized(responses) {
					responses.wait(WAIT_SLICE);
				}
				
				// Calculate time lapse as soon as possible.
				timeLeft = timeLeft - (System.currentTimeMillis() - startTime);
				startTime = System.currentTimeMillis();
				
			} catch (InterruptedException i) {
				// Ok...  Don't care.
			}
			
			// Try again
			result = next();
		}
		
		return result;
	}
	
	/**
	 * Get a rollup of all the attributes.
	 * @return submission or null if it is not DONE.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public AttributeReader rollup() throws SystemException {
		AttributeReader result;
		
		if (state != ResponseState.DONE) {
			// Not done.
			result = null;
			
		} else {
			// Done
			result = rollupAttributes;
		}
		return result;			
	}
	
	/**
	 * Wait for the whole transation to be done and then get a rollup of attributes.  It will validate against the scheme.
	 * @return submission, or null if it is not DONE.
	 * @param timeout a timeout period in milliseconds.  It'll throw a ThingsCodes.SYSTEM_SERVICE_RESPONSE_TIMEOUT SystemException at the end of the timeout.  This is a harmless exception and can be safely thrown away.  The timeout resolution isn't exact, so don't base any critical timing on it.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public AttributeReader waitRollup(int timeout) throws SystemException {
		
		// wait for responses until it is done.
		while(state != ResponseState.DONE) {
			waitResponse(timeout);
		}
		return rollupAttributes;
	}
	
	/**
	 * Dispose of the response, command, or anything else that might be associated with this interaction.
	 * You should let your commander call this for you.
	 */
	public void dispose() {
		responses = null;
		state = ResponseState.DISPOSED;
		try {
			myCommander.disposal();
		} catch (Throwable t) {
			// We don't really care at this point.  We're just being nice.
		}
	}
	
	// =======================================================================================================
	// == METHODS
	
	/**
	 * Add a response item.  It may throw and exception if the processing doesn't make sense.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public void add(Entity<Receipt> responseItem) throws SystemException {
		
		if (responseItem==null) SystemException.softwareProblem("responseItem passed as a null.");
		
		try {
		
			// Get receipt.
			Receipt responseReceipt = responseItem.getTypedThing();
			
			// State based operation.
			switch (state) {
			
			case OPEN:
				addAndRollupAttributes(responseItem);
				state = ResponseState.STARTED;
				stateCheck(responseReceipt);
				break;
				
			case STARTED: 
				addAndRollupAttributes(responseItem);			
				stateCheck(responseReceipt);
				break;
				
			case DISPOSED:
				throw new SystemException("Response posted for Command that was already DISPOSED.", SystemException.SYSTEM_COMMAND_ERROR_COMMAND_ALREADY_DONE);			
				
			case DONE:
				throw new SystemException("Response posted for Command that was already DONE.", SystemException.SYSTEM_COMMAND_ERROR_COMMAND_ALREADY_DONE);	
				
			default:
				SystemException.softwareProblem("responseItem unexpected state.  name=" + state.toString());
			}
		
			// Notify
			synchronized(responses) {
				responses.notifyAll();
			}
				
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Response posting vailed to spurious exception.", SystemException.SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING, t);
		}
	}
	
	// =======================================================================================================
	// == HELPERS
	
	/**
	 * Check the state and modify as neccessary.
	 */
	private void stateCheck(Receipt responseReceipt) {	
		
		// Is it a terminal receipt?
		if (responseReceipt.getType().isTerminal()) {	
			state = ResponseState.DONE;
		}
	}
	
	/**
	 * Add the entity and roll up the attributes.<br>
	 * This is going to be brute force.
	 */
	private void addAndRollupAttributes(Entity<Receipt> responseItem) throws Throwable {
		
		responses.add(responseItem);
		Collection<NVImmutable> inputAttributes = responseItem.getAttributes().getAttributes();
		for (NVImmutable item : inputAttributes) {
			rollupAttributes.addAttribute(item);
		}
	}
	
}
