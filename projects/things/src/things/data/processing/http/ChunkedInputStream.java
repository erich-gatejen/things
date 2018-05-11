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
package things.data.processing.http;

import java.io.IOException;
import java.io.InputStream;

import things.common.ThingsException;
import things.data.processing.LexicalTool;

/**
 * A response processor.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 11 AUG 03
 * EPG - Moved to http and changed to match http chunking- 12 FEB 07
 * </pre> 
 */
public class ChunkedInputStream extends InputStream {
	
	// ================================================================================================================
	// INTERNAL DATA 
    private InputStream ins;
    private int sizeCurrentChunk;
    private int roverCurrentChunk;

    // Size flags.  The sizeCurrentChunk is used for the flags.
    private final static int NEXT_CHUNK = 0;
    private final static int EOF = -1;    
   
    /**
     * Constructor.
     * @param source stream.  It will NOT be closed (even with the close method).  You need to close it when you are done.
     * @throws Throwable if the stream is null. 
     */
    public ChunkedInputStream(InputStream source) throws Throwable {
    	if (source == null) ThingsException.softwareProblem("ChunkedInputStream created with a null stream.");
        ins = source;
        roverCurrentChunk = 0;
        sizeCurrentChunk = NEXT_CHUNK;
    }
    
	// ================================================================================================================
	// INPUT STREAM 
    
    /**
     * Reads the next byte of data from the input stream. The value byte is returned as an int in the range 0 to 255. 
     * If no byte is available because the end of the stream has been reached, the value -1 is returned. This method blocks 
     * until input data is available, the end of the stream is detected, or an exception is thrown. 
     * @return the next byte of data, or -1 if the end of the stream is reached. 
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {

    	// Done or chunk?
    	if (sizeCurrentChunk==EOF) return -1;
    	if (roverCurrentChunk>=sizeCurrentChunk) {
    		// Attempt next chunk.
    		nextChunk();
    		if (sizeCurrentChunk==EOF) return -1;
    	} 
    	
		// Read from current chunk
		roverCurrentChunk++;
		return ins.read();
    }

    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many as len bytes, but a smaller number may be read. The number of bytes actually read is returned as an integer. 
     * @param b the buffer into which the data is read.
     * @param off the start offset in array b  at which the data is written.
     * @param len the maximum number of bytes to read. 
     * @return  the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached. 
     * @throws If the first byte cannot be read for any reason other than end of file or if some other I/O error occurs. 
     */
    public int read (byte[] b, int off, int len) throws IOException {

    	// Done or chunk?
    	if (sizeCurrentChunk==EOF) return -1;
    	if (roverCurrentChunk>=sizeCurrentChunk) {
    		// Attempt next chunk.
    		nextChunk();
    		if (sizeCurrentChunk==EOF) return -1;
    	} 
    	
    	// Get what's left in the chunk or the len, whichever is smaller, so we don't crack into the next chunk.
    	int left = sizeCurrentChunk - roverCurrentChunk;
    	if (left < len) len = left;
        int actuallyRead = ins.read(b, off, len);
        roverCurrentChunk += actuallyRead;
        return actuallyRead;
    }


    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes actually read is returned as an integer.
     * This method blocks until input data is available, end of file is detected, or an exception is thrown. 
     * @param b the buffer into which the data is read.
     * @return  the total number of bytes read into the buffer, or -1 if there is no more data because the end of the stream has been reached. 
     * @throws If the first byte cannot be read for any reason other than end of file or if some other I/O error occurs. 
     */
    public int read (byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    
    /**
     * Returns an estimate of the number of bytes that can be read (or skipped over) from this input stream without blocking by the next invocation of a method for this input stream. The next invocation might be the same thread or another thread. A single read or skip of this many bytes will not block, but may read or skip fewer bytes. 
 	 * @return number of bytes available.
 	 * @throws IO exception
     */
    public int 	available()  throws IOException {
    	return ins.available();
    }
    
    /**
     * Close the stream.  It will NOT deplete the stream.  However, all subsequent calls to reads will yield EOF and any internal
     * references to the stream will be cleared.
     * @throws IOException
     * All subsequent calls to the reads will result in EOF.
     */
    public void close() throws IOException {
    	sizeCurrentChunk = EOF;
    	ins = null;
    }

    
	// ================================================================================================================
	// INTERNAL 
    
    /**
     * Chunk process state.
     * I anticipate having to deal other stuff.
     */
    private enum ChunkState {
    	READ,
    	WS,
    	CR,
    	DONE
    }

    /**
     * Find the next chunk or end it all (sizeCurrentChunk = EOF).
     * This is pretty tight logic, so be careful.
     * @throws IOException
     */
    private void nextChunk() throws IOException {
    	int value = 0;
    	int readValue = 0;
    	ChunkState state = ChunkState.READ;

    	// First character
    	int currentChar = ins.read();
		switch(currentChar) {
		case '\r':
			// Maybe just depleting previous line.
			currentChar = ins.read();
			if (currentChar=='\n') currentChar = ins.read();
			else throw new IOException("Chunk specification missing LF following CR.");
			if (LexicalTool.getHexValue(currentChar)==LexicalTool.NOT_ALLOWED) throw new IOException("Strange character encountered starting the chunk specification after CRLF.  char=" + currentChar);
			break;
		case '\n':
			throw new IOException("Chunk specification had LF before a CR.");
		case ' ':
		case '\t':
			throw new IOException("Chunk specification started with whitespace.");
		default:
			readValue = LexicalTool.getHexValue(currentChar);
			if (readValue==LexicalTool.NOT_ALLOWED) {
				throw new IOException("Strange character encountered in chunk specification.  char=" + currentChar);
			}
			value = readValue;
			currentChar = ins.read();
			break;
		}
    	
    	while(state == ChunkState.READ) {
    		switch(currentChar) {
    		case '\r':
    			state = ChunkState.CR;
    			currentChar = ins.read();
    			break;
    		case '\n':
    			throw new IOException("Chunk specification had LF before a CR.");
    		case ' ':
    		case '\t':
    			state = ChunkState.WS;
    			currentChar = ins.read();
    			break;
    		default:
    			readValue = LexicalTool.getHexValue(currentChar);
    			if (readValue==LexicalTool.NOT_ALLOWED) {
    				throw new IOException("Strange character encountered in chunk specification.  char=" + currentChar);
    			}
    			value = (value << 4) + readValue;
    			currentChar = ins.read();
    			break;
    		}
    		if (currentChar < 0) throw new IOException("Chunk specification not completed or terminated before end of stream.");
    	}
    	
    	while(state == ChunkState.WS) {
    		switch(currentChar) {
    		case '\r':
    			state = ChunkState.CR;
    			currentChar = ins.read();
    			break;
    		case '\n':
    			throw new IOException("Chunk specification had LF before a CR.");
    		case ' ':
    		case '\t':
    			currentChar = ins.read();
    			break;
    		default:
    			throw new IOException("Strange character encountered in chunk specification between value and line termination.  char=" + currentChar);
    		}
    		if (currentChar < 0) throw new IOException("Chunk specification not terminated before end of stream.");
    	}
    	
    	// Final LF
    	if (currentChar=='\n') {
    		if (value==0) {
    			value = EOF;
    			
        		// It appears there is a final CRLF sequence behind the EOF.  I'll deplete them here.  This might be a bad idea--time will tell.
        		// There is a chance that net lag could screw this up.
        		ins.read();
        		if (ins.read()!='\n') throw new IOException("Did not find second CRLF.  This may be a bug with ChunkedInputStream.");
    		}
    		
    		sizeCurrentChunk = value;
    		roverCurrentChunk = 0;		
    	} else {
			// Not a LF
			throw new IOException("Strange character after CR.  Expected an LF.  char=" + currentChar);
    	}
  
    }
    
    
   
}
