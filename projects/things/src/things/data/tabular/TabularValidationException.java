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
 * A tabular validation exception. 
 * <p>
 * <b>NOTE: This package was never completed and isn't used anywhere.</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 NOV 04
 * </pre> 
 */
public class TabularValidationException extends TabularException {
	private static final long serialVersionUID = 1L;
	
	// PRIVATE FIELDS
	private int theLineNumber;
	
	/*
	 * This field defines the value of no line number.  All real line numbers will be natural number: 0 through MAX INT.
	 */
	public final static int NO_LINE_NUMBER = -1;
	
	/**
	 *  Default constructor.
	 */
	public TabularValidationException() {
		super();
		theLineNumber = NO_LINE_NUMBER;
	}
	public TabularValidationException(String message) {
		super(message);
		theLineNumber = NO_LINE_NUMBER	;	
	}
	public TabularValidationException(String message, int lineNumber) {
		super(message);
		theLineNumber = lineNumber;
	}
	public TabularValidationException(String message, Throwable cause) {
		super(message,cause);
		theLineNumber = NO_LINE_NUMBER;
	}
	public TabularValidationException(Throwable cause) {
		super(cause);
		theLineNumber = NO_LINE_NUMBER;
	}


	public int getLineNumber() {
		return theLineNumber;
	}
}

