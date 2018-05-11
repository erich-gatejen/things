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
package test.system.things.set1;

import test.system.SystemTestSuite_LogSequence;
import test.system.testtools.TestTools;
import things.common.tools.StringScanner;
import things.testing.unit.Test;


/**
 * TEST for simple system CLI commands.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 JUL 07
 * </pre> 
 */
public class TEST_Things_Set1 extends Test implements SystemTestSuite_LogSequence {
	
	/**
	 * Set 1, Root A.  Test branching quiet.  The results will only be reported by the root.
	 */
	private final static String	TEST_SET1A_BRANCHQUIET = "SET1 RootA PASS - Basic THING branches quiet and passes.";
	private final static String	TEST_SET1A_BRANCHQUIET_INVOKE_ARG0 = "test.system.universe_things.set1.THING_TEST_set1RootA";
	private final static String	TEST_SET1B_BRANCHQUIET = "SET1 RootB FAIL - Basic THING branches quiet and fails.";
	private final static String	TEST_SET1B_BRANCHQUIET_INVOKE_ARG0 = "test.system.universe_things.set1.THING_TEST_set1RootB";

	// ====================================================================================================
	// == DATA
	
	// Services
	
	// ====================================================================================================
	// == TEST
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("system.things.set1");
	    DECLARE(TEST_SET1A_BRANCHQUIET);
	    DECLARE(TEST_SET1B_BRANCHQUIET);	    
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		StringScanner logScanner = null;
		
		// Set 1 A
		try {
			logScanner = TestTools.runThingAndGetLog(TEST_SET1A_BRANCHQUIET_INVOKE_ARG0, true);
			if (!logScanner.seek("Root completed.")&&!logScanner.seek("thing.name=T_T_set1RootA")) PUNT("T_T_set1RootA did not complete. ('Root completed T_T_set1RootA')");
			if (!logScanner.seek("thing.result.type=PASS")) PUNT("T_T_set1RootA did not PASS. ('thing.result.type=PASS')");
			if (!logScanner.seek("metric.cases=2")) PUNT("T_T_set1RootA did not have 2 cases. ('metric.cases=2')");
			if (!logScanner.seek("metric.pass=2")) PUNT("T_T_set1RootA did not have 2 passes. ('metric.pass=2')");
			if (!logScanner.seek("THING_TEST_set1RootA execution complete")) PUNT("Kernel did not release THING_TEST_set1RootA");
			
			PASS(TEST_SET1A_BRANCHQUIET);
		} catch (Throwable t) {
			ABORT(TEST_SET1A_BRANCHQUIET,t.getMessage());
		}
		
		// Set 1 B
		try {
			logScanner = TestTools.runThingAndGetLog(TEST_SET1B_BRANCHQUIET_INVOKE_ARG0, true);
			if (!logScanner.seek("Starting as root T_T_set1RootB")) PUNT("T_T_set1RootB not started. ('Starting as root T_T_set1RootB')");
			if (!logScanner.seek("T_T_set1Branch2_1_PASS")&&!logScanner.seek("metric.pass=1")) PUNT("T_T_set1Branch2_1_PASS step failed.");
			if (!logScanner.seek("T_T_set1Branch2_PASS")&&!logScanner.seek("metric.pass=2")) PUNT("T_T_set1Branch2_PASS step failed.");
			if (!logScanner.seek("T_T_set1Branch2_1_FAIL")&&!logScanner.seek("metric.fail=1")&&!logScanner.seek("thing.result.type=FAIL")) PUNT("T_T_set1Branch2_1_FAIL step failed.");
			if (!logScanner.seek("T_T_set1Branch2_FAIL")&&!logScanner.seek("metric.fail=1")&&!logScanner.seek("thing.result.type=FAIL")&&
					!logScanner.seek("etric.cases=2 metric.pass=1")) PUNT("T_T_set1Branch2_FAIL step failed.");
			if (!logScanner.seek("T_T_set1Branch1_PASS")&&!logScanner.seek("metric.cases=1")&&!logScanner.seek("metric.pass=1")) PUNT("T_T_set1Branch1_PASS step failed.");
			if (!logScanner.seek("thing.name=T_T_set1RootB")&&!logScanner.seek("metric.fail=1")&&!logScanner.seek("thing.result.type=FAIL")&&
					!logScanner.seek("metric.count=3")&&!logScanner.seek("metric.cases=3")&&!logScanner.seek("metric.pass=2")) PUNT("T_T_set1RootB test result failed.");
			if (!logScanner.seek("Root completed")&&!logScanner.seek("THING_TEST_set1RootB")&&!logScanner.seek("metric.fail=1")&&
					!logScanner.seek("thing.result.type=FAIL")&&!logScanner.seek("metric.cases=3")&&!logScanner.seek("metric.pass=2")) PUNT("THING_TEST_set1RootB root completed failed.");
			if (!logScanner.seek("THING_TEST_set1RootB execution complete")) PUNT("Kernel did not release THING_TEST_set1RootB.");
					
			PASS(TEST_SET1B_BRANCHQUIET);
		} catch (Throwable t) {
			ABORT(TEST_SET1B_BRANCHQUIET,t.getMessage());
		}
		
	}
	
	// ====================================================================================================
	// == TOOLS AND INTERNAL
	

}
