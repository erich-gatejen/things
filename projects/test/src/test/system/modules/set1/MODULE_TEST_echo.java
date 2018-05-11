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
package test.system.modules.set1;

import things.thing.MODULE;
import things.thing.UserException;

/**
 * A module for testing RUN.  It will define a single result and pass it.<br>      
 * <p>
 * NOTES:<br>
 * Logging is currently done with a system logger.  May want to change that.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public class MODULE_TEST_echo extends MODULE {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	/**
	 * Names.
	 */
	public final static String PING_RESPONSE = "pong";
	
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * This will be called during initialization.  Expect it to happen at any time; if the module is already in use, you should 
	 * re-initialize it.  The System Data fields will be set when this is called.
	 */
	public void INITIALIZE() throws UserException {
		// Nothing, really.
	}
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public String GET_NAME() {
		return "MODULE_TEST_echo";
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Just ping it. 
	 * @param whisper what to pong.
	 * @return the pong
	 */
	public String ping(String whisper) {
		return whisper;
	}
	
	
}
