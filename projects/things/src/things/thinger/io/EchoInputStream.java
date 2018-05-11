/**
 * THINGS/THINGER 2004, 2005, 2006, 2006, 2007
 * Copyright Erich P Gatejen (c) 2004, 2005, 2006, 2007 ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.thinger.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * An echoing input stream.
 * <p>
 * It is a good idea to flush() this before letting anything else use the echoStream.  I did
 * a bunch of flushed inline, but it really slowed things down.
 * <p>
 * Calling close will do nothing.  It's up to the user to decide the fate of the streams.
 * <p>
 * @author Erich P. Gatejen<br>
 * @version 1.0<br>
 * <i>Version History</i><br>
 * <code>EPG - New - 20Feb05</code> 
 */
public class EchoInputStream extends InputStream {

	// DATA
	private InputStream  ins;
	private OutputStream echo;
		
	/**
	 * Constructor.  This is a one-use echo.
	 * @param source The input stream.
	 * @param echoStream Where to echo everything.
	 * @throws Exception
	 */
	public EchoInputStream(InputStream source, OutputStream  echoStream) throws Exception {
		super();
		if (source==null) throw new IOException("Null source stream.");
		if (echoStream == null) throw new IOException("Null echo stream.");
		ins = source;
		echo = echoStream;
	}
	
	/**
	 * Close.  This is a NOP.  It is up to the users to close streams.
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
		int value = ins.read();
		if (value >=0) echo.write(value);
		return value;	
	}
    
	/**
	 * Reads into an array.  It will echo to the echo stream before returning.
	 * @param b the destination array.
	 * @return the number of bytes read.
	 * @throws IOException
	 */
    public int read(byte[] b) throws IOException {
    	int result = ins.read(b);
		if (result >=0) echo.write(b,0,result);  	
    	return result;
    }
    
	/**
	 * Reads into an array from an offset.  It will echo to the echo stream before returning.
	 * @param b the destination array.
	 * @param off the offset in the array.
	 * @param len the maximum number to read.
	 * @return the number of bytes actually read.
	 * @throws IOException
	 */
    public int read(byte[] b,  int off,   int len)  throws IOException {
    	int result = ins.read(b, off, len);
		if (result >=0) echo.write(b,off,result);  	
    	return result;	
    }
     
    /**
     * Skip is not supported and will always throw an IOException.
     * @throws IOException
     */
    public long skip(long n) throws IOException {
		throw new IOException("EchoInputStream does not allow skip.");    	 
    }
    
    /**
     * Returns the number of characters available for read without blocking.
     * @throws IOException
     * @return the number available.
     */
    public int available() throws IOException {
    	return ins.available();
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
