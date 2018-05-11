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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.universe.Universe;

/**
 * Session for a proxy session.  It connects connections/contexts.
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public class ProxySession {
	
	// ===================================================================================================
	// DATA
	private Universe universe;
	private String root;
	
	private PrintWriter pw;
	
	// ===================================================================================================
	// EXPOSED DATA
	public String sessionName;
	
	/**
	 * The last time this session was touched.
	 */
	public long	lastAccess;

	// ===================================================================================================
	// METHODS


	/**
	 * Create the session.
	 * @param sessionName
	 * @param universe
	 * @param root
	 * @throws Throwable
	 */
	public ProxySession(String sessionName, Universe universe, String root) throws Throwable {
		try {
			
			// Data
			this.universe = universe;
			this.root = root;
			this.sessionName = sessionName;
			
			// Services
			pw = new PrintWriter(new BufferedOutputStream(universe.putStream(root, sessionName, "log")), true);		
			
		} catch (Throwable t) {
			throw new ThingsException("Could set up a new session.", ThingsException.PROXY_FAULT_SESSION, t, ThingsNamespace.ATTR_ID, sessionName);
		}
	}

	/**
	 * Dispose this session.  It is done.
	 */
	public synchronized void dispose() {
		// Use pw as a flag--null means it has already been disposed.
		if (pw != null) {
			try {
				pw.close();
			} catch (Throwable tt) {
			}
			pw = null;
		}
	}
	
	// ===================================================================================================
	// SERVICES
	
	/**
	 * Post a single line to the log.  It will be terminated with a platform friendly line separator.
	 * This is synchronized to make sure contexts dont' clobber each other.
	 * @param message the message.  
	 * @throws Throwable for IO problems.  
	 */
	public synchronized void POST(String message) throws Throwable {
		pw.println(message);
	}
		
	/**
	 * Get an output stream to a new file, given the name.  It will be buffered, if necessary.
	 * @param name the name.  It should be unique.
	 * @return the output stream.
	 * @throws Throwable
	 */
	public OutputStream GET_FILE_OUTPUT(String name) throws Throwable {
		return new BufferedOutputStream(universe.putStream(root, sessionName, name));
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL

	/**
	 * The finalizer.  Make sure it was disposed.  This isn't necessary, but I'm hoping to hurry up closing of everything.
	 */
	protected void finalize() throws Throwable {
		dispose();
	}
}
