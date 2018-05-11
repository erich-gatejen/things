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
package things.data;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ListIterator;
import java.util.regex.Pattern;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.tools.Plato;
import things.common.tools.ValidatingStringNumericComparator;

/**
 * A grab bag of static property utilities as well as a view utility object class.  These will define 
 * how the properties are parsed and presented.
 * <p>
 * Property lists are comma delimited.  Whitespace is preserved.
 * <p>
 * ENCODING:
 * Escape with ?.
 * Separate multivalues with ,.
 * The , may be escaped with ?.
 * The = indicates equality.  It may be escaped with the ?.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Partially adapted from another project - 22 NOV 04
 * </pre> 
 * </pre> 
 */
public class ThingsPropertyReaderToolkit implements ThingsProperty {
	
	// =================================================================================
	// Validation classifications
	
	/**
	 * The supported validations.
	 * <br>
	 * Note that NOT_NULL is always assumed.
	 */
	public enum Validations {
		
		/**
		 * Not a null string.  This one is always assumed!  Any validated string will fail if this is set.
		 */
		NOT_NULL,
		
		/**
		 * Has more than one character or digit.
		 */
		NOT_EMPTY,		
		
		/**
		 * A valid, parsable numeric.
		 */
		IS_NUMERIC
		;
	}
	
	
	// ==========================================================================================
	// ==========================================================================================
	// DATA
	private ThingsPropertyViewReader myView;
	
	// ==========================================================================================
	// ==========================================================================================
	// METHODS
	
	/**
	 * Default constructor.  DO NOT CALL THIS!  It is not valid.
	 * @throws things.common.ThingsException
	 */
	public ThingsPropertyReaderToolkit() throws ThingsException {
		ThingsException.softwareProblem("Cannot call default constructor in ThingsPropertyToolkit.");
	}
	
	/**
	 * Constructor.  Create the toolkit.
	 * @param propertyViewReader The view reader to use.
	 * @throws things.common.ThingsException
	 */
	public ThingsPropertyReaderToolkit(ThingsPropertyViewReader		propertyViewReader) throws ThingsException {
		if (propertyViewReader == null) ThingsException.softwareProblem("ThingsPropertyToolkit constructed with a null propertyView.");
		myView = propertyViewReader;
	}

	/**
	 * Constructor.  Create the toolkit.
	 * @param propertyTree The tree to use.
	 * @throws things.common.ThingsException
	 */
	public ThingsPropertyReaderToolkit(ThingsPropertyTree	propertyTree) throws ThingsException {
		if (propertyTree == null) ThingsException.softwareProblem("ThingsPropertyToolkit constructed with a null propertyTree.");
		myView = propertyTree.getRoot();
	}
	
	/**
	 * Get a required property.  If the property does not exist, it will throw an exception.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequired(String path) throws ThingsException {
		if (path == null) throw new ThingsException("Property path was null for getRequired property.",ThingsException.SYSTEM_ERROR_BAD_PROPERTY_NAME_NULL);
		String result = myView.getProperty(path);
		if (result == null) throw new ThingsException("Property was not set.",ThingsException.SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SET,ThingsNamespace.ATTR_PROPERTY_NAME,path);
		return result;
	}
	
	/**
	 * Get a required property.  If the property does not exist, it will throw an exception.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequired(String... path) throws ThingsException {
		return getRequired(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a required property and evaluate it for truth.  If the property does not exist, it will throw an exception.
	 * @param path pieces of the path.
	 * @return true if it appears true (according to the Plato tool), otherwise false.
	 * @throws things.common.ThingsException 
	 * @see things.common.tools.Plato
	 */
	public boolean getRequiredTruth(String... path) throws ThingsException {
		String value = getRequired(ThingsPropertyReaderToolkit.path(path));
		return Plato.decideTruth(value, false);
	}

	
	/**
	 * Get a required property and evaluate it as an integer.  If the property does not exist or it is not an integer, it will throw an exception.
	 * @param path the property name
	 * @return the integer value
	 * @throws things.common.ThingsException 
	 * @see things.common.tools.Plato
	 */
	public int getRequiredInteger(String path) throws ThingsException {
		String value = getRequired(ThingsPropertyReaderToolkit.path(path));
		int result;
		try {
			result = Integer.parseInt(value);
		} catch (Throwable t) {
			throw new ThingsException("Property not a valid integer.",ThingsException.SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_AN_INTEGER,ThingsNamespace.ATTR_PROPERTY_NAME,path,ThingsNamespace.ATTR_DATA_VALUE,value);
		}
		return result;
	}
	
	/**
	 * Get a required property and evaluate it as an integer.  If the property does not exist or it is not an integer, it will throw an exception.
	 * @param path pieces of the path.
	 * @return the integer value
	 * @throws things.common.ThingsException 
	 * @see things.common.tools.Plato
	 */
	public int getRequiredInteger(String... path) throws ThingsException {
		return getRequiredInteger(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a required property.  If the property does not exist, it will throw an exception.  Check for requests validations.  If any
	 * validations fail, it will throw an exception with code DATA_ERROR_PROPERTY_FAILED_VALIDATION.
	 * @param path the property name
	 * @param requestedValidations The requested validations
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequired(String path, Validations... requestedValidations) throws ThingsException {
		String result = getRequired(path);
		validate(result, path, requestedValidations);
		return result;
	}
	
	/**
	 * Get a required property.  If the property does not exist, it will throw an exception.  Check for requests validations.  If any
	 * validations fail, it will throw an exception with code DATA_ERROR_PROPERTY_FAILED_VALIDATION.
	 * @param path pieces of the path.
	 * @param requestedValidations The requested validations
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequired(Collection<Validations> requestedValidations, String... path ) throws ThingsException {
		String name = ThingsPropertyReaderToolkit.path(path);
		String result = getRequired(name);
		validate(result, name, requestedValidations);
		return result;
	}
	
	/**
	 * Get a required property.  It must be a single value.  If the property does not exist, it will throw an exception.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequiredSingle(String path) throws ThingsException {
		if (path == null) throw new ThingsException("Property path was null for getRequired property.",ThingsException.SYSTEM_ERROR_BAD_PROPERTY_NAME_NULL);
		String[] result = myView.getPropertyMultivalue(path);
		if (result == null) throw new ThingsException("Property was not set.",ThingsException.SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SET,ThingsNamespace.ATTR_PROPERTY_NAME,path);
		if (result.length != 1) throw new ThingsException("Property was not a single value, as required.",ThingsException.SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SINGLE,ThingsNamespace.ATTR_PROPERTY_NAME,path);
		return result[0];
	}
	
	/**
	 * Get a required property.  It must be a single value.   If the property does not exist, it will throw an exception.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequiredSingle(String... path) throws ThingsException {
		return getRequiredSingle(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a required property.  It must be a single value.  If the property does not exist, it will throw an exception.
	 * Check for requests validations.  If any validations fail, it will throw an exception with code DATA_ERROR_PROPERTY_FAILED_VALIDATION.
	 * @param path the property name
	 * @param requestedValidations The requested validations
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequiredSingle(String path, Validations... requestedValidations) throws ThingsException {
		String result = getRequiredSingle(path);
		validate(result, path, requestedValidations);
		return result;
	}
	
	/**
	 * Get a required property.  If the property does not exist, it will throw an exception.  Check for requests validations.  If any
	 * validations fail, it will throw an exception with code DATA_ERROR_PROPERTY_FAILED_VALIDATION.
	 * @param path pieces of the path.
	 * @param requestedValidations The requested validations
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getRequiredSingle(Collection<Validations> requestedValidations, String... path ) throws ThingsException {
		String name = ThingsPropertyReaderToolkit.path(path);
		String result = getRequiredSingle(name);
		validate(result, name, requestedValidations);
		return result;
	}
	
	/**
	 * Get a required property as a multi-value.  If the property does not exist, it will throw an exception.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String[] getRequiredAsMulti(String path) throws ThingsException {
		if (path == null) throw new ThingsException("Property path was null for getRequired property.",ThingsException.SYSTEM_ERROR_BAD_PROPERTY_NAME_NULL);
		String[] result = myView.getPropertyMultivalue(path);
		if (result == null) throw new ThingsException("Property was not set.",ThingsException.SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SET,ThingsNamespace.ATTR_PROPERTY_NAME,path);
		return result;
	}
	
	/**
	 * Get a required property as a multi-value.  If the property does not exist, it will throw an exception.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String[] getRequiredAsMulti(String... path) throws ThingsException {
		return getRequiredAsMulti(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a optional property.  If the property does not exist, it will return a null.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getOptional(String path) throws ThingsException {
		if (path == null) return null;
		return myView.getProperty(path);
	}
	
	/**
	 * Get a optional property.  If the property does not exist or is empty, return the default value.  It will never throw an exception.  Any problem
	 * will yield the default value.
	 * @param path the property name
	 * @param defaultValue the default value.
	 * @return value of the property
	 */
	public String getDefaulted(String path, String defaultValue) {
		String result = null;
		try {
			result = myView.getProperty(path);
		} catch (Throwable t) {
			// Dont care.
		}
		if ((result==null)||(result.trim().length()<1)) result = defaultValue;
		return result;
	}
	
	/**
	 * Get a optional property.  If the property does not exist, it will return a null.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getOptional(String... path) throws ThingsException {
		return getOptional(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a optional property and evaluate it for truth.  If the property does not exist, it will return false.
	 * @param path pieces of the path.
	 * @return true if it appears true (according to the Plato tool), otherwise false.
	 * @throws things.common.ThingsException 
	 * @see things.common.tools.Plato
	 */
	public boolean getOptionalTruth(String... path) throws ThingsException {
		String value = getOptional(ThingsPropertyReaderToolkit.path(path));
		if (value==null) return false;
		return Plato.decideTruth(value, false);
	}
	
	/**
	 * Get a optional property.  It must be a single value.  If the property does not exist, it will return a null.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getOptionalSingle(String path) throws ThingsException {
		if (path == null) return null;
		String[] result = myView.getPropertyMultivalue(path);
		if (result == null) return null;
		if (result.length != 1) throw new ThingsException("Property was not a single value, as required.",ThingsException.SYSTEM_ERROR_OPTIONAL_PROPERTY_NOT_SINGLE,ThingsNamespace.ATTR_PROPERTY_NAME,path);
		return result[0];
	}
	
	/**
	 * Get a optional property.  It must be a single value.  If the property does not exist, it will return a null.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String getOptionalSingle(String... path) throws ThingsException {
		return getOptionalSingle(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get a optional property as a multi-value.  If the property does not exist, it will return a null.
	 * @param path the property name
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String[] getOptionalAsMulti(String path) throws ThingsException {
		if (path == null) return null;
		String[] result = myView.getPropertyMultivalue(path);
		if (result == null) return null;
		return result;
	}
	
	/**
	 * Get a optional property as a multi-value.  If the property does not exist, it will return a null.
	 * @param path pieces of the path.
	 * @return value of the property
	 * @throws things.common.ThingsException 
	 */
	public String[] getOptionalAsMulti(String... path) throws ThingsException {
		return getOptionalAsMulti(ThingsPropertyReaderToolkit.path(path));
	}
	
	/**
	 * Get the property view reader.
	 * @return The property view
	 * @see things.data.ThingsPropertyViewReader
	 */
	public ThingsPropertyViewReader getView() {
		return myView;
	}
		
	/**
	 * This will create an ordered list of property names from a ply that consists entirely of numeric entries.
	 * Anything other than numerics (integer) will cause an exception.
	 * @param path to the ply
	 * @return array of property names at the ply.
	 * @throws ThingsException
	 */
	public String[] getOrderedNumberedPly(String... path) throws ThingsException {
	
		// Get data
		Collection<String> itemList = null;
		String[] set = null;
		int size;
		
		try {
			itemList = myView.ply(ThingsPropertyReaderToolkit.path(path));
			size = itemList.size();
			set = new String[size];
			itemList.toArray(set);
			
		} catch (Throwable t) {
			throw new ThingsException("Bad ply.", ThingsException.DATA_ERROR_PROPERTY_BAD_PLY, t);
		}

		// Validate and sort.
		if (size < 1 ) throw new ThingsException("Empty ply.", ThingsException.DATA_ERROR_PROPERTY_EMPTY_PLY);
		try {
			Arrays.sort(set, new ValidatingStringNumericComparator());
		} catch (Throwable t) {
			throw new ThingsException("Bad property definition.", ThingsException.DATA_ERROR_PROPERTY_BAD_PLY, t);
		}
		
		return set;
	}
	
	// =================================================================================
	// Static tools
	
	// Privates
	private static Pattern p = Pattern.compile(ThingsProperty.PROPERTY_LINE_SEPARATOR_ASSTRING);

	/**
	 * Validate the path.  It will throw an Error level exception if it is bad.
	 * @throws things.common.ThingsException
	 */
	static public void validatePath(String path) throws ThingsException {
		if (path == null) throw new ThingsException("Property path is null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL);
	}
	
	/**
	 * Validate a value.  It will throw an Error level exception if it is bad.
	 * @throws things.common.ThingsException
	 */
	static public void validateValue(String value) throws ThingsException {
		if (value == null) throw new ThingsException("Property value is null", ThingsException.DATA_ERROR_PROPERTY_VALUE_NULL);
	}
	
	/**
	 * Validate a values.  It will throw an Error level exception if it is bad.
	 * @throws things.common.ThingsException
	 */
	static public void validateValues(String[] values) throws ThingsException {
		if ((values == null)||(values.length < 1)) throw new ThingsException("Property values is null", ThingsException.DATA_ERROR_PROPERTY_VALUE_NULL);
	}
	
	/**
	 * Fix the path to the root.
	 * @param root the root offset.  You can use null.
	 * @param offset the offset from root.
	 * @return the fixed path.
	 * @throws things.common.ThingsException
	 */
	static public String fixPath(String root, String offset) throws ThingsException {

		if ((root==null)&&(offset==null)) throw new ThingsException("Property root and offset are null", ThingsException.DATA_ERROR_PATH_NULL);
		
		if ((root==null)||(root.length() < 1)) {
			return offset;
		} else {
			if ((offset==null)||(offset.length()) < 1)
				return new String(root);
			else 
				return new String(root + ThingsProperty.PROPERTY_PATH_SEPARATOR + offset);
		}
	}
	
	/**
	 * Build a path.  If it can't build anything, it'll return a null.
	 * @param items the strings to build.
	 * @return a full path.
	 */
	static public String path(String... items) {
		if (items==null) return ThingsConstants.EMPTY_STRING;
		StringBuffer result = new StringBuffer();
		try {
			boolean first = false;
			if (items[0]!=null) {
				first = true;
				result.append(items[0]);
			}
			for (int index = 1; index < items.length; index++) {
				if (items[index]!=null) {
					if (first==true) result.append(ThingsProperty.PROPERTY_PATH_SEPARATOR);
					else first=true;
					result.append(items[index]);
				}
			}
		} catch (Throwable t) {
			// Don't care
		}
		return result.toString();
	}
	
	/**
	 * Decode the string.
	 * @param value The string to decode.
	 * @return An array of values.
	 * @throws ThingsException Null or bad strings will cause exceptions.
	 */
	public static String[]  decodeString(String value) throws ThingsException {

		StringBuffer accumulator = new StringBuffer();
		ArrayList<String> items = new ArrayList<String>();
		int size = value.length();
		char current;
		DecodeState state = DecodeState.OPEN;
		for (int index = 0; index < size; index++) {
			
			try {
				current = value.charAt(index);
				switch(current) {
				
					// Escaped
					case ThingsProperty.PROPERTY_ESCAPE_CHARACTER:
						index++;
						current = value.charAt(index);
					
						switch(current) {
						case ThingsProperty.PROPERTY_ESCAPE_CHARACTER:
							accumulator.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
							break;	
						case ThingsProperty.PROPERTY_LINE_SEPARATOR:
							accumulator.append(ThingsProperty.PROPERTY_LINE_SEPARATOR);
							break;			
						case ThingsProperty.PROPERTY_LINE_EQUALITY:
							accumulator.append(ThingsProperty.PROPERTY_LINE_EQUALITY);
							break;	
						case ThingsProperty.PROPERTY_LINE_CONTINUATION:
							accumulator.append(ThingsProperty.PROPERTY_LINE_CONTINUATION);
							break;			
						case ThingsProperty.PROPERTY_ESCAPED_CR:
							accumulator.append(ThingsProperty.PROPERTY_CR);
							break;	
						case ThingsProperty.PROPERTY_ESCAPED_LF:
							accumulator.append(ThingsProperty.PROPERTY_LF);
							break;								
						default:
							accumulator.append(current);
							break;
					
						} // end escaped switch	
						break;
						
					case ThingsProperty.PROPERTY_LINE_SEPARATOR:						
						// closure
						items.add(accumulator.toString().trim());
						accumulator = new StringBuffer();
						break;
						
					case ThingsProperty.PROPERTY_LINE_EQUALITY:
						throw new ThingsException("ThingsPropertyUtility failed decodeString() due to unescaped equal (=) sign.", ThingsException.DATA_ERROR_PROPERTY_DECODING_PROBLEM);
					
					default:
						accumulator.append(current);
					break;
					
				} // end switch	
				
			} catch (IndexOutOfBoundsException io) {
				throw new ThingsException("ThingsPropertyUtility failed decodeString() because an escape character (?) was left dangling.", ThingsException.DATA_ERROR_PROPERTY_DECODING_PROBLEM);
			}
			
		} // end for
		
		// Final Disposition by state and add it (even if blank).
		switch (state) {
		case ESCAPE:
			// Escape closure.
			accumulator.append(ThingsProperty.PROPERTY_LINE_SEPARATOR);
			break;
			
		case SEPERATOR:
			// Closure of prior and open a new (which will be saved as a blank).
			items.add(accumulator.toString().trim());
			accumulator = new StringBuffer();		
			break;
			
		default:
			break;
		}
		items.add(accumulator.toString().trim());

		// return the items.
		String[] itemsArray = new String[items.size()];
		return items.toArray(itemsArray);
	}
	private enum DecodeState { OPEN, SEPERATOR, ESCAPE } ;
	
	/**
	 * Encode a string of values and return as a new string.
	 * @param values The strings to encode.
	 * @return the String.
	 * @throws ThingsException Null or bad strings will cause exceptions.
	 */
	public static String  encodeString(String... values) throws ThingsException {
		StringWriter swout = new StringWriter();
		encodeString(swout, values);
		return swout.toString();
	}
	
	/**
	 * Encode a string and return as a new string.
	 * @param value The string to encode.
	 * @return the String.
	 * @throws ThingsException Null or bad strings will cause exceptions.
	 */
	public static String  encodeString(String value) throws ThingsException {
		StringWriter swout = new StringWriter();
		try {
			encodeString(swout, value);
		} catch (Throwable e) {
			throw new ThingsException("Spurious encoding exception.", ThingsException.DATA_ERROR_PROPERTY_ENCODING_PROBLEM, e);
		}
		return swout.toString();
	}
	
	/**
	 * Encode a string of values to a writer.
	 * @param values The strings to encode.
	 * @param out The target writer.
	 * @throws ThingsException Null or bad strings will cause exceptions.
	 */
	public static void encodeString(Writer out, String... values) throws ThingsException {
	
		// run it
		try {
			
			// Do the first one.
			encodeString(out, values[0]);
	
			// For each value
			for (int rover = 1; rover < values.length; rover++) {
				out.append(ThingsProperty.PROPERTY_LINE_SEPARATOR);
				encodeString(out, values[rover]);
			}
			out.flush();
			
		} catch (Throwable t) {
			throw new ThingsException("ThingsPropertyUtility failed encode2String().", ThingsException.DATA_ERROR_PROPERTY_ENCODING_PROBLEM, t);
		} 
	}
	
	/**
	 * Encode a single string to a writer.
	 * @param value The string to encode.
	 * @param out The target writer.
	 * @throws Throwable Null or bad strings will cause exceptions.
	 */
	public static void encodeString(Writer out, String value) throws Throwable {	
			
		int size = value.length(); 
		
		// For each character.
		for (int index = 0; index < size; index++) {
		
			switch(value.charAt(index)) {
			case ThingsProperty.PROPERTY_ESCAPE_CHARACTER:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				break;
			case ThingsProperty.PROPERTY_LINE_SEPARATOR:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_LINE_SEPARATOR);
				break;			
			case ThingsProperty.PROPERTY_LINE_EQUALITY:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_LINE_EQUALITY);
				break;					
			case PROPERTY_LINE_CONTINUATION:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_LINE_CONTINUATION);
				break;
			case PROPERTY_CR:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_ESCAPED_CR);
				break;
			case PROPERTY_LF:
				out.append(ThingsProperty.PROPERTY_ESCAPE_CHARACTER);
				out.append(ThingsProperty.PROPERTY_ESCAPED_LF);
				break;
			default:
				out.append(value.charAt(index));
				break;
			
			} // end switch
			
		} // end for
		out.flush();
	}
	
	// ============================================================================================================================
	// ============================================================================================================================
	// VALIDATION TOOLS
	
	/**
	 * Validate.  It will throw an exception with any problem. 
	 * <p>
	 * This is a kludge with the toArray.  Bleh.  No time.
	 * @param target The string to check.
	 * @param name Property name (used for error reporting, if necessary).
	 * @param requestedValidations All the validations as a collection.  Remember, NOT_NULL is assumed.
	 * @throws ThingsException with code DATA_ERROR_PROPERTY_FAILED_VALIDATION if any validation fails.
	 */
	public void validate(String target, String name, Collection<Validations> requestedValidations) throws ThingsException  {
		validate(target, name, requestedValidations.toArray(new Validations[requestedValidations.size()]));
	}
		
	/**
	 * Validate.  It will throw an exception with any problem.

	 * @param target The string to check.
	 * @param name Property name (used for error reporting, if necessary).
	 * @param requestedValidations All the validations.  Remember, NOT_NULL is assumed.
	 * @throws ThingsException with code DATA_ERROR_PROPERTY_FAILED_VALIDATION if any validation fails.
	 */
	public void validate(String target, String name, Validations... requestedValidations) throws ThingsException  {
		
		// Assumed NOT_NULL
		if (target==null) throw new ThingsException("Null value.", ThingsException.DATA_ERROR_PROPERTY_FAILED_VALIDATION, ThingsNamespace.ATTR_PROPERTY_NAME, name,  ThingsNamespace.ATTR_PROPERTY_VALIDATION, Validations.NOT_NULL.name());
		
		// Do the desired validations.
		// I'll inline these now, since there really is only one validation that examines the characters.  
		for (Validations specificValidation : requestedValidations) {
			switch (specificValidation) {
			
			case NOT_NULL:
				// Already done.
				break;
			case NOT_EMPTY:
				if (target.length() < 1) throw new ThingsException("Empty.", ThingsException.DATA_ERROR_PROPERTY_FAILED_VALIDATION, ThingsNamespace.ATTR_PROPERTY_NAME, name, ThingsNamespace.ATTR_PROPERTY_VALIDATION, Validations.NOT_EMPTY.name());
				break;
				
			case IS_NUMERIC:
				// Account for signed.
				int start;
				if (target.charAt(0)=='-') {
					start = 1;	// Jump over the -
				} else {
					start = 0;		
				}
				
				// Check to make sure the chars only fall in the ASCII numeric range.
				// 	CHAR	48	30	60	0	
				// 	CHAR	57	39	71	9		
				int character;
				for (int index = start; index < target.length(); index++) {
					character = target.charAt(index);
					if ( (character < '0') || (character > '9') ) 
						throw new ThingsException("Not a numeric.", ThingsException.DATA_ERROR_PROPERTY_FAILED_VALIDATION, ThingsNamespace.ATTR_PROPERTY_NAME, name,  ThingsNamespace.ATTR_PROPERTY_VALIDATION, Validations.IS_NUMERIC.name(), ThingsNamespace.ATTR_PROPERTY_VALUE, target);
				}
				break;	
			}
			
		} // end for desires validations
	
	}
	
	/**
	 * Validate integer.  Make sure the target comes out as a good integer.
	 * @param target The string to check.
	 * @param name property name for error reporting purposes.
	 * @return the value if it works out ok..
	 * @throws ThingsException with code DATA_ERROR_PROPERTY_FAILED_VALIDATION if any validation fails.
	 */
	public int validateInt(String target, String name) throws ThingsException  {
		int result;
		try {
			result = Integer.parseInt(target);
		} catch (Throwable t) {
			throw new ThingsException("Not a valid integer number.", ThingsException.DATA_ERROR_PROPERTY_FAILED_VALIDATION, ThingsNamespace.ATTR_PROPERTY_VALIDATION, Validations.IS_NUMERIC.name(), ThingsNamespace.ATTR_PROPERTY_VALUE, target, ThingsNamespace.ATTR_PROPERTY_NAME, name);		
		}
		return result;
	}
	
	/**
	 * Validate long.  Make sure the target comes out as a good long.
	 * @param target The string to check.
	 * @param name property name for error reporting purposes.
	 * @return the value if it works out ok..
	 * @throws ThingsException with code DATA_ERROR_PROPERTY_FAILED_VALIDATION if any validation fails.
	 */
	public long validateLong(String target, String name) throws ThingsException  {
		long result;
		try {
			result = Long.parseLong(target);
		} catch (Throwable t) {
			throw new ThingsException("Not a valid long number.", ThingsException.DATA_ERROR_PROPERTY_FAILED_VALIDATION,  ThingsNamespace.ATTR_PROPERTY_VALIDATION, Validations.IS_NUMERIC.name(), ThingsNamespace.ATTR_PROPERTY_VALUE, target, ThingsNamespace.ATTR_PROPERTY_NAME, name);		
		}
		return result;
	}
	
	/**
	 * It will take a single property and treat it as a list.  it will
	 * return a ListIterator for the list.  If the property is null or empty, it will return
	 * an empty list.
	 * @return a ListIterator to a list of Strings
	 */
	public static ListIterator<String> propertyList(String property) {
		
		ListIterator<String> result = null;
		try {
			String[] theSet = p.split(property);
			ArrayList<String> list =  new ArrayList<String>(theSet.length);
			for (int index = 0; index < theSet.length; index++) {
				list.add(index,theSet[index]);
			}
			result = list.listIterator();
				
		} catch (Exception ee) {
			result = new ArrayList<String>().listIterator();
		}
		return result;
	}

}
