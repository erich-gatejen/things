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
package things.thinger.service.httptool.stock;

import things.thinger.service.httptool.Action;
import things.thinger.service.httptool.ActionResult;

/**
 * The main action for the stock implementation.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * </pre> 
 */
public class Action_Main extends Action {

	// =================================================================================================
	// == DATA

	// =================================================================================================
	// == ABSTRACT METHODS
	
	/**
	 * Just return the main page.
	 */
	protected ActionResult process() throws Throwable {
		ActionResult result = new ActionResult(ActionResult.Type.PAGE);
		result.setPageResult(StockConstants.ACTION_MAIN__PAGE);
		return result;
	}
	

	// =================================================================================================
	// == METHODS
	

	// =================================================================================================
	// == TOOLS
	
}
