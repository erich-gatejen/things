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
package test.things.thing.modules;

import test.things.STUB_SystemSuperInterface;
import test.things.universe.server.CommonUniverseTestInfrastructure;
import things.data.NVImmutable;
import things.data.ThingsPropertyView;
import things.testing.unit.Test;
import things.thing.modules.PropertyUniverseActor;

/**
 * TEST the TEST_PropertyUniverseActor module.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 27 JUN 07
 * </pre>
 */
public class TEST_PropertyUniverseActor extends Test {
	
	public final static String SETUP = "Setup";
	public final static String BASIC_SAVE = "Basic save";
	public final static String BASIC_LOAD = "Basic load";
	
	// Configuration
	public final static String UNIVERSE_OBJECT = "tpua.properties.save";
	
	// Test values
	public final static String PROPERTY_PLY = "test.tpua";
	
	public final static String PROPERTY_1_NAME = "test.tpua.1";
	public final static String PROPERTY_1_VALUE = "1234567890987654321";
	
	public final static String PROPERTY_2_NAME = "test.tpua.2";
	public final static String PROPERTY_2_VALUE = "asdlfjalsdjfnv2j4knmavdj0nw3vevv";
	
	public final static String PROPERTY_2_MONKEY_NAME = "test.tpua.2.monkey";
	public final static String PROPERTY_2_MONKEY_VALUE = "asdfgh hjkllkjh gfdsa";
	
	public final static String PROPERTY_3multi_NAME = "test.tpua.3multi";
	public final static String PROPERTY_3multi_VALUE_1 = "11111111111111111111111111111111111111111";
	public final static String PROPERTY_3multi_VALUE_2 = "22222222222222222222222222222222222222222";
	public final static String PROPERTY_3multi_VALUE_3 = "3 3 3 3 3 3 3 3 3  3 3 3 3 3 3 3 3 3 3  3";

	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.thing.modules.TEST_PropertyUniverseActor");
		DECLARE(SETUP);
	    DECLARE(BASIC_SAVE);		
	    DECLARE(BASIC_LOAD);	
	}

	public void test_execute() throws Throwable {     
		//Universe		 primaryUni;
		PropertyUniverseActor  actor = null;
		CommonUniverseTestInfrastructure infr;
		ThingsPropertyView testView = null;
		STUB_SystemSuperInterface si;
		
		// == SETUP =========================================================================================================================
		try {
			
			// Universe
			infr = new CommonUniverseTestInfrastructure();
			infr.init(properties);
			//primaryUni = infr.getA();
			infr.getA();  // Just get it.  ^^ dont bother to keep it.
			
			// Get the stub.
			si = STUB_SystemSuperInterface.getStub();
			
			// the actor.  use the global si.
			actor = new PropertyUniverseActor();
			actor.init(si);
			
			// Set the parameters need for this test.  Make sure we use the ones from the stub.
			testView = si.getGlobalProperties();
			
			testView.setProperty(PROPERTY_1_NAME, PROPERTY_1_VALUE);
			testView.setProperty(PROPERTY_2_NAME, PROPERTY_2_VALUE);
			testView.setProperty(PROPERTY_2_MONKEY_NAME, PROPERTY_2_MONKEY_VALUE);

			testView.setPropertyMultivalue(PROPERTY_3multi_NAME, PROPERTY_3multi_VALUE_1, PROPERTY_3multi_VALUE_2, PROPERTY_3multi_VALUE_3);		
			
			PASS(SETUP);	
		} catch (Throwable e) {
		    ABORT(SETUP,e.getMessage());
		}
	    
		// == BASIC SAVE =========================================================================================================================
		try {
			actor.save(CommonUniverseTestInfrastructure.UNIVERSE_A, PROPERTY_PLY, UNIVERSE_OBJECT);			
				
			PASS(BASIC_SAVE);
//		} catch (TestLocalException t) {
//		    FAIL(BASIC_SAVE,t.getMessage());
		} catch (Throwable e) {
		    ABORT(BASIC_SAVE,e.getMessage());
		}
		
		// == BASIC LOAD =========================================================================================================================
		try {
			
			// Get rid of the tree. and load the new one.
			testView.prune(PROPERTY_PLY);
			actor.load(CommonUniverseTestInfrastructure.UNIVERSE_A, UNIVERSE_OBJECT);
			
			// Validate
			String current = testView.getProperty(PROPERTY_1_NAME);
			if (current == null) PUNT("Missing PROPERTY_1_NAME : " + PROPERTY_1_NAME);
			if (!current.equals(PROPERTY_1_VALUE)) PUNT("Bad value for PROPERTY_1_VALUE.  actual=" + current);
				
			current = testView.getProperty(PROPERTY_2_NAME);
			if (current == null) PUNT("Missing PROPERTY_2_NAME : " + PROPERTY_2_NAME);
			if (!current.equals(PROPERTY_2_VALUE)) PUNT("Bad value for PROPERTY_2_VALUE.  actual=" + current);
			
			current = testView.getProperty(PROPERTY_2_MONKEY_NAME);
			if (current == null) PUNT("Missing PROPERTY_2_MONKEY_NAME : " + PROPERTY_2_MONKEY_NAME);
			if (!current.equals(PROPERTY_2_MONKEY_VALUE)) PUNT("Bad value for PROPERTY_2_MONKEY_NAME.  actual=" + current);
			
			NVImmutable currentNV = testView.getPropertyNV(PROPERTY_3multi_NAME);
			if (currentNV == null) PUNT("Missing PROPERTY_3multi_NAME : " + PROPERTY_3multi_NAME);
			String[] currentMulti = currentNV.getValues();
			if (currentMulti==null) PUNT("No values for PROPERTY_3multi_NAME : " + PROPERTY_3multi_NAME);
			if (currentMulti.length < 3)  PUNT("Missing values for PROPERTY_3multi_NAME : expected=3 actual=" + currentMulti.length);
			if (!currentMulti[0].equals(PROPERTY_3multi_VALUE_1)) PUNT("Bad value for PROPERTY_3multi_VALUE_1.  actual=" + current);
			if (!currentMulti[1].equals(PROPERTY_3multi_VALUE_2)) PUNT("Bad value for PROPERTY_3multi_VALUE_2.  actual=" + current);
			if (!currentMulti[2].equals(PROPERTY_3multi_VALUE_3)) PUNT("Bad value for PROPERTY_3multi_VALUE_3.  actual=" + current);
				
			PASS(BASIC_LOAD);
//		} catch (TestLocalException t) {
//		    FAIL(BASIC_SAVE,t.getMessage());
		} catch (Throwable e) {
		    ABORT(BASIC_LOAD,e.getMessage());
		}
		
		
	}  // end test_execute()

}
