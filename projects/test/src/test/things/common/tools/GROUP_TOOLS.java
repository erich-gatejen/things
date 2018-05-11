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
package test.things.common.tools;

import things.testing.unit.TestGroup;

/**
 * GROUP for common.tools
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 13 JAN 07
 * EPG - Add CommanLineProcessor 5 JUN 07
 * EPG - Add StringScanner 28 JUN 07
 * </pre> 
 */
public class GROUP_TOOLS extends TestGroup {

	public void group_prepare() throws Throwable {
		SET_LONG_NAME("things.common.tools.");
	    DECLARETEST("things.common.tools.TEST_Plato","test.things.common.tools.TEST_Plato");
	    DECLARETEST("things.common.tools.TEST_CommandLineSingleTokenizer","test.things.common.tools.TEST_CommandLineSingleTokenizer");
	    DECLARETEST("things.common.tools.TEST_CommanLineProcessor","test.things.common.tools.TEST_CommanLineProcessor");
	    DECLARETEST("things.common.tools.TEST_StringScanner","test.things.common.tools.TEST_StringScanner");
	}

	public void group_execute() throws Throwable {
		RUN("things.common.tools.TEST_Plato");
		RUN("things.common.tools.TEST_CommandLineSingleTokenizer");
		RUN("things.common.tools.TEST_CommanLineProcessor");
		RUN("things.common.tools.TEST_StringScanner");
	}

}
