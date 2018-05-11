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

import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;

/**
 * Headers found in requests and/or responses.<p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 FEB 07
 * </pre> 
 */
public class HttpHeaders {

	// =========================================================================================
	// CONSTANT DATA
	public final static int CODE_CONTINUE = 100;
	public final static int CODE_OK = 200;	
	
	// =========================================================================================
	// PUBLIC DATA 
	
	/**
	 * The Server.
	 */
	public String 	server;
	
	/**
	 * The Path.
	 */
	public String 	path;
	
	/**
	 * Code text.
	 */
	public String 	codeText;
	
	/**
	 * The transfer encoding.
	 */
	public String 	transferEncoding;
	
	/**
	 * Header items.  Note that contentLength and contentType should not be included, as they are
	 * already processed.
	 */
	public ThingsPropertyView 	headerItems;

	/**
	 * Request body values.
	 */
	public ThingsPropertyView 	bodyValues;
	
	/**
	 * Request URL  values.
	 */
	public ThingsPropertyView 	urlValues;
	
	/**
	 * Content length.
	 */
	public long					contentLength;
	
	/**
	 * Content type.
	 */
	public String				contentType;
	
	/**
	 * HTTP version.
	 */
	public String				httpVersion;
	
	// =========================================================================================
	// INTERNAL DATA 

	
	// =========================================================================================
	// CONSTRUCTOR
	
	public HttpHeaders() {
		headerItems = new ThingsPropertyTreeBASIC();
		urlValues = new ThingsPropertyTreeBASIC();
		bodyValues = new ThingsPropertyTreeBASIC();
	}

}


