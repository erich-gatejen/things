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

import test.things.ThingsTestSuite;
import things.common.commands.CommandLine;
import things.common.commands.CommandLineProcessor;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.impl.FileAccessor;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTrunkIO;
import things.testing.unit.Test;

/**
 * TEST the CommandLineProcessor implementation.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 06
 * </pre> 
 */
public class TEST_CommanLineProcessor extends Test {
	
	//private NamedTree   theTree;
	public final static String SETUP = "Test setup";
	public final static String BASIC_TEST = "Basic test";
	public final static String FANCY_TEST1 = "Fancy test #1";
	public final static String FANCY_TEST2 = "Fancy test #2";

	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.tools.CommandLineTokenizer");
	    DECLARE(SETUP);		
	    DECLARE(BASIC_TEST);
	    DECLARE(FANCY_TEST1);
	    DECLARE(FANCY_TEST2);
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
			// E0                                              E1
			// V0   V1				   V2					   V3
			// test command-1=command1 property-1=^property.1^ token 
			CommandLine command = CommandLineProcessor.processStatic(new StringReader("test command-1=command1 -A property-1=^property.1^ -bZ token -Q"), view);
	
			// Check each element
			if ( ! (command.hasEntity(0)) ) PUNT("Entity 'test' failed because nothing in position 0.");
			if ( ! (command.getEntity(0).equals("test")) ) PUNT("Entity 'test' does not equal 'test'"); 

			
			if ( command.hasValue("command-1") ) {
				if ( ! (command.getValue("command-1").getValue().equals("command1")) ) PUNT("Value for 'command-1' does not equal 'command1'"); 
				if ( ! (command.getValue("coMManD-1").getValue().equals("command1")) ) PUNT("Name for value 'command-1' was not treated as case-insensitive"); 		
			} else {
				PUNT("Value 'command-1' does not exist.");
			}
	
			if ( command.hasValue("property-1") ) {
				if ( ! (command.getValue("property-1").getValue().equals("property1")) ) PUNT("Value for 'property-1' does not equal 'property1'"); 
			} else {
				PUNT("Value 'property-1' does not exist.");
			}
			
			// Check each element
			if ( ! (command.hasEntity(1)) ) PUNT("Entity 'token' failed because nothing in position 1.");
			if ( ! (command.getEntity(1).equals("token")) ) PUNT("Entity 'test' does not equal 'test'"); 
			
			// Check the options.
			if ( !command.isOptionSet('A') ) PUNT("Option 'A' not set."); 
			if ( !command.isOptionSet('b') ) PUNT("Option 'b' not set."); 	
			if ( !command.isOptionSet('Z') ) PUNT("Option 'Z' not set."); 	
			if ( !command.isOptionSet('Q') ) PUNT("Option 'Q' not set."); 	
			if ( command.isOptionSet('z') ) PUNT("Option 'z' (lower case) is set when it shouldn't be."); 					
			if ( command.isOptionSet('t') ) PUNT("Option 't' (lower case) is set when it shouldn't be."); 	
			if ( command.isOptionSet('1') ) PUNT("Option '1' (inside command-1) is set when it shouldn't be."); 	
			
			PASS(BASIC_TEST);
		} catch (Throwable e) {
		    FAIL(BASIC_TEST,e.getMessage());
		}
		
		// FANCY_TEST1 - Fancy test
		try {
			// <spaces>test ^property.2^=zork2 thang3= =thang4 thang5=thanger5  	<tab>bork <newline> finally<spaces>
			CommandLine command = CommandLineProcessor.processStatic(new StringReader("  test ^property.2^=zork2 thang3= =thang4 -^B- thang5=thanger5  	\tbork \n finally  "), view);

			// Check each element
			if ( ! (command.hasEntity(0)) ) PUNT("Entity 'test' failed because nothing in position 0.");
			if ( ! (command.getEntity(0).equals("test")) ) PUNT("Entity 'test' does not equal 'test'"); 
		
			if ( command.hasValue("property_2") ) {
				if ( ! (command.getValue("property_2").getValue().equals("zork2")) ) PUNT("Value for 'property_2' (^property.2^) does not equal 'zork2'"); 
			} else {
				PUNT("Value 'property_2' does not exist.");
			}
			
			if ( command.hasValue("thang3") ) {
				if ( ! (command.getValue("thang3").getValue().equals("thang3")) ) PUNT("Value for 'thang3' does not equal 'thang3'"); 
			} else {
				PUNT("Value 'thang3' does not exist.");
			}			
			
			if ( command.hasValue("thang4") ) {
				if ( ! (command.getValue("thang4").getValue().equals("thang4")) ) PUNT("Value for 'thang4' does not equal 'thang4'"); 
			} else {
				PUNT("Value 'thang4' does not exist.");
			}			
			
			if ( command.hasValue("thang5") ) {
				if ( ! (command.getValue("thang5").getValue().equals("thanger5")) ) PUNT("Value for 'thang5' does not equal 'thanger5'"); 
			} else {
				PUNT("Value 'thang5' does not exist.");
			}			
			
			if ( ! (command.hasEntity(1)) ) PUNT("Entity 'bork' failed because nothing in position 1.");
			if ( ! (command.getEntity(1).equals("bork")) ) PUNT("Entity 'bork' does not equal 'bork'"); 

			if ( ! (command.hasEntity(2)) ) PUNT("Entity 'finally' failed because nothing in position 2.");
			if ( ! (command.getEntity(2).equals("finally")) ) PUNT("Entity 'finally' does not equal 'finally'"); 
		
			// Check the options.
			if ( !command.isOptionSet('B') ) PUNT("Option 'B' not set."); 
			if ( command.isOptionSet('^') ) PUNT("Option '^' is set when it should not be (not allowed)."); 	
			if ( command.isOptionSet('-') ) PUNT("Option '-' is set when it should not be (not allowed)."); 	
			
			PASS(FANCY_TEST1);
		} catch (Throwable e) {
		    FAIL(FANCY_TEST1,e.getMessage());
		}
		
		// FANCY_TEST2 - Fancy test
		try {
			// <spaces>=test ^property.2^^property.1^=zork2 thang3=
			CommandLine command = CommandLineProcessor.processStatic(new StringReader("  =test ^property.2^^property.1^=zork2 middle thang3="), view);

			if ( command.hasValue("test") ) {
				if ( ! (command.getValue("test").getValue().equals("test")) ) PUNT("Value for 'test' does not equal 'test'"); 
			} else {
				PUNT("Value 'test' does not exist.");
			}	
			
			if ( command.hasValue("property_2property1") ) {
				if ( ! (command.getValue("test").getValue().equals("test")) ) PUNT("Value for 'property_2property1' (^property.2^^property.1^) does not equal 'zork2'"); 
			} else {
				PUNT("Value 'property_2property1' (^property.2^^property.1^) does not exist.");
			}	
			
			if ( ! (command.hasEntity(0)) ) PUNT("Entity 'middle' failed because nothing in position 0.");
			if ( ! (command.getEntity(0).equals("middle")) ) PUNT("Entity 'middle' does not equal 'middle'"); 
		
			PASS(FANCY_TEST2);
		} catch (Throwable e) {
		    FAIL(FANCY_TEST2,e.getMessage());
		}
		
	}  // end test_execute()
}
