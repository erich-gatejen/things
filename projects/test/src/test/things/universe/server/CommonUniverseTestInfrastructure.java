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
package test.things.universe.server;

import test.things.ThingsTestSuite;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.universe.Universe;
import things.universe.UniverseID;
import things.universe.UniverseRegistry;
import things.universe.server.UniverseRegistry_Simple;

/**
 * This is synced up with the registry_simple.prop test setup. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 DEC 04
 * </pre>
 */
public class CommonUniverseTestInfrastructure {
	
	// DATA
    public UniverseRegistry registry = new UniverseRegistry_Simple();
    
    public final static String UNIVERSE_A = "aaaaPRIMARY1111";
    public final static String UNIVERSE_B = "bbbbSECONDARY2222";
    
	
	/**
	 * Init the infrastructure and load the universe registry.
	 * @param properties the overall properties (provided by Test).
	 * @throws Throwable
	 */
	public void init(ThingsPropertyView  properties) throws Throwable {
		String my_prop = new String(properties.getProperty(ThingsTestSuite.REQUIRED_PROP_ROOT_DIR) + "/universe/server/registry_simple.prop"); 
		ThingsPropertyView regView = ThingsPropertyTreeBASIC.getExpedientFromFile(my_prop);	
		registry.loadRegistry(regView);
	}
	

	/**
	 * Get the A universe.
	 * @return
	 * @throws Throwable
	 */
	public Universe getA() throws Throwable {
		return registry.getAccessor(new UniverseID(UNIVERSE_A));
	}
	
	/**
	 * Get the B universe.
	 * @return
	 * @throws Throwable
	 */
	public Universe getB() throws Throwable {
		return registry.getAccessor(new UniverseID(UNIVERSE_B));
	}
	
} // end class
