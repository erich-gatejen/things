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
package things.thinger.service.command.impl;

import things.data.NVImmutable;
import things.thinger.SystemException;
import things.thinger.service.command.Command;

/**
 * An implemented command.  This is a ping-pong used for testing.  It requires a ping parameter and will respond with a pong and
 * some nonsense.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JAN 07
 * </pre> 
 */
public class Command_PINGPONG extends Command {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	final static long serialVersionUID = 1;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == I/O NAMES
	public final static String NAME = "things.pingpong";
	public final static String PARAMETER_PING = "ping";
	public final static String RESPONSE_PONG = "pong";
	public final static String RESPONSE_SECOND_PONG = "pong.2";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Command declaration.  Do not call this directly!
	 * @throws things.thinger.SystemException
	 */
	public void declare() throws SystemException{
		DECLARE_NAME(NAME, this.getClass().getName());
		DECLARE_PARAMETER(PARAMETER_PING,	Requirement.REQUIRED,	Occurrence.ONLYONE, DataType.VALUE	);		
		DECLARE_RESPONSE(RESPONSE_PONG, 	Requirement.REQUIRED, 	Occurrence.MANY, 	DataType.LIST	);
		DECLARE_RESPONSE(RESPONSE_SECOND_PONG, 	Requirement.OPTIONAL, 	Occurrence.MANY, 	DataType.LIST	);
	}
	
	/**
	 * Return the official name of this command.  If there is another command named the same of  different class signature, it will cause
	 * a significant system fault.
	 * @return The official name of the command.
	 */
	public String named() {
		return NAME;
	}
	
	/**
	 * This will be called when the command is called.
	 * @throws things.thinger.SystemException
	 */
	public void accept() throws SystemException {
		
		// Get info
		NVImmutable thePing = GET_PARAMETER(PARAMETER_PING);
		
		// Set up response
		RESPOND(RESPONSE_PONG, thePing.getValue());
		
		// Flush
		FLUSH();
		
		// Set up response
		RESPOND(RESPONSE_SECOND_PONG, thePing.getValue(), "second response <much fun>, and stuff.");
		
		// Done
		DONE();
		
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ORGANIC
	
	
	
}
