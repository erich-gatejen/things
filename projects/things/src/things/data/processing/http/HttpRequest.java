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

/**
 * An HTTP request.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 12 FEB 07
 * </pre> 
 */
public class HttpRequest extends HttpHeaders {

	// =========================================================================================
	// PUBLIC DATA 
	
	public enum Method {
		POST, 
		GET,
		HEAD,
		UNSUPPORTED;
		
		/**
		 * Lookup the method.  It is very forgiving and is not case sensitive.<br>
		 * @param methodText
		 * @return The method.
		 */
		public static Method match(String methodText) {
			Method result = UNSUPPORTED;
			try {
				result = Method.valueOf(methodText.toUpperCase());
			} catch (Throwable t) {
			}
			return result;
		}
	}
	
	/**
	 * The HTTP method.
	 */
	public Method 	method;
	
	/**
	 * The HTTP method text -- for debugging.
	 */
	public String 	methodText;
	
	// =========================================================================================
	// INTERNAL DATA 

	
	// =========================================================================================
	// CONSTRUCTOR
	
	public HttpRequest() {
		super();
		method = Method.UNSUPPORTED;
	}

}


