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
 * A stream source interface.  I'm leaving it to the implementations to deal with construction of reuse.
 * Yes, in know this invites bad style.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial (toolkit) - 7 SEP 03
 * </pre> 
 */
public interface StreamSource {

	/**
	 * Get the next byte.  It'll throw an exception if there is nothing left.
	 * @return the next integer (always fits int a byte).
	 * @throws Exception
	 */
	public int next() throws Exception;
	
	/**
	 * Does the source have more to get?
	 * @return true if it does, otherwise false.
	 */
	public boolean hasMore() throws Exception;
	
}
