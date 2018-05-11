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
package test.system.cli;

import test.system.SystemTestSuite_LogSequence;
import test.system.testtools.CLIServiceWrapper;
import test.system.testtools.KernelBasic_WriterLogger_BroadInterceptionFactory;
import things.testing.unit.Test;
import things.thinger.service.local.CLIService;
import things.thinger.service.local.CLIServiceConstants;
import things.thinger.service.local.CLIServiceTools;

/**
 * TEST for the RUN command.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 JUL 07
 * </pre> 
 */
public class TEST_CLIService_RUN extends Test implements SystemTestSuite_LogSequence {
	
	private final static String	TEST_RUN_INVOKE = "Run invoke";
	private final static String	TEST_RUN_INVOKE_ARG0 = "test.system.universe_things.THING_TEST_echoRun";
	private final static String	TEST_RUN_FAIL_INVOKE = "Fail run invoke";
	private final static String	TEST_RUN_FAIL_INVOKE_ARG0 = "THING_TEST_echoRun";
	private final static String	TEST_RUN_LOGINTERCEPTOR_INVOKE = "Log interceptor";
	private final static String	TEST_RUN_LOGINTERCEPTOR_INVOKE_ARG0 = TEST_RUN_INVOKE_ARG0;
	// TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID is part of the SystemTestSuite_LogSequence 
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("system.cli.RUN");
	    DECLARE(TEST_RUN_INVOKE);
	    DECLARE(TEST_RUN_FAIL_INVOKE);
	    DECLARE(TEST_RUN_LOGINTERCEPTOR_INVOKE);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		CLIService service = null;
		
		// Invoke RUN
		try {
			service = CLIServiceWrapper.getService();
			String response = service.tender(CLIServiceConstants.COMMAND_RUN + " " + TEST_RUN_INVOKE_ARG0);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				PASS(TEST_RUN_INVOKE);
			} else {
				FAIL(TEST_RUN_INVOKE, "Response not ok.  response=" + response);			
			}
				
		} catch (Exception e) {
			ABORT(TEST_RUN_INVOKE,"Run invoke to exception:" + e.getMessage());
		}

		// Fail Invoke RUN
		try {
			String response = service.tender(CLIServiceConstants.COMMAND_RUN + " " + TEST_RUN_FAIL_INVOKE_ARG0);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				FAIL(TEST_RUN_FAIL_INVOKE, "It worked when it should have failed.  response=" + response);		

			} else {
				PASS(TEST_RUN_FAIL_INVOKE);
			}
				
		} catch (Exception e) {
			ABORT(TEST_RUN_FAIL_INVOKE,"Run (failed) invoke failed to exception:" + e.getMessage());
		}
		
		// Test the log interceptor (used by the test suite).
		try {
			KernelBasic_WriterLogger_BroadInterceptionFactory logInterceptor = KernelBasic_WriterLogger_BroadInterceptionFactory.getInterceptor();

			String response = service.tender(CLIServiceConstants.COMMAND_RUN + " " + TEST_RUN_LOGINTERCEPTOR_INVOKE_ARG0);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				
				// Get the value again one more time.
				String value = logInterceptor.get();
				
				// Validate value.
				if (value.indexOf("thing.result.type=PASS") < 0) PUNT("Log does not have expected token (thing.result.type=PASS).");
				if (value.indexOf("execution complete.  Quitting|nothing") < 0) PUNT("Log does not have expected token (execution complete.  Quitting|nothingS).");
				
				PASS(TEST_RUN_LOGINTERCEPTOR_INVOKE);	
			} else {
				FAIL(TEST_RUN_LOGINTERCEPTOR_INVOKE, "Invoke response not ok.  response=" + response);			
			}
				
		} catch (Exception e) {
			ABORT(TEST_RUN_LOGINTERCEPTOR_INVOKE,"Run invoke failed:" + e.getMessage());
		}
		
		
		/* Using the named interceptor.  We don't do that anymore 

		// Test the log interceptor (not used by the test suite).
		try {
			KernelBasic_WriterLogger_NamedInterceptionFactory logInterceptor = KernelBasic_WriterLogger_NamedInterceptionFactory.getInterceptor();
			ByteArrayOutputStream ibuffer = logInterceptor.intercept(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID);
			if (ibuffer == null) PUNT("Could not intercept() the log.");
			
			String response = service.transactionInterface.tender(CLIServiceConstants.COMMAND_RUN + " " + TEST_RUN_LOGINTERCEPTOR_INVOKE_ARG0);
			if ( (response.indexOf(CLIServiceConstants.RESPONSE_OK) == 0) && (response.indexOf("completed") > 0)) {
				
				// Get the value
				String beforeReleaseValue = logInterceptor.requestString(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID, false);
				
				// Make sure the logs aren't out of sequence.
				if ((beforeReleaseValue==null)||(beforeReleaseValue.indexOf(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID)<0)) PUNT("Log squence broken.  broken at " + TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID);
				
				// Release
				logInterceptor.release(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID);
				
				// Get the value again and clear it
				String releaseValue = logInterceptor.requestString(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID, true);
				
				// Get the value again one more time.
				String clearedValue = logInterceptor.requestString(TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID, false);
				if ((clearedValue!=null)&&(clearedValue.length() > 0)) PUNT("Cleared interception buffer is not zero length.");
				
				// Validate the before release.
				if (beforeReleaseValue.indexOf("thing.result.type=PASS") < 0) PUNT("beforeReleaseValue does not have expected token (thing.result.type=PASS).");
				if (beforeReleaseValue.indexOf("execution complete.  Quitting|nothing") < 0) PUNT("beforeReleaseValue does not have expected token (execution complete.  Quitting|nothingS).");
			
				// Validate the after release.
				if (releaseValue.indexOf("thing.result.type=PASS") < 0) PUNT("releaseValue does not have expected token (thing.result.type=PASS).");
				if (releaseValue.indexOf("execution complete.  Quitting|nothing") < 0) PUNT("releaseValue does not have expected token (execution complete.  Quitting|nothingS).");
				
				PASS(TEST_RUN_LOGINTERCEPTOR_INVOKE);	
			} else {
				FAIL(TEST_RUN_LOGINTERCEPTOR_INVOKE, "Invoke response not ok.  response=" + response);			
			}
				
		} catch (Exception e) {
			ABORT(TEST_RUN_LOGINTERCEPTOR_INVOKE,"Run invoke failed:" + e.getMessage());
		}
		
		*/
		
	}
}
