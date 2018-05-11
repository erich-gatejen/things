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
package things.common.commands;

import things.common.ThingsException;

/**
 * A command exception. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 06
 * </pre> 
 */
public class CommandException extends ThingsException {
	public static final long serialVersionUID=1;
	
	/**
	 *  Default Constructor.
	 */
	public CommandException() {
		super("Messageless CommandException");
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 *  Default Constructor with Cause
	 * @param theCause for exception chaining
	 */
	public CommandException(Throwable theCause) {
		super("Messageless CommandException", theCause);
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public CommandException(String message) {
		super(message);
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 *  Message constructor with Cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public CommandException(String message, Throwable theCause) {
		super(message, theCause);
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 *  Numeric constructor
	 * @param n numeric error
	 */
	public CommandException(int n) {
		super("ThingsException numeric=" + n);
		numeric = n;
	}

	/**
	 * Numeric constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public CommandException(int n, Throwable theCause) {
		super("Numbered ThingsException numeric=" + n, theCause);
		numeric = n;
	}

	/**
	 * Message and numberic constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public CommandException(String message, int n) {
		super(message);
		numeric = n;
	}

	/**
	 * Message and numberic constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public CommandException(String message, int n, Throwable theCause) {
		super(message, theCause);
		numeric = n;
	}
	
	/**
	 * Message and numberic constructor
	 * @param message text message for exception
	 * @param attr A list of attributes.  They should come in paris, but if there is an odd dangling attribute name, the value will be the name.  The system will attempt to ignore null entries, but it could get confused.
	 * @param n numeric error
	 */
	public CommandException(String message, int n, String... attr) {
		super(message,n,attr);
	}

	/**
	 * Message and numberic constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.   
	 */
	public CommandException(String message, int n, Throwable theCause, String... attr) {
		super(message, n, theCause, attr);
	}
	
	/**
	 * This will throw a common-formatted CommandException reporting a software problem.
	 * @param message information message
	 * @throws things.thinger.CommandException
	 */
	 public static void softwareProblem(String message) throws CommandException {
	 	throw new CommandException("SOFTWARE PROBLEM (bug):" + message, SYSTEM_FAULT_SOFTWARE_PROBLEM);
	}		 

	/**
	 * This will throw a common-formatted CommandException reporting a software problem.  This one
	 * allows exception chaining.
	 * @param message information message
	 * @param t The throwable to chain
	 * @throws things.thinger.CommandException
	 */
	 public static void softwareProblem(String message, Throwable t) throws CommandException {
	 	throw new CommandException("SOFTWARE PROBLEM (bug):" + message, SYSTEM_FAULT_SOFTWARE_PROBLEM,t);
	}	

	/**
	 * This will throw a common-formatted CommandException reporting a software problem.  This one
	 * allows exception chaining.
	 * @param message information message
	 * @param t The throwable to chain
	 * @throws things.thinger.CommandException
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.   
	 */
	 public static void softwareProblem(String message, Throwable t, String... attr) throws CommandException {
	 	throw new CommandException("SOFTWARE PROBLEM (bug):" + message, SYSTEM_FAULT_SOFTWARE_PROBLEM,t,attr);
	}	
	 
}
