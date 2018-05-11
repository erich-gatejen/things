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

import things.common.ThingsException;
import things.common.ThingsNamespace;

/**
 * A request processor.  It handles just GET and POST for now.  <p>
 * This implementation assumes 8-bit characters throughout the header.<p>
 * It only supports application/x-www-form-urlencoded bodies.  We don't actually verify that is the format too!  (Just that it isn't
 * multipart).
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 FEB 07
 * </pre> 
 */
public class HttpRequestProcessor {

	// ================================================================================================================
	// PUBLIC DATA 
	
	// ================================================================================================================
	// INTERNAL DATA 
	
	// Tools
	private HttpHeaderProcessor headerProcessor;
	private RequestLineParser requestLineParser;
	
	private BodyProcessor_FormURLEncoded	bodyProcessor_FURLE;	
	
	
	// ================================================================================================================
	// CONSTRUCTOR
	public HttpRequestProcessor() throws Throwable {
		super();
		headerProcessor = new HttpHeaderProcessor();
		requestLineParser = new RequestLineParser();
		bodyProcessor_FURLE = new BodyProcessor_FormURLEncoded();
	}
	
	// ================================================================================================================
	// CONSTRUCTOR
	
	/**
	 * Process a request from a stream.  This implementation is memory intensive.
	 * <p>
	 * Only one thread per object at a time, so we synchronize it.
	 * @param input the source input.  It should be buffered already.
	 * @return the request
	 * @see things.data.processing.http.HttpRequest
	 * @throws ThingsException for any problem not related to the input source.  Check the level to decide how fatal it is.
	 * @throws IOException if the input source has a problem.  You can assume it is dead.
	 */
	public synchronized HttpRequest  process(InputStream input) throws ThingsException, InterruptedException, IOException {
		HttpRequest request = null;
		
		try {
			// Qualify
			if (input==null) ThingsException.softwareProblem("Null input passed to process");
			
			// Data
			request = new HttpRequest();
			
			// Step 1 - Start-line
			try {
				requestLineParser.parser(input, request);
			} catch (ThingsException te) {
				// Let's forgive error level problems for now.
				if (te.isWorseThanWarning()) {
					throw new ThingsException("Failed parsing Request-Line.", ThingsException.PROCESSOR_HTTPREQUEST_FAILED, te); 
				}
			}
			
			// Step 2 - Header processing
			headerProcessor.processHeader(input, request);
			
			// Step 3 - Body processing, if not a multipart.  We are not a patient processor; if there is no data waiting, assume it's done and quit.  
			if ((input.available() > 0)&&(request.contentLength>0)) {
				if (request.contentType.toLowerCase().indexOf("application/x-www-form-urlencoded")==0) {
					bodyProcessor_FURLE.parser(new LimitedInputStream(input, request.contentLength), request);
				} else if (request.contentType.toLowerCase().indexOf("text/plain")==0) {
					// The FURLE can handle these do.  I'm not sure how many people use this as the encoding for these.  Does anyone on the internet behave?!?!?
					bodyProcessor_FURLE.parser(new LimitedInputStream(input, request.contentLength), request);					
				} else if (request.contentType.toLowerCase().indexOf("text/xml")==0) {
					throw new ThingsException("Unsupported body type.  Probably SOAP.  I'll add this later.", ThingsException.PROCESSOR_HTTPREQUEST_FAILED, ThingsNamespace.ATTR_DATA_ARGUMENT, request.contentType);
				} else {
					throw new ThingsException("Unsupported body type.", ThingsException.PROCESSOR_HTTPREQUEST_FAILED, ThingsNamespace.ATTR_DATA_ARGUMENT, request.contentType);
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
		
		return request;
	}
	
	// ========================================================================================
	// METHODS
	

	
	
}


