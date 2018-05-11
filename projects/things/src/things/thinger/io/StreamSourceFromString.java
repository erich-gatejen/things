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
package things.thinger.io;

/**
 * Implements a character source from a string.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 15 FEB 05
 * </pre> 
 */
public class StreamSourceFromString implements StreamSource {

	// DATA
	private String  data;
	private int rover;
	private int length;
	
	/**
	 * Constructor.  Pass the string to source.
	 * @param source must give it the source
	 */
	public StreamSourceFromString(String source) {
		data = source;
		rover = 0;
		length = source.length();
	}
	
	/**
	 * Get the next character.  It'll throw an exception if there is nothing left.
	 * @return the next character
	 * @throws Exception
	 */
	public int next() throws Exception {
		if (rover >= length) throw new Exception("Source depleted.");
		int result = data.charAt(rover);
		rover++;
		return result;
	}
	
	/**
	 * Does the source have more to get?
	 * @return true if it does, otherwise false.
	 */
	public boolean hasMore() throws Exception {
		if (rover < length) return true;
		else return false;
	}
	
}
