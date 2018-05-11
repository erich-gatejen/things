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

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.tools.Rendezvous;
import things.thinger.SystemException;

/**
 * A specific thread.
 * <p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public class ProxyServiceThread extends Thread {
	
	// ===================================================================================================
	// EXPOSED DATA
	public Rendezvous<ProxyServiceContext> handoff;
	
	// ===================================================================================================
	// INTERNAL DATA
	ProxyServiceContext currentContext;

	// ===================================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 */
	public ProxyServiceThread() {
		handoff = new Rendezvous<ProxyServiceContext>();
	}

	/**
	 * Run method.
	 */
	public void run() {
		boolean running = true;
		
		while (running) {

			try {
				
				currentContext = handoff.enter();
				if (currentContext==null) throw new InterruptedException();		// Done.
				currentContext.process();
				
			} catch (InterruptedException e) {
				// time to die.
				running = false;
	
			} catch (Throwable t) {
	
				// Generally, don't care why.
				try {
					if (currentContext.logger.debuggingState())
						currentContext.logger.error("Connection died to exception.  message=" + t.getMessage(), ThingsCodes.PROXY_SESSION_ERROR, ThingsNamespace.ATTR_ID, currentContext.id.toString(), ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
					else
						currentContext.logger.error("Connection died to exception.  message=" + t.getMessage(), ThingsCodes.PROXY_SESSION_ERROR, ThingsNamespace.ATTR_ID, currentContext.id.toString());
				} catch (SystemException e) {
					handoff = null;
					throw new Error("Failed to log an error.  This is a very bad thing.", e);
				}
	
			} finally {
				currentContext.complete();
			}
			
		} // end while
	}
	
	// ==========================================================================================================
	// == PRIVATE AND INTERNAL
	
	
}
