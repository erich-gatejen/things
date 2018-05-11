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

import things.common.ThingsException;
import things.common.tools.StringScanner;
import things.testing.unit.Test;
import things.testing.unit.TestLocalException;

/**
 * TEST the StringScanner tool.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 JUN 07
 * </pre> 
 */
public class TEST_StringScanner extends Test {
	
	public final static String SETUP = "Test setup";
	public final static String SEEK = "Basic seek";
	public final static String CURSOR_WORK = "Cursor work";
	public final static String CURSOR_WORK_NEGATIVE = "Cursor work, negative cases.";
	public final static String REGX_SEEK = "RegEx seek";
	
	public final static String TestString = "The Nellie, a cruising yawl, swung to her anchor without a flutter of " +
		"the sails, and was at rest. The flood had made, the wind was nearly " +
		"calm, and being bound down the river, the only thing for it was to come " +
		"to and wait for the turn of the tide. " +
		"\n\r" +
		"The sea-reach of the Thames stretched before us like the beginning of " +
		"an interminable waterway. In the offing the sea and the sky were welded " +
		"together without a joint, and in the luminous space the tanned sails " +
		"of the barges drifting up with the tide seemed to stand still in red " +
		"clusters of canvas sharply peaked, with gleams of varnished sprits. A " +
		"haze rested on the low shores that ran out to sea in vanishing flatness. " +
		"The air was dark above Gravesend, and farther back still seemed " +
		"condensed into a mournful gloom, brooding MotionLess over the biggest, " +
		"and the greatest, town on earth.\n\r";

	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.tools.StringScanner");
	    DECLARE(SETUP);		
	    DECLARE(SEEK);	
	    DECLARE(CURSOR_WORK);	
	    DECLARE(CURSOR_WORK_NEGATIVE);	
	    DECLARE(REGX_SEEK);
	}

	public void test_execute() throws Throwable {     
		StringScanner scanner1 = null;
		
		// == SETUP =========================================================================================================================
		try {
			
			// A good scanner.
			scanner1 = new StringScanner();
			scanner1.start(TestString);
			
			// Have to setup first, yes?  We want an exception out of this.  SEEK
			StringScanner badScanner = new StringScanner();
			try {
				badScanner.seek("monkeybone");
				PUNT("seek() ran without start() first.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.SYSTEM_FAULT_SOFTWARE_PROBLEM) PUNT("seek() without start() caused spurious exception.  message=" + te.getMessage() );
			}
			// SEEKINSENSITIVE
			try {
				badScanner.seekinsensitive("monkeybone");
				PUNT("seekinsensitive() ran without start() first.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.SYSTEM_FAULT_SOFTWARE_PROBLEM) PUNT("seekinsensitive() without start() caused spurious exception.  message=" + te.getMessage() );
			}
			// FIND
			try {
				badScanner.find("monkeybone");
				PUNT("find() ran without start() first.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.SYSTEM_FAULT_SOFTWARE_PROBLEM) PUNT("find() without start() caused spurious exception.  message=" + te.getMessage() );
			}
			// MOVE
			try {
				badScanner.move(1);
				PUNT("move() ran without start() first.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.SYSTEM_FAULT_SOFTWARE_PROBLEM) PUNT("move() without start() caused spurious exception.  message=" + te.getMessage() );
			}
	
			PASS(SETUP);	
		} catch (TestLocalException t) {
		    FAIL(SETUP,t.getMessage());
		} catch (Throwable e) {
		    ABORT(SETUP,e.getMessage());
		}
	    
		// == BASIC SEEK =========================================================================================================================
		try {
			// Use scanner 1 from above.
			
			// Dont' find
			if (scanner1.seek("monkeybonz")) PUNT("Found 'monkeybonz' when it shouldn't have.");
			
			// find in sequence
			if (!scanner1.seek("The Nellie")) PUNT("Did not find 'The Nellie'.");	
			if (!scanner1.seek("Thames")) PUNT("Did not find 'Thames'.");
			if (!scanner1.seek("Gravesend")) PUNT("Did not find 'Gravesend'.");
			
			// Do not find something we passed.
			if (scanner1.seek("Thames")) PUNT("Found 'Thames' after we should have passed it..");
			
			// find insensitive -- loooking for 'MotionLess'
			if (!scanner1.seekinsensitive("motionless")) PUNT("Did not find 'MotionLess' with seekinsensitive().");		
			
			// Find the end.
			if (!scanner1.seek("earth.\n\r")) PUNT("Did not find 'earth.<cr><lf>'.");
			
			PASS(SEEK);
		} catch (TestLocalException t) {
		    FAIL(SEEK,t.getMessage());
		} catch (Throwable e) {
		    ABORT(SEEK,e.getMessage());
		}
		
		// == CURSOR_WORK =========================================================================================================================
		try {
			// Reset the cursor and find Thames
			scanner1.set(0);		
			if (!scanner1.seek("Thames")) PUNT("Did not find 'Thames'.");
			
			// Back to a specific substring
			scanner1.move(-17);
			if (!scanner1.seek("sea-reach")) PUNT("Did not find 'sea-reach' after momving back 17.");
			
			// Back a couple more and find it again
			scanner1.move(-2);
			if (!scanner1.seek("sea-reach")) PUNT("Did not find 'sea-reach' after momving back 2 more.");
		
			// Move past the Th in Thames and dont' find it.
			// Back to a specific substring
			scanner1.move(19);
			if (scanner1.seek("Thames")) PUNT("Should have moved past the Th in Thames.");
			
			PASS(CURSOR_WORK);
		} catch (TestLocalException t) {
		    FAIL(CURSOR_WORK,t.getMessage());
		} catch (Throwable e) {
		    ABORT(CURSOR_WORK,e.getMessage());
		}
			
		// == CURSOR_WORK_NEGATIVE =========================================================================================================================
		try {
			// Set bounds
			try {
				scanner1.set(-10);
				PUNT("Allow set(-10).");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS) PUNT("set(-10) caused spurious exception.  message=" + te.getMessage() );
			}
			try {
				scanner1.set(100000);
				PUNT("Allow set(100000).");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS) PUNT("set(100000) caused spurious exception.  message=" + te.getMessage() );
			}
			
			// Move bounds
			try {
				scanner1.move(-500);
				PUNT("Allow move(-500).");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS) PUNT("move(-500) caused spurious exception.  message=" + te.getMessage() );
			}
			try {
				scanner1.move(100000);
				PUNT("Allow move(100000).");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS) PUNT("move(100000) caused spurious exception.  message=" + te.getMessage() );
			}
			
			// Move right at the end.
			scanner1.seek("earth.\n\r");
			try {
				scanner1.move(8);
				PUNT("Allow move(8) to the very end.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS) PUNT("move(8) to the very end caused spurious exception.  message=" + te.getMessage() );
			}
			
			PASS(CURSOR_WORK_NEGATIVE);
		} catch (TestLocalException t) {
		    FAIL(CURSOR_WORK_NEGATIVE,t.getMessage());
		} catch (Throwable e) {
		    ABORT(CURSOR_WORK_NEGATIVE,e.getMessage());
		}

		// == REGX_SEEK =========================================================================================================================
		try {
			// Use same scanner, but new
			scanner1 = new StringScanner();
			scanner1.start(TestString);
			scanner1.addpattern("pattern1", "The Nellie");
			scanner1.addpattern("pattern2", "Gravesend");
			
			// Find the two patterns
			if (scanner1.find("pattern1")<0) PUNT("Failed to find with pattern1 ('The Nellie').");
			if (scanner1.find("pattern2")<0) PUNT("Failed to find with pattern2 ('Gravesend').");
			
			// Make sure we can't backtrack.
			if (scanner1.find("pattern1")>=0) PUNT("Found with pattern1 again when we shouldn't have ('The Nellie').");
			
			// Make sure we stop a wild pattern name.
			try {
				scanner1.find("MonkEyBoneFUNFUNFUN");
				PUNT("find() with a not-added pattern name did not throw an exception--like it should have.");
			} catch (TestLocalException tle) {
				throw tle;
			} catch (ThingsException te) {
				if (te.numeric != ThingsException.DATA_ERROR_MATCHER_PATTERN_NOT_ADDED) PUNT("find() with a not-added pattern name yielded an unexpected exception.  message=" + te.getMessage() );
			}
			
			PASS(REGX_SEEK);
		} catch (TestLocalException t) {
		    FAIL(REGX_SEEK,t.getMessage());
		} catch (Throwable e) {
		    ABORT(REGX_SEEK,e.getMessage());
		}
		
	}  // end test_execute()

}
