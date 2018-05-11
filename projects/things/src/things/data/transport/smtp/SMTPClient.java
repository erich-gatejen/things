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

import things.common.ThingsException;

/**
 * General interface to the SMTP clients.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 23 NOV 06
 * </pre> 
 */
public interface SMTPClient  {

	// == EXPOSED SETTINGS ===============================================================================
	public final static int CONNECTION_TIMEOUT = 1000 * 60 * 3; // 3 minutes
	public final static int CONNECTION_RETRIES = 0;  // NONE
	
	/**
	 * SMTP result codes.  These will come directly from the server.
	 */
    public static final int CODE_211 = 211;
    public static final int CODE_214 = 214;
    public static final int CODE_215 = 215;
    public static final int CODE_220 = 220;
    public static final int CODE_221 = 221;
    public static final int CODE_250 = 250;
    public static final int CODE_251 = 251;
    public static final int CODE_354 = 354;
    // Failures
    public static final int CODE_421 = 421;
    public static final int CODE_FAIL_THRESHOLD = CODE_421;
    public static final int CODE_450 = 450;
    public static final int CODE_451 = 451;
    public static final int CODE_452 = 452;
    public static final int CODE_500 = 500;
    public static final int CODE_501 = 501;
    public static final int CODE_502 = 502;
    public static final int CODE_503 = 503;
    public static final int CODE_504 = 504;
    public static final int CODE_550 = 550;
    public static final int CODE_551 = 551;
    public static final int CODE_552 = 552;
    public static final int CODE_553 = 553;
    public static final int CODE_554 = 554;
    public static final int SYSTEM_STATUS = CODE_211;
    public static final int HELP_MESSAGE = CODE_214;
    public static final int SERVICE_READY = CODE_220;
    public static final int SERVICE_CLOSING_TRANSMISSION_CHANNEL = CODE_221;
    public static final int ACTION_OK = CODE_250;
    public static final int USER_NOT_LOCAL_WILL_FORWARD = CODE_251;
    public static final int START_MAIL_INPUT = CODE_354;
    public static final int SERVICE_NOT_AVAILABLE = CODE_421;
    public static final int ACTION_NOT_TAKEN = CODE_450;
    public static final int ACTION_ABORTED = CODE_451;
    public static final int INSUFFICIENT_STORAGE = CODE_452;
    public static final int UNRECOGNIZED_COMMAND = CODE_500;
    public static final int SYNTAX_ERROR_IN_ARGUMENTS = CODE_501;
    public static final int COMMAND_NOT_IMPLEMENTED = CODE_502;
    public static final int BAD_COMMAND_SEQUENCE = CODE_503;
    public static final int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = CODE_504;
    public static final int MAILBOX_UNAVAILABLE = CODE_550;
    public static final int USER_NOT_LOCAL = CODE_551;
    public static final int STORAGE_ALLOCATION_EXCEEDED = CODE_552;
    public static final int MAILBOX_NAME_NOT_ALLOWED = CODE_553;
    public static final int TRANSACTION_FAILED = CODE_554;
	
	// ===================================================================================================
	// == METHODS  ==================================
	
	/**
	 * Get client name.
	 * @return the clients name.
	 */
	public String getName();
	
	/**
	 * Get the state.
	 * @return State
	 */
	public SMTPState getState();
	
	/**
	 * Set the socket timeout for each connection.  A timeout may be retried, depending on the value set with setSocketRetries(int).<br>
	 * The default value is CONNECTION_TIMEOUT.
	 * <p>
	 * It will take effect whenever a new connection is established.
	 * @param timeout the timeout in milliseconds.
	 */
	public void setTimeout(int timeout);
	
	/**
	 * Number of times a socket read will be retried after a timeout before completely giving up.<br>
	 * The default value is CONNECTION_RETRIES.
	 * <p>
	 * It will take effect whenever a new operation is started.
	 * @param retries Number of retries.  Zero or a negative number is the same as no retries.
	 */
	public void setSocketRetries(int retries);
	
	/**
	 * Start a connection.  Return a code.  It will throw an ERROR is already connect, but will leave the connection intact.
	 * @param connectAddress 
	 * @param connectPort
	 * @return ACTION_OK if it was ok.
	 * @throws ThingsException will always be fatal at this point.
	 */
	public Reply connect(String connectAddress, int connectPort) throws ThingsException, InterruptedException;
	/**
	 * Start a connection.  Return a code.
	 * @return The reply.
	 * @throws ThingsException which will never happen, since we'll just abandon the bad connection.
	 */
	public Reply	disconnect() throws ThingsException, InterruptedException ;
	
	/**
	 * HELO with the hostname--no authentication.  A state problem will cause a FAULT.  A transmission problem is just an ERROR.  It will transition to LOGIN_COMPLETE only
	 * of the reply is an ACTION_OK.
	 * @param hostname
	 * @return the reply.
	 */
	public Reply login(String hostname) throws ThingsException, InterruptedException;
	
	/**
	 * Set the sender using MAIL FROM.  It must be at LOGIN_COMPLETE.
	 * @param senderText as a valid SMTP address.
	 * @return A reply.
	 * @throws ThingsException if not ready for MAIL FROM or actual transmission problem.
	 */
	public Reply sender(String senderText) throws ThingsException, InterruptedException;
	
	/**
	 * Set the sender using MAIL FROM.  It must be at LOGIN_COMPLETE.  Implementation of the DSN is up to the client.  It may choose to ignore it.
	 * @param senderText as a valid SMTP address.
	 * @param dsn command.  If null, it will be ignored.
	 * @return A reply.
	 * @throws ThingsException if not ready for MAIL FROM or actual transmission problem.
	 */
	public Reply sender(String senderText, DSN dsn) throws ThingsException, InterruptedException;	
	
	/**
	 * Set the recipient using RCPT TO.  It must be at MAILFROM_DONE.
	 * @param recipientText as a valid SMTP address.
	 * @return A reply.
	 * @throws ThingsException if not ready for  RCPT TO or actual transmission problem.
	 */
	public Reply recipient(String recipientText) throws ThingsException, InterruptedException;
	
	/**
	 * Set the recipient using RCPT TO.  It must be at MAILFROM_DONE.  Implementation of the DSN is up to the client.  It may choose to ignore it.
	 * @param recipientText as a valid SMTP address.
	 * @param dsn command.  If null, it will be ignored.
	 * @return A reply.
	 * @throws ThingsException if not ready for  RCPT TO or actual transmission problem.
	 */
	public Reply	recipient(String recipientText, DSN dsn) throws ThingsException, InterruptedException;
	
	/**
	 * RESET the connection.  This will return the state to LOGIN_COMPLETE, as long as it has already been logged in.
	 * @return the reply.
	 * @throws ThingsException if the connection is made and logged in.
	 */
	public Reply reset() throws ThingsException, InterruptedException;
	
	/**
	 * Send DATA.  It must be at RCPTTO_DONE.  It will do the appropriate dot doubling and CRLF management.
	 * @param ios Stream to send.  You handle any buffering.  Null entries are allowed and ignored.
	 * @return the reply
	 */
	public Reply sendData(InputStream... ios) throws ThingsException, InterruptedException;
	
	/**
	 * Quit the client.  This will return it to FRESH.  It will never throw an exception, but may return a bad reply.
	 * @return a reply
	 */
	public Reply done() throws ThingsException, InterruptedException;

}