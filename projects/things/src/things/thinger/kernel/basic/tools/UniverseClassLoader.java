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
package things.thinger.kernel.basic.tools;

import java.io.File;

import things.common.ThingsUtilityBelt;
import things.universe.UniverseAnchor;

/**
 * A goofy class loader for compile time.
 * <p>
 * NOTE: the universeOnly boolean is VERY IMPORTANT for security.  It should be true for execution of classes and false for 
 * compile time.  This will keep users from eclipsing core classes with THINGS.

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 8 JUL 07
 * </pre> 
 */
public class UniverseClassLoader extends ClassLoader {
		    
	UniverseAnchor myAnchor;
	boolean universeOnlyFlag = true;
	
	// No constructor, but must be initialized.
	
	/**
	 * Init it.  Without doing this, it will not actually see a universe.
	 * @param ua where in the universe to look.
	 * @param universeOnly if true, it will look at the universe only!
	 * @see things.universe.UniverseAnchor
	 */
	public void init(UniverseAnchor ua, boolean universeOnly) {
		myAnchor = ua;
		universeOnlyFlag = universeOnly;
	}
	
	/** 
	 * Find the class by name.
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		
		File actualFile;
		
		// See if we have this class already.  Though, this should have already been cached.
		Class<?> theClass = findLoadedClass(name);
		if (theClass != null) return theClass;
		

		try {
			String fileName  = ThingsUtilityBelt.binaryNameToFileName(name);
			
			if (myAnchor.hasObject(fileName)) {
				actualFile = myAnchor.getLocal(fileName);
					
			} else if (myAnchor.hasObject(fileName + ".class")) {
				actualFile = myAnchor.getLocal(fileName + ".class");
				
			} else {
				
				// CONFIG!
				if (universeOnlyFlag) {
					// Do not look beyond.
					throw new ClassNotFoundException(name);
					
				} else {		
					// Let the main system handle it.
					return super.findClass(name);
				}
			}
		
		} catch (ClassNotFoundException cnfe) {
			throw cnfe;
		} catch (Throwable t) {
			throw new ClassNotFoundException(t.getMessage(), t);
		}
		
		// Ok, were going to load it ourselves.
	    byte  classData[];
	    try {
	    	classData = ThingsUtilityBelt.loadFileToArray(actualFile);
	    } catch (Throwable t) {
	    	throw new ClassNotFoundException(t.getMessage(),t);
	    }
	
	    // Define the class
		theClass = defineClass(name, classData, 0, classData.length);
		
		// Done!
		return theClass;
	 
	}
}
