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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A cousin to the ByteArrayOutputStream.  Instead of just expanding it forever, throw an IOException.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 JUL 06
 * </pre> 
 */
public class TightByteArrayOutputStream extends ByteArrayOutputStream {

	// =======================================================================================
	// DATA
	private int mark;

	// =======================================================================================
	// METHODS
	
	/**
	 * Construct.
	 * @throws Throwable if the data is not allowed.
	 */
	public TightByteArrayOutputStream() throws Throwable {
		super();
		mark = 0;
	}
	
	/**
	 * Get the total buffer.  
	 * @return the buffer.
	 */
	public byte[] get() {
		return super.buf;
	}
	
	/**
	 * Get how much of the buffer is filled.  This might be silly, but I really don't want people to copy a huge buffer array
	 * just to get access to this data.  Get the buffer and then getFillSize().
	 * @return the fill size in bytes.
	 */
	public int getFillSize() {
		return super.count;
	}
	
	/**
	 * Mark the next spot that will get a byte and return the value.  If the array is bad you'll get a negative number.
	 */
	public int mark() {
		mark = count+1;
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
		while (rover < count) {
			ous.write(buf[rover]);
			rover++;
		}
	}
	
	// =======================================================================================
	// OUTPUT STREAM IMPEMENTATION
	
   
	
}
