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
package things.testing.unit;

import java.util.ArrayList;
import java.util.List;

import things.data.NV;

/**
 * A test case.  This will be used to collect results for a Test.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <br>
 * <code>5</code>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial  - 30 NOV 04
 * EPG - Re-write - 18 JUN 05
 * </pre> 
 */
public class TestCase {

	// Constant fields
	public final static String NAMELESS_NAME = "Nameless...";
	
	// FIELDS USABLE BY EVERYTHING
	public String	name;
	public String 	message;
	public Result	theResult;
	
	// Optional reportables
	private long time	= ResultExpresser.NO_NUMERIC_RESULT;
	private long size	= ResultExpresser.NO_NUMERIC_RESULT;
	private String code  = ResultExpresser.NO_CODE;
	private List<NV> values = ResultExpresser.NO_LIST;
	
	/**
	 * The default constructor.  It will be assumed nameless and waiting.
	 */	
	public TestCase() {
		theResult = Result.WAITING;
		name = NAMELESS_NAME;
	}
	
	/**
	 * The named constructor.  It will be assumed waiting.
	 * @param theName the name of the case
	 */	
	public TestCase(String theName) {
		theResult = Result.WAITING;
		name = theName;
	}

	/**
	 * Get the result for this case.
	 * @return the result
	 * @see things.testing.unit.Result
	 */	
	public Result getResult() {
		return theResult;
	}
	
	/**
	 * Get the message for this case.
	 * @return the message
	 */	
	public String getMessage() {
		return message;
	}

	/**
	 * Set the message for this case.
	 * @param message the message
	 */	
	public void setMessage(String message) {
		this.message = message;
	}	
	
	/**
	 * Set a pass result.
	 * @param msg The message associated with this result.
	 */
	public void pass(String msg) {
		theResult = Result.PASS;
	    message = msg;
	}
	
	/**
	 * Set a fail result.
	 * @param msg The message associated with this result.
	 */
	public void fail(String msg) {
		theResult = Result.FAIL;
	    message = msg;
	}
	
	/**
	 * Set a exception result.
	 * @param msg The message associated with this result.
	 */
	public void exception(String msg) {
		theResult = Result.EXCEPTION;
	    message = msg;
	}
	
	/**
	 * Set an abort result.
	 * @param msg The message associated with this result.
	 */
	public void abort(String msg) {
		theResult = Result.ABORT;
	    message = msg;
	}
	
	/**
	 * Set an inconclusive result.
	 * @param msg The message associated with this result.
	 */
	public void inconclusive(String msg) {
		theResult = Result.INCONCLUSIVE;
	    message = msg;
	}
	
	/**
	 * Set a comment result.
	 * @param msg The message associated with this result.
	 */
	public void comment(String msg) {
		theResult = Result.COMMENT;
	    message = msg;
	}

	/**
	 * Get the size value.  This is an optional field.  If the field was not set,
	 * you will get a negative value.
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Set the size value.  
	 * @param size the size
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * Get the time value in miliseconds.  This is an optional field.  If the field was not set,
	 * you will get a negative value.
	 * @return the time in milliseconds
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Set the time value.  This should be in milliseconds.
	 * @param time the time
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Get the code.
	 * @return the code as a String
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Set the code.
	 * @param theCode the code
	 */
	public void setCode(String theCode) {
		this.code = theCode;
	}
	
	/**
	 * Add a name/value.  This will not catch or process any exceptions. 
	 * @param name the name
	 * @param value the value
	 */
	public void addValue(String name, String value) {
		// If the list isn't built, then build it.
		if (values == ResultExpresser.NO_LIST) {
			values = new ArrayList<NV>();
		}
		values.add(new NV(name,value));
	}

	/**
	 * Get values list.
	 * @return A list of NVs
	 * @see things.data.NV
	 */
	public List<NV> getValues() {
		return values;
	}
	
}