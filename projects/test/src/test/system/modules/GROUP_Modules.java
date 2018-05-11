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
package test.system.modules;

import things.testing.unit.TestGroup;

/**
 * GROUP for the system Modules.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 FEB 07
 * </pre> 
 */
public class GROUP_Modules extends TestGroup {

	public void group_prepare() throws Throwable {
		SET_LONG_NAME("system.modules.");
		DECLARETEST("system.modules.set1.TEST_Modules_Set1","test.system.modules.set1.TEST_Modules_Set1");
	}
    
	public void group_execute() throws Throwable {
		THE_BUCK_STOPS_HERE();
		RUN("system.modules.set1.TEST_Modules_Set1");
	}
}
