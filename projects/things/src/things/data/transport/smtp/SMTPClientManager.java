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
import things.thing.RESULT;
import things.thing.UserException;
import things.thinger.io.Logger;

/**
 * Interface for the client managers.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 23 NOV 06
 * </pre> 
 */
public interface SMTPClientManager  {
	
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
	public void init(Logger aLogger, String name, String targetAddress, int targetPort, boolean isStreaming, boolean extensions) throws ThingsException, InterruptedException;
	
	/**
	 * Set the maximum number of messages a stream can send before it is closed and reestablished.
	 * @param max the maximum number.  It must be 1 or greater or you'll get an exception.  
	 * @throws ThingsException
	 * @throws InterruptedException
	 */
	public void setStreamMax(int max) throws ThingsException, InterruptedException;
	
	/**
	 * Set a new timeout.  It'll take effect after the next connection is made (which might not be obvious).
	 * @param timeout in milliseconds.
	 * @throws ThingsException
	 */
	public void setTimeout(int timeout) throws ThingsException;
	
	/**
	 * Set a DSN for all subsequent sends.  If set to null, it will not be used.  Extensions must be supported for this to work.
	 * @param dsn the dsn
	 */
	public void setDSN(DSN dsn);
	
	/**
	 * Are extensions supported?
	 * @return true if they are
	 */
	public boolean isExtensionsSet();
	
	/**
	 * Submit a message.  We'll let InterruptedExceptions out like a good THINGS citizen.
	 * @param senderInet
	 * @param recipientInet
	 * @param dataStream InputStreams to be sent in turn.  Null entries are allowed and ignored.
	 * @return the SMTP reply for the send.  The size field is useful.
	 * @throws ThingsException if not successful for any reason.  (Don't change this without modifying the other submit methods.)
	 */
	public Reply submit(String senderInet, String recipientInet, InputStream... dataStream) throws ThingsException, InterruptedException;
	
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
	public RESULT meassuredSubmit(String senderInet, String recipientInet, String  id, InputStream... dataStream) throws UserException, InterruptedException;

	/**
	 * Force a disconnection.  It is nice, but not necessary, to call this when you are done--or just done for a while.  It doesn't ruin
	 * the client.  You could still use it again (but remember, init is only once!).
	 * @throws ThingsException
	 * @throws InterruptedException
	 */
	public void disconnect()  throws ThingsException, InterruptedException;
		
	


	
}
