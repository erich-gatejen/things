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

import things.common.tools.Plato;
import things.testing.unit.Test;
import things.testing.unit.TestLocalException;

/**
 * TEST the Plato truth tool.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 NOV 06
 * </pre> 
 */
public class TEST_Plato extends Test {
	
	public final static String BASIC = "Basic";
	public final static String ADVANCED = "Advanced";
	
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.tools.Plato");
	    DECLARE(BASIC);		
	    DECLARE(ADVANCED);	
	}

	public void test_execute() throws Throwable {     
	    
		// == BASIC =========================================================================================================================
		try {
			// TRUE THINGS
			if (!Plato.decideTruth("TRUE")) PUNT("'TRUE' should be true.  (Basic-True-1)");
			if (!Plato.decideTruth("T")) PUNT("'T' should be true.  (Basic-True-2)");
			if (!Plato.decideTruth("true")) PUNT("'true' should be true.  (Basic-True-3)");
			if (!Plato.decideTruth("t")) PUNT("'t' should be true.  (Basic-True-4)");
			if (!Plato.decideTruth("tRuE")) PUNT("'tRuE' should be true.  (Basic-True-5)");
			
			// TRUE NUMBERS
			if (!Plato.decideTruth("1")) PUNT("'1' should be true.  (Basic-TrueNumbers-1)");		
			if (!Plato.decideTruth("01")) PUNT("'01' should be true.  (Basic-TrueNumbers-2)");	
			if (!Plato.decideTruth("101")) PUNT("'101' should be true.  (Basic-TrueNumbers-3)");	
			if (!Plato.decideTruth("1234567890")) PUNT("'1234567890' should be true.  (Basic-TrueNumbers-4)");	
			if (!Plato.decideTruth("0987654321")) PUNT("'0987654321' should be true.  (Basic-TrueNumbers-5)");	
			
			// FALSE THINGS
			if (Plato.decideTruth("FALSE")) PUNT("'FALSE' should be false.  (Basic-False-1)");
			if (Plato.decideTruth("F")) PUNT("'F' should be false.  (Basic-False-2)");
			if (Plato.decideTruth("ftrue")) PUNT("'ftrue' should be false.  (Basic-False-3)");
			if (Plato.decideTruth("jwene.kj")) PUNT("'jwene.kj' should be false.  (Basic-False-4)");
			if (Plato.decideTruth("dftruemvnd")) PUNT("'dftruemvnd' should be false.  (Basic-False-5)");	
			
			// FALSE NUMBERS
			if (Plato.decideTruth("-1")) PUNT("'-1' should be false.  (Basic-FalseNumbers-1)");		
			if (Plato.decideTruth("0")) PUNT("'0' should be false.  (Basic-FalseNumbers-2)");	
			if (Plato.decideTruth("00000000")) PUNT("'00000000' should be false.  (Basic-FalseNumbers-3)");	
			if (Plato.decideTruth("1111.11212")) PUNT("'1111.11212' should be false.  (Basic-FalseNumbers-4)");	
			if (Plato.decideTruth("0987654321s12323")) PUNT("'0987654321s12323' should be false.  (Basic-FalseNumbers-5)");	
			
			// NULL IS ALWAYS FALSE
			if (Plato.decideTruth(null)) PUNT("Null was not false.  (Basic-Null-1)");	
			
			PASS(BASIC);
		} catch (TestLocalException t) {
		    FAIL(BASIC,t.getMessage());
		} catch (Throwable e) {
		    FAIL(BASIC,e.getMessage());
		}
		
		// == ADVANCED =========================================================================================================================
		try {
			// TRUE THINGS
			if (!Plato.decideTruth(" TRUE ")) PUNT("'TRUE' with whitespace should be true.  (Advanced-True-1)");
			if (!Plato.decideTruth(" T ")) PUNT("'T' with whitespace should be true.  (Advanced-True-2)");
			if (!Plato.decideTruth(" true	 ")) PUNT("'true' with whitespace should be true.  (Advanced-True-3)");
			if (!Plato.decideTruth("		t ")) PUNT("'t' with whitespace should be true.  (Advanced-True-4)");
			if (!Plato.decideTruth("  	  tRuE   		 ")) PUNT("'tRuE' with whitespace should be true.  (Advanced-True-5)");
			
			// TRUE NUMBERS
			if (!Plato.decideTruth(" 1 ")) PUNT("'1' with whitespace should be true.  (Advanced-TrueNumbers-1)");		
			if (!Plato.decideTruth("	01	")) PUNT("'01' with whitespace should be true.  (Advanced-TrueNumbers-2)");	
			if (!Plato.decideTruth("	 	 101  	 	  	\n	 	 	 \r  			")) PUNT("'101' with whitespace should be true.  (Advanced-TrueNumbers-3)");	
			if (!Plato.decideTruth("	1234567890	")) PUNT("'1234567890' with whitespace should be true.  (Advanced-TrueNumbers-4)");	
			if (!Plato.decideTruth("\n\r	   0987654321")) PUNT("'0987654321' with whitespace should be true.  (Advanced-TrueNumbers-5)");	
			
			// FALSE THINGS
			if (Plato.decideTruth("  FALSE  ")) PUNT("'FALSE' with whitespace should be false.  (Advanced-False-1)");
			if (Plato.decideTruth("  F  ")) PUNT("'F' with whitespace should be false.  (Advanced-False-2)");
			if (Plato.decideTruth("	ftrue	")) PUNT("'ftrue' with whitespace should be false.  (Advanced-False-3)");
			if (Plato.decideTruth("\rjwene.kj	")) PUNT("'jwene.kj' with whitespace should be false.  (Advanced-False-4)");
			if (Plato.decideTruth("\ndftruemvnd   ")) PUNT("'dftruemvnd' with whitespace should be false.  (Advanced-False-5)");	
			
			// FALSE NUMBERS
			if (Plato.decideTruth("  - 1  ")) PUNT("'-1' with whitespace should be false.  (Advanced-FalseNumbers-1)");		
			if (Plato.decideTruth("	0	")) PUNT("'0' with whitespace should be false.  (Advanced-FalseNumbers-2)");	
			if (Plato.decideTruth("000	000	0	\n0")) PUNT("'00000000' with whitespace should be false.  (Advanced-FalseNumbers-3)");	
			if (Plato.decideTruth("1 11\n1.11	2	12")) PUNT("'1 11\n1.11	2	12' with whitespace should be false.  (Advanced-FalseNumbers-4)");	
			if (Plato.decideTruth("0987654	90jkvsd2!(#$%*#$*#$*@#*$321s12323")) PUNT("'yes0987654	90jkvsd2!(#$%*#$*#$*@#*$321s12323' should be false.  (Advanced-FalseNumbers-5)");	
			
			PASS(ADVANCED);
		} catch (TestLocalException t) {
		    FAIL(ADVANCED,t.getMessage());
		} catch (Throwable e) {
		    FAIL(ADVANCED,e.getMessage());
		}
		
	}  // end test_execute()

}
