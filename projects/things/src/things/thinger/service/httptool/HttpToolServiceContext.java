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

import java.net.Socket;

import things.common.WhoAmI;
import things.thinger.SystemInterface;

/**
 * Context for a http session used by the service. 
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  Based on something from another tool - 13 DEC 08
 * </pre> 
 */
public class HttpToolServiceContext  {
	
	// ===================================================================================================
	// EXPOSED DATA

	
	// ===================================================================================================
	// INTERNAL DATA
	public WhoAmI id;
	public HttpToolService ownerService;
	public Socket link;
	public SystemInterface si;
	public String serveRoot;
	
	// ===================================================================================================
	// METHODS

	/**
	 * Contructor.
	 * @param id ID for this connection.  Used for logging.
	 * @param ownerService The owning service.
	 * @param link The socket link.
	 * @param si A system interface.
	 * @param serveRoot the file serve root.  If null. files cannot be served.
	 * @throws Throwable
	 */
	public HttpToolServiceContext(WhoAmI id, HttpToolService ownerService, Socket link, SystemInterface si, String serveRoot) throws Throwable {
		this.id = id;
		this.ownerService = ownerService;
		this.link = link;
		this.si = si;
		this.serveRoot = serveRoot;
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL

}
