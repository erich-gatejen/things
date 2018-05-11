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
package things.thinger;

/**
 * Catalog of all calls.  This wasn't actually ever used.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 MAY 05
 * </pre> 
 */
public interface SystemCalls {

	/**
	 * Numeric lower boundaries. <br>
	 */
	public final static int INVALID_NUMERIC = -1;

	
	/**
	 * Get a logger unique to the process.<br>
	 * It returns a Logger object.
	 * @see things.thinger.io.Logger
	 */
	public final static int GET_MY_LOGGER = 1;
	
	/**
	 * Get a table that represents the process list.<br>
	 * It returns a Table<String> object.
	 * @see things.data.tables.Table
	 */
	public final static int GET_PROCESS_LIST = 2;
	
}