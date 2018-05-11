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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import things.common.PuntException;
import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.thinger.SystemInterface;
import things.thinger.io.Logger;

/**
 * The actor client base.  This can be used to make specific clients.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 MAY 07
 * </pre> 
 */
public abstract class ActorClientBase {
	
	/**
	 * Usable local logger.
	 */
	protected Logger logger;
	
	/**
	 * The System Interface
	 */
	protected SystemInterface si;

	/**
	 * The client socket.
	 */
	private Socket 					clientSocket = null;
	private BufferedOutputStream 	bos;
	private BufferedInputStream 	bis;
	
	/**
	 * Expected sequence.
	 */
	private int	rxSequence;
	private int txSequence;
	private HashSet<String> pending;

	private boolean gateBackend = false;
	
	// =====================================================================================================
	// == ABSTRACTS ========================================================================================
	
	/**
	 *  Init the Service.
	 *  @throws Throwable
	 */
	public abstract void init_chain() throws Throwable;	
	
	/**
	 *  The service is going.
	 *  @throws Throwable
	 */
	public abstract void quit_chain() throws Throwable;
	
	/**
	 *  Client base does not handle this kind of message.  Implementing class should.
	 *  Letting exceptions out of this method will generally foul the client for good.
	 *  @param e A received message.
	 *  @throws Throwable
	 */
	public abstract void message_chain(ActorMessage  e) throws Throwable;
	
	/**
	 *  Manage an NACK message.
	 *  
	 *  Letting exceptions out of this method will generally foul the client for good. 
	 */
	public abstract void message_ack(int sequence, String	response) throws Throwable;
	
	/**
	 *  Manage an ACK message.
	 *  Letting exceptions out of this method will generally foul the client for good.
	 */
	public abstract void message_nack(int sequence, String	response) throws Throwable;
	
	/**
	 *  Manage an LOG message.
	 *  Letting exceptions out of this method will generally foul the client for good.
	 */
	public abstract void message_log(String	logEntry) throws Throwable;
	
	// =====================================================================================================
	// === METHODS =========================================================================================
	
	/**
	 * Start a session.  If the session cannot be establish, it will throw an exception.
	 * @param host The host to connect to.
	 * @param port The target port.
	 * @param serviceClass The class name of the service at the distant end.
	 * @param si The system interface
	 */
	public void start(String 	host, String 	port, String	serviceClass, SystemInterface  si) throws Throwable {
		
		// Setup
		rxSequence = ActorConstants.STARTUP_SEQUENCE_NUMBER;
		txSequence = ActorConstants.STARTUP_SEQUENCE_NUMBER;

		this.si = si;
		logger = si.getSystemLogger();   
		
		// Open socket.  Ok?
		try {
			
			clientSocket = new Socket(host,Integer.parseInt(port));
			bos = new BufferedOutputStream(clientSocket.getOutputStream());
			bis = new BufferedInputStream(clientSocket.getInputStream());
			
		} catch (NumberFormatException nfe) {
			throw new ThingsException("FAULT: PORT not a valid number.", ThingsException.ACTORSERVICE_FAULT_STARTUP, nfe, ThingsNamespace.ATTR_TRANSPORT_PORT,  port);
		} catch (UnknownHostException uhe) {
			throw new ThingsException("FAULT: Cannot resolve host.", ThingsException.ACTORSERVICE_FAULT_STARTUP, uhe, ThingsNamespace.ATTR_TRANSPORT_ADDRESS,  host);	
		} catch (IOException  ioe) {
			throw new ThingsException("FAULT: Cannot connect to host.", ThingsException.ACTORSERVICE_FAULT_STARTUP, ioe, ThingsNamespace.ATTR_TRANSPORT_ADDRESS,  host, ThingsNamespace.ATTR_TRANSPORT_PORT,  port);			
		} catch (Throwable t) {
			throw new ThingsException("FAULT: Spurious exception.", ThingsException.ACTORSERVICE_FAULT_STARTUP, t, ThingsNamespace.ATTR_TRANSPORT_ADDRESS,  host, ThingsNamespace.ATTR_TRANSPORT_PORT,  port);	
		}
		
		// Send service request and read the ACK.  
		try {
			
			// Send
			ActorMessage s = ActorMessage.buildQuickie(txSequence,ActorConstants.MESSAGE_TYPE_SERVICE,ActorConstants.PARAMETER_CLASS_NAME,serviceClass);
			s.save(bos);
			txSequence++;
			
			// Get response to service
			ActorMessage initialMessage = new ActorMessage();
			initialMessage.load(bis);

			// Is it a valid ACK?
			if (initialMessage.getType().equals(ActorConstants.MESSAGE_TYPE_ACK)) {
				
				// Right sequence?
				if (initialMessage.sequence==ActorConstants.STARTUP_SEQUENCE_NUMBER) {
					
					// Parameter ok?
					String ackP = initialMessage.getParameter(ActorConstants.PARAMETER_ACK_SEQUENCE);
					if (ackP != null) {
						
						// OK, set sequence
						rxSequence++;
				
					} else {
						throw new ThingsException("FAULT: Initial message not a proper ACK.  Does not have a PARAMETER_ACK_SEQUENCE.", ThingsException.ACTORSERVICE_FAULT_STARTUP);
					}
					
				} else {
					throw new ThingsException("FAULT: Initial message does not hat the startup sequence.", ThingsException.ACTORSERVICE_FAULT_STARTUP, ThingsNamespace.ATTR_TRANSPORT_SEQUENCE, Integer.toString(initialMessage.sequence));
				}
					
			} else {
				throw new ThingsException("FAULT: Initial message not an ACK to the service startup.", ThingsException.ACTORSERVICE_FAULT_STARTUP);
			}

			// It's all good!		
			logger.info("ESTABLISHED for service " + serviceClass, ThingsCodes.SERVICE_ACTOR_CONNECTION_ACCEPTED, ThingsNamespace.ATTR_TRANSPORT_ADDRESS,  host, ThingsNamespace.ATTR_TRANSPORT_PORT,  port);
			
		} catch (IOException  ioe) {
			throw new ThingsException("FAULT: Failed to establish connection.", ThingsException.ACTORSERVICE_FAULT_STARTUP, ioe);
			
		} catch (Throwable t) {
			throw new ThingsException("FAULT: Failed to spurious exception.", ThingsException.ACTORSERVICE_FAULT_STARTUP, t);			
		}	
		 
	}
	
	/**
	 * Give the client a chance to handle inbound messages.  It is a sort of yield.  This will automatically happen after a send.
	 * @throws Throwable
	 */
	public void recieve() throws Throwable {
		// Backend
		if (clientSocket==null) return;
		if (backend()) quit_processing(); 
	}
	
	/**
	 * Send a message.
	 * @param message the message.
	 * @throws Throwable
	 */
	public void send(ActorMessage 	message) throws Throwable {
		
		String	seqString = null;
		
		try {
			
			// Are we alive?
			if ((clientSocket==null)||(!clientSocket.isConnected())||(clientSocket.isOutputShutdown())) {
				throw new ThingsException("Connection not established or uplink dead.  Send() failed.", ThingsException.ACTORSERVICE_LINK_ERROR	);
			}
			
			// Send it
			message.sequence = txSequence;
			message.save(bos);
			txSequence++;
			
			// Remember the sequence number
			seqString = Integer.toString(message.sequence);
			pending.add(seqString);
			
			// Backend 
			if (backend()) quit_processing();

		} catch (ThingsException ape) {
			throw new ThingsException("Failed to send.", ThingsException.ACTORSERVICE_SEND_ERROR, ape);
			
		} catch (NullPointerException npe) {
			throw new ThingsException("A component is null.  Likely, the connection failed.  Send() failed.", ThingsException.ACTORSERVICE_FAULT, npe);		
			
		} catch (SocketException se) {
			throw new ThingsException("Connection problem.  Likely, the connection failed.  Send() failed.", ThingsException.ACTORSERVICE_LINK_ERROR, se);					

		} catch (IOException ieo) {
			throw new ThingsException("Connection I/O problem.  Likely, the connection failed.  Send() failed.", ThingsException.ACTORSERVICE_LINK_ERROR, ieo);				
			
		} catch (Throwable t) {
			throw new ThingsException("Spurious exception.  Send() failed.", ThingsException.ACTORSERVICE_FAULT, t);		
		}
 	}
	
	// =======================================================================================
	// ## INTERNAL PROCESSING ############################################################################################
	
	/**
	 * Quit processing.
	 */
	private void quit_processing() throws Throwable {
		
		logger.post("Quitting.  Subsequint sends will cause an error.");
		this.quit_chain();
		try {
			clientSocket.close();	
			bos.close();
			bis.close();
		} catch (Exception e) {
			// Don't care
		}
		clientSocket = null;
	}
	
	/**
	 * Spend some time on the inbound queue.
	 */
	private boolean backend() throws Throwable {
		
		// Prevent recursion.  The gate must be opened at the end!
		if (gateBackend == true) return false;
		gateBackend = true;
		
		ActorMessage	currentMessage = new ActorMessage();
		String 			messageType;
		String			seqString;
		String			text;
		
		try {

			// Anything pending?  This can be dicey, since we can get held up waiting for a message.  A better client would put this in another thread.
			while (bis.available()>0) {
				
				// Get it
				currentMessage.load(bis);
				messageType = currentMessage.getType();
				
				// Type?
				if (messageType.equals(ActorConstants.MESSAGE_TYPE_ACK)) {
					seqString = currentMessage.getParameter(ActorConstants.PARAMETER_ACK_SEQUENCE);
					text = currentMessage.getParameter(ActorConstants.PARAMETER_RESPONSE);
					if (seqString==null) throw new ThingsException("FAULT:  ACK recieved without a ACK SEQUENCE number.", ThingsException.ACTORSERVICE_FAULT);
					if (pending.contains(seqString)) {
						//OK!
						pending.remove(seqString);
						message_ack(Integer.parseInt(seqString),text);
					} else {
						throw new ThingsException("FAULT:  ACK for a message does not match a sent sequence.", ThingsException.ACTORSERVICE_FAULT);
					}
					
				// NAK
				} else if (messageType.equals(ActorConstants.MESSAGE_TYPE_NACK)) {
					seqString = currentMessage.getParameter(ActorConstants.PARAMETER_ACK_SEQUENCE);
					text = currentMessage.getParameter(ActorConstants.PARAMETER_RESPONSE);					
					if (seqString==null) throw new ThingsException("FAULT:  NACK recieved without a NACK SEQUENCE number.", ThingsException.ACTORSERVICE_FAULT);
					
					if (pending.contains(seqString)) {
						//OK!
						pending.remove(seqString);
						int nak_seq = Integer.parseInt(seqString);
						message_nack(nak_seq,text);
					} else {
						throw new ThingsException("FAULT:  NACK for a message does not match a sent sequence.", ThingsException.ACTORSERVICE_FAULT);
					}
					
				} else if (messageType.equals(ActorConstants.MESSAGE_TYPE_QUIT)) {
					throw new PuntException("Quit requested by server.");
				
				} else if (messageType.equals(ActorConstants.MESSAGE_TYPE_LOG)) {
					text = currentMessage.getParameter(ActorConstants.MESSAGE_TYPE_LOG);					
					if (text==null) throw new ThingsException("FAULT:  LOG recieved without a LOG entry parameter.", ThingsException.ACTORSERVICE_FAULT);
					message_log(text);
					
				} else if (messageType.equals(ActorConstants.MESSAGE_TYPE_QUITING)) {
					// Ok, we are quitting.
					return true;
					
				} else {
					
					// Unnumbered?
					if (currentMessage.sequence!=ActorConstants.UNNUMBERED_SEQUENCE_NUMBER) rxSequence++;
						
					// let the user do it
					message_chain(currentMessage);
				}
			}
		
		} catch (Exception e) {
			throw e;
		} finally {
			// Always open the gate again.
			gateBackend = false;
		}
		
		// Dont' quit
		return false;
 	} 

	

}