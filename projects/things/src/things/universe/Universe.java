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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import things.data.Accessor;
import things.data.ThingsPropertyView;

/**
* Universe interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Adapted from another project - 16 MAY 04
 * EPG - Add Make-Local concept - 2 SEP 06
 * </pre> 
*/
public interface Universe {
	
	// CONFIGS
	public final static String CONFIG_PATH = "path";
	public final static String CONFIG_LIST = "list";
	public final static String CONFIG_ROOT = "root";
	public final static String CONFIG_TYPE = "type";
	public final static String CONFIG_TYPE_LOCAL = "local";
	public final static String CONFIG_NAME = "name";
	public final static String CONFIG_CLEARANCE = "clearance";
	
	/**
	 *  Get the id for this universe.
	 *  @return the Id.
	 *  @see things.universe.UniverseID
	 */
	public UniverseID getId() throws UniverseException;
	
	/**
	 *  Get a validated name for the object.  This is valid for this universe ONLY.
	 * @param name universe object name (path components will be appended).
	 * @return the validated name as a single string.
	 * @throws things.universe.UniverseException
	 */
	public String getValidatedName(String... name) throws UniverseException;
	
	/**
	 *  Get an object accessor for the named object.  This is valid for this universe ONLY.
	 * @param name universe object name (path components will be appended).
	 * @return the validated name as a single string.
	 * @throws things.universe.UniverseException
	 */
	public Accessor getObjectAccessor(String... name) throws UniverseException;
	
	/**
	 *  Get an InputStream that can read from the universe object
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public InputStream getStream(String... name) throws UniverseException;
	
	/**
	 * Get an InputStream that can read from the universe object.
	 * This will unlock the object so the write can occur.  If the key is bad, it will throw an exception. 
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public InputStream getStreamByKey(String key) throws UniverseException;	
	
	/**
	 * Get an OutputStream that can write to the universe object.
	 * If will replace the object if one is already there.
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStream(String... name) throws UniverseException;	
	
	/**
	 * Get an OutputStream that can write to the universe object.  If will replace the object if one is already there.
	 * This will unlock the object so the write can occur.  If the key is bad, it will throw an exception. 
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamByKey(String key) throws UniverseException;	
	
	/**
	 * Get an OutputStream that can write to the universe object.  
	 * If will append to the object if one is already there, otherwise it will create a new one.
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamAppender(String... name) throws UniverseException;	
	
	/**
	 * Get an OutputStream that can write to the universe object.  
	 * If will append to the object if one is already there, otherwise it will create a new one.
	 * This will unlock the object so the write can occur for this operation only.  If the key is bad, it will throw an exception.  
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamAppenderByKey(String key) throws UniverseException;
	
	/**
	 *  Reserve unique object in the universe.  Guarantee it is a unique
	 *  instance of it.  Great for temp objects.
	 * @param base base path for the object (including root object name)
	 * @return name of the reserved unique object.
	 * @throws things.universe.UniverseException
	 */
	public String reserveUnique(String base) throws UniverseException;
	
	/**
	 * Lock an object.  This is blocking.  It will return a key that can
	 * be used to unlock it.  Locks will prevent anyone from using the named item.
	 * @param name universe object name (path components will be appended).
	 * @return a key that can be used to unlock it.
	 * @throws things.universe.UniverseException
	 */
	public String lock(String... name) throws UniverseException;

	/**
	 * Unlock an object using a key for this operation only.  If the key doesn't unlock anything, nothing
	 * bad will happen (other than return false).
	 * @param key a key that can unlock it
	 * @return true if the object was found and unlocked, false if no matching key
	 * @throws things.universe.UniverseException
	 */
	public boolean unlock(String key) throws UniverseException;
	
	/**
	 * Check to see if the object is locked
	 * @param name universe object name (name components will be appended).
	 * @return true if the object is locked, otherwise false
	 * @throws things.universe.UniverseException
	 */
	public boolean isLocked(String... name) throws UniverseException;
		
	/**
	 * Make a local reference or copy of the object in a FILE.  The reference will be good until you
	 * release it.  It will persist past system shutdown, so if you don't release it, they can build up.
	 * If the object may or may not be directly modified by the File--assume either case.  However, when released, 
	 * it will allow modifications the original with the local copy (if it wasn't already).  Note the word "allow," 
	 * it will not account for any unflushed data or dangling references.  So, be sure the local usage is DONE and 
	 * flushed before releasing.  
	 * <p>
	 * This will lock the object until released.  No other operations will be allowed.
	 * <p> 
	 * This could be a good way to subvert security, if the implementation allows it.  It is up to the implementation
	 * to keep the world safe from local files.
	 * @param name universe object name (path components will be appended).
	 * @return a File
	 * @throws things.universe.UniverseException
	 */
	public File makeLocal(String... name) throws UniverseException;

	/**
	 * Release a local reference or copy of the object in a FILE.  The original object will resemble the 
	 * local.
	 * @param theLocalFile the file.  If nonsensical, it will quietly return.
	 * @throws things.universe.UniverseException
	 */
	public void releaseLocal(File theLocalFile) throws UniverseException;
	
	/**
	 * Check to see if an object exists
	 * @param name universe object name (path components will be appended).
	 * @return true if the object exists, otherwise false
	 * @throws things.universe.UniverseException
	 */
	public boolean exists(String... name) throws UniverseException;	
	
	/**
	 * Delete an object.  If the object has open streams, it will quietly fail.  It will
	 * not schedule it for later deletion.  It's up to you to verify and destroy it later.
	 * @param name universe object name (path components will be appended).
	 * @throws things.universe.UniverseException
	 */
	public void delete(String... name) throws UniverseException;
	
	/**
	 * Delete an object by Key.
	 * This will unlock the object so the delete can occur for this operation only.  If the key is bad, it will throw an exception.  
	 * @param key a key that can unlock it
	 * @throws things.universe.UniverseException
	 */
	public void deleteByKey(String key) throws UniverseException;
	
	/**
	 * Report the size object from the universe in bytes.
	 * @param name universe object name (path components will be appended).
	 * @return the size or 0 if empty
	 * @throws things.universe.UniverseException
	 */
	public long size(String... name) throws UniverseException;		
	
	/**
	 * Get the last modified date in milliseconds from epoch time.  It will throw an exception if it can't find it.
	 * @param name universe object name (path components will be appended).
	 * @return milliseconds from epoch time.
	 * @throws things.universe.UniverseException
	 */
	public long lastModifiedDate(String... name) throws UniverseException;		
	
	/**
	 * Get all the object names that match the path.  No wildcards are necessary.  The final path element will treated as a substring of all matches, if it doesn't match
	 * a path completely.  If it does match a path completely, all elements under that path (in the one ply only) will be given.
	 * @param name universe object name, partial name, and/or path (path components will be appended)
	 * @return all the matches.
	 * @throws things.universe.UniverseException
	 */
	public Collection<String> match(String... name) throws UniverseException;	
	
	/**	 * Activate a feature by name.
	 * @param name name of the feature
	 * @param params parameters for the feature expressed as a view
	 * @return a response from the feature as a String
	 * @see things.data.ThingsPropertyView
	 * @throws things.universe.UniverseException
	 */
	public String feature(String name, ThingsPropertyView params) throws UniverseException;

	/**
	 * Activate a feature by name.  
	 * This will unlock the object so the feature can occur for this operation only.  If the key is bad, it will throw an exception.  
	 * @param key key to the object
	 * @param params parameters for the feature expressed as a view
	 * @return a response from the feature as a String
	 * @see things.data.ThingsPropertyView
	 * @throws things.universe.UniverseException
	 */
	public String featureByKey(String key, ThingsPropertyView params) throws UniverseException;
	
	// == IMPLEMENTATION SPECIFIC ==========================
	
	/**
	 * This will always be called when the universe is accessor is destroyed.
	 * You should not call this directly.
	 * @throws things.universe.UniverseException
	 */
	public void finalizeUniverse() throws UniverseException;

}
