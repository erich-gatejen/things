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
package things.data.processing;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import things.common.ThingsException;
import things.common.ThingsNamespace;

/**
 * General phrase matcher.  This one I've tried to make Unicode compatible.  I just hope the reader will yield the 
 * right characters.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 JUL 09
 * </pre> 
 */
public abstract class PhraseMatcher {

	// =========================================================================================
	// PUBLIC DATA 
	public final static int 	MAX_PHRASE_SIZE_IN_BYTES = 1024;
	
	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER USAGE
	
	// Per usage
	private	int	stateChart[][];
	private int nextColumn;
	
	private final static int 	INITIAL_CHART_CAPACITY = 100;
	private final static int	CHART_DEPTH	= 256;				// Allow charting 8-bit character
	private final static int 	STATE_START = 0;
	
	private final static int	PHRASE_BUFFER_SIZE	=	MAX_PHRASE_SIZE_IN_BYTES * 10;	// This has to be much large to handle spacing.
	
	private final static int 	SPACE_STATE = -1;
	private final static int 	MINIMUM_ID = (SPACE_STATE * -1) + 1;
	
	private final static int 	SPACE_CHARACTER_BYTE = 32;	// ASCII space

	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER PROCESSSING
	
	
	// =========================================================================================
	// INTERNAL DATA - There is only one entry into this class and it is synchronized, so these are class global and safe.
	// PER PROCESSING - AVAILABLE TO SUBCLASS
	protected char		phraseBuffer[];
	protected int		phraseBufferLength;
	
	// =========================================================================================
	// CONSTRUCTOR
	public PhraseMatcher() throws Throwable {
		phraseBuffer = new char[PHRASE_BUFFER_SIZE];
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
	 * Start on a specific document.  This gives the implementation a chance to initialize.
	 * @param docId The id for the document, data, or whatever.  The implementation may choose to ignore it.
	 * @throws Throwable
	 */
	abstract protected void start(String docId) throws Throwable;
	
	/**
	 * This method will be called when a phrase is matched.  Be sure to write to outs if you want anything preserved!
	 * 
	 * The read() method will supply the read of the header line.  
	 * @param id The defined id.
	 * @param phrase The phrase data as it exactly appears in the stream.
	 * @param len The number of valid characters in the phraseBuffer.  The offset is always 0.
	 * @param out Writer to write the processed data.  If null, then the caller asked not to write anything, but it is up to the implementation.
	 * @throws Throwable
	 */
	abstract protected void match(int	id, char[] phrase, int len, Writer 	out) throws Throwable;
	
	// ========================================================================================
	// DECLARATION
	
	/**
	 * Declare a phrase.  White space is not significant other than to break tokens.  Punctuation is significant only if declared.
	 * @param phrase The phrase.
	 * @param id The phrase id.  This can be a duplicate.  It must be MINIMUM_ID or higher.
	 * @param caseSensitive if true, the phrase will be case sensitive.
	 * @throws Throwable
	 */
	public synchronized void declare(String phrase, int id, boolean caseSensitive) throws Throwable {
		
		//String normalized;
		byte[] bytes;
		boolean spacing = false;
		
		// Validate 
		if ((phrase==null)||(phrase.length()<2)) throw new Exception("Bad headerName.  Must be at least two characters.  name=" + phrase);
		if (phrase.length() > MAX_PHRASE_SIZE_IN_BYTES) throw new Exception("Exceeds max allowable phrase.  allowed=" + MAX_PHRASE_SIZE_IN_BYTES + " actual characters=" + phrase.length());
		//normalized = phrase.toLowerCase();	
		bytes = phrase.getBytes();
		if (bytes.length > MAX_PHRASE_SIZE_IN_BYTES) throw new Exception("Exceeds max allowable phrase.  allowed=" + MAX_PHRASE_SIZE_IN_BYTES + " actual=" + phrase.length());
		if (id < MINIMUM_ID) throw new Exception("The ID must be " + MINIMUM_ID + " or greater.");

		//if (headers.containsKey(normalizedHeader)) throw new Exception("Header already declared.  name=" + headerName);

		// Prepare
		int currentColumn = STATE_START;
		StringReader srin = new StringReader(phrase);
		
		// Strip leading whitespace
		int current = srin.read();
		while (Character.isWhitespace(current)) {
			current = srin.read();
		}
		
		// Read through.
		while (current>=0)  {
			
			switch(Character.getType(current)) {
			
			// Definable
			case Character.CURRENCY_SYMBOL:
			case Character.COMBINING_SPACING_MARK:
			case Character.DASH_PUNCTUATION:
			case Character.DECIMAL_DIGIT_NUMBER:
			case Character.LETTER_NUMBER:
			case Character.MATH_SYMBOL:
			case Character.MODIFIER_LETTER:
			case Character.MODIFIER_SYMBOL:
			case Character.OTHER_LETTER:
			case Character.OTHER_NUMBER:
			case Character.OTHER_SYMBOL:
			case Character.TITLECASE_LETTER:
				spacing=false;
				currentColumn = push(current, -1, currentColumn);
				break;
				
			// Definable - uppercase
			case Character.UPPERCASE_LETTER:
				spacing=false;
				if (caseSensitive)
					currentColumn = push(current, -1, currentColumn);
				else 
					currentColumn = push(current, Character.toLowerCase(current), currentColumn);
				break;
				
			// Definable - lowercase
			case Character.LOWERCASE_LETTER:
				spacing=false;
				if (caseSensitive)
					currentColumn = push(current, -1, currentColumn);
				else 
					currentColumn = push(current, Character.toUpperCase(current), currentColumn);
				break;	
				
			// Token breaking definition
			case Character.SPACE_SEPARATOR:
				if (spacing==false) {
					// Push a single space.  It'll always point at itself.
					pushSpace(currentColumn);
					spacing = true;
				}
				break;
				
			// Not allowed ever	
			case Character.CONNECTOR_PUNCTUATION:
			case Character.CONTROL:
			case Character.UNASSIGNED:				// Don't know what to do with this.
			// Not allowed during definition.
			case Character.ENCLOSING_MARK:
			case Character.END_PUNCTUATION:
			case Character.FINAL_QUOTE_PUNCTUATION:
			case Character.FORMAT:
			case Character.INITIAL_QUOTE_PUNCTUATION:
			case Character.LINE_SEPARATOR:
			case Character.NON_SPACING_MARK:
			case Character.OTHER_PUNCTUATION:
			case Character.PARAGRAPH_SEPARATOR:
			case Character.PRIVATE_USE:
			case Character.START_PUNCTUATION:
			case Character.SURROGATE:
			default:
				throw new ThingsException("Bad phrase definition.  Contains a letter that isn't allowed.  Use letters, digits, connecting puncutation, and simple spaces (for token seperation) only.", ThingsException.PROCESSING_ERROR_DEFINITION, 
						ThingsNamespace.ATTR_DATA_ARGUMENT, phrase, ThingsNamespace.ATTR_DATA_CLASSIFICATION_CHAR, Integer.toString(Character.getType(current)));
			}
			
			// Iterate
			current = srin.read();
		}
		
		// Terminal
		stateChart[currentColumn][LexicalTool.LEXICAL_HEADER_TERMINATION] = -id;  // Make it negative!
	}
	
	/**
	 * Push a reader character.  It might result in two bytes.
	 * @param c the character from the Basic Multilingual Plane (BMP) (what we'll get out of the stream readers.).
	 * @param alt the alternate character or -1 if it isn't used.
	 * @param column the current column
	 * @return the next column
	 */
	private int push(int c, int alt, int column) throws Throwable {
		if (c > 0xFF) {
			if (alt > 0xFF) {
				throw new ThingsException("Bad phrase definition.  Alternate letter must be the same class as the primary.", ThingsException.PROCESSING_ERROR_DEFINITION, 
						ThingsNamespace.ATTR_DATA_CLASSIFICATION_CHAR, Integer.toString(Character.getType(alt)));				
			} else {
				return pushb((byte)(c&0xFF), (byte)(alt&0xFF), 		// Mask off the lower bits. 
						pushb((byte)( ((c&0xFF00)>>8) ), (byte)( ((alt&0xFF00)>>8) ), column));		// Mask off the high bits and shift them to the byte.  do it first.
			}
			
		} else {
			if (alt > 0xFF) {
				throw new ThingsException("Bad phrase definition.  Alternate letter must be the same class as the primary.", ThingsException.PROCESSING_ERROR_DEFINITION, 
						ThingsNamespace.ATTR_DATA_CLASSIFICATION_CHAR, Integer.toString(Character.getType(alt)));
			} else {
				return pushb((byte)c, (byte)alt, column);
			}
		}
	}

	/**
	 * Push a character byte into the chart.
	 * @param c the character byte.  It must be between 0 and 255, inclusive.
	 * @param alt the alternate character byte, typically the other case.  It must be between 0 and 255, inclusive, or -1 if it isn't used.
	 * @param column the current column
	 * @return the next column
	 */
	private int pushb(byte c, byte alt, int column) {
		int result = 0;
		
		// Does this create a new node?
		if (stateChart[column][c] == STATE_START) {
			
			// extend the table?
			if (stateChart.length <= nextColumn) extendChartDeclaration();
			
			// base
			stateChart[column][c] = nextColumn;
			if (alt>=0) {
				stateChart[column][alt] = nextColumn;
			}
			
			// Transition
			result = nextColumn; 
			nextColumn++;
			
		} else {
			
			if (alt>=0) {
				if (stateChart[column][alt] == STATE_START) {
					// TODO This might cause an undefined state if a previous declaration was not case sensitive, but this one is, and they share the step to this point.
					// Right now, we'll just sync them now and hope for the best.  This could end up being a big bug.
					stateChart[column][alt] = stateChart[column][c];
				}
			}
			
			// Yes.  Grab state and move on.
			result = stateChart[column][c];					
		}
		return result;
	}
	
	/**
	 * Push a spacing character.  It will always point at itself.
	 * @param column the current column
	 * @return the next column
	 */
	private int pushSpace(int column) {
		stateChart[column][SPACE_CHARACTER_BYTE] = column;
		return column;
	}


	// ========================================================================================
	// METHODS
	
	/**
	 * Process the reader.  This is designed to take a stream reader, where each read yields a 16-bit character.
	 * @param docId The id for the document, data, or whatever.  This may be echoed into the entries, depending on the specific implementation.
	 * @param input The input reader.
	 * @param output The output writer.  Set to null if this is a read only process.
	 * @throws Throwable
	 */
	public synchronized void process(String docId, Reader input, Writer output) throws Throwable {
		
		// Setup
		int character;
		int currentColumn = STATE_START;

		// Start the header.  This will usually be cleared by either a match or a ack'd non-match.  Otherwise, you can assume
		// a positive value is dangling characters.
		phraseBufferLength = 0;
		
		// Go until we know we are done.
		start(docId);
		
		// Writing	 ---------------------------------------------------------------------
		try {
			character = input.read();
			while (character >= 0) {

				// matching? Normalize all whitespace to space separator for matching.
				switch(Character.getType(character)) {
				
				// Space
				case Character.SPACE_SEPARATOR:			
				case Character.LINE_SEPARATOR:				
				case Character.PARAGRAPH_SEPARATOR:
					currentColumn = stateChart[currentColumn][SPACE_CHARACTER_BYTE];
	
				default:
					currentColumn = stateChart[currentColumn][character];
					break;
				}					
				
				// Where did we shift?
				if (currentColumn == STATE_START) {
					
					// Write a buffer, if it exists.
					if (phraseBufferLength>0) {
						output.write(phraseBuffer, 0, phraseBufferLength);
						phraseBufferLength = 0;
					}
					
					// write this character
					output.write(character);
					
				} else if (currentColumn < 0) {
					
					// Matched.  inverse the state value to get the id.
					match(-currentColumn, phraseBuffer, phraseBufferLength, output);
					phraseBufferLength = 0;
					
				} else {
					
					// Matching, so save it.
					phraseBuffer[phraseBufferLength] = (char)character;
					phraseBufferLength++;
				}
				
				// Next
				character = input.read();
				
			} // end while
			
		} catch (Throwable t) {
			throw new ThingsException("Fault while processing for phase matching.", ThingsException.PROCESSING_FAULT_MATCHER, t);
		}
			
		// If there are any header characters pending, we should write them.	
		// Write a buffer, if it exists.
		if (phraseBufferLength>0) {
			output.write(phraseBuffer, 0, phraseBufferLength);
		}

	}
	
	/**
	 * Reinitialize the processor.
	 */
	public synchronized void init() throws Throwable {
		this.initialize();
	}
	
	// =========================================================================================================
	// = PRIVATE METHODS
	
	/**
	 * Initialize the processor.
	 */
	private synchronized void initialize() throws Throwable {
		
		// Set me up
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
	}
	
	
}


