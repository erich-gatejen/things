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
package test.system.testtools;

import test.system.KernelBasic_TestingStub;
import things.common.tools.StringScanner;
import things.testing.unit.TestLocalException;
import things.thinger.service.local.CLIServiceConstants;
import things.thinger.service.local.CLIServiceTools;

/**
 * General static test tools that standardize methodologies (for synergies or something like that).
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JUL 07
 * </pre> 
 */
public class TestTools {
	
	
	// ====================================================================================================
	// == TOOLS AND INTERNAL
	
	/**
	 * Run a THING and get the log.  Break a deal, spin the wheel.
	 * @param commandString the command string for the THING to run.
	 * @param waitForThingToComplete if true, it will wait for the thing process to complete before returning.
	 * @throws TestLocalException for any problem at all.
	 */
	public static StringScanner runThingAndGetLog(String commandString, boolean waitForThingToComplete) throws TestLocalException {
		String response = null;
		StringScanner result = null;
		
		// Run the command.  Who runs Bartertown???
		try {
			response = CLIServiceWrapper.getService().tender(CLIServiceConstants.COMMAND_RUN + " " + commandString);
		} catch (Throwable t) {
			throw new TestLocalException("Could not run command.  " + t.getMessage());
		}
		
		// Get the log.   Master Blaster runs Bartertown!
		if ( CLIServiceTools.isResponseOkAndComplete(response) ) {
	
			try {
				
				// Are we going to wait?
				if (waitForThingToComplete) {
					
					// Expects a format like: ||id=kernel.process.9.test.system.universe_things.THING_for_MODULE_TEST_echo||tag=P9
					String responseText = CLIServiceTools.getTextFromResponse(response);
					String processName = responseText.substring(responseText.indexOf("id=") + 3, responseText.indexOf("||tag="));
					if (processName==null) throw new Exception("processName in runThingAndGetLog() got null somehow.");
					if (processName.indexOf("id=")==0) processName = processName.substring(3);
					KernelBasic_TestingStub.getStub().kernel.waitProcessDone(processName);
				}
					
				// Get the goods
				String logData = KernelBasic_WriterLogger_BroadInterceptionFactory.getInterceptor().get();
				StringScanner logScanner = new StringScanner();
				logScanner.start(logData);
				result = logScanner;
				
			} catch (Throwable t) {
				throw new TestLocalException("Failed to get the log for " + commandString + ".  message=" + t.getMessage());	
			}
			
		} else {
			throw new TestLocalException("Could not start (or finish) " + commandString + ".  response=" + response);			
		}
		
		//Done
		return result;
	}
	
}
