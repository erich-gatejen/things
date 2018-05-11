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

import things.common.Verbose;
import things.data.ThingsPropertyView;
import things.thinger.kernel.Clearance;


/**
 * Universe registry interface. The implementation is responsible for
 * maintaining the configuration of universes and manufacturing of access
 * objects.
 * <p>
 * There are two ways to refer to a Universe-UniverseId or name.  The UniverseId is globally valid
 * can be expressed as an object or a String.  A name is a local unique String name that can 
 * only be used with a single registry.
 * <p>
 * Universe objects can found with a URI that is <Name>:<Path> or <UniverseId>:<Path>
 * <p>
 * There are four types of universes: <br>
 * UNIVERSE_LOCAL - Local system <br>
 * UNIVERSE_DEFAULT - Alias for the local default <br>
 * <p>
 * The implementation will decide the method of configuration.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 04
 * </pre> 
 */
public interface UniverseRegistry extends Verbose {

	/**
	 * Types of universe
	 */
	public static final int UNIVERSE_INVALID = 0;
	public static final int UNIVERSE_LOCAL = 1;
	public static final String UNIVERSE_LOCAL_name = "local";

	/**
	 * Get an accessor to a universe.
	 * 
	 * @param theUniverse
	 *            ID for the universe to access
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Universe getAccessor(UniverseID theUniverse) throws UniverseException;
	
	/**
	 * Get an accessor to a universe by local name.
	*
	 * @param name the name
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Universe getAccessor(String name) throws UniverseException;

	/**
	 * Get an clearance for a universe by id.
	 * 
	 * @param theUniverse  ID for the universe to access
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Clearance getClearance(UniverseID theUniverse) throws UniverseException;
	
	/**
	 * Get an clearance for a universe by localName
	*
	 * @param name the local name
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Clearance getClearance(String name) throws UniverseException;
	
	/**
	 * Register a universe.
	 * 
	 * @param type
	 *            numeric as specific in this class
	 * @param config
	 *            information
	 * @throws UniverseException
	 */
	public void register(int type, ThingsPropertyView config) throws UniverseException;

	/**
	 * Load a registry from a configuration node. See documentation above for
	 * expected properties.
	 * 
	 * @param config
	 *            a configuration node
	 * @throws UniverseException
	 * @see things.data.ThingsPropertyView
	 */
	public void loadRegistry(ThingsPropertyView config) throws UniverseException;

	/**
	 * Safety the Registry. If the configuration node supports it, the registry
	 * will be checkpointed and/or saved. This effectively sets the last known
	 * good configuration.
	 * 
	 * @throws UniverseException
	 */
	public void safetyTheRegistry() throws UniverseException;
}