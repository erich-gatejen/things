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

import test.system.testtools.CLIServiceWrapper;
import things.testing.unit.Test;
import things.thinger.service.local.CLIService;
import things.thinger.service.local.CLIServiceConstants;
import things.thinger.service.local.CLIServiceTools;

/**
 * TEST for simple system CLI commands.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 FEB 07
 * </pre> 
 */
public class TEST_CLIService_Simple extends Test {
	
	private final static String	TEST_PING = "Ping";
	private final static String	TEST_PING_ARG0 = "bork";
	
	private final static String	TEST_PROCESSLIST = "Process List";
	private final static String	TEST_PROCESSLIST_LOG = "Process List (log only)";
	private final static String TEST_PROCESSLIST_VALIDATION1 = "header";
	private final static String TEST_PROCESSLIST_VALIDATION2 = "kernel";
	private final static String TEST_PROCESSLIST_VALIDATION3 = "PRIVILEGED";
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("system.cli.simple");
	    DECLARE(TEST_PING);
	    DECLARE(TEST_PROCESSLIST);
	    DECLARE(TEST_PROCESSLIST_LOG);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		CLIService service = null;
		
		// Ping
		try {
			service = CLIServiceWrapper.getService();
			String response = service.tender(CLIServiceConstants.COMMAND_PING + " " + TEST_PING_ARG0);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				PASS(TEST_PING);
			} else {
				FAIL(TEST_PING, "Response not ok.  response=" + response);			
			}
				
		} catch (Exception e) {
			ABORT(TEST_PING,"ping failed to exception:" + e.getMessage());
		}

		// Process list - log and cli
		try {
			String response = service.tender(CLIServiceConstants.COMMAND_PROCESSLIST);
			if ( (CLIServiceTools.isResponseOkAndComplete(response))  && (response.indexOf(TEST_PROCESSLIST_VALIDATION1) >= 0) &&
				 (response.indexOf(TEST_PROCESSLIST_VALIDATION2) >= 0)     && (response.indexOf(TEST_PROCESSLIST_VALIDATION3) >= 0)       ) {
				PASS(TEST_PROCESSLIST);
			} else {
				FAIL(TEST_PROCESSLIST, "Response not ok.  See log for response.");			
			}
				
		} catch (Exception e) {
			ABORT(TEST_PROCESSLIST,"process list failed to exception:" + e.getMessage());
		}	
		
		// Process list - log only - the CLI output should not contain any validations, just the OK.
		try {
			String response = service.tender(CLIServiceConstants.COMMAND_PROCESSLIST + " =" + CLIServiceConstants.COMMAND_PROCESSLIST_LOG_VALUE);
			if ( (CLIServiceTools.isResponseOkAndComplete(response))  && (response.indexOf(TEST_PROCESSLIST_VALIDATION1) < 0) &&
			     (response.indexOf(TEST_PROCESSLIST_VALIDATION2) < 0)      && (response.indexOf(TEST_PROCESSLIST_VALIDATION3) < 0)       ) {
				PASS(TEST_PROCESSLIST_LOG);
			} else {
				FAIL(TEST_PROCESSLIST_LOG, "Response not ok.  See log for response.");			
			}
				
		} catch (Exception e) {
			ABORT(TEST_PROCESSLIST_LOG,"process list (log only) failed to exception:" + e.getMessage());
		}	

	}
}
