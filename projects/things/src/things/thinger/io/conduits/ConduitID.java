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
package things.thinger.io.conduits;

import things.common.WhoAmI;

/**
 * A ConduitID.  It implements WHoAmI, so it can express a conduit ID as a unique String.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public class ConduitID implements WhoAmI {

	// ======================================================================================
	// DATA
	private String	id;
	private String	tag;
	
	// ======================================================================================
	// FIELDS
	
	/**
	 * Defines the path separator for a conduit it.
	 */
	public final static String CONDUIT_ID_SEPERATOR = ".";

	
	// ======================================================================================
	// DATA
	
	/**
	 * Construct with an imposed ID;
	 * @param imposedID The id imposed by the constructing caller.
	 */
	public ConduitID(String imposedID) {
		this.id = imposedID;
		this.tag = imposedID;
	}
	
	/**
	 * Construct with an imposed ID;
	 * @param imposedID The id imposed by the constructing caller.
	 * @param imposedTAG The optional TAG of the caller, not to be used to positive identification.
	 */
	public ConduitID(String imposedID, String imposedTAG) {
		this.id = imposedID;
		this.tag = imposedID;
	}

	/**
	 * Give a textual ID. This is a tunable name.
	 * @return the textual representation of the ID.
	 */
	 public String toString() {
		 return id;
	 }
	 
	/**
	 * Give a TAG version of the ID.  This cannot be used for positive ID, but may make a convenient mnemonic.  An implementation
	 * may return the same value as toString().
	 * @return the tag representation of the ID.
	 */
	 public String toTag() {
		 return tag;
	 }
	 
	/**
	 * Create a child ID using the given name.
	 * @param childsName the given name for the child.
	 * @return the textual representation of the ID.
	 */
	 public ConduitID birthMyChild(String  childsName) {
		 return new ConduitID(id + CONDUIT_ID_SEPERATOR + childsName);
	 }
	 
	/**
	 * Create a child ID using the given name and tag.  It must yield the same ID if the same value is used for childsName.  
	 * @param childsName the given name for the child.
	 * @param childsTag the tag for the child.
	 * @return the id
	 */
	 public WhoAmI birthMyChild(String  childsName, String childsTag) {
		 return new ConduitID(id + CONDUIT_ID_SEPERATOR + childsName, childsTag); 
	 }
}
