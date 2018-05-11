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

/**
 * A general odds tool.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite - 10 AUG 06
 * </pre> 
 */
public class Gamer {
	
	// ========================================================================================
	// = PRIVATE FIELDS
	private final static int MAX_THROW = 2000000000;	
	private final static int MID_THROW = 1000000000;	// 0 -> 999999999 is true, 1000000000 -> 1999999999 is false;
	
	// ========================================================================================
	// = FIELDS
	
	/**
	 * The Random source.  I can't remember why I left it public.
	 */
	public Random rng;
	
	
	// ========================================================================================
	// = METHODS
	
    /**
     * Construct the gamer.
     */
    public Gamer() {
    	rng = new Random();
    } 
    
    /**
     * Flip a coin, true or false.  
     * @return true or false, 50/50 chance of either.
     */
    public boolean flipcoin() {
    	// Maybe we can dodge from precision introduced problems.  Yeah, i'm a nut.
    	int guess = rng.nextInt(MAX_THROW);
    	if (guess < MID_THROW) return true;
    	return false;
    }
 
    /**
     * A percentage chance.  
     * @param chance the percentage change.  0 is never, 100 is always, and all chances in between.  other numbers will always yeild a lose (false).
     * @return true if win (within the percentage range), otherwise false.
     */
    public boolean percent(int  chance) {
    	if (chance == 100) return true;
    	if ((chance < 1)||(chance > 100)) return false;
    	if ( (rng.nextInt(100)+1) <= chance) return true;
    	return false;
    }

    /**
     * A range.  It'll use whatever you give it, even if it yields weird results.
     * @param lowest lowest number, inclusive
     * @param highest highest number, inclusive.
     * @return the number chosen.
     */
    public int range(int lowest, int highest) {
    	return lowest + rng.nextInt(highest - lowest);
    }
    
    /**
     * Pick one.
     * @param strings an array of Strings.
     * @return the string that we picked of null if the array is null or empty.
     */
    public String pick(String[]  strings) {
    	if ((strings==null)||(strings.length <1)) return null;
    	return strings[rng.nextInt(strings.length)];
    }
    
    /**
     * Pick one.
     * @param numbers an array of int.
     * @return the int that we picked or 0 if the array is null or empty.
     */
    public int pickNumber(int[]  numbers) {
    	if ((numbers==null)||(numbers.length <1)) return 0;
    	return numbers[rng.nextInt(numbers.length)];
    }

    
}