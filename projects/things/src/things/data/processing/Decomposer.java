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

package things.data.processing;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import things.common.ThingsException;

/**
 * General decomposer.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 22 OCT 06
 * </pre> 
 */
public class Decomposer {

	// ========================================================================================
	// DATA
	private boolean valid = false;
	private BufferedReader ireader;
	private StringTokenizer currentTokens;
	private String currentLine;
	private String currentDelim;
	private int lineNumber;
	
	/**
	 * The default delimiter characters in a string.
	 */
	public final static String DEFAULT_DELIMITERS = " \t\n\r\f";

	// ========================================================================================
	// METHODS
	
	public Decomposer() {
		// Always start invalid.  Done() will always make it invalid again, as well as normal processing.
		valid = false;
		lineNumber = 0;
	}
	
	/**
	 * Start a decomposition on an Input Stream using specified delimiters.
	 * If a prior session has been started, it will dump it.
	 * @param ios the input stream.  It will be closed by this class when it is done with it.  Done is either explicitly stated (with done()), the object is finalized, or you start() a new session.
	 * @param delimiters the delimiters used to decompose tokens.  If null, the default will be used, which is SPACE, TAB, CF, LF, and FORM FEED.
	 * @throws Throwable
	 */
	public void start(InputStream ios, String delimiters) throws Throwable {
		
		// Qualify
		if (ios==null) ThingsException.softwareProblem("You cannot start a decomposer with a null stream.");
		if (delimiters==null) {
			currentDelim = DEFAULT_DELIMITERS;
		} else {
			if (delimiters.length()<1) ThingsException.softwareProblem("You cannot start a decomposer with an empty (but not null) delimiters String.");
			currentDelim = delimiters;
		}

		// Invalidate the whole thing
		valid = false;
		lineNumber = 0;

		// If one is already chugging, we are done with it.
		if (ireader != null) {
			done();
		}

		// Build a buffered reader
		ireader = new BufferedReader(new InputStreamReader(ios));
		if (this.eat()) {
			// It's a valid stream
			valid = true;
		}
	}

	/**
	 * Start a decomposition on an Input Stream using default delimiters.
	 * If a prior session has been started, it will dump it.
	 * @param ios the input stream.  It will be closed by this class when it is done with it.  Done is either explicitly stated (with done()), the object is finalized, or you start() a new session.
	 * @throws Throwable
	 */
	public void start(InputStream ios) throws Throwable {
		start(ios, null);
	}

	/**
	 * Return the next line.  If there is no next line or no session has been started, it will return a null.
	 * @return the next line or null
	 */
	public String line() {

		String result = null;

		if (valid) {
			result = currentLine;
			valid = this.eat();

		}
		return result;
	}

	/**
	 * Look at the current line.  If there is no next line or no session has been started, it will return a null.
	 * @return the next line or null
	 */
	public String peekLine() {

		String result = null;

		if (valid) {
			result = currentLine;
		}
		return result;
	}
	
	/**
	 * Return the next token.  If there is no next token or no session has been started, it will return a null.
	 * @return the next token or a null
	 */
	public String token() {
		String result = null;

		if (valid) {

			if (currentTokens.hasMoreTokens()) {
				result = currentTokens.nextToken();
			} else {
				valid = this.eat();
				if (valid == true) {
					result = currentTokens.nextToken();
				}
			}

		}
		return result;
	}

	/**
	 * Return the next token from the current line.  If there is no next token or no session has been started, it will return null.
	 * @return the next token or a null
	 */
	public String nonbreaking_token() {
		String result = null;

		if (valid) {

			if (currentTokens.hasMoreTokens()) {
				result = currentTokens.nextToken();
			}
		}
		return result;
	}

	
	/**
	 * Return "true" if there are more tokens and/or lines, else "false."
	 * @return "true" if there is more, otherwise "false"
	 */
	public boolean hasmore() {

		// Assume it will fail
		if (valid) {
			if (currentTokens.hasMoreTokens()) {
				// current line has the goods
				return true;
			} else {
				// see if the next line has the goods
				valid = this.eat();
				if (valid) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Done.  The source will be closed.  All errors will be eaten.
	 */
	public void done() {

		// No longer valid.
		valid = false;
		lineNumber = 0;
		
	    try  {
		// If one is already chugging, close it
		if (ireader != null)
			ireader.close();
			ireader = null;
	    } catch (Exception e) {
	        //dont care
	    }
	}

	/**
	 * Get the current line number.
	 * @return the current line number.
	 */
	public int getLineNumber() {
		return lineNumber;
	}
	
	
	// ========================================================================================
	// INTERNAL
	
	/**
	 * Finalizer.  Just so we can close stuff like we promised.
	 * @throws Throwable but it will not ever happen.
	 */
	protected void finalize() throws Throwable {
		// Be nice.
		done();
	}
	
	/**
	 * Chew lines until one is found with a token or there is nothing left to chew.
	 * @return true if successful, false if we ran out
	 */
	private boolean eat() {

		try {
			currentLine = ireader.readLine();
			lineNumber++;
			while (currentLine != null) {
				currentTokens = new StringTokenizer(currentLine, currentDelim);
				if (currentTokens.hasMoreTokens())
					return true;
				currentLine = ireader.readLine();
				lineNumber++;
			}
		} catch (Exception e) {
			// Don't care.  false will bubble out
		}
		return false;
	}
	
}