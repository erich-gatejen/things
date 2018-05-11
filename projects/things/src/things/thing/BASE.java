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
package things.thing;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.Accessor;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyView;
import things.thinger.SystemInterface;
import things.thinger.io.Logger;
import things.universe.Universe;

/**
 * Base for things, modules, and tigers--oh my!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB07
 * </pre> 
 */
public abstract class BASE implements BASEInterface {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	
	// SYSTEM INFORMATION
	protected ThingsPropertyView  localProperties;
	protected Logger localSystemLogger;
	protected SystemInterface mySystemInterface;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public abstract String GET_NAME();
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
		
	/**
	 * Initialize system data.  This should be called by the init() method in super class.
	 * @param si A system interface.  
	 * @throws UserException which will always be a FAULT.
	 */
	public final void systemInit(SystemInterface	si) throws UserException {
		if (si==null) throw new UserException("Failure to initialize caused a FAULT.  Cannot pass null SystemInterface.", ThingsCodes.SYSTEM_FAULT_FAILED_INIT, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
		
		// Any init failure is a fault
		try {
			mySystemInterface = si;
			localProperties = si.getLocalProperties();
			localSystemLogger = si.getSystemLogger();
			
		} catch (ThingsException te) {
			throw new UserException("Failure to initialize caused a FAULT.", ThingsCodes.SYSTEM_FAULT_FAILED_INIT, te, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
		} catch (Throwable t) {
			throw new UserException("Spurious exception to initialize caused a FAULT.", ThingsCodes.SYSTEM_FAULT_FAILED_INIT, t, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
			
		}
	}
	
	/**
	 * Get a good property toolkit.
	 * @throws UserException
	 */
	public ThingsPropertyReaderToolkit PROPERTY_READER_TOOLKIT() throws UserException {
		if (mySystemInterface==null) UserException.softwareProblem("PROPERTY_READER_TOOLKIT() called before systemInit.");

		ThingsPropertyReaderToolkit toolkit = null;
		try {
			toolkit = new ThingsPropertyReaderToolkit(localProperties);
		} catch (Throwable t) {
			 throw new UserException("Could not get a toolkit for the local properties.", UserException.SYSTEM_FAULT_TOOLKIT_FAILED, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
		}
		return toolkit;
	}
	
	/**
	 * Get the global configuration property toolkit for all users.
	 * @throws UserException
	 */
	public ThingsPropertyReaderToolkit CONFIG_PROPERTY_TOOLKIT() throws UserException {
		if (mySystemInterface==null) UserException.softwareProblem("PROPERTY_READER_TOOLKIT() called before systemInit.");
		
		ThingsPropertyReaderToolkit toolkit = null;
		try {
			toolkit = new ThingsPropertyReaderToolkit(mySystemInterface.getConfigProperties());
		} catch (Throwable t) {
			 throw new UserException("Could not get a toolkit for the local properties.", UserException.SYSTEM_FAULT_TOOLKIT_FAILED, ThingsNamespace.ATTR_THING_NAME, GET_NAME());
		}
		return toolkit;
	}
	
	/**
	 * Get a universe by the local name.
	 * @param name the local name (as registered).
	 * @return the universe
	 * @throws UserException
	 * @see things.universe.Universe
	 */
	public Universe GET_UNIVERSE(String name) throws UserException {
		Universe result = null;
		try {
			result = mySystemInterface.getUniverse(name);
		} catch (Throwable t) {
			throw new UserException("Could not get universe", UserException.THING_FAULT_SERVICE_COULD_NOT_GET_UNIVERSE, t);
		}
		return result;
	}
	
	/**
	 * Get a universe object accessor.
	 * @param universeName the local name (as registered) of the universe.
	 * @param name the object name.
	 * @return the accessor
	 * @throws UserException
	 * @see things.universe.Universe
	 */
	public Accessor GET_UNIVERSE_ACCESSOR(String universeName, String... name) throws UserException {
		Accessor result = null;
		try {
			result = mySystemInterface.getUniverse(universeName).getObjectAccessor(name);
		} catch (Throwable t) {
			throw new UserException("Could not get accessor", UserException.THING_FAULT_SERVICE_COULD_NOT_GET_UNIVERSE_ACCESSOR, t);
		}
		return result;
	}
	
	/**
	 * Get the THING's Logger.  Do not use this for any result data.
	 * @return a logger
	 * @throws UserException
	 * @see things.thinger.io.Logger
	 */
	public Logger GET_LOGGER() throws UserException {
		return localSystemLogger;
	}
	
}
