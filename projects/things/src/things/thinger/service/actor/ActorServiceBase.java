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
package things.thinger.service.actor;

import things.common.PuntException;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.thinger.SystemInterface;
import things.thinger.io.Logger;

/**
 * The actor service base.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 MAY 07
 * </pre> 
 */
public abstract class ActorServiceBase {
	
	/**
	 * Usable local logger.
	 */
	protected Logger logger;
	
	/**
	 * The System Interface
	 */
	protected SystemInterface si;
	
	/**
	 * Current message.  A subclass can assume this is valid in their implementation of the process() method.
	 */
	public ActorMessage currentMessage;
	
	/**
	 * Current disposition.  Used by the base only.
	 */
	private ActorDisposition currentDisposition;
	
	/**
	 * Usable ActorServiceContextInterface.
	 */
	private ActorServiceContextInterface myContext;
	
	/**
	 * Expected sequence
	 */
	int	rxSequence;
	int txSequence;
	
	/**
	 *  Init the Service.  This will be called by the service Thread.
	 *  @param si a system interface.
	 *  @param context the context.
	 *  @throws Throwable
	 */
	protected void init_service(SystemInterface  si, ActorServiceContextInterface context) throws Throwable  {
		this.si = si;
		logger = si.getSystemLogger();
		myContext = context;
		rxSequence = ActorConstants.INITIAL_SEQUENCE_NUMBER;
		txSequence = ActorConstants.INITIAL_SEQUENCE_NUMBER;
		 
		// Null messages for call safety
		currentMessage = new ActorMessage(ActorConstants.STARTUP_SEQUENCE_NUMBER);
		currentDisposition = new ActorDisposition(ActorDisposition.DISPOSITION_NONE,ActorConstants.STARTUP_SEQUENCE_NUMBER);
	}

	/**
	 *  Process a message.  This will be called by the service Thread.
	 *  @param message the message
	 *  @return true if the process indicates a quit condition.
	 */
	protected boolean process_message(ActorMessage  message) throws Throwable {
		boolean 			result = false;
		
		// Set current
		currentMessage = message;
		
		// Unnumbered or numbers?
		if (message.sequence == ActorConstants.UNNUMBERED_SEQUENCE_NUMBER) {
			
			// Only worry about QUIT
			if (this.typeIs(ActorConstants.MESSAGE_TYPE_QUIT)) {
				result = true;
				
				// Send a quit
				ActorMessage quitting = ActorMessage.buildQuickie(ActorConstants.UNNUMBERED_SEQUENCE_NUMBER,ActorConstants.MESSAGE_TYPE_QUITING);
				myContext.send(quitting);
				
			} else {
				process_unnumbered();
			}
			
		} else {
			
			// Validate
			if (message.sequence != rxSequence )  throw new ThingsException("FAULT.  RX sequence not expected.", ThingsException.ACTORSERVICE_FAULT_MESSAGE,
					ThingsNamespace.ATTR_SEQUENCE_NUMBER_EXPECTED, Integer.toString(rxSequence),  ThingsNamespace.ATTR_SEQUENCE_NUMBER_ACTUAL, Integer.toString(message.sequence));
			
			// Run
			currentDisposition = new ActorDisposition(ActorDisposition.DISPOSITION_NONE, rxSequence);
			currentDisposition.sequence = ActorConstants.UNNUMBERED_SEQUENCE_NUMBER;
			
			// Do it
			try {
				process();				
			} catch (PuntException ape) {
				// Catch the punts.
				logger.post("PUNT: " + ape.getMessage());
			} catch (Throwable t) {
				throw t;
			}
	
			// Up expected sequence
			rxSequence++;	
			
			// Handle disposition
			currentDisposition.fixType();
			myContext.send(currentDisposition);
		}
		
		return result;
	}
	
	// -- ABSTRACT INTERFACE ------------------------------------------------------------------------------------------------
	
	/**
	 *  Init the Service.
	 */
	public abstract void init() throws Throwable;
	
	/**
	 *  Quit the Service.
	 */
	public abstract void quit() throws Throwable;

	/**
	 *  Process a message.  The message is available in currentMessage.
	 */
	public abstract void process()throws Throwable;

	/**
	 *  Process an unnumbered message.  No critical processing should occur with these messages,
	 */
	public abstract void process_unnumbered()throws Throwable;
	
	// -- SERVICES ------------------------------------------------------------------------------------------------
	
	/**
	 * Get required parameter from message.  Throws an ActorPuntException if it isn't there and sets the disposition to failed.
	 * @param parameterName The name of the parameter.
	 * @see Throwable
	 * @throws Exception
	 */
	public String getRequiredParameter(String parameterName) throws Throwable {
		String result = currentMessage.getParameter(parameterName);
		if (result==null) currentDisposition.fail("Required parameter '"+ parameterName +"' not specified in a '" + currentMessage.getType() + "'");
		return result;
	}
	
	/*
	 * Get optional parameter from message.  Returns null if it isn't there.
	 * @param parameterName The name of the parameter.
	 */
	public String getOptionalParameter(String parameterName) {
		return currentMessage.getParameter(parameterName);
	}
	
	/**
	 * Send the sendLog entry.
	 * @param logEntry The log entry to send.
	 * @throws Throwable
	 */
	public void sendLog(String 	logEntry)  throws Throwable {
		ActorMessage log = ActorMessage.buildQuickie(ActorConstants.UNNUMBERED_SEQUENCE_NUMBER,ActorConstants.MESSAGE_TYPE_LOG, ActorConstants.PARAMETER_LOG,logEntry);
		myContext.send(log);
	}

	/**
	 * Respond that the message was OK.  It'll set the disposition to ok.
	 * @throws Throwable
	 */
	public void OK() throws Throwable {
		currentDisposition.ok();
	}
	
	/**
	 * Add a parameter to the response.
	 * @param name The name of the parameter.
	 * @param value The value of the parameter.
	 * @throws Throwable
	 */
	public void ADDPARAMETER(String name, String value) throws Throwable {
		currentDisposition.addParamter(name,value);
	}

	/**
	 * Respond that the message was OK with a message.  It'll set the disposition as ok, with the PARAMETER_RESPONSE being the message passed.
	 * @param message text message 
	 * @throws Throwable
	 */
	public void OK(String message) throws Throwable {
		currentDisposition.ok(message);
	}

	/**
	 * Respond that the message was FAILED.  It'll set the disposition appropriately, with the PARAMETER_RESPONSE being the message passed.
	 * This will not throw an exception.
	 * @param message The response message.
	 * @throws Throwable
	 */
	public void FAIL(String message) throws Throwable {
		currentDisposition.failSoft(message);
	}
	
	/**
	 * Respond that the message was FAILED.  It'll set the disposition appropriately, with the PARAMETER_RESPONSE being the message passed.
	 * This will then throw an PuntException.
	 * @param message The response message.
	 * @throws Throwable
	 */
	public void PUNT(String message) throws Throwable {
		currentDisposition.fail(message);
	}
	
	/**
	 * General a fault.  This will certainly teardown the service.  An attempt may be made to send a QUIT to the
	 * client.
	 * This will then throw an ActorFaultException.
	 * @param message The error message.
	 * @throws Throwable It will always be a fault.
	 */
	public void FAULT(String message) throws Throwable {
		throw new ThingsException(message, ThingsException.ACTORSERVICE_FAULT);
	}
	
	/**
	 * General a fault.  This will certainly teardown the service.  An attempt may be made to send a QUIT to the
	 * client.
	 * This will then throw an ActorFaultException.
	 * @param message The error message.
	 * @param t An exception for the chain.
	 * @throws Throwable It will always be a fault.
	 */
	public void FAULT(String message, Throwable t) throws Throwable {
		throw new ThingsException(message, ThingsException.ACTORSERVICE_FAULT, t);
	}
	
	/**
	 * Is the current message this type?
	 * @param type The type.
	 * @return true if it is, otherwise false.
	 */
	public boolean typeIs(String type) {
		return currentMessage.getType().equals(type);
	}
	
	
}