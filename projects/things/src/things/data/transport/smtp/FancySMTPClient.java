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
package things.data.transport.smtp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import things.common.Debuggable;
import things.common.ThingsCodes;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.thinger.io.Logger;

/**
 * This is a fancy SMTP client.  It expected the server to supposed extensions<p>
 * The client will propagate any InterruptedExceptions like a good THINGs citizen, though it will then mark the client as BAD and it will 
 * no longer work--through exceptions for any call thereafter.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request - 1 JUNV 09
 * </pre> 
 */
public class FancySMTPClient implements SMTPClient, Debuggable {

	// == EXPOSED SETTINGS ===============================================================================
	public final static int CONNECTION_TIMEOUT = 1000 * 60 * 3; // 3 minutes
	public final static int CONNECTION_RETRIES = 0;  // NONE
	
	// == INERNTAL SETTINGS SETTINGS ====================================================================
	private final static int READ_FULLY_LIMIT = 1024 * 2;
	private final static int BUMP_LIMIT = 5;
	
	// ===================================================================================================
	// == EXPOSED DATA AND CLASSES ==================================

	/**
	 * Extension names.
	 */
    public static final String EXTENSION_DSN = "DSN";

	
	// ===================================================================================================
	// == INTERNAL DATA ==================================
    
    // Connection data
	private Socket 	connection;
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	private Logger myLogger;
	private SMTPState state;
	private boolean extensionDSN;
	
	// Other data
	private String 	clientName;
	private long	lastSize;
	private int		timeout;
	private int		retries;

	// ===================================================================================================
	// == METHODS  ==================================
	
	/**
	 * Create a new client with an imposed name.  This will not connect.
	 * @param theLogger the logger to use.  It will be quiet unless you start debugging.
	 * @param name the client's name.  It will be slightly altered for uniqueness.
	 */
	public FancySMTPClient(Logger theLogger, String name) {
		myLogger = theLogger;
		state = SMTPState.FRESH;
		clientName = name + "." + ThingsUtilityBelt.nextInteger();
	}

	/**
	 * Create a new client.  This will not connect.
	 * @param theLogger the logger to use.  It will be quiet unless you start debugging.
	 */
	public FancySMTPClient(Logger theLogger) {
		myLogger = theLogger;
		state = SMTPState.FRESH;
		clientName = "cleansmtp." + ThingsUtilityBelt.nextInteger();
	}
	
	/**
	 * Get client name.
	 * @return the clients name.
	 */
	public String getName() {
		return clientName;
	}
	
	/**
	 * Get the SMTPState.
	 * @return State
	 */
	public SMTPState getState() {
		return state;
	}
	
	/**
	 * Set the socket timeout for each connection.  A timeout may be retried, depending on the value set with setSocketRetries(int).<br>
	 * The default value is CONNECTION_TIMEOUT.
	 * <p>
	 * It will take effect whenever a new connection is established.
	 * @param timeout the timeout in milliseconds.
	 */
	public void setTimeout(int timeout) {
		if (timeout < 0) this.timeout = 0;
		else this.timeout = timeout;
	}
	
	/**
	 * Number of times a socket read will be retried after a timeout before completely giving up.<br>
	 * The default value is CONNECTION_RETRIES.
	 * <p>
	 * It will take effect whenever a new operation is started.
	 * @param retries Number of retries.  Zero or a negative number is the same as no retries.
	 */
	public void setSocketRetries(int retries) {
		if (retries < 0) this.retries = 0;
		else this.retries = retries;
	}
	
	/**
	 * Start a connection.  Return a code.  It will throw an ERROR is already connect, but will leave the connection intact.
	 * @param connectAddress 
	 * @param connectPort
	 * @return ACTION_OK if it was ok.
	 * @throws ThingsException will always be fatal at this point.
	 */
	public Reply connect(String connectAddress, int connectPort) throws ThingsException, InterruptedException   {
		
		Reply welcome = null;  // This null should never last, since the only way this wont be set is if the next try has an exception.
		
		try {
			
			if (state == SMTPState.BAD) abandonConnection();
			if (state != SMTPState.FRESH) {
				if (myLogger.debuggingState()) myLogger.post("Already connected " + clientName);
				throw new ThingsException("Already connected.", ThingsCodes.SMTPCLIENT_ERROR_ALREADY_CONNECTED, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
			}
			
			// Connect
			state = SMTPState.BAD;
			try {
				connection = new Socket(connectAddress, connectPort);
				connection.setSoTimeout(timeout);
				connection.setKeepAlive(true);
				bis = new BufferedInputStream(connection.getInputStream());
				bos = new BufferedOutputStream(connection.getOutputStream());	
			} catch (Throwable t) {
				if (myLogger.debuggingState()) myLogger.post("Failed to connect " + clientName);
				throw new ThingsException("Failed to connect.", ThingsCodes.SMTPCLIENT_FAULT_STARTUP, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
			}
			
			// Read the welcome
			try {
				welcome = getOnlyOne(readReplies()); 

			} catch (Throwable t) {
				throw new ThingsException(t.getMessage(), ThingsCodes.SMTPCLIENT_FAULT_STARTUP, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
			}		
			
			// Is it a problem?
			if (welcome.code >= CODE_FAIL_THRESHOLD)
				throw new ThingsException("Connection rejected by server.", ThingsCodes.SMTPCLIENT_FAULT_STARTUP, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, 
						ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(welcome.code), ThingsNamespace.ATTR_MESSAGE, welcome.text);
		
			// Complete
			state = SMTPState.CONNECTED;
			if (myLogger.debuggingState()) debugReply("connect", welcome);
			
		} finally {
			// Abandon connection if bad.
			if (state != SMTPState.CONNECTED) abandonConnection();
		}
			
		return welcome;
	}	
	
	
	/**
	 * Start a connection.  Return a code.
	 * @return The reply.
	 * @throws ThingsException which will never happen, since we'll just abandon the bad connection.
	 */
	public Reply	disconnect() throws ThingsException, InterruptedException  {
		try {
			connection.close();
		} catch (Throwable t) {
			// Just abandon it unless it is an InterruptedException
			if (t instanceof InterruptedException) throw (InterruptedException)t;
		} finally {
			state = SMTPState.FRESH;
			connection = null;
		}
		
		return new Reply(ACTION_OK, "ok");
	}
	
	/**
	 * HELO with the hostname--no authentication.  A state problem will cause a FAULT.  A transmission problem is just an ERROR.  It will transition to LOGIN_COMPLETE only
	 * of the reply is an ACTION_OK.
	 * @param hostname
	 * @return the reply.
	 */
	public Reply login(String hostname) throws ThingsException, InterruptedException  {
		
		// Qualify.  Must be connected.
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if (state != SMTPState.CONNECTED) throw new ThingsException("Not connected.", ThingsCodes.SMTPCLIENT_FAULT_NOT_STARTED, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_ACTUAL_STATE, state.name());
			
		// Do it.
		Reply reply = null; 
		try {
			sendCommand("EHLO " + hostname);
			reply = processLoginReplies(readReplies());
			if (myLogger.debuggingState()) debugReply("login", reply);
			
			// Is it a problem?  Only transition state if we got a ACTION_OK.
			if (reply.code == ACTION_OK) {
				state = SMTPState.LOGIN_COMPLETE;
			} else {
				if (myLogger.debuggingState()) post("login() not ACTION_OK so we are not LOGIN_COMPLETE.");
			}
			
		} catch (InterruptedException ie) {
			disconnect();
			state = SMTPState.BAD;
			throw ie;			
		} catch (Throwable t) {
			throw new ThingsException("Failed login.", ThingsCodes.SMTPCLIENT_ERROR_LOGIN_FAILED, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}
		
		return reply;	
	}
	
	/**
	 * Set the sender using MAIL FROM.  It must be at LOGIN_COMPLETE.
	 * @param senderText as a valid SMTP address.
	 * @return A reply.
	 * @throws ThingsException if not ready for MAIL FROM or actual transmission problem.
	 */
	public Reply 	sender(String senderText) throws ThingsException, InterruptedException  {		
		return sender(senderText, null);
	}

	/**
	 * Set the sender using MAIL FROM.  It must be at LOGIN_COMPLETE.
	 * @param senderText as a valid SMTP address.
	 * @param dsn command.  If null, it will be ignored.
	 * @return A reply.
	 * @throws ThingsException if not ready for MAIL FROM or actual transmission problem.
	 */
	public Reply 	sender(String senderText, DSN dsn) throws ThingsException, InterruptedException  {		
		// Qualify.  Must be connected.
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if (state != SMTPState.LOGIN_COMPLETE) throw new ThingsException("Bad SMTPState.  Not LOGIN_COMPLETE.", ThingsCodes.SMTPCLIENT_FAULT_NOT_STARTED, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_ACTUAL_STATE, state.name());
			
		// Do it.
		Reply reply = null; 
		try {
			if (dsn==null) {
				sendCommand("MAIL FROM:<" + senderText + ">");
			} else {
				if (!extensionDSN) throw new Exception("Server does not implement DSN extension.");
				sendCommand("MAIL FROM:<" + senderText + "> " + dsn.renderFrom());
			}

			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) debugReply("sender", reply);
			
			// Is it a problem? 
			if (reply.code < CODE_FAIL_THRESHOLD) {
				state = SMTPState.MAILFROM_DONE;
			} else {
				if (myLogger.debuggingState()) post("sender() not ok.");
			}
			
		} catch (InterruptedException ie) {
			disconnect();
			state = SMTPState.BAD;
			throw ie;		
		} catch (Throwable t) {
			throw new ThingsException("Set sender (MAIL FROM) failed.", ThingsCodes.SMTPCLIENT_ERROR_MAILFROM_FAILED, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}
		
		return reply;	
	}
	
	/**
	 * Set the recipient using RCPT TO.  It must be at MAILFROM_DONE.
	 * @param recipientText as a valid SMTP address.
	 * @return A reply.
	 * @throws ThingsException if not ready for  RCPT TO or actual transmission problem.
	 */
	public Reply	recipient(String recipientText) throws ThingsException, InterruptedException  {	
		return recipient(recipientText, null);
	}
	
	/**
	 * Set the recipient using RCPT TO.  It must be at MAILFROM_DONE.
	 * @param recipientText as a valid SMTP address.
	 * @param dsn command.  If null, it will be ignored.
	 * @return A reply.
	 * @throws ThingsException if not ready for  RCPT TO or actual transmission problem.
	 */
	public Reply	recipient(String recipientText, DSN dsn) throws ThingsException, InterruptedException  {	
		// Qualify.  Must be connected.
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if (state != SMTPState.MAILFROM_DONE) throw new ThingsException("Bad SMTPState.  Not MAILFROM_DONE.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_ACTUAL_STATE, state.name());
			
		// Do it.
		Reply reply = null; 
		try {
			
			if (dsn==null) {
				sendCommand("RCPT TO:<" + recipientText + ">");
			} else {
				if (!extensionDSN) throw new Exception("Server does not implement DSN extension.");
				sendCommand("RCPT TO:<" + recipientText + "> " + dsn.renderRecipient());
			}
			
			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) debugReply("recipient", reply);
			
			// Is it a problem? 
			if (reply.code < CODE_FAIL_THRESHOLD) {
				state = SMTPState.RCPTTO_DONE;
			} else {
				if (myLogger.debuggingState()) post("recipient() not ok.");
			}
			
		} catch (InterruptedException ie) {
			disconnect();
			state = SMTPState.BAD;
			throw ie;			
		} catch (Throwable t) {
			throw new ThingsException("Set sender (MAIL FROM) failed.", ThingsCodes.SMTPCLIENT_ERROR_RCPTTO_FAILED, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}	
		return reply;	
	}
	
	/**
	 * RESET the connection.  This will return the state to LOGIN_COMPLETE, as long as it has already been logged in.
	 * @return the reply.
	 * @throws ThingsException if the connection is made and logged in.
	 */
	public Reply	reset() throws ThingsException, InterruptedException  {
		
		// Qualify.  Must be connected.
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if ((state == SMTPState.FRESH)||(state == SMTPState.CONNECTED)) throw new ThingsException("Bad SMTPState.  Must be at least LOGIN_COMPLETE.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_ACTUAL_STATE, state.name());
		
		// Do it.
		Reply reply = null; 
		try {
			sendCommand("RSET");
			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) debugReply("reset", reply);
			
			// Is it a problem? 
			if (reply.code < CODE_FAIL_THRESHOLD) {
				state = SMTPState.LOGIN_COMPLETE;
			} else {
				// This will be bad.
				throw new Exception();
			}
			
		} catch (InterruptedException ie) {
			disconnect();
			state = SMTPState.BAD;
			throw ie;
		} catch (Throwable t) {
			// This is really bad.  If we can't reset then we're hosed--throw a FAULT
			if (reply != null) 
				throw new ThingsException("Could not reset (RSET)", ThingsCodes.SMTPCLIENT_FAULT_GENERAL, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(reply.code), ThingsNamespace.ATTR_MESSAGE, reply.text);
			else
				throw new ThingsException("Could not reset (RSET)", ThingsCodes.SMTPCLIENT_FAULT_GENERAL, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}	
		return reply;		
	}
	
	/**
	 * Send DATA.  It must be at RCPTTO_DONE.  It will do the appropriate dot doubling and CRLF management.
	 * @param ios Stream to send.  You handle any buffering.  Null entries are allowed and ignored.
	 * @return the reply
	 */
	public Reply sendData(InputStream... ios) throws ThingsException, InterruptedException {
		
		// Qualify.  Must be connected.
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if (state != SMTPState.RCPTTO_DONE) throw new ThingsException("Bad SMTPState. Not RCPTTO_DONE.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_ACTUAL_STATE, state.name());
		
		Reply reply = null; 
		
		// -- SEND SECTION --------------------------------------------------------------------------------------------	
		try {

			// Setup=
			sendCommand("DATA");
			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) debugReply("sendData", reply);

			// Will it allow us to send?
			if (reply.code != START_MAIL_INPUT) throw new Exception();  // PUNT		
			
			// Push the data
			int iostate = STATE_START;
			int character  = -1;
			lastSize = 0;
			
			// pipeline the streams.
			InputStream currentInputStream;
			for (int index = 0; index < ios.length; index++) {
				
				currentInputStream = ios[index];
				if (currentInputStream == null) continue;	// Allow and ignore nulls.
				
				character = currentInputStream.read();
				while (character >= 0) {
					lastSize++;	// A good char.  Count it.
					
					switch (character) {
					case ThingsConstants.LF:
						if (iostate==STATE_CR) iostate = STATE_LINE;
						break;
					case ThingsConstants.CR:
						iostate = STATE_LF;
						break;
					case '.':
						if (iostate==STATE_LINE) {
							iostate = STATE_START;
							bos.write('.'); // double it.
							lastSize++;	
						}
						break;
					default:
						iostate = STATE_START;
						break;
					}
					bos.write(character);
					character = currentInputStream.read();
				
				}	// end while this stream has goodies. 
			
			} // end while there are streams in the pipeline.		
			
			// Terminate it.
			bos.write(ThingsConstants.CR);
			bos.write(ThingsConstants.LF);
			bos.write('.');
			bos.write(ThingsConstants.CR);
			bos.write(ThingsConstants.LF);
			lastSize += 5;
			bos.flush();	
			if (myLogger!=null) myLogger.post("sendData stream complete.  Waiting for reply.");
		
		} catch (InterruptedIOException iioe) {
			if (myLogger.debuggingState()) myLogger.post("sendData interrupted by IO timeout.");
			// Do not disconnect.  Connect is ok.
			throw new ThingsException("Send data (DATA) interrupted by timeout.", ThingsCodes.SMTPCLIENT_ERROR_SEND_TIMEOUT, iioe, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_TRANSPORT_SENT_SIZE_ACTUAL, Integer.toString(iioe.bytesTransferred));			
			
		} catch (InterruptedException ie) {	
			if (myLogger.debuggingState()) myLogger.post("sendData interrupted during transmission.");
			disconnect();
			state = SMTPState.BAD;
			throw ie;

		} catch (Throwable t) {
			if (myLogger.debuggingState()) myLogger.post("sendData exception during transmission.  message=" + t.getMessage());
			disconnect();
			state = SMTPState.BAD;
			
			// This is really bad.  If we can't reset then we're hosed--throw a FAULT
			if (reply != null) 
				throw new ThingsException("Could not send data (DATA).  Fail in transmission.", ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(reply.code), ThingsNamespace.ATTR_MESSAGE, reply.text);
			else
				throw new ThingsException("Could not send data (DATA).  Fail in transmission.", ThingsCodes.SMTPCLIENT_FAULT_GENERAL, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}			
		
		// -- REPLY SECTION --------------------------------------------------------------------------------------------			
		try {
			
			// Get reply
			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) myLogger.post("sendData complete (DATA) REPLY: " + reply.text);
			reply.size = lastSize;
			
		} catch (InterruptedIOException iioe) {
			if (myLogger.debuggingState()) myLogger.post("sendData interrupted by IO timeout.");	
			// Do not disconnect.  Connect is ok.
			throw new ThingsException("Send data (DATA) interrupted by timeout while waiting for a response.", ThingsCodes.SMTPCLIENT_ERROR_SEND_TIMEOUT, iioe, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_TRANSPORT_SENT_SIZE_ACTUAL, Integer.toString(iioe.bytesTransferred));
			
		} catch (InterruptedException ie) {
			if (myLogger.debuggingState()) myLogger.post("sendData interrupted while waiting for a respone.");
			disconnect();
			state = SMTPState.BAD;
			throw ie;

		} catch (Throwable t) {	
			if (myLogger.debuggingState()) myLogger.post("sendData exception while waiting for a response.  message=" + t.getMessage());
			disconnect();
			state = SMTPState.BAD;
			
			// This is really bad.  If we can't reset then we're hosed--throw a FAULT
			if (reply != null) 
				throw new ThingsException("Could not send data (DATA)", ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(reply.code), ThingsNamespace.ATTR_MESSAGE, reply.text);
			else
				throw new ThingsException("Could not send data (DATA)", ThingsCodes.SMTPCLIENT_FAULT_GENERAL, t, ThingsNamespace.ATTR_TRANSPORT_CLIENT_NAME, clientName);
		}	
		return reply;	
	}
	
	/**
	 * Quit the client.  This will return it to FRESH.  It will never throw an exception, but may return a bad reply.
	 * @return a reply
	 */
	public Reply done() throws ThingsException, InterruptedException  {
		
		// Qualify
		if (state == SMTPState.BAD) throw new ThingsException("This client is now BAD.", ThingsCodes.SMTPCLIENT_FAULT_BAD_STATE);
		if (state == SMTPState.FRESH) return new Reply(ACTION_OK, "Done.");
		
		// Do it.
		lastSize = 0;
		Reply reply = null; 
		try {
			
			sendCommand("QUIT");
			reply = getOnlyOne(readReplies());
			if (myLogger.debuggingState()) debugReply("done", reply);
			
			// Close them.  Don't worry about errors.  We'll deference everything in the end.
			try {
				bos.close();
			} catch (Throwable t) {
				// Don't care;
			}
			try {
				bis.close();
			} catch (Throwable t) {
				// Don't care;
			}
			try {
				connection.close();
			} catch (Throwable t) {
				// Don't care;
			}
			bis = null;
			bos = null;
			connection = null;
			
		} catch (InterruptedException ie) {
			connection = null;
			state = SMTPState.BAD;
			throw ie;
		} catch (Throwable t) {
			reply = new Reply(TRANSACTION_FAILED, t.getMessage());
		}
		reply.size = lastSize;
		return reply;
	}
	
	/**
	 * Is a named extension present?
	 * @param extensionName
	 * @return true if the named extension is present.
	 */
	public boolean extensionPresent(String extensionName) {
		boolean result = false;
		if (extensionName!=null) {
			if (extensionName.equalsIgnoreCase(EXTENSION_DSN)) result = extensionDSN;
		}
		return result;
	}
	
	// ============================================================================================================================================
	// === INTERNAL ENGINE

	/**
	 * Post a reply debug message.
	 * @param oper the operation name.
	 * @param r the reply.
	 */
	private void debugReply(String oper, Reply r) {
		myLogger.postit("REPLY: operation=" + oper + " text=" + r.text + " code=" + r.code + " state=" + getState().name());
	}
	
	/**
	 * Send a command string.  It will be encoded as US-ASCII, so use it for SMTP commands only!
	 * @param theString
	 * @throws Exception
	 */
	private void sendCommand(String theString) throws Exception {
		byte[] data = theString.getBytes("US-ASCII");
		bos.write(data);
		bos.write(ThingsConstants.CR);
		bos.write(ThingsConstants.LF);	
		bos.flush();
		lastSize = data.length + 2;
	}

	/**
	 * Get the login replies.  It'll set if certain features are available.
	 * @param replies
	 * @return
	 * @throws Throwable
	 */
	private Reply processLoginReplies(List<Reply> replies) throws Throwable {
		Reply result;
		if (replies.size()>0) {
			Iterator<Reply> replyIterator = replies.listIterator();
			result = replyIterator.next();
			Reply current;
			while(replyIterator.hasNext()) {
				current = replyIterator.next();
				if (current.text.equalsIgnoreCase(EXTENSION_DSN)) {
					this.extensionDSN = true;
				}
			}
			
		} else {
			result = new Reply();
			result.broken=true;
			result.text="There was no reply.";
		}
		return result;
	}
	
	
	// State engine.
	private final int STATE_START = 0;
	private final int STATE_CODE1 = 1;
	private final int STATE_CODE2 = 2;
	private final int STATE_INTERSPACE = 3;
	private final int STATE_LINE = 4;
	private final int STATE_CR = 5;
	private final int STATE_LF = 6;
	
	/**
	 * Read everything it can into replies.
	 * This is SUPPOSED to be 7-bit ASCII!!!!
	 */
	private List<Reply> readReplies() throws Throwable {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(READ_FULLY_LIMIT);
		List<Reply> replies = new LinkedList<Reply>();
		
		int bufooneries = 0;
		boolean multiline = false;
		int state = STATE_START;
		int item;
		Reply currentReply = new Reply();
		try {
			item = bis.read();
		} catch (InterruptedIOException iioe) {
			item = retryRead(bis, iioe);
		}
		while (item >= 0) {
			baos.write(item);		// Add it.
			switch (state) {
			case STATE_START:
				if ((item >= '0')&&(item <='9')) {
					currentReply.code = ((item-'0')*100);  // The code's hundreds place
					state = STATE_CODE1;
				} else {
					// No code char
					currentReply.broken = true;
					state = STATE_LINE;
				}
				break;
				
			case STATE_CODE1:
				if ((item >= '0')&&(item <='9')) {
					currentReply.code += ((item-'0')*10);  // The code's tens place
					state = STATE_CODE2;
				} else {
					// No code char
					currentReply.broken = true;
					state = STATE_LINE;
				}
				break;
				
			case STATE_CODE2:
				if ((item >= '0')&&(item <='9')) {
					currentReply.code += (item-'0');  // The code's ones place
					state = STATE_INTERSPACE;
				} else {
					// No code char
					currentReply.broken = true;
					state = STATE_LINE;
				}
				break;
				
			case STATE_INTERSPACE:
				if (item == '-') {
					// Multiline
					multiline = true;
					state = STATE_LINE;
				} else if (item == ' ') {
					state = STATE_LINE;			
				} else {
					// Broken;
					currentReply.broken = true;
					state = STATE_LINE;			
				}
				break;
				
			case STATE_LINE:
				// CR ??
				if (item == ThingsConstants.CR) {
					state = STATE_CR;
				}
				break;
				
			case STATE_CR:	
				// LF ??
				if (item == ThingsConstants.LF) {
					currentReply.text = baos.toString("US-ASCII").trim().substring(4);
					replies.add(currentReply);
					if (multiline) {
						// Good line.  Get the next.
						multiline = false;
						currentReply = new Reply();
						baos = new ByteArrayOutputStream(READ_FULLY_LIMIT);
						state = STATE_START;
						
					} else {
						// HAPPY!!!
						return replies;
					}
				
				// Another stupid CR?
				} else if (item == ThingsConstants.CR) {
					// Bufoonery protection.  Let's give it a couple CR's before the LF to see if it's just screwing around.
					bufooneries++;
					if (bufooneries > BUMP_LIMIT) throw new Exception("Reply thrashing on CR SMTPState.  Giving up.  The SMTP server is mimsbehaving.");			
		
				// Something else?
				} else {
					if (multiline) {
						// I guess they don't like LFs.  Allow it.  We'll need to pretend it's STATE_START and we've finished the last night.
						currentReply.text = baos.toString("US-ASCII").trim().substring(4);
						replies.add(currentReply);
						currentReply = new Reply();
						baos = new ByteArrayOutputStream(READ_FULLY_LIMIT);
						multiline = false;
						if ((item >= '0')&&(item <='9')) {
							currentReply.code = ((item-'0')*100);  // The code's hundreds place
							state = STATE_CODE1;
						} else {
							// Don't know that the hell it's doing now.  Let it deplete with the line, unless we hit the bufoonery limit.
							bufooneries++;
							if (bufooneries > BUMP_LIMIT) throw new Exception("Reply thrashing on multiline.  Giving up.  The SMTP server is mimsbehaving.");			
							currentReply.broken = true;
							state = STATE_LINE;
						}
						break;
	
					} else {
						 throw new Exception("Bad reply from server.  It only sent a CR, instead of CR/LF.  No cookie for you.");			
					}	

				}
				break;

			} // end state switch
			try {
				item = bis.read();
			} catch (InterruptedIOException iioe) {
				item = retryRead(bis, iioe);
			}
		}
		
		// This is bad.  The pipe is dead.
		try {
			connection.close();
		} catch (Throwable t) {
			// abandon ship
		}
		connection = null;
		throw new IOException("The connection closed during read.");		
		
	}
	
	/**
	 * Check to see if we got a single reply.  If we got more than one, it is a problem.
	 * @param replies
	 * @return the single good reply or new ABORT reply that will render all the replies.
	 * @throws Throwable
	 */
	private Reply getOnlyOne(List<Reply> replies) throws Throwable  {
		Reply result;
		if (replies.size()==1) {
			result = replies.get(0);
		} else {
			// Render all replies
			StringBuffer render = new StringBuffer();
			render.append("Expecting single reply, but got multiple."); render.append("\r\n");
			for (Reply item : replies) {
				render.append(item.code); render.append(":"); render.append(item.text); render.append("\r\n");
			}
			
			// Create a single ABORT reply.
			result = new Reply();  // leave the code as ABORT
			result.broken = true;
			result.text = render.toString();
		}
		return result;
	}

	/**
	 * Retry the read (or not) depending on the configuration.
	 * @param is The stream to read.
	 * @param instigator the instigating exception.
	 * @return the data from the is.read().
	 * @throws Throwable For any problem.  A InterruptedIOException is possible if the retries are expended.
	 */
	private int retryRead(InputStream is, InterruptedIOException instigator) throws Throwable {
		
		// Trivial check, do we even retry?
		if (this.retries <= 0) throw instigator;
		
		// DO the retries
		int tally = instigator.bytesTransferred;
		String message = instigator.getMessage();		
		for (int iteration = 0 ; iteration < retries ; iteration++) {
			try {
				return is.read();
			} catch (InterruptedIOException iioe) {
				tally += iioe.bytesTransferred;
				message = iioe.getMessage();
			}
		}
		
		// Fail
		InterruptedIOException finalException = new InterruptedIOException(message);
		finalException.bytesTransferred = tally;
		throw finalException;
		
	}
	
	/**
	 * Abandon any connection and return to the FRESH SMTPState.
	 * This will never throw any exceptions.  It will attempt to close and dispose of bis, bos, and the connection itself.
	 */
	private void abandonConnection() {
		if (bis!=null) {
			try {
				bis.close();
			} catch (Throwable t) {
				// Don't care.
			}
			bis = null;
		}
		if (bos!=null) {
			try {
				bos.close();
			} catch (Throwable t) {
				// Don't care.
			}
			bos = null;
		}
		if (connection!=null) {
			try {
				connection.close();
			} catch (Throwable t) {
				// Don't care.
			}
			connection = null;
		}
		state = SMTPState.FRESH;		
	}
	
	// ============================================================================================================================================
	// ============================================================================================================================================
	// == DEBUGGABLE INTERFACE
	
	/**
	 * Turn debugging on.  Logs with debug level priority will be passed.
	 */
	public void debuggingOn() {
		myLogger.debuggingOn();
	}
	
	/**
	 * Turn debugging off.  Logs with debug level priority will not be passed.
	 */
	public void debuggingOff() {
		myLogger.debuggingOff();
	}
	
	/**
	 * Get the current debugging SMTPSMTPState.
	 * @return debugging state
	 */
	public boolean debuggingState() {
		return myLogger.debuggingState();
	}

	/**
	 * Post as a message.
	 * @param message String to post
	 */
	public void post(String message) throws ThingsException{
		// NOP
	}
	
	/**
	 * Post as a message.  Best effort.  Ignore errors.
	 * @param message String to post
	 */
	public void postit(String message) {
		// NOP
	}
	
	/**
	 * Try to flush.  Never error no matter what.
	 */
	public void flush() {
		// NOP
	}

}