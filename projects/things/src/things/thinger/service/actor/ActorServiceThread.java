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

import things.common.ThingsException;
import things.thinger.SystemInterface;
import things.thinger.io.Logger;

/**
 * The actor service.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 MAY 07
 * </pre> 
 */
public class ActorServiceThread extends Thread implements ActorServiceContextInterface {
	
	// ============================================================================================================
	// CONFIGURATION
	Logger 	logger;
	SystemInterface si;
	Socket	socket;
	int 	rvcSequence;
	BufferedInputStream		bis;
	BufferedOutputStream	bos;
	ActorServiceBase	impl;
	
	/**
	 * Initialize the lightweight service thread.
	 * @param si A system interface.
	 * @param sock The server socket.
	 * @throws Throwable
	 */
	public void initialize(SystemInterface si, Socket 	sock) throws Throwable {
		this.si = si;
		logger = si.getSystemLogger();
		socket = sock;
	}
	
	/**
	 * The run method.
	 */
	@SuppressWarnings("unchecked")
	public void run() { 
		
		String serviceImplClass = null;
		boolean failed = false;
		
		// Validate 
		if (socket==null) {
			logger.postit("QUITING.  ActorServiceThread never initialize()'d.");
			return;
		}
		if (!socket.isConnected()) {
			logger.postit("ActorServiceThread tried to run with a socket that isn't connected.");
			return;
		}
		
		// Startup 
		try {
			
			// Stream setup
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());
			
			// Read the required first message
			ActorMessage initialMessage = new ActorMessage();
			initialMessage.load(bis);
			
			// Validate 
			String messageType = initialMessage.getType();
			if (messageType.equals(ActorConstants.MESSAGE_TYPE_SERVICE)) throw new Exception("QUITING: Initial message not a SERVICE type.");
			serviceImplClass = initialMessage.getParameter(ActorConstants.PARAMETER_CLASS_NAME);
			if ((serviceImplClass==null)||(serviceImplClass.length()<1)) throw new Exception("QUITING: Initial message does not have a " + ActorConstants.PARAMETER_CLASS_NAME + " specification.");
			
			// Sequence?
			if (initialMessage.sequence!=ActorConstants.STARTUP_SEQUENCE_NUMBER) {
				throw new Exception("Initial message does not have a sequence number of " + ActorConstants.STARTUP_SEQUENCE_NUMBER + ".  It was " + initialMessage.sequence);
			}
			
			// Load implementation
			Class t = Class.forName(serviceImplClass);
			impl = (ActorServiceBase) t.newInstance();
			
			// Init the implementation
			impl.init_service(si,this);
			
			// ACK the first
			ActorMessage initialMessageResponse = ActorMessage.buildQuickie(ActorConstants.STARTUP_SEQUENCE_NUMBER,
																			ActorConstants.MESSAGE_TYPE_ACK,
																			ActorConstants.PARAMETER_ACK_SEQUENCE,
																			Integer.toString(ActorConstants.STARTUP_SEQUENCE_NUMBER));
			initialMessageResponse.save(bos);
			
		} catch (ClassNotFoundException cee) {	
			logger.postit("ActorServiceInterface implementation not found.  class name=" + serviceImplClass + " message=" + cee.getMessage());
			failed = true;
		} catch (Throwable ee) {
			logger.postit("ActorServiceThread failed startup.  message=" + ee.getMessage());
			ee.printStackTrace();
			failed = true;
		} finally {	
			if (failed) {
				try {
					socket.close();
				} catch (Exception se) {
				}
				return;		
			}
		}
		
		// Run 
		try {
			
			// Run until it quits.
			ActorMessage message;
			boolean quit = false;
			while (quit==false) {
				
				message = new ActorMessage();
				message.load(bis);
				
				quit = impl.process_message(message);	
			}
			
		} catch (Throwable ee) {
			logger.postit("ActorServiceThread failed.  message=" + ee.getMessage());
			ee.printStackTrace();
			return;		
		} finally {
			try {
				socket.close();
			} catch (Exception eee){
				// Don't care
			}
		}
		
	}
	
	/**
	 * Send the message.
	 * @param theMessage The message to send.
	 * @throws Throwable
	 */
	public void send(ActorMessage theMessage) throws Throwable {
		
		try {
			theMessage.save(bos);
			bos.flush();
		} catch (IOException ieo) {
			throw new ThingsException("IO error writing to socket.", ThingsException.ACTORSERVICE_FAULT_MESSAGE, ieo);
		} catch (Throwable t) {
			throw t;
		}
	}


}