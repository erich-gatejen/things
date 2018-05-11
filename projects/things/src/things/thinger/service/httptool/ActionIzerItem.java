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
package things.thinger.service.httptool;


/**
 * A configured item for ActionIzer.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 NOV 08
 * </pre> 
 */
public class ActionIzerItem  {

	// =================================================================================================
	// == FIELDS
	
	public static String BOOLEAN__SELECTED_VALUE  = "checked";		// This is because the booleans should be HTML radio
	
	// =================================================================================================
	// == DATA
	
	// -- PARAMETER ----------------------------------------------
	public String name = null;
	public boolean isRequired;
	public Type type = null;
	
	// -- DEFAULT VALUES  ----------------------------------------------
	public String value = null;			// counts for true
	public String valueFalse = null;	// counts for false for checked
	public boolean booleanValue;
	
	// -- ASSOCIATED TAG -----------------------------------------
	public String tag;			// Counts for true for CHECKED items.
	public String tag_false;	
	
	// -- ASSOCIATED PROPERTY -----------------------------------------
	public String propertyName;  // If null, there is no property associated with it.
	
	public enum Type { 
		STRING,
		BOOLEAN,
		CLASSED,
		CHECKED, 
		PROPERTIES;
	}
	
	// =================================================================================================
	// == METHODS
	public ActionIzerItem(String name, String value, boolean isRequired, Type type, String tag, String propertyName) {
		if (name!=null) this.name = name.trim();
		if (value!=null) this.value = value.trim();
		this.isRequired = isRequired;
		this.type = type;
		this.tag = tag;
		this.propertyName = propertyName;
		if (propertyName!=null) this.propertyName = propertyName.trim();
	}	
	
}
