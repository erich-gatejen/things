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

import java.util.Hashtable;

/**
 * An module schema line.
 * <p>
 * <b>NOTE: This package was never completed and isn't used anywhere.</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Refactor completely. - 10 NOV 04
 * </pre> 
 */
public class ModuleSchemaLine {
	
	// == FIELDS  =====
	public String 		myName;
	public Hashtable<String,ModuleSchemaEntry>	entries;
	public int			neccessity;
	public int			frequency;

	public final static int NECCESSITY_REQUIRED = 1;
	public final static int NECCESSITY_OPTIONAL = 2;	
	public final static int FREQUENCY_ONE = 1;
	public final static int FREQUENCY_MANY = 2;
	
	public ModuleSchemaLine() throws Throwable {
		 throw new TabularException("BUG: Do not use ModuleSchemaLine() default constructor.");
	}
	
	public ModuleSchemaLine(String name) {
		myName = Module.normalize(name);
		entries = new Hashtable<String,ModuleSchemaEntry>();
	}
	
	void checkAndSetNecessity(int n) throws TabularException {
		switch(n) {
		case NECCESSITY_OPTIONAL:
		case NECCESSITY_REQUIRED:		
			neccessity = n;
		default:
			throw new TabularException("BUG: Neccessity value of " + n + " not allowed. (Use the constants!)");
		}
	}
	void checkAndSetFrequency(int n) throws TabularException {
		switch(n) {
		case FREQUENCY_MANY:
		case FREQUENCY_ONE:		
			frequency = n;
		default:
			throw new TabularException("BUG: Frequency value of " + n + " not allowed. (Use the constants!)");
		}
	}
	
	// Check members
	public boolean hasEntry(String name) throws TabularException {
		if (name==null) throw new TabularException("BUG: ModuleSchemaLine.hasEntry(null) called with null parameter.");
		return entries.containsKey(Module.normalize(name));
	}
	
	
	public void addEntry(ModuleSchemaEntry entry) throws TabularException {
		if (entry==null) throw new TabularException("BUG: ModuleSchemaLine.addEntry(null) called with null parameter.");
		if (entries.containsKey(entry.myName)) throw new TabularException("ADD_ENTRY(" + myName + "," + entry.myName + " failed because the line already has the entry defined.");
		entries.put(entry.myName,entry);
	}
	
}