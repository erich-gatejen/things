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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import things.common.ThingsException;
import things.data.Accessor;
import things.thinger.SystemNamespace;

/**
 * This is a cheap access for a file.  Only one access allowed at a time!  It will lock the object.
 * <p>
 * You are at the mercy of the implementation in terms of multi-threading.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 JAN 05
 * </pre> 
 */
public class UniverseObjectAccessor implements Accessor {
	
	// ============================================================================================================
	// = DATA
	
	private String universeObjectName;
	Universe theUniverse;
	private InputStream  workingIS;
	private String 		 universeLock;
	private OutputStream workingOS;
	
	// ============================================================================================================
	// = METHODS
	
	/**
	 * Constructor.
	 * @param name name of the universe object.
	 * @param theUniverse the universe in which it exists.
	 */
	public UniverseObjectAccessor(Universe theUniverse, String... name) throws ThingsException {
		// Qualify
		if (name==null) throw new ThingsException("Cannot create UniverseObjectAccessor with null file.", ThingsException.ACCESS_ERROR_NULL_ITEM);
		if (theUniverse==null) throw new ThingsException("Cannot create UniverseObjectAccessor with null theUniverse.", ThingsException.ACCESS_ERROR_NULL_ITEM);	

		// Validate name and make sure it is there.
		universeObjectName = theUniverse.getValidatedName(name);
		this.theUniverse = theUniverse;
	}

	// ============================================================================================================
	// = ACCESSOR IMPLEMENTATION
	
    /**
     * Open the item for read. You can assume it is buffered.
     * @return return an input stream for the item.  It will never return null; problems will cause exceptions.
     * @throws ThingsException for any problem.
     */
    public InputStream openForRead() throws ThingsException {
    	if ((workingIS!=null)||(workingOS!=null)) throw new ThingsException("Already opened.", ThingsException.ACCESS_ERROR_CONCURRENT_NOT_ALLOWED);
    	
    	InputStream result = null;
    	try {
    		universeLock = theUniverse.lock(universeObjectName);
    		result = new BufferedInputStream(theUniverse.getStreamByKey(universeLock));
    		workingIS = result;
    		
    	} catch (Throwable t) {
    		throw new ThingsException("Could not open.", ThingsException.ACCESS_ERROR_CANNOT_OPEN, t, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, universeObjectName);
    	}
    	return result;
    }
    
    /**
     * Declare you are done reading from the input stream.  It is a good idea to call this, though it shouldn't be fatal if you don't.
     * @param ios the stream you were given when you openForRead().  You may close the stream already, if you want.
     * @throws ThingsException for any problem.
     */
    public void doneWithRead(InputStream ios) throws ThingsException {
    	if (workingIS==null) return;  // Nothing open, so just quietly return.
    	if (ios!=workingIS) throw new ThingsException("Not the opened stream.",  ThingsException.ACCESS_ERROR_STREAM_NOT_RECOGNIZED);
    	
    	// Make sure it is closed.  
    	try {
    		ios.close();
    	} catch (Throwable t) {
    		// Don't really care.
    	}
  
    	// Unlock.  
    	try {
    		theUniverse.unlock(universeLock);
    	} catch (Throwable t) {
    		// TODO: put something here or we can have undetected leaks.
    	}
    	
    	workingIS = null;
    }
    
    /**
     * Open the item for writing. You can assume it is buffered.
     * @return return an output stream for the item.  It will never return null; problems will cause exceptions.
     * @throws ThingsException for any problem.
     */
    public OutputStream openForWrite() throws ThingsException {
    	if ((workingIS!=null)||(workingOS!=null)) throw new ThingsException("Already opened.", ThingsException.ACCESS_ERROR_CONCURRENT_NOT_ALLOWED);
    	
    	OutputStream result = null;
    	try {
    		universeLock = theUniverse.lock(universeObjectName);
    		result = new BufferedOutputStream(theUniverse.putStream(universeObjectName));
    		workingOS = result;
    		
    	} catch (Throwable t) {
    		throw new ThingsException("Could not open.", ThingsException.ACCESS_ERROR_CANNOT_OPEN, t, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, universeObjectName);
    	}
    	return result;
    }
    
    /**
     * Declare you are done reading from the input stream.  It is a good idea to call this, though it shouldn't be fatal if you don't.
     * @param oos the stream you were given when you openForWrite().  You may flush and close the stream already, if you want.
     * @throws ThingsException for any problem.
     */
    public void doneWithWrite(OutputStream oos) throws ThingsException {
    	if (workingOS==null) return;  // Nothing open, so just quietly return.
    	if (oos!=workingOS) throw new ThingsException("Not the opened stream.",  ThingsException.ACCESS_ERROR_STREAM_NOT_RECOGNIZED);
    	
    	// Make sure it is closed.  
    	try {
    		oos.close();
    	} catch (Throwable t) {
    		// Don't really care.
    	}
    	
    	// Unlock.  
    	try {
    		theUniverse.unlock(universeLock);
    	} catch (Throwable t) {
    		// TODO: put something here or we can have undetected leaks.
    	}
    	
    	workingOS = null;
    }
    
}
