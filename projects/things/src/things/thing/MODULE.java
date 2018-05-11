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
import things.thinger.SystemInterface;

/**
 * A module.  It is an implementation with access to services like a THING, but without the process workflow and it has no RESULTS.<br>  
 * The methods can be called directly after instantiating one.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public abstract class MODULE extends BASE implements MODULEInterface {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS.  To be implemented by the user.
	
	/**
	 * This will be called during initialization.  Expect it to happen at any time; if the module is already in use, you should 
	 * re-initialize it.  The System Data fields will be set when this is called.
	 */
	public abstract void INITIALIZE() throws UserException;

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == Class Methods
		
	/**
	 * Initialize this MODULE.  This will be done by the kernel.
	 * @param si A system interface.  
	 * @throws UserException which will always be a FAULT.
	 */
	public void init(SystemInterface	si) throws UserException {
		
		// Any init failure is a fault
		try {
			systemInit(si);
			INITIALIZE();
			
		} catch (ThingsException te) {
			throw new UserException("Failure to initialize the MODULE caused a FAULT.", ThingsCodes.SYSTEM_FAULT_THING_FAILED_INIT, te, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
		} catch (Throwable t) {
			throw new UserException("Spurious exception to initialize the MODULE caused a FAULT.", ThingsCodes.MODULE_FAULT_FAILED_INIT, t, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getName());
			
		}
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == SERVICE AND INTERFACE METHODS.  These are exposed in MODULEInterface only.
	
}
