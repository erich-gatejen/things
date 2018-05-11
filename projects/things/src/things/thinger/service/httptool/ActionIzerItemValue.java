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
 * An actual value for an  item for ActionIzer.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 NOV 08
 * </pre> 
 */
public class ActionIzerItemValue  {

	// =================================================================================================
	// == DATA
	
	/**
	 * Name.
	 */
	public String name = null;
	
	/**
	 * Actual value if a string.  This counts for properties too.
	 */
	public String value = null;
	
	/**
	 * Actual value if a boolean.
	 */
	public boolean booleanValue = false;
	
	/**
	 * Act tag to use during rendering.
	 */
	public String tag;	
	
	/**
	 * The type.
	 */
	ActionIzerItem.Type type;
	
	// =================================================================================================
	// == METHODS
	public ActionIzerItemValue(String name, String value, boolean booleanValue, String tag, ActionIzerItem.Type type) {
		if (name!=null) this.name = name.trim();
		if (value!=null) this.value = value.trim();
		this.name = name;
		this.value = value;
		this.booleanValue = booleanValue;
		this.tag = tag;
		this.type = type;
	}	
	
}
