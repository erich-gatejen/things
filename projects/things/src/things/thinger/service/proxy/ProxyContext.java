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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import things.common.ThingsException;
import things.common.WhoAmI;
import things.thinger.io.Logger;

/**
 * Context for a proxy session. 
 * <p>
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
public class ProxyContext {
	
	// ===================================================================================================
	// FIELDS
	
	public Socket uplink;
	public Socket downlink;
	public OutputStream uplinkOut;
	public InputStream uplinkIn;
	public OutputStream downlinkOut;
	public InputStream downlinkIn;	
	public Logger logger;
	public WhoAmI id;
	
	// ===================================================================================================
	// DATA
	private ProxySession session;
	private StringBuffer post;
	
	private final static String lineSeparator = System.getProperty("line.separator");
	
	// ===================================================================================================
	// METHODS


	/**
	 * Constructor.
	 * @param id
	 * @param session
	 * @param uplink
	 * @param downlink
	 * @param logger
	 * @throws Throwable
	 */
	public ProxyContext(WhoAmI id, ProxySession session, Socket uplink, Socket downlink, Logger logger) throws Throwable {
		try {
			this.id = id;
			this.session = session;
			this.uplink=uplink;
			uplinkIn = new BufferedInputStream(uplink.getInputStream());
			uplinkOut = new BufferedOutputStream(uplink.getOutputStream());
			this.downlink=downlink;
			downlinkIn = new BufferedInputStream(downlink.getInputStream());
			downlinkOut = new BufferedOutputStream(downlink.getOutputStream());
			this.logger = logger;
		} catch (Throwable t) {
			throw new ThingsException("Could not get streams for proxy context.", ThingsException.PROXY_FAULT_SOCKET_PREPARE, t);
		}
	}

	/**
	 * Dispose the context.  Everything will be flushed and closed.
	 */
	public synchronized void dispose() {
		if (uplink!=null) {
			try {
				uplinkOut.flush();		// Force it now.
			} catch (Throwable t) {
			}
			try {
				downlinkOut.flush();	// Force it now.
			} catch (Throwable t) {
			}
			try {
				uplink.close();
			} catch (Throwable t) {
			}
			try {
				downlink.close();
			} catch (Throwable t) {
			}
		}
		uplink=null;
		
	}
	
	// ===================================================================================================
	// SERVICES

	/**
	 * Get an output reference.  This will include the final name of the file as well as a stream to it.
	 * @param name
	 * @return the reference as a ProxyOutput object.
	 * @throws Throwable
	 */
	public ProxyOutput GET_OUTPUT(String name) throws Throwable {
		OutputStream ous = session.GET_FILE_OUTPUT(name);
		return new ProxyOutput(name, ous);
	}

	/**
	 * Post a messages.
	 * <p>
	 * This is a just a pass-through right now.  Depending on how the processors go, I might put common formatting services here.
	 * @param message
	 * @throws Throwable
	 */
	public void POST(String message) throws Throwable {
		session.POST(message);
	}
	
	/**
	 * Start a post.  This will be a level 1 entry.
	 * @param tokens unrelated tokens
	 * @throws Throwable
	 */
	public void POSTSTART(String... tokens) throws Throwable {
		post = new StringBuffer();
		for (int index = 0 ; index < tokens.length ; index++) {
			if (index>0) post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
			post.append(ProxyCodec.encodeString(tokens[index]));
		}
	}
	
	/**
	 * Post action tokens.  This will be a level 2 entry.
	 * @param tokens  unrelated tokens
	 * @throws Throwable Definitely will be thrown if POSTSTART not yet called.
	 */
	public void POSTACTION(String... tokens)  throws Throwable {
		post.append(lineSeparator);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);		// level 2
		for (int index = 0 ; index < tokens.length ; index++) {
			if (index>0) post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
			post.append(ProxyCodec.encodeString(tokens[index]));
		}
	}
	
	/**
	 * Post multiline of name/value pairs.
	 * @param token line token.
	 * @param pairs name/value pairs
	 * @throws Throwable Definitely will be thrown if POSTSTART not yet called or the number of strings passed in pairs is an odd number.
	 */
	public void POSTMULTI(String token, String... pairs)  throws Throwable {
		post.append(lineSeparator);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);		// level 3
		post.append(token);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
		for (int index = 0 ; index < pairs.length ; index+=2) {
			if (index>0) post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
			post.append(ProxyCodec.encodeString(pairs[index]));
			post.append('=');
			post.append(ProxyCodec.encodeString(pairs[index+1]));
		}
	}
	
	/**
	 * Post header name/value pairs
	 * @param token line token.
	 * @param name name
	 * @param value value
	 * @throws Throwable Definitely will be thrown if POSTSTART
	 */
	public void POSTSINGLE(String token, String name, String value)  throws Throwable {
		post.append(lineSeparator);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);		// level 3
		post.append(token);
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
		post.append(ProxyCodec.encodeString(name));
		post.append(ProxyCodec.POST_TOKEN_SEPARATOR);
		post.append(ProxyCodec.encodeString(value));
	}
		
	/**
	 * Done with the post.  Go ahead and send it.
	 * @throws Throwable
	 */
	public void POSTDONE() throws Throwable {
		POST(post.toString());
	}

	// ==========================================================================================================
	// == PRIVATE AND INTERNAL

	/**
	 * The finalizer.  Make sure it was disposed.
	 */
	protected void finalize() throws Throwable {
		dispose();
	}
}
