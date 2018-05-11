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
package things.thinger.service;

/**
 * Constants for services.

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 11 AUG 05<br>
 * EPG - Add http service - 6 NOV 07
 * </pre> 
 */
public interface ServiceConstants {

	// ========================================================================================================
 	// == Service specification properties.  This should be part of the service local properties.
	
	/**
	 * Command and response channel.
	 */
	public static final String	CHANNEL_COMMAND	 = "command_channel";
	public static final String	CHANNEL_RESPONSE = "response_channel";
	
	/**
	 * Socket listen port.
	 */
	public static final String	LISTEN_PORT	 = "listen";
	public static final String	RELAY_CONNECT_ADDRESS	= "relay.address";
	public static final String	RELAY_CONNECT_PORT	 	= "relay.port";

	/**
	 * Service thread pool..
	 */
	public static final String	POOL_MAX = "pool.max";
	

	/**
	 * Http tool service.
	 */
	public static final String	PAGE_MANAGER = "manager.page";
	public static final String	PAGE_MANAGER_UNIVERSE_ADDRESS = "manager.page.addy";
	public static final String	ACTION_MANAGER = "manager.action";	
	public static final String	SERVE_ROOT = "root";
	public static final String	CLEARANCE = "clearance";
	
	/**
	 * Proxy service.
	 */
	public static final String PROXY_PROCESSOR = "proxy.processor";
	public static final String PROXY_SESSION_PATH = "proxy.session.path";
	
	
}