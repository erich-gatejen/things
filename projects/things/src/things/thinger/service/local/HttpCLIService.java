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
package things.thinger.service.local;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.service.ServiceConstants;

/**
 * A Http Service that will bridge to the CLIServer.  This is a real cheap ploy for now.  
 * I just want to submit CLI commands via HTTP.  If the CLIServer is not running, it will fail.  The decoded
 * get URL will be handed to transaction tender and the complete response will be sent back.  Single threaded too!
 * Dern I suck bad!
 *< p>
 * This requires the LISTEN_PORT (listen) to be set as a local parameter.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 NOV 07
 * </pre> 
 */
public class HttpCLIService extends CLIBackbone {
	
	// ===================================================================================================
	// EXPOSED DATA
	
	// ===================================================================================================
	// INTERNAL DATA
	
	final private static int ACCEPT_TIMEOUT = 2000;
	
	private ServerSocket listen;
	
	// ===================================================================================================
	// CONSTRUCTOR
	public HttpCLIService() {
		super();
	}
	
	// ===================================================================================================
	// SERVICE IMPLEMENTATION
	
	/**
	 * Called to turn the service on.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOn() throws SystemException {
		// Always on
	}
	
	/**
	 * Called to turn the service off.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public void serviceOff() throws SystemException {
		// Always on		
	}
	
	/**
	 * This is the entry point for the actual processing.
	 * @throws things.thinger.SystemException
	 */
	public void executeThingsProcess() throws SystemException {
			
		// The current processing is for test purposes only.
		try {
			
			myLogger.info(getName() + " is alive.");
			
			// DATA
			String currentLine; 
			String response = null;
			Socket accepted = null;
			BufferedInputStream bis = null;
			BufferedWriter bw = null;
			
			// Loop it.  If the consumer wants to stop they should throw a SystemException with SYSTEM_SERVICE_DONE as a numeric.
			while (true) {

				// Get next.  I'm leaving the stream creation outside of the inner try.  Maybe that's brave, but 
				// I hope this is never a problem.  I have to add this stupid timeout because the accept channel doesn't 
				// always respond to thread interruption.
				accepted = null;
				while (accepted == null) {
					
					try {
						accepted = listen.accept();
					} catch (SocketTimeoutException sti) {
						// Punt if we are dying.
						if (this.getCurrentState().isHalting()) throw new ThingsException(ThingsException.SYSTEM_SERVICE_DONE);
					}
				}
				bis = new BufferedInputStream(accepted.getInputStream());
				bw = new BufferedWriter(new OutputStreamWriter(accepted.getOutputStream()));

				try {
					currentLine = null;
					currentLine = decodeUri(bis);
					if ( currentLine.length() > 1) {
						if (currentLine.charAt(0)=='/') currentLine = currentLine.substring(1); // Peal off the first slash 
					} else {
						// Assume it's bad and just do the help.  Since it's either a /, a bad URL, or nothing.
						currentLine = "help";
					}

					response = transactionInterface.tender(currentLine);
					
					// OK RESPONSE
					bw.write("HTTP 200 OK\n");
					bw.write("Server: HttpCLIService\n");
					bw.write("Content-Type: text/plain; charset=ISO-8859-1\n");
					bw.write("Content-Length: " + response.length() + "\n");
					bw.write("\n");
					bw.write(response);
					
				} catch (Throwable tt) {
					
					// It might just be the system.
					if ( (tt instanceof InterruptedException) || (tt.getCause() instanceof InterruptedException)) {
						response = "Interrupted by system.  May be shutting down.\n";
						
					} else {
						if (tt.getCause() instanceof InterruptedException)
						response = "FAULT\n" 
							+ ThingsException.toStringComplex(tt) + "\n";
					}
					
					try {
						// OK RESPONSE
						bw.write("HTTP 500 BAD\n");
						bw.write("Server: HttpCLIService\n");
						bw.write("Content-Type: text/plain; charset=ISO-8859-1 \n");
						bw.write("Content-Length: " + response.length() + "\n");
						bw.write("\n");
						bw.write(response);
					} catch (Throwable ttt) {
						if (myLogger.debuggingState()) {
							myLogger.error("Failed to respond after an error.", ThingsCodes.PROCESSING_FAULT_HTTP, ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringComplex(ttt), ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE_ORIGINAL, response);						
						}
						else
							myLogger.error("Failed to respond after an error.", ThingsCodes.PROCESSING_FAULT_HTTP, ThingsNamespace.ATTR_PLATFORM_MESSAGE, ttt.getMessage());
					}
					
					// Do not throw it if it was from parsing the URI.  I guess this could be prettier... but not now.
					if ( !(tt instanceof NumberFormatException) && !(tt instanceof URISyntaxException) && !(tt instanceof IOException)) throw tt;
					
				} finally {
					
					// Get rid of everything.  Best effort.
					try {
						bw.flush();
						bw.close();
						bis.close();
						accepted.close();
					} catch (Throwable ttt ) {
					}
				}
				
			} // End while working OK
			
		} catch (ThingsException te) {
			
			// Ignore it if it is just a DONE exception
			if (te.numeric != ThingsException.SYSTEM_SERVICE_DONE) {
				throw new SystemException("Unrecoverable exception in the CLIService.", SystemException.SYSTEM_FAULT_SERVICE_PROBLEM, te, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());						
			}
			
		} catch (Throwable t) {
			if (!this.getCurrentState().isHalting())  // Are we just quitting?
				throw new SystemException("Unrecoverable exception in the CLIService.", SystemException.PANIC_SYSTEM_SERVICE_UNRECOVERABLE, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());			

		} finally {
			myLogger.info(getName() + " stopping.");
		}	
	}

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * <p>
	 * Set up the listening port.
	 */
	public void constructThingsProcess() throws SystemException {
		
		try {
			
			// Get the listen port.
			int listenPortValue = 0;
			ThingsPropertyView localProperties = ssi.getLocalProperties();
			String listenPort = localProperties.getProperty(ServiceConstants.LISTEN_PORT);
			if (listenPort==null) throw new ThingsException("Required property not set.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT);
			try {
				listenPortValue = Integer.parseInt(listenPort);
				if ((listenPortValue<1)||(listenPortValue>65334)) throw new Exception();
			} catch (Throwable t) {
				throw new ThingsException("Property value bad.  Expecting a valie socket port.", SystemException.CONFIGURATION_ERROR_BAD_CONFIGURATION, ThingsNamespace.ATTR_PROPERTY_NAME, ServiceConstants.LISTEN_PORT, ThingsNamespace.ATTR_PROPERTY_VALUE, listenPort);
			}
			
			listen = new ServerSocket(listenPortValue);
			listen.setSoTimeout(ACCEPT_TIMEOUT);
			
		} catch (Throwable t) {
			throw new SystemException("Failed to construct CLIService.", SystemException.SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT, t, SystemNamespace.ATTR_SYSTEM_SERVICE_CLASS, this.getClass().getName());
		}
	}

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 */
	public void destructThingsProcess() throws SystemException {
	}
		
	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public String getProcessName() {
		return "HttpService";
	}
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS
	
	/**
	 * The identified resource is in the process of being revoked.  It is still possible for the resource listener to call the resource within the context
	 * of this thread and call.
	 * <p>
	 * @param resourceID the ID of the resource that is being revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevocation(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * The identified resource bas been revoked.  It is gone.  Attempting to call it would be a very bad thing.  The listener should remove the resource 
	 * from it's internal lists..
	 * <p>
	 * @param resourceID the ID of the resource that has been revoked.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void resourceRevoked(WhoAmI	resourceID) throws SystemException, InterruptedException {
		 // Don't care
	}
	
	/**
	 * Get the ID of the listener.
	 * <p>
	 * @return The listener's ID.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getListenerId() {
		return getProcessId();
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL
	
	/**
	 * States for the URI decoder.
	 */
	private enum State {
		NEW,
		METHOD,
		METHOD_SPACE,
		URI,
		URI_ESC,
		URI_ESC_16S,
		URI_ESC_1S;
	}
	
	/**
	 * Pull the URI.  Convert encoded characters.
	 * @param bis input stream
	 * @return The URI (which will be the command string.)
	 * @throws Throwable If line is bad.
	 */
	public static String decodeUri(BufferedInputStream bis) throws IOException, URISyntaxException, NumberFormatException {
		
		StringBuffer accumulator = new StringBuffer();	
		State state = State.NEW;
		int hexValue = 0;
		
		int current = bis.read();
		while (current >= 0) {

			switch(state) {
			
			case NEW:
				if (Character.isWhitespace(current)) throw new URISyntaxException("Bad URI", "Bad method");
				state = State.METHOD;
				break;
				
			case METHOD:
				if (Character.isWhitespace(current)) {
					state = State.METHOD_SPACE;
				} 
				break;
				
			case METHOD_SPACE:
				if (!Character.isWhitespace(current)) {
					state = State.URI;
					accumulator.append((char)current);
				}
				break;
				
			case URI:
				if (Character.isWhitespace(current)) {
					return accumulator.toString();
				} else if (current=='%') {
					state = State.URI_ESC;
				} else {
					accumulator.append((char)current);
				}
				break;
			
			case URI_ESC:
				// Ok, so I code stuff like this because I *THINK* it'll optimize out of the compiler.  Maybe I'm making it worse!  I dunno.
				switch(current) {
				case '0':	hexValue =0; break;
				case '1': hexValue =1*16; break;
				case '2': hexValue =2*16; break;
				case '3': hexValue =3*16; break;
				case '4': hexValue =4*16; break;
				case '5': hexValue =5*16; break;
				case '6': hexValue =6*16; break;
				case '7': hexValue =7*16; break;
				case '8': hexValue =8*16; break;
				case '9': hexValue =9*16; break;
				case 'a': case 'A': hexValue =10*16; break;
				case 'b': case 'B': hexValue =11*16; break;
				case 'c': case 'C': hexValue =12*16; break;
				case 'd': case 'D': hexValue =13*16; break;
				case 'e': case 'E': hexValue =14*16; break;
				case 'f': case 'F':	hexValue =15*16; break;			
				default:
					throw new NumberFormatException("Broken % encoding in URI (16s value).");
				}
				state = State.URI_ESC_16S;
				break;
				
			case URI_ESC_16S:
				switch(current) {
				case '0':	break;
				case '1': hexValue += 1; break;
				case '2': hexValue +=2; break;
				case '3': hexValue +=3; break;
				case '4': hexValue +=4; break;
				case '5': hexValue +=5; break;
				case '6': hexValue +=6; break;
				case '7': hexValue +=7; break;
				case '8': hexValue +=8; break;
				case '9': hexValue +=9; break;
				case 'a': case 'A': hexValue +=10; break;
				case 'b': case 'B': hexValue +=11; break;
				case 'c': case 'C': hexValue +=12; break;
				case 'd': case 'D': hexValue +=13; break;
				case 'e': case 'E': hexValue +=14; break;
				case 'f': case 'F':	hexValue +=15; break;			
				default:
					throw new NumberFormatException("Broken % encoding in URI (1s value).");
				}
				accumulator.append((char)hexValue);
				state = State.URI;
				break;				
			
			}
			//DO NOT EDIT BELOW
			current = bis.read();
			
		} // end while
			
		// This is bad.  We never finished the URI
		throw new URISyntaxException("Bad URI", "Could not parse.");
	}   
	
	// ==========================================================================================================
	// == TOOLS

}
