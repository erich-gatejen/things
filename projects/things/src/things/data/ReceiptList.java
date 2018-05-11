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
package things.data;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * This is standard receipt list.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 JUL 05
 * </pre> 
 */
public class ReceiptList extends LinkedHashSet<Receipt> {

	final static long serialVersionUID = 1;
	
	/**
	 * Get the first receipt in the list or a blank receipt ( as created by the Receipt default constructor).  If it returns a null, something very bad happened.
	 * @return the first receipt.
	 * @see things.data.Receipt
	 */
	public Receipt first() {
		Receipt result = null;
		if (this.size() > 0) {
			result = this.iterator().next();
		} else { 
			try {
				result = new Receipt();
			} catch (Throwable ee) {
				// SCREWED UP GOOD AND PROPER
			}
		}
		return result;
	}
	
	/**
	 * Get the first receipt that is OK and TERMINAL.  If none are, it'll return null.
	 * @return the first receipt or a null.
	 * @see things.data.Receipt
	 */
	public Receipt firstOk() {
		if (this.size() > 0) {
			// Not sure why it wont let me use the new FOR here.  Perhaps it is just eclipse.
			Receipt item;
			Iterator<Receipt> items = this.iterator();
			while (items.hasNext()) {
				item = items.next();
				if ((item.getType().isOk()) && (item.getType().isTerminal())) return item;
			}
		}
		return null;
	}
}