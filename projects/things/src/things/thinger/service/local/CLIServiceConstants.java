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
package things.thinger.service.local;

import things.common.ThingsConstants;
import things.thinger.service.command.impl.Command_KILL;

/**
 * A CLI Service Constants. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 06
 * </pre> 
 */
public interface CLIServiceConstants {
	
	// ===================================================================================================
	// CANNED RESPONSES
	
	// help -------------------------  --------------------------------------------------------------------------------               
	public final static String HELP = "CLI Command system for THINGS.  The following commands are available:" + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  ping (whisper)          - Ping the server to see if it is alive.  "  + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  ps ('log')              - Get the process list.  Token 'log' will force the "  + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "                            output to the log only (not the console)."  + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  run [thing]             - Run the thing named [thing]." + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  setprop [name] [value]  - Set a local property with the [name] and [value]." + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  showprops               - Show local properties." + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  kill [pid]              - Kill the process given by [pid]." + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  help                    - Show this help."  + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "  quit                    - Stop the server." + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "Local properties will be applied to the THINGS run after they are set, and will" + ThingsConstants.CHEAP_LINESEPARATOR +
	                                  "disappear after the server is stopped.  " + ThingsConstants.CHEAP_LINESEPARATOR;
	                                  
	// ===================================================================================================
	// MMI Strings

	
	// ===================================================================================================
	// COMMAND LINE TOKENS
	public final static int POSITION_COMMAND = 0;
	public final static int POSITION_ARG1 = 1;
	public final static int POSITION_ARG2 = 2;
	
	// ===================================================================================================
	// CONFIGURATION
	public final static int ROLLUP_WAIT = 1000 * 10;  // 10 Seconds?
	
	// ===================================================================================================
	// COMMAND TOKENS
	
	/**
	 * A Ping command.<br>
	 * PARAMETER: (OPTIONAL) whisper.
	 * <p>
	 * Response.<br>  
	 * OK will contain the pong whisper token, which is defined as Command_PINGPONG.RESPONSE_PONG
	 * <p>
	 * Implements Command_PINGPONG.
	 * @see things.thinger.service.command.impl.Command_PINGPONG
	 */
	public final static String COMMAND_PING = "ping";
	public final static String COMMAND_PING_WHISPER = "whisper";	
	public final static int COMMAND_PING_WHISPER_POSITION = POSITION_ARG1;

	/**
	 * A processlist command.<br>
	 * PARAMETER: (OPTIONAL) log.  If this is set as a value, it will supress output to the CLI and will instead just log the result.
	 * <p>
	 * Response.<br>  
	 * OK will contain a header and the process list.
	 * <p>
	 * Implements Command_PROCESSLIST.
	 * @see things.thinger.service.command.impl.Command_PROCESSLIST
	 */
	public final static String COMMAND_PROCESSLIST = "ps";
	public final static String COMMAND_PROCESSLIST_LOG_VALUE = "log";

	/**
	 * A run command.  This will run the named thing.<br>
	 * ARG1: (REQUIRED) name.  Path the the THING implementation.  What the path looks like is up to the server, but the most simple form would be a class definition for a class that can be found in the CLASSPATH.
	 * <p>
	 * Response.<br>  
	 * OK will contain the ID for the THING.
	 * <p>
	 * Implements Command_RUN.
	 * @see things.thinger.service.command.impl.Command_RUN
	 */
	public final static String COMMAND_RUN= "run";
	public final static String COMMAND_RUN_NAME = "name";
	public final static int COMMAND_RUN_NAME_POSITION = POSITION_ARG1;
	
	/**
	 * Set a user global property.<br>
	 * ARG1: (REQUIRED) name.  The property name.
	 * ARG2: (REQUIRED) value.  The property value.  A single value only.
	 * <p>
	 * Response.<br>  
	 * OK alone
	 * <p>
	 * Implements Command_SETPROP.
	 * @see things.thinger.service.command.impl.Command_SETPROP
	 */
	public final static String COMMAND_SETPROP = "setprop";
	public final static String COMMAND_SETPROP_NAME = "name";
	public final static int COMMAND_SETPROP_NAME_POSITION = POSITION_ARG1;
	public final static String COMMAND_SETPROP_VALUE = "value";
	public final static int COMMAND_SETPROP_VALUE_POSITION = POSITION_ARG2;

	/**
	 * A showprops command.<br>
	 * PARAMETER: (OPTIONAL) log.  If this is set as a value, it will supress output to the CLI and will instead just log the result.
	 * <p>
	 * Response.<br>  
	 * OK will contain a header and the properties list.
	 * <p>
	 * Implements Command_SHOWPROPS.
	 * @see things.thinger.service.command.impl.Command_SHOWPROPS
	 */
	public final static String COMMAND_SHOWPROPS = "showprops";
	public final static String COMMAND_SHOWPROPS_LOG_VALUE = "log";
	
	/**
	 * A kill command.  This will kill the names process.<br>
	 * ARG1: (REQUIRED) name.  Path the the THING implementation.  What the path looks like is up to the server, but the most simple form would be a class definition for a class that can be found in the CLASSPATH.
	 * <p>
	 * Response.<br>  
	 * OK 
	 * <p>
	 * Implements Command_KILL.
	 * @see things.thinger.service.command.impl.Command_KILL
	 */
	public final static String COMMAND_KILL= "kill";
	public final static String COMMAND_KILL_PID = Command_KILL.PARAMETER_PID;
	public final static int COMMAND_KILL_PID_POSITION = POSITION_ARG1;
	
	/**
	 * Command help.<br>
	 * PARAMETER:
	 * Response.<br>  
	 * List of commands and what they do.
	 * <p>
	 * This is a CLIService-only command.
	 */
	public final static String COMMAND_HELP = "help";
	
	/**
	 * Command quit. <br>
	 * PARAMETER:  None.
	 * Response.<br>  
	 * Ask the server to quit.
	 * <p>
	 */
	public final static String COMMAND_QUIT = "quit";
	
}
