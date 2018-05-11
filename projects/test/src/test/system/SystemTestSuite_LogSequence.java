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
package test.system;

/**
 * This is not used anymore!!!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 2 AUG 04
 * EPG - Retired - 1 JUN 05
 * </pre> 
 */
public interface SystemTestSuite_LogSequence {
	
	// system.cli.RUN.TEST_CLIService_RUN
	public final static String	TEST_RUN_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID = "kernel.process.5.test.system.cli.THING_TEST_echoRun";
	public final static String	TEST_Things_Set1_LOGINTERCEPTOR_INVOKE_EXPECTED_PROCESS_ID = "kernel.process.6.test.system.things.THING_TEST_set1Root";
	
}
