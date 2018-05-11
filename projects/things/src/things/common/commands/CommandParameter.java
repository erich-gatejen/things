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
package things.common.commands;

/**
 * A parameter definition for command parameters.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 06
 * </pre> 
 */
public class CommandParameter {
	
	// =============================================================================
	// == FIELDS
	
	/**
	 * Type of parameter.  
	 */
	public CommandLine.PARAMETER_TYPES	type;
	
	/**
	 * Name of the parameter.
	 */
	public String						name;
	
	/**
	 * Message associated with the parameter. It is used for usage reporting/
	 */
	public String						message;
	
	/**
	 * Is the parameter required?
	 */
	public boolean						required;
	
	// =============================================================================
	// == METHODS
	
	/**
	 * The only constructor.
	 * @param theType
	 * @param theName
	 * @param theMessage
	 * @param isRequired
	 */
	public CommandParameter(CommandLine.PARAMETER_TYPES  theType, String theName, String theMessage, boolean isRequired) {
		type = theType;
		name = theName;
		message = theMessage;
		required = isRequired;
	}
}