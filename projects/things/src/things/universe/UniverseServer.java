/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.universe;

import things.thinger.kernel.Clearance;

/**
 * Universe server interface.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <br>
 * <code>
 * EPG - New - 24 NOV 04
 * </code>
 */
public interface UniverseServer {

	/**
	 * Manufacture an accessor to a universe.
	 * @return a Universe
	 */
	public Universe getAccessor()
			throws UniverseException;

	/**
	 * Return the ID of this Universe.
	 * @return UniverseID of this universe
	 */
	public UniverseID getID();
	
	/**
	 * Get the required clearance.
	 * @return the Clearance.
	 * @see things.thinger.kernel.Clearance
	 */
	public Clearance getClearance();
}