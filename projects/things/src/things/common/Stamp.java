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
package things.common;

import java.io.Serializable;

/**
 * A immutable stamp.  It is an ID, time, and token identifier.  This is safe within this system only.  Do
 * not use this for security. <br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 OCT 04
 * </pre> 
 */
public class Stamp implements Serializable  {

	private String  theStamp;
	private long timeStamp;
	final static long serialVersionUID = 1;
	
	private static Long centerOfTheUniverse;
	// Create the center of the universe
	static {
		centerOfTheUniverse = new Long(1);
	}
	
	/**
	 * Default constructor.  DO NOT USE!  It will throw a ThingsException every time.
	 * @see things.common.ThingsException
	 */
	public Stamp() throws ThingsException {
		ThingsException.softwareProblem("Stamp cannot be constructed with the Default Constructor.  You must call with the ID");
	}

	/**
	 * Construct with an imposed ID.
	 * @param callerID The WhoAmI for the caller.
	 * @param theToken Any string token the creator wants to pass.
	 * @see things.common.WhoAmI
	 */
	public Stamp(WhoAmI callerID, String theToken) throws ThingsException {
		
		StringBuffer working = new StringBuffer();
		
		try {
			timeStamp = System.currentTimeMillis();
			working.append(callerID.toString() + ThingsConstants.CODEC_SEPARATOR_CHARACTER);
			working.append(theToken + ThingsConstants.CODEC_SEPARATOR_CHARACTER);
			working.append( timeStamp + ".");
		} catch (Throwable ee) {
			throw new ThingsException("Stamp creation failed.", ThingsException.SYSTEM_FAULT_STAMP_CREATION_FAILED, ee);
		}
		
		// Get the slew.
		synchronized(centerOfTheUniverse) {
			working.append(centerOfTheUniverse.toString());
			centerOfTheUniverse++;
		}
		
		// Set it
		theStamp = working.toString();
	}
	
	/**
	 * Get a string version of the stamp.
	 * @return the textual representation of the stamp.
	 */
	 public String toString() {
		 return theStamp;
	 }
	 
	 /**
	  * Get the time stamp.
	  * @return the timestamp in milliseconds.
	  */
	 public long getTimestamp() {
		 return timeStamp;
	 }
	
}
