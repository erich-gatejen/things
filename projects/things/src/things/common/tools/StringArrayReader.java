/**
 * THINGS/THINGER 2004, 2005, 2006
 * Copyright Erich P Gatejen (c) 2004, 2005, 2006  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.common.tools;

import java.io.IOException;
import java.io.Reader;

/**
 * This will read an array of strings.
 * <p>
 * @author Erich P. Gatejen<br>
 * @version 1.0<br>
 * <i>Version History</i><br>
 * <code>EPG - Rewrite - 6 May 06</code> 
 */
public class StringArrayReader extends Reader {

	// DATA
	private String[] data;
	private int elementPointer = 0;
	private int characterPointer = 0;
	private String currentElement;
	private boolean spaceDelimit = false;

	/**
	 * Construct.  Use the data passed.
	 * @param inData The data to read.
	 * @param spaceDelimit simulate a space between each array element.
	 * @throws Throwable if the data is not allowed.
	 */
	public StringArrayReader(String[] inData, boolean spaceDelimit) throws Throwable {
		super();
		data = inData;
		if (data==null) throw new IOException("Data cannot be null.");
		if (data.length<1) currentElement = null;
		else if (data[0]==null) throw new IOException("First element cannot be null.");
		else if (data[0].length()<1) throw new IOException("First element cannot be empty (no characters).");
		else currentElement = data[0];
		elementPointer = 1;
		this.spaceDelimit = spaceDelimit;
	}
	
    /**
     * Read into a buffer.
     * @param cbuf  Destination buffer
	 * @param off  Offset at which to start storing characters
	 * @param len  Maximum number of characters to read 
     * @see java.io.Reader#read(char[], int, int)
     * @throws IOException
     */
    public int read(char cbuf[], int off, int len) throws IOException {
    	
    	int numberRead = 0;
    	
    	// Qualify it
		if (cbuf==null) throw new IOException("Buffer cannot be null.");
		if ((off >= cbuf.length)||(off < 0))  throw new IOException("Bad offset.");
	
		// Run it
		int candidate = this.read();
    	while (candidate >=0 ) {
    		
    		// Keep it.
    		cbuf[numberRead+off] = (char)candidate;
    		numberRead++;
    		
    		// Iteration
    		if ((numberRead >= len)||((numberRead+off)>=cbuf.length)) break;  // All full!
    		candidate = this.read();
    	}
    	
    	return numberRead;
    }

    /**
     * Read a single character.  Returns -1 when the reader is empty.
     * @return the character.
     * @see java.io.Reader#read()
     * @throws Not really anything, ever.
     */
    public int read() throws IOException {
    	
    	// Do we have an elements?
    	if (currentElement==null) return -1;
    	
    	// See if we need to move on to other elements.
    	// This should always break because characterPointer is set to 0 is we get a new elements and a 0-length element will not
    	// qualify to leave the inner while.
    	if (characterPointer>=currentElement.length()) {
    		
    		// Delimit with a space.
    		if ((spaceDelimit)&&(characterPointer==currentElement.length())) {
    			characterPointer++;
    			return ' ';
    		}
    		
    		// Next element?  Loop though any that are null or empty.
    		currentElement = null;
    		while ( (currentElement==null) || (currentElement.length()<1) ){
    			
        		if (elementPointer>=data.length) {
        			// All empty!
        			return -1;
        			
        		} else {
        			// Next string
        			currentElement = data[elementPointer];
        			elementPointer++;
        			characterPointer = 0;
        		}   			
    		}
    	}
    	
    	int result = currentElement.charAt(characterPointer);
    	characterPointer++;
    	return result;
    }

    /**
     * Close.  It's a NOOP.
     */
    public void close() throws IOException {
    }
	
	
}
