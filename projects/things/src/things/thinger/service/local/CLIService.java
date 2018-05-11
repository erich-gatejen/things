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

import java.io.StringReader;
import java.util.Collection;

import org.omg.CORBA.Any;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.common.commands.CommandLine;
import things.common.commands.CommandLineProcessor;
import things.data.AttributeCodec;
import things.data.AttributeReader;
import things.data.NVImmutable;
import things.data.Receipt;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitController;
import things.thinger.io.conduits.ConduitID;
import things.thinger.service.ServiceConstants;
import things.thinger.service.command.Command;
import things.thinger.service.command.CommandResponse;
import things.thinger.service.command.Commander;
import things.thinger.service.command.impl.Command_KILL;
import things.thinger.service.command.impl.Command_PINGPONG;
import things.thinger.service.command.impl.Command_PROCESSLIST;
import things.thinger.service.command.impl.Command_QUIT;
import things.thinger.service.command.impl.Command_RUN;
import things.thinger.service.command.impl.Command_SETPROP;
import things.thinger.service.command.impl.Command_SHOWPROPS;
import things.thinger.service.command.local.LocalCommander;

/**
 * A CLI Service.
 * <p>
 * This is pretty ugly, as I flesh out EVERY command.  I'm sure it could be optimised.
 * <p>
 * Format: (command) (arg1) (arg2) (argn)<p>
 * arg format: (token) or (name=value)<br>
 * Any token with an '=' will be assumed to be a name/value pair, even if the value is blank.  A blank name will be an error.<p>
 * <p><h2>COMMANDS</h2><p><br>
 * <B>RUN</B><br>
 * Run a command.<br>
 * Command line: run (name/classname of thing to run)<br>
 * Response line: (STATUS) : (FINAL RECIEPT) : (ATTRIBUTES: id=PROCESS_ID)<br>
 * The PROCESS_ID is usable in other commands.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 06
 * </pre> 
 */
public class CLIService extends CLIBackbone {
	
	// ===================================================================================================
	// EXPOSED DATA
	
	// ===================================================================================================
	// INTERNAL DATA
	private Commander myCommander;
	private ThingsPropertyView myProperties;
	private CommandLineProcessor cliProcessor;
		
	// ===================================================================================================
	// CONSTRUCTOR
	public CLIService() {
		super();
		cliProcessor = new CommandLineProcessor();
	}
	
	// ===================================================================================================
	// SERVICE IMPLEMENTATION
	
	/**
	 * Called to turn the service on.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOn() throws SystemException {
		// Always on
	}
	
	/**
	 * Called to turn the service off.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOff() throws SystemException {
		// Always on		
	}
	
	/**
	 * This is the entry point for the actual processing.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException {
			
		// The current processing is for test purposes only.
		try {
			
			// DATA
			String currentLine = null; 
			String response = null;
			myProperties = ssi.getLocalProperties();
			boolean responded = false;
			
			// Loop it.  If the consumer wants to stop they should throw a SystemException with SYSTEM_SERVICE_DONE as a numeric.
			while (true) {
				try {
					responded = false;	// We haven't responded.
					currentLine = null;	// We haven't gotten a command yet.
					currentLine = transactionInterface.accept();
					response = process(currentLine);
					transactionInterface.response(response);
					responded = true;
					
				} catch (InterruptedException ie) {
					// Snap it up.  The check below will see if we are really dying.
					myLogger.debug("Service interrupted by kernel.");
					
				} catch (Throwable t) {
					throw t;

				} finally {
					// Make sure we respond, if we havn't.  If we already responded, we'll get an exception we can throw away.
					try {
						if ((currentLine!=null)&&(responded==false)) transactionInterface.response(CLIServiceTools.Responses.DYING.format(null));
					} catch (Throwable tt) {
						// Don't care.  We are dying.
					}
				}
				
				if (this.getCurrentState().isDeadOrDying()) {
					break;		// See if we are supposed to stay alive.
				}
			}

		} catch (SystemException se) {
			
			// Ignore it if it is just a DONE exception
			if (se.numeric != SystemException.SYSTEM_SERVICE_DONE) {
				throw new SystemException("Unrecoverable exception in the CLIService.", SystemException.SYSTEM_FAULT_SERVICE_PROBLEM, se, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());						
			}
			
		} catch (Throwable t) {
			throw new SystemException("Unrecoverable exception in the CLIService.", SystemException.PANIC_SYSTEM_SERVICE_UNRECOVERABLE, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());			

		} 	
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 */
	public void constructThingsProcess() throws SystemException {
		
		try {
			// Get local command info
			ThingsPropertyView localProperties = ssi.getLocalProperties();
			String commandChannelName = localProperties.getProperty(ServiceConstants.CHANNEL_COMMAND);
			String responseChannelName = localProperties.getProperty(ServiceConstants.CHANNEL_RESPONSE);
						
			// Create our conduits
			ConduitController systemController = this.ssi.getSystemConduits();
			Conduit commandChannel = systemController.tune(new ConduitID(commandChannelName), this.getProcessId());
			Conduit responseChannel = systemController.tune(new ConduitID(responseChannelName), this.getProcessId());
			
			// Attach to a new LocalCommander
			myCommander = new LocalCommander(commandChannel, responseChannel, this.getProcessId());
			
		} catch (Throwable t) {
			throw new SystemException("Failed to construct CLIService.", SystemException.SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());
		}
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
	}
		
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "CLIService";
	}
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS
	
	/**
	 * The identified resource is in the process of being revoked.  It is still possible for the resource listener to call the resource within the context
	 * of this thread and call.
	 * <p>
	 * @param resourceID the ID of the resource that is being revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * The identified resource bas been revoked.  It is gone.  Attempting to call it would be a very bad thing.  The listener should remove the resource 
	 * from it's internal lists..
	 * <p>
	 * @param resourceID the ID of the resource that has been revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * Get the ID of the listener.
	 * <p>
	 * @return The listener's ID.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getListenerId() {
		return getProcessId();
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL
	
	/**
	 * Process a line.  This is a sucky CLI engine.  IT's enough for now though.
	 * @param line The line to process.
	 * @throws Any exception, though it should throw a SystemException with the numeric SystemException.SYSTEM_SERVICE_DONE if it just wants to 
	 * quit.
	 */
	private String process(String line) throws SystemException {


		CommandLine currentCommand;
		try {
			currentCommand =  cliProcessor.process(new StringReader(line), myProperties);
		} catch (ThingsException te) {
			return CLIServiceTools.Responses.FAIL_BAD_COMMAND_PARSE.format("UNKNOWN");
		}
		
		// Is there even a command?
		if ( !currentCommand.hasEntity(POSITION_COMMAND) ) return CLIServiceTools.Responses.OK_NO_COMMAND.format("NONE");
		
		// We have a command!
		String normalizedCommandToken = currentCommand.getEntity(POSITION_COMMAND).toLowerCase();
		myLogger.debug("Got command: " + normalizedCommandToken);
		
		// Let's brute this for now - choose and build command.
		Command c = null;
		Receipt r = null;
		String responseResult = CLIServiceTools.Responses.FAIL_BAD_GENERAL_PROBLEM.format(normalizedCommandToken);
		try {
			
			if (normalizedCommandToken.equals(COMMAND_PING)) {
				c = build_PINGPONG(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_PINGPONG(r);
			} else if (normalizedCommandToken.equals(COMMAND_PROCESSLIST)) {
					c = build_PROCESSLIST(currentCommand);
					r = issue(c, normalizedCommandToken);
					responseResult = response_PROCESSLIST(r, currentCommand.hasValue(COMMAND_PROCESSLIST_LOG_VALUE));
			} else if (normalizedCommandToken.equals(COMMAND_RUN)) {
				c = build_RUN(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_RUN(r);	
			} else if (normalizedCommandToken.equals(COMMAND_SETPROP)) {
				c = build_SETPROP(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_SETPROP(r);						
			} else if (normalizedCommandToken.equals(COMMAND_SHOWPROPS)) {
				c = build_SHOWPROPS(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_SHOWPROPS(r, currentCommand.hasValue(COMMAND_SHOWPROPS_LOG_VALUE));	
			} else if (normalizedCommandToken.equals(COMMAND_KILL)) {
				c = build_KILL(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_KILL(r);	
			} else if (normalizedCommandToken.equals(COMMAND_QUIT)) {
				c = build_QUIT(currentCommand);
				r = issue(c, normalizedCommandToken);
				responseResult = response_QUIT(r);	
			} else if (normalizedCommandToken.equals(COMMAND_HELP)) {
				responseResult = HELP;
			} else {
				return CLIServiceTools.Responses.FAIL_BAD_UNKNOWN_COMMAND.format(normalizedCommandToken);
			}
		
		} catch (CLIServiceBadCommandLine clisbcd) {
			// Private exception for catching bad commands.
			return CLIServiceTools.Responses.FAIL_BAD_COMMAND.format(normalizedCommandToken, null, null, clisbcd.getMessage());
			
		} catch (SystemException se) {
			// Let's only punt if it is a FAULT (and kill the tender.  Otherwise, just report the error.
			if (se.isWorseThanError()) {
				transactionInterface.unhealthy();			
				throw new SystemException ("Could not issue command.", SystemException.SYSTEM_COMMAND_FAULT_COULD_NOT_ISSUE_CLI_COMMAND, se, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, normalizedCommandToken);
			}
			else return CLIServiceTools.Responses.FAIL_BAD_COULD_NOT_ISSUE.format(normalizedCommandToken, null, null, se.getMessage());
		} catch (Throwable t) {
			// It's broken, so kill the tender and throw the exception.
			transactionInterface.unhealthy();
			throw new SystemException ("Could not issue command due to spurious exception.", SystemException.SYSTEM_COMMAND_FAULT_COULD_NOT_ISSUE_CLI_COMMAND, t, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, normalizedCommandToken);
		} finally {
			myLogger.flush();	// Make sure we don't dangle.
		}
		return responseResult;
	}
 	
	// ==========================================================================================================
	// == Inner classes
	
	/**
	 * A private exception for command processing
	 */
	class CLIServiceBadCommandLine extends ThingsException {
		final static long serialVersionUID = 1;
		public CLIServiceBadCommandLine(String message) {
			super(message);
		}
	}
	
	// ==========================================================================================================
	// == PRIVATE COMMAND METHODS

	/**
	 * General issue.
	 */
	private Receipt issue(Command c, String name) throws Throwable {
		Receipt result = myCommander.issueCommand(c);
		myLogger.info("Posted command: "  + name  + ".  receipt=" + result.toString());
		return result;
	}
	
	/**
	 * Pingpong.
	 */
	private Command build_PINGPONG(CommandLine currentCommand) throws Throwable {
		String pingToken = requireParameter(currentCommand, COMMAND_PING_WHISPER, COMMAND_PING_WHISPER_POSITION);
		Command c = new Command_PINGPONG();
		c.SET_PARAMETER(Command_PINGPONG.PARAMETER_PING, pingToken);
		return c;
	}
	private String response_PINGPONG(Receipt r) throws Throwable {
		CommandResponse cr = myCommander.queryResponse(r);
		AttributeReader resultAttributes = cr.waitRollup(ROLLUP_WAIT);

		// Result.
		String pongWhisper = AttributeCodec.encode2String(resultAttributes.getAttribute(Command_PINGPONG.RESPONSE_PONG));
		myLogger.info("PING Response.  pong=" + pongWhisper  + 
				" pong2=" + AttributeCodec.encode2String(resultAttributes.getAttribute(Command_PINGPONG.RESPONSE_SECOND_PONG)) );
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_PINGPONG.NAME, cr.getFinalReceipt(), pongWhisper, null); 
			
		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	/**
	 * Process list.
	 */
	private Command build_PROCESSLIST(CommandLine currentCommand) throws Throwable {
		Command c = new Command_PROCESSLIST();
		return c;
	}
	private String response_PROCESSLIST(Receipt r, boolean suppressOutputToCli) throws Throwable {

		// Wait for it.
		CommandResponse cr = myCommander.queryResponse(r);
		AttributeReader resultAttributes = cr.waitRollup(ROLLUP_WAIT);
		
		// Result.
		NVImmutable header = resultAttributes.getAttribute(Command_PROCESSLIST.RESPONSE_HEADER);
		Collection<NVImmutable> entries = resultAttributes.getAttributes(Command_PROCESSLIST.RESPONSE_ENTRY);
		
		// Log it
		myLogger.info("PS.  " + AttributeCodec.encode2String(header));
		for (NVImmutable item : entries) {
			myLogger.info("PS.  " + AttributeCodec.encode2String(item));		
		}
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// CLI response if not suppressed.  The additional will hold it, if we want to put the fully response to the CLI.
		String additional = null;
		if (!suppressOutputToCli) {	
			StringBuffer responseBuffer = new StringBuffer();
			
			responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);
			responseBuffer.append(CLIServiceTools.RESPONSE_LEADER);
			responseBuffer.append(AttributeCodec.encode2String(header));
			responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);
			for (NVImmutable item : entries) {
				responseBuffer.append(CLIServiceTools.RESPONSE_LEADER);
				responseBuffer.append(AttributeCodec.encode2String(item));
				responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);			
			}		
			additional =  responseBuffer.toString();
		}

		// Complete response.
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_PROCESSLIST.NAME, cr.getFinalReceipt(), null, additional); 

		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	/**
	 * Run.
	 */
	private Command build_RUN(CommandLine currentCommand) throws Throwable {
		Command c = new Command_RUN();
		c.SET_PARAMETER(Command_RUN.PARAMETER_NAME, requireParameter(currentCommand, COMMAND_RUN_NAME, COMMAND_RUN_NAME_POSITION));
		return c;
	}
	private String response_RUN(Receipt r) throws Throwable {
		CommandResponse cr = myCommander.queryResponse(r);
		AttributeReader resultAttributes = cr.waitRollup(ROLLUP_WAIT);
		String result = AttributeCodec.encode2String(resultAttributes.getAttribute(Command_RUN.RESPONSE_ID), resultAttributes.getAttribute(Command_RUN.RESPONSE_TAG));
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_RUN.NAME, cr.getFinalReceipt(), result, null); 
		
		// Result.
		myLogger.info("RUN Response.  " + result );
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	/**
	 * Kill
	 */
	private Command build_KILL(CommandLine currentCommand) throws Throwable {
		Command c = new Command_KILL();
		c.SET_PARAMETER(Command_KILL.PARAMETER_PID, requireParameter(currentCommand, COMMAND_KILL_PID, COMMAND_KILL_PID_POSITION));
		return c;
	}
	private String response_KILL(Receipt r) throws Throwable {
		CommandResponse cr = myCommander.queryResponse(r);
		cr.waitRollup(ROLLUP_WAIT);
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_KILL.NAME, cr.getFinalReceipt(), "Complete", null); 
		
		// Result.
		myLogger.info("KILL complete.");
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	/**
	 * Set a parameter.
	 */
	private Command build_SETPROP(CommandLine currentCommand) throws Throwable {
		Command c = new Command_SETPROP();
		c.SET_PARAMETER(Command_SETPROP.PARAMETER_NAME, requireParameter(currentCommand, COMMAND_SETPROP_NAME, COMMAND_SETPROP_NAME_POSITION));
		c.SET_PARAMETER(Command_SETPROP.PARAMETER_VALUE, requireParameter(currentCommand, COMMAND_SETPROP_VALUE, COMMAND_SETPROP_VALUE_POSITION));
		return c;
	}
	private String response_SETPROP(Receipt r) throws Throwable {
		CommandResponse cr = myCommander.queryResponse(r);
		cr.waitResponse(ROLLUP_WAIT);
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_RUN.NAME, cr.getFinalReceipt(), "Complete", null); 
		
		// Result.
		myLogger.info("SETPROP complete.");
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	
	/**
	 * Process list.
	 */
	private Command build_SHOWPROPS(CommandLine currentCommand) throws Throwable {
		Command c = new Command_SHOWPROPS();
		return c;
	}
	private String response_SHOWPROPS(Receipt r, boolean suppressOutputToCli) throws Throwable {

		// Wait for it.
		CommandResponse cr = myCommander.queryResponse(r);
		AttributeReader resultAttributes = cr.waitRollup(ROLLUP_WAIT);
		
		// Result.
		NVImmutable header = resultAttributes.getAttribute(Command_SHOWPROPS.RESPONSE_HEADER);
		Collection<NVImmutable> entries = resultAttributes.getAttributes(Command_SHOWPROPS.RESPONSE_ENTRY);
		
		// Log it.
		myLogger.info("SHOWPROPS.  " + AttributeCodec.encode2String(header));
		
		// There may be no entries!
		if (entries!=null) {
			for (NVImmutable item : entries) {
				myLogger.info("SHOWPROPS.  " + AttributeCodec.encode2String(item));		
			}
		} else {
			myLogger.info("SHOWPROPS.  None.");			
		}
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// CLI response if not suppressed.  The additional will hold it, if we want to put the fully response to the CLI.
		String additional = null;
		if (!suppressOutputToCli) {	
			StringBuffer responseBuffer = new StringBuffer();
			
			responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);
			responseBuffer.append(CLIServiceTools.RESPONSE_LEADER);
			responseBuffer.append(AttributeCodec.encode2String(header));
			responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);
			if (entries != null) {
				for (NVImmutable item : entries) {
					responseBuffer.append(CLIServiceTools.RESPONSE_LEADER);
					responseBuffer.append(AttributeCodec.encode2String(item));
					responseBuffer.append(ThingsConstants.CHEAP_LINESEPARATOR);			
				}		
			}
			additional =  responseBuffer.toString();
		}

		// Complete response.
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_SHOWPROPS.NAME, cr.getFinalReceipt(), null, additional); 

		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	/**
	 * Quit.
	 */
	private Command build_QUIT(CommandLine currentCommand) throws Throwable {
		Command c = new Command_QUIT();
		return c;
	}
	private String response_QUIT(Receipt r) throws Throwable {
		CommandResponse cr = myCommander.queryResponse(r);
		cr.waitResponse(ROLLUP_WAIT);
		String response =  CLIServiceTools.getCompleteResponse(cr.validateCompletion(false), Command_QUIT.NAME, cr.getFinalReceipt(), "Complete", null); 
		
		// Result.
		myLogger.info("QUIT request complete.");
		myLogger.info("Final receipt=" + cr.getFinalReceipt().toString());
		
		// Dispose and Response
		cr.dispose();
		return response;
	}
	
	// ==========================================================================================================
	// == TOOLS
	
	/**
	 * Require a parameter.  Throw an exception if it isn't present.
	 * @param currentCommand a working command line
	 * @param name the user--friendly name of the parameter.
	 * @param index the index.
	 * @throws CLIServiceBadCommandLine
	 */
	private String requireParameter(CommandLine currentCommand, String  name, int	index) throws CLIServiceBadCommandLine {
		if (! currentCommand.hasEntity(index)) throw new CLIServiceBadCommandLine("Missing required parameter at position " + index + " : name= "  + name);
		return currentCommand.getEntity(index);
	}
	
}
