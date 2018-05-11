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
package test.system.universe_things.set1;

import things.data.Data;
import things.thing.RESULT;
import things.thing.ResultExpectation;
import things.thing.THING;
import things.thing.UserException;

/**
 * A thing for testing RUN.  This is a branch2 for set 1.  It will FAIL internally and return a pass using default processing.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAR 07
 * </pre>
 */
public class THING_TEST_set1Branch2_FAIL extends THING {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	/**
	 * Names.
	 */
	public final static String RESULT_SET1BRANCH2_FAIL_STEP1 = "set1Branch2.fail.step1.localpass";
	public final static String RESULT_SET1BRANCH2_FAIL_STEP1_DESC = "set1Branch2.fail.step1.localpass.  It will PASS.";
	
	public final static String RESULT_SET1BRANCH2_FAIL_STEP2 = "set1Branch2.fail.step2.2_1fail";
	public final static String RESULT_SET1BRANCH2_FAIL_STEP2_DESC = "set1Branch2.fail.step2.2_1fail.  Call THING_TEST_set1Branch2_1_FAIL.";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * Definition step.
	 */
	public void DEFINITION() throws UserException {
		DEFINE_RESULT(RESULT_SET1BRANCH2_FAIL_STEP1, RESULT_SET1BRANCH2_FAIL_STEP1_DESC, ResultExpectation.PASS_FAIL);		
		DEFINE_RESULT(RESULT_SET1BRANCH2_FAIL_STEP2, RESULT_SET1BRANCH2_FAIL_STEP2_DESC, ResultExpectation.PASS_FAIL);
	}
	
	/**
	 * The process can only be called once per instance!
	 * @throws any Throwable.  It's important to let InterruptedException's to escape or the system may pound your THING with interruptions.
	 */
	public void PROCESS() throws Throwable {

		// Step 1
		SET_RESULT(RESULT_SET1BRANCH2_FAIL_STEP1, Data.Type.PASS);
		
		// Step 2
		RESULT step2Result = CALL("test.system.universe_things.set1.THING_TEST_set1Branch2_1_FAIL");
		SET_RESULT(RESULT_SET1BRANCH2_FAIL_STEP1, step2Result);	
		
	}
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public String GET_NAME() {
		return "T_T_set1Branch2_FAIL";
	}
	
}
