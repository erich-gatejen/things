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
package things.thinger.kernel;

import things.thing.MODULE;
import things.thing.THING;
import things.thinger.SystemException;
import things.universe.Universe;

/**
 * A THINGS loader interface.   
 * <p>
 * What a 'path' means is up to the inplementation.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 8 MAR 07
 * </pre> 
 */
public interface Loader {
	
	/**
	 * Initialize the loader.  It will clear out any previous configuration, including the added sources.
	 * It should be harmless to do this whenever.
	 * @param cacheUniverse The universe in which to cache.
	 * @param cacheRoot The root for cached items.
	 * @throws SystemException
	 */
	public void init(Universe   cacheUniverse,   String  cacheRoot) throws SystemException;
	
	/**
	 * Purge the loader.  Any cached things will be dumped.
	 * @throws SystemException
	 */
	public void purge() throws SystemException;
	
	/**
	 * Purge a specific thing from the loader.  If cached, it will be cleared.  
	 * This will never cause an error, unless there is an init() problem.
	 * @param path the path to the item to purge.
	 * @throws SystemException
	 */
	public void purgeThing(String path) throws SystemException;

	/**
	 * Add a source universe.
	 * @param sourceUniverse The universe from where to load items.
	 * @param root The root within the universe.
	 * @throws SystemException
	 */
	public void addSource(Universe   sourceUniverse,  String  root) throws SystemException;
	
	/**
	 * Load a THING class.
	 * @param path to the THING.
	 * @return A class for that thing.
	 * @throws SystemException
	 * @see things.thing.THING
	 */
	public Class<THING> loadThing(String path) throws SystemException;
	
	/**
	 * Load a MODULE class.
	 * @param path the path to the MODULE.  Depends on the loader being used, but typically it's a classpath.
	 * @return A class for that module.
	 * @throws SystemException
	 * @see things.thing.MODULE
	 */
	public Class<MODULE> loadModule(String path) throws SystemException;
	

}
