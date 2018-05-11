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
 * An abstract MODULE reactor.
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
public class ModuleSchemaValidation {

	// == FIELDS  =====
	public final static String NO_SET = null;

	// AUTOMATIC
	public String 		myName;
	public int			myType;
	public String		myTitle;
	public String		myText;
	public String		myErrorTitle;
	public String		myErrorText;
	public String		myValue;	// Default value
	public int			myNeccessity;
	public String		myMinimum;
	public String		myMaximum;

	// NOT AUTOMATIC
	public int			myIncidence;
	public String		myValidator = NO_SET;
	public String 		myDefaultText = NO_SET;
	
	// CONSTANTS
	public final static int TYPE_HELP_ONLY = 1;
	public final static int TYPE_LIST = 2;
	public final static int TYPE_TEXT = 3;
	public final static int TYPE_NUMERIC = 4;
	public final static int NECCESSITY_REQUIRED = 1;
	public final static int NECCESSITY_OPTIONAL = 2;	
	public final static String NO_MINIMUM = null;	
	public final static String NO_MAXIMUM = null;	

	public ModuleSchemaValidation() throws Throwable {
		 throw new TabularException("BUG: Do not use ModuleSchemaValidation() default constructor.");
	}	
	
	/*
	 * ModuleSchemaValidation constructor.
	 */
	public ModuleSchemaValidation(String name, int  type, String title, String text, 
								  String value, String min, String max, int	necessity,
								  String errorTitle, String errorText) throws Throwable {
		myName = Module.normalize(name);
		myType = type;
		myTitle = title;
		myText = text;
		myErrorTitle = errorTitle;
		myErrorText = errorText;
		myMinimum = min;
		myMaximum = max;
		this.checkAndSetType(type);
		this.checkAndSetNecessity(necessity);	
	}

	void checkAndSetNecessity(int n) throws TabularException {
		switch(n) {
		case NECCESSITY_OPTIONAL:
		case NECCESSITY_REQUIRED:		
			myNeccessity = n;
		default:
			throw new TabularException("BUG ModuleSchemaValidation.checkAndSetNecessity: Neccessity value of " + n + " not allowed. (Use the constants!)");
		}
	}
	void checkAndSetType(int t) throws TabularException {
		switch(t) {
		case TYPE_HELP_ONLY:
		case TYPE_LIST:
		case TYPE_TEXT:
		case TYPE_NUMERIC:
			myType = t;
		default:
			throw new TabularException("BUG ModuleSchemaValidation.checkAndSetType: Type value of " + t + " not allowed. (Use the constants!)");
		}
	}
	
}