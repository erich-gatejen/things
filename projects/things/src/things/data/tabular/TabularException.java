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

package things.data.tabular;

/**
 * A Tabular exception. 
 * <p>
 * <b>NOTE: This package was never completed and isn't used anywhere.</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 04
 * </pre> 
 */
public class TabularException extends Exception {
	private static final long serialVersionUID = 1L;
	
	/**
	 *  Default Constructor.
	 */
	public TabularException() {
		super();
	}
	public TabularException(String message) {
		super(message);
	}
	public TabularException(String message, Throwable cause) {
		super(message,cause);
	}
	public TabularException(Throwable cause) {
		super(cause);
	}
}

