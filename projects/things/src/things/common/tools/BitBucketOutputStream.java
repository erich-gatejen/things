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

package things.common.tools;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A oneway trip to Nowheresville.  Nothing is actually written anywhere
 * by this output stream.
 * @see java.io.OutputStream
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 JUL 06
 * </pre
 */
public class BitBucketOutputStream extends OutputStream {
	
	// =======================================================================================
	// OUTPUT STREAM IMPEMENTATION
	
    /**
     * Writes b.length bytes from the specified byte array to this output stream.
     * @param b the data.
     * @throws IOException
     */
	public void write(byte[] b) throws IOException {
	}

    /**
     * Writes len bytes from the specified byte array starting at offset off to this output stream.
     * @param b the data.
	 * @param off  the start offset in the data.
	 * @param len  the number of bytes to write.
      * @throws IOException
     */
	public void write(byte[] b,   int off,   int len) throws IOException {
	}
	
	/**
	 * Writes the specified byte to this output stream.
	 * @param  b the byte.
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
	}
	
	
    /**
     * Close.  It's a NOOP.
     */
    public void close() throws IOException {
    }
	
	
}
