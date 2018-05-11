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

import java.io.InputStream;

/**
 * An HTTP response. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 07
 * </pre> 
 */
public class HttpResponse extends HttpHeaders {

	// =========================================================================================
	// PUBLIC DATA 
	
	/**
	 * The busy stream.  It may be a processed stream to deal with things like chunking.
	 */
	public InputStream 	bodyStream;
	
	/**
	 * The busy stream.  It may be a processed stream to deal with things like chunking.
	 */
	public int 	code;
	
	/**
	 * The reason phrase.
	 */
	public String reasonPhrase;
	
	/**
	 * Always drain flag.  Regardless of the content length, always drain the body.
	 */
	public boolean alwaysDrain;
	
	// =========================================================================================
	// INTERNAL DATA 
	

	// =========================================================================================
	// CONSTRUCTOR
	
	public HttpResponse() {
		super();
	}

}


