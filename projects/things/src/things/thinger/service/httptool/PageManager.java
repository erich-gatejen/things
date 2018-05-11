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

import java.util.HashMap;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.thinger.SystemInterface;

/**
 * A base page manager.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * </pre> 
 */
public abstract class PageManager  {
	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	// == IMPLEMENTED FOR GENERAL USE  ===================
	
	/**
	 * Get the error page name.
	 * @return the name of the error page.
	 */
	public abstract String getErrorPageName();
	
	// == IMPLEMENTED FOR INTERNAL USE ===================
	
	/**
	 * Load a page by name.  This will not come from the cache.  Use get() if you want the cache.  If it could not be found, it will
	 * return null.
	 * Most user applications should use get() instead.
	 * @param pageName the name of the action.
	 * @return the page or null if it could not be found.
	 * @throws Throwable for any problem (except if it couldn't be found).  This is usually fatal.
	 */
	public abstract Page load(String  pageName) throws Throwable;
	
	/**
	 * The initialization chain.  This will be called when the system inits the manager.  It gives the base class
	 * a chance to init.  The si (SystemInterface) will be set and usable.  The system may call this more than once.  If it
	 * does, the state should be reset and the initialization done again.
	 * @throws ThingsException for any problem.
	 */
	public abstract void init_chain() throws ThingsException;
	
	// =================================================================================================
	// == INTERNAL DATA

	/**
	 * Cached classes.
	 */
	HashMap<String, Page>	pageImplementationCache;
	
	/**
	 * The system interface.  It will be set by the system before get is ever called.
	 */
	protected SystemInterface	si;

	// =================================================================================================
	// == METHODS=

	/**
	 * Constructor.  Makes good for template work.
	 */
	public PageManager() {
		pageImplementationCache = new HashMap<String, Page>();	
	}

	/**
	 * Initialize the manager.  This will be called by the system, so there is no need to do it yourself.
	 * @throws ThingsException by the init_chain if applicable.  The base will never throw it.
	 */
	public void init(SystemInterface	si) throws ThingsException {
		this.si = si;
		init_chain();
	}
	
	/**
	 * Get an page implementation.  It will get it from the cache.  If it isn't in the cache, it'll load it.  They are not pooled right now.  It will
	 * return null if it could not be found.
	 * @param pageName the name of the action.
	 * @return The page implementation or null if not found.
	 * @throws Throwable
	 */
	public synchronized Page get(String  pageName) throws Throwable {
		Page result = pageImplementationCache.get(pageName);
		if (result != null) return result;
		
		// Need to load it.
		try {
			result = load(pageName);
			
			// and cache it.
			pageImplementationCache.put(pageName, result);
			
		} catch (Throwable t) {
			throw new ThingsException("Could not load action.", ThingsException.SERVICE_HTTPTOOL_PAGE_LOAD_FAILED, ThingsNamespace.ATTR_PROCESSING_HTTP_PAGE, pageName);
		}
		return result;
	}
	
	
}
