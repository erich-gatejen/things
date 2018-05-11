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

import things.common.ThingsException;

/**
 * An action exception.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * EPG - Upgrade to ThingsException - 13 DEC 08
 * </pre> 
 */
public class ActionException extends ThingsException  {

	final static long serialVersionUID = 1;
	
	// =================================================================================================
	// == DATA
	private String description;

	// =================================================================================================
	// == METHODS
	
	/**
	 * Constructor.
	 * @param message the full message.  This will be rendered as CommonTagsParams.TAG_ERROR_MESSAGE.  This is available through getMessage();
	 * @param description the short description.  This will be rendered as CommonTagsParams.TAG_ERROR_DESCRIPTION.  This is available through getDescription();
	 */
	public ActionException(String  description, String  message) {
		super(message);
		this.description = description;
	}

	/**
	 * Get the description.
	 * @return the description.
	 */
	public String getDescription() {
		return description;
	}
	
    /**
     * Returns the detail message string of this exception, including the description and message.
     * @return  a fully rolled message.
     */
    public String getMessage() {
    	return super.getMessage() + " : " + description;
    }
	
	
}
