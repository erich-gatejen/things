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

import things.common.WhoAmI;
import things.common.tools.StoplightMonitor;
import things.thing.RESULT;
import things.thinger.SystemException;
import things.thinger.kernel.ThingsProcess;

/**
 * The suite itself will run in this process context so it can have access to KernelBasic security features.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 NOV 06
 * </pre> 
 */
public class KernelBasic_SuiteContext extends ThingsProcess {

	// =====================================================================================================================
	// =====================================================================================================================
	// DATA
	
	SystemTestSuite  callingSuite;
	StoplightMonitor doneSignal;

	// =====================================================================================================================
	// =====================================================================================================================
	
	/**
	 * Create the context.
	 * @param callingSuite
	 * @param doneSignal This stoplight keeps the calling thread waiting.
	 */
	public KernelBasic_SuiteContext(SystemTestSuite  callingSuite, StoplightMonitor doneSignal) {
		this.callingSuite = callingSuite;
		this.doneSignal = doneSignal;
	}
	
	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * ABSTRACT METHODS

	/**
	 * This is the entry point for the actual processing.  It's ok to let interrupted exceptions leave.  It'll be the
	 * kernel or process that did it.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException, InterruptedException {
		callingSuite.executeTest();
		doneSignal.turnGreen();
		
		// Note , exceptions should never happen as SystemTestSuite will keep them.
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * @throws things.thinger.SystemException
	 */
	public void constructThingsProcess() throws SystemException {
		
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 * @throws things.thinger.SystemException
	 */
	public void destructThingsProcess() throws SystemException {
		
	}

	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "SuiteContext";
	}
	
	/**
	 * @return the latest or the last result for the thread.  It is completely up to the implementation how to implement this.
	 */
	public RESULT getResult() {
		return null;
	}
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS
	
	/**
	 * The identified resource is in the process of being revoked.  It is still possible for the resource listener to call the resource within the context
	 * of this thread and call.
	 * <p>
	 * @param resourceID the ID of the resource that is being revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * The identified resource bas been revoked.  It is gone.  Attempting to call it would be a very bad thing.  The listener should remove the resource 
	 * from it's internal lists..
	 * <p>
	 * @param resourceID the ID of the resource that has been revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * Get the ID of the listener.
	 * <p>
	 * @return The listener's ID.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getListenerId() {
		return getProcessId();
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// METHODS

	
	// =====================================================================================================================
	// =====================================================================================================================
	// PRIVATE METHODS
	


}
