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
package test.system.universe_things;

import things.data.Data;
import things.thing.ResultExpectation;
import things.thing.THING;
import things.thing.UserException;

/**
 * A thing for testing RUN.  It will define a single result and pass it.<br>      
 *<p>
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
public class THING_TEST_echoRun extends THING {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	/**
	 * Names.
	 */
	public final static String RESULT_NAME_1 = "result.name.1";
	public final static String RESULT_DESCRIPTION_1 = "Result decription for result.name.1";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * Definition step.
	 */
	public void DEFINITION() throws UserException {
		DEFINE_RESULT(RESULT_NAME_1, RESULT_DESCRIPTION_1, ResultExpectation.PASS_FAIL);
	}
	
	/**
	 * The process can only be called once per instance!
	 * @throws any Throwable.  It's important to let InterruptedException's to escape or the system may pound your THING with interruptions.
	 */
	public void PROCESS() throws Throwable {
		SET_RESULT(RESULT_NAME_1, Data.Type.PASS);
	}
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public String GET_NAME() {
		return "T_T_echoRun";
	}
	
}
