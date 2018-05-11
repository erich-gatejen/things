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

import test.system.KernelBasic_TestingStub;
import test.system.SystemTestSuite_LogSequence;
import test.system.testtools.TestTools;
import things.common.tools.StringScanner;
import things.testing.unit.Test;

/**
 * TEST for simple system MODULES.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 JUL 07
 * </pre> 
 */
public class TEST_Modules_Set1 extends Test implements SystemTestSuite_LogSequence {
	
	/**
	 * Set 1.  ECHO test.
	 */
	private final static String	TEST_SET1_ECHO_DIRECTINSTANCE = "SET1 ECHO - Direct Instance.";
	
	private final static String	TEST_SET1_ECHO_THING = "SET1 ECHO - Use THING_echo.";
	private final static String	TEST_SET1_ECHO_THING_INVOKE_ARG0 = "test.system.universe_things.THING_for_MODULE_TEST_echo";
	
	private final static String PING_PONG = "monkeyface";


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
	    DECLARE(TEST_SET1_ECHO_DIRECTINSTANCE);
	    DECLARE(TEST_SET1_ECHO_THING);	    
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		StringScanner logScanner = null;
		
		// Set 1 ECHO DIRECT
		try {

			KernelBasic_TestingStub kernelStub = KernelBasic_TestingStub.getStub();
			MODULE_TEST_echo myModule = (MODULE_TEST_echo) kernelStub.kernel.loadModule("test.system.modules.set1.MODULE_TEST_echo");
			
			String pong = myModule.ping(PING_PONG);
			if (!pong.equals(PING_PONG)) PUNT("The ping did not pong.");
			
			PASS(TEST_SET1_ECHO_DIRECTINSTANCE);
		} catch (Throwable t) {
			ABORT(TEST_SET1_ECHO_DIRECTINSTANCE,t.getMessage());
		}
		
		// Set 1 ECHO THING
		try {
			logScanner = TestTools.runThingAndGetLog(TEST_SET1_ECHO_THING_INVOKE_ARG0, true);
			if (!logScanner.seek("Root completed.")&&!logScanner.seek("thing.name=T_for_MODULE_TEST_echo")) PUNT("T_for_MODULE_TEST_echo did not complete. ('Root completed T_T_set1RootA')");
			if (!logScanner.seek("thing.result.type=PASS")) PUNT("MODULE_TEST_echo did not PASS. ('thing.result.type=PASS')");
			
			PASS(TEST_SET1_ECHO_THING);
		} catch (Throwable t) {
			ABORT(TEST_SET1_ECHO_THING,t.getMessage());
		}
		
		
		
		
	}
	
	// ====================================================================================================
	// == TOOLS AND INTERNAL
	

}
