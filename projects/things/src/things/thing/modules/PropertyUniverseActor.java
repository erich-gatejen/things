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
package things.thing.modules;

import things.common.ThingsException;
import things.data.Accessor;
import things.data.NV;
import things.data.NVImmutable;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTrunkIO;
import things.thing.MODULE;
import things.thing.UserException;

/**
 * Give us the ability to load and save local properties to a universe object.
 *<p>
 * NOTES:<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public class PropertyUniverseActor extends MODULE {

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
	public void INITIALIZE() throws UserException {
		// Nothing.
	}
	
	/**
	 * Get the simple name.  The implementing class can choose it.  It need not be unique.  It is not a system ID!  So don't use it as one.
	 * This can be called at any point after object construction (and before DEFINITION), so don't rely on additional setup.
	 * @return the name.  
	 */
	public String GET_NAME() {
		return "things.PropertyUniverseActor";
	}

	// ====================================================================================================================================
	// ====================================================================================================================================
	// ==  Methods
		
	/**
	 * Save local properties starting with the given path to the named universe in the named universe object.
	 * @param universeName the local name (as registered).
	 * @param path the path for the properties to save.  If left blank or null, it'll do all visible.
	 * @param universeObject the universe object.
	 * @throws ThingsException if anything goes wrong.
	 */
	public void save(String universeName, String path, String... universeObject) throws ThingsException {
		
		try {
		
			// Get interface to universe
			Accessor objectAccessor = GET_UNIVERSE_ACCESSOR(universeName, universeObject);
			ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
			trunk.init(GET_NAME(), objectAccessor);
			trunk.startWrite();
	
			// Get our working space
			NVImmutable current;
			for (String item : this.localProperties.sub(path) ) {
				current = localProperties.getPropertyNV(ThingsPropertyReaderToolkit.path(path,item));
				trunk.writeNext(current);
			}
			
			// Done.
			trunk.endWrite();
		
		} catch (Throwable t) {
			throw new ThingsException("Failed to save local properties.", ThingsException.MODULE_BUILTIN_UPA_SAVE_FAILED, t);
		}
	}
	
	/**
	 * Load local properties from the named universe in the named universe object.
	 * @param universeName the local name (as registered).
	 * @param universeObject the universe object.
	 * @throws ThingsException if anything goes wrong.
	 */
	public void load(String universeName, String... universeObject) throws ThingsException {
		
		try {
		
			// Get interface to universe
			Accessor objectAccessor = GET_UNIVERSE_ACCESSOR(universeName, universeObject);
			ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
			trunk.init(GET_NAME(), objectAccessor);
			trunk.startRead();
	
			// Get our working space
			NV current =  trunk.readNext();
			while(current != null) {
				
				localProperties.setProperty(current);
				
				// Iterate
				current =  trunk.readNext();
			}

			// Done.
			trunk.endRead();
		
		} catch (Throwable t) {
			throw new ThingsException("Failed to load local properties.", ThingsException.MODULE_BUILTIN_UPA_SAVE_FAILED, t);
		}
	}
	
	/**
	 * Save properties starting with the given path to the named universe in the named universe object.
	 * @param properties properties view from where to save.
	 * @param universeName the local name (as registered).
	 * @param path the path for the properties to save.  If left blank or null, it'll do all visible.
	 * @param universeObject the universe object.
	 * @throws ThingsException if anything goes wrong.
	 */
	public void save(ThingsPropertyView properties, String universeName, String path, String... universeObject) throws ThingsException {
		
		try {
		
			// Get interface to universe
			Accessor objectAccessor = GET_UNIVERSE_ACCESSOR(universeName, universeObject);
			ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
			trunk.init(GET_NAME(), objectAccessor);
			trunk.startWrite();
	
			// Get our working space
			NVImmutable current;
			for (String item : properties.sub(path) ) {
				current = properties.getPropertyNV(ThingsPropertyReaderToolkit.path(path,item));
				trunk.writeNext(current);
			}
			
			// Done.
			trunk.endWrite();
		
		} catch (Throwable t) {
			throw new ThingsException("Failed to save properties.", ThingsException.MODULE_BUILTIN_UPA_SAVE_FAILED, t);
		}
	}
	
	/**
	 * Load properties from the named universe in the named universe object.
	 * @param properties properties view where to load.
	 * @param universeName the local name (as registered).
	 * @param universeObject the universe object.
	 * @throws ThingsException if anything goes wrong.
	 */
	public void load(ThingsPropertyView properties, String universeName, String... universeObject) throws ThingsException {
		
		try {
		
			// Get interface to universe
			Accessor objectAccessor = GET_UNIVERSE_ACCESSOR(universeName, universeObject);
			ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
			trunk.init(GET_NAME(), objectAccessor);
			trunk.startRead();
	
			// Get our working space
			NV current =  trunk.readNext();
			while(current != null) {
				
				properties.setProperty(current);
				
				// Iterate
				current =  trunk.readNext();
			}

			// Done.
			trunk.endRead();
		
		} catch (Throwable t) {
			throw new ThingsException("Failed to load properties.", ThingsException.MODULE_BUILTIN_UPA_SAVE_FAILED, t);
		}
	}
	
}
