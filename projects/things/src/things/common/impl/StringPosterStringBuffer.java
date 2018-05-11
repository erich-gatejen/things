/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.common.impl;

import things.common.StringPoster;
import things.common.ThingsConstants;
import things.common.ThingsException;

/**
 * Postable implementation for printing to a StringBuffer.  It will automatically instantiate the
 * buffer on contruction.  There is no way to reintialize it.  It will just continue to add until 
 * disposed.
 * <p>
 * Each post will be seperated with a CR/LF sequence.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 16Mar04</code> 
 * 
 */
public class StringPosterStringBuffer implements StringPoster {
	
	private StringBuffer buffer;
	
	/**
	 * Constructor.
	 */	
	public StringPosterStringBuffer() {
		buffer = new StringBuffer();
	}

	/**
	 * Post as a message.
	 * @param message String to post
	 */
	public void post(String message) throws ThingsException {
		buffer.append(message);
		buffer.append(ThingsConstants.CRLF);
	}
	
	/**
	 * Post as a message.  Best effort.  Ignore errors.
	 * @param message String to post
	 */
	public void postit(String message) {
		buffer.append(message);
		buffer.append(ThingsConstants.CRLF);
	}
	
	/**
	 * Return the contents of this StringBuffer as a String.
	 * @return the contents
	 */
	public String toString() {
		return buffer.toString();
	}
	
	/**
	 * Try to flush.  Never error no matter what.
	 */
	public void flush() {
		// NOP
	}
}
