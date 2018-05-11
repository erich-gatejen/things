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
package things.thinger.io.conduits.basic;

import java.util.HashSet;
import java.util.LinkedList;

import things.common.ThingsCodes;
import things.data.Data;
import things.data.Receipt;
import things.thinger.SystemException;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.PullDrainContainer;

/**
 * A basic implementation of a conduit drain container interface for a poller.  This is the interface that the Conduit uses.  
 * The container manages threading, queuing, etc.  The end user might only use the PullDrain interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public class BasicPullDrainContainer implements PullDrainContainer {
	
	//===============================================================================================
	// DATA
	private LinkedList<Data>	queue;
	private HashSet<Data> catalog;
	private ConduitID	myId = null;
	private long		recieptNumber;
	
	//===============================================================================================
	// METHODS
	
    /**
     * Initialize the PullDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PullDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException {
		if (myId != null) throw new SystemException("Conduit PullDrainContainer (id=" + myId.toString() + ") was reinitialized.  System is now unreliable.",SystemException.PANIC_SYSTEM_REINIT_CONDUIT_NOT_ALLOWED);
		catalog = new HashSet<Data>();
		queue = new LinkedList<Data>();
		myId = yourId;
    }
	
	/**
	 * Poll for an item.  It will return the item or null if it isn't there.
	 * @return a Data or null if nothing is there.
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public Data poll() throws SystemException {
		if (myId==null) SystemException.softwareProblem("BasicPullDrainContainer.poll() called before .init()");
		Data result = Conduit.NOTHING;
		synchronized(queue) {
			if (!queue.isEmpty()) {
				result = queue.removeFirst();
				catalog.remove(result);
			}
		}
		return result;
	}
	
	/**
	 * Wait for an item.  It will block until there is an item, when it will return it.  However, it is 
	 * possible that a ThingsException could intrerupt the block due to a FAULT.
	 * @return a Data or null if nothing is there.
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public Data waitItem() throws SystemException {
		if (myId==null) SystemException.softwareProblem("BasicPullDrainContainer.waitItem() called before .init()");
		Data result = Conduit.NOTHING;
		try {
			// Keep trying until we get something.  
			while (result == Conduit.NOTHING) {
				synchronized(queue) {
					if (!queue.isEmpty()) {
						result = queue.removeFirst();
						catalog.remove(result);
					} else {
						queue.wait();						
					}
					queue.notifyAll();
				}
			}
		} catch (Throwable e) {
			// Any exception has to be the thread interrupt.
			throw new SystemException("PullDrain interrupted while waiting for an item.",ThingsCodes.SYSTEM_FAULT_PROCESS_WAIT_INTERRUPTED,e);
		}
		return result;		
	}
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The data to post.
	 * @return a receipt
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public Receipt postListener(Data		n) throws SystemException {
		if (myId==null) SystemException.softwareProblem("BasicPullDrainContainer.postListener() called before .init()");
		Receipt result = null;
		try {
							
			// Do it.  If we can't do a simple enqueue, then we are in serious trouble.
			synchronized(queue) {
				recieptNumber++;
				queue.add(n);
				catalog.add(n);
				queue.notifyAll();
			}
			result = new Receipt(myId, new String("#" + recieptNumber), Receipt.Type.ACCEPTANCE);

		} catch (Throwable t) {
			throw new SystemException("Conduit PullDrainContainer (id=" + myId.toString() + ") failed to enqueue.  System is now unreliable.",SystemException.PANIC_SYSTEM_CONDUIT_UNRELIABLE,t);
		}
		return result;
	}
	
	/**
	 * Tell if a Nubblet has drained.  If the Nubblet was never posted, it will treat it as it was drained.
	 * @param n The Nubblet to check.
	 * @return true if it is drained (or never was sent), otherwise false.
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public boolean isDrained(Data		n) throws SystemException {
		if (myId==null) SystemException.softwareProblem("BasicPullDrainContainer.isDrained() called before .init()");
		if (catalog.contains(n)) return false;
		return true;
	}

	/**
	 * Wait for a drain.  If there are no pending drains, it'll immediately return.
	 */
	public void waitForDrain() {
		
		try {
			// All drained?
			synchronized(queue) {
				if (queue.size()<1) return;
				//	Wait for a drain event
				queue.wait();
			}
			
		} catch (Exception e) {
			// Probibly a thread exception.  Don't care.
		}	
	}
		
	
}
