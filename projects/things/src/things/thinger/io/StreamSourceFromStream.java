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

import java.io.EOFException;
import java.io.InputStream;

/**
 * A character stream source.  It will tee a single character, so be sure the underlying stream will starve it (pass EOF) if it should cross
 * a barrier and read something it isn't allowed to have.  See the HeaderProcessor for an example of how to do this.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial (toolkit) - 7 SEP 03
 * </pre> 
 */
public class StreamSourceFromStream implements StreamSource {

	// DATA
	private InputStream  ins;
	private int tee;
	
	/**
	 * A null constructor.  You must call reuse() before the other methods or they will throw exceptions.
	 */
	public StreamSourceFromStream() {
	}
	
	/**
	 * Constructor.  Pass the stream to source.
	 * @param source the input stream source.
	 */
	public StreamSourceFromStream(InputStream source) throws Exception {
		reuse(source);
	}
	
	/**
	 * Reuse this object but with another stream.  Since we anticipate using a lot of these, this should
	 * spare some heap thrash.
	 * @param source The input stream source.
	 * @throws Exception
	 */
	public void reuse(InputStream source) throws Exception {
		ins = source;
		try {
			tee = ins.read();
		} catch (Exception t) {
			throw new Exception("Source stream doesn't work.", t);
		}	
	}
	
	/**
	 * Get the next character.  It'll throw an EOFException if there is nothing left.
	 * @return the next character
	 * @throws Exception
	 */
	public int next() throws Exception {
		int result = tee;
		if (result < 0) throw new EOFException("No more characters.");
		tee = ins.read();
		return result;
	}
	
	/**
	 * Does the source have more to get?
	 * @return true if it does, otherwise false.
	 */
	public boolean hasMore() throws Exception {
		if (tee >= 0) return true;
		else return false;
	}
	
}
