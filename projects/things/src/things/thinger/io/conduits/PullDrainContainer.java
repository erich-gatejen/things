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
 * A Conduit drain container interface for a poller.  This is the interface that the Conduit uses.  The container manages threading,
 * queuing, etc.  The end user might only use the PullDrain interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Adapted from autohit - 29 JUN 05
 * EPG - Switch from Nubblet to Data type - 1 SEP 05
 * </pre> 
 */
public interface PullDrainContainer extends PullDrain {
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The Data to post.
	 * @return a receipt
	 * @throws things.thinger.SystemException
	 * @see things.data.Data
	 */
	public Receipt postListener(Data		n) throws SystemException;
	
	/**
	 * Tell if a Data has drained.  If the Data was never posted, it will treat it as it was drained.
	 * @param n The Data to check.
	 * @return true if it is drained (or never was sent), otherwise false.
	 * @throws things.thinger.SystemException
	 * @see things.data.Data
	 */
	public boolean isDrained(Data		n) throws SystemException;
	
	
	/**
	 * Wait for a drain.  If there are no pending drains, it'll immediately return.
	 */
	public void waitForDrain();
	
}
