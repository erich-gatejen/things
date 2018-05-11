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
package things.thinger;

import things.common.ThingsNamespace;

/**
* System namespace definitions.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 05
 * </pre> 
*/
public interface SystemNamespace extends ThingsNamespace {

	// ===========================================================================================
	// == ATTRIBUTES
	
	public static final String ATTR_PROCESS_ID = "process.id";
	public static final String ATTR_PROCESS_ID_ORGANIC = "process.id.organic";
	
	public static final String ATTR_CLEARANCE_REQUIRED = "clearance.required";
	public static final String ATTR_CLEARANCE_ACTUAL = "clearance.actual";
	
	public static final String ATTR_SYSTEM_ID_WHOAMI = "system.id";
	public static final String ATTR_SYSTEM_ID_WHOAMI_FOR_A_LOGGER = "system.id.logger";
	public static final String ATTR_SYSTEM_ID_WHOAMI_CREATOR = "system.id.creator";
	
	public static final String ATTR_SYSTEM_RECEIPT = "system.receipt";
	public static final String ATTR_SYSTEM_RECEIPT_TEXT = "system.receipt.text";
	
	public static final String ATTR_SYSTEM_OPERATION_NAME = "system.operation.name";
	
	public static final String ATTR_SYSTEM_SERVICE_NAME = "system.service.name";
	public static final String ATTR_SYSTEM_SERVICE_CLASS = "system.service.class";
	
	public static final String ATTR_SYSTEM_OBJECT_NAME = "system.object.name";
	public static final String ATTR_SYSTEM_OBJECT_NAME_ACTUAL = "system.object.name.actual";
	public static final String ATTR_SYSTEM_OBJECT_BASE = "system.object.base";
	
	public static final String ATTR_SYSTEM_COMMAND_TEXT = "system.command.text";
	public static final String ATTR_SYSTEM_COMMAND_NAME = "system.command.name";
	public static final String ATTR_SYSTEM_COMMAND_ID = "system.command.id";
	public static final String ATTR_SYSTEM_COMMAND_PARAMETER_NAME = "system.command.parameter.name";
	public static final String ATTR_SYSTEM_COMMAND_RESPONSE_NAME = "system.command.response.name";
	public static final String ATTR_SYSTEM_COMMAND_NUMERIC = "system.command.numeric";
	public static final String ATTR_SYSTEM_COMMAND_RESPONSE_STATE = "system.command.response.state";
	public static final String ATTR_SYSTEM_COMMAND_RESPONSE_TRANSMISSION_RECEIPT = "system.command.response.transmission.reciept";
	public static final String ATTR_SYSTEM_COMMAND_INSTANCE_TYPE = "system.command.instance.type";
	public static final String ATTR_SYSTEM_COMMAND_COMMANDER_ID = "system.command.commander.id";
	
	public static final String ATTR_SYSTEM_RESOURCE_STATE = "system.resource.state";  
		
	public static final String ATTR_PLATFORM_FS_PATH = "platform.fs.path";
	public static final String ATTR_PLATFORM_FS_PATH_ABSOLUTE = "platform.fs.path.absolute";
	
	public static final String ATTR_UNIVERSE_NAME = "universe.name";
	
	public static final String ATTR_DANGLE_FILE = "dangle.file";
	public static final String ATTR_DANGLE_CAUSE = "dangle.cause";
	
	public static final String ATTR_ADDRESS = "address";
	public static final String ATTR_ADDRESS_NETWORK = "address.network";

	
	// ====================================================================================================
	// == CONFIGURATION
	
	public static final String CONFIG_COMMAND_CHANNEL = "command.channel";
	public static final String CONFIG_COMMAND_CHANNEL_SERVICE_COMMAND_LOCAL = "command.channel.service.command.local";
	public static final String CONFIG_COMMAND_CHANNEL_LOCAL_CLISERVICE = "command.channel.local.cliservice";
		
	
}
