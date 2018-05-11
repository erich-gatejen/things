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

/**
 * The actor service.  This was never really finished.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 MAY 07
 * </pre> 
 */
public class ActorConstants {

	/**
	 * The top of reserved orders.  This is not enforced.
	 */
	static final long RESERVED_ORDER_MAX = 1000;

	/**
	 * Message token size limit in bytes.  This will be twice the character count in most cases.
	 * The token is both the name and value for each parameter pair.
	 */
	static final long MAX_MESSAGE_TOKEN = 4096;

	/**
	 * Max parameters per message.
	 */
	static final long MAX_PARAMETERS_PER_MESSAGE = 10;
	
	/**
	 * Message type.
	 */
	
	// Unnumbered
	public final static String MESSAGE_TYPE_NONE = "NONE";
	public final static String MESSAGE_TYPE_QUIT = "QUIT";
	public final static String MESSAGE_TYPE_ACK = "ACK";
	public final static String MESSAGE_TYPE_NACK = "NACK";
	public final static String MESSAGE_TYPE_QUITING = "QUITING";  
	public final static String MESSAGE_TYPE_LOG = "LOG";
	
	// Numbered
	public final static String MESSAGE_TYPE_SERVICE = "SERVICE";
	public final static String MESSAGE_TYPE_CONFIGURATION = "CONFIGURATION";
	public final static String MESSAGE_TYPE_SEND = "SEND";
	
	/**
	 * No parameter.
	 */
	static final String NO_PARAMETER = null;
	
	// General
	public final static String PARAMETER_ACK_SEQUENCE = "ACKSEQ";
	public final static String PARAMETER_CLASS_NAME = "CLASS";
	public final static String PARAMETER_RESPONSE = "RESPONSE";
	public final static String PARAMETER_LOG = "LOG";
	
	// Specific
	public final static String PARAMETER_HOST = "HOST";
	public final static String PARAMETER_PORT = "PORT";
	public final static String PARAMETER_RESETS = "RESETS";
	public final static String PARAMETER_FROM = "FROM";
	public final static String PARAMETER_TO = "TO";
	public final static String PARAMETER_FILE = "FILE";
	public final static String PARAMETER_CODE = "CODE";	
	public final static String PARAMETER_TIME = "TIME";		
	public final static String PARAMETER_SIZE = "SIZE";		
	
	/**
	 * Initial sequence number.  Typical, this message will be the SERVICE implementation.
	 */
	static final int STARTUP_SEQUENCE_NUMBER = 0;
	
	/*
	 * Running initial sequence number.
	 */
	static final int INITIAL_SEQUENCE_NUMBER = 1;
	
	/*
	 * Not used sequence number.
	 */
	static final int UNNUMBERED_SEQUENCE_NUMBER = -1;

	/**
	 * Encode an integer.
	 * @param encoded
	 * @param offset
	 * @param value
	 */
	public static void encodeInteger(byte[]	encoded,	int		offset, 	int 	value) {
		encoded[offset + 3] = (byte) (value >>> 0);
		encoded[offset + 2] = (byte) (value >>> 8);
		encoded[offset + 1] = (byte) (value >>> 16);
		encoded[offset] = (byte) (value >>> 24);
	}
	
	/**
	 * Decode an integer.
	 * @param encoded
	 * @param offset
	 * @return
	 */
    static int decodeInteger(byte[]	encoded, int 	offset) {
    	return ((encoded[offset + 3] & 0xFF) << 0) +
    	       ((encoded[offset + 2] & 0xFF) << 8) +
    	       ((encoded[offset + 1] & 0xFF) << 16) +
    	       ((encoded[offset + 0] & 0xFF) << 24);
    }
	
    
    
}

