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

/**
 * An identification object for a nobody.  This means the ID is nothing<br>
 * It will textually resolve as ThingsConstants.A_NOBODY
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 OCT 04
 * </pre> 
 */
public class IAmNobody implements WhoAmI   {

	/**
	 * Give a textual ID
	 * @return the textual representation of the ID.
	 * @see things.common.ThingsConstants
	 */
	 public String toString() {
		 return ThingsConstants.A_NOBODY; 
	 }
	
	/**
	 * Give a TAG version of the ID.  This cannot be used for positive ID, but may make a convenient mnemonic.  An implementation
	 * may return the same value as toString().
	 * @return the tag representation of the ID.
	 */
	 public String toTag() {
		 return ThingsConstants.A_NOBODY; 
	 }
		 
	 
	/**
	 * Create a child ID using the given name.
	 * @param childsName the given name for the child.
	 * @return the textual representation of the ID.
	 */
	 public IAmNobody birthMyChild(String  childsName) {
		 return new IAmNobody();
	 }
	 
	/**
	 * Create a child ID using the given name and tag.  It must yield the same ID if the same value is used for childsName.  
	 * @param childsName the given name for the child.
	 * @param childsTag the tag for the child.
	 * @return the id
	 */
	 public WhoAmI birthMyChild(String  childsName, String childsTag) {
		 return new IAmNobody();
	 }
	 
}
