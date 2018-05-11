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
import things.data.Receipt;
import things.thinger.SystemException;

/**
 * A conduit push drain interface.  You will register your own implementation with this.  It will run within
 * the Conduit's thread context, so make sure it is thread safe.  You never know when it will be called.
 * <p>
 * Implement the postListener method.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public interface PushDrain {

    /**
     * Initialize the PushDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PushDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException;
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The data to post.
	 * @return a receipt
	 * @throws things.thinger.SystemException
	 */
	public Receipt postListener(Data		n) throws SystemException;
	
}
