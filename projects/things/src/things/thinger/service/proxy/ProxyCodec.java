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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Encode/decode proxy log data.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
/**
 * @author erich
 *
 */
public class ProxyCodec {
	
	// ===================================================================================================
	// FIELDS
	public final static char POST_TOKEN_SEPARATOR = '\t';
	public final static char POST_ESCAPE_CHARACTER = '\\';

	
	// ===================================================================================================
	// DATA
	
	// ===================================================================================================
	// METHODS

	/**
	 * Encode a string.  Null strings will write nothing.
	 * @param in The string to encode.
	 * @param out The target writer.
	 * @throws Exception Bad strings will cause exceptions.
	 */
	public static void encodeString(String in, Writer out) throws Exception {
		
		// validate
		if (in == null) return;
		
		// run it
		try {
			StringReader sin = new StringReader(in);
			int current = sin.read();
			while (current >=0) {
				switch(current) {
				case POST_TOKEN_SEPARATOR:
					out.append(POST_ESCAPE_CHARACTER);
					out.append('t');
					break;
				case POST_ESCAPE_CHARACTER:
					out.append(POST_ESCAPE_CHARACTER);
					out.append(POST_ESCAPE_CHARACTER);
					break;							
				default:
					out.write(current);
				}
				current = sin.read();
			}
			out.flush();
		} catch (Throwable t) {
			// Ignore encoding issues and dump it.
		} 
	}
	
	/**
	 * Encode a string.  Null strings will write nothing.
	 * @param in The string to encode.
	 * @throws Exception Bad strings will cause exceptions.
	 */
	public static String encodeString(String in) throws Exception {
		StringWriter sw = new StringWriter();
		encodeString(in, sw);
		return sw.toString();
	}
}
