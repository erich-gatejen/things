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

/**
 * And object Rendezvous dead drop.  This should be between two threads only!  The giver can dead-drop it and 
 * move on.  This could be a problem with multiple events, but I can revisit that later.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 APR 07
 * </pre> 
 */
public class DeadDropRendezvous<T> extends Object {

	// The data
	private T	item;
	
	/**
	 * Wait for the Rendezvous.
	 * @return the item.
	 */
	public synchronized T enter() {
		
		// Wait for something to rendezvous.
		try {
			if (item==null)	this.wait();		
		} catch (Throwable t) {
			// Assume it is empty.
			return null;
		}

		// Give it the result.
		T result = item;
		item = null;
		return result;
	}
	
	/**
	 * Meet the Rendezvous.  Set or replace the item.
	 * @param thing the Thing
	 */
	public synchronized void meet(T  thing) {
		item = thing;
		this.notify();
	}
}
