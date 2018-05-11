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

/**
 * An module schema entry.
 * section, line,    entry,  type,        Default text,      incidence,          validator
 * DECLARE_ENTRY("GROUP", "GROUP", "Name", TYPE_STRING, "Enter name here", INCIDENCE_UNIQUE,	"VALIDATE_GROUP_NAME");
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
public class ModuleSchemaEntry {
	
	// == FIELDS  =====
	public final static String NO_SET = null;
	public final static String SCHEMA_TOKEN = null;
	
	// AUTOMATIC
	public String 		myName;
	public String		myToken;

	// NOT AUTOMATIC
	public int			myType;
	public int			myIncidence;
	public String		myValidator = NO_SET;
	public String 		myDefaultText = NO_SET;
	
	// CONSTANTS
	public final static int TYPE_STRING = 1;
	public final static int TYPE_NUMERIC = 2;	
	public final static int TYPE_PERCENTAGE = 3;
	public final static int INCIDENCE_FREE = 1;
	public final static int INCIDENCE_UNIQUE = 2;

	public ModuleSchemaEntry() throws Throwable {
		 throw new TabularException("BUG: Do not use ModuleSchemaEntry() default constructor.");
	}	
	
	public ModuleSchemaEntry(String name, String token) {
		myName = Module.normalize(name);
		myToken = token;
	}
	
	void checkAndSetValidator(String validator) throws TabularException {
		myValidator = validator;
	}
	
	void checkAndSetDefaultText(String defaultText) throws TabularException {
		myDefaultText = defaultText;
	}
	
	void checkAndSetType(int t) throws TabularException {
		switch(t) {
		case TYPE_STRING:
		case TYPE_NUMERIC:		
		case TYPE_PERCENTAGE:		
			myType = t;
		default:
			throw new TabularException("BUG: Type value of " + t + " not allowed.  (Use the constants!)");
		}
	}
	
	void checkAndSetIncidence(int i) throws TabularException {
		switch(i) {
		case INCIDENCE_FREE:
		case INCIDENCE_UNIQUE:			
			myIncidence = i;
		default:
			throw new TabularException("BUG: Incidence value of " + i + " not allowed.  (Use the constants!)");
		}
	}
}