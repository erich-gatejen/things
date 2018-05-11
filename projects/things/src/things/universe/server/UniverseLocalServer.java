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
package things.universe.server;

import things.thinger.kernel.Clearance;
import things.universe.Universe;
import things.universe.UniverseException;
import things.universe.UniverseID;
import things.universe.UniverseServer;

/**
 * Universe server management class.  There is no reason why anything other than
 * the universe system should use this.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre
 * EPG - New - 24 NOV 04
 * EPG - Add Clearance - 3 DEC 06
 * </pre>
 */
public class UniverseLocalServer  implements UniverseServer {
	
	// ====================================================================================
	// DATA
	public String root;
	public UniverseID id;
	public Clearance requiredClearance;
	
	// ====================================================================================
	// METHODS
	
	/**
	 * Manufacture an accessor to a universe.
	 * @return a Universe
	 */
	public Universe getAccessor()
			throws UniverseException {
	    UniverseLocal ul = new UniverseLocal();
	    ul.genesis(root,id);
	    return ul;
	}

	/**
	 * Return the ID of this Universe
	 * @return UniverseID of this universe
	 */
	public UniverseID getID() {
	    return id;
	}
	
	/**
	 * Get the required clearance.
	 * @return the Clearance.
	 * @see things.thinger.kernel.Clearance
	 */
	public Clearance getClearance() {
		return requiredClearance;
	}
}
