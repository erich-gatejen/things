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

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;

/**
 * An action result. 
 * <p><pre> It can be:
 * QUIET - which means there is no response.  The server will just put a polite OK message.
 * PAGE - there is a page response.  The page name is available from this object with a call to getPageResult();
 * ACTION - there is a new action to perform.  The request should be relayed to it.  The new action can be gotten from a call to getActionResult()
 * ERROR - there was an error.  the response will be handled by the service.  However, you can influence which page is used for error by setting it with a call to setPageResult().
 * </pre>
*  <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * </pre> 
 */
public class ActionResult {
	
	public enum Type {
		/**
		 * There is no response.
		 */
		QUIET,
		
		/**
		 * Display the named page.  Use getPageResult() to get the page name.
		 */
		PAGE,
		
		/**
		 * Render headers.
		 */
		HEAD,
		
		/**
		 * Perform another action.  Use getActionResult() to get the action name.
		 */
		ACTION,
		
		/**
		 * Serve a file.
		 */
		SERVE,
		
		/**
		 * There was an error.
		 */
		ERROR;	
		
	}
	
	// ===========================================================================================
	//  DATA
	public Type type;
	
	// Page results
	private String 	pageName;
	private	String	actionName;
	
	// Other results.
	private HashMap<String,String> headers;
	private String reponse;
	private InputStream dataStream;
	
	
	// ===========================================================================================
	//  METHODS
	
	/**
	 * Constructor.
	 * @param type the type.
	 */
	public ActionResult(Type type) {
		this.type = type;
		headers = new HashMap<String,String>();
	}
	
	/**
	 * Get the result page name.  It may be null.
	 * @return the page name.
	 */
	public String getPageResult() {
		return pageName;
	}
	
	/**
	 * Set the page result name.  Null is acceptable.
	 * @param pageName the page name.  Null is acceptable.
	 */
	public void setPageResult(String pageName) {
		this.pageName = pageName;
	}
	
			
	/**
	 * Get the action result name.
	 * @return the name.
	 */
	public String getActionResult() {
		return actionName;
	}
	
	/**
	 * Set the action result name.
	 * @param actionName the name.  Null is acceptable.
	 */
	public void setActionResult(String actionName) {
		this.actionName = actionName;
	}
		
	/**
	 * Get header values.
	 * @return the headers.
	 */
	public Collection<String> getHeaders() {
		return headers.values();
	}
	
	/**
	 * Add a header.  It will override any previous header of the same name.
	 * @param name Name
	 * @param value Value
	 */
	public void addHeader(String name, String value) {
		headers.put(name, name + ": " + value);
	}
	
	/**
	 * Get the response line.
	 * @return the response.
	 */
	public String getResponse() {
		return reponse;
	}
	
	/**
	 * Set the response line.
	 * @param reponse the response.
	 */
	public void setResponse(String reponse) {
		this.reponse = reponse;
	}
	
	/**
	 * Set input stream.  This done sometime right before reponse.
	 * @param inputStream the data stream.
	 */
	public void setInputStream(InputStream inputStream) {
		dataStream = inputStream;
	}
	
	/**
	 * Get input stream.  This is required for SERVE action.
	 * @return the data stream.
	 */
	public InputStream getInputStream() {
		return dataStream;
	}
	
}

