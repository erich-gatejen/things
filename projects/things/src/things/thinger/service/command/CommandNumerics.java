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

import things.thinger.SystemNamespace;

/**
 * The command numerics.  This will associate a Java command numeric with an integer.  Also, the parameters and responses for each command are defined.
 * For channel based commands, all parameters and responses are open-ended strings (UTF-8).
 * <p>
 * Note that every command will end with an attribute(name) SystemNamespace.COMMAND_REASON valued as SystemNamespace.COMMAND_REASON_DONE.
 * <p>
 * Any response can be interrupted with an attribute(name) SystemNamespace.COMMAND_REASON valued as SystemNamespace.COMMAND_REASON_EXCEPTION.  It does not 
 * necessarily mean all the response data is bad.
 * <p>
 * This hasn't really been implemented.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 30 MAR 06
 * </pre> 
 */
public interface CommandNumerics {

	/**
	 * No operations.<p>
	 * There are no parameters.
	 */
	public static final int NUMERIC_COMMAND_NOP = 0;
	
	/**
	 * Get the process list.<p><pre>
	 * There are no parameters.<p>
	 * RESPONSE SEQUENCE:<br>
	 * 1- (REQUIRED, ONLY ONE)       - SystemNamespace.COMMAND_REASON_HEADER, (header strings)*<br>
	 * 2- (OPTIONAL, ZERO OR MORE)   - SystemNamespace.COMMAND_REASON_ENTRY, INTEGER.number, (column strings)*<br>
	 * 3- COMMAND_REASON_DONE
	 * </pre>
	 */	
	public static final int NUMERIC_PROCESS_LIST = 1;
	
	/**
	 * Get state of a named process.<p><pre>
	 * PARAMEMTERS (name/value): <br>
	 * 1- (REQUIRED, ONLY ONE)       - PROCESS_STATE_1_ID: Textual id for the process.<br>
	 * <p>
	 * 1- (REQUIRED, ONLY ONE)       - SystemNamespace.COMMAND_REASON_HEADER, (header strings)*<br>
	 * 2- (OPTIONAL, ZERO OR MORE)   - SystemNamespace.COMMAND_REASON_ENTRY, INTEGER.number, (column strings)*<br>
	 * </pre>
	 */		
	public static final int NUMERIC_PROCESS_STATE = 2;
	public static final String PROCESS_STATE_1_ID = SystemNamespace.ATTR_PROCESS_ID;

}
