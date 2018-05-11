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
package test.system;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.common.impl.StringPosterBitBucket;
import things.common.impl.WhoAmISimple;
import things.common.tools.StoplightMonitor;
import things.data.ThingsPropertyView;
import things.testing.unit.ResultExpresserText;
import things.testing.unit.TestGroup;
import things.thinger.kernel.Clearance;
import things.thinger.kernel.PCB;

/**
 * This is the root of the entire System Test Suite.  It runs as a process (@see KernelBasic_SuiteContext) inside a live
 * kernel (@see KernelBasic_TestingStub).
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 AUG 04
 * </pre> 
 */
public class SystemTestSuite extends TestGroup {

	// FIELDS USABLE BY EVERYTHING
    public final static String REQUIRED_PROP_ROOT_DIR = "system.root";

	// PRIVATE FIELDS
    private StringPoster poster;
    private ResultExpresserText myExpresser;
	private Throwable resultException;
	
	public WhoAmI thisProcessId;
	public PCB contextPCB;
	
	/**
	 * Run the whole Things system test suite.
	 * 
	 * @param runnerNaming
	 *            is the name giving to the test run by the running agent
	 * @param posterIn
	 *            is a StringPoster for reporting results as test strings.
	 * @param view
	 *            is a properties view that will be available to the test
	 *            implementation
	 * @param serverPropertyFilePath path to the server properties
	 * @param holdExceptions
	 *            set as true if you want this to log exceptions, false
	 *            if you want them to throw the exceptions
	 * @see things.data.ThingsPropertyView
	 * @throws things.common.ThingsException
	 */
	public void go(String runnerNaming, StringPoster posterIn,
			ThingsPropertyView view, boolean holdExceptions, String serverPropertyFilePath) throws ThingsException {

		// locals
		runnerName = runnerNaming;
		poster = posterIn;
		properties = view;
		KernelBasic_TestingStub kernelStub   = new KernelBasic_TestingStub();
		
		// We use this to stop this thread until the suite is done.
		StoplightMonitor suiteMonitor = new StoplightMonitor();
		suiteMonitor.turnRed();

		// GO
		try {

//			// Set our ID.
			thisProcessId = new WhoAmISimple("SystemTestSuite", "SS");
			
		    // verify it has the required properties
		    String root = view.getProperty(REQUIRED_PROP_ROOT_DIR);
		    if (root == null) throw new ThingsException("Required property '" + REQUIRED_PROP_ROOT_DIR +  "' not set.", ThingsException.PANIC_REQUIRED_PROPERTY_NOT_SET);
		    
		    // Build a ResultExpresser
		    myExpresser = new ResultExpresserText();
		    myExpresser.init(poster);
		    
		    // Build the server
		    kernelStub.init(serverPropertyFilePath, new StringPosterBitBucket());
		    kernelStub.start();
		    
		    // Wait for the kernel to start
		    Thread.sleep(1000);

			// Call thread.
			if (kernelStub.kernel.getCurrentState().isDeadOrDying()) {
				poster.post("Kernel didn't start.  Not starting test.");
			} else {
				
				//DO IT		
				
				// Create KernelBasic_SuiteContext and attach this to is.
				KernelBasic_SuiteContext context = new KernelBasic_SuiteContext(this, suiteMonitor);
				context.fix(thisProcessId);
				context.init(kernelStub.kernel);

				// Create a PCB from it and register with the kernel stub
				contextPCB = new PCB(context, Clearance.EXTREME, view, view);
				kernelStub.kernel.registerProcess(contextPCB, Clearance.EXTREME);
				
				// Let it run.
				context.release();
				
				// Wait for KernelBasic_SuiteContext to be done.
				suiteMonitor.stoplight();
			}

		} catch (ThingsException ee) {
			if (holdExceptions) {
				posterIn.post("Quitting SystemTestSuite to exception.  msg=" + ee.getMessage());
			} else {
				throw ee;
			}
		} catch (Throwable tee) {
			if (holdExceptions) {
				posterIn.post("Quitting SystemTestSuite to exception.  msg=" + tee.getMessage());
			} else {
				throw new ThingsException("Bad thing happened in SystemTestSuite", ThingsException. SYSTEM_FAULT_TEST_SUITE,tee);
			}			
		} finally {
			
			// Make sure the kernel is done
			kernelStub.kernel.forceHalt();
						
		}
	}
	
	/**
	 * The context will call this.
	 * @see things.data.ThingsPropertyView
	 */
	public void executeTest() {
		resultException = null;
			
		// Run this.
		try {

		    // Prepare me and run me
			prepare(runnerName,"system.",myExpresser,poster,properties,1);
			execute();
						
		} catch (Throwable tee) {
			resultException = tee;
		}
	}
	
	/**
	 * Run the test.
	 */
	public void group_execute() throws Throwable {
		
		DECLAREGROUP("system.cli.", "test.system.cli.GROUP_CLIService");	
		DECLAREGROUP("system.things.", "test.system.things.GROUP_Things");	
		DECLAREGROUP("system.modules.", "test.system.modules.GROUP_Modules");	
		
		RUNGROUP("system.cli.");
		RUNGROUP("system.things.");
		RUNGROUP("system.modules.");
	}
	
	/**
	 * Prepare for the group run.
	 */
	public void group_prepare() throws Throwable {
	    // Nuthin'
	}

	/**
	 * Get the result exception.
	 * @return the exception.  Null if it isn't anything.
	 */
	public Throwable getResultException() {
		return resultException;
	}
	
}