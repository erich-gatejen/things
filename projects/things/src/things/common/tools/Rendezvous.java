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
 * And object Rendezvous.  This should be between two threads only!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JUN 06
 * </pre> 
 */
public class Rendezvous<T> extends Object {

	// The data
	private T	item;
	private boolean gate = false;
	
	/**
	 * Wait for the Rendezvous.
	 * @return the item.
	 */
	public synchronized T enter() throws Throwable {
		
		// Wait for something to rendezvous.
		try {
			gate = true;
			this.notify();
			this.wait();			
		} catch (Throwable t) {
			if (t instanceof InterruptedException) throw t;
			// Assume it is empty.
			return null;
		}

		// Give it the result.
		T result = item;
		item = null;
		gate = false;
		return result;
	}
	
	/**
	 * Meet the Rendezvous.
	 * @param thing
	 */
	public synchronized void meet(T  thing) throws Throwable {
		try {
			if (gate==false) this.wait();
		} catch (Throwable t) {
			if (t instanceof InterruptedException) throw t;
		}	
		item = thing;
		this.notify();
	}
}
