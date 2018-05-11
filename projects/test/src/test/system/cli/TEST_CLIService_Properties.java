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
import things.common.tools.StringScanner;
import things.testing.unit.Test;
import things.thinger.service.local.CLIService;
import things.thinger.service.local.CLIServiceConstants;
import things.thinger.service.local.CLIServiceTools;


/**
 * TEST for simple system CLI commands.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 10 FEB 07</code> 
 * 
 */
public class TEST_CLIService_Properties extends Test {
	
	private final static String	TEST_SETPROP_1 = "Set properties #1";
	private final static String	TEST_SETPROP_1_NAME = "TSP1_name1";
	private final static String	TEST_SETPROP_1_VALUE = "TSP1_value1";
	private final static String	TEST_SETPROP_2 = "Set properties #2 - sub 1";
	private final static String	TEST_SETPROP_2_NAME = "TSP1_name1.sub1";
	private final static String	TEST_SETPROP_2_VALUE = "TSP1_value2";
	
	private final static String	TEST_CHECKPROP_NORMAL = "Check properties, normal.";
	private final static String	TEST_CHECKPROP_SUPPRESSED = "Check properties, suppressed output.";
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("system.cli.properties");
	    DECLARE(TEST_SETPROP_1);
	    DECLARE(TEST_SETPROP_2);
	    DECLARE(TEST_CHECKPROP_NORMAL);
	    DECLARE(TEST_CHECKPROP_SUPPRESSED);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		CLIService service = null;
		String response = null;
		
		// Parameter 1
		try {
			service = CLIServiceWrapper.getService();
			response = service.tender(CLIServiceConstants.COMMAND_SETPROP + " " + TEST_SETPROP_1_NAME + " " + TEST_SETPROP_1_VALUE);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				PASS(TEST_SETPROP_1);
			} else {
				FAIL(TEST_SETPROP_1, "Response not ok.  response=" + response);			
			}	
		} catch (Exception e) {
			ABORT(TEST_SETPROP_1,"ping failed to exception:" + e.getMessage());
		}
		// Parameter 2
		try {
			response = service.tender(CLIServiceConstants.COMMAND_SETPROP + " " + TEST_SETPROP_2_NAME + " " + TEST_SETPROP_2_VALUE);
			if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
				PASS(TEST_SETPROP_2);
			} else {
				FAIL(TEST_SETPROP_2, "Response not ok.  response=" + response);			
			}	
		} catch (Exception e) {
			ABORT(TEST_SETPROP_2,"ping failed to exception:" + e.getMessage());
		}
		
		
		// Process list - log and cli
		StringScanner scanner = new StringScanner();
		try {
			response = service.tender(CLIServiceConstants.COMMAND_SHOWPROPS);
			scanner.start(response);
			if (!CLIServiceTools.isResponseOkAndComplete(response)) PUNT("Response not OK.");
			if (!scanner.seek(TEST_SETPROP_1_NAME)) PUNT("Didn't find " + TEST_SETPROP_1_NAME);
			if (!scanner.seek(TEST_SETPROP_1_VALUE)) PUNT("Didn't find " + TEST_SETPROP_1_VALUE);
			scanner.reset();
			if (!scanner.seek(TEST_SETPROP_2_NAME)) PUNT("Didn't find " + TEST_SETPROP_2_NAME);
			if (!scanner.seek(TEST_SETPROP_2_VALUE)) PUNT("Didn't find " + TEST_SETPROP_2_VALUE);		
			
			if (scanner.seek("entry=kb")) PUNT("I can see kernel properties.");	
	
			PASS(TEST_CHECKPROP_NORMAL);			
		} catch (Exception e) {
			FAIL(TEST_CHECKPROP_NORMAL,"process list failed:" + e.getMessage());
		}	
		
		// Process list - log and cli
		try {
			response = service.tender(CLIServiceConstants.COMMAND_SHOWPROPS + " =" + CLIServiceConstants.COMMAND_SHOWPROPS_LOG_VALUE);
			scanner.start(response);
			if (!CLIServiceTools.isResponseOkAndComplete(response)) PUNT("Response not OK.");
			if (scanner.seek(TEST_SETPROP_1_NAME)) PUNT("Suppresses CLI output but got data anyway.");

			PASS(TEST_CHECKPROP_SUPPRESSED);			
		} catch (Exception e) {
			FAIL(TEST_CHECKPROP_SUPPRESSED,"process list failed:" + e.getMessage());
		}	
		

	}
}
