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
package things.data.processing.rfc822;


/**
 * A complete RFC822 address.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 05
 * </pre> 
 */
public class FullAddress  {

		/**
		 * The Internet address.
		 */
		public String inet;
		
		/**
		 * The friendly name.
		 */
		public String friendly;
		
		/**
		 * The group name.  (Usually not used).
		 */
		public String group;
		
		/**
		 * Busted field.  Anything the parsers can't handle goes here.  Technically, if anything is here, the address is bad.
		 */
		public String busted;
		
		
		/**
		 * Default constructor.
		 */
		public FullAddress() {
			super();
			inet = "";
			friendly = "";
		}
		
		/**
		 * Full constructor.
		 * @param inet
		 * @param friendly
		 * @param group
		 * @param busted
		 */
		public FullAddress(String inet, String friendly, String group, String busted ) {
			super();
			this.busted = busted;
			if (friendly==null) friendly = "";
			else this.friendly = friendly;
			if (inet==null) inet = "";
			else this.inet = inet;
			this.group = group;
		}
		
		/**
		 * Render to a string using common 822 notation.  It will always render the complete address, if available.
		 * TODO need to fix the render to handle escapes.
		 * @return the rendering.
		 * @throws Throwable
		 */
		public String render() throws Throwable {
			if (friendly==null) {
				return inet;
			} else {
				return friendly + " <" + inet + ">";
			}
		}
		
		/**
		 * Render to a string using less common notation, where the friendly name is quoted.  (If the friendly is already quoted, it won't do it).
		 * TODO need to fix the render to handle escapes.  It only checks the first character to see if it is quoted.
		 * @return the rendering.
		 * @throws Throwable
		 */
		public String renderQuoted() throws Throwable {
			if (friendly==null) {
				return inet;
			} else {
				if ((friendly.length()>0)&&(friendly.charAt(0)=='"'))
					return friendly + " <" + inet + ">";
				else
					return "\"" + friendly + "\" <" + inet + ">";	
			}
		}
		
		/**
		 * Is the friendly name null or empty?
		 * @return true if it is.
		 */
		public boolean isFriendlyNullOrEmpty() {
			if (friendly==null) return true;
			if (friendly.length() < 1) return true;
			return false;
		}
		
}
