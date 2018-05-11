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

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.thinger.io.EmptyInputStream;

/**
 * A response processor.  Does not handle trailing headers!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 07
 * </pre> 
 */
public class HttpResponseProcessor {

	// ================================================================================================================
	// PUBLIC DATA 
	
	// ================================================================================================================
	// INTERNAL DATA 
	private HttpHeaderProcessor headerProcessor;
	private ResponseLineParser responseLineParser;
	
	private final static EmptyInputStream emptyStream = new EmptyInputStream();

	// ================================================================================================================
	// CONSTRUCTOR
	public HttpResponseProcessor() throws Throwable {
		super();
		headerProcessor = new HttpHeaderProcessor();
		responseLineParser = new ResponseLineParser();
	}
	
	// ================================================================================================================
	// METHODS
	
	/**
	 * Process a response from a stream. 
	 * <p>
	 * Only one thread per object at a time, so we synchronize it.
	 * @param input the source input.  It should be buffered already.
	 * @return the request
	 * @see things.data.processing.http.HttpResponse
	 * @throws ThingsException for any problem not related to the input source.  Check the level to decide how fatal it is.
	 * @throws IOException if the input source has a problem.  You can assume it is dead.
	 */
	public synchronized HttpResponse  process(InputStream input) throws ThingsException, InterruptedException, IOException {
		HttpResponse response = null;
		
		try {
			// Qualify
			if (input==null) ThingsException.softwareProblem("Null input passed to process");
			
			// Data
			response = new HttpResponse();
			
			// Step 1 - Start-line
			try {
				responseLineParser.parser(input, response);
			} catch (ThingsException te) {
				// Let's forgive error level problems for now.
				if (te.isWorseThanWarning()) {
					throw new ThingsException("Failed parsing Status-Line.", ThingsException.PROCESSOR_HTTPREQUEST_FAILED, te); 
				}
			}
			
			// Step 2 - Header processing
			headerProcessor.processHeader(input, response);
			
			// Step 3 - What is the bopy?
			if (response.code == HttpHeaders.CODE_CONTINUE) {
				// Continue.  Deplete the trailing CRLF and return a empty stream
				input.read();
				if (input.read() != ThingsConstants.LF) ThingsException.softwareProblem("Was expecting a trailing CRLF in a Continue.");
				response.bodyStream = new EmptyInputStream();		
				
			} else {
				
				// Chunked or limited?
				if ((response.transferEncoding!=null)&&(response.transferEncoding.trim().equalsIgnoreCase("chunked"))) {
					response.bodyStream = new ChunkedInputStream(input);
					response.alwaysDrain=true;
				} else if (response.contentLength>0) {
					response.bodyStream = new LimitedInputStream(input, response.contentLength);
					response.alwaysDrain=true;
				} else {
					// No.  Just hand back an empty stream.
					response.bodyStream = emptyStream;
					response.alwaysDrain=false;
				}

			}

		} catch (ThingsException te) {
			throw te;
		} catch (InterruptedException ie) {
			throw ie;
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			throw new ThingsException("Processing failed.", ThingsException.PROCESSOR_HTTPREQUEST_FAILED, t);
		}
		
		return response;
	}
	
	// ========================================================================================
	// METHODS
	

}


