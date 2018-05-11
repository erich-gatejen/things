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
package things.data.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import things.common.ThingsException;
import things.data.Accessor;

/**
 * This is a cheap access for a string.  You can read it all you want.  Writing dumps stuff in a 
 * ByteArrayOutputStream.  You can ask for it before you are doneWithWrite. 
 * <p>
 * You are at the mercy of the implementation in terms of multithreading.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 14 JAN 05
 * </pre> 
 */
public class StringAccessor implements Accessor {

	// ============================================================================================================
	// = DATA
	
	private String theString;
	private InputStream  workingIS;
	private ByteArrayOutputStream workingOS;
	
	// ============================================================================================================
	// = METHODS
	
	/**
	 * Constructor.
	 */
	public StringAccessor(String theString) throws ThingsException {
		if (theString==null) throw new ThingsException("Cannot create StringAccessor with null String.", ThingsException.ACCESS_ERROR_NULL_ITEM);
		this.theString = theString;
	}
	
	/**
	 * Get the output stream.  It'll be null if it isn't open for write.
	 * @return the output stream.
	 */
	public ByteArrayOutputStream getOutputStream() {
		return workingOS;
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
    		result = new ByteArrayInputStream(theString.getBytes());
    		workingIS = result;
    		
    	} catch (Throwable t) {
    		throw new ThingsException("Could not open.", ThingsException.ACCESS_ERROR_CANNOT_OPEN, t);
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
    	
    	workingIS = null;
    }
    
    /**
     * Open the item for writing. You can assume it is buffered.
     * @return return an output stream for the item.  It will never return null; problems will cause exceptions.
     * @throws ThingsException for any problem.
     */
    public OutputStream openForWrite() throws ThingsException {
    	if ((workingIS!=null)||(workingOS!=null)) throw new ThingsException("Already opened.", ThingsException.ACCESS_ERROR_CONCURRENT_NOT_ALLOWED);
    	
    	ByteArrayOutputStream result = null;
    	try {
    		result = new ByteArrayOutputStream();
    		workingOS = result;
    		
    	} catch (Throwable t) {
    		throw new ThingsException("Could not open.", ThingsException.ACCESS_ERROR_CANNOT_OPEN, t);
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
    	
    	workingOS = null;
    }
    
}
