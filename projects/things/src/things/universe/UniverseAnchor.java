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
package things.universe;

import java.io.File;

import things.common.ThingsConstants;

/**
 * An anchor in a universe.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 SEP 07
 * </pre> 
 */
public class UniverseAnchor {
	
	/**
	 * The path to the root of the anchor.
	 */
	private String root;
	
	/**
	 * The universe is anchors.
	 */
	private Universe uni;
	
	/**
	 * Constructor.
	 * @param anchorRoot the path into the universe to the anchor spot
	 * @param theUniverse the universe in question
	 * @throws Throwable if either is null it'll throw an exception
	 */
	public UniverseAnchor(String anchorRoot, Universe theUniverse) throws Throwable {
		if ((anchorRoot==null)||(theUniverse==null)) UniverseException.softwareProblem("Cannot create a UniverseAnchor with null values.");
		root = anchorRoot;
		uni = theUniverse;
	}
	
	/**
	 * Does the object exist?
	 * @param path from the anchor.
	 * @return true if it does, otherwise false.
	 * @throws UniverseException
	 */
	public boolean hasObject(String path) throws UniverseException {
		return	uni.exists(root, path);
	}
	
	/**
	 * Resolve the path.  It will give a full path into the root of the universe.
	 * @param path from the anchor.
	 * @return the full path.
	 */
	public String resolvePath(String path) {
		return	root + ThingsConstants.PATH_SEPARATOR + path;
	}
	
	/**
	 * Get a local file for the object.  It will LOCK the object until released (releaseLocal).
	 * @param path
	 * @return return a reference to the object as a file.
	 * @throws UniverseException
	 */
	public File getLocal(String path) throws UniverseException {
		return	uni.makeLocal(root, path);
	}
	
	/**
	 * Get the universe itself.
	 * @return return the universe accessor.
	 * @throws UniverseException
	 */
	public Universe getUniverseAccessor() throws UniverseException {
		return	uni;
	}
	
	/**
	 * Release the local.  This will never throw an error.
	 * @param local
	 */
	public void releaseLocal(File local) {
		try {
			uni.releaseLocal(local);
		} catch (Throwable t) {
			//I hope this doesn't bite me later.
		}
	}		
}