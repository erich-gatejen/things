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

import things.common.impl.StringPosterConsole;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;

/**
 * A very basic invoker for the Things test suite.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 AUG 04
 * </pre> 
 */
public class CLISystemSuiteTester {

	/**
	 * Main method to build and run a {@link SystemTestSuite test.system.SystemTestSuite}.
	 * <p>
	 * you must pass two arguments:  A path to the suite properties (suite.prop) and the server properties (basic_config.prop). 
	 */
	public static void main(String[] args) {

		// handle arguments
		if (args.length < 2) {
			System.out.println("Required properties not specified.  Must give suite and server properties file paths.");
			return;
		}

		ThingsPropertyView props = null;
		try {
			props = ThingsPropertyTreeBASIC.getExpedientFromFile(args[0]);
		} catch (Throwable e) {
			System.out.println("Cannot load properties.  Did you set up the test environment?");
			e.printStackTrace();
			return;
		}
			
		try {
			SystemTestSuite me = new SystemTestSuite();
			me.go("Console", new StringPosterConsole(), props, true,  args[1]);
		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
