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
package test.things.common.impl;

import things.data.impl.NamedTree;
import things.testing.unit.Test;

/**
 * TEST a  named tree implementation
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 24 JUL 04
 * </pre> 
 */
public class TEST_NamedTree extends Test {
	
	private NamedTree   theTree;
	private final static String	TEST_CONSTRUCT = "Construct";
	private final static String	TEST_SETS = "Sets";
	private final static String	TEST_GETS = "Gets";
	private final static String	TEST_REMOVES = "Removes";
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.impl.NamedTree");
	    DECLARE(TEST_CONSTRUCT);
	    DECLARE(TEST_SETS);
	    DECLARE(TEST_GETS);
	    DECLARE(TEST_REMOVES);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		// Construct
		try {
			theTree = new NamedTree(); 
			PASS(TEST_CONSTRUCT);
		} catch (Exception e) {
			EXCEPTION(TEST_CONSTRUCT,e);
		}
		
		// sets
		int level = 1;
		try {
			theTree.set("level1", "level1");
			level = 2;
			theTree.set("level2.level2", "level2.level2");
			theTree.set("level2.level2.level2a.level2b", "level2.level2.level2a.level2b");		
			level = 3;
			theTree.set("level3.level3aaa", "level3.level3aaa");
			level = 4;
			theTree.set("level4.level4aaa.level4.level444", "level4.level4aaa.level4.level444");			
			theTree.set("level4.level4aaa.aaa", "level4.level4aaa.aaa");		
			theTree.set("level4.level4bbb.b","level4.level4bbb.b");		
			
		} catch (Exception e) {
			ABORT(TEST_SETS,"sets failed at level" + level + " to exception:" + e.getMessage());
		}
		PASS(TEST_SETS);
		
		// gets
		level = 1;
		String item;
		//boolean pass = true;
		try {
			item = (String)theTree.get("level1");
			if (!item.equals("level1")) throw new Exception("not gets 1");
			item = (String)theTree.get("level2.level2");
			if (!item.equals("level2.level2")) throw new Exception("not gets 2");
			item = (String)theTree.get("level2.level2.level2a.level2b");	
			if (!item.equals("level2.level2.level2a.level2b")) throw new Exception("not gets 2b");
			item = (String)theTree.get("level3.level3aaa");
			if (!item.equals("level3.level3aaa")) throw new Exception("not gets 3");		
			item = (String)theTree.get("level4.level4aaa.level4.level444");
			if (!item.equals("level4.level4aaa.level4.level444")) throw new Exception("not gets 4");			
			item = (String)theTree.get("level4.level4bbb.b");		
			if (!item.equals("level4.level4bbb.b")) throw new Exception("not gets 4b");					
			item = (String)theTree.get("level4.level4bbb.b");		
			if (!item.equals("level4.level4bbb.b")) throw new Exception("not gets 4b");
		} catch (Exception e) {
			ABORT(TEST_GETS,"Failed at " + e.getMessage());
		}
		PASS(TEST_GETS);
	
		// removes
		level = 1;
		try {
			theTree.remove("level1");
			level = 2;
			theTree.remove("level2.level2");
			// theTree.set("level2.level2.level2a.level2b", two);	CHECK THIs	
			level = 3;
			theTree.remove("level3.level3aaa");
			level = 4;
			theTree.remove("level4.level4aaa.level4.level444");			
			theTree.remove("level4.level4aaa.aaa");			
			
			// Check this
			level = 2;
			item = (String)theTree.get("level2.level2.level2a.level2b");	
			if (item != null) {
			    FAIL(TEST_REMOVES,"remove check 2b failed 'tree intact'");			
			} else {
			
				// Check this
				level = 3;
				item = (String)theTree.get("level3.level3aaa");	
				if (item == null) PASS(TEST_REMOVES,"remove check 3 (not there)");
				else FAIL(TEST_REMOVES,"remove check 3 (not there but it is)");	
			}
			
		} catch (Exception e) {
			ABORT(TEST_REMOVES,"removes failed at level" + level + " to exception:" + e.getMessage());
		}
		
	}
}
