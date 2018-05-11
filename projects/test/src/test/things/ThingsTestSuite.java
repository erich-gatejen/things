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
import things.common.ThingsException;
import things.data.ThingsPropertyView;
import things.testing.unit.ResultExpresserText;
import things.testing.unit.TestGroup;

/**
 * UNIT testing tool root for the whole Things Unit test tree.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 AUG 04
 * </pre> 
 */
public class ThingsTestSuite extends TestGroup {

	// STATIC FIELDS

	// FIELDS USABLE BY EVERYTHING
    public final static String REQUIRED_PROP_ROOT_DIR = "unit.root";

	// PRIVATE FIELDS
    private StringPoster poster;
	
	/**
	 * Run the whole Things unit test suite.
	 * 
	 * @param runnerNaming
	 *            is the name giving to the test run by the running agent
	 * @param posterIn
	 *            is a StringPoster for reporting results as test strings.
	 * @param view
	 *            is a properties view that will be available to the test
	 *            implementation
	 * @param holdExceptions
	 *            set as true if you want this to log exceptions, false
	 *            if you want them to throw the exceptions
	 * @see things.data.ThingsPropertyView
	 * @throws things.common.ThingsException
	 */
	public void go(String runnerNaming, StringPoster posterIn,
			ThingsPropertyView view, boolean holdExceptions) throws ThingsException {

		// locals
		runnerName = runnerNaming;
		poster = posterIn;
		properties = view;

		// chain prepare
		try {

		    // verify it has the required properties
		    String root = view.getProperty(REQUIRED_PROP_ROOT_DIR);
		    if (root == null) throw new ThingsException("Required property '" + REQUIRED_PROP_ROOT_DIR +  "' not set.", ThingsException.PANIC_REQUIRED_PROPERTY_NOT_SET);
		    
		    // Build a ResultExpresser
		    ResultExpresserText myExpresser = new ResultExpresserText();
		    myExpresser.init(poster);
		    
		    // Prepare me
			prepare(runnerNaming,"things.",myExpresser,poster,properties,1);
			execute();
						
		} catch (ThingsException ee) {
			if (holdExceptions) {
				posterIn.post("Quitting ThingsTestSuite to exception.  msg=" + ee.getMessage());
			} else {
				throw ee;
			}
		} catch (Throwable tee) {
			if (holdExceptions) {
				posterIn.post("Quitting ThingsTestSuite to exception.  msg=" + tee.getMessage());
			} else {
				throw new ThingsException("Bad thing happened in ThingsTestSuite", ThingsException. SYSTEM_FAULT_TEST_SUITE,tee);
			}			
		}
	}
	
	/**
	 * Run the test. Overload this with the group implementation.
	 * This will call 
	 */
	public void group_execute() throws Throwable {
		
		DECLAREGROUP("things.common.","test.things.common.GROUP_common");
		DECLAREGROUP("things.data.","test.things.data.GROUP_data");
		DECLAREGROUP("things.universe.","test.things.universe.GROUP_universe");
		DECLAREGROUP("things.thinger.","test.things.thinger.GROUP_thinger");		
		DECLAREGROUP("things.thing.","test.things.thing.GROUP_thing");
		
		RUNGROUP("things.common.");
		RUNGROUP("things.universe.");
		RUNGROUP("things.data.");
		RUNGROUP("things.thinger.");
		RUNGROUP("things.thing.");
	}
	
	/**
	 * Prepare for the group run.
	 */
	public void group_prepare() throws Throwable {
	    // Nuthin'
	}
}