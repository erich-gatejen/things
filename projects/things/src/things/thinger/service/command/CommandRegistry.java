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

import things.thinger.SystemException;

/**
 * A Command Registry.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 06
 * </pre> 
 */
public interface CommandRegistry {

	final static String NOT_NAMED = null;
		

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Get the generic global registry.
	 * @return a reference to the system 
	 */
	public CommandRegistry getGlobalRegistry();
	
	/**
	 * Does it have a command registered by this name?
	 * @param name The name.
	 * @return true if it does, otherwise false.
	 */
	public boolean has(String  name);
	/**
	 * Register a command.  It will throw an exception if the command is already registered.
	 * @param name The name.
	 * @param command The command.
	 * @see things.thinger.SystemException
	 */
	public void register(String  name, CommandDefinition command ) throws SystemException;

	/**
	 * Get a command.  It will throw an exception if the command is not registered.
	 * @param name The name.
	 * @return The definition.
	 * @see things.thinger.SystemException
	 */
	public CommandDefinition get(String  name) throws SystemException;
	
}
