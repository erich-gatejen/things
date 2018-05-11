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
package things.data.processing.rfc822;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import things.data.processing.LexicalTool;

/**
 * Processes headers.<p>
 * Your specific processor will extend this class.  The declarations() method will register supported headers with calls to declare();
 * A call to process() will process a specific message, which will in turn call the other abstract methods for matches (or not-matched).  
 * The object can be reused all you want--so keep it around and process() over and over again.
 * <p>
 * EXAMPLE LEX MATCHING ENGINE<br>
 * <pre>				
Inputs	beb					
		bea					
		be					

	0	1	2	3	4	5

a	0	0	4	0	0	0
b	1	0	3	0	0	0
c	0	0	0	0	0	0
d	0	0	0	0	0	0
e	0	2	0	0	0	0
f	0	0	0	0	0	0
:	0	0	-3	-1	-2	0

Terms	-1	beb				
		-2	bea
		-3	be
		-4	
</pre>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial (part of toolkit) - 22 NOV 02
 * EPG - Modified for this project - 14 FEB 05
 * EPG - Forked into a second implementation - 25 MAR 05
 * </pre> 
 */
public abstract class HeaderProcessor extends InputStream  {

	// =========================================================================================
	// PUBLIC DATA 
	public final static int 	MAX_HEADER_NAME_SIZE = 1024;
	
	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER USAGE
	
	// Per usage
	private HashMap<String,String> headers;	
	private	int	stateChart[][];
	private int nextColumn;
	
	private final static int 	INITIAL_CHART_CAPACITY = 100;
	private final static int	CHART_DEPTH	= 256;				// Allow charting 8-bit character
	private final static int	STATE_NONE = 0;

	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER PROCESSSING
	private int				latchCharacter;
	private int				teeCharacter;
	private int 			readState = LexicalTool.HP_NOT_USED;
	private int 			unpauseState = LexicalTool.HP_NOT_USED;
	private InputStream		ins;
	
	/**
	 * The output stream is available to subclasses, if passed as part of the process.  If it is null, then the processor does
	 * not wish to write it--the header is read only.
	 */
	private OutputStream	outs;
	
	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER PROCESSING - AVAILABLE TO SUBCLASS
	protected boolean	headerLineBroken;
	protected byte		headerBuffer[];
	protected int		headerBufferLength;
	
	// =========================================================================================
	// CONSTRUCTOR
	public HeaderProcessor() throws Throwable {
		headerBuffer = new byte[MAX_HEADER_NAME_SIZE];
		initialize();
	}
	
	// =========================================================================================
	// =========================================================================================
	// ABSTRACT METHODS
	
	/**
	 * All declarations should be put here, so they are done with any initialization.
	 * @throws Throwable
	 */
	abstract protected void declarations() throws Throwable;
	
	/**
	 * Start on a specific header.  This gives the implementation a chance to initialize.
	 * @param messageId the id for the message being processed.  The implementation may choose to ignore it.
	 * @throws Throwable
	 */
	abstract protected void start(String messageId) throws Throwable;
	
	/**
	 * This method will be called when a header is matched.  Be sure to write to outs if you want anything preserved!  This includes the CRLF.
	 * The read() method will supply the read of the header line.  
	 * @param id The defined id.
	 * @param out OutputStream to write the processed data.
	 * @throws Throwable
	 */
	abstract protected void match(int	id, OutputStream 	out) throws Throwable;
	
	/**
	 * This method will be called when a header is unmatched.  Be sure to write to outs if you want anything preserved!  This includes the CRLF.
	 * The read() method will supply the read of the header line.  
	 * @param out OutputStream to write the processed data.
	 * @param headerBuffer What we read about the header already.
	 * @throws Throwable
	 */
	abstract protected void unmatch(byte[] headerBuffer, 		int size, 	OutputStream 	out) throws Throwable;

	/**
	 * This method will be called when a header is matched.  This will be called when a header is matched for a read-only operation.
	 * @param id The defined id.
	 * @throws Throwable
	 */
	abstract protected void matchReadOnly(int	id) throws Throwable;
	
	/**
	 * Complete on a specific header.  This gives the implementation a chance to return entries.  If anything is returned at all
	 * is up to the implementation.  It can give a null if it wants to.
	 * @return A list of header fields.
	 * @throws Throwable
	 */
	abstract protected List<String> complete(OutputStream 	out) throws Throwable;
	
	// ========================================================================================
	// DECLARATION
	
	/**
	 * Declare a header.  
	 * @param headerName The header name.
	 * @param id The header id.  This can be a duplicate.  Cannot be =< 0.
	 * @param caseSensitive if true, the header name will be case sensitive.
	 * @throws Throwable
	 */
	public synchronized void declare(String headerName, int id, boolean caseSensitive) throws Throwable {
		
		String normalizedHeader;
		
		// Validate 
		if ((headerName==null)||(headerName.length()<2)) throw new Exception("Bad headerName.  Must be at least two characters.  name=" + headerName);
		if (headerName.length() > MAX_HEADER_NAME_SIZE) throw new Exception("Exceeds max allowable header name.  allowed=" + MAX_HEADER_NAME_SIZE + " actual=" + headerName.length());
		normalizedHeader = headerName.toLowerCase();
		if (headers.containsKey(normalizedHeader)) throw new Exception("Header already declared.  name=" + headerName);
		if (id <= 0) throw new Exception("Id cannot be <= 0");

		// Prepare
		int currentColumn = STATE_NONE;
		int type;
		StringReader srin = new StringReader(headerName);
		int current = srin.read();
		
		// Disqualification
		if (LexicalTool.get822HeadernameType(current) == LexicalTool.BREAKING ) throw new Exception ("Header empty.  Lone colon.");
		
		// Run it
		while (current >= 0) {
			
			// manage
			type = LexicalTool.get822HeadernameType(current);
			switch(type) {
			case LexicalTool.ALLOWED:
				
				// See if it already is there
				if (stateChart[currentColumn][current] == STATE_NONE) {
					
					// Nope.  It is new.
					
					// extend the table?
					if (stateChart.length <= nextColumn) extendChartDeclaration();
					
					// Pointer.  If it's not case sensitive and a char, put for upper and lower.
					if ((caseSensitive)||(LexicalTool.getClassification(current) != LexicalTool.CLASS_ALPHA)) {
						stateChart[currentColumn][current] = nextColumn;
					} else {
						stateChart[currentColumn][LexicalTool.getLower(current)] = nextColumn;				
						stateChart[currentColumn][LexicalTool.getUpper(current)] = nextColumn;			
					}
					
					// Transition
					currentColumn = nextColumn;
					nextColumn++;
					
				} else {
					
					// Yes.  Grab state and move on.
					currentColumn = stateChart[currentColumn][current];					
				}
				break;
				
			case LexicalTool.BREAKING:
				//  Error since we should have an embedded break
				throw new Exception("Header cannot have a colon character.");
				
			case LexicalTool.NOT_ALLOWED:
			default:
				throw new Exception("Character not allowed in header.  character=" + current);		
			
			}
			
			// DO NOT EDIT NEXT LINE
			current = srin.read();
		}
		
		// Terminal
		stateChart[currentColumn][LexicalTool.LEXICAL_HEADER_TERMINATION] = -id;  // Make it negative!
	}
	
	// ========================================================================================
	// METHODS
	
	/**
	 * Process the headers.  All headers that are unmatched will be echoed to output.  If they are matched, it is up to the subclass
	 * to decide to echo them or not.
	 * @param messageId The id for the message or document.  This may be echoed into the entries, depending on the specific implementation.
	 * @param input The input stream.
	 * @param output The output stream.  Set to null if this is a read only process.
	 * @return a List of string entries.  The type of entries is up to the specific processor.
	 * @throws Throwable
	 */
	public synchronized List<String> process(String messageId, InputStream input, OutputStream output) throws Throwable {
		
		// Setup
		ins = input;
		outs = output;
		startProcessing();
		int character;
		int currentColumn = STATE_NONE;

		// Start the header.  This will usually be cleared by either a match or a ack'd non-match.  Otherwise, you can assume
		// a positive value is dangling characters.
		headerBufferLength = 0;
		
		// Go until we know we are done.
		start(messageId);
		
		// Unroll the writing v. the non-writing
		if (outs==null) {
		
			// Not Writing ----------------------------------------------------------------------
			while (!isDone()) {
			
				// Match headers - read until depleted.  
				try {
					character = this.read();
					while (character >= 0) {
		
						
						// matching? Get and shift.
						currentColumn = stateChart[currentColumn][character];
						if (currentColumn == STATE_NONE) {
							
							// broken match - just write and deplete it
							character = this.read();
							while (character >= 0) {
								character = this.read();
							}
							
							// break this header.  As promised, we will clear it.
							break;
							
						} else if (currentColumn < 0) {
							
							// matched.  inverse the state value to get the id.
							matchReadOnly(-currentColumn);
							break;
						}
						
						// Next
						character = this.read();
						
					} // end while
					
				} catch (Exception e) {
					throw new Exception("FAILED message.  " + e.getMessage(), e);
				}
				
				// If we are paused, clear it.  Remember, the stream may be depleted for that header line only.  Clearing the pause
				// would move on to the next header--or not.  
				deplete();				
				clearPause();
				currentColumn = STATE_NONE;
						
			} // while header remains
			
		
		} else {
			
			// Writing	 ---------------------------------------------------------------------
			while (!isDone()) {
			
				// Match headers - read until depleted.  
				try {
					character = this.read();
					while (character >= 0) {
		
						// Save it.
						headerBuffer[headerBufferLength] = (byte)character;
						headerBufferLength++;
						
						// matching? Get and shift.
						currentColumn = stateChart[currentColumn][character];
						if (currentColumn == STATE_NONE) {
							
							// broken match - eat the buffer until we get the colon or EOF.
							character = this.read();
							while(character>0) {
								headerBuffer[headerBufferLength] = (byte)character;
								headerBufferLength++;
								if (character==':') break;
								character = this.read();
							}
							
							// then give it to the unmatcher.
							unmatch(headerBuffer, headerBufferLength, outs);
							
							// break this header.  As promised, we will clear it.
							headerBufferLength = 0;
							break;
							
						} else if (currentColumn < 0) {
							
							// matched.  inverse the state value to get the id.
							match(-currentColumn, outs);
							
							// break this header.  As promised, we will clear it.
							headerBufferLength = 0;
							break;
						}
						
						// Next
						character = this.read();
						
					} // end while
					
				} catch (Exception e) {
					throw new Exception("FAILED message.  " + e.getMessage(), e);
				}
				
				// If we are paused, clear it.  Remember, the stream may be depleted for that header line only.  Clearing the pause
				// would move on to the next header--or not.  
				deplete();
				clearPause();
				currentColumn = STATE_NONE;
						
			} // while header remains
			
			// If there are any header characters pending, we should write them.	
			if (headerBufferLength > 0) {
				unmatch(headerBuffer, headerBufferLength, outs);
				headerBufferLength = 0;
			}			
			
		}
		
		// Result
		return complete(output);
	}
	
	/**
	 * Reinitialize the processor.
	 */
	public synchronized void init() throws Throwable {
		this.initialize();
	}

	// ========================================================================================
	// PROTECTED TOOLS
	
	/**
	 * Write the original header name with a colon.
	 * @throws Throwable
	 */
	public void writeOriginalHeaderNameWithColon() throws Throwable {
		outs.write(headerBuffer,0,headerBufferLength);
		//outs.write(LexicalTool.LEXICAL_HEADER_TERMINATION);
		outs.flush();
	}
	
	/**
	 * Write the original header name with a colon into the given output stream.
	 * @param os the stream.
	 * @throws Throwable
	 */
	public void writeOriginalHeaderNameWithColon(OutputStream os) throws Throwable {
		os.write(headerBuffer,0,headerBufferLength);
		//os.write(LexicalTool.LEXICAL_HEADER_TERMINATION);
		os.flush();
	}
	
	
	// ========================================================================================
	// PRIVATE AND PROTECTED TOOLS
	
	/** 
	 * Start the processing.
	 */
	private void startProcessing() {
		readState = LexicalTool.HP_START;
	}
	
	// ========================================================================================
	// INPUT PROCESSING METHODS
	
	/**
	 * Deplete current header line.  It'll just throw it all away.
	 * @throws Throwable
	 */
	public void deplete() throws Throwable {
		while (read()!=LexicalTool.NO_CHARACTER) {
		}
	}
	
	/**
	 * Is the line broken?
	 */
	public boolean isLineBroken() {
		return headerLineBroken;
	}
	
	/** 
	 * Is the stream paused?  Meaning that the current line has been depleted.
	 * @return true or false
	 */
	public boolean isPaused() {
		if (readState == LexicalTool.HP____SPECIAL_PAUSE) return true;
		else return false;
	}

	/** 
	 * Clear a pause (if any) and allow it to move to the next line.
	 */
	public void clearPause() {
		if (readState == LexicalTool.HP____SPECIAL_PAUSE) {
			readState = unpauseState;
		}
	}
	
	/** 
	 * Is the stream done?  Is there anything left in the header?
	 * @return true or false
	 */
	public boolean isDone() {
		if (readState == LexicalTool.HP____SPECIAL_DEAD) return true;
		else return false;
	}
	
	// ========================================================================================
	// INPUTSTREAM IMPLEMENTATION
	
	/**
	 * Implement the read method which will be used by header data processors.  The header is already ripped and this will just read the rest of the header line.
	 * @return a character or -1 if empty.
	 * @throws IOException
	 */		
	public int read() throws IOException {
		while(true) {
			switch (readState) {
			case LexicalTool.HP_START:
				// clear_flags	read	transition	
				clearFlags();
				teeCharacter = ins.read();			
				if (teeCharacter>=0) {
					readState = LexicalTool.HEADER_READ_STATE_CHART[teeCharacter][readState];
				} else {
					readState = LexicalTool.HP_CLOSURE;
				}
				break;
				
			case LexicalTool.HP_BROKEN:
				// transition	FLAG_BROKEN
				headerLineBroken = true;
				if (teeCharacter>=0) {
					readState = LexicalTool.HEADER_READ_STATE_CHART[teeCharacter][readState];
				} else {
					readState = LexicalTool.HP_CLOSURE;
				}
				break;
				
			case LexicalTool.HP_READ:
			case LexicalTool.HP_CR:
        	case LexicalTool.HP_LF:
			case LexicalTool.HP_LFCR:
			case LexicalTool.HP_HEAD_CR:
			case LexicalTool.HP_HEAD_LF:
			case LexicalTool.HP_HEAD_CRLF:
			case LexicalTool.HP_PAUSE_CRLFCR:
				// yield	read	transition	
				latchCharacter = teeCharacter;	
				teeCharacter = ins.read();
				if (teeCharacter>=0) {
					readState = LexicalTool.HEADER_READ_STATE_CHART[teeCharacter][readState];
				} else {
					readState = LexicalTool.HP_CLOSURE;
				}
				return latchCharacter;				
				
			case LexicalTool.HP_PAUSE:
			case LexicalTool.HP_PAUSE_CRLF:
				// transition	PAUSE
				if (teeCharacter>=0) {
					unpauseState = LexicalTool.HEADER_READ_STATE_CHART[teeCharacter][readState];
				} else {
					unpauseState = LexicalTool.HP_CLOSURE;
				}
				readState = LexicalTool.HP____SPECIAL_PAUSE;
				return -1;
				
			case LexicalTool.HP_CLEAR_PAUSE:
			case LexicalTool.HP_CLEAR_PAUSE_CRLF:
				// clear_flags		transition	
				clearFlags();
				if (teeCharacter>=0) {
					readState = LexicalTool.HEADER_READ_STATE_CHART[teeCharacter][readState];
				} else {
					readState = LexicalTool.HP_CLOSURE;
				}
				break;

			case LexicalTool.HP_CLOSURE:
				// yeild			DONE
				readState = LexicalTool.HP____SPECIAL_DEAD;
				int finalCharacter = teeCharacter;			// So available() gets the right count.
				teeCharacter = -1;
				return finalCharacter;
				
			case LexicalTool.HP____SPECIAL_PAUSE:
			case LexicalTool.HP____SPECIAL_DEAD:
				return -1;
				
			} // end state switch
			
		} // end the endless true
	}
	
	/**
	 * We do not support mark.  This will always return false.
	 * @return false no matter what.
	 */
	public boolean markSupported() {
		return false;
	}
	
	/** 
	 * Do not let a user close this stream.  So this is a NOOP.
	 */
	public void close() {
	}
		
	/**
	 * Make sure we always report out available, even if it is just the remaining tee character.
	 */
	public int available() throws IOException {
		int result = ins.available();
		if (teeCharacter>=0) result++;
		return result;
	}
	
	// =========================================================================================================
	// = PRIVATE METHODS
	
	/**
	 * Initialize the processor.
	 */
	private synchronized void initialize() throws Throwable {
		
		// Set me up
		headers = new HashMap<String,String>();
		stateChart = new int[INITIAL_CHART_CAPACITY][CHART_DEPTH];
		nextColumn = 1;
		
		// Let my subclass make the declarations
		declarations();
	}
	
	// ========================================================================================
	// PRIVATE - TOOLS FOR DECLARATION
	
	/**
	 * Extend the chart.
	 */
	private void extendChartDeclaration() {
		int	newChart[][] = new int[stateChart.length*2][CHART_DEPTH];
		for (int index = 0; index < stateChart.length; index++) {
			newChart[index] = stateChart[index];
		}
		stateChart = newChart;
	}
	
	// ========================================================================================
	// PRIVATE - OTHER
	
	/**
	 * Clear the flags.
	 */
	void clearFlags() {
		headerLineBroken = false;
	}
	
}


