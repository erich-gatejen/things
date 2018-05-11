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
import things.data.NV;
import things.thinger.SystemException;
import things.thinger.SystemInterface;

/**
 * An interface for handling responses.  This will be used by Command and supplied by the CommandService.  This will extend the AttributeReader
 * that exposes the items sent as part of the command.  No need to access those directly; let the commander handle it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 APR 06
 * </pre> 
 */
public interface CommandResponder {

	// =======================================================================================================
	// == METHODS
	
	/**
	 * Get the command attributes.
	 * @throws SystemException
	 */
	public AttributeReader getCommandAttributes() throws SystemException;

	/**
	 * Add a response.
	 * @param item the item
	 * @throws SystemException
	 */
	public void add(NV	item) throws SystemException;
	
	/**
	 * Get a system interface.
	 * @return A system interface.
	 * @throws SystemException
	 */
	public SystemInterface getSystemInterface() throws SystemException;
	
	/**
	 * Remove a response for replacement, if possible.  If the reponder has been flushed, the response may have already been transmitted, and then it is too late.
	 * So be careful with this.
	 * @param name the item to replace
	 * @throws SystemException
	 */
	public void remove(String name) throws SystemException;
	
	/**
	 * Force the responder to flush.  This should make it transmit.
	 * @throws SystemException
	 */
	public void flush() throws SystemException;
	
	/**
	 * Make the response done.  This will flush.  Any further adds or replaces will result in an exception.
	 * @throws SystemException
	 */
	public void done() throws SystemException;
}
