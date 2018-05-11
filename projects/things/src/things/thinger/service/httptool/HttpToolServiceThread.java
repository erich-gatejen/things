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
package things.thinger.service.httptool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Random;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.common.tools.Base64;
import things.common.tools.Rendezvous;
import things.data.impl.ThingsPropertyTreeRAM;
import things.data.processing.http.HttpRequest;
import things.data.processing.http.HttpRequestProcessor;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.kernel.ThingsProcess;

/**
 * A specific thread.
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  Based on something from another tool - 13 DEC 08
 * EPG - Set content type charset for PAGE as utf-8.  Firefox 4 gets bitchy without it.  - 8 APR 10
 * EPG - Stupid bug.  I set the Http-Version as "HTTP" instead of "HTTP/1.1"  Firefox 4 really didn't like.  It treated the streamed data as text instead of an HTTP message. 
 *       For some reason whenever it went through the logging proxy it worked.  Weird. - 9 APR 10
 * </pre> 
 */
public class HttpToolServiceThread extends ThingsProcess {
	
	// ===================================================================================================
	// EXPOSED DATA
	public Rendezvous<HttpToolServiceContext> handoff;
	public final static int COPY_BUFFER_SIZE = 4096;
	
	// ===================================================================================================
	// INTERNAL DATA
	private HttpToolServiceContext currentContext;
	private HttpRequestProcessor requestProcessor;
	private PageManager pageManager;
	private ActionManager actionManager;
	private ServeManager serveManager;
	//private boolean running;
	
	private Logger logger;
	
	// -- per call --
	// These are not thread safe, since it is assumed only this thread will ever touch them.
	private BufferedInputStream bis;
	private BufferedOutputStream bos;
	private ThingsPropertyTreeRAM tags;
	private HttpRequest request;

	// ===================================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 */
	public HttpToolServiceThread(PageManager pageManager, ActionManager actionManager, ServeManager serveManager) throws Throwable {
		super();

		this.pageManager = pageManager;
		this.actionManager = actionManager;
		this.serveManager = serveManager;	
	}
	
	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * ABSTRACT METHODS

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * @throws things.thinger.SystemException
	 */
	public void constructThingsProcess() throws SystemException {
		handoff = new Rendezvous<HttpToolServiceContext>();
		try  {
			requestProcessor  = new HttpRequestProcessor();
		} catch (Throwable t) {
			SystemException.softwareProblem("Couldn't build HttpToolServiceThread", t);
		}
	}

	/**
	 * This is the entry point for the actual processing.  It's ok to let interrupted exceptions leave.  It'll be the
	 * kernel or process that did it.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException, InterruptedException {
		
		while (!getCurrentState().isHalting()) {

			try {
				currentContext = handoff.enter();
				if (currentContext==null) throw new InterruptedException();		// Done.
				logger = currentContext.si.getSystemLogger();
				logger.debug("---- Accepted a connection.");
				process();
				
			} catch (InterruptedException e) {
				throw e;
	
			} catch (Throwable t) {
	
				// Generally, don't care why.
				try {
					if (logger.debuggingState())
						logger.error("Connection died to exception.  message=" + t.getMessage(), ThingsCodes.SERVICE_HTTPTOOL_ERROR, ThingsNamespace.ATTR_ID, currentContext.id.toString(), ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
					else
						logger.error("Connection died to exception.  message=" + t.getMessage(), ThingsCodes.SERVICE_HTTPTOOL_ERROR, ThingsNamespace.ATTR_ID, currentContext.id.toString());
				} catch (SystemException e) {
					handoff = null;
					throw new Error("Failed to log an error.  This is a very bad thing.", e);
				}
	
			} finally {
				//logger.debug("---- Completed a connection.");
				currentContext.ownerService.complete(this);
			}
			
		} // end while
		
		logger.info("HttpToolService stopping.");
	}
	
	/**
	 * Destroy. This will be called when the Process is finalizing.
	 * @throws things.thinger.SystemException
	 */
	public void destructThingsProcess() throws SystemException {
		// NOP
	}

	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "HttpToolServiceThread";
	}	
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL
	
	/**
	 * The no-cache head.
	 */
	private Head emptyHead = new Head();
	private Random emptyRandom = new Random();		// This can be changed at any time by any thread.
	
	/**
	 * This is the entry point for the actual processing
	 */
	public void process() throws Throwable {
		long dataSize = 0;
		
		// -- Run ----------------------------------------------
		
		try {
			
			// Get the next connection
			bis = new BufferedInputStream(currentContext.link.getInputStream());
			bos = new BufferedOutputStream(currentContext.link.getOutputStream());
			
			while (!getCurrentState().isHalting()) {
					
				// Read the request.
				request  = requestProcessor.process(bis);					
				
				// Process it
				dataSize = processRequest();
						
				// Done.
				logger.info("Completed request.", ThingsCodes.SERVICE_HTTPTOOL_OK, ThingsNamespace.ATTR_PROCESSING_HTTP_ACTION, request.path, ThingsNamespace.ATTR_DATA_SIZE, Long.toString(dataSize));
			}
			
		} catch (InterruptedException ie) {
			HttpToolkit.feebleReply("Server is shutting down.", bos);
			throw ie;
			
		} catch (EOFException eof) {
			// NOP for now.  Browser probably dropped the connection.
			
		} catch (SocketTimeoutException stm) {
			// NOP for now
			
		} catch (FileNotFoundException fnfe) {
			// I assume this exception can only happen AFTER request is read.  If I'm wrong, there might be null pointer exceptions here.
			HttpToolkit.feebleReply("Not found:" + request.path + "<p>", bos, "404 Not Found");
			logger.error("Error while processing request.  File not found.", ThingsCodes.SERVICE_HTTPTOOL_BAD_REQUEST, ThingsNamespace.ATTR_UNIVERSE_PATH, request.path);

		} catch (ThingsException se) {
			if (se.isWorseThanFault()) {
				HttpToolkit.feebleReply("Serious fault while processing request.<p>The http server will stop.<p>" + se.toStringComplex(), bos);
				logger.postit("Fault while processing request.  Quitting with fault.");
				throw new SystemException("Exception caused fault for HttpToolService process execute().  Quitting with error.",SystemException.SYSTEM_COMMAND_FAULT_SERVICE_ABORTED, se, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
			} else {
				HttpToolkit.feebleReply("Error while processing request.<p>" + se.toStringComplex(), bos);
				logger.error("Error while processing request.", ThingsCodes.SERVICE_HTTPTOOL_BAD_REQUEST, se.getAttributesNVDecorated(ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, se.toStringCauses()));
			}
				
		} catch (Throwable t) {
			HttpToolkit.feebleReply("Error while processing request.<p>" + t.getMessage(), bos);
			logger.error("Error while processing request.", ThingsCodes.SERVICE_HTTPTOOL_BAD_REQUEST, ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
										
		} finally {
			
			// Make sure everything is closed.
			try {
				bos.flush();
				bos.close();
			} catch (Throwable t) {
				// Abandon ship
			}
			try {
				bis.close();
			} catch (Throwable t) {
				// Abandon ship
			}	
			try {
				currentContext.link.close();
			} catch (Throwable t) {
				// Abandon ship
			}
		}
			
	}

	// =====================================================================================================================================
	// =====================================================================================================================================
	// ENGINE
	
	/**
	 * Process a request.  This will be single threaded for now.
	 * @throws SystemException *ONLY* if you want the service to die!
	 */
	protected long processRequest() throws SystemException  {
		long result;
		
		// Final tags
		tags = new ThingsPropertyTreeRAM();
		Action	workingAction = null;
		ActionResult workingResult = null;
		
		// Try to run it.  NEVER let an exception out of here.
		try {
			if (request.method == HttpRequest.Method.UNSUPPORTED) throw new ThingsException("Unknown/unsupported HTTP method.", ThingsException.SERVICE_HTTPTOOL_ERROR_UNKNOWN_METHOD, ThingsNamespace.ATTR_PROCESSING_HTTP_METHOD, request.method.name());
			logger.debug("Processing a " + request.method.name() );

			String workingActionName = getActionFromPath(request.path);
			
			// Loop while we still have actions to do.
			do {
				
				// Get the action
				workingAction = actionManager.get(workingActionName);
				
				// HEAD or POST/GET?
				switch(request.method) {
				
				case HEAD:
					if (workingAction == null) {
						workingResult = new ActionResult(ActionResult.Type.SERVE);
						workingResult = serveHead(request);

					} else {
						workingResult = pageHead(workingAction);
					}
					break;	
					
				case POST:
				case GET:
					if (workingAction == null) {
						workingResult = serve(request);
						
					} else {
						workingResult = request(workingAction);
					}
					break;	
					
				default:
					throw new ActionException("Unsupported HTTP method.", "This server does not support that method.  method=" + request.method.name());	
				}
				
				// In case we iterate.
				workingActionName = workingResult.getActionResult();
				
			} while (workingResult.type == ActionResult.Type.ACTION);
				
		} catch (ActionException ae) {
			try {
				workingResult = error(ae.getDescription(), ae.getMessage(), ae);
			} catch (Throwable tt) {
				// Ok, something is very wrong.
				SystemException.softwareProblem("Can't even set error tags.", tt);
			}
			
		} catch (Throwable t) {
			try {		
				// Try to get what really matters.  Sort of ugly.
				if (t instanceof ThingsException) {
					workingResult = error(t.getMessage(), ((ThingsException)t).toStringCauses(), t);
				} else {
					Throwable tCause = t.getCause();
					if (tCause == null) {
						workingResult = error("Could not run action.", ThingsException.toStringCauses(t), t);
					} else {
						if ( tCause instanceof ThingsException) {
							workingResult = error(t.getMessage(), ((ThingsException)tCause).toStringCauses(), tCause);
						} else {
							workingResult = error(t.getMessage(),  ThingsException.toStringCauses(tCause), t);
						}
					}
				}
			} catch (Throwable tt) {
				// Ok, something is very wrong.
				SystemException.softwareProblem("Can't even set error tags.", tt);
			}
		}
		
		// Prepare the result for response.	
		String resultPage = "N/A";
		if (workingResult.type != ActionResult.Type.SERVE) {
			
			try {
				// Make sure we have a result page.  If we don't, it's an error.
				resultPage = workingResult.getPageResult();
				if (workingResult.getPageResult() == null) throw new Exception("Processing did not complete a page.  This is a bug.");
				
				// Let the manager run.
				Page thePage = pageManager.get(resultPage);
				if (thePage== null) throw new Exception("Page source not found in universe.");
				
				byte[] text = thePage.process(tags).getBytes();
				workingResult.setInputStream(new ByteArrayInputStream(text));
				workingResult.addHeader("Content-Length", Integer.toString(text.length));

			} catch (Throwable t) {
				throw new SystemException("Failed to render page.", SystemException.SERVICE_HTTPTOOL_PAGE_RENDER_FAILED, t, ThingsNamespace.ATTR_PROCESSING_HTTP_PAGE, resultPage);
			}
		}

		// Render
		try {
			result = respond(workingResult);
		} catch (Throwable t) {
			throw new SystemException("Failed to respond page.", SystemException.SERVICE_HTTPTOOL_RESPONSE_FAILED, t);
		} finally {
			// Close the source stream.
			try {
				workingResult.getInputStream().close();
			} catch (Throwable t) {
				// Oh well.  Perhaps finalization will whack it.
			}
		} 
		return result;
	}

	/**
	 * Build an error result.  It will log them here.
	 * @param description a short description of the error.
	 * @param message a message explaining the error.
	 * @param t optional exception.  It'll be expressed if debugging is on.
	 * @return the result.
	 * @throws Throwable
	 */
	private ActionResult error(String description, String message, Throwable t) throws Throwable {
		ActionResult result = new ActionResult(ActionResult.Type.ERROR);		// Standard error page.
		
		// Build response
		result.setPageResult(pageManager.getErrorPageName());
		try {
			// Use the information from the action exception.
			tags.setProperty(CommonTagsParams.TAG_ERROR_DESCRIPTION, description);
			tags.setProperty(CommonTagsParams.TAG_ERROR_MESSAGE, "<pre>" + HttpToolkit.htmlString(message) + "</pre>");
		} catch (Throwable tt) {
			// Ok, something is very wrong.
			SystemException.softwareProblem("Can't even set error tags.", tt);
		}
		result.setResponse("HTTP/1.1 500 Internal Error");
		result.addHeader("Server", "HttpCommandService");
		result.addHeader("Content-Type", "text/html");
		
		// Log it.
		if (logger.debuggingState()&&(t!=null)) {
			logger.error(message, ThingsCodes.SERVICE_HTTPTOOL_ERROR, ThingsNamespace.ATTR_THING_RESULT_DESCRIPTION, description, ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringComplex(t));
		} else {
			logger.error(message, ThingsCodes.SERVICE_HTTPTOOL_ERROR, ThingsNamespace.ATTR_THING_RESULT_DESCRIPTION, description);
		}

		return result;
	}

	/**
	 * PAGE request.
	 * @param workingAction the action.
	 * @return the result.
	 * @throws Throwable
	 */
	private ActionResult request(Action workingAction) throws Throwable {
		// ACTION
		ThingsPropertyTreeRAM merged = new ThingsPropertyTreeRAM();
		for (String item : request.bodyValues.sub(null)) {						// Added junk to deal with splitting body from URL values.
			merged.setProperty(item, request.bodyValues.getProperty(item));
		}
		for (String item : request.urlValues.sub(null)) {
			merged.setProperty(item, request.urlValues.getProperty(item));
		}
		ActionResult result = workingAction.execute(merged.getRoot(), tags, currentContext.si);
		result.setResponse("HTTP/1.1 200 OK");
		result.addHeader("Server", "HttpCommandService");
		result.addHeader("Content-Type", "text/html; charset=utf-8");
		return result;
	}
	
	/**
	 * PAGE head.
	 * @param workingAction the action.
	 * @return the result.
	 * @throws Throwable
	 */
	private ActionResult pageHead(Action workingAction) throws Throwable {
		ThingsPropertyTreeRAM merged = new ThingsPropertyTreeRAM();
		for (String item : request.bodyValues.sub(null)) {						// Added junk to deal with splitting body from URL values.
			merged.setProperty(item, request.bodyValues.getProperty(item));
		}
		for (String item : request.urlValues.sub(null)) {
			merged.setProperty(item, request.urlValues.getProperty(item));
		}
		
		Head head = workingAction.head(merged.getRoot(), tags, currentContext.si);		
		return convertHead2Result(head);
	}
	
	/**
	 * SERVE request.
	 * @param request the request.
	 * @return the result.
	 * @throws Throwable
	 */
	private ActionResult serveHead(HttpRequest request) throws Throwable {
		ActionResult result;
		
		ServeFile file = serveManager.get(request.path);
		if (file==null) {
			throw new FileNotFoundException();
		} else {
			result = new ActionResult(ActionResult.Type.SERVE);
			result.setResponse("HTTP/1.1 200 OK");
			result.addHeader("Server", "HttpCommandService");
			result.addHeader("Content-Type", file.type);
			result.addHeader("Content-Length", Long.toString(file.length));
			try {
				file.source.close();  // We won't be needing the actual source stream.
			} catch (Throwable t) {
				// Oh well.
			}
		}
		
		return result;
	}
	
	/**
	 * SERVE request.
	 * @param request the request.
	 * @return the result.
	 * @throws Throwable
	 */
	private ActionResult serve(HttpRequest request) throws Throwable {
		ActionResult result;
		
		ServeFile file = serveManager.get(request.path);
		if (file==null) {
			throw new FileNotFoundException("Page not defined for " + request.path);
		} else {
			result = new ActionResult(ActionResult.Type.SERVE);
			result.setResponse("HTTP/1.1 200 OK");
			result.addHeader("Server", "HttpCommandService");
			result.addHeader("Content-Type", file.type);
			result.addHeader("Content-Length", Long.toString(file.length));
			result.setInputStream(file.source);
		}
		
		return result;
	}
	
	/**
	 * Respond.
	 * @param result the result.  It must have the final input stream set.
	 * @return data size.
	 * @throws Throwable
	 */
	private long respond(ActionResult result) throws Throwable {
	
		String item = result.getResponse();
		if (item==null) item = "HTTP/1.1 200 OK";
		emitLine(item);
		
		for (String header : result.getHeaders()) {
			emitLine(header);
		}
		emitLine("");
		return drain(result.getInputStream());	
	}
	
	/**
	 * Convert a head to a result.  If head is null, it'll manufacture a simple response.
	 * @param head the head.
	 * @return a response.
	 */
	private ActionResult convertHead2Result(Head head) {
		
		// Fix head, if necessary.
		Head actual = head;
		if (head==null) {
				// Randomize it.
				byte[] data = new byte[16];
				for (int index = 0; index < data.length ; index++){
					data[index] = (byte)emptyRandom.nextInt(256);
				}
				actual = emptyHead;
				actual.setMd5(Base64.encodeBytes( data, Base64.DONT_BREAK_LINES ));
		}
		
		
		// Build result.
		ActionResult result = new ActionResult(ActionResult.Type.PAGE);
		result.setResponse("HTTP/1.1 200 OK");
		result.addHeader("Server", "HttpCommandService");
		actual.addHeadersToResult(result);
		
		return result;
	}
	
	/**
	 * Get the action from the path.  It does NOT handle encoded actions right now.
	 * <p>
	 * It will return the MAIN action if the URL path is only a /.
	 * @param path
	 * @return the action.
	 * @throws Throwable
	 */
	private String getActionFromPath(String path) throws Throwable {
		
		// Qualify
		if((path==null)||(path.length() < 1)) throw new Exception("Action/Path Not found.  Empty");
		String normal = path.trim();
		
		// Parse
		try { 
			
			// Hax0r - is it just a / ?
			if ( (path.length()==1) && (path.charAt(0)=='/') ) {
				return actionManager.getMainName();
			}
			
			// Peal off the action.
			if (path.charAt(0)=='/') normal = normal.substring(1);
			int end = normal.indexOf('/');
			if (end >= 1) normal = normal.substring(0,end);
			
		} catch (Throwable t) {
			throw new ThingsException("Action/Path Not is bad.  value=" + path, t);
		}
		
		return normal;
	}
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS - mostly unused.
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	public WhoAmI getListenerId() {
		return getProcessId();
	}
	
	// ==========================================================================================================
	// == TOOLS
	
	//private void emit(String item) throws Throwable {
	//	bos.write(item.getBytes());
	//}
	private void emitLine(String line) throws Throwable {
		bos.write(line.getBytes());
		bos.write('\r');
		bos.write('\n');		
	}
	private byte buffer[] = new byte[COPY_BUFFER_SIZE];
	private long drain(InputStream ins) throws Throwable {
		long totalSize = 0;
		int size;
		while ((size = ins.read(buffer)) > 0) {
			bos.write(buffer, 0, size);
			totalSize+=size;
		}
		bos.flush();
		return totalSize;
	}
}
