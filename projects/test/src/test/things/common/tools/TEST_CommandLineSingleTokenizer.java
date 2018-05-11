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

import java.io.File;
import java.io.StringReader;
import java.util.Vector;

import test.things.ThingsTestSuite;
import things.common.tools.CommandLineSingleTokenizer;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.impl.FileAccessor;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTrunkIO;
import things.testing.unit.Test;

/**
 * TEST the TEST_CommandLineSingleTokenizer implementation.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 MAY 06
 * </pre> 
 */
public class TEST_CommandLineSingleTokenizer extends Test {
	
	//private NamedTree   theTree;
	public final static String SETUP = "Test setup";
	public final static String BASIC_TEST = "Basic test";
	public final static String FANCY_TEST1 = "Fancy test #1";
	public final static String NEGATIVE_DANGLING_QUOTE = "Negative case: dangling quote";
	public final static String NEGATIVE_DANGLING_PROPERTY = "Negative case: dangling property";

	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.tools.CommandLineSingleTokenizer");
	    DECLARE(SETUP);		
	    DECLARE(BASIC_TEST);
	    DECLARE(FANCY_TEST1);
	    DECLARE(NEGATIVE_DANGLING_QUOTE);
	    DECLARE(NEGATIVE_DANGLING_PROPERTY);
	}

	public void test_execute() throws Throwable {
		
		// Load properties
	    String my_prop = new String(properties.getProperty(ThingsTestSuite.REQUIRED_PROP_ROOT_DIR) + "/common/tools/cli_test1.prop");
	    ThingsPropertyTrunkIO  tio = null;
	    ThingsPropertyTree tree = null;
	    ThingsPropertyView view = null;        

		// Set up the test.
		try {
			
			// Load properties
		    tio = new ThingsPropertyTrunkIO();
		    tio.init(my_prop, new FileAccessor(new File(my_prop)));
		    tree = new ThingsPropertyTreeBASIC();
		    tree.init(tio);
		    tree.load();
		    view = tree.getRoot();

		    // Make sure one is there
		    if (! (view.getProperty("property.1").indexOf("property1")>=0) ) PUNT("Properties are wrong.");
		    
			PASS(SETUP);
		} catch (Throwable e) {
		    ABORT(SETUP,e.getMessage());
		}
	    
		// BASIC_TEST - Basic test
		try {
			// test command number one ^property.1^   done
			// 0    1       2      3   4 (property1)  5
			Vector<String> parseResult = CommandLineSingleTokenizer.tokenize(new StringReader("test1 test2"),view);
		    if ( ! (parseResult.get(0).equals("test1")) ) PUNT("Token 'test1' not right.");   
		    if ( ! (parseResult.get(1).equals("test2")) ) PUNT("Token 'test2' not right.");     
		    
			PASS(BASIC_TEST);
		} catch (Throwable e) {
		    FAIL(BASIC_TEST,e.getMessage());
		}
		
		// FANCY_TEST1 - Fancy test
		try {
			// test command number one ^property.1^   done
			// 0    1       2      3   4 (property1)  5
			Vector<String> parseResult = CommandLineSingleTokenizer.tokenize(new StringReader("  	 test \"command number \" one ^property.1^ done  "),view);
		    if ( ! (parseResult.get(0).equals("test")) ) PUNT("Token 'test' not right.");   
		    if ( ! (parseResult.get(1).equals("command number ")) ) PUNT("Token 'command number' not right.  Bad quote processing.");   
		    if ( ! (parseResult.get(2).equals("one")) ) PUNT("Token 'one' not right.  Bad quote processing.");   
		    if ( ! (parseResult.get(3).equals("property1")) ) PUNT("Token '^property.1^' (value=property1) not right.");   
		    if ( ! (parseResult.get(4).equals("done")) ) PUNT("Token 'done' not right.");   
		    
			PASS(FANCY_TEST1);
		} catch (Throwable e) {
		    FAIL(FANCY_TEST1,e.getMessage());
		}
		
		// NEGATIVE_DANGLING_QUOTE 
		try {
			// subcase 1
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("  	 test command number two \"spork spork done  "),view);				
				PUNT("Missed middle dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}
			// subcase 2
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("\"spork spork done  "),view);				
				PUNT("Missed front dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}		
			// subcase 3
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("test command number two \""),view);				
				PUNT("Missed end dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}	
			// subcase 4
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("test command \"in and out\" number two \""),view);				
				PUNT("Missed dangle with an in and out quote.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}	
			
			PASS(NEGATIVE_DANGLING_QUOTE);
		} catch (Throwable e) {
		    FAIL(NEGATIVE_DANGLING_QUOTE,e.getMessage());
		}		

		// NEGATIVE_DANGLING_PROPERTY 
		try {
			// subcase 1
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("  	 test command number three ^spork spork done  "),view);				
				PUNT("Missed middle dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}
			// subcase 2
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("^spork spork done  "),view);				
				PUNT("Missed front dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}		
			// subcase 3
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("test command number three ^"),view);				
				PUNT("Missed end dangle.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}	
			// subcase 4
			try {
				CommandLineSingleTokenizer.tokenize(new StringReader("test command ^middle^ number three ^"),view);				
				PUNT("Missed dangle with an in and out property.");
			} catch (Throwable t) {
				// OK!  This means we failed.
			}	
			
			PASS(NEGATIVE_DANGLING_PROPERTY);
		} catch (Throwable e) {
		    FAIL(NEGATIVE_DANGLING_PROPERTY,e.getMessage());
		}	
		
	}  // end test_execute()

}
