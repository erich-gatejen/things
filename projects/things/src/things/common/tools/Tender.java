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
package things.common.tools;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import things.common.ThingsException;

/**
 * A tender transaction object.  There can be only ONE accept thread, but as many tender threads.  The
 * tender threads will lock the tender in turn until they get their response.
 * <p>
 * Either side can declare the tender healthy or not.  If the responder does so, it'll cause the tender to error.  
 * Once it is unhealthy, it'll accept no more tenders.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from anther project - 2 FEB 07
 * </pre> 
 */
public class Tender<I, O> extends Object {

    // =========================================================================================
    // DATA
	
	private I				inputItem = null;
	private O				outputItem = null;
    private ReentrantLock 	primaryLock = new ReentrantLock();
    private ReentrantLock 	tenderLock = new ReentrantLock();
    private Condition 		inputReady = primaryLock.newCondition();
    private Condition 		outputReady = primaryLock.newCondition();
    private boolean			healthy = true;
	
    // =========================================================================================
    // METHODS
    
	/**
	 * Tender a transaction.
	 * @param input The input object.
	 * @return the output object.
	 */
	public O tender(I input) throws ThingsException {
		tenderLock.lock();
		primaryLock.lock();
		try {
			
			// Healthy?
			if (healthy==false) throw new ThingsException("Tender is unhealthy.");
		
			// Set the input
			if (inputItem != null) throw new ThingsException("Already tendered.");
			inputItem = input;
			inputReady.signal();
			
			// Wait for the output
			outputReady.await();
			
			// Is it still healthy?
			if (healthy==false) throw new ThingsException("Tender is unhealthy during transaction.");
			
			return outputItem;
			
		} catch (InterruptedException ie) {
			throw new ThingsException("Interrupted.", ThingsException.KERNEL_FAULT_PROCESS_INTERRUPTED, ie);
		} finally {
			primaryLock.unlock();
			tenderLock.unlock();
		}
	}
	
	/**
	 * Accept a tendered transaction.
	 * @return The input object.
	 * @throws ThingsException
	 * @throws InterruptedException
	 */
	public I accept() throws ThingsException, InterruptedException {
		primaryLock.lock();
		try {
			if (inputItem == null) {
				inputReady.await();
			} 
			return inputItem;
		} finally {
			primaryLock.unlock();
		}
	}
	
	/**
	 * Respond to a tendered transaction.
	 * @param output The response.
	 * @throws ThingsException
	 */
	public void response(O output) throws ThingsException {
		primaryLock.lock();
		try {
			if (inputItem == null) ThingsException.softwareProblem("Nothing tendered.  Not sure why this was called.");
			outputItem = output;
			inputItem = null;
			outputReady.signal();
		} finally {
			primaryLock.unlock();
		}			
	}
	
	/**
	 * Mark the tender as unhealthy.
	 */
	public void unhealthy() {
		primaryLock.lock();
		try {
			healthy = false;
			outputItem = null;
			inputItem = null;
			outputReady.signal();
		} catch (Throwable t) {
			// Don't care.  It's unhealthy now.
		} finally {
			primaryLock.unlock();
		}			
	}

}
