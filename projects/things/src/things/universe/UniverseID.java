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
package things.universe;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Universe IDENITY.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 23 SEP 03
 * </pre> 
 */
public class UniverseID implements Serializable {

	public static final long serialVersionUID=1;
	
	private String 	myName;
	private String  myId;

	private static long cache_network;
	private static String hex;
	private static Object blocker = new Object();

	/**
	 * Create as a long ID from what we know about the system.
	 */
	public UniverseID() {
		myId = new String ("ID." + network());
		myName = myId;
	}

	/**
	 * Create as an ID based on a string.
	 * @param name   The name.  It should be locally unique.
	 */
	public UniverseID(String   name) {
		myId = new String (name + "@" + network());
		myName = name;
	}

	/**
	 * See if this object is equal to the passes object.
	 * @param other
	 *             object to compare
	 * @return true if they are equal, otherwise false
	 */
	public boolean equals(UniverseID other) {
		if (other.toString().equals(myId))
			return true;
		return false;
	}

	/**
	 * Get the universe ID expressed as a string in hex
	 * 
	 * @return the universe ID (as a String)
	 */
	public String toString() {
		return myId;
	}
	
	/**
	 * Get the local name of the universe.
	 * @return the name.
	 */
	public String getName() {
		return myName;
	}
	
	// HELPERS

	private long network() {
		if (hex == null) {
			synchronized (blocker) {
				try {
					InetAddress inetA = InetAddress.getLocalHost();
					byte[] ip = inetA.getAddress();
					// BAD cheat
					cache_network = ip[0] + (ip[1] * 256) + (ip[2] * 65526) + (ip[3] * 16777216);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return cache_network;
		} else {
			return cache_network;
		}
	}

}