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
package things.data.processing.rfc822;

/**
 * Static tools for address processing.  It's a brute, so don't use it.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 15 FEB 05
 * </pre> 
 */
public class AddressTools {

	/**
	 * It will trim whitespace and encapsulating quotes (if present and matched front and back).
	 * @param address the string.
	 * @return the trimmed string.  Null in will result in null out.
	 */
	static public String trimFriendly(String address) {
		if (address == null) return null;
		String result = address.trim();
		
		// Find bounds
		int front = result.indexOf('"');
		int back = result.lastIndexOf('"');
		
		// Trivial check
		if (front >= 0) {
			
			// Empty?
			if (back == front+1) result = "";
			
			// Balanced?
			if (back > front+1) result = result.substring(front+1, back);
			
		}
		return result;
	}
	
}


