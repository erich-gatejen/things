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

/**
 * A flagged string.  The flags let us know if the string has certain attributes. 
 * <p>
 * I'm thinking a more generic flaggable Data type would be better than this, but
 * there really is no lightweight way to do so in java (the generic part).  Since
 * this was added to support a port of classes, brute force seemed to be the way 
 * to go. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 06
 * </pre> 
 */
public class FlaggedString {
	
	// =========================================================================
	// DATA
	private String  	data; 
	private boolean		hasWhiteSpace;
	private boolean 	hasQuotable;

	// =========================================================================
	// METHODS
	
	/**
	 * Create a flagged string.
	 * @param theData the string.
	 * @param hasWhiteSpace the string has whitespace.
	 * @param hasQuotable the string has quotable characters.  (That is, you'll what to quote it for certain purposes.)
	 */
	public FlaggedString(String theData, boolean hasWhiteSpace, boolean	hasQuotable) {
		data = theData;
		this.hasWhiteSpace = hasWhiteSpace;
		this.hasQuotable = hasQuotable;
	}
	
	/**
	 * Does it have whitespace?
	 * @return true if so.
	 */
	public boolean hasWhiteSpace() {
		return hasWhiteSpace;
	}
	
	/**
	 * Does it have quotable?
	 * @return true if so.
	 */
	public boolean hasQuotable() {
		return hasQuotable;
	}
	
	/**
	 * Get the string.
	 * @return the string.
	 */
	public String getString() {
		return data;
	}
}