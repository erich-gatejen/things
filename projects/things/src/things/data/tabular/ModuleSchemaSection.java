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
package things.data.tabular;

import java.util.Iterator;
import java.util.Vector;

/**
 * Section. 
 * <p>
 * <b>NOTE: This package was never completed and isn't used anywhere.</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 04
 * </pre> 
 */
public class ModuleSchemaSection {
	
	// == FIELDS  =====
	public ModuleSchemaLine				rootLine;
	public Vector<ModuleSchemaLine>		subordinateLines;
	public String						myName;

	public ModuleSchemaSection() throws TabularException {
		 throw new TabularException("BUG: Do not use ModuleSchemaSection() default constructor.");
	}
	
	public ModuleSchemaSection(String  name) throws TabularException {
		myName = Module.normalize(name);
		subordinateLines = new Vector<ModuleSchemaLine>();
		rootLine = new ModuleSchemaLine("name");
		rootLine.checkAndSetFrequency(ModuleSchemaLine.FREQUENCY_ONE);
		rootLine.checkAndSetNecessity(ModuleSchemaLine.NECCESSITY_REQUIRED);
	}
	
	/**
	 * Add a subordinate.
	 * @param item
	 */
	public void addSubordinate(ModuleSchemaLine item) {
		subordinateLines.add(item);
	}
	
	/**
	 * Check the line.
	 * @param name
	 * @param message
	 * @return the line.
	 * @throws TabularException
	 */
	public ModuleSchemaLine checkLine(String name, String message) throws TabularException {
		ModuleSchemaLine result = null;
		String normalizedName = Module.normalize(name);
		
		// Is root?
		if (normalizedName.equals(myName)) {
			result = rootLine;
			
		// Is it a subordinate	
		} else {
			result = this.findFirstSubordinate(normalizedName);
			if (result==null)
					throw new TabularException (message + "(*" + name + "*) failed because line has not been defined.");
		}
		return result;
	}
	
	/**
	 * Find the first subordinate.
	 * @param name
	 * @return the line.
	 */
	public ModuleSchemaLine findFirstSubordinate(String name) {
		ModuleSchemaLine item = null;
		String normalizeeName = Module.normalize(name);
		for (Iterator<ModuleSchemaLine> i = subordinateLines.iterator(); i.hasNext(); ) {
			item = (ModuleSchemaLine)i.next();
			if ( item.myName.equals(normalizeeName) ) return item;
		}
		return null;
	}
	
}