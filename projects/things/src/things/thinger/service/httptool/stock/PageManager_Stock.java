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
package things.thinger.service.httptool.stock;

import java.io.BufferedInputStream;

import things.common.ThingsException;
import things.common.tools.StreamTools;
import things.data.ThingsPropertyReaderToolkit;
import things.thinger.service.ServiceConstants;
import things.thinger.service.httptool.Page;
import things.thinger.service.httptool.PageManager;
import things.universe.Universe;
import things.universe.UniverseAddress;

/**
 * This is a stock implementation of the page manager.
 * <p>
 * All pages load from pages/ under the system universe.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 07
 * </pre> 
 */
public class PageManager_Stock extends PageManager  {
	
	// =================================================================================================
	// == DATA

	private Universe  pageUniverse;
	private String 	  root;
	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	// == IMPLEMENTED FOR GENERAL USE  ===================
	
	/**
	 * Get the error page name.
	 * @return the name of the error page.
	 */
	public String getErrorPageName() {
		return StockConstants.PAGE_ERROR;
	}
	
	// == IMPLEMENTED FOR INTERNAL USE ===================
	
	/**
	 * Load a page by name.  This will not come from the cache.  Use get() if you want the cache.  If it could not be found, it will
	 * return null.
	 * Most user applications should use get() instead.
	 * @param pageName the name of the action.
	 * @return the page or null if it could not be found.
	 * @throws Throwable for any problem (except if it couldn't be found).  This is usually fatal.
	 */
	public Page load(String  pageName) throws Throwable {
		
		Page result = null;
		
		// Need to load it.
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(pageUniverse.getStream(root, pageName));
			String pageData = StreamTools.loadStream2String(bis);
		
			// Do last!
			result = new Page(pageName,pageData);
	
		} catch (Throwable t) {
			// Let the null return.  We don't do anything fancy.
		} finally {
			try {
				bis.close();
			} catch (Throwable t) {
				// Best effort.
			}
		}
		return result;
	}
	
	/**
	 * The initialization chain.  This will be called when the system inits the manager.  It gives the base class
	 * a chance to init.  The si (SystemInterface) will be set and usable.  The system may call this more than once.  If it
	 * does, the state should be reset and the initialization done again.
	 * @throws ThingsException for any problem.
	 */
	public void init_chain() throws ThingsException {
		
		// Set the page source.
		try {
			ThingsPropertyReaderToolkit propToolkit = new ThingsPropertyReaderToolkit(si.getLocalProperties());
			UniverseAddress uAddy = new UniverseAddress(propToolkit.getRequired(ServiceConstants.PAGE_MANAGER_UNIVERSE_ADDRESS));
			pageUniverse = si.getUniverse(uAddy.universeName);
			root = uAddy.path;
			
		} catch (Throwable t) {
			throw new ThingsException("Could not find or prepare for stock pages", ThingsException.SERVICE_FAULT_HTTPTOOL_STOCK_SETUP, t);
		}
	}
	

}
