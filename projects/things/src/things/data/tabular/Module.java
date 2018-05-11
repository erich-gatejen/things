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

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

/**
 * An abstract module reactor.  A concrete module will implement a specific sheet.  Each of the abstract methods helps define the sheet.
 * All DECLARATIVE methods (upper case) are used in the abstract sections to define the sheet.
 * <p>
 * <pre>
MODULE_Example() {

	// A sheet defines general attributes and one-time entries.
	void sheet()	{
		HEADER_TEMPLATE("/tmp/template_file");
		PARAMETER("TOKEN_XXXX", "Database", "Validator_X");
	}

	// A section is a repeatable unit.
	void section() {
		DECLARE_SECTION("SECTION_1");
	//		         section line       mod1	  mod2
		DECLARE_LINE("GROUP","ITEM1", 	REQUIRED, MANY);
		DECLARE_LINE("GROUP","ITEM2",   OPTIONAL, MANY);
		DECLARE_LINE("GROUP","ITEM3",	REQUIRED, MANY);
		DECLARE_LINE("GROUP","ITEM4",	OPTIONAL, MANY);
	}

	// Lines are repeatable units within each section.
	void lines() {
		//            section  line     entry,  default,	     , modifyer	 validator		
		DECLARE_ENTRY("GROUP", "GROUP", "Name", "Enter name here", UNIQUE,	"VALIDATE_SECTION_NAME");

		DECLARE_LINE("GROUP","VALIDATE_GROUP")
		DECLARE_ENTRY("GROUP","NAME", "VALIDATE_GROUP_NAME");
		DECLARE_ENTRY("GROUP","SELECTION_TYPE", "VALIDATE_SELECTION_TYPE");
	}

	// Validations specify validation steps for each line.
	void validations() {	
		VALIDATE_HELP("VALIDATE_GROUP_HEADER", "Title", "Text");
		VALIDATE_TEXT("VALIDATE_GROUP_NAME", "Title", "Text", 4, 39, "Error title", "Error text");
		VALIDATE_LIST("VALIDATE_SELECTION_TYPE", "Title", "Text", "ITEM1, ITEM2, ITEM3", false, "Error title", "Error text");		
	}
}
 * </pre>
 * <p>
 * <b>NOTE: This package was never completed and isn't used anywhere.</b>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 NOV 04
 * </pre> 
 */
public abstract class Module {
	
	// ==================================================================================	
	// == ABSTRACT METHODS  =============================================================
	public abstract String sheet() throws TabularException;
	public abstract String section() throws TabularException;	
	public abstract String lines() throws TabularException;	
	public abstract String validations() throws TabularException;	
		
	// ==================================================================================	
	// == DECLARATIVE METHODS ===========================================================
	
	/*
	 * HEADER_TEMPLATE directive.  It defines a template for a static header.
	 * @param templateFile The path to the file.
	 * @throws things.data.tabular.TabularException
	 */
	protected void HEADER_TEMPLATE(String  templateFile) throws TabularException {
		checkRegistrationGate();
		
		// validate
		if (templateFile == null) throw new TabularException("HEADER_TEMPLATE(null) is not allowed.");
		File 	theActualFile = new File(templateFile);
		if (theActualFile.isFile()) {
			headerTemplateFile = theActualFile;
		} else {
			throw new TabularException("HEADER_TEMPLATE(" + theActualFile.getAbsolutePath() + "): Header template file specified does not exist."); 
		}
	}

	/*
	 * PARAMETER directive.
	 * @param name Name of the parameter.
	 * @param templateToken Set value.
	 * @param validator Name of a validator.
	 * @throws things.data.tabular.TabularException
	 */
	protected void PARAMETER(String name, String templateToken, String validator) throws TabularException {
		if (name == null) throw new TabularException("PARAMETER(null,*,*) is not allowed.");
		if (templateToken == null) throw new TabularException("PARAMETER(*,null,*) is not allowed.");
		
		// Already registered?  If not, get it.
		String registeredName = checkFreeAndReserve(name,"PARAMETER");
		
		// build and save
		ModuleSchemaEntry newParameter = new ModuleSchemaEntry(registeredName,templateToken);
		newParameter.checkAndSetValidator(validator);
		parameters_schema.put(registeredName,newParameter);
	}
	
	/*
	 * DECLARE_SECTION directive.
	 * @param name Name of the section.
	 * @throws things.data.tabular.TabularException
	 */
	protected void DECLARE_SECTION(String name) throws TabularException {
		disallowNull(name,"DECLARE_LINE(name)");
		
		// Already registered?  If not, get it.
		String registeredName = checkFreeAndReserve(name,"DECLARE_SECTION");
		
		// build and save
		ModuleSchemaSection newSection = new ModuleSchemaSection(registeredName);
		sections_schema.put(registeredName,newSection);
	}
	
	/*
	 * DECLARE_LINE directive.
	 * @param section Name of the owning section.
	 * @param name Name of the line.
	 * @
	 * @throws things.data.tabular.TabularException
	 */
	protected void DECLARE_LINE(String section, String name, int necessity, int frequency) throws TabularException {
		disallowNull(section,"DECLARE_LINE(section,*,*,*)");
		disallowNull(name,"DECLARE_LINE(*,name,*,*)");
	
		// Checks
		ModuleSchemaSection sectionObject = checkSection(section,"DECLARE_LINE");
		String registeredName = checkFreeAndReserve(name,"DECLARE_LINE");
		
		// Create a new one
		ModuleSchemaLine newLine = new ModuleSchemaLine(registeredName);
		newLine.checkAndSetFrequency(frequency);
		newLine.checkAndSetNecessity(necessity);

		// Add subordinate
		sectionObject.addSubordinate(newLine);
	}

	/*
	 * DECLARE_ENTRY directive.
	 *                    section, line,    entry,  type,        Default text,      incidence,          validator
	 *      DECLARE_ENTRY("GROUP", "GROUP", "Name", TYPE_STRING, "Enter name here", INCIDENCE_UNIQUE,	"VALIDATE_GROUP_NAME");
	 * @throws things.data.tabular.TabularException
	 */
	protected void DECLARE_ENTRY(String section, String line, String entry, int type,
			String defaultEntry, int incidence,  String validator) throws TabularException {
		disallowNull(section,"DECLARE_ENTRY(section,*,*,*)");		
		disallowNull(line,"DECLARE_ENTRY(*,line,*,*)");			
		disallowNull(entry,"DECLARE_ENTRY(*,*,entry,*)");		
		
		// Find it's home
		ModuleSchemaSection sectionObject = checkSection(section,"DECLARE_ENTRY");
		ModuleSchemaLine lineObject = sectionObject.checkLine("line","DECLARE_ENTRY");
		
		// Entries must be unique per line
		if (lineObject.hasEntry(entry)) throw new TabularException("DECLARE_ENTRY(*,*," + entry + "*) failed because the ENTRY NAME has already been used for this line."); 
		
		// Make a new one and set it
		ModuleSchemaEntry entryObject = new ModuleSchemaEntry(entry,ModuleSchemaEntry.SCHEMA_TOKEN);
		entryObject.checkAndSetIncidence(incidence);
		entryObject.checkAndSetIncidence(type);	
		entryObject.checkAndSetDefaultText(defaultEntry);
		entryObject.checkAndSetValidator(validator);
		
		// Save it
		lineObject.addEntry(entryObject);
	}
	
	/*
	 * DECLARE_VALIDATION directive.
	 *             
	 *            		  name, type, title, text, value, min, max, necessity, errorTitle, errorText
	 * DECLARE_VALIDATION("V_GROUP", TYPE_NUMERIC, "Field A", "Field A Help", "10", "1", "100", NECCESSITY_REQUIRED, "Bad value", "BAD help")
	 * 
	 * 	public ModuleSchemaValidation(String name, int  type, String title, String text, 
								  String value, String min, String max, int	necessity,
								  String errorTitle, String errorText) throws Throwable {
TabularException
	 */
	protected void DECLARE_VALIDATION(String name, int  type, String title, String text, 
			  String value, String min, String max, int	necessity,
			  String errorTitle, String errorText) throws Throwable {
		
		//String registeredName = checkFreeAndReserve(name,"DECLARE_VALIDATION");
		
		
		
		
	}
	
	


	// ==================================================================================	
	// == OWNER METHODS =================================================================
	
	/*
	 * Prepare the definition.  An owner of this definition should call this first.
	 * TabularException
	 */
	public synchronized void prepare() throws TabularException  {
		
		// Prepare data
		headerTemplateFile = null;
		reserved_names = new HashSet<String>();
		parameters_schema = new Hashtable<String,ModuleSchemaEntry>();	// Key is the name
		sections_schema = new Hashtable<String,ModuleSchemaSection>();
		
		// Let the implementaion module define the schema.
		gateOpenForRegistration = true;
		this.sheet();
		this.section();
		this.lines();
		this.validations();
		gateOpenForRegistration = false;
	}
	
	// ==================================================================================	
	// == INTERNAL ======================================================================
	
	// == FIELDS  =====
	@SuppressWarnings("unused")
	private int entryIndex;
	
	// == SCHEMA FIELDS  =====
	// Global sheet parameters
	private Hashtable<String,ModuleSchemaEntry> parameters_schema;
	private Hashtable<String,ModuleSchemaSection> sections_schema;
	private HashSet<String> reserved_names;
	
	@SuppressWarnings("unused")
	private File headerTemplateFile;
	@SuppressWarnings("unused")
	private Vector<?> entryList;
	
	// This field is to prevent coding bufoonery down the line.  Declarative methods should only
	// be called through instance()->register()->METHOD();
	private boolean gateOpenForRegistration = false;
	private void checkRegistrationGate() throws TabularException {
		if (gateOpenForRegistration==false) throw new TabularException("BUG: Something called a DECLARATIVE METHOD rather than letting initialize()->register() do it.");
	}
	
	// == Public Helpers ================================================================
	
	/*
	 * Normalize any string.
	 * @param in The input.
	 * @returns the output.
	 */
	public static String normalize(String in) {
		if (in != null) return in.toUpperCase();
		return in;
	}
	
	// == Private Helpers ================================================================	
	private void disallowNull(Object o, String function) throws TabularException {
		if (o == null) throw new TabularException(function + " does not allow a null parameter.");
	}
		
	// Reserved names
	private String checkFreeAndReserve(String name, String message) throws TabularException {
		String normalizedName = normalize(name);
		if (reserved_names.contains(normalizedName)) throw new TabularException(message + "(*" + name + "*) failed because the name has already been declared.");
		reserved_names.add(normalizedName);		
		return normalizedName;
	}
	
	// Check members
	private ModuleSchemaSection checkSection(String name, String message) throws TabularException {
		String normalizedName = normalize(name);
		if (!sections_schema.containsKey(normalizedName)) 
			throw new TabularException (message + "(*" + name + "*) failed because section has not been defined.");
		return (ModuleSchemaSection)sections_schema.get(normalizedName);
	}
	
	
}
	
