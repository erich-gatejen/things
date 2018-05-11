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
package test.things.thinger.io.conduits.basic;

import things.common.impl.WhoAmISimple;
import things.data.Data;
import things.data.Nubblet;
import things.data.Receipt;
import things.data.ReceiptList;
import things.testing.unit.Test;
import things.thinger.SystemException;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitController;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.Injector;
import things.thinger.io.conduits.PullDrainContainer;
import things.thinger.io.conduits.PushDrain;
import things.thinger.io.conduits.Conduit.InjectorType;
import things.thinger.io.conduits.basic.BasicConduitController;
import things.thinger.io.conduits.basic.BasicPullDrainContainer;

/**
 * TEST the basic conduits through a basic controller.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 DEC 05
 * </pre>
 */
public class TEST_conduits_basic extends Test implements PushDrain {
	
	// Test names
	private final static String	TEST_CONSTRUCT = "Construct";
	private final static String	TEST_REGISTER = "Register";
	private final static String	TEST_INJECTORS = "Injectors";
	private final static String	TEST_FIRSTDRAIN = "First Drain";
	private final static String	TEST_PULLDRAIN = "Pull Drain";	
	private final static String	TEST_BROADCAST = "Broadcast";	
	
	// Test data
	private final static String CONDUIT_1 = "test.conduit.1";
	private final static int TEST_NUMERIC_1 = 918231;
	private final static int TEST_NUMERIC_2 = 238171;
	
	Nubblet		testNubblet1 = new Nubblet(Data.Type.GENERIC, Data.Priority.IMMEDIATE, TEST_NUMERIC_1, "Nubblet 1");
	Nubblet		testNubblet2 = new Nubblet(Data.Type.GENERIC, Data.Priority.IMMEDIATE, TEST_NUMERIC_2, "Nubblet 2");	
	boolean		testNubblet1RX = false;
	boolean		testNubblet2RX = false;	
	Receipt		testReceipt1;
	Receipt		testReceipt2;
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.thinger.io.conduits.basic.BasicConduitController");
	    DECLARE(TEST_CONSTRUCT);
	    DECLARE(TEST_REGISTER);	    
	    DECLARE(TEST_INJECTORS);	   
	    DECLARE(TEST_FIRSTDRAIN);	 
	    DECLARE(TEST_PULLDRAIN);	
	    DECLARE(TEST_BROADCAST);
	    
	    // Test data
	    testReceipt1 = new Receipt(TEST_FIRSTDRAIN, Receipt.Type.DELIVERY);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
	
		Conduit conduit1 = null;
		ConduitController	controller1 = null;
		PullDrainContainer	pulldrain1 = null;
		Injector			injectorFirstDrain1 = null;
		Injector			injectorDrainAll1 = null;
		Injector			injectorBroadcast1 = null;
		//Injector			injectorBroadcast2Unnamed = null;		
		WhoAmISimple me = new WhoAmISimple("TEST_conduits_basic");
			
		// Construct
		try {
			// Controller
			controller1 = new BasicConduitController();
			
			// Tune test 1
			conduit1 = controller1.tune(new ConduitID(CONDUIT_1),me);
			
			// Pull drain
			pulldrain1 = new BasicPullDrainContainer();
			
			PASS(TEST_CONSTRUCT);
		} catch (Exception e) {
			EXCEPTION(TEST_CONSTRUCT,e);
		}
		
		// Register
		try {
			conduit1.registerPullDrain(pulldrain1);	
			conduit1.registerPushDrain(this);		// This test implements the drain
			
			PASS(TEST_REGISTER);
		} catch (Exception e) {
			ABORT(TEST_REGISTER,"Register failed to exception:" + e.getMessage());
		}	
		
		// Injectors
		try {		
			injectorFirstDrain1 = conduit1.getInjector(InjectorType.REQUIRE_FIRST_DRAIN,"injectorFirstDrain1");
			injectorDrainAll1  = conduit1.getInjector(InjectorType.REQUIRE_ALL_DRAIN,"injectorDrainAll1");
			injectorBroadcast1	= conduit1.getInjector(InjectorType.BROADCAST,"injectorBroadcast1");			
			if ((injectorFirstDrain1==null)||(injectorDrainAll1==null)||(injectorBroadcast1==null)) PUNT("An injector was set as null.");

			PASS(TEST_INJECTORS);
		} catch (Exception e) {
			ABORT(TEST_INJECTORS,"Injector establishment failed to exception:" + e.getMessage());
		}	
		
		// First drain
		try {		
			
			ReceiptList rList = injectorFirstDrain1.post(testNubblet1);
			if (testNubblet1RX==true) {
				if (rList!=null) {
					if (rList.size()>0) {
						if (rList.contains(testReceipt1)) {
							PASS(TEST_FIRSTDRAIN,"OK");
						} else {
							FAIL(TEST_FIRSTDRAIN, "Receipt list does not contain the proper receipt.");								
						}
					} else {
						FAIL(TEST_FIRSTDRAIN, "Receipt list is empty.");						
					}
				} else {
					FAIL(TEST_FIRSTDRAIN, "Null receipt list.");
				}
			} else {
				FAIL(TEST_FIRSTDRAIN, "Push drain never saw it.");
			}
		} catch (Exception e) {
			ABORT(TEST_FIRSTDRAIN,"Injector test for First Drain failed to exception:" + e.getMessage());
		}	
		
		// Pull Drain
		try {		
			Data n2 = pulldrain1.poll();
			if (n2 != null) {
				if (n2.getNumeric() == TEST_NUMERIC_1) {
					PASS(TEST_PULLDRAIN, "OK.");		
				} else {
					FAIL(TEST_PULLDRAIN, "Nubblet is not the one expected.");			
				}
			} else {
				FAIL(TEST_PULLDRAIN, "No nubblet available for drain.");					
			}
		} catch (Exception e) {
			ABORT(TEST_PULLDRAIN,"Injector test for Pull Drain failed to exception:" + e.getMessage());
		}	
		
		// Broadcast
		try {		
			ReceiptList rList = injectorBroadcast1.post(testNubblet2);
			if (rList==null) PUNT("Null receipt list.");
			if (testNubblet2RX==true) {
				PASS(TEST_BROADCAST,"OK");
			} else {
				FAIL(TEST_BROADCAST, "Broadcast drain never saw it.");
			}
		} catch (Exception e) {
			ABORT(TEST_BROADCAST,"Injector test for Broadcast failed to exception:" + e.getMessage());
		}	
	}
	
	// OTHER ================================================
    /**
     * Initialize the PushDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PushDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException {
    	
    }
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The Data to post.
	 * @return a receipt
	 * @throws things.thinger.SystemException
	 */
	public Receipt postListener(Data		n) throws SystemException {
		if (n.getNumeric()==TEST_NUMERIC_1) {
			testNubblet1RX = true;
			return testReceipt1;
		} else if (n.getNumeric()==TEST_NUMERIC_2) {
			testNubblet2RX = true;
			return testReceipt2;
		}
		
		return null;
	}
}
