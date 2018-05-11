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
package things.thinger.kernel.basic;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.impl.StringPosterConsole;
import things.data.ThingsPropertyTree;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.thinger.io.Logger.LEVEL;

/**
 * A local bootstrap for starting the system.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 07
 * </pre> 
 */
public class KernelBasic_System_Bootstrap {

	// =====================================================================================================================
	// =====================================================================================================================
	// PUBLIC DATA
	
	/**
	 * The kernel itself.
	 */
	public KernelBasic kernel;
	
	/**
	 * Any output exception.
	 */
	public ThingsException outputException;
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PRIVATE DATA
	private StringPoster bootstrapLogger;
	private ThingsPropertyTree properties;
	
    // ==================================================================================================================
    // == MAIN entry.
    
	/**
	 * Main interface.
	 */
	public static void main(String[] args) {

		// handle arguments
		if (args.length < 1) {
			System.out.println("ERROR: Giving up.");
			System.out.println("Required parameter not specified.  Need path to startup properties.");
			System.out.println("(Typically basic_config.prop)");
			return;
		}

		// Do it.
		KernelBasic_System_Bootstrap me = new KernelBasic_System_Bootstrap();
		me.runIt(args[0]);
	}
    
	/**
	 * Run it.
	 * @param propertiesPath the path to the configuration properties.
	 */
	public void runIt(String propertiesPath) {
		
		// Logger
		bootstrapLogger = new StringPosterConsole();
		
		// Setup	
		try {
			properties = ThingsPropertyTreeBASIC.getExpedientFromFile(propertiesPath);
			kernel = new KernelBasic();
		
		} catch (ThingsException te) {
			bootstrapLogger.postit("FAULT: Giving up while setting up system.");
			bootstrapLogger.postit(te.toStringComplex());
			System.exit(1);
			
		} catch (Throwable t) {
			bootstrapLogger.postit("FAULT: Giving up while setting up system.");
			bootstrapLogger.postit(t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
		
		// Run the bootstrap
		try {
			LEVEL candidateLevel = LEVEL.getLevelByName(properties.getRoot().getProperty(KernelBasic_Constants.LOGGING_LEVEL));
			boolean verbose = false;
			if (candidateLevel == LEVEL.DEBUG) verbose = true;
			kernel.bootstrap(verbose, bootstrapLogger, properties);
		    
		} catch (ThingsException te) {
			bootstrapLogger.postit("FAULT: Giving up while starting system.");
			bootstrapLogger.postit(te.toStringComplex());
			System.exit(1);
			
		} catch (Throwable t) {
			bootstrapLogger.postit("FAULT: Giving up while starting system.");
			bootstrapLogger.postit(t.getMessage());
			t.printStackTrace();
			System.exit(1);
		}
		
	}
	
}