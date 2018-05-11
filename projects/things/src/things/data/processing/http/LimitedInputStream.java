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
package things.data.processing.http;

import java.io.IOException;
import java.io.InputStream;

import things.common.ThingsException;

/**
 * Allow it to only read so many bytes.
 * <p>
 * @author Erich P. Gatejen<br>
 * @version 1.0<br>
 * <i>Version History</i>
 * <br>
 * <code>EPG - New - 12 FEB 2007</code> 
 */
public class LimitedInputStream extends InputStream {
	
	// ================================================================================================================
	// INTERNAL DATA 
    private InputStream ins;
    private long remaining;  
   
    /**
     * Constructor.
     * @param source stream.  It will NOT be closed (even with the close method).  You need to close it when you are done.
     * @param limit the limit in bytes.
     * @throws Throwable if the stream is null or the limit is less than 1.
     */
    public LimitedInputStream(InputStream source, long limit) throws Throwable {
    	if (source == null) ThingsException.softwareProblem("LimitedInputStream created with a null stream.");
    	if (limit < 1) ThingsException.softwareProblem("LimitedInputStream created with a limit less than one.  limit=" + limit);
        ins = source;
        remaining = limit;
    }
    
	// ================================================================================================================
	// INPUT STREAM 
    
    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. 
     * If no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks 
     * until input data is available, the end of the stream is detected, or an exception is thrown. 
     * @return the next byte of data, or -1 if the end of the stream is reached. 
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {

    	// Done or chunk?
    	if (remaining > 0) {
    		remaining--;
    		return ins.read();
    	}
    	return -1;
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as len bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer. 
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b  at which the data is written.
     * @param len the maximum number of bytes to read. 
     * @return  the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached. 
     * @throws If the first byte cannot be read for any reason other than end of file or if some other I/O error occurs. 
     */
    public int read (byte[] b, int off, int len) throws IOException {

    	if (remaining > 0) {
        	if (remaining < len) len = (int)remaining;
            int actuallyRead = ins.read(b, off, len);
            remaining -= actuallyRead;
            return actuallyRead;
    	}
    	return -1;
    
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes actually read is returned as an integer.
     * This method blocks until input data is available, end of file is detected, or an exception is thrown. 
     * @param b the buffer into which the data is read.
     * @return  the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached. 
     * @throws If the first byte cannot be read for any reason other than end of file or if some other I/O error occurs. 
     */
    public int read (byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream. The next invocation might be the same thread or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes. 
 	 * @return number of bytes available.
 	 * @throws IO exception
     */
    public int 	available()  throws IOException {
    	return ins.available();
    }
    
    /**
     * Close the stream.  It will NOT deplete the stream.  However, all subsequent calls to reads will yield EOF and any internal
     * references to the stream will be cleared.
     * @throws IOException
     * All subsequent calls to the reads will result in EOF.
     */
    public void close() throws IOException {
    	remaining = 0;
    	ins = null;
    }

    
	// ================================================================================================================
	// INTERNAL 
 
   
}
