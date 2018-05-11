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
package things.thinger.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Chain up a set of input streams.  This isn't the fastest thing in the world, so use judiciously.  The chained streams will be
 * read in turn.  NONE WILL BE CLOSED.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 FEB 05
 * </pre>
 */
public class ChainedInputStream extends InputStream {

	// DATA
	private InputStream[]  streams;
	private InputStream	current;
	private int index = 0;
		
	/**
	 * Constructor.  This is a one-use echo.
	 * @param streams The input streams.  Each one will be read in turn.  Null elements will be ignored, but at least one has to be non-null.  They will NOT BE CLOSED.
	 * @throws Exception
	 */
	public ChainedInputStream(InputStream... streams) throws Exception {
		super();
		if (streams==null) throw new IOException("Null source stream.");
		if (streams.length < 1) throw new IOException("No streams given.");
		this.streams = streams;
		
		// Get the first non-null.
		for (int index = 0; index < streams.length; index++) {
			current = streams[index];
			if (current!=null) break;
		}
		if (current==null) throw new IOException("All streams were null.");
	}
	
	/**
	 * Nop
	 */
	public void close() throws IOException {
		// NOP
	}
	
	/**
	 * Read a single character.  It will echo to the echo stream before returning.
	 * @return the character.
	 * @throws IOException
	 */
	public int read() throws IOException {
		int value = current.read();
		while ((value < 0)&&(index < streams.length)) {
			
			for (; index < streams.length; index++) {
				current = streams[index];
				if (current!=null) {
					index++;
					break;
				}
			}
			value = current.read();
			
		}
		return value;	
	}
     
    /**
     * Skip is not supported and will always throw an IOException.
     * @throws IOException
     */
    public long skip(long n) throws IOException {
		throw new IOException("EchoInputStream does not allow skip.");    	 
    }
    
    /**
     * Returns the number of characters available for read without blocking.  This is a bit tricky in that it'll count for the 
     * currently read stream only.
     * @throws IOException
     * @return the number available.
     */
    public int available() throws IOException {
    	return current.available();
    }
    
    /**
     * Mark does nothing.
     */
    public void mark(int readlimit) {
		// NOP
	}
	
    /**
     * Reset is not supported and will always throw and IOException.
     * @throws IOException
     */
	public void reset() throws IOException {
		throw new IOException("EchoInputStream does not allow resets.");
	}
    
	/**
	 * Mark does nothing so this will always return false.
	 * @return false
	 */
    public boolean markSupported() {
    	return false;
    }
	

}
