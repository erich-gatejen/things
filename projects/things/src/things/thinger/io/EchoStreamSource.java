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
import java.io.OutputStream;

/**
 * An echoing stream source.  Everything read will be copied to the given output stream.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 FEB 05
 * </pre> 
 */
public class EchoStreamSource implements StreamSource {

	// ==================================================================================================
	// DATA
	private InputStream  ins;
	private OutputStream echo;
	private int tee;
		
	// ==================================================================================================
	// METHODS
	
	/**
	 * Use this object but with another stream.  I'm being particularly naughty by letting a method
	 * change the streams in-flight.  This will make data processing pipelines a bit more interesting,
	 * but I probably can be convinced this is a bad idea.
	 * @param source The input stream source.
	 * @return itself
	 * @throws Exception
	 */
	public EchoStreamSource use(InputStream source, OutputStream  echoStream) throws Exception {
		ins = source;
		echo = echoStream;
		try {
			tee = ins.read();
			if (tee >= 0) echo.write(tee);
		} catch (Exception t) {
			throw new Exception("Source stream doesn't work.", t);
		}	
		return this;
	}
	
	/**
	 * Get the next character.  It'll throw an EOFException if there is nothing left.
	 * @return the next integer (always fits in a byte)
	 * @throws Exception
	 */
	public int next() throws Exception {
		int result = tee;
		if (result < 0) throw new EOFException("No more characters.");
		tee = ins.read();
		if (tee >= 0) echo.write(tee);
		return result;
	}
	
	/**
	 * Does the source have more to get?
	 * @return true if it does, otherwise false.
	 */
	public boolean hasMore() throws Exception {
		if (tee >= 0) return true;
		else {
			echo.flush();
			return false;
		}
	}
	
}
