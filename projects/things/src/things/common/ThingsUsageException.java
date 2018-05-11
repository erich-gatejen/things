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
package things.common;

/**
 * Usage exception.  Intended for use with {@link things.common.commands commands}.<br>
 * @see things.common.ThingsCodes
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 MAY 05
 * </pre> 
 */
public class ThingsUsageException extends ThingsException implements ThingsCodes  {

	final static long serialVersionUID = 1;
	
	/**
	 *  Numeric values for the exception.
	 */
	public static final int THINGS_USAGE_EXCEPTION_GENERIC = USAGE_ERROR;
	
	/**
	 *  Default Constructor.
	 */
	public ThingsUsageException() {
		super("Messageless ThingsUsageException", THINGS_USAGE_EXCEPTION_GENERIC);
	}

	/**
	 *  Default Constructor with Cause
	 * @param theCause for exception chaining
	 */
	public ThingsUsageException(Throwable theCause) {
		super("Messageless ThingsUsageException", THINGS_USAGE_EXCEPTION_GENERIC, theCause);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public ThingsUsageException(String message) {
		super(message, THINGS_USAGE_EXCEPTION_GENERIC);
	}

	/**
	 *  Message constructor with Cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public ThingsUsageException(String message, Throwable theCause) {
		super(message, THINGS_USAGE_EXCEPTION_GENERIC, theCause);
	}

	/**
	 *  Numeric constructor
	 * @param n numeric error
	 */
	public ThingsUsageException(int n) {
		super("ThingsUsageException numeric=" + n, n);
	}

	/**
	 * Numeric constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public ThingsUsageException(int n, Throwable theCause) {
		super("Numbered ThingsException numeric=" + n, n, theCause);
	}

	/**
	 * Message and numberic constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public ThingsUsageException(String message, int n) {
		super(message, n);
	}

	/**
	 * Message and numberic constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public ThingsUsageException(String message, int n, Throwable theCause) {
		super(message, n, theCause);
	}

}
