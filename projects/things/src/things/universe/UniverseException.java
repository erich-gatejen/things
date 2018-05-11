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
package things.universe;

import things.common.ThingsException;

/**
 * A Universe exception. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 16 MAY 04
 * </pre> 
 */
public class UniverseException extends ThingsException {

	public static final long serialVersionUID=1;
	
	/**
	 *  Default Constructor.
	 */
	public UniverseException() {
		super("Messageless UniverseException");
		numeric = UNIVERSE_ERROR_DEFAULT;
	}

	/**
	 *  Default Constructor with Cause
	 * @param theCause for exception chaining
	 */
	public UniverseException(Throwable theCause) {
		super("Messageless UniverseException", theCause);
		numeric = UNIVERSE_ERROR_DEFAULT;
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public UniverseException(String message) {
		super(message);
		numeric = UNIVERSE_ERROR_DEFAULT;
	}

	/**
	 *  Message constructor with Cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public UniverseException(String message, Throwable theCause) {
		super(message, theCause);
		numeric = UNIVERSE_ERROR_DEFAULT;
	}

	/**
	 *  Numeric constructor
	 * @param n numeric error
	 */
	public UniverseException(int n) {
		super("UniverseException numeric=" + n);
		numeric = n;
	}

	/**
	 * Numeric constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public UniverseException(int n, Throwable theCause) {
		super("Numbered ThingsException numeric=" + n, theCause);
		numeric = n;
	}

	/**
	 * Message and numeric constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public UniverseException(String message, int n) {
		super(message);
		numeric = n;
	}

	/**
	 * Message and numeric constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public UniverseException(String message, int n, Throwable theCause) {
		super(message, theCause);
		numeric = n;
	}

	/**
	 * Message and numeric constructor
	 * @param message text message for exception
	 * @param attr A list of attributes.  They should come in paris, but if there is an odd dangling attribute name, the value will be the name.  The system will attempt to ignore null entries, but it could get confused.
	 * @param n numeric error
	 */
	public UniverseException(String message, int n, String... attr) {
		super(message,n,attr);
	}

	/**
	 * Message and numeric constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public UniverseException(String message, int n, Throwable theCause, String... attr) {
		super(message, n, theCause, attr);
	}

}
