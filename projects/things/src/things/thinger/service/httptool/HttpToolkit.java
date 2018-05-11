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

import java.io.OutputStream;

import things.common.ThingsException;

/**
 * Common merge tags for pages.
* <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * </pre> 
 */
public class HttpToolkit {
		
	// ====================================================================================================
	// == DATA

	
	// ====================================================================================================
	// == METHODS
	
	/**
	 * Respond with HTTP.  I could speed this up significantly by using the stream escaping, but that's just too much for this
	 * little GUI.
	 * <p>
	 * @param page The page.
	 * @param bos output stream.
	 * @throws Throwable
	 */
	public static void respondWithPage(String  page, OutputStream bos) throws Throwable {
		respondWithPage(page, bos, "200 OK");
	}	
	
	/**
	 * Respond with HTTP.  I could speed this up significantly by using the stream escaping, but that's just too much for this
	 * little GUI.
	 * <p>
	 * @param page The page.
	 * @param bos output stream.
	 * @param status status string for HTTP.  It should be the code, whitespace, and then test.  For example: 200 OK
	 * @throws Throwable
	 */
	public static void respondWithPage(String  page, OutputStream bos, String status) throws Throwable {
		
		// Write the page
		bos.write(("HTTP " + status + "\n").getBytes());
		bos.write("Server: HttpCommandService\n".getBytes());
		bos.write("Content-Type: text/html\n".getBytes());
		bos.write(("Content-Length: " + page.length() + "\n").getBytes());
		//bw.write("Accept-ranges: bytes\n");
		bos.write("\n".getBytes());
		bos.write(page.getBytes());
        try { 
        	Thread.sleep(100); 
        } catch (Exception e) {
        	// dont care
        }
	}	

	/**
	 * Respond to an HTTP HEAD.
	 * <p>
	 * @param headResult The head result.
	 * @param bos output stream.
	 * @throws Throwable
	 */
	public static void respondWithHead(Head  headResult, OutputStream bos) throws Throwable {
		
		// Write the page
		bos.write("HTTP 200 OK\n".getBytes());
		bos.write("Server: HttpCommandService\n".getBytes());
		bos.write(headResult.renderFields().getBytes());
		bos.write("Content-Type: text/html\n".getBytes());
		//bw.write("Accept-ranges: bytes\n");
		bos.write("\n".getBytes());
        try { 
        	Thread.sleep(100); 
        } catch (Exception e) {
        	// dont care
        }
	}	
	
	/**
	 * Make a feeble attempt to reply.  This should be used for errors only!  All exceptions are ignored.
	 * @param message what to say.
	 * @param destination where to write it.
	 */
	public static void feebleReply(String message, OutputStream destination) {
		feebleReply(message, destination, "200 OK");
	}

	/**
	 * Make a feeble attempt to reply.  This should be used for errors only!  All exceptions are ignored.
	 * @param message what to say.
	 * @param destination where to write it.
	 * @param status status string for HTTP.  It should be the code, whitespace, and then test.  For example: 200 OK
	 */
	public static void feebleReply(String message, OutputStream destination, String status) {
		try {
			StringBuffer page = new StringBuffer();
			page.append("<h1>ERROR - Server could not repond</h1><p><pre>\n\r");
			page.append(message);
			page.append("</pre><p>See server logs.");
			
			HttpToolkit.respondWithPage(page.toString(), destination, status);
			
		} catch (Throwable t) {
			// Indeed, we are feeble.
		}
	}
	
	/**
	 * Make a string HTML friendly.
	 * <p>
	 * @param in the string to convert.
 	 * @return the HTML friendly version.
	 * @throws Throwable
	 */
    public static String htmlString(String in) throws Throwable {

        StringBuffer result = new StringBuffer();
        try {
            int length = in.length();
            char working;
            for (int index = 0; index < length; index++) {
                working = in.charAt(index);
                switch (working) {
                 case '&':
                	 result.append("&amp;");
                    break;     
                case '>':
                	result.append("&gt;");
                    break;
                case '<':
                	result.append("&lt;");
                    break;                    
         
                default:
                	result.append(working);
                    break;
                }
            }

        } catch (Throwable t) {
           throw new ThingsException("Failed to convert String to http friendly.", ThingsException.SERVICE_HTTPTOOL_CONVERSION_ERROR, t);
        }
        return result.toString();
    }

	
	
}
