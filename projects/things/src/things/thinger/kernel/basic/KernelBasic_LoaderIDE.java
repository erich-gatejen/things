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
package things.thinger.kernel.basic;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.Verbose;
import things.thing.MODULE;
import things.thing.THING;
import things.thinger.SystemException;
import things.thinger.kernel.Loader;
import things.universe.Universe;

/**
 * A THINGS loader implementation for KernelBasic.  This one is meant for IDE usage, so you can let the system classloader 
 * find things for you.  It makes debugging a lot better.  Of course, you lose the recompile on the fly feature.   
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i><br>

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 8 JUL 07
 * </pre> 
 */
public class KernelBasic_LoaderIDE implements Loader, Verbose {
	
	// ===========================================================================================
	// INTERNAL DATA
	
	
	// ===========================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 */
	//public KernelBasic_LoaderIDE() throws SystemException {
	//}
	
	/**
	 * Initialize the loader.  It will clear out any previous configuration, including the added sources.
	 * It should be harmless to do this whenever.
	 * @param cacheUniverse The universe in which to cache.
	 * @param cacheRoot The root for cached items.
	 * @throws SystemException
	 */
	public void init(Universe   cacheUniverse,   String  cacheRoot) throws SystemException {
		// Don't care
	}
	
	/**
	 * Add a source universe.
	 * @param sourceUniverse The universe from where to load items.
	 * @param sourceRoot The root within the universe.
	 * @throws SystemException
	 */
	public void addSource(Universe   sourceUniverse,  String  sourceRoot) throws SystemException {
		// NOP
	}
	
	/**
	 * Purge the loader.  Any cached things will be dumped.  This affects the internal cache only.  The compiled cache in the system
	 * universe is left alone.
	 * @throws SystemException
	 */
	public synchronized void purge() throws SystemException {
		// NOP
	}
	
	/**
	 * Purge a specific thing from the loader.  If cached, it will be cleared. This affects the internal cache only.  The compiled cache in the system
	 * universe is left alone.  
	 * This will never cause an error, unless there is an init() problem.
	 * @throws SystemException
	 */
	public synchronized void purgeThing(String path) throws SystemException {
		// NOP
	}
	
	/**
	 * Load a THING class.  It will take the cached version first.
	 * @param name to the THING.
	 * @return A class for that thing.  This is the binary name and it should reside in one of the registered 
	 * @throws SystemException
	 * @see things.thing.THING
	 */
	@SuppressWarnings("unchecked")
	public synchronized Class<THING> loadThing(String name) throws SystemException {
		
		// Let the 
		Class<THING> result = null ;
		
		try {		
			// Let the classpath find it.
			result = (Class<THING>)Class.forName(name);

		} catch (Throwable  t) {
			throw new SystemException("Loader failed to load THING (IDE).", SystemException.SYSTEM_LOADER_ERROR_COULD_NOT_LOAD, t, ThingsNamespace.ATTR_THING_NAME, name);
		} 
		return result;
	}
	
	/**
	 * Load a MODULE class.
	 * @param name the path/name to the MODULE.  Depends on the loader being used, but typically it's a classpath.
	 * @return A class for that module.
	 * @throws SystemException
	 * @see things.thing.MODULE
	 */
	@SuppressWarnings("unchecked")
	public Class<MODULE> loadModule(String name) throws SystemException {
		// Let the 
		Class<MODULE> result = null ;
		
		try {		
			// Let the classpath find it.
			result = (Class<MODULE>)Class.forName(name);

		} catch (Throwable  t) {
			throw new SystemException("Loader failed to load MODULE (IDE).", SystemException.SYSTEM_LOADER_ERROR_COULD_NOT_LOAD, t, ThingsNamespace.ATTR_THING_NAME, name);
		} 
		return result;
	}
	
	// ===========================================================================================
	// TOOLS
	
	// ===========================================================================================
	// VERBOSE IMPLEMENTATION
	
	// My poster.
	private StringPoster myPoster;
	
	/**
	 * Turn on.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
    	myPoster = poster;
    }
	/**
	 * Turn off the verbose mode.
	 */
	public void verboseOff() {
		myPoster = null;
	}
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation may find a 
	 * way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message) {
		if (myPoster!=null)	myPoster.postit(message);
	}
	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose() {
		if (myPoster == null) return false;
		return true;
	}


}
