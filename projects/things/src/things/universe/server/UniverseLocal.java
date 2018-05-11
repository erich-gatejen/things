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
package things.universe.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.collections.bidimap.DualHashBidiMap;

import things.common.ThingsConstants;
import things.common.tools.FileTools;
import things.common.tools.TokenFactory;
import things.data.Accessor;
import things.data.ThingsPropertyView;
import things.thinger.SystemNamespace;
import things.universe.Universe;
import things.universe.UniverseException;
import things.universe.UniverseID;
import things.universe.UniverseObjectAccessor;

/**
 * Universe local.  Relative paths are NEVER allowed.
 * <p>
 * There are some places where lock races could occur.  For this simple server, it isn't much of a deal.  If they become a problem, I'll fix them later.  It's mostly
 * the time between a lock is checked and the operation is actually done, like in delete.
 * <p>
 * CONFIGS:<br>
 * CONFIG_PATH "path" = root path to universe<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
   EPG - New - 2 JUL 04
 * EPG - Add multi-Strings.  It's a bit messy now - 10 AUG 06 
 * </pre> 
 */
public class UniverseLocal implements Universe {

	// PRIVATE MEMBERS
	private String root;
	private UniverseID id;
	private DualHashBidiMap lockTable; 
	private HashMap<File, String> localTable;
	private TokenFactory	tokens = new TokenFactory(10, false, false);
	
	private final static int SIZE_TOKEN_KEY = 12;
	
	/**
	 *  Get the id for this universe.
	 *  @return the Id.
	 *  @see things.universe.UniverseID
	 */
	public UniverseID getId() throws UniverseException {
		return id;
	}
	
	/**
	 *  Get a validated name for the object.  This is valid for this universe ONLY.
	 * @param name universe object name (path components will be appended).
	 * @return the validated name as a single string.
	 * @throws things.universe.UniverseException
	 */
	public String getValidatedName(String... name) throws UniverseException {
		return validatePath(name);
	}
	
	/**
	 *  Get an object accessor for the named object.  This is valid for this universe ONLY.
	 * @param name universe object name (path components will be appended).
	 * @return the validated name as a single string.
	 * @throws things.universe.UniverseException
	 */
	public Accessor getObjectAccessor(String... name) throws UniverseException {
		Accessor result = null;
		try {
			result =  new UniverseObjectAccessor(this, name);
		} catch (UniverseException ue) {
			throw ue;
		} catch (Throwable t) {
			throw new UniverseException("FAULT while creating accessor.",UniverseException.UNIVERSE_FAULT_ACCESSOR_PROBLEM, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,validatePath(name));
		}
		return result; 
	}

	/**
	 *  Get an InputStream that can read from the universe object
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public InputStream getStream(String... name) throws UniverseException {
		String vname = validatePath(name);
	    if (vname==null) 
	    if (!exists(vname)) throw new UniverseException("Universe object does not exist.",UniverseException.UNIVERSE_ERROR_OBJECT_DOESNT_EXIST, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		if (privileged_isLocked(vname)) throw new UniverseException("Universe object is locked; cannot delete().",UniverseException.UNIVERSE_ERROR_OBJECT_LOCKED, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
	
		return privileged_getStream(vname);		
	}
	
	/**
	 * Get an InputStream that can read from the universe object.
	 * This will unlock the object so the write can occur.  If the key is bad, it will throw an exception. 
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public InputStream getStreamByKey(String key) throws UniverseException {
		String vname = this.getNameFromKey(key);
		return privileged_getStream(vname);		
	}
	
	/**
	 * Private method for getStream.  It will not check locks.
	 * @param vname Validated object name.
	 * @return The InputStream.  It will not be buffered.
	 * @throws things.universe.UniverseException
	 */ 
	private InputStream privileged_getStream(String vname) throws UniverseException  {
		
		// Do it.
		FileInputStream tempFIS = null;
		try {
			tempFIS = new FileInputStream(root + ThingsConstants.PATH_SEPARATOR + vname);

		} catch (Throwable e) {
			// Every other exception should be consider an IO error caused by the underlying system.
			throw new UniverseException(
					"IO Error on object.",
					UniverseException.UNIVERSE_ERROR_IO_PROBLEM, e,SystemNamespace.ATTR_PLATFORM_MESSAGE,e.getMessage(), SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname);
		}
		return (InputStream) tempFIS;
	}
	
	/**
	 * Get an OutputStream that can write to the universe object.
	 * If will replace the object if one is already there.
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStream(String... name) throws UniverseException  {
		String vname = validatePath(name);
		if (isLocked(vname)) throw new UniverseException("Universe object is locked; cannot replace with a putStream().",UniverseException.UNIVERSE_ERROR_OBJECT_LOCKED, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		return privileged_putStream(vname);
	}
	
	/**
	 * Get an OutputStream that can write to the universe object.  If will replace the object if one is already there.
	 * This will unlock the object so the write can occur.  If the key is bad, it will throw an exception. 
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamByKey(String key) throws UniverseException {
		String name = this.getNameFromKey(key);
		return privileged_putStream(name);	
	}

	/**
	 * Private method for putStream. 
	 * @param vname Validated object name.
	 * @return The OutputStream.  It will not be buffered.
	 * @throws things.universe.UniverseException
	 */ 
	private OutputStream privileged_putStream(String vname) throws UniverseException  {
		
		// Do it.
		FileOutputStream tempFOS = null;
		try {
			File target = FileTools.makeFile(root + ThingsConstants.PATH_SEPARATOR + vname);
			tempFOS = new FileOutputStream(target);

		} catch (Throwable e) {
			// Every other exception should be consider an IO error caused by the underlying system.
			throw new UniverseException(
					"IO Error on object.",
					UniverseException.UNIVERSE_ERROR_IO_PROBLEM, e,SystemNamespace.ATTR_PLATFORM_MESSAGE,e.getMessage(), SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname);
		}
		return (OutputStream) tempFOS;
	}
	
	/**
	 *  Get an OutputStream that can write to the universe object.  
	 * 	If will append to the object if one is already there, otherwise
	 * it will create a new one.
	 * @param name universe object name (path components will be appended).
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamAppender(String... name) throws UniverseException {
		String vname = validatePath(name);
		if (isLocked(vname)) throw new UniverseException("Universe object is locked; cannot replace with a putStream().", UniverseException.UNIVERSE_ERROR_OBJECT_LOCKED, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		return privileged_putStreamAppender(vname);
	}
	
	/**
	 * Get an OutputStream that can write to the universe object.  
	 * If will append to the object if one is already there, otherwise it will create a new one.
	 * This will unlock the object so the write can occur for this operation only.  If the key is bad, it will throw an exception.  
	 * @param key a valid key for the object
	 * @return a stream to the object
	 * @throws things.universe.UniverseException
	 */
	public OutputStream putStreamAppenderByKey(String key) throws UniverseException {
		String name = this.getNameFromKey(key);
		return privileged_putStreamAppender(name);			
	}
	
	/**
	 * Private method for putStream. 
	 * @param vname Validated object name.
	 * @return The OutputStream.  It will not be buffered.
	 * @throws things.universe.UniverseException
	 */ 
	private OutputStream privileged_putStreamAppender(String vname) throws UniverseException  {
		
		FileOutputStream tempFOS = null;
		try {
			File target = FileTools.appendFile(root + ThingsConstants.PATH_SEPARATOR + vname);
			tempFOS = new FileOutputStream(target, true);

		} catch (Exception e) {
			// Every other exception should be consider an IO error caused by the underlying system.
			throw new UniverseException(
					"IO Error on object.  object=" + vname,
					UniverseException.UNIVERSE_ERROR_IO_PROBLEM, e, SystemNamespace.ATTR_PLATFORM_MESSAGE,e.getMessage());
		}
		return (OutputStream) tempFOS;
	}
	
	/**
	 * Reserve unique object in the universe.  Guarantee it is a unique
	 * instance of it.  Great for temp objects.
	 * @param base base path for the object (including root object name)
	 * @return name of the reserved unique object.
	 * @throws things.universe.UniverseException
	 */
	public synchronized String reserveUnique(String base) throws UniverseException {
	    
		String name = base + "-" + System.currentTimeMillis() + tokens.rng.nextInt(100000);
		
		// Check to see if it is unique.  Since this is synchronized
		// any wait will guarantee the new name is unique.
		try {
			File target = new File(root + ThingsConstants.PATH_SEPARATOR + name);
			if (target.exists()) {
				this.wait(1);
				name = base + "-" + System.currentTimeMillis() + "-" + tokens.rng.nextInt(100000);
			}
		} catch (Exception io) {
			// Every other exception should be consider an IO error caused by the underlying system.
			throw new UniverseException(
					"IO Error on object.  base=" + base + " name=" + name,
					UniverseException.UNIVERSE_ERROR_IO_PROBLEM, io, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, name, SystemNamespace.ATTR_SYSTEM_OBJECT_BASE, base, SystemNamespace.ATTR_PLATFORM_MESSAGE,io.getMessage());	
		}
		return name;
	}
	
	/**
	 * Lock an object.  This is blocking.  It will return a key that can
	 * be used to unlock it.  Locks will prevent anyone from getting new
	 * access to an object.  However, if something is already access
	 * an object, a new lock will not effect it. 
	 * 
	 * @param name universe object name (path components will be appended).
	 * @return a key that can be used to unlock it.
	 * @throws things.universe.UniverseException
	 */
	public String lock(String... name) throws UniverseException {
		String result = null;
		
		// Validate
	    if (name==null) throw new UniverseException("Null universe object name.",UniverseException.UNIVERSE_FAULT_BAD_CALL);
		String vname = validatePath(name);
		
		// Is it already locked?
		if (isLocked(vname)) throw new UniverseException("Universe object is already locked.",UniverseException.UNIVERSE_ERROR_OBJECT_LOCKED,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		
		// Create a key.  Doesn't need to be particularly secure on a local system
		try {
			String newKey = new String(tokens.randomTokenBASE64(SIZE_TOKEN_KEY) + System.currentTimeMillis()); 
			synchronized(lockTable) {
				// A little danger of a race condition.  Something to futz with later.
			    lockTable.put(vname,newKey);
			}
			result = newKey;
		} catch (Exception e) {
		    throw new UniverseException("Unexpected problem while locking object.",UniverseException.UNIVERSE_FAULT_UNEXPECTED,e,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname,SystemNamespace.ATTR_PLATFORM_MESSAGE,e.getMessage());
		}
		return result;
	}
	
	/**
	 * Unlock an object using a key.  If the key doesn't unlock anything, nothing
	 * bad will happen (other than return false).
	 * @param key a key that can unlock it
	 * @return true if the object was found and unlocked, false if no matching key
	 * @throws things.universe.UniverseException
	 */
	public boolean unlock(String key) throws UniverseException {
	    // NOTE: the key is the value in the Map
	    boolean result = false;
		synchronized(lockTable) {
		    if (key==null) throw new UniverseException("Null universe object key.",UniverseException.UNIVERSE_FAULT_BAD_CALL);
		    if (lockTable.containsValue(key)) {
		        lockTable.removeValue(key);
		        result = true;
		    }
		}
		return result;
	}

	/**
	 * Check to see if the object is locked.  It'll throw an exception
	 * if the name is null.
	 * @param name universe object name (path components will be appended).
	 * @return true if the object is locked, otherwise false
	 * @throws things.universe.UniverseException
	 */
	public boolean isLocked(String... name) throws UniverseException {
		String vname = validatePath(name);
	    return privileged_isLocked( vname );
	}

	/**
	 * Check to see if the object is locked.
	 * @param vname Validated universe object name.  (Cannot be null!)
	 * @return true if the object is locked, otherwise false
	 * @throws things.universe.UniverseException
	 */
	private boolean privileged_isLocked(String vname) throws UniverseException {
	    boolean result = false;
	    synchronized(lockTable) {
		    if (lockTable.containsKey(vname)) result = true;
		}
		return result;
	}
	
	/**
	 * Make a local reference or copy of the object in a FILE.  The reference will be good until you
	 * release it.  It will persist past system shutdown, so if you don't release it, they can build up.
	 * If the object may or may not be directly modified by the File--assume either case.  However, when released, 
	 * it will allow modifications the original with the local copy (if it wasn't already).  Note the word "allow," it will not account for any
	 * unflushed data or dangling references.  So, be sure the local usage is DONE and flushed before releasing.  
	 * <p>
	 * This will lock the object until released.  No other operations will be allowed.
	 * <p> 
	 * This could be a good way to subvert security, if the implementation allows it.  It is up to the implementation
	 * to keep the world safe from local files.
	 * @param name universe object name (path components will be appended).
	 * @return a File
	 * @throws things.universe.UniverseException
	 */
	public File makeLocal(String... name) throws UniverseException {
		
		// Lock and reference
		String key = lock(name); 
		String vname = validatePath(name);
		File target = new File(root + ThingsConstants.PATH_SEPARATOR + vname);
		
		// Make sure the parent dir is there
		File parent = target.getParentFile();
		if ((parent != null)&&(!parent.exists())) {
			if (!parent.mkdirs()) throw new UniverseException("Could not ensure path for local", UniverseException.UNIVERSE_FAULT_LOCAL_PATH_MANAGEMENT, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		}
		
		// Remember and return
		localTable.put(target, key);
		return target;
	}

	/**
	 * Release a local reference or copy of the object in a FILE.  The original object will resemble the 
	 * local.
	 * @param theLocalFile the file.  If nonsensical, it will quietly return.
	 * @throws things.universe.UniverseException
	 */
	public void releaseLocal(File theLocalFile) throws UniverseException {
		
		// Qualify
		if (theLocalFile==null) return;
		
		// Remove from table and unlock.
		synchronized(localTable) {
			if (localTable.containsKey(theLocalFile)) {
				String key = localTable.get(theLocalFile);
				localTable.remove(theLocalFile)	;
				unlock(key);
			}
		}
	}
	
	/**
	 * Check to see if an object exists
	 * @param name universe object name (path components will be appended).
	 * @return true if the object exists, otherwise false
	 * @throws things.universe.UniverseException
	 */
	public boolean exists(String... name) throws UniverseException {

		// Validate
	    if (name==null) throw new UniverseException("Null universe object name.",UniverseException.UNIVERSE_FAULT_BAD_CALL);
		String vname = validatePath(name);
		
		// Do it.
	    File thing =  new File(root + ThingsConstants.PATH_SEPARATOR + vname);
	    boolean result = false;
	    try {
	        if (thing.exists()) result = true;
	    } catch (SecurityException se) {
	        // Obfuscate files that cannot be accessed because of security.  Just
	        // pretend they are not there
	    }
		return result;
	}	
	
	/**
	 * Delete an object.  If the object has open streams, it will quietly fail.  It will
	 * not schedule it for later deletion.  It's up to you to verify and destroy it later.
	 * If the object is locked, you'll get an exception.
	 * @param name universe object name (path components will be appended).
	 * @throws things.universe.UniverseException
	 */
	public void delete(String... name) throws UniverseException {
		String vname = validatePath(name);
		if (privileged_isLocked(vname)) throw new UniverseException("Universe object is locked; cannot delete.",UniverseException.UNIVERSE_ERROR_OBJECT_LOCKED, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
		privileged_delete(vname);
	}
	
	/**
	 * Delete an object.  If the object has open streams, it will quietly fail.  It will
	 * not schedule it for later deletion.  It's up to you to verify and destroy it later.
	 * This will unlock the object and delete it.  If the key is bad, it will throw an exception.  
	 * @param key a key that can unlock it
	 * @throws things.universe.UniverseException
	 */
	public void deleteByKey(String key) throws UniverseException {
		String name = this.getNameFromKey(key);
		this.unlock(key);
		privileged_delete(name);
	}
	
	/**
	 * Delete the object.
	 * @param vname the validated path.  It must not be null!
	 * @throws UniverseException
	 */
	private void privileged_delete(String vname) throws UniverseException  {
	    try {
		    File thing =  new File(root + ThingsConstants.PATH_SEPARATOR + vname);
		    int tries = 5;
	        while( thing.exists() && !thing.delete() ) {
	              System.gc();
                  try
                  {
                    Thread.sleep(10);
                  } catch(InterruptedException ex) {}
                  tries--;
                  if (tries == 0) throw new Exception("Exceeded delete try.  File doesn't want to die.");
	        }
	    } catch (Exception se) {
	        throw new UniverseException("Delete failed.",UniverseException.UNIVERSE_ERROR_DELETE_FAILED,se,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
	    }
	}
	
	/**
	 * Report the size object from the universe in bytes.  It will throw a UniverseException if the object doesn't
	 * exist or cannot be accessed.
	 * @param name universe object name (path components will be appended).
	 * @return the size or 0 if empty
	 * @throws things.universe.UniverseException
	 */
	public long size(String... name) throws UniverseException {
	    long result = 0;
		String vname = validatePath(name);

	    File thing =  new File(root + ThingsConstants.PATH_SEPARATOR + vname);
	    try {
	        if (!thing.isFile()) throw new UniverseException("Object does not exist.", UniverseException.UNIVERSE_ERROR_OBJECT_DOESNT_EXIST, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,vname);
	        result = thing.length();
	    } catch (UniverseException ue) {
	        throw ue;
	    } catch (Exception se) {
	        throw new UniverseException("Size failed.",UniverseException.UNIVERSE_ERROR_DELETE_FAILED,se,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
	    }
		return result;
	}	
	
	/**
	 * Get the last modified date in milliseconds from epoch time.  It will throw an exception if it can't find it.
	 * @param name universe object name (path components will be appended).
	 * @return milliseconds from epoch time.
	 * @throws things.universe.UniverseException
	 */
	public long lastModifiedDate(String... name) throws UniverseException	 {
	    long result = 0;
		String vname = validatePath(name);
		
	    if (name==null) throw new UniverseException("Null universe object name.",UniverseException.UNIVERSE_FAULT_BAD_CALL);
	    File thing =  new File(root + ThingsConstants.PATH_SEPARATOR + vname);
	    try {
	        if (!thing.isFile()) throw new UniverseException("Object does not exist.", UniverseException.UNIVERSE_ERROR_OBJECT_DOESNT_EXIST, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname);
	        result = thing.lastModified();
	    } catch (UniverseException ue) {
	        throw ue;
	    } catch (Exception se) {
	        throw new UniverseException("Get modified date failed.",UniverseException.UNIVERSE_ERROR_GET_MODIFIED_DATE_FAILED,se,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
	    }
		return result;
	}
	
	/**
	 * Get all the object names that match the path.  No wildcards are necessary.  The final path element will treated as a substring of all matches, if it doesn't match
	 * a path completely.  If it does match a path completely, all elements under that path (in the one ply only) will be given.
	 * @param name universe object name, partial name, and/or path (path components will be appended)
	 * @return all the matches.
	 * @throws things.universe.UniverseException
	 */
	public Collection<String> match(String... name) throws UniverseException {	
		Collection<String> result = null;
		String vname = validatePath(name);
		if (name==null) throw new UniverseException("Null universe object name.",UniverseException.UNIVERSE_FAULT_BAD_CALL);

	    try {
	    	
		    File ply =  new File(root + ThingsConstants.PATH_SEPARATOR + vname);	  
		    if (ply.isDirectory()) {
		    	// Match a directory!
	    		result = matcher(ply, null);
		    	
		    } else if (ply.isFile()) {
		    	// Matches a single object.
		    	LinkedList<String> resultList = new LinkedList<String>();
		    	resultList.add(vname);
		    	
		    } else {
		    	
		    	// Is a path here?
		    	int pivot = vname.lastIndexOf(ThingsConstants.PATH_SEPARATOR);
		    	if ( (pivot > 0)&&(pivot < vname.length())) {		    		
		    		result = matcher(new File(root + ThingsConstants.PATH_SEPARATOR + vname.substring(0, pivot)), vname.substring(pivot+1));
		    		
		    	} else {
		    		result = matcher(new File(root), vname.substring(pivot+1));
		    	}
		
		    }
	    	
	    } catch (Exception se) {
	        throw new UniverseException("Match failed.",UniverseException.UNIVERSE_ERROR_GET_MATCHES,se,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, vname, SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
	    }
		return result;
	
	}
	
	/**
	 * Activate a feature by name.
	 * 
	 * @param name name of the feature
	 * @param params parameters for the feature expressed as a view
	 * @return a response from the feature as a String
	 * @see things.data.ThingsPropertyView
	 * @throws things.universe.UniverseException
	 */
	public String feature(String name, ThingsPropertyView params) throws UniverseException {
		return null;
	}
	
	/**
	 * Activate a feature by name.  
	 * This will unlock the object so the feature can occur for this operation only.  If the key is bad, it will throw an exception.  
	 * @param key key to the object
	 * @param params parameters for the feature expressed as a view
	 * @return a response from the feature as a String
	 * @see things.data.ThingsPropertyView
	 * @throws things.universe.UniverseException
	 */
	public String featureByKey(String key, ThingsPropertyView params) throws UniverseException {
		return null;
	}

	// ===================================================================================================================
	// == IMPLEMENTATION SPECIFIC =======================================================================================
	
	/**
	 * This will always be called when the universe server is created.
	 * You should not call this directly.
	 * @param path immutable path to the root
	 * @param uid immutable id
	 * @see things.universe.UniverseID
	 * @throws things.universe.UniverseException
	 */
	public void genesis(String path, UniverseID  uid) throws UniverseException {
	    root = path;
	    id =  uid;
	    lockTable = new DualHashBidiMap();
	    
	    localTable = new HashMap<File, String>();
	}

	/**
	 * This will always be called when the universe is accessor is destroyed.
	 * You should not call this directly.
	 * @throws things.universe.UniverseException
	 */
	public void finalizeUniverse() throws UniverseException {
		
	}
	// ===================================================================================================================
	// == INTERNAL TOOLS  ================================================================================================
	
	/**
	 * Validate the full path and give a single string path. 
	 * @param path
	 * @return
	 * @throws UniverseException
	 */
	private String validatePath(String... path) throws UniverseException {
		String finalPath = ThingsConstants.EMPTY_STRING;
		
		// Is the path empty?
		if (path==null) throw new UniverseException("Null universe object name.",UniverseException.UNIVERSE_FAULT_BAD_CALL);
		
		// Validate input
		if (path.length<1) throw new UniverseException("Empty object name and/or path.", UniverseException.UNIVERSE_ERROR_EMPTY_NAME);
		
		// Forge the full path.  
		try {
			if (path.length == 1) {
				finalPath = ThingsConstants.PATH_SEPARATOR + path[0];
			} else {
				StringBuffer finalBuffer = new StringBuffer();
				for (int index =0 ;  index < path.length ; index++) {
					finalBuffer.append(ThingsConstants.PATH_SEPARATOR);
					finalBuffer.append(path[index]);
				}
				finalPath = finalBuffer.toString();
			}
		} catch (Throwable t) {
			 throw new UniverseException("Could not construct path to object name", UniverseException.UNIVERSE_FAULT_NAMING_FAILED, t);
		}
			
		// Validate final path
		if (FileTools.detectRelativePath(finalPath)) throw new UniverseException("Relative path detected in object name.  name="+path,UniverseException.UNIVERSE_ERROR_DISALLOWED_NAME);
	
		// Done
		return finalPath;
	}
	
	/**
	 * Private helper for key2name conversion.  This will be used by all ByKey methods.
	 * @param key the Key
	 * @return the vname
	 */
	private String getNameFromKey(String key) throws UniverseException {
		String name = null;
		synchronized (lockTable) {
		    if (lockTable.containsValue(key)) {
		    	name = (String)lockTable.getKey(key);
		    } else {
	    		throw new UniverseException("Given key does not refer to any object in this universe.",UniverseException.UNIVERSE_ERROR_BAD_KEY);		    	
		    }
		}
    	if ((name==null)||(name.length()<=0)) 
    		throw new UniverseException("Given key refers to a dead universe object.",UniverseException.UNIVERSE_ERROR_BAD_KEY,SystemNamespace.ATTR_SYSTEM_OBJECT_NAME,name);
    	return name;
	}
	
	/**
	 * A real ugly matcher.
	 * @param base
	 * @param name
	 * @return
	 */
	private Collection<String> matcher(File base, String name) {
		
		LinkedList<String> result = new LinkedList<String>();
		
		// Clean name .  Ugly.  sorry!
		String cleanName = null;
		if (name!=null) {
			switch (name.length()) {
			case 0:
				// Leave it null;
				break;
				
			case 1:
				if (name.charAt(0)!=ThingsConstants.PATH_SEPARATOR) {
					cleanName = name;
				} 
				// else leave null
				break;
				
			case 2:
				if (name.charAt(0)==ThingsConstants.PATH_SEPARATOR) {
					if (name.charAt(1)!=ThingsConstants.PATH_SEPARATOR) {
						cleanName = name.substring(1);
					}
					// else its a double path.  leave it null
					
				} else {
					if (name.charAt(1)==ThingsConstants.PATH_SEPARATOR) {
						cleanName = name.substring(0,1);
					} else {
						cleanName = name;	// not separators		
					}
				}
				
			default:
				// Get bookends.
				cleanName = name;
				if (cleanName.charAt(0)==ThingsConstants.PATH_SEPARATOR) {
					cleanName = cleanName.substring(1);
				}
				if (cleanName.charAt(cleanName.length()-1)==ThingsConstants.PATH_SEPARATOR) {
					cleanName = cleanName.substring(0, cleanName.length());
				}
			}
		}

		// Get 'em
		try {
			File checkFile;
			String[] names = base.list();
			if (names != null) {
				for (int index = 0; index < names.length ; index++) {
					if (names[index].indexOf(name) >= 0) {
						checkFile = new File(base.getAbsolutePath() + ThingsConstants.PATH_SEPARATOR + names[index]);
						if (checkFile.isFile()) result.add(names[index]);
					}
				}

			}
			
		} catch (Throwable t) {
			// Really, we don't care.  If it doesn't match, it doesn't match.
		}
		
		return result;
	}
	
}
