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
package things.data.language;

/**
 * A hunk of text.  I'm just exposing the fields and not making methods, because it'll just slow it down way too much. 
 * <p>  
 * <b>THIS PACKAGE WAS ABANDONED IN FAVOR OF ANOTHER PROJECT</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 MAY 06
 * </pre> 
 */
public class AText {
	
	// ==========================================================================================================
	// == FIELDS
	
	/**
	 * Current wrap position.
	 */
	public int	currentWrap;
	
	/**
	 * The text.
	 */
	public StringBuffer	text;
	
	// ==========================================================================================================
	// == METHODS
	
	/**
	 * Constructor.
	 */
	public AText() {
		text = new StringBuffer();
	}
	
	

}
