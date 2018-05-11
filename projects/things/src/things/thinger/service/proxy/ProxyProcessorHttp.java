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
package things.thinger.service.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.data.processing.http.HttpRequest;
import things.data.processing.http.HttpRequestProcessor;
import things.data.processing.http.HttpResponse;
import things.data.processing.http.HttpResponseProcessor;
import things.thinger.io.EchoInputStream;

/**
 * A proxy processor implementation for http.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public class ProxyProcessorHttp implements ProxyProcessor {
	
	// ===================================================================================================
	// FIELD
	public final static String DEFAULT_EXTENSION = "out";
	public final static int BUFFER_SIZE = 1024;
	
	// The various level-3 lines.
	public final static String LINE_TRANSACTION = "TX";
	public final static String LINE_DROP = "DROP";
	public final static String LINE_TRANSPORT = "TRANSPORT";
	public final static String LINE_HEADER = "HEADER";
	public final static String LINE_URLVALUE = "URLVALUE";
	public final static String LINE_BODYVALUE = "BODYVALUE";
	
	// ===================================================================================================
	// DATA
	private HttpRequestProcessor	requestProcessor;
	private HttpResponseProcessor	responseProcessor;

	// Buffer for copy
	private byte[] buffer = new byte[BUFFER_SIZE];
	private int size;

	// ===================================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 * @throws Throwable
	 */
	public ProxyProcessorHttp() throws Throwable {
		requestProcessor = new HttpRequestProcessor();
		responseProcessor = new HttpResponseProcessor();
	}

	/**
	 * The processor.  NOT THREAD SAFE!
	 * @param context the context.
	 * @throws Throwable for any problem.
	 */
	public void process(ProxyContext context) throws Throwable {
		
		OutputStream out = null;
		InputStream in = null;
		int number = 0;
		
		String corrId = "";
		try {
			context.logger.info("Start processing http connection.", ThingsCodes.SERVICE_PROXY_CONTEXT_START_PROCESS, ThingsNamespace.ATTR_ID, context.id.toString());
			
			while(true) {
				
				// Do request portion 
				number++;
				corrId = context.id.toString() + "_" + number;
				long startTime = System.currentTimeMillis();
				HttpRequest request = requestProcessor.process(new EchoInputStream(context.downlinkIn, context.uplinkOut));
				context.uplinkOut.flush();	// Make sure it all makes it to the server.
				
				// Do response
				HttpResponse response = responseProcessor.process(new EchoInputStream(context.uplinkIn, context.downlinkOut));
				
				// Decide the extension
				String extension = DEFAULT_EXTENSION;
				int dot = request.path.lastIndexOf('.');
				if ((response.contentType!=null)&&(dot>0)&&(dot<request.path.length()-1)) {
					extension = request.path.substring(dot+1);
				}
				
				// Get the streams and push the body, if it exists.
				String poutFileName = "";
				if (response.alwaysDrain) {
					ProxyOutput pout = context.GET_OUTPUT(corrId + "." + extension);
					out = pout.getStream();
					in = response.bodyStream;
					
					// Push the body.
					while ((size = in.read(buffer)) > 0) {
						out.write(buffer, 0, size);
					}
					pout.done();
					poutFileName = pout.getName();
				} 
				context.downlinkOut.flush();	// Make sure it all makes it to browser.
				long endTime = System.currentTimeMillis();
				
				// Log input
				context.POSTSTART(LINE_TRANSACTION, ThingsUtilityBelt.timestampFormatterYYYYDDDHHMMSSmmmm(startTime), corrId);
				context.POSTACTION(request.method.toString(), request.path);
				context.POSTMULTI(LINE_TRANSPORT, "httpVersion", request.httpVersion, "contentLength", Long.toString(request.contentLength), "contentType", request.contentType, "transferEncoding", request.transferEncoding);
				for (String name : request.headerItems.sub(null)) {
					context.POSTSINGLE(LINE_HEADER, name, request.headerItems.getProperty(name).trim());
				}
				for (String name : request.urlValues.sub(null)) {
					context.POSTSINGLE(LINE_URLVALUE, name, request.urlValues.getProperty(name).trim());
				}
				for (String name : request.bodyValues.sub(null)) {
					context.POSTSINGLE(LINE_BODYVALUE, name, request.bodyValues.getProperty(name).trim());
				}
				
				// Log output
				context.POSTACTION(ThingsUtilityBelt.timestampFormatterYYYYDDDHHMMSSmmmm(endTime), Integer.toString(response.code), poutFileName, response.codeText, response.reasonPhrase);
				context.POSTMULTI(LINE_TRANSPORT, "httpVersion", response.httpVersion, "contentLength", Long.toString(response.contentLength), "contentType", response.contentType, "transferEncoding", response.transferEncoding);
				for (String name : response.headerItems.sub(null)) {
					context.POSTSINGLE(LINE_HEADER, name, response.headerItems.getProperty(name).trim());
				}
				for (String name : response.urlValues.sub(null)) {
					context.POSTSINGLE(LINE_URLVALUE, name, response.urlValues.getProperty(name).trim());
				}		
				for (String name : response.bodyValues.sub(null)) {
					context.POSTSINGLE(LINE_BODYVALUE, name, response.bodyValues.getProperty(name).trim());
				}					
				context.POSTDONE();

				context.logger.info("Query/Response complete.", ThingsCodes.SERVICE_PROXY_SR_COMPLETE, ThingsNamespace.ATTR_DATA_TARGET, poutFileName, ThingsNamespace.ATTR_ID, context.id.toString());
			}
			
		} catch (InterruptedException ie) {
			throw ie;
			
		} catch (IOException se) {
			// The peer dumped us.  That's cool.  Let the context die.
			context.POSTSTART(LINE_DROP, ThingsUtilityBelt.timestampFormatterYYYYDDDHHMMSSmmmm(System.currentTimeMillis()), corrId);
			context.POSTDONE();
			context.logger.debug("Connection dropped.", ThingsCodes.DEBUG, ThingsNamespace.ATTR_ID, context.id.toString(), ThingsNamespace.ATTR_PLATFORM_MESSAGE, se.getMessage());
			
		} catch (Throwable t) {
			throw new ThingsException("Failed to processes request.", ThingsException.PROXY_CONNECTION_ERROR, t, ThingsNamespace.ATTR_ID, context.id.toString());
			
		} finally {
			try {
				out.close();
			} catch (Throwable tt) {}
			try {
				in.close();
			} catch (Throwable tt) {}
		}
	}
	
}
