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

import things.thing.RESULT;
import things.thing.ResultExpectation;
import things.thing.THING;
import things.thing.UserException;

/**
 * A thing for testing RUN.  This is the root THING for set1 tests with branches.  This will be an overall fail, with
 * one pass and on fail subs.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAR 07
 * </pre>
 */
public class THING_TEST_set1RootB extends THING {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	/**
	 * Names.
	 */
	public final static String RESULT_SETROOT1B_STEP1 = "set1RootB.step1.branch2.pass";
	public final static String RESULT_SETROOT1B_STEP1_DESC = "set1RootB.step1.pass.  Double branch pass.";
	
	public final static String RESULT_SETROOT1B_STEP2 = "set1RootB.step1.branch2.fail";
	public final static String RESULT_SETROOT1B_STEP2_DESC = "set1RootB.step1.branch2.fail.  Double branch fail.";

	public final static String RESULT_SETROOT1B_STEP3 = "set1RootB.step1.branch1.pass";
	public final static String RESULT_SETROOT1B_STEP3_DESC = "set1RootB.step1.branch1.pass.  Double branch pass.";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * Definition step.
	 */
	public void DEFINITION() throws UserException {
		DEFINE_RESULT(RESULT_SETROOT1B_STEP1, RESULT_SETROOT1B_STEP1_DESC, ResultExpectation.PASS_FAIL);
		DEFINE_RESULT(RESULT_SETROOT1B_STEP2, RESULT_SETROOT1B_STEP2_DESC, ResultExpectation.PASS_FAIL);	
		DEFINE_RESULT(RESULT_SETROOT1B_STEP3, RESULT_SETROOT1B_STEP3_DESC, ResultExpectation.PASS_FAIL);	
	}
	
	/**
	 * The process can only be called once per instance!
	 * @throws any Throwable.  It's important to let InterruptedException's to escape or the system may pound your THING with interruptions.
	 */
	public void PROCESS() throws Throwable {
		
		// Step 1 - pass
		RESULT step1Result = CALL("test.system.universe_things.set1.THING_TEST_set1Branch2_PASS");
		SET_RESULT(RESULT_SETROOT1B_STEP1, step1Result);
		
		// Step 2 - fail
		RESULT step2Result = CALL("test.system.universe_things.set1.THING_TEST_set1Branch2_FAIL");
		SET_RESULT(RESULT_SETROOT1B_STEP2, step2Result);	
		
		// Step 3 - pass
		RESULT step3Result = CALL("test.system.universe_things.set1.THING_TEST_set1Branch1_PASS");
		SET_RESULT(RESULT_SETROOT1B_STEP3, step3Result);	
		
	}
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public String GET_NAME() {
		return "T_T_set1RootB";
	}
	
}
