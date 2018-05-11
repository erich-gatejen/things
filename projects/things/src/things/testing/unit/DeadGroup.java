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
package things.testing.unit;


/**
 * This is a dead test implementation.  It's used to represent tests that would not load.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 19 JUN 05
 * </pre> 
 */
public class DeadGroup extends TestGroup {

	private String specialClass = null;
	
	// TESTS MUST IMPLEMENT THE FOLLOWING TWO METHOD.
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void  group_prepare() throws Throwable {
		// nuthin
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void group_execute() throws Throwable {
		throw new TestAbortException("Aborted.");
	}

	/**
	 * This is a special constructor that will allow you to set the name of the 
	 * DeadTest.
	 * @param name The name to use.
	 * @param className the class to run
	 */
	DeadGroup(String name, String className) {
		super();
		groupName = name;
		specialClass = className;
		
	}
	
	/**
	 * Get the class name for the declared test.
	 * @return the class name
	 */
	public String getClassName() {
		return specialClass;
	}	
}