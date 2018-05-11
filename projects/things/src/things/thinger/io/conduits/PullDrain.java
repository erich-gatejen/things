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
package things.thinger.io.conduits;

import things.data.Data;
import things.thinger.SystemException;

/**
 * A Conduit drain interface for a poller.  The poll and wait calls are thread safe and may be run outside any Conduit thread context.
 * Depending on the injector's registered, they may wait for this drain to continue, so please be careful
 * not to spend too much time away from the poll or wait.
 * <p>
 * You will not register your own implementation of this with the Conduit.  Instead, register a PullDrainContainer.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public interface PullDrain {
	
    /**
     * Initialize the PullDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PullDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException;
	
	/**
	 * Poll for an item.  It will return the item or null if it isn't there.
	 * @return a data or null if nothing is there.
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public Data poll() throws SystemException;
	
	/**
	 * Wait for an item.  It will block until there is an item, when it will return it.  However, it is 
	 * possible that a ThingsException could interupt the block due to a FAULT.
	 * @return a data or null if nothing is there.
	 * @see things.data.Data
	 * @throws things.thinger.SystemException
	 */
	public Data waitItem() throws SystemException;
	
}
