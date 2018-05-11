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
package things.common.impl;

import things.common.StringPoster;
import things.common.ThingsException;

/**
 * Postable implementation that just throws it all away.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 27 JUL 04
 * </pre> 
 */
public class StringPosterBitBucket implements StringPoster {

	/**
	 * Post as a message.
	 * @param message String to post
	 */
	public void post(String message) throws ThingsException {
		// Goo-buh!
	}
	
	/**
	 * Post as a message.  Best effort.  Ignore errors.
	 * @param message String to post
	 */
	public void postit(String message) {
		// Goo-buh!
	}
	
	/**
	 * Try to flush.  Never error no matter what.
	 */
	public void flush() {
		// NOP
	}
}
