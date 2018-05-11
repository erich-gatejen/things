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
 * A limited ByteArrayOutputStream.  Instead of just expanding it forever, throw an IOException.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 JUL 06
 * </pre> 
 */
public class BoundedByteArrayOutputStream extends OutputStream {

	// =======================================================================================
	// DATA
	private byte[] buffer;
	private int spot = 0;
	private int mark = -1;

	// =======================================================================================
	// METHODS
	
	/**
	 * Construct.  Use the buffer passed (and no more!)
	 * @param buffer The buffer.
	 * @throws Throwable if the data is not allowed.
	 */
	public BoundedByteArrayOutputStream(byte[] buffer) throws Throwable {
		super();
		this.buffer = buffer;
		spot = 0;
	}
	
	/**
	 * Get the total buffer.  
	 * @return the buffer.
	 */
	public byte[] get() {
		return buffer;
	}
	
	/**
	 * Get how much of the buffer is filled.  This might be silly, but I really don't want people to copy a huge buffer array
	 * just to get access to this data.  Get the buffer and then getFillSize().
	 * @return the fill size in bytes.
	 */
	public int getFillSize() {
		return spot;
	}
	
	/**
	 * Mark the next spot that will get a byte and return the value.  If the array is bad you'll get a negative number.
	 */
	public int mark() {
		mark = spot;
		return mark;
	}
	
	/**
	 * Copy from the last mark (or start if not mark made) to the end of the buffer into a stream.  Mark will be not be changed after
	 * the operation.  If there is no difference between the mark and the next stop (meaning nothing was written since the last marking), nothing
	 * will out copied to the output stream.
	 * @param ous the output stream.
	 * @throws IOException
	 */
	public void copyMarkTo(OutputStream ous) throws IOException {
		if (ous==null) throw new IOException("Copy stream is null.");
		int rover = mark;
		while (rover < spot) {
			ous.write(buffer[rover]);
			rover++;
		}
	}
	
	// =======================================================================================
	// OUTPUT STREAM IMPEMENTATION
	
    /**
     * Writes b.length bytes from the specified byte array to this output stream.
     * @param b the data.
     * @throws IOException
     */
	public void write(byte[] b) throws IOException {
		if ( (spot+b.length) >= buffer.length ) throw new IOException("BoundedByteArrayOutputStream is full.  spot=" + spot + " length=" + b.length + " buffer.length=" + buffer.length);
		System.arraycopy(b, 0, buffer, spot, b.length);
		spot = spot + b.length;
	}

    /**
     * Writes len bytes from the specified byte array starting at offset off to this output stream.
     * @param b the data.
	 * @param off  the start offset in the data.
	 * @param len  the number of bytes to write.
      * @throws IOException
     */
	public void write(byte[] b,   int off,   int len) throws IOException {
		if ( (spot+len) >= buffer.length ) throw new IOException("BoundedByteArrayOutputStream is full  (offset).  spot=" + spot + " len=" + len + " buffer.length=" + buffer.length);
		System.arraycopy(b, off, buffer, spot, len);
		spot = spot + len;
	}
	
	/**
	 * Writes the specified byte to this output stream.
	 * @param b the byte.
	 * @throws IOException
	 */
	public void write(int b) throws IOException {
		if ( (spot+1) >= buffer.length ) throw new IOException("BoundedByteArrayOutputStream is full.");
		buffer[spot] = (byte)b;
		spot++;
	}

    /**
     * Close.  It's a NOOP.
     */
    public void close() throws IOException {
    }
	
	
}
