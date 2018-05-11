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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import things.common.ThingsException;
import things.data.LineFeeder;

/**
 * A line feeder implementation that works on file.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 MAY 05
 * </pre> 
 */
public class LineFeederFromStream implements LineFeeder {

	private BufferedReader lineSource;
	private String lookAhead;
	private int	line;
	
    /**
     * Init the feeder.  It will accept and buffer an unbuffered stream as a source.
     * @param ios InputStream source
     * @throws thingss.common.ThingsException
     */
    public void init(InputStream ios) throws ThingsException {
		
		try {
			// Create the line source and read ahead
			lineSource = new BufferedReader(new InputStreamReader(ios));
			lookAhead = lineSource.readLine();		
			line=1;
		} catch (Throwable t) {
			throw new ThingsException("spurious IO problem while inititializing a LineFeederFromStream", ThingsException.IO_FAULT_ABSTRACT_STREAM,t);
		}
    }
	
    /**
     * Get the next line as a String.  It will return null if there
     * is nothing left to give.
     * @return the next line
     */
    public String getNextLine() {
		String result = null;
		try {
			// Create the line source and read ahead
			result = lookAhead;
			if (lookAhead!= null) {
				lookAhead = lineSource.readLine();	
				line++;
			}
		} catch (Throwable t) {
			result = null;
		}		
		return result;
	}
    
    /**
     * peek at next line, but don't read it.
     * @return the next line
     */
    public String peekNextLine() {
		return lookAhead;
    }
    
    /**
     * Get the number of the current line (last one read).
     * @return the next line
     */   
    public int lineNumber() {
		return line;
    }
}
