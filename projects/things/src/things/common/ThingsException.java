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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import things.data.AttributeCodec;
import things.data.AttributeReader;
import things.data.NV;
import things.data.NVImmutable;
import things.data.impl.AttributesReaderWrapper;

/**
 * A base exception for all things. It adds functionality for problem management
 * rather than just a way to code exceptions.<br>
 * 
 * @see things.common.ThingsCodes <p>
 * @author Erich P. Gatejen
 * @version 1.0
 *          <p>
 *          <i>Version History</i>
 * 
 *          <pre>
 * EPG - Initial - 2 DEC 01
 * TNH - Change how attributes works - 1 AUG 02
 * EPG - Add various rendering options - 16 MAY 04
 * EPG - Add attribute functions and fix bugs - 11 JAN 05
 * EPG - Clean up reports so they work better with the kernel debugging - 19 MAR 06
 * EPG - Add 'worst-exception' for RendSite.  - 23 AUG 09
 * </pre>
 */
public class ThingsException extends Throwable implements ThingsCodes {

	final static long serialVersionUID = 1;

	// ==================================================================================
	// == FIELDS

	/**
	 * Exception numeric.
	 */
	public int numeric;

	/**
	 * Attributes.
	 */
	protected HashMap<String, String> attributes = null;
	
	/**
	 * The worst exception from all ThingsException causes.
	 */
	private ThingsException worstException;

	/**
	 * Numeric values for the exception.
	 */
	public static final int THINGS_EXCEPTION_GENERIC = DEFAULT_NUMERIC;

	// ==================================================================================
	// == METHODS

	/**
	 * Default Constructor.
	 */
	public ThingsException() {
		super("Messageless ThingsException");
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 * Default Constructor with Cause
	 * 
	 * @param theCause
	 *            for exception chaining
	 */
	public ThingsException(Throwable theCause) {
		super("Messageless ThingsException", theCause);
		numeric = THINGS_EXCEPTION_GENERIC;
		setWorst(theCause);
	}

	/**
	 * Message constructor.
	 * 
	 * @param message
	 *            text message for exception
	 */
	public ThingsException(String message) {
		super(message);
		numeric = THINGS_EXCEPTION_GENERIC;
	}

	/**
	 * Message constructor with Cause.
	 * 
	 * @param message
	 *            text message for exception
	 * @param theCause
	 *            for exception chaining
	 */
	public ThingsException(String message, Throwable theCause) {
		super(message, theCause);
		numeric = THINGS_EXCEPTION_GENERIC;
		setWorst(theCause);
	}

	/**
	 * Numeric constructor
	 * 
	 * @param n
	 *            numeric error
	 */
	public ThingsException(int n) {
		super("ThingsException numeric=" + n);
		numeric = n;
	}

	/**
	 * Numeric constructor with cause
	 * 
	 * @param n
	 *            numeric error
	 * @param theCause
	 *            for exception chaining
	 */
	public ThingsException(int n, Throwable theCause) {
		super("Numbered ThingsException numeric=" + n, theCause);
		numeric = n;
		setWorst(theCause);
	}

	/**
	 * Message and numeric constructor
	 * 
	 * @param message
	 *            text message for exception
	 * @param n
	 *            numeric error
	 */
	public ThingsException(String message, int n) {
		super(message);
		numeric = n;
	}

	/**
	 * Message and numeric constructor with cause
	 * 
	 * @param message
	 *            text message for exception
	 * @param n
	 *            numeric error
	 * @param theCause
	 *            for exception chaining
	 */
	public ThingsException(String message, int n, Throwable theCause) {
		super(message, theCause);
		numeric = n;
		setWorst(theCause);
	}

	/**
	 * Message and numeric constructor
	 * 
	 * @param message
	 *            text message for exception
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.
	 * @param n
	 *            numeric error
	 */
	public ThingsException(String message, int n, String... attr) {
		super(message);
		attributes = AttributeCodec.encode2Map(attributes, attr);
		numeric = n;
	}

	/**
	 * Message and numeric constructor with cause.
	 * 
	 * @param message
	 *            text message for exception
	 * @param n
	 *            numeric error
	 * @param theCause
	 *            for exception chaining
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.
	 */
	public ThingsException(String message, int n, Throwable theCause,
			String... attr) {
		super(message, theCause);
		attributes = AttributeCodec.encode2Map(attributes, attr);
		numeric = n;
		setWorst(theCause);
	}
	
	/**
	 * Get the worst ThingsException from all the causes.  If there are no causes, it'll return this.  It is possible that this is the worst exception too.
	 * Worst is defined as the least numeric.  
	 * @return the worst exception.
	 */
	public ThingsException getWorst() {
		if (worstException==null) return this;
		return worstException;
	}
		
	/**
	 * Helper for determining level - Informational
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isInformational(int code) {
		if ((code >= INFO) && (code <= INFO_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - DEBUG
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isDebug(int code) {
		if ((code >= DEBUG) && (code <= DEBUG_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - WARNING
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isWarning(int code) {
		if ((code >= WARNING) && (code <= WARNING_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - ERROR
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isError(int code) {
		if ((code >= ERROR) && (code <= ERROR_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - FAULT
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isFault(int code) {
		if ((code >= FAULT) && (code <= FAULT_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - FAULT
	 * 
	 * @param code
	 *            numeric code
	 * @return true if it is informational
	 */
	static public boolean isPanic(int code) {
		if ((code >= PANIC) && (code <= PANIC_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - Informational
	 * 
	 * @return true if it is informational
	 */
	public boolean isInformational(ThingsException e) {
		if ((numeric >= INFO) && (numeric <= INFO_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - DEBUG
	 * 
	 * @return true if it is informational
	 */
	public boolean isDebug() {
		if ((numeric >= DEBUG) && (numeric <= DEBUG_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - WARNING
	 * 
	 * @return true if it is informational
	 */
	public boolean isWarning() {
		if ((numeric >= WARNING) && (numeric <= WARNING_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - ERROR
	 * 
	 * @return true if it is informational
	 */
	public boolean isError() {
		if ((numeric >= ERROR) && (numeric <= ERROR_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - FAULT
	 * 
	 * @return true if it is informational
	 */
	public boolean isFault() {
		if ((numeric >= FAULT) && (numeric <= FAULT_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - FAULT
	 * 
	 * @return true if it is informational
	 */
	public boolean isPanic() {
		if ((numeric >= PANIC) && (numeric <= PANIC_TOP))
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - Is it worse than a WARNING?
	 * 
	 * @return true if it is informational
	 */
	public boolean isWorseThanWarning() {
		if (numeric < WARNING)
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - Is it worse than an ERROR?
	 * 
	 * @return true if it is informational
	 */
	public boolean isWorseThanError() {
		if (numeric < ERROR)
			return true;
		else
			return false;
	}

	/**
	 * Helper for determining level - Is it worse than a FAULT?
	 * 
	 * @return true if it is informational
	 */
	public boolean isWorseThanFault() {
		if (numeric < FAULT)
			return true;
		else
			return false;
	}

	/**
	 * Check to see if this Exception passes a threshold. This matches the
	 * threshold characteristics decumented in ThingsCodes. To pass the
	 * threshold, the numeric must be BELOW the threshold level, not equal or
	 * within the range.
	 * 
	 * @param threshold
	 *            The threshold level to check against.
	 * @return true if it passes the threshold, else false.
	 * @see things.common.ThingsCodes
	 */
	public boolean pass(int threshold) {
		if (numeric < threshold)
			return true;
		else
			return false;
	}

	/**
	 * Check to see if any numeric passes a threshold. This matches the
	 * threshold characteristics decumented in ThingsCodes. To pass the
	 * threshold, the numeric must be BELOW the threshold level, not equal or
	 * within the range.
	 * 
	 * @param threshold
	 *            The threshold level to check against.
	 * @param numeric
	 *            The numeric to check.
	 * @return true if it passes the threshold, else false.
	 * @see things.common.ThingsCodes
	 */
	static public boolean pass(int threshold, int numeric) {
		if (numeric < threshold)
			return true;
		else
			return false;
	}

	/**
	 * Get attributes.
	 * 
	 * @return a HashMap representing the attributes.
	 */
	public HashMap<String, String> getAttributes() {
		return attributes;
	}

	/**
	 * Get attributes as NVImmutable. Decorate them with message and numeric
	 * attributes. This is mostly useful for bridging between exception and
	 * logging.
	 * 
	 * @return an NVImmutable array representing the attributes.
	 */
	public NVImmutable[] getAttributesNVDecorated() {
		NV[] inaarray = new NV[2];
		inaarray[0] = new NV(ThingsNamespace.ATTR_PLATFORM_MESSAGE,
				getMessage());
		inaarray[1] = new NV(ThingsNamespace.ATTR_LINE_NUMERIC_CAUSE, Integer
				.toString(numeric));
		return chainAttributes(0, 0, inaarray);
	}

	/**
	 * Get attributes wrapped in a reader.
	 * 
	 * @return an attribute reader for the attributes.
	 */
	public AttributeReader getAttributesReader() {
		return new AttributesReaderWrapper(attributes);
	}

	/**
	 * Get attributes as NVs.
	 * 
	 * @param ina
	 *            initial attributes. This array of NVs will be merged with the
	 *            current attributes in the return array. If you pass null or an
	 *            empty set, it will be ignored.
	 * @return an NV array representing the attributes.
	 * @see things.data.NVImmutable
	 */
	public NVImmutable[] getAttributesNVMulti(NV... ina) {
		return chainAttributes(0, 0, ina);
	}

	/**
	 * Get attributes as NVImmutable.
	 * 
	 * @param ina
	 *            initial attributes. This single NV will be merged with the
	 *            current attributes in the return array. If you pass null or an
	 *            empty set, it will be ignored.
	 * @return an NV array representing the attributes.
	 * @see things.data.NVImmutable
	 */
	public NVImmutable[] getAttributesNV(NV ina) {
		NV[] inaarray = new NV[1];
		inaarray[0] = ina;
		return chainAttributes(0, 0, inaarray);
	}

	/**
	 * Get attributes as NVImmutable. Merge with an input NV pair (as Strings).
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param value
	 *            The value of the attribute.
	 * @return an NVImmutable array representing the attributes.
	 */
	public NVImmutable[] getAttributesNV(String name, String value) {
		NV[] inaarray = new NV[1];
		inaarray[0] = new NV(name, value);
		return chainAttributes(0, 0, inaarray);
	}

	/**
	 * Get attributes as name value pairs in alternating sequence in an array.
	 * Merge with an input NV pair (as Strings). Decorate them with message and
	 * numeric attributes. This is mostly useful for bridging between exception
	 * and logging.
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param value
	 *            The value of the attribute.
	 * @return Attributes as name value pairs in alternating sequence in an
	 *         array.
	 */
	public String[] getAttributesDecorated(String name, String value) {
		NV[] inaarray = new NV[3];
		inaarray[0] = new NV(name, value);
		inaarray[1] = new NV(ThingsNamespace.ATTR_PLATFORM_MESSAGE,
				getMessage());
		inaarray[2] = new NV(ThingsNamespace.ATTR_LINE_NUMERIC_CAUSE, Integer
				.toString(numeric));
		NVImmutable[] data = chainAttributes(0, 0, inaarray);
		String[] result = new String[(data.length * 2)];
		for (int index = 0; index < data.length; index++) {
			result[(index*2)] = data[index].getName();
			if (data[index].isMultivalue()) {
				try {
					result[(index*2) + 1] = AttributeCodec
							.encode2String(data[index].getValues());
				} catch (Throwable t) {
					// Really should NEVER happen
					result[(index*2) + 1] = "CODEC BUG";
				}
			} else
				result[(index*2) + 1] = data[index].getValue();
		}
		return result;
	}

	/**
	 * Get attributes as NVImmutable. Merge with an input NV pair (as Strings).
	 * Decorate them with message and numeric attributes. This is mostly useful
	 * for bridging between exception and logging.
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param value
	 *            The value of the attribute.
	 * @return an NVImmutable array representing the attributes.
	 */
	public NVImmutable[] getAttributesNVDecorated(String name, String value) {
		NV[] inaarray = new NV[3];
		inaarray[0] = new NV(name, value);
		inaarray[1] = new NV(ThingsNamespace.ATTR_PLATFORM_MESSAGE,
				getMessage());
		inaarray[2] = new NV(ThingsNamespace.ATTR_LINE_NUMERIC_CAUSE, Integer
				.toString(numeric));
		return chainAttributes(0, 0, inaarray);
	}

	/**
	 * The total exception depth we'll examine for attributes. This is to
	 * prevent runaway recursion.
	 */
	public final static int CHAIN_DEPTH_LIMIT = 16;

	/**
	 * Chain attributes through all causes.
	 * 
	 * @param number
	 *            the number of attributes so far.
	 * @param limit
	 *            the number of plies allowed. This will help detect recursion.
	 * @return attributes. These will NOT be deduped.
	 */
	protected NV[] chainAttributes(int number, int limit, NV[] starting) {

		NV[] result;

		// How many does it have?
		int localNumber;
		if (attributes == null) {
			localNumber = number;
		} else {
			localNumber = attributes.size() + number;
		}

		// Branch?
		Throwable theCause = this.getCause();
		if ((limit < CHAIN_DEPTH_LIMIT) && (theCause != null)
				&& (theCause instanceof ThingsException)) {

			// Yes. Recurse.
			result = ((ThingsException) theCause).chainAttributes(localNumber,
					limit + 1, starting);

		} else {

			// No. Unroll from here.
			if (starting == null) {
				result = new NV[localNumber];
			} else {

				// Add in the starting first.
				result = new NV[localNumber + (starting.length)];
				for (int rover = 0; rover < starting.length; rover++) {
					result[localNumber + rover] = starting[rover];
				}
			}
		}

		// Add mine. For each in the attributes, create a new NV. Then place it
		// at the localNumber spot and count down.
		if (attributes != null) {
			for (String item : attributes.keySet()) {
				localNumber--;
				result[localNumber] = new NV(item, attributes.get(item));
			}
		}

		return result;
	}

	/**
	 * Add an attribute. If the attribute name is null, it will not be added.
	 * 
	 * @param name
	 *            The attribute name.
	 * @param value
	 *            The attribute value.
	 */
	public void addAttribute(String name, String value) {
		if (name != null) {
			if (attributes == null)
				attributes = new HashMap<String, String>();
			attributes.put(name, value);
		}
	}

	/**
	 * Adds attribute as name value pairs.
	 * 
	 * @param pairs
	 *            pairs of names and values.
	 * @throws ThingsException
	 *             if it is a null paramter or an uneven number of items.
	 */
	public void addAttributes(String... pairs) throws ThingsException {
		if (pairs == null)
			ThingsException
					.softwareProblem("Null pairs in call to ThingsException.addAttributes");
		if ((pairs.length % 2) > 0)
			ThingsException
					.softwareProblem("Uneven pairs in call to ThingsException.addAttributes");
		for (int rover = 0; rover < pairs.length; rover = rover + 2) {
			addAttribute(pairs[rover], pairs[rover + 1]);
		}
	}

	/**
	 * Add attributes from a hashtable. Duplicates will be overwritten (which
	 * may or may not be a problem for you).
	 * 
	 * @param attributes
	 *            the hashtable. if null, it will quietly return.
	 */
	public void addAttribute(HashMap<String, String> attributes) {
		if (attributes != null) {
			for (String name : attributes.keySet()) {
				attributes.put(name, attributes.get(name));
			}
		}
	}

	/**
	 * Add an attribute as a NV. If the attribute is null or its name is null,
	 * it will not be added.
	 * 
	 * @param attribute
	 *            The NV representing the attribute.
	 * @see things.data.NV
	 */
	public void addAttribute(NV attribute) {
		if (attribute != null) {
			String name = attribute.getName();
			if (name != null) {
				if (attributes == null)
					attributes = new HashMap<String, String>();
				attributes.put(attribute.getName(), attribute.getValue());
			}
		}
	}

	/**
	 * Get attributes as a String.
	 * 
	 * @return Returns a string reprsentation. If there was an error or there
	 *         are no attributes, it will return an empty string.
	 * @see things.common.ThingsConstants
	 */
	public String getAttributestoString() throws ThingsException {
		String result = ThingsConstants.EMPTY_STRING;
		try {
			if (attributes != null) {
				result = AttributeCodec.encode2String(attributes);
			}
		} catch (Throwable t) {
			// Don't care. String will remain a EMPTY_STRING
		}
		return result;
	}

	/**
	 * Render the exception as a string without the trace.
	 * 
	 * @return a string representation of this exception.
	 */
	public String toStringSimple() {
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(this.getMessage());
			sb.append(":");
			sb.append(numeric);
			sb.append(":");
			if (attributes != null)
				sb.append(AttributeCodec.encode2String(attributes));

		} catch (Throwable t) {
			sb.append("ERROR IN RENDER EXCEPTION.  message=" + t.getMessage());
		}
		return sb.toString();
	}

	/**
	 * Render the exception as a string without the trace, but with all causes.
	 * 
	 * @return a string representation of this exception.
	 */
	public String toStringCauses() {
		return toStringCauses(this);
	}

	/**
	 * Render the exception as attributes only.
	 * @return a string representation of this exception.
	 */
	public String toStringAttributes() {
		return toStringAttributes(this);
	}

	/**
	 * Get the root cause.  This will be the terminal ThingsException.
	 * @return the terminal ThingsException, which may be this.
	 */
	public ThingsException getRootCause() {
		ThingsException result = this;
		Throwable cause = getCause();
		if (cause!=null) {
			if (cause instanceof ThingsException) result = ((ThingsException)cause).getRootCause(); 
		}
		return result;
	}
	
	
	/**
	 * Render the exception as attributes only.
	 * 
	 * @param tr
	 *            the exception chain.
	 * @return a string representation of this exception.
	 */
	public static String toStringAttributes(Throwable tr) {
		StringWriter sw = new StringWriter();
		try {

			// Elements
			Throwable current = tr;
			Throwable candidate;
			while (current != null) {

				// Things processing v. other exceptions
				if (current instanceof ThingsException) {
					if (((ThingsException) current).attributes != null) {
						sw.append("attributes:");
						sw.append(((ThingsException) current)
								.getAttributestoString());
						sw.append(ThingsConstants.CRLF);
					}
				}

				// Next and do some tardo JDK protection
				candidate = current.getCause();
				if (candidate == current)
					candidate = null; // Some will fricken point at themselves.
				current = candidate;

			}

			// Footer
			sw.flush();

		} catch (Throwable t) {
			sw
					.append("Did not complete exception report due to problem.  message="
							+ t.getMessage() + ThingsConstants.CRLF);
		}
		return sw.toString();
	}
	
	/**
	 * Render the exception as a string without the trace, but with all causes.
	 * 
	 * @param tr
	 *            the exception chain.
	 * @return a string representation of this exception.
	 */
	public static String toStringCauses(Throwable tr) {
		StringWriter sw = new StringWriter();
		try {

			// Elements
			Throwable current = tr;
			Throwable candidate;
			while (current != null) {

				// Message
				sw.append("message:" + current.getMessage()
						+ ThingsConstants.CRLF);

				// Things processing v. other exceptions
				if (current instanceof ThingsException) {
					sw.append("numeric:" + ((ThingsException) current).numeric
							+ ThingsConstants.CRLF);
					if (((ThingsException) current).attributes != null) {
						sw.append("attributes:");
						sw.append(((ThingsException) current)
								.getAttributestoString());
						sw.append(ThingsConstants.CRLF);
					}
				}

				// Next and do some tardo JDK protection
				candidate = current.getCause();
				if (candidate == current)
					candidate = null; // Some will fricken point at themselves.
				current = candidate;

			}

			// Footer
			sw.flush();

		} catch (Throwable t) {
			sw.append("Did not complete exception report due to problem.  message="
							+ t.getMessage() + ThingsConstants.CRLF);
		}
		return sw.toString();
	}

	/**
	 * Render the exception as a string. It will print a stack trace into the
	 * string too.
	 * 
	 * @return a string representation of this exception.
	 */
	public String toStringComplex() {
		return toStringComplex(this);
	}

	/**
	 * Render a throwable as a string. It will print a stack trace into the
	 * string too.
	 * 
	 * @param tr
	 *            The throwable to render.
	 * @return a string representation of this exception.
	 */
	public static String toStringComplex(Throwable tr) {
		StringWriter sw = new StringWriter();
		String working;
		try {

			// Header
			sw.append("== Exception report ====================================================================="
							+ ThingsConstants.CRLF);

			// Elements
			Throwable current = tr;
			Throwable candidate;
			while (current != null) {

				// Message
				sw.append("message:" + current.getMessage()
						+ ThingsConstants.CRLF);

				// Things processing v. other exceptions
				if (current instanceof ThingsException) {
					sw.append("numeric:" + ((ThingsException) current).numeric
							+ ThingsConstants.CRLF);
					if (((ThingsException) current).attributes != null) {
						sw.append("attributes:");

						// Pretty this: I'll regret this later, but it's hard to
						// read right now.
						// TODO : review this monkeybusiness later.
						working = ((ThingsException) current).getAttributestoString();
						working = working.replace(
										ThingsConstants.CODEC_SEPARATOR_CHARACTER_ESCAPED,
										ThingsConstants.CODEC_SEPARATOR_CHARACTER_ESCAPED
												+ ThingsConstants.CHEAP_LINESEPARATOR);

						sw.append(working);
						sw.append(ThingsConstants.CRLF);
					}
				} else {
					sw.append("class:" + current.getClass().getName() + ThingsConstants.CRLF);
				}

				// Next and do some tardo JDK protection
				candidate = current.getCause();
				if (candidate == current)
					candidate = null; // Some will fricken point at themselves.
				current = candidate;

				// Sep
				sw.append(" --------------------------------------------------"
						+ ThingsConstants.CRLF);
			}

			// Stacktrace
			PrintWriter p = new PrintWriter(sw);
			tr.printStackTrace(p);
			p.flush();

			// Footer
			sw.append("========================================================================================="+ ThingsConstants.CRLF);
			sw.flush();

		} catch (Throwable t) {
			sw.append("Did not complete exception report due to problem.  message="
							+ t.getMessage() + ThingsConstants.CRLF);
		}
		return sw.toString();
	}

	/**
	 * Panic report. Something bad happens and we won't be able to report it
	 * through the usual subsystems. this report for this instance of
	 * ThingsException.
	 */
	public void panicReport() {
		panicReport(this.getMessage());
		this.printStackTrace();
	}

	/**
	 * Panic report. Something bad happens and we won't be able to report it
	 * through the usual subsystems. this report for this instance of
	 * ThingsException.
	 * @param message the message.
	 */
	public static void panicReport(String message) {
		System.out.println("PANIC REPORT: " + message);
	}

	/**
	 * This will throw a common-formatted ThingsException reporting a software
	 * problem.
	 * 
	 * @param message
	 *            information message
	 * @throws things.common.ThingsException
	 */
	public static void softwareProblem(String message) throws ThingsException {
		throw new ThingsException("SOFTWARE PROBLEM (bug):" + message,
				SYSTEM_FAULT_SOFTWARE_PROBLEM);
	}

	/**
	 * This will throw a common-formatted ThingsException reporting a software
	 * problem. This one allows exception chaining.
	 * 
	 * @param message
	 *            information message
	 * @param t
	 *            The throwable to chain
	 * @throws things.common.ThingsException
	 */
	public static void softwareProblem(String message, Throwable t)
			throws ThingsException {
		throw new ThingsException("SOFTWARE PROBLEM (bug):" + message,
				SYSTEM_FAULT_SOFTWARE_PROBLEM, t);
	}

	/**
	 * This will throw a common-formatted ThingsException reporting a software
	 * problem. This one allows exception chaining.
	 * 
	 * @param message
	 *            information message
	 * @param t
	 *            The throwable to chain
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.
	 * @throws things.common.ThingsException
	 */
	public static void softwareProblem(String message, Throwable t,
			String... attr) throws ThingsException {
		throw new ThingsException("SOFTWARE PROBLEM (bug):" + message,
				SYSTEM_FAULT_SOFTWARE_PROBLEM, t, attr);
	}
	
	/**
	 * This will throw a common-formatted ThingsException reporting a software
	 * problem.
	 * 
	 * @param message
	 *            information message
	 * @param attr
	 *            A list of attributes. They should come in paris, but if there
	 *            is an odd dangling attribute name, the value will be the name.
	 *            The system will attempt to ignore null entries, but it could
	 *            get confused.
	 * @throws things.common.ThingsException
	 */
	public static void softwareProblem(String message,
			String... attr) throws ThingsException {
		throw new ThingsException("SOFTWARE PROBLEM (bug):" + message,
				SYSTEM_FAULT_SOFTWARE_PROBLEM, attr);
	}

	/**
	 * Get causes. If this exception as a cause, get the root cause. It'll
	 * recurse until all causes are encoded.
	 * 
	 * @return a string listing all the causes.
	 */
	public String reportCause() {
		if (this.getCause() != null) {
			return depthCause(this).toString();
		} else
			return ThingsConstants.EMPTY_STRING;
	}

	// ===========================================================================================================================================
	// == PRIVATE HELPERS


	/**
	 * Set the worst exception.  If the cause is null, it'll set this as the worst.
	 * @param cause the cause.
	 */
	private void setWorst(Throwable cause) {
		if ( (cause==null) || (!(cause instanceof ThingsException)) ) {
			worstException = this;
		} else {
			ThingsException te = (ThingsException)cause;
			if (te.getWorst().numeric < this.numeric) worstException = te;
		}
	}
	
	/**
	 * Get causes. If this exception as a cause, get the root cause. It'll
	 * recurse until all causes are encoded.
	 * 
	 * @param t The throwable to process.
	 * @returns a StringBuffer with encoded data.
	 */
	private StringBuffer depthCause(Throwable t) {
		Throwable nextT = t.getCause();
		StringBuffer value;
		if (nextT == null) {
			value = new StringBuffer();
			value.append(t.getMessage()
					+ ThingsConstants.CODEC_SEPARATOR_CHARACTER);
			return value;
		} else {
			value = depthCause(nextT);
			value.append(t.getMessage()
					+ ThingsConstants.CODEC_SEPARATOR_CHARACTER);
		}
		return value;
	}

	/**
	 * Get attributes.
	 * 
	 * @param ina the attributes.
	 * @return decodes attributes.
	 */
	@SuppressWarnings("unused")
	private NV[] decodeAttributes(NV[] ina) {
		NV[] result = null;

		// Make sure there are attributes
		if (attributes == null) {

			if (ina == null)
				result = new NV[0];
			else
				result = ina;

		} else {

			// Size the ina
			int sizeina = 0;
			if (ina != null)
				sizeina = ina.length;

			// Our attributes
			int size = attributes.size() + sizeina;
			int index = 0;
			result = new NV[size];
			for (String item : attributes.keySet()) {
				result[index] = new NV(item, attributes.get(item));
				index++;
			}

			// The passes attributes, if any.
			// index will carry over from the above section.
			if (sizeina > 0) {
				for (int indexina = 0; indexina < sizeina; indexina++) {
					result[index] = ina[indexina];
					index++;
				}
			}
		}
		return result;
	}

}
