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
package things.data.processing.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import things.common.ThingsException;
import things.data.processing.rfc822.HeaderProcessorv3;

/**
 * Processes headers for HTTP.  This implementation assumes 8-bit characters throughout the header.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 07
 * </pre> 
 */
public class HttpHeaderProcessor extends HeaderProcessorv3 {

	// ================================================================================================================
	// PUBLIC DATA 
	
	// ================================================================================================================
	// INTERNAL DATA 

	// HEADERS THAT WE CARE ABOUT
	public final static int HEADER_CONTENT_TYPE	=	1;
	public final static String HEADER_CONTENT_TYPE_STRING	= "Content-Type";
	public final static int HEADER_CONTENT_LENGTH	=	2;
	public final static String HEADER_CONTENT_LENGTH_STRING	= "Content-Length";
	public final static int HEADER_TRANSFER_ENCODING	=	3;
	public final static String HEADER_TRANSFER_ENCODING_STRING	= "Transfer-Encoding";
	
	// Session data
	private HttpHeaders headers;
	
	
	// ================================================================================================================
	// CONSTRUCTOR
	public HttpHeaderProcessor() throws Throwable {
		super();
	}
	
	// ================================================================================================================
	// CONSTRUCTOR
	
	/**
	 * Process the header.  This is a one way trip.  This implementation does NOT write anything.  
	 * @param input the source input.
	 * @param headers the headers.  all header data will be written into it.
	 * @throws Throwable
	 */
	public synchronized void  processHeader(InputStream input, HttpHeaders headers) throws Throwable {
		try {
		
			// Qualify
			if (input==null) ThingsException.softwareProblem("Null input passed to processHeader");
			if (headers==null) ThingsException.softwareProblem("Null headers passed to processHeader");
			this.headers = headers;
			
			// Process
			this.process("nothing", input);

		} catch (InterruptedException ie) {
			throw ie;
		} catch (Throwable t) {
			throw new ThingsException("Header processing failed.", ThingsException.PROCESSOR_HTTPHEADER_FAILED, t);
		}
	}
	
	
	// ================================================================================================================
	// ================================================================================================================
	// ABSTRACT METHODS
	
	/**
	 * All declarations should be put here, so they are done with any initialization.
	 * @throws Throwable
	 */
	protected void declarations() throws Throwable {
		declare(HEADER_CONTENT_TYPE_STRING,	HEADER_CONTENT_TYPE, 	 false);
		declare(HEADER_CONTENT_LENGTH_STRING,	HEADER_CONTENT_LENGTH,	false);
		declare(HEADER_TRANSFER_ENCODING_STRING,	HEADER_TRANSFER_ENCODING,	false);
	}
	
	/**
	 * Start on a specific header.  This gives the implementation a chance to initialize.
	 * @param messageId the id for the message being processed.  The implementation may choose to ignore it.
	 * @throws Throwable
	 */
	protected void start(String messageId) throws Throwable {
		// Nothing.
	}
	
	/**
	 * This method will be called when a header is unmatched.  Be sure to write to outs if you want anything preserved!  This includes the CRLF.
	 * The read() method will supply the read of the header line.  
	 * @param headerBuffer What we read about the header already.
	 * @param size how much is actually in the buffer.
	 * @throws Throwable
	 */
	protected void unmatch(byte[] headerBuffer, int size) throws Throwable {
		try  {
			String name = new String(headerBuffer, 0, size);
			String value = rest(this);
			if (name.charAt(name.length()-1)==':') name = name.substring(0, name.length()-1);
			headers.headerItems.setProperty(name, value );
		} catch (IOException ioe) {
			throw ioe;
		} catch (InterruptedException ie) {
			throw ie;			
		} catch (Throwable t) {
			throw new ThingsException("Processing broke on unrecognized header.", ThingsException.PROCESSOR_HTTPHEADER_UNHANDLED, t);
		}
	}

	/**
	 * This method will be called when a header is matched.  This will be called when a header is matched for a read-only operation.
	 * @param id The defined id.
	 * @throws Throwable
	 */
	protected void match(int	id) throws Throwable {
		String value = null;
		try {
			value = rest(this).trim();
		} catch (IOException ioe) {
			throw ioe;
		} catch (InterruptedException ie) {
			throw ie;
		} catch (Throwable t) {
			throw new ThingsException("Bad header.", ThingsException.PROCESSOR_HTTPHEADER_FAILED, t);
		}
		
		switch(id) {
		case HEADER_CONTENT_TYPE: 
			try {
				headers.contentType = value;
				headers.headerItems.setProperty(HEADER_CONTENT_TYPE_STRING, value);
				
			} catch (Throwable t) {
				throw new ThingsException("Bad content type header.", ThingsException.PROCESSOR_HTTPHEADER_FAILED, t);
			}
			break;
			
		case HEADER_CONTENT_LENGTH: 
			try {
				headers.contentLength = Long.parseLong(value);
				headers.headerItems.setProperty(HEADER_CONTENT_LENGTH_STRING, value);
				
			} catch (Throwable t) {
				throw new ThingsException("Bad content length header.", ThingsException.PROCESSOR_HTTPHEADER_FAILED, t);
			}
			break;
			
		case HEADER_TRANSFER_ENCODING: 
			try {
				headers.transferEncoding = value;
				headers.headerItems.setProperty(HEADER_TRANSFER_ENCODING_STRING, value);
				
			} catch (Throwable t) {
				throw new ThingsException("Bad transfer encoding header.", ThingsException.PROCESSOR_HTTPHEADER_FAILED, t);
			}
			break;
		
		default:
			ThingsException.softwareProblem("Matched but not declared.  This is a bug.");
		}
	}
	
	/**
	 * Complete on a specific header.  This gives the implementation a chance to return entries.  If anything is returned at all
	 * is up to the implementation.  It can give a null if it wants to.
	 * @return A list of header fields.
	 * @throws Throwable
	 */
	protected List<String> complete() throws Throwable {
		return null;
	}
	
	/**
	 * The headers are done.  All that is left is the final CR and LFs, whatever they may be. This is a good time to add your own headers.
	 * @throws Throwable
	 */
	protected void headersComplete() throws Throwable {
		// NOP
	}
	
	// ========================================================================================
	// METHODS
	
	/**
	 * Read the rest of the stream into a new string.
	 * BE VERY BAD AND ASSUME 8bit characters!
	 * @return A String with the rest of the stream.
	 * @param is the stream to deplete.
	 */
	private String rest(InputStream is) throws Throwable {
		StringBuffer buffer = new StringBuffer();
		int current = is.read();		
		while (current >= 0) {
			buffer.append((char)current);    // bad programmer!
			current = is.read();
		}
		return buffer.toString();
	}

}


