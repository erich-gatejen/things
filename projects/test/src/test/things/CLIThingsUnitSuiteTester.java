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
package test.things;

import things.common.StringPoster;
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
public class CLIThingsUnitSuiteTester {

	/**
	 * main interface 
	 */
	public static void main(String[] args) {

		// handle arguments
		if (args.length == 0) {
			System.out.println("No properties specified");
			return;
		}

		try {
			
			// Set up
			ThingsPropertyView props = ThingsPropertyTreeBASIC.getExpedientFromFile(args[0]);
			StringPoster poster = new StringPosterConsole();
			
			// Make sure the system stub is loaded 
			STUB_SystemSuperInterface.getStub(props, poster);
			
			// Build and run.
			ThingsTestSuite me = new ThingsTestSuite();
			me.go("Console", poster, props, true );
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
