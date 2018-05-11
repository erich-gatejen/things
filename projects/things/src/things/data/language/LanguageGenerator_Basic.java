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
package things.data.language;

import things.common.ThingsException;
import things.common.tools.Gamer;

/**
 * Basic implementation of the language interface.
 * <p>  
 * <b>THIS PACKAGE WAS ABANDONED IN FAVOR OF ANOTHER PROJECT</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 MAY 06
 * </pre> 
 */
public class LanguageGenerator_Basic implements LanguageGenerator {

	// ==========================================================================================================
	// == DATA
	
	private WordDictionary dictionary;
	private Gamer rng = new Gamer();
	
	// Config
	private final static int COMMA_THRESHOLD = 15;
	private final static int COMMA_CHANCE = 10;			// Percentage.
	
	private final static int WORD_CUTOFF_SIZE = 6;
	
	private final static int PARAGRAPH_MIN_SIZE = 40;
	
	private final static int SENTENCE_MIN_SIZE = 50;	
	private final static int SENTENCE_NORMAL_SIZE = 200;
	private final static int SENTENCE_SMALL_RANDOM = SENTENCE_NORMAL_SIZE - SENTENCE_MIN_SIZE;
	private final static int SENTENCE_MAX_SIZE = 2000;	
	private final static int SENTENCE_LARGE_RANDOM = SENTENCE_MAX_SIZE - SENTENCE_NORMAL_SIZE;
	
	// ==========================================================================================================
	// == FIELDS

	public final static String LINE_WRAP = "\r\n";
	public final static String SENTENCE_SEPERATOR = "  ";
	
	// ==========================================================================================================
	// == INTERFACE
	
	/**
	 * Initialize it with a dictionary.
	 * @param dictionary
	 * @throws Throwable
	 */
	public void initialize(WordDictionary dictionary) throws Throwable {
		if (dictionary==null) ThingsException.softwareProblem("Cannot initialize a LanguageGenerator_Basic with a null dictionary.");
		this.dictionary = dictionary;
	}
	
	/**
	 * Get a word of random size.
	 * @return the word.
	 * @throws Throwable
	 */
	public String word() throws Throwable {
		return dictionary.get();
	}
	
	/**
	 * Get a word of specified size
	 * @param size In character count(not byte count).  It must be more than 0.
	 * @return the word.
	 * @throws Throwable
	 */
	public String word(int size) throws Throwable {
		return dictionary.get(size);
	}

	/**
	 * Get a sentence of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count), including whitespace.  It must be more than 2.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed sentence
	 * @throws Throwable
	 */
	public String sentence(int size, int wrap, String wrapPrepend) throws Throwable {
		AText result = new AText();
		sentence(result, size, wrap, wrapPrepend);
		return result.text.toString();
	}
	
	/**
	 * Get a sentence of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param target The sentence will be added to this.
	 * @param size In character count(not byte count), including whitespace.  It must be more than 2.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void sentence(AText target, int size, int wrap, String wrapPrepend) throws Throwable {
		String word;
		int realSize = size-1;
		boolean firstWord = true;
		int realWrap = wrap-1; // Always leave from for a final period.
		String wrapString;
		if (wrapPrepend==null) {
			wrapString = LINE_WRAP;
		} else {
			wrapString = LINE_WRAP + wrapPrepend;
		}
		
		if (wrap < 3) ThingsException.softwareProblem("A sentence wrap must be more than size 2.");
		if (size < 3) ThingsException.softwareProblem("A sentence must be more than size 2.");
		if (target==null) ThingsException.softwareProblem("Target for sentence is null.");
		
		int run = target.currentWrap;
		while (true) {
			
			// Next word.  
			if (realSize < WORD_CUTOFF_SIZE) 
				word = dictionary.get(realSize);		// Don't get a runt near the end.  Just take what is left.
			else
				word = dictionary.getMax(realSize);
			realSize-=word.length();
			
			// If there is room, occasionally we'll add a comma.
			if ((realSize > COMMA_THRESHOLD)&&(rng.percent(COMMA_CHANCE))) {
				word = word + ",";
				realSize--;
			}
			
			// BAD HACK, but I'm in a hurry
			if (firstWord) {
				word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
				firstWord = false;
			}
			
			// Wrap
			run += word.length();
			if ( run >= realWrap) {
				
				// Can we wrap with our wrap string and still have something?
				if (realSize < (wrapString.length()+1)) {
					
					// Not enough characters remaining to add the wrap string. 
					if (word.length() > wrapString.length()) {
						
						// Steal from the word.
						word = dictionary.getMax(word.length()-wrapString.length());
						target.text.append(wrapString);						
						realSize = 0;
						
					} else {
						
						// Can't steal from the word.  Ouch.  Just force something.
						switch(realSize + word.length()) {
						case 0:
							word = "";
							break;
						case 1:
							word = "X";
							break;
							
						case 2:
							target.text.append("\r\r\n");	
							target.currentWrap = 0;
							return;  // Don't stick around for the period.
							
						case 3:
							target.text.append("X.\r\n");	
							target.currentWrap = 0;
							return;
							
						case 4:
							target.text.append("at.\r\n");	
							target.currentWrap = 0;
							return;
						
						default:
							// This is sort of likely at this point.  I want to see what happens before I decide what to do here.
							ThingsException.softwareProblem("Erich's math is bad in sentence.");
						}
					}
					
				} else {
					
					// We can add the wrap.
					target.text.append(wrapString);
					realSize-=wrapString.length();
					run = word.length();
				}

			} else {
				
				// Not wrapping.
				run+=word.length();
			}
			
			// emit it
			target.text.append(word);
	
			// Are we done?  
			if (realSize < 1) {
				
				// Done.
				target.text.append('.');
				break;
				
			} else {
				
				// Not done.  Add whitespace.
				target.text.append(' ');
				realSize--;
				run++;
			}
			
		} //end while
		
		target.currentWrap = run;
		
	}
	
	/**
	 * Get a paragraph of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed paragraph.
	 * @throws Throwable
	 */
	public String paragraph(int size, int wrap, String wrapPrepend) throws Throwable {
		AText result = new AText();
		paragraph(result, size, wrap, wrapPrepend);
		return result.text.toString();	
	}
	
	/**
	 * Get a paragraph of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param target The sentence will be added to this.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void paragraph(AText target, int size, int wrap, String wrapPrepend) throws Throwable {
		int realSize = size;
		int sentenceSize = 0;
		
		// Qualify
		if (size < PARAGRAPH_MIN_SIZE) {
			ThingsException.softwareProblem("Paragraph size is too small.  size=" + size);
		}
		if (target==null) ThingsException.softwareProblem("Target for paragraph is null.");
		
		while(true) {
			
			// Pick a size
			if (realSize <= SENTENCE_MIN_SIZE) {
				sentenceSize = realSize;
			} else {
				
				// Randomly select the size
				if (rng.percent(75)) {
					// Bias the smaller sizes
					sentenceSize = rng.rng.nextInt(SENTENCE_SMALL_RANDOM) + SENTENCE_MIN_SIZE;
				} else {
					sentenceSize = rng.rng.nextInt(SENTENCE_LARGE_RANDOM) + SENTENCE_NORMAL_SIZE;	
				}
				
				// Take it all.
				//if (sentenceSize > realSize) sentenceSize = realSize;	
				
				// If the remaining is less than SENTENCE_MIN_SIZE, just do all that remains.
				if ((realSize-sentenceSize) < SENTENCE_MIN_SIZE) sentenceSize = realSize;
			}
			
			sentence(target, sentenceSize, wrap, wrapPrepend);
			realSize -= sentenceSize;
			
			if (realSize <= 0) {
				// Done
				break;
			} else {
				// This is safe because realSize will always have at least SENTENCE_MIN_SIZE or it will be 0.
				realSize-=SENTENCE_SEPERATOR.length();
				target.text.append(SENTENCE_SEPERATOR);
			}
			
		}	// end while
		
	}
	
	/**
	 * Get a page of paragraphs.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * Single or double CRLF will be put between paragraphs, but not at the end!
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed paragraph.
	 * @throws Throwable
	 */
	public String page(int size, int wrap, String wrapPrepend) throws Throwable {
		AText result = new AText();
		page(result, size, wrap, wrapPrepend);
		return result.text.toString();		
	}
	
	/**
	 * Get a page of paragraphs.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * Single or double CRLF will be put between paragraphs, but not at the end!
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param target The sentence will be added to this.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void page(AText target,  int size, int wrap, String wrapPrepend) throws Throwable {
		int realSize = size;
		int paragraphSize = 0;
		
		// Qualify
		if (size < PARAGRAPH_MIN_SIZE) ThingsException.softwareProblem("Size is too small in page().  size=" + size);
		if (target==null) ThingsException.softwareProblem("Target for page is null.");
		
		while(true) {
			
			// If we are getting near the minimum size, just take it all.
			if (realSize <= ((PARAGRAPH_MIN_SIZE*2)+10)) {
				paragraphSize = realSize;
			} else {
				
				// Randomly select the size as a percentage of what is left.
				paragraphSize = (int)((float)realSize * (float)(((float)rng.rng.nextInt(80)+10)/(float)100));
				if (paragraphSize < PARAGRAPH_MIN_SIZE) paragraphSize = PARAGRAPH_MIN_SIZE; // Just bad luck.
				if (realSize-paragraphSize <= (PARAGRAPH_MIN_SIZE*2))  paragraphSize = realSize; // If little remains after that, just take it.

			}
			
			paragraph(target, paragraphSize, wrap, wrapPrepend);
			realSize -= paragraphSize;
			
			if (realSize <= 0) {
				// Done
				break;
			} else {
				// This is safe because realSize will always have at least SENTENCE_MIN_SIZE or it will be 0.
				if (rng.flipcoin()) {
					// Single
					target.text.append(LINE_WRAP);
					realSize-=LINE_WRAP.length();
				} else {
					// Double
					target.text.append(LINE_WRAP);
					target.text.append(LINE_WRAP);
					realSize-=(LINE_WRAP.length()*2);
				}

			}
			
		}	// end while
	}
	
}

