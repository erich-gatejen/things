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

import things.data.Receipt;
import things.thinger.SystemException;

/**
 * Interface to a commander.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 06
 * </pre> 
 */
public interface Commander {
	
	/**
	 * This will issue a command.  It returns a receipt for the issuance, not for the other all interaction.
	 * @param commandToIssue The command to issue.
	 * @throws things.thinger.SystemException
	 */
	public Receipt issueCommand(Command		commandToIssue) throws SystemException;
	
	/**
	 * Query the reponse progress a command.  It is up to the implementation as to when responses are handled.  If they
	 * are not handled in a separate thread, it is possible this call with take time to handle responses.  The implementation will
	 * make a best-effort to keep InterruptedExceptions from fouling the process.  If it successful, the interruption will propagate
	 * as a simple WARNING level SystemException.  If not, you could see anything up to a PANIC.
	 * @param commandReceipt The issuance receipt. 
	 * @return A response object.
	 * @throws things.thinger.SystemException
	 */
	public CommandResponse queryResponse(Receipt	commandReceipt) throws SystemException;
	
	
	/**
	 * Allow the disposal procecure to run.  It is safe to call this any time.  It will sweep any DISPOSED responses
	 * from the system, which may build up over time.  The CommandResponse implementations may or may not call it.
	 * @throws things.thinger.SystemException
	 */
	public void disposal() throws SystemException;
	
}
