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
package things.thinger.kernel;

/**
 * Clearance management.  These define security levels used by the Kernel, Universes, and other services. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 13 FEB 06<p>
 * EPG - Break out from PCB - 15 DEC 06
 * </pre> 
 */
public enum Clearance {

	/**
	 * Security level definition for a process is the CLEARANCE.
	 * <p>
	 * <pre>
	 * The following defines the named and numbered values for the levels.<br>
	 * name                      value<br>
	 * FLOOR                     0<br>
	 * UNKNOWN                   1000<br>
	 * UNCLASSIFIED              2000<br>
	 * PUBLIC                    3000<br>
	 * PRIVATE                   4000<br>
	 * OFFICIAL                  4500<br>
	 * SENSITIVE                 5000<br>
	 * SECRET                    6000<br>
	 * PRIVILEGED                6500<br>
	 * TOPSECRET                 7000<br>
	 * RESTRICTED                7500<br>
	 * EXCLUSIVE                 8000<br>
	 * ELITE                     8500<br>
	 * EXTREME                   9000<br>
	 * TOP                       10000<br>
	 * </pre>
	 */
	FLOOR(0), UNKNOWN(1000), UNCLASSIFIED(2000), PUBLIC(3000), PRIVATE(4000), OFFICIAL(4500), 
	SENSITIVE(5000),SECRET(6000), PRIVILEGED(6500), TOPSECRET(7000), 
	RESTRICTED(7500), EXCLUSIVE(8000), ELITE(8500), EXTREME(9000), TOP(10000);
	
	// ===================================================================================
	// ENUM INTERNALS
	private final int rank;    
	private Clearance(int rank) { this.rank = rank; }
	protected final int value() {return rank; }
	
	// ===================================================================================
	// METHODS
	
	/**
	 * Does this clearance pass the given clearance?
	 * @param thanThis the given clearance.
	 * @return true if it passes, otherwise false.
	 */
	public boolean pass(Clearance thanThis) { 
		if (this.rank >= thanThis.value()) return true; return false; 
	}	
	
	/**
	 * Does this clearance not pass the given clearance?  This is just the reverse logic of pass(Clearance).
	 * @param thanThis the given clearance.
	 * @return false if it passes, otherwise true.
	 */
	public boolean dontpass(Clearance thanThis) { 
		if (this.rank < thanThis.value()) return true; return false; 
	}
	
}
