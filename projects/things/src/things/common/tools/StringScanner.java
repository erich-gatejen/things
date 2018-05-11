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
package things.common.tools;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import things.common.ThingsException;
import things.common.ThingsNamespace;

/**
 * A String Scanner.  It's not for matching, parsing, or anything like that.  Use if for very 
 * simple in-order string searches.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 7 JUN 07
 * </pre> 
 */
public class StringScanner {

	// =====================================================================================================================================
	// = INTERNAL DATA
	
	/*
	 * Pattern cache.
	 */
	private HashMap<String,Pattern> patterncache = new HashMap<String,Pattern>();
	
	/**
	 * The string we are working on.
	 */
	private String currentString;
	
	/**
	 * Cursor position.
	 */
	private int cursor;


	// =====================================================================================================================================
	// = METHODS
	
	/**
	 * Start a scan.  This must be done before any other methods are called.
	 * @param stringToScan The string to scan.
	 * @throws ThingsException if passed a null or empty string.
	 */
	public void start(String stringToScan) throws ThingsException {
		if ((stringToScan == null)||(stringToScan.length()<1)) throw new ThingsException("Cannot start with a null or empty string.", ThingsException.DATA_ERROR_CANNOT_BE_NULL_OR_EMPTY);
		currentString = stringToScan;
		cursor = 0;
	}
	
	/**
	 * Resets the cursor to the start of the string.
	 * @throws ThingsException if the scanner was not start()'d.
	 */
	public void reset() throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.reset() called before start().");
		cursor = 0;
	}
	
	
	// - MATCH USING STRING METHODS ------------------------------------------------
	
	/**
	 * Seek a string from the current cursor location.  If it is found, the cursor will be set to the new location and
	 * the method will return true.  Otherwise, it will return false.  It is case sensitive.
	 * @param stringToSeek The string to seek.
	 * @throws ThingsException Only if the object was not start()'d.
	 * @return true if found, otherwise false.
	 */
	public boolean seek(String stringToSeek) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.seek() called before start().", ThingsNamespace.ATTR_DATA_ARGUMENT, stringToSeek);

		// Assume we'll fail.
		boolean result = false;

		// Make sure the cursor is not at or beyond the end.
		if (cursor < currentString.length()) {

			try {
				int index = currentString.indexOf(stringToSeek, cursor);
				if (index >= 0) {
					// We found one.
					cursor = index;
					result = true;
				}

			} catch (Throwable t) {
				// Burn all exceptions.
			}
		}
		return result;
	}

	/**
	 * Seek a string from the current cursor location.  If it is found, the cursor will be set to the new location and
	 * the method will return true.  Otherwise, it will return false.  It is not case sensitive and is definately slower.
	 * @param stringToSeek The string to seek.
	 * @throws ThingsException Only if the object was not start()'d.
	 * @return true if found, otherwise false.
	 */
	public boolean seekinsensitive(String stringToSeek) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.seekinsensitive() called before start().", ThingsNamespace.ATTR_DATA_ARGUMENT, stringToSeek);

		boolean result = false;
		
		try {
		
		    int currentLength = currentString.length();
		    int seekLength = stringToSeek.length();
		    int run = 0;
		    int proposedCursor = cursor;
		    int rovingCursor = cursor;
		    char sourceCandidate;
		    char compareCandidate;
		    while (rovingCursor < currentLength) { 
		        
		        sourceCandidate = Character.toLowerCase(currentString.charAt(rovingCursor));
		        compareCandidate = Character.toLowerCase(stringToSeek.charAt(run));
		        
		        // Are they the same?
		        if (sourceCandidate == compareCandidate) {
		            
		            // Start a run
			        run++;		            
		            if (run == seekLength) {
		            	// We found it!
		                cursor = proposedCursor;
		                result = true;
		                break;
		            }
		            
		        } else {
		        	// break run
		            run = 0;
			        proposedCursor = rovingCursor;
		        }		     
		        
		        // Pop ahead one
		        rovingCursor++;
		    }		    
		    
		} catch (Exception e) {
			// Burn all exceptions.  We'll let false fall out.
		}
		return result; 
	}
	
	
	// - CURSOR OPERATIONS ------------------------------------------------

	/**
	 * Adds a regex pattern to the scanner.  It uses the java regex package.
	 * @param patternName The name of the pattern.
	 * @param pattern The regular expression pattern.
	 * @throws ThingsException for any problems
	 * @see java.util.regex.Pattern
	 */
	public void addpattern(String patternName, String pattern) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.addpattern() called before start().", ThingsNamespace.ATTR_DATA_ARGUMENT, patternName);

		try {
			Pattern compiledPattern = Pattern.compile(pattern);
			patterncache.put(patternName, compiledPattern);
			
		} catch (Throwable t) {
			throw new ThingsException("Could not compile a pattern.", ThingsException.DATA_ERROR_MATCHER_CANNOT_ADD, t);
		}
	}
	
	/**
	 * Find a pattern.   return size of pattern, if found.  zero if not found.  cursor left at beginning.  Cursor does not move if match fails.
	 * @param patternName The name of the pattern to find.
	 * @return The size of pattern, if found, or -1 if not found.
	 * @throws Exception
	 */
	public int find(String patternName) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.find() called before start().", ThingsNamespace.ATTR_DATA_ARGUMENT, patternName);

		Pattern workingPattern;
		int result = -1;	

		// Make sure the pattern has been added.
		if (!patterncache.containsKey(patternName)) {
			throw new ThingsException("Pattern was not added.", ThingsException.DATA_ERROR_MATCHER_PATTERN_NOT_ADDED);
		}
		workingPattern = patterncache.get(patternName);
		
		// Make sure the cursor is not at or beyond the end.
		if (cursor < currentString.length()) {
			try {
				Matcher workingMatcher = workingPattern.matcher(currentString.substring(cursor));
				if (workingMatcher.find()) {
					int local = workingMatcher.start();
					result = workingMatcher.end() - local;
					cursor = cursor + local;
				}

			} catch (Throwable t) {
				throw new ThingsException("Find failed to exception.", ThingsException.DATA_ERROR_MATCHER_FAILED, t, patternName);
			}		
		}
		return result;
	}
	
	// - CURSOR OPERATIONS ------------------------------------------------

	/**
	 * Set the cursor to the spot.  If it is out of bounds, it will throw a ThingsException.
	 * @param spot The spot to set the cursor.  If it is negative or past the end of the string, it will be out of bounds.
	 * @throws ThingsException
	 */
	public void set(int spot) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.set() called before start().");

		if ((spot < 0)||(spot >= currentString.length())) {
			throw new ThingsException("Out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(spot), ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(currentString.length()));
		}
		cursor = spot;
	}

	/**
	 * Move the cursor by an offset.  If the new cursor is out of bounds, it will throw a ThingsException.
	 * @param offset The offset to change the cursor.  It can be positive or negative.
	 * @throws ThingsException
	 */
	public void move(int offset) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.move() called before start().");
		
		int newSpot = cursor + offset;
		if (newSpot < 0) {
			throw new ThingsException("Underrun.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(offset), ThingsNamespace.ATTR_DATA_INDEX_OFFSET, Integer.toString(offset), ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(currentString.length()));	
		} else if (newSpot >= currentString.length()) {
			throw new ThingsException("Out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(offset), ThingsNamespace.ATTR_DATA_INDEX_OFFSET, Integer.toString(offset), ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(currentString.length()));	
		}
		cursor = newSpot;
	}

	/**
	 * Get the cursor position
	 * @return the cursor position.
	 */
	public int get(){
		return cursor;
	}

	/**
	 * Get the string being scanned.  This is just a convenience.
	 * @return the string we are scanning.
	 */
	public String getData(){
		return currentString;
	}

	
	/**
	 * Get a substring from the cursor for so many characters.  If the number of characters is too many, it'll throw a ThingsException.
	 * @param numberCharacters The number to get.  Zero or negative will be ignored.
	 * @return The string.
	 * @throws ThingsException
	 */
	public String substring(int numberCharacters) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.substring() called before start().");
		String result = "";

		// Only bother if it's a positive.
		if (numberCharacters > 0) {
			int newSpot = cursor + numberCharacters;
			if (newSpot > currentString.length()) 
				throw new ThingsException("Out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS, ThingsNamespace.ATTR_DATA_INDEX_BOUNDS, Integer.toString(newSpot), ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(currentString.length()));
			result = currentString.substring(cursor, newSpot);
		}
		
		return result;
	}
	
	/**
	 * Like the standard substring, from the mark to the current cursor minus one.
	 * @param mark  Mark point.
	 * @return The string.
	 * @throws ThingsException
	 */
	public String substringFromMark(int mark) throws ThingsException {
		if (currentString == null) ThingsException.softwareProblem("StringScanner.substringFromMark() called before start().");
		String result = "";
		
		try {
			result = currentString.substring(mark, cursor);
		} catch (Throwable t) {
		 	throw new ThingsException("Out of bounds.", ThingsException.DATA_ERROR_INDEX_OUTOFBOUNDS, t, ThingsNamespace.ATTR_DATA_INDEX_BOUNDS_START, Integer.toString(mark), ThingsNamespace.ATTR_DATA_INDEX_BOUNDS_END, Integer.toString(cursor));
		}
		
		return result;
	}

}
