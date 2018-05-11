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

import java.io.OutputStream;

/**
 * Proxy output reference. 
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public class ProxyOutput {
	
	// ===================================================================================================
	// DATA
	private OutputStream ous;
	private String name;
	
	// ===================================================================================================
	// METHODS

	/**
	 * Constructor
	 * @param name The name.  It should be the final file/uobject name.
	 * @param ous A stream to it.
	 * @throws Throwable
	 */
	public ProxyOutput(String name, OutputStream ous) throws Throwable {
		this.name = name;
		this.ous = ous;
	}

	/**
	 * Get the output stream.
	 * @return the stream.
	 */
	public OutputStream getStream() {
		return ous;
	}
	
	/**
	 * Get the name.
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Annouce we are done with this output.
	 */
	public synchronized void done() {
		try {
			ous.close();
		} catch (Throwable t) { }
	}
	
	// ===================================================================================================
	// INTERNAL
	
	/**
	 * The finalizer.  Make sure it was closed.  I fear bugs in all the stream overlays.
	 */
	protected void finalize() throws Throwable {
		done();
	}
	
	
}
