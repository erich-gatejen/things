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

import java.util.Collection;

import things.data.AttributeCodec;
import things.data.AttributeReader;
import things.data.NVImmutable;
import things.data.Receipt;
import things.thinger.io.Logger;
import things.thinger.service.command.Command;
import things.thinger.service.command.CommandResponse;
import things.thinger.service.command.Commander;
import things.thinger.service.command.impl.Command_PROCESSLIST;
import things.thinger.service.httptool.Action;
import things.thinger.service.httptool.ActionResult;
import things.thinger.service.httptool.CommonTagsParams;
import things.thinger.service.local.CLIServiceTools;

/**
 * The main action for the stock implementation.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 07
 * </pre> 
 */
public class Action_ProcessList extends Action  {

	// =================================================================================================
	// == DATA

	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	/**
	 * Just return the main page.
	 */
	protected ActionResult process() throws Throwable {
		
		Logger myLogger = si.getSystemLogger();
		
		// -- Get the process list ----------
		Commander myCommander = ActionManager_Stock.getCommander();
		
		// Submit
		Command c = new Command_PROCESSLIST();
		Receipt r = myCommander.issueCommand(c);
		myLogger.debug("Posted command: PROCESSLIST.  receipt=" + r.toString());
		
		// Get response.
		CommandResponse cr = myCommander.queryResponse(r);
		AttributeReader resultAttributes = cr.waitRollup(1000);
		NVImmutable header = resultAttributes.getAttribute(Command_PROCESSLIST.RESPONSE_HEADER);
		Collection<NVImmutable> entries = resultAttributes.getAttributes(Command_PROCESSLIST.RESPONSE_ENTRY);
		
		// Log that we did it.
		myLogger.info("PROCESSLIST completed.");
		
		// Create list output.	
		StringBuffer responseBuffer = new StringBuffer();
		responseBuffer.append("<h2>");
		responseBuffer.append(AttributeCodec.encode2String(header));
		responseBuffer.append("</h2><br>");
		for (NVImmutable item : entries) {
			responseBuffer.append(CLIServiceTools.RESPONSE_LEADER);
			responseBuffer.append(AttributeCodec.encode2String(item));
			responseBuffer.append("<br>");			
		}		
		responseBuffer.append("<p>");	
		this.tags.setProperty(CommonTagsParams.TAG_PROCESS_LIST, responseBuffer.toString());
		
		// The page
		ActionResult result = new ActionResult(ActionResult.Type.PAGE);
		result.setPageResult(StockConstants.ACTION_PROCESSLIST__PAGE);

		return result;
	}
	

	// =================================================================================================
	// == METHODS
	

	
	// =================================================================================================
	// == TOOLS
	
	
}
