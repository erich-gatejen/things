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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import things.common.ThingsException;
import things.common.tools.TokenFactory;
import things.data.processing.Decomposer;

/**
 * Create a word dictionary.  Regardless of what you load, there will always be at least one word per word size 
 * in characters from 1 character to the max size characters.
 * <p>  
 * <b>THIS PACKAGE WAS ABANDONED IN FAVOR OF ANOTHER PROJECT</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial -10 MAY 06
 * </pre> 
 */
public class WordDictionary implements Translator {

	// ========================================================================================
	// FIELDS
	
	/**
	 * Maximum supported word size.
	 * 
	 */
	public final static int MAXSIZE = 36;
	
	// ========================================================================================
	// INTERNAL DATA
	
	private HashSet<String> masterBucket;
	private ArrayList<ArrayList<String>> sizeBuckets;
	private HashMap<String,String> rememberedTranslations;
	private TokenFactory tokaner;
	private Random rng;
	private int[] sizeCache;
	private int[] frequencyChart;

	// ========================================================================================
	// METHODS
	
	/**
	 * A  ready to do stream from which WS delimited words can be found.  Buffer it if necessary.
	 * This is memory hungry, since we'll be converting a hashset to a array for every word size.
	 * @param wordSource a stream to the word source.
	 * @throws Throwable for any failure.
	 */
	public WordDictionary(InputStream	wordSource) throws Throwable {
		if (wordSource==null) ThingsException.softwareProblem("wordSource cannot be null.");
		
		rng = new Random();
		masterBucket = new HashSet<String>();
		tokaner = new TokenFactory(MAXSIZE, false, false);
		rememberedTranslations = new HashMap<String,String>();
		
		frequencyChart = defaultFrequencyChart;
	
		ArrayList<HashSet<String>> tempBuckets = new ArrayList<HashSet<String>>(MAXSIZE);
		sizeBuckets = new ArrayList<ArrayList<String>>(MAXSIZE);
		sizeCache = new int[MAXSIZE];
		for (int index = 0; index < MAXSIZE; index++) {
			sizeBuckets.add(new ArrayList<String>());
			tempBuckets.add(new HashSet<String>());
		}
		
		try {
			Decomposer decomposer = new Decomposer();
			decomposer.start(wordSource);
			String working = null;
			String token = decomposer.token();
			while (token != null) {
				
				working = token.trim();
				masterBucket.add(working);
				if ((working.length() > 0)&&(working.length()<=MAXSIZE)) {
					tempBuckets.get(working.length()-1).add(working);
				}
				
				// Do not alter below this line
				token = decomposer.token();
			}
			
			// Convert all the buckets and make sure they all have at least one word.
			for (int index = 0; index < MAXSIZE; index++) {
				sizeBuckets.get(index).addAll(tempBuckets.get(index));
				sizeCache[index] = sizeBuckets.get(index).size();
				if (sizeCache[index] < 1) {
					sizeBuckets.get(index).add(tokaner.randomTokenANS(index+1));
					sizeCache[index]++;
				}
			}
	
		} catch (Throwable t) {
			throw new ThingsException("Failed to load words.", ThingsException.DECOMPOSER_FAULT, t);
			
		}
	}
	
	/**
	 * Refresh what we've remembered.
	 */
	public void refreshMemory() {
		rememberedTranslations = new HashMap<String,String>();
	}
	
	/**
	 * Get a word of random size.
	 * @return the word
	 * @throws Throwable
	 */
	public String get() throws Throwable {
		return get(rng.nextInt(MAXSIZE)-1);
	}
	
	/**
	 * Get a word of random size up to the specified maximum.  It still cannot be bigger than MAXSIZE.  It will use
	 * the configured frequency chart for picking the size.
	 * @param max max size.
	 * @return the word
	 * @throws Throwable
	 */
	public String getMax(int max) throws Throwable {
		return getMax(max, frequencyChart);
	}
	
	/**
	 * Get a word of random size up to the specified maximum.  It still cannot be bigger than MAXSIZE.   It will use
	 * the passed frequency chart for picking the size.
	 * @param max max possible size.
	 * @param chart use this frequency chart instead of the configured.
	 * @return the word
	 * @throws Throwable
	 */
	public String getMax(int max, int[]	chart) throws Throwable {
		int size = chart[rng.nextInt(100)];
		int loop = 0;
		while (size > max) {
			size = chart[rng.nextInt(100)];
			
			// runaway protection
			loop++;	
			if (loop>5) size = max;
		}
		return get(size);
	}
	
	/**
	 * Get a word of specified size.  It still cannot be bigger than MAXSIZE.
	 * @param size if less than one, it'll return an empty string.  If there is no words of the specified size, it returns an empty string.
	 * @return the word
	 * @throws Throwable
	 */
	public String get(int size) throws Throwable {
		String result = null;
		
		try {

			if (size < 1) return "";
			int sizeBucket;
			if (size > MAXSIZE) sizeBucket = MAXSIZE-1;
			else sizeBucket = size-1;
			
			// Randomly select a word from the bucket.
			if (sizeCache[sizeBucket] < 1) return "";
			result = sizeBuckets.get(sizeBucket).get(rng.nextInt(sizeCache[sizeBucket]));
				
		} catch (Throwable t) {
			// This will always be bad usage.
			throw new ThingsException("Spurious translation exception", ThingsException.DECOMPOSER_LOOKUP_ERROR, t);
		}
		
		return result;
	}
	
	
	/**
	 * Translate a word.  It will be the same size unless it exceeds a maximum size (as defined by the implementation) in which case the translated word 
	 * of the maximum size.
	 * @param word The word to translate.
	 * @return the translated word or null if passed null.
	 * @throws Throwable
	 */
	public String translate(String word) throws Throwable {
		String result = null;
		
		try {
		
			// Available?
			if (rememberedTranslations.containsKey(word)) return rememberedTranslations.get(word);
			 
			// Randomly select a dictionary word the same size
			int wordSize = word.length();
			if (wordSize < 1) return "";
			if (wordSize > MAXSIZE) wordSize = MAXSIZE;
			
			// Randomly select a word from the bucket.
			int selected = rng.nextInt(sizeCache[wordSize-1]);
			result = sizeBuckets.get(wordSize-1).get(selected);
			rememberedTranslations.put(word, result);
				
		} catch (Throwable t) {
			// This will always be bad usage.
			if (word==null) throw new ThingsException("You cannot use a null word.");
			throw new ThingsException("Spurious translation exception", ThingsException.DECOMPOSER_LOOKUP_ERROR, t);
		}
		
		return result;
	}
	
	/**
	 * Set the frequency chart.  There is a default geared to English if you don't set it.
	 * @param chart It must be exactly 100 entries.  The size will be chosen randomly from one of the hundred.
	 * @throws Throwable if null or not 100 entries.
	 */
	public void setFrequencyChart(int[] chart) throws Throwable {
		if (chart==null)  ThingsException.softwareProblem("The frequency chart cannot be null.");
		if (chart.length != 100) ThingsException.softwareProblem("The frequency chart must be exactly 100 entries.  This chart had " + chart.length + " entries.");
		
	}
	
	// =========================================================================================
	// = INTERNAL
	
	/**
	 * The default size frequency chart for random selection.  IT is geared towards English.
	 */
	public static int 	defaultFrequencyChart[] = {
			1,
			1,
			1,
			1,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			6,
			6,
			6,
			6,
			6,
			6,
			6,
			6,
			7,
			7,
			7,
			7,
			7,
			7,
			8,
			8,
			8,
			8,
			1,
			9,
			9,
			9,
			1,
			10,
			10,
			10,
			11,
			11,
			11,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			20,
			21,
			22,
			23,
			24,
			25
	};
	
	/**
	 * The first name size frequency chart for random selection.  It is geared towards English.
	 */
	public static int 	firstnameFrequencyChart[] = {
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			2,
			2,
			2,
			2,
			2,
			2,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			3,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			5,
			6,
			6,
			6,
			6,
			6,
			6,
			6,
			6,
			7,
			7,
			7,
			7,
			7,
			7,
			8,
			8,
			8,
			8,
			5,
			9,
			9,
			9,
			5,
			10,
			7,
			5,
			6,
			11,
			6,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			6,
			6,
			5,
			5,
			5,
			5
	};
	
	/**
	 * The last name size frequency chart for random selection.  It is geared towards English.
	 */
	public static int 	lastnameFrequencyChart[] = {
			3,
			3,
			8,
			12,
			3,
			7,
			7,
			7,
			3,
			8,
			8,
			8,
			8,
			2,
			2,
			3,
			3,
			3,
			3,
			3,
			9,
			9,
			6,
			6,
			3,
			6,
			6,
			3,
			3,
			3,
			3,
			3,
			11,
			3,
			3,
			4,
			4,
			4,
			5,
			4,
			5,
			4,
			4,
			4,
			4,
			4,
			4,
			4,
			5,
			5,
			5,
			14,
			5,
			5,
			5,
			5,
			5,
			6,
			6,
			6,
			6,
			6,
			10,
			6,
			6,
			7,
			7,
			7,
			7,
			7,
			7,
			8,
			8,
			8,
			8,
			5,
			9,
			9,
			9,
			5,
			10,
			7,
			5,
			6,
			11,
			6,
			12,
			13,
			14,
			15,
			16,
			17,
			18,
			19,
			6,
			6,
			11,
			10,
			5,
			9
	};
}
