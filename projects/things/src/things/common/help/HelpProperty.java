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

package things.common.help;

import java.util.List;

import things.data.NVImmutable;


/**
 * Help item for a property
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 SEP 08
 * </pre> 
 */
public class HelpProperty  extends HelpItem {
	
	/**
	 * Is the property required?  If true, it is.
	 */
	public boolean required;
	
	/**
	 * Suggested values.  If left null, there are none.
	 */
	public List<NVImmutable> values;
	
	/**
	 * Constructor.
	 * @param required Is the property required?
	 * @param name  The name of the property.  This is suitable for lookup in a view.
	 * @param help The help text for the property. 
	 */
	public HelpProperty(boolean required, String name, String help) {
		super(name, help);
		this.required = required;
	}
	
	/**
	 * Constructor.
	 * @param required Is the property required?
	 * @param name  The name of the property.  This is suitable for lookup in a view.
	 * @param user The user name for this property.
	 * @param help The help text for the property. 
	 */
	public HelpProperty(boolean required, String name, String user, String help) {
		super(name, user, help);
		this.required = required;
		
	}
	
}
