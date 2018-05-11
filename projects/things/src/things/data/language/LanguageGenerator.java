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

/**
 * Language generator interface.
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
public interface LanguageGenerator {
	
	/**
	 * Initialize it with a dictionary.
	 * @param dictionary
	 * @throws Throwable
	 */
	public void initialize(WordDictionary dictionary) throws Throwable;
	
	/**
	 * Get a word of random size.
	 * @return the word
	 * @throws Throwable
	 */
	public String word() throws Throwable;
	
	/**
	 * Get a word of specified size
	 * @param size In character count(not byte count).  It must be more than 0.
	 * @return The word.
	 * @throws Throwable
	 */
	public String word(int size) throws Throwable;

	/**
	 * Get a sentence of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count), including whitespace.  It must be more than 2.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed sentence
	 * @throws Throwable
	 */
	public String sentence(int size, int wrap, String wrapPrepend) throws Throwable;
	
	/**
	 * Get a sentence of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param target The sentence will be added to this.
	 * @param size In character count(not byte count), including whitespace.  It must be more than 2.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void sentence(AText target, int size, int wrap, String wrapPrepend) throws Throwable;
	
	/**
	 * Get a paragraph of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed paragraph.
	 * @throws Throwable
	 */
	public String paragraph(int size, int wrap, String wrapPrepend) throws Throwable;
	
	/**
	 * Get a paragraph of specified size.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param target The sentence will be added to this.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void paragraph(AText target, int size, int wrap, String wrapPrepend) throws Throwable;
	
	/**
	 * Get a page of paragraphs.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * Single or double CRLF will be put between paragraphs, but not at the end!
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @return the completed paragraph.
	 * @throws Throwable
	 */
	public String page(int size, int wrap, String wrapPrepend) throws Throwable;
	
	/**
	 * Get a page of paragraphs.  It will have language appropriate punctuation and whitespace, which is included in the size count.
	 * Single or double CRLF will be put between paragraphs, but not at the end!
	 * @param size In character count(not byte count).  It must be more than PARAGRAPH_MIN_SIZE.
	 * @param target The sentence will be added to this.
	 * @param wrap Line wrap size.   It must be more than 3.
	 * @param wrapPrepend if not null, prepend this after each line wrap.
	 * @throws Throwable
	 */
	public void page(AText target, int size, int wrap, String wrapPrepend) throws Throwable;
	
}

