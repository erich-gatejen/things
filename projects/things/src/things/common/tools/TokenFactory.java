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
package things.common.tools;

import java.util.Random;

import things.data.FlaggedString;

/**
 * A set of tools for creating tokens.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial, integrated from another project. - 26 NOV 04<br>
 * EPG - Converted to use Gamer and added methods. - 11 AUG 06
 * </pre> 
 */
public class TokenFactory extends Gamer {
	
	// ========================================================================================================================
	// = PUBLIC FIELDS
	
	/**
	 * The max sets allowed.
	 */
	public final static int MAX_SETS = 10;
	
	// ========================================================================================================================
	// = INTERNAL DATA	
	private int top;
	private int bottom;
	private int MAX_SIZE;
	private int THROW_SIZE;
	
	// ========================================================================================================================
	// = CONFIGURATION	
	
	/**
	 * Smallest token possible.
	 */
	public static final int MIN_SIZE = 2;
	
	// ========================================================================================================================
	// = METHODS	
	
    /**
     * Construct the factory.
     * @param maxSize maximum size of the token, must be at least 3.  The minimum size is automatically 2.
     * @param highASCII if true, it will include chars over 127
     * @param lowASCII if true, it will include chars between 1 and 32 (inclusive)
     */
    public TokenFactory(int  maxSize, boolean highASCII, boolean lowASCII) {
  
    	top = 127;
    	bottom = 33;
    	if (highASCII) top = 255;
    	if (lowASCII) bottom = 1;
    	if (maxSize <= MIN_SIZE) {
    		MAX_SIZE = MIN_SIZE + 1;
    	} else {
    		MAX_SIZE = maxSize;
    	}
    	THROW_SIZE = MAX_SIZE - MIN_SIZE;
    	
    	rng = new Random();
    } 

    /**
     * Get a random token of random size.  It will abide by the construction configuration.
     * @return the token.
     */
    public String randomToken() {
    	return randomToken(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    } 

    /**
     * Get a random token of specified size.  It will abide by the construction configuration.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token.
     */
    public String randomToken(int size) {
    	int span = top-bottom;
		StringBuffer token = new StringBuffer();	
		for (int index = 0; index < size; index++) {
			token.append((char)(rng.nextInt(span) + bottom));
		}	
    	return token.toString();
    }
    
    /**
     * Get a random alphanumeric token of random size.
     * @return the token.
     */
    public String randomTokenAN() {
    	return randomTokenAN(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    }
    
    /**
     * Random alphanumeric token of a specific size.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token.
     */
    public String randomTokenAN(int size) {
		StringBuffer token = new StringBuffer();	
		for (int index = 0; index < size; index++) {
			token.append(alphaNumericCharacters[rng.nextInt(alphaNumericCharacters.length)]);
		}			
    	return token.toString();
    }
    
    /**
     * Get a random numeric token of random size.
     * @return the token.
     */
    public String randomTokenN() {
    	return randomTokenAN(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    }
    
    /**
     * Random numeric token of a specific size.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token.
     */
    public String randomTokenN(int size) {
		StringBuffer token = new StringBuffer();	
		for (int index = 0; index < size; index++) {
			token.append(numericCharacters[rng.nextInt(numericCharacters.length)]);
		}			
    	return token.toString();
    }
    
    /**
     * Random alphanumeric token of random size with a chance of spaces and a few benign punctuation.
     * @return the token.
     */
    public String randomTokenANS() {
    	return randomTokenANS(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    }
    
    /**
     *  Random alphanumeric token of random size with a chance of spaces and a few benign punctuation of a specific size.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token.
     */
    public String randomTokenANS(int size) {
		StringBuffer token = new StringBuffer();		
		for (int index = 0; index < size; index++) {
			token.append(alphaNumericCharactersS[rng.nextInt(alphaNumericCharactersS.length)]);
		}			
    	return token.toString();
    }
    
    /**
     * Random alphanumeric token of specified sets.  The sets # of tokens will be concatenated into one token.
     * @param sets the number of sets.  If it is less than one or greater than MAX_SETS, it'll treat it as 1.  
     * @return the token.
     */
    public String randomTokenANSets(int sets) {
    	// Trivial case.
    	if ((sets < 1)||(sets > MAX_SETS)) return randomTokenAN();
    	
    	StringBuffer result = new StringBuffer();
    	for (int index = 0 ; index < sets ; index++) result.append(randomTokenAN());
    	return result.toString();
    } 
    
    /**
     * Random hex token of random size.
     * @return the token.
     */
    public String randomHex() {
    	return randomHex(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    } 
    
    /**
     * Random hex token of a specific size.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token.
     */
    public String randomHex(int size) {
		StringBuffer token = new StringBuffer();	
		for (int index = 0; index < size; index++) {
			token.append(hexCharactersLower[rng.nextInt(hexCharactersLower.length)]);
		}			
    	return token.toString();
    } 

    /**
     * Random hex token of specified sets.  The sets # of tokens will be concatenated into one token.
     * @param sets the number of sets.  If it is less than one or greater than MAX_SETS, it'll treat it as 1.  
     * @return the token.
     */
    public String randomHexSets(int sets) {
    	// Trivial case.
    	if ((sets < 1)||(sets > MAX_SETS)) return randomHex();
    	
    	StringBuffer result = new StringBuffer();
    	for (int index = 0 ; index < sets ; index++) result.append(randomHex());
    	return result.toString();
    } 
    
    /**
     * Get a random, flagged token appropriate for rfc822 fields (such as mail addresses) of specfied size.
     * @param spacePercentage the percent chance for character that it'll be a space character.
     * @param specialPecentage the percent chance for character that it'll be a 822 'special' character.
     * @param quotablePercentage the percent chance for character that it'll be a 822 'quotable' character (besides spaces).
     * @return a flagged string.  Be sure to quote it in an address if hasWhiteSpace() or hasQuotable() is true.
     */
    public FlaggedString randomToken822(int spacePercentage, int specialPecentage, int quotablePercentage) {
    	return randomToken822(rng.nextInt(THROW_SIZE)+ MIN_SIZE, spacePercentage, specialPecentage, quotablePercentage);
    }
    
    /**
     * Get a random, flagged token appropriate for rfc822 fields (such as mail addresses) of specfied size.
     * @param spacePercentage the percent chance for character that it'll be a space character.
     * @param specialPercentage the percent chance for character that it'll be a 822 'special' character.
     * @param quotablePercentage the percent chance for character that it'll be a 822 'quotable' character (besides spaces).
     * @return a flagged string.  Be sure to quote it in an address if hasWhiteSpace() or hasQuotable() is true.
     */
    public FlaggedString randomToken822(int size, int spacePercentage, int specialPercentage, int quotablePercentage) {
    	
    	// Data
    	int item;
    	boolean hasQuotable = false;
    	boolean hasWhiteSpace = false;
		StringBuffer token = new StringBuffer();
    	
		// Run it by brute force.  
		for (int index = 0; index < size; ) {
			item = rng.nextInt(rfc822Chars.length);
			switch (rfc822Chars[item][1]) {
			
			case RFC822_NOP:
				token.append((char)rfc822Chars[item][0]);
				index++;				
				break;
				
			case RFC822_NOTFIRST:
				if (index > 0) {
					token.append((char)rfc822Chars[item][0]);
					index++;						
				}
				break;
				
			case RFC822_QUOTE:
				if (this.percent(quotablePercentage)) {
					token.append((char)rfc822Chars[item][0]);
					index++;				
					hasQuotable = true;
				}
				break;
				
			case RFC822_SPACE:
				if (this.percent(spacePercentage)) {
					token.append((char)rfc822Chars[item][0]);
					index++;				
					hasWhiteSpace = true;
				}
				break;
				
			case RFC822_SPECIAL:
				if (this.percent(specialPercentage)) {
					token.append((char)rfc822Chars[item][0]);
					index++;					
				}
				break;	
				
			
			} // end switch
			
		} // end for
			
    	return  new FlaggedString(token.toString(), hasWhiteSpace, hasQuotable);
    } 
    
    /**
     * Create a random token of random size with only valid DNS characters.
     * @return the token as a string
     */
    public String randomTokenDNS() {
    	return randomTokenDNS(rng.nextInt(THROW_SIZE)+ MIN_SIZE);
    } 
    
    /**
     * Create a random token of specified size with only valid DNS characters.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token as a string
     */
    public String randomTokenDNS(int size) {
    	StringBuffer token = new StringBuffer();
        for (int index = 0; index < size; index++) {
            token.append((char)dnsChars[rng.nextInt(dnsChars.length)]);
        }
        return token.toString();
    }
    
    /**
     * Create a random token of specified size with only valid BASE64 characters.
     * @param size the size.  If less than 1, it'll return an empty token.  It will ignore the maxSize specified at construction.
     * @return the token as a string
     */
    public String randomTokenBASE64(int size) {
    	StringBuffer token = new StringBuffer();
        for (int index = 0; index < size; index++) {
            token.append((char)base64Chars[rng.nextInt(base64Chars.length)]);
        }
        return token.toString();
    }
	
	/**
	 * A sentence of randomly generated fake words.
	 * @param maxWords absolute max words
	 * @param wordPercentChance percent chance after each word that we'll do another word.
	 * @param maxSize maximum total size.
	 * @return the sentence.
	 */
	public String sentence(int maxWords, int wordPercentChance, int maxSize) {
		StringBuffer result = new StringBuffer();
		
		int words = 0;
		int wordSize;
		while(words < maxWords) {
			wordSize = rng.nextInt(8) + rng.nextInt(3) + 1;
			if ((wordSize+result.length()) >= maxSize) {
				wordSize = maxSize - result.length();
				result.append(randomTokenANS(wordSize));
				break;
			}
			result.append(randomTokenANS(wordSize));
			if (!percent(wordPercentChance)) break;	// No more words!
			result.append(' ');
		}
		
		return result.toString();
	}
	
	/**
	 * Get a random alpha character.
	 * @return the character.
	 */
	public char randomACharacter() {
		return alphaCharacters[rng.nextInt(alphaCharacters.length)];
	}
    
	/**
	 * Get a random numeric character.
	 * @return the character.
	 */
	public char randomNCharacter() {
		return numericCharacters[rng.nextInt(numericCharacters.length)];
	}
	
	// ========================================================================================================================
	// = INTERNAL TABLES
    
    public final static char alphaCharacters[] = {
    	'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };
	
    public final static char numericCharacters[] = {
    	'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };
    
    public final static char alphaNumericCharacters[] = {
    	'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    	'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };
    
    public final static char alphaNumericCharactersS[] = {
    	'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    	'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    	'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', ' ', '.', ',' , '-'
    };
    
    public final static char alphaNumericCharactersLower[] = {
    	'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    	'1', '2', '3', '4', '5', '6', '7', '8', '9', '0'
    };
    
    private static final byte[] dnsChars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
        '7', '8', '9', '.', '-'};
    
    private static final byte[] base64Chars = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', '+', };
	
    public final static char hexCharactersLower[] = {
    	'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };
    
    private final static byte RFC822_NOP = 0;
    private final static byte RFC822_SPECIAL = 1;
    private final static byte RFC822_QUOTE = 2;
    private final static byte RFC822_SPACE = 3;
    private final static byte RFC822_NOTFIRST = 4;
    private static final byte[][] rfc822Chars = { 
    	{'A', RFC822_NOP},
    	{'B', RFC822_NOP},
    	{'C', RFC822_NOP},
    	{'D', RFC822_NOP},
    	{'E', RFC822_NOP},
    	{'F', RFC822_NOP},
    	{'G', RFC822_NOP},
    	{'H', RFC822_NOP},
    	{'I', RFC822_NOP},
    	{'J', RFC822_NOP},
    	{'K', RFC822_NOP},
    	{'L', RFC822_NOP},
    	{'M', RFC822_NOP},
    	{'N', RFC822_NOP},
    	{'O', RFC822_NOP},
    	{'P', RFC822_NOP},
    	{'Q', RFC822_NOP},
    	{'R', RFC822_NOP},
    	{'S', RFC822_NOP},
    	{'T', RFC822_NOP},
    	{'U', RFC822_NOP},
    	{'V', RFC822_NOP},
    	{'W', RFC822_NOP},
    	{'X', RFC822_NOP},
    	{'Y', RFC822_NOP},
    	{'Z', RFC822_NOP},
    	{'a', RFC822_NOP},
    	{'b', RFC822_NOP},
    	{'c', RFC822_NOP},
    	{'d', RFC822_NOP},
    	{'e', RFC822_NOP},
    	{'f', RFC822_NOP},
    	{'g', RFC822_NOP},
    	{'h', RFC822_NOP},
    	{'i', RFC822_NOP},
    	{'j', RFC822_NOP},
    	{'k', RFC822_NOP},
    	{'l', RFC822_NOP},
    	{'m', RFC822_NOP},
    	{'n', RFC822_NOP},
    	{'o', RFC822_NOP},
    	{'p', RFC822_NOP},
    	{'q', RFC822_NOP},
    	{'r', RFC822_NOP},
    	{'s', RFC822_NOP},
    	{'t', RFC822_NOP},
    	{'u', RFC822_NOP},
    	{'v', RFC822_NOP},
    	{'w', RFC822_NOP},
    	{'x', RFC822_NOP},
    	{'y', RFC822_NOP},
    	{'z', RFC822_NOP},
    	{'0', RFC822_NOP},
    	{'1', RFC822_NOP},
    	{'2', RFC822_NOP},
    	{'3', RFC822_NOP},
    	{'4', RFC822_NOP},
    	{'5', RFC822_NOP},
    	{'6', RFC822_NOP},
    	{'7', RFC822_NOP},
    	{'8', RFC822_NOP},
    	{'9', RFC822_NOP},      
    	{'!', RFC822_SPECIAL},
    	{'#', RFC822_SPECIAL},
    	{'$', RFC822_SPECIAL},
    	{'%', RFC822_SPECIAL},
    	{'&', RFC822_SPECIAL},
    	{'*', RFC822_SPECIAL},
    	{'+', RFC822_SPECIAL},
    	{'-', RFC822_SPECIAL},
    	{'?', RFC822_SPECIAL},
    	{'=', RFC822_SPECIAL},
    	{'_', RFC822_SPECIAL},
    	{'`', RFC822_SPECIAL},
    	{'{', RFC822_SPECIAL},
    	{'}', RFC822_SPECIAL},
    	{'|', RFC822_SPECIAL},
    	{'~', RFC822_SPECIAL},    
    	{'(', RFC822_QUOTE},
    	{')', RFC822_QUOTE},
    	{'<', RFC822_QUOTE}, 
    	{'>', RFC822_QUOTE},
    	{'@', RFC822_QUOTE},
    	{';', RFC822_QUOTE},
    	{':', RFC822_QUOTE},
    	{']', RFC822_QUOTE},       
    	{' ', RFC822_SPACE},  
    	{'.', RFC822_NOTFIRST},
    	{'^', RFC822_NOTFIRST},
    	{'\'', RFC822_NOTFIRST}
    
    };
    
    
}