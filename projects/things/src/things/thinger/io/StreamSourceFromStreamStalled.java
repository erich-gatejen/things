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
package things.thinger.io;

import java.io.EOFException;
import java.io.InputStream;

import things.common.ThingsException;

/**
 * A character stream source.  It will tee a single character, so be sure the underlying stream will starve it (pass EOF) if it should cross
 * a barrier and read something it isn't allowed to have.  See the HeaderProcessor for an example of how to do this.
 * <p>
 * I had to add this one to deal with socket streams, where you don't really know if you are at the end or not.  It will treat 
 * no available bytes the same as EOF.  This means it is somewhat sensitive to truncation on slow pipes.  There is a slight delay given if the available
 * count is 0 just to see if something shows up.
 * <p>
 * I really upped the delays, because network connections can seriously lag.  It's a pain.  The initial delay will always be way longer than 
 * subsequent reads.
 * <p>
 * Why not use timeouts, you ask?  We really don't know what kind of stream we are getting.  In all, a proper implementation would
 * probably deal directly with the channels and leave the layers stream abstractions out of it.  Since this is just a tool, I'm not 
 * going to bother.  Unfortunately, it makes the mini-http server a little slow.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial (toolkit) - 7 SEP 03
 * EPG - Played with the delays - 10 MAY 07
 * </pre> 
 */
public class StreamSourceFromStreamStalled implements StreamSource {

	/**
	 * This is the stall period in millis for the initialization.  It will be retried so many times.  I don't really know the best value for this.  
	 */
	public final static int INIT_STALL_PERIOD = 25;
	
	/**
	 * This is how much we increase the stall period for every iteration. 
	 */
	public final static int INIT_STALL_RAMP = 35;

	/**
	 * Maximum stall period for the initialization.  After this, give up.
	 */
	public final static int MAX_INIT_STALL = 10 * 60 * 1000;
	
	/**
	 * This is the stall period in millis.  It will be retried so many times.  I don't really know the best value for this.  
	 */
	public final static int STALL_PERIOD = 30;
	
	/**
	 * Maximum stall period for the initialization.  After this, give up.
	 */
	public final static int STALL_RETRIES = 100;
	
	// DATA
	private InputStream  ins;
	private int tee;
	
	/**
	 * A null constructor.  You must call reuse() before the other methods or they will throw exceptions.
	 */
	public StreamSourceFromStreamStalled() {
	}
	
	/**
	 * Constructor.  Pass the stream to source.
	 * @param source the input stream source.
	 */
	public StreamSourceFromStreamStalled(InputStream source) throws Throwable {
		reuse(source);
	}
	
	/**
	 * Reuse this object but with another stream.  Since we anticipate using a lot of these, this should
	 * spare some heap thrash.
	 * @param source The input stream source.
	 * @throws Exception
	 */
	public void reuse(InputStream source) throws Throwable {
		if (source == null) ThingsException.softwareProblem("Cannot call StreamSourceFromStreamStalled.reuse with a null source.");
		ins = source;
	
		// Anything available?
		if (ins.available() < 1) {
			
			// Loop the stall.
			int totalStall = 0;
			int stall = INIT_STALL_PERIOD;
			while ((totalStall <= MAX_INIT_STALL)&&(ins.available() < 1)) {
				totalStall +=  stall;
				try {
					Thread.sleep(stall);
				} catch (InterruptedException te) {
					throw te;	// Be nice to the OS.
				} catch (Throwable t) {
					// Don't care.
				}
				if (ins.available() > 0) break;
				stall+=INIT_STALL_RAMP;
				totalStall+=stall;
			}
			
			// Try again.
			if (ins.available() < 1) {

				// All empty.
				tee  = -1;
				
			} else {
				tee = ins.read();	
			}
			
		} else {
			tee = ins.read();			
		}
		
	}
	
	/**
	 * Get the next character.  It'll throw an EOFException if there is nothing left.  This 
	 * <p>
	 * This treats the available < 0 as EOF.
	 * @return the next character
	 * @throws Exception
	 */
	public int next() throws Exception {
		int result = tee;
			
		// Done?
		if (tee < 0) throw new EOFException("No more characters.");
		
		// Anything available?
		if (ins.available() < 1) {
			
			// Loop the stall.
			int tries = STALL_RETRIES;
			while ((tries >0)&&(ins.available() < 1)) {
				try {
					Thread.sleep(STALL_PERIOD);
				} catch (InterruptedException te) {
					throw te;	// Be nice to the OS.
				} catch (Throwable t) {
					// Don't care.
				}
				if (ins.available() > 0) break;
				tries--;
			}
			
			// Try again.
			if (ins.available() < 1) {

				// All empty.
				tee  = -1;
				
			} else {
				tee = ins.read();	
			}
			
		} else {
			tee = ins.read();			
		}
			
		return result;
	}
	
	/**
	 * It'll try a few times until something is available or it runs out of time.
	 * @return the number available.
	 * @throws Exception
	 */
	public int available() throws Exception {
		int result;
		// Loop the stall.
		int tries = STALL_RETRIES;
		result = ins.available();
		while ((tries >0)&&(result < 1)) {
			try {
				Thread.sleep(STALL_PERIOD);
			} catch (InterruptedException te) {
				throw te;	// Be nice to the OS.
			} catch (Throwable t) {
				// Don't care.
			}
			result = ins.available();
			tries--;
		}
		return result;

	}
	
	/**
	 * Does the source have more to get?
	 * @return true if it does, otherwise false.
	 */
	public boolean hasMore() throws Exception {
		if (tee < 0) return false;
		return true;
	}
	
}
