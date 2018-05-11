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
package things.thinger.service.command;

import things.data.AttributeReader;
import things.data.Entity;
import things.data.Receipt;
import things.data.ReceiptList;
import things.thinger.SystemException;

/**
 * A command response interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 APR 06
 * </pre> 
 */
public interface CommandResponse {

	final static long serialVersionUID = 1;
	
	// =======================================================================================================
	// == DEFINITIONS
	
	/**
	 * The response state disposition describes state of the interaction.  They are as follows:<p>
	 * <code>
	 * OPEN     : command has been sent, but there has been no response.  <br>
	 * STARTED  : command has been sent, there has been some response, but it is not done. <br>
	 * DONE     : command has been sent and the response is complete (or errored).<br>
	 * DISPOSED : everything is done and it has been disposed--waiting garbage collection.<br> 
	 * </code><p>
	 */
	public enum ResponseState { OPEN, STARTED, DONE, DISPOSED };
	
	/**
	 * The completion disposition describes the response.  They are as follows:<p>
	 * <code>
	 * OPEN : the reponse is not done.  <br>
	 * GOOD : the response is done and well formed. <br>
	 * BAD  : the response is done but violates the scheme.<br>
	 * </code><p>
	 */
	public enum CompletionDisposition { OPEN, GOOD, BAD };

	// =======================================================================================================
	// == METHODS
	
	/**
	 * Get state of the command response.
	 * @return the State
	 */
	public ResponseState getState();
	
	/**
	 * Assuming the response is done, was the reponse well formed per the scheme?
	 * @param throwReason If true and the disposition is BAD, it will throw a SystemException for the reason that makes it BAD.
	 * @throws things.thinger.SystemException
	 */
	public CompletionDisposition validateCompletion(boolean throwReason) throws SystemException;

	/**
	 * Get the final receipt.
	 * @return the terminal receipt for the interaction, or null if it isn't DONE.
	 * @see things.data.Receipt
	 */
	public Receipt getFinalReceipt();
	
	/**
	 * Get the receipt list.
	 * @return the complete receipt list for the interaction, or null if it isn't DONE.
	 * @see things.data.Receipt
	 */
	public ReceiptList getReceiptList();

	/**
	 * Get next available submission.  If there is an exception, it'll be from the underlying implementation and not something specific to API usage.
	 * @return submission, or null if nothing is available.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public Entity<Receipt> next() throws SystemException;
	
	/**
	 * Wait for a submission and then get it.  If there is an exception, it'll be from the underlying implementation and not something specific to API usage.
	 * @return submission, or null if nothing is available.
	 * @param timeout a timeout period in milliseconds.  It'll throw a ThingsCodes.SYSTEM_SERVICE_RESPONSE_TIMEOUT SystemException at the end of the timeout.  This is a harmless exception and can be safely thrown away.  The timeout resolution isn't exact, so don't base any critical timing on it.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public Entity<Receipt> waitResponse(int timeout) throws SystemException;
	
	/**
	 * Get a rollup of all the attributes.  It will validate against the scheme.
	 * @return submission, or null if it is not DONE.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public AttributeReader rollup() throws SystemException;
	
	/**
	 * Wait for the whole transation to be done and then get a rollup of attributes.  It will validate against the scheme.
	 * @return submission, or null if it is not DONE.
	 * @param timeout a timeout period in milliseconds.  It'll throw a ThingsCodes.SYSTEM_SERVICE_RESPONSE_TIMEOUT SystemException at the end of the timeout.  This is a harmless exception and can be safely thrown away.  The timeout resolution isn't exact, so don't base any critical timing on it.
	 * @see things.data.Receipt
	 * @see things.data.Entity
	 * @throws things.thinger.SystemException
	 */
	public AttributeReader waitRollup(int timeout) throws SystemException;
	
	/**
	 * Dispose of the response, command, or anything else that might be associated with this interaction.
	 */
	public void dispose();
	
	
}
