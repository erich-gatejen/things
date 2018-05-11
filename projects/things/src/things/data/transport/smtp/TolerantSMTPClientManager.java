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

import java.io.InputStream;

import things.common.PuntException;
import things.common.PuntRetryableException;
import things.common.ThingsCodes;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.tools.StopWatch;
import things.data.Data;
import things.thing.Metrics;
import things.thing.RESULT;
import things.thing.UserException;
import things.thinger.io.Logger;

/**
 * This is a tolerant manager for a clean SMTP client.  It will retry operations.<p>
 * The default socket timeout will be 7 minutes.  You can increase it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 23 NOV 06
 * </pre> 
 */
public class TolerantSMTPClientManager implements SMTPClientManager {

	// == EXPOSED SETTINGS ===============================================================================
	public final static int CONNECTION_TIMEOUT = 1000 * 60 * 7; // 7 minutes
	
	/**
	 * This is the default number of messages sent per stream.  After this number, the connection is always closed and reestablished.  This was
	 * done to avoid platform problems associated with seriously aged sockets.  Yes, they really exist and it they aren't my bugs. :&)
	 */
	public final static int DEFAULT_STREAM_MAX = 1000;
	
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	// Internal data
	private SMTPClient myClient;
	private String mailtarget;
	private int port;
	private boolean streaming;
	private Logger myLogger;
	private int streamMax;
	private int streamSent;
	private boolean extensions = false;
	private DSN dsn;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == Methods
	
	/**
	 * Init the system.  This is a one-time deal.  The managers are one use only.  By default, no extensions will be supported.
	 * @param aLogger
	 * @param name
	 * @param targetAddress
	 * @param targetPort
	 * @param isStreaming does it stream?  (Streaming attempts to reuse the connection).
	 * @param extensions if true, server extensions will be supported.
	 * @throws UserException
	 */
	public synchronized void init(Logger aLogger, String name, String targetAddress, int targetPort, boolean isStreaming, boolean extensions) throws ThingsException {
	
		// Qualify
		if (myClient != null) UserException.softwareProblem("TolerantSMTPClientManager.init() called more than once");
		if (aLogger == null) throw new UserException("aLogger cannot be null", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "aLogger");
		if (name == null) throw new UserException("name cannot be null", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "name");
		if (targetAddress == null) throw new UserException("targetAddress cannot be targetAddress", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "targetAddress");
		if ((targetPort < 1)||(targetPort>65535)) throw new UserException("targetPort not a valid point", UserException.MODULE_FAULT_BAD_USAGE, ThingsNamespace.ATTR_PARAMETER_NAME, "targetPort", ThingsNamespace.ATTR_PARAMETER_VALUE, Integer.toString(targetPort));
		
		// Set
		mailtarget = targetAddress;
		port = targetPort;
		streaming = isStreaming;
		myLogger = aLogger;
		this.extensions =  extensions;
		
		if (extensions) {
			myClient = new FancySMTPClient(aLogger,name);	
		} else {
			myClient = new CleanSMTPClient(aLogger,name);
		}
		myClient.setTimeout(CONNECTION_TIMEOUT);	
	}
	
	/**
	 * Set the maximum number of messages a stream can send before it is closed and reestablished.
	 * @param max the maximum number.  It must be 1 or greater or you'll get an exception.  
	 * @throws ThingsException
	 * @throws InterruptedException
	 */
	public void setStreamMax(int max) throws ThingsException, InterruptedException {
		if (max == 0) ThingsException.softwareProblem("setStreamMax() must be called with a positive integer.");
		streamMax = max;
	}
	
	/**
	 * Set a new timeout.  It'll take effect after the next connection is made (which might not be obvious).
	 * @param timeout in milliseconds.
	 * @throws ThingsException
	 */
	public void setTimeout(int timeout) throws ThingsException {
		if (timeout < 0) UserException.softwareProblem("Cannot set the timeout as a negative number");
		if (myClient == null) UserException.softwareProblem("TolerantSMTPClientManager.init() must be called before any other methods.");
		myClient.setTimeout(timeout);
	}
	
	/**
	 * Set a DSN for all subsequent sends.  If set to null, it will not be used.  Extensions must be supported for this to work.
	 * @param dsn the dsn
	 */
	public void setDSN(DSN dsn) {
		this.dsn = dsn;
	}
	
	/**
	 * Are extensions supported?
	 * @return true if they are
	 */
	public boolean isExtensionsSet() {
		return extensions;
	}
	
	/**
	 * Submit a message.  We'll let InterruptedExceptions out like a good THINGS citizen.
	 * @param senderInet
	 * @param recipientInet
	 * @param dataStream InputStreams to be sent in turn.  Null entries are allowed and ignored.
	 * @return the SMTP reply for the send.  The size field is useful.
	 * @throws ThingsException if not successful for any reason.  (Don't change this without modifying the other submit methods.)
	 */
	public Reply submit(String senderInet, String recipientInet, InputStream... dataStream) throws ThingsException, InterruptedException {
		Reply currentReply  = null;
		
		// Qualify
		if (senderInet == null) throw new UserException("senderInet cannot be null", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "senderInet");
		if (recipientInet == null) throw new UserException("recipientInet cannot be null", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "recipientInet");
		if (dataStream == null) throw new UserException("dataStream cannot be null", UserException.MODULE_FAULT_NULL_PARAMETER, ThingsNamespace.ATTR_PARAMETER_NAME, "dataStream");
		
		try {
		
			// If we are FRESH, go through the login process.
			if (myClient.getState() == SMTPState.FRESH) {
				connect();
			}
			
			// Loop through.  Note that if we have a problem, we need to restart the connection and do each step again.
			for (int tryIndex = ThingsConstants.DEFAULT_TRANSPORT_CONNECT_RETRIES; tryIndex > 0 ; tryIndex--) {
			
				try	{
					
					// Sender
					if (myClient.getState() == SMTPState.LOGIN_COMPLETE) {
						currentReply = myClient.sender(senderInet, dsn);	
						if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) throw new PuntException("Sender");		
					} else {
						throw new PuntException("Sender-BAD_STATE");
					}
		
					// Sender
					if (myClient.getState() == SMTPState.MAILFROM_DONE) {
						currentReply = myClient.recipient(recipientInet, dsn);	
						if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) throw new PuntException("Recipient");		
					} else {
						throw new PuntException("Recipient-BAD_STATE");
					}
				
					// Send data.  Only ONE retry from this point.
					tryIndex = 1;
					if (myClient.getState() == SMTPState.RCPTTO_DONE) {
						currentReply = myClient.sendData(dataStream);	
						if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) throw new PuntRetryableException("Error code response.");  // Allow these to try again.		
						// HERE WE PASSED!  Break out of the for.
						break;
						
					} else {
						throw new PuntException("SendData-BAD_STATE");
					}
					
				} catch (PuntRetryableException pe) {		
					
					// The server didn't like it.
					// Gah.  Kluge.  Only reconnect if we think we'll iterate.  It'll re-establish even if it was just a timeout.
					if (tryIndex > 1) {
						// These we'll try again.  Wait a little bit and rebuild connection.
						Thread.sleep(ThingsConstants.DEFAULT_TRANSPORT_CONNECT_RETRY_DELAY);
						
						// Try to re-establish
						myClient.disconnect();
						connect();
						
						if (myLogger.debuggingState()) myLogger.debug("Server rejected message.  Trying again.", ThingsCodes.DEBUG_SMTP_RETRIABLE_ERROR, ThingsNamespace.ATTR_STATE, pe.getMessage(), ThingsNamespace.ATTR_TRANSPORT_CODE, "", ThingsNamespace.ATTR_MESSAGE, "");
						
					} else {
						break;
					}

					// These we just give up.  Since these usually mean configuration problems, let's fault.
					if (currentReply==null)
						throw new UserException("Failed SMTP operation after retries.", ThingsCodes.SMTPCLIENT_FAULT_CANNOT_COMPLETE, ThingsNamespace.ATTR_STATE, pe.getMessage(), ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(CleanSMTPClient.TRANSACTION_FAILED), ThingsNamespace.ATTR_MESSAGE, ThingsConstants.EPIC_FAIL);
					else
						throw new UserException("Failed SMTP operation after retries.", ThingsCodes.SMTPCLIENT_FAULT_CANNOT_COMPLETE, ThingsNamespace.ATTR_STATE, pe.getMessage(), ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(currentReply.code), ThingsNamespace.ATTR_MESSAGE, currentReply.text);

					
				} catch (PuntException pe) {
					// These we just give up.  Since these usually mean configuration problems, let's fault.
					if (currentReply==null)
						throw new UserException("Failed SMTP operation (no longer retryable).", ThingsCodes.SMTPCLIENT_FAULT_CANNOT_COMPLETE_NOT_RETRYABLE, ThingsNamespace.ATTR_STATE, pe.getMessage(), ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(CleanSMTPClient.TRANSACTION_FAILED), ThingsNamespace.ATTR_MESSAGE, ThingsConstants.EPIC_FAIL);
					else
						throw new UserException("Failed SMTP operation (no longer retryable).", ThingsCodes.SMTPCLIENT_FAULT_CANNOT_COMPLETE_NOT_RETRYABLE, ThingsNamespace.ATTR_STATE, pe.getMessage(), ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(currentReply.code), ThingsNamespace.ATTR_MESSAGE, currentReply.text);

					
				} catch (InterruptedException ie ) {
					if (myLogger.debuggingState()) myLogger.info("Current send interrupted by system and will be lost.");
					throw ie;
					
				} catch (Throwable  t) {
					
					// SPURIOUS AND NOT RECOVERABLE.
					if (currentReply==null)
						throw new UserException("SPURIOUS EXCEPTION in SMTP operation (not retryable).", ThingsCodes.SMTPCLIENT_FAULT_SPURIOUS, t, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(CleanSMTPClient.TRANSACTION_FAILED), ThingsNamespace.ATTR_MESSAGE, ThingsConstants.EPIC_FAIL);
					else
						throw new UserException("SPURIOUS EXCEPTION in SMTP operation (not retryable).", ThingsCodes.SMTPCLIENT_FAULT_SPURIOUS, t, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(currentReply.code), ThingsNamespace.ATTR_MESSAGE, currentReply.text);
				} 
				
			} // For retry loop
			
			// Did we send it?
			if (currentReply==null) 
				throw new UserException("Failed to send after retries.", ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(CleanSMTPClient.TRANSACTION_FAILED), ThingsNamespace.ATTR_MESSAGE, ThingsConstants.EPIC_FAIL);

			// HERE WE PASSED !!!
			
		} catch (ThingsException ue) {
			throw ue;
			
		} catch (InterruptedException ie) {
			myLogger.info("Client aborting because it was interrupted.  The last transaction was truncated.", ThingsCodes.SMTPCLIENT_INTERRUPTED_AND_QUITTING);
			throw ie;
			
		} catch (Throwable t) {
			// Forget it.  This is fatal.
			throw new UserException("Client FAULT to spurious exception.  Giving up.", ThingsCodes.SMTPCLIENT_FAULT_SPURIOUS, t);
			
		} finally {
			
			// Count as a send, even if it failed.
			streamSent++;
			
			// If we are not streaming or hit max streaming, drop the connection.
			if ((streaming == false)||(streamSent<=streamMax)) {
				try {
					myClient.disconnect();
				} catch (Throwable t) {
					// Consume these.
					if (myLogger.debuggingState()) myLogger.debug("Exception while disconnecting.", ThingsCodes.SMTPCLIENT_BENIGN_DISCONNECT_ERROR, ThingsNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
				}
				
			} else {
				
				// Attempt a reset if alive.  if it fails, just disconnect.
				try {
					if (myClient.getState().isLive()) {
						myClient.reset();
					} else {
						myClient.disconnect();
					}
				} catch (Throwable t) {
					try {
						myClient.disconnect();
					} catch (Throwable tt) {
						// Consume these.
						if (myLogger.debuggingState()) myLogger.debug("Exception while disconnecting after RSET error.", ThingsCodes.SMTPCLIENT_BENIGN_DISCONNECT_ERROR, ThingsNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
					}
				}
				
			} // end if streaming	
			
		} // end try
		
		return currentReply;
	}
	
	/**
	 * Submit a message and return the result.  We'll let InterruptedExceptions out like a good THINGS citizen.
	 * It will not let any other exception escape, but will reflect them in the RESULT.  (It will log complex info if debug is enabled.)  
	 * <p>
	 * A FAULT will result in a Type.EXCEPTION.  An ERROR will result in a Type.FAIL.  A successful transaction will result in a Type.PASS.
	 * <p>
	 * The metrics will contain a valid size giving the actual byte sent in the difference field, as well as count values.
	 * <p>
	 * @param senderInet
	 * @param recipientInet
	 * @param dataStream InputStreams to send in turn.
	 * @param id the id of the operations.
	 * @throws UserException for any really serious stuff.  Most problems will be returned in the RESULT.
	 */
	public RESULT meassuredSubmit(String senderInet, String recipientInet, String  id, InputStream... dataStream) throws UserException, InterruptedException {

		RESULT result = null;
		Reply currentReply  = null;
		StopWatch watch = new StopWatch();
		
		// Prepare for the result. 
		Metrics metrics = new Metrics();
		metrics.count = 1;
		metrics.cases = 1;
		
		try {
			
			watch.start();
			currentReply = submit(senderInet, recipientInet, dataStream);
			
			// Factor result.
			metrics.millis = watch.time();
			metrics.difference = currentReply.size;
			if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) {
				metrics.fail = 1;
				result = new RESULT(ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, metrics, Data.Type.FAIL, ThingsNamespace.ATTR_ID, id, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(currentReply.code), ThingsNamespace.ATTR_MESSAGE, currentReply.text);
			
			} else {
				metrics.pass = 1;
				result = new RESULT(ThingsCodes.SMTPCLIENT_SEND_OK, metrics, Data.Type.PASS, ThingsNamespace.ATTR_ID, id, ThingsNamespace.ATTR_TRANSPORT_CODE, Integer.toString(currentReply.code));
			}
			
			
		} catch (ThingsException ue) {

			metrics.millis = watch.time();
			metrics.exception = 1;
			
			try {
			
				ue.addAttribute(ThingsNamespace.ATTR_PLATFORM_MESSAGE, ue.getMessage());	
				if (ue.isWorseThanError()) {
					result = new RESULT(ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, metrics, Data.Type.EXCEPTION, ue.getAttributesReader());
					
				} else {
					result = new RESULT(ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, metrics, Data.Type.FAIL, ue.getAttributesReader());
				}
			
			} catch (ThingsException ttee) {
				UserException.softwareProblem("Couldn't create ThingsException RESULT.", ttee);
			}
				
		} catch (InterruptedException ie) {
			throw ie;
			
		} catch (Throwable t) {
			
			metrics.millis = watch.time();
			metrics.exception = 1;
			
			try {
				result = new RESULT(ThingsCodes.SMTPCLIENT_ERROR_SEND_FAILED, metrics, Data.Type.EXCEPTION, ThingsNamespace.ATTR_MESSAGE, t.getMessage());
			} catch (ThingsException ttee) {
				UserException.softwareProblem("Couldn't create Throwable RESULT.", t);
			}
				
		} finally {
			

		} // end try
		
		// Done.
		return result;
	}

	/**
	 * Force a disconnection.  It is nice, but not necessary, to call this when you are done--or just done for a while.  It doesn't ruin
	 * the client.  You could still use it again (but remember, init is only once!).
	 * @throws ThingsException
	 * @throws InterruptedException
	 */
	public void disconnect()  throws ThingsException, InterruptedException {
		try {
			myClient.reset();
		} catch (Throwable t) {
			// Don't care
		}
		try {
			myClient.disconnect();
		} catch (Throwable t) {
			// Don't care
		}
	}
	
	
	// =========================================================================================
	// Helpers
	
	/**
	 * Connect helper.
	 */
	private void connect() throws ThingsException, InterruptedException {
		Reply currentReply = null;
		for (int tryIndex = 0; tryIndex < ThingsConstants.DEFAULT_TRANSPORT_CONNECT_RETRIES ; tryIndex++) {
		
			try	{
	
				// Need to connect?
				if (myClient.getState() != SMTPState.CONNECTED) {
					currentReply = myClient.connect(mailtarget, port);	
					if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) throw new Exception();
				}
				
				// Need to login?
				if (myClient.getState() != SMTPState.LOGIN_COMPLETE) {
					currentReply = myClient.login("SendList");	
					if (currentReply.code >= CleanSMTPClient.CODE_FAIL_THRESHOLD) throw new Exception();
				}
				
				// HAPPY!
				return;
				
			} catch (Throwable t) {
				// We'll just go around again.
				Thread.sleep(ThingsConstants.DEFAULT_TRANSPORT_CONNECT_RETRY_DELAY);
			}
		
		} // For retry loop
		
		// Really bad.
		throw new UserException("Failed to connect after retries.", ThingsCodes.SMTPCLIENT_FAULT_CANNOT_CONNECT, ThingsNamespace.ATTR_RETRIES, Integer.toString(ThingsConstants.DEFAULT_TRANSPORT_CONNECT_RETRIES));
	}
	
}
