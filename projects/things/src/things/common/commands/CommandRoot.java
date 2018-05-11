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
package things.common.commands;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.data.NVImmutable;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTreeRAM;
import things.thinger.io.Logger;

/**
 * Command root for all the commands.
 * <p>
 * It will automatically expect a root directory as the first entity (#0).  This one is reserved.  You can have 
 * scripts or wrappers obfuscate it if none of your commands care about it.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 1 NOV 01
 * EPG - Welded more tightly into the system, inclufing logging. - 10 AUG 06
 * </pre> 
 */
public abstract class CommandRoot {

	/**
	 * Built-in options.
	 */
	public final static int	OPTION_VERBOSE = 'V';
	public final static int OPTION_HELP1 = 'h';
	public final static int OPTION_HELP2 = '?';
	
	/**
	 * Build-in values
	 */
	public final static String VALUES_CONFIGURATION = "config";
	public final static String VALUES_VALUES = "values";
	
	/**
	 * Built-in entities.
	 */
	public final static int ENTITY_ROOT_DIRECTORY = 0;
	
	// =============================================================================================================\
	// == EXPOSED DATA
	
	/**
	 * Configuration properties, if provided. Not all commands need them. They
	 * can be load from a file with the -configs option.
	 */
	public ThingsPropertyReaderToolkit configProps = null;

	/**
	 * Value properties, if provided. Not all commands need them. They can be
	 * load from a file with the -values option.
	 */
	public ThingsPropertyReaderToolkit valueProps = null;
	
	/**
	 * Value properties from a writable view, if provided. Not all commands need them. They can be
	 * load from a file with the -values option.
	 */
	public ThingsPropertyView valuePropsView = null;
	
	/**
	 * Configuration properties from a writable view, if provided. Not all commands need them. They can be
	 * load from a file with the -values option.
	 */
	public ThingsPropertyView configPropsView = null;
		
	/**
	 * A default logger.  It'll be set during startup, but if you are brave, overwrite it.
	 */
	public Logger defaultLogger;
	
	// =============================================================================================================\
	// == ABSTRACT INTERFACE

	/**
	 * Declare usage.  Gives the subclass a chance to declare values and options.
	 */
	abstract protected void declare() throws Throwable;
	
	/**
	 * Run the command.  The implementation should NOT persist any state!  It's up the the creator to decide if
	 * to keep the command object around.  Any exception that finds its way out will be stopped and logged.  Obviously, ThingsExceptions will
	 * provide richer detail than plain Exceptions.
	 * @throws Throwable
	 */
	abstract public void run() throws Throwable;

	/**
	 * Get the command name.  This is how the subclass defines the name (which is mostly used in the help).
	 * @return the name.
	 */
	abstract protected String getName();
	
	/**
	 * Get the command token.  This is a short token that will head each log line.
	 * @return the token.
	 */
	abstract protected String getToken();
	
	/**
	 * OVERRIDE IF YOU WANT TO PROVIDE MORE INFO in the help about what the
	 * command does.  Return null is you don't want to print a header.
	 * @return The header or null.
	 */
	protected String getHeader() {
		return null;
	}

	/**
	 * OVERRIDE IF YOU WANT TO PROVIDE PARAMETER INFORMATION to the command help.   Return null is you don't want to print a footer.
	 * @return a string that is the footer.
	 */
 	protected String getFooter() {
		return " ";
	}

	// =============================================================================================================\
	// == PROTECTED SERVICES FOR SUBCLASS
 	
	/**
	 * Declare an option.  (they are always optional).
	 * @param option The character for the option.
	 * @param helpText The help text.
	 * @throws Throwable Most likely a ThingsException if parameters are bad or if the option was already declared.
	 */
	protected void declareOption(int	option, String  helpText ) throws Throwable {
		if (helpText==null) throw new ThingsException("helpText cannot be null.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION, ThingsNamespace.ATTR_PARAMETER_META, Integer.toString(option));
		if (option > CommandLineProcessor.MAX_CHARACTER) throw new ThingsException("Option character out of range.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION, ThingsNamespace.ATTR_PARAMETER_META, Integer.toString(option));
		if (options[option] != null) throw new ThingsException("Option already defined.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION, ThingsNamespace.ATTR_PARAMETER_VALUE, options[option]);
		options[option] = helpText;
	}
	
	/**
	 * Declare a value.
	 * @param name The option name.   Not case sensitive.  Internally, they are normalized to lower case.
	 * @param helpText The help text.
	 * @param isRequired is it required?  false means it is optional.
	 * @throws Throwable Most likely a ThingsException if parameters are bad or if the option was already declared.
	 * @see ThingsException
	 */
	protected void declareValues(String name, String helpText, boolean isRequired) throws Throwable {
		if (helpText==null) throw new ThingsException("helpText cannot be null.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION);
		if (name==null) throw new ThingsException("name cannot be null.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION);
		String normalized = name.toLowerCase();
		if (values.containsKey(normalized)) throw new ThingsException("Value already declared.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION, ThingsNamespace.ATTR_PARAMETER_NAME, name);
		values.put(name, new CommandParameter(CommandLine.PARAMETER_TYPES.VALUE, name, helpText, isRequired));
	}
	
	/**
	 * Declare an entity.  These MUST be done sequentially by item number, starting with 1 (0 is reserved by the system for the install root).  If you declare them out
	 * of numeric order, like 1-2-4-3, or have gaps, like 1-2-3-5, you will get an exception.  I'm just being a pain here.  :6)
	 * @param name The option name.   Not case sensitive.  Internally, they are normalized to lower case.
	 * @param helpText The help text.
	 * @param isRequired is it required?  false means it is optional.
	 * @throws Throwable Most likely a ThingsException if parameters are bad or if the option was already declared.
	 * @see ThingsException
	 */
	protected void declareEntities(int name, String helpText, boolean isRequired) throws Throwable {
		if (helpText==null) throw new ThingsException("helpText cannot be null.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION);
		if (name == 0) throw new ThingsException("Entity 0 is reserved for the root directory.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION);
		if (name < 1) throw new ThingsException("item cannot be less than 1.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION);
		if (entities.size() != name) throw new ThingsException("item not sequential.", ThingsException.THING_FAULT_COMMANDLINE_BAD_DECLARATION,
				ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER,  Integer.toString(name), 
				ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER_EXPECTED, Integer.toString(name));
		entities.add(new CommandParameter(CommandLine.PARAMETER_TYPES.ENTITY, Integer.toString(name), helpText, isRequired));
	}
	
	/**
	 * When called during declaration, any subsequent list or help will not show the install root entity.
	 */
	protected void suppressListingRootInstallEntity() {
		suppressRootInstallList = true;
	}
	
	/**
	 * Raise an error and let the command root render it to the user.
	 * @param message the error
	 * @throws Throwable 
	 */
	protected void ERROR(String message) throws Throwable {
		throw new ThingsException(message, ThingsException.USER_COMMAND_ERROR);
	}
	
	/**
	 * Raise an error and let the command root render it to the user.
	 * @param message the error 
	 * @param t the causing exception.
	 * @throws Throwable
	 */
	protected void ERROR(String message, Throwable t) throws Throwable {
		throw new ThingsException(message, ThingsException.USER_COMMAND_ERROR, t);
	}
	
 	/**
 	 * Get the number of entities.  Besides the number you declare, there is always the root (0) entity.
 	 * @return the total number of entities (including the root).
 	 */
 	protected int numberOfEntites() {
 		return entities.size();
 	}
 	
 	/**
 	 * Get the number of values.
 	 * @return the total number of values.
 	 */
 	protected int numberOfValues() {
 		return values.size();
 	}
	
	// =============================================================================================================\
	// == CONTROL DATA

	// Declaration data.
	private String[] options;
 	private HashMap<String,CommandParameter> values;
 	private LinkedList<CommandParameter> entities;		// Cheesey map of a String integer.
 	//private int numberEntities;

 	private CommandLineProcessor 	clp;
 	private ThingsPropertyView emptyView = new ThingsPropertyTreeRAM();
 	
 	private PrintWriter consolePw;
 	
 	// Run data
 	private CommandLine currentLine;
 	private boolean suppressRootInstallList = false;
 	
	// =============================================================================================================\
	// == MAIN PROCESSING
	
	/**
	 * The constructor.  It will declare the command, so be ready for exceptions if done wrong.
	 * @throws Throwable
	 */
	public CommandRoot() throws Throwable {
	
		// Set up the options and values maps
		options = new String[CommandLineProcessor.MAX_CHARACTER+1];
		declareOption(OPTION_VERBOSE, "Verbose output.");
		declareOption(OPTION_HELP1, "Get help.");
		declareOption(OPTION_HELP2, "Get help.");;
		 
		values = new HashMap<String,CommandParameter>();
		declareValues(VALUES_CONFIGURATION, "Configuration properties file.", false);
		declareValues(VALUES_VALUES, "Value properties file.", false);		
		
		entities = new LinkedList<CommandParameter>();
		entities.add(new CommandParameter(CommandLine.PARAMETER_TYPES.ENTITY, "Root directory", "Root directory", true));  // Add root.
		
		// Chain the declaration
		declare();
		
		// Tools and good stuff
		clp = new CommandLineProcessor();
	}
	
	/**
	 * A main entry for the command.  The subclass should call this right from static main.
	 * <p>
	 * This will not throw any exceptions.
	 * @param args The command line arguments.  What's passed from main() works dandy.
	 */
	public void mainEntry(String[] args) {

		// Try to build our base logger.  If this fails, it's game over.
		consolePw = new PrintWriter(System.out);
		try {
			CommandLogger candidateLogger = new CommandLogger();
			candidateLogger.init(consolePw, getToken(),  Logger.LEVEL.INFO);
			defaultLogger = candidateLogger;
		} catch (Throwable t) {
			System.out.println("Could not start command root.  message=" + t.getMessage());
			t.printStackTrace();
			return;
		}
		
		try {
			
			// Parse the line.
			currentLine = clp.process(args, emptyView);
			
			// Verbose?
			if (currentLine.isOptionSet(OPTION_VERBOSE)) defaultLogger.debuggingOn();
			
			// Help?
			if ((currentLine.isOptionSet(OPTION_HELP1))||(currentLine.isOptionSet(OPTION_HELP2))) {
				usage();
				return;
			}
			
			// Properties?
			if (currentLine.hasValue(VALUES_CONFIGURATION)) {
				String path = currentLine.getValue(VALUES_CONFIGURATION).getValue();
				ThingsPropertyTree tree =ThingsPropertyTreeBASIC.getExpedientFromFile(path);
				configPropsView = tree.getRoot();
				configProps = new ThingsPropertyReaderToolkit(tree);
			}
			if (currentLine.hasValue(VALUES_VALUES)) {
				String path = currentLine.getValue(VALUES_VALUES).getValue();
				ThingsPropertyTree tree = ThingsPropertyTreeBASIC.getExpedientFromFile(path);
				valuePropsView = tree.getRoot();
				valueProps = new ThingsPropertyReaderToolkit(tree);
			}
			
			// System stub
			
			
			// RUN IT
			run();
			
		} catch (ThingsException te) {
			
			if (te.numeric==ThingsException.USER_COMMAND_ERROR) {
				
				// RAISED by the command itself.
				if (defaultLogger.debuggingState()) {
					defaultLogger.postit("COMMAND FAILED : " + te.getMessage());
					defaultLogger.postit(te.toStringComplex());					
				} else {
					defaultLogger.postit("COMMAND FAILED : " + te.getMessage() + " : " + te.reportCause());
				}
				
			} else {
				
				// Something underlying in the command went wrong.
				defaultLogger.postit("COMMAND FAILED (unexpected): " + te.getMessage());
				if (defaultLogger.debuggingState()) {
					defaultLogger.postit(te.toStringComplex());
				} else {
					defaultLogger.postit(te.toStringAttributes());
				}
			}		
			
		} catch (Throwable t) {
			
			// SPURIOUS exception.  Might be a bug in the system itself.
			defaultLogger.postit("COMMAND FAILED (spurious): " + t.getMessage());
			if (defaultLogger.debuggingState()) {
				defaultLogger.postit(ThingsException.toStringComplex(t));
			} 
			
		} finally {
			defaultLogger.flush();
		}
	}

	/**
	 * Formats for the usage printing.
	 */
	private final static String FORMAT_ENTITIES_FIRST = "   %-3s           - %-8s, %-71s";
	private final static String FORMAT_ENTITIES_FOLLOWING = "                   %-81s";
	private final static String FORMAT_OPTIONS_FIRST = "   %c             - %-81s";
	private final static String FORMAT_OPTIONS_FOLLOWING = "                   %-81s";
	private final static String FORMAT_VALUES_FIRST = "   %-12s  - %-8s, %-71s";
	private final static String FORMAT_VALUES_FOLLOWING = "                   %-81s";
	
	/**
	 * Print the usage (help).
	 * <p> 
The formats:<br>
<pre>
   aaa           - REQUIRED, AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
                   AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
   b             - BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
                   BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
   cccccccccccc  - OPTIONAL, CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC
                   CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC

"   %-3s           - %-8s, %-71s"
"                   %-81s"
"   %c             - %-81s"
"                   %-81s"
"   %-12s  - %-8s, %-71s"
"                   %-81s"
</pre>
	 */
	public void usage() {
		
		// Writers.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
	
		// Header
		String text = getHeader();
		if (text != null) pw.printf(text);
		
		String[] textStrings;
		
		// Entities - Command line
		CommandParameter	currentParameter;
		StringBuilder helpLine = new StringBuilder("Command general format: ");
		helpLine.append(getName());
		helpLine.append(" [OPTIONS] [VALUES] ");
		for (int index = 1; index < entities.size(); index++) {
			currentParameter = entities.get(index);
			if (currentParameter.required) helpLine.append("(p" + index + ") ");
			else                           helpLine.append("[p" + index + "] ");
		}
		pw.println(helpLine.toString());

		// Entities - parameter help
		pw.println("PARAMETERS :");
		int startIndex = 0;
		if (suppressRootInstallList) startIndex = 1;
		for (int index = startIndex; index < entities.size(); index++) {
			currentParameter = entities.get(index);
			textStrings = ThingsUtilityBelt.snapStrings(currentParameter.message, 71, 81);
			if (textStrings.length == 0) {
				pw.printf(FORMAT_ENTITIES_FIRST, "p"+index, isRequiredText(currentParameter.required), "Undefined."); pw.println();
			} else {
				pw.printf(FORMAT_ENTITIES_FIRST, "p"+index, isRequiredText(currentParameter.required), textStrings[0]); pw.println();	
				for (int stringIndex = 1 ; stringIndex < textStrings.length ; stringIndex++ ) {
					pw.printf(FORMAT_ENTITIES_FOLLOWING, textStrings[stringIndex]); pw.println();			
				}
			}
		}
		
		// Options
		pw.println("OPTIONS (preceeded by a '-' character) :");
		for (int index = 0; index < options.length; index++) {
			if (options[index] != null) {
				textStrings = ThingsUtilityBelt.snapStrings(options[index], 71, 81);
				if (textStrings.length == 0) {
					pw.printf(FORMAT_OPTIONS_FIRST, (char)index, "Undefined."); pw.println();
				} else {
					pw.printf(FORMAT_OPTIONS_FIRST, (char)index, textStrings[0]); pw.println();	
					for (int stringIndex = 1 ; stringIndex < textStrings.length ; stringIndex++ ) {
						pw.printf(FORMAT_OPTIONS_FOLLOWING, textStrings[stringIndex]); pw.println();		
					}
				}
			}
		}
		
		// Values
		pw.println("VALUES (name=value pairs) :");
		for (String item : values.keySet()) {
			currentParameter = values.get(item);
			textStrings = ThingsUtilityBelt.snapStrings(currentParameter.message, 71, 81);
			if (textStrings.length == 0) {
				pw.printf(FORMAT_VALUES_FIRST, currentParameter.name, isRequiredText(currentParameter.required), "Undefined."); pw.println();
			} else {
				pw.printf(FORMAT_VALUES_FIRST, currentParameter.name, isRequiredText(currentParameter.required), textStrings[0]); pw.println();	
				for (int stringIndex = 1 ; stringIndex < textStrings.length ; stringIndex++ ) {
					pw.printf(FORMAT_VALUES_FOLLOWING,textStrings[stringIndex]); pw.println();		
				}
			}
		}
		
		// Footer
		pw.println("INFORMATION :");
		text = getFooter();
		if (text != null) pw.printf(text);
		
		// Post it.
		pw.flush();  // I'm paranoid.
		defaultLogger.postit(sw.toString());
	}
	
	private String isRequiredText(boolean required) {
		if (required==true) return "REQUIRED";
		else return "OPTIONAL";
	}
	
	/**
	 * Get the console print writer.
	 * @return the console print writer.
	 */
	public PrintWriter getConsole() {
		return consolePw;
	}
	
	// =============================================================================================================\
	// == SERVICES TO SUBCLASS
	
	/**
	 * Get an entity (numbered parameter).  If it isn't present and it is REQUIRED, it will throw a CommandException with the numeric COMMANDLINE_ERROR_MISSING_REQUIRED.
	 * If it isn't present and isn't required, it will return a null.
	 * @param item The item number.
	 * @return The value of the parameter.
	 * @throws ThingsException
	 */
	public String getEntity(int item) throws ThingsException {
		if (currentLine==null) ThingsException.softwareProblem("Command called CommandRoot.getRequiredEntity before initialized.");
		String result = currentLine.getEntity(item);
		
		// Only error check if it isn't set.
		if (result == null) {
			if (item >= entities.size()) throw new CommandException("Required parameter missing and not declared.", CommandException.THING_FAULT_COMMANDLINE_NOT_DECLARED, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER,  Integer.toString(item));
			CommandParameter entityDeclaration = entities.get(item);
			
			// If it isn't required, just return the null
			if (entityDeclaration.required) throw new CommandException("Required parameter missing.  " + entityDeclaration.name + " - " + entityDeclaration.message, CommandException.COMMANDLINE_ERROR_MISSING_REQUIRED, ThingsNamespace.ATTR_PARAMETER_HELP, entityDeclaration.message, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER,  entityDeclaration.name);
		}
		return result;
	}
	
	/**
	 * Get an entity (numbered parameter).  If it isn't present it is assumed to be required, and it will throw a CommandException with the numeric COMMANDLINE_ERROR_MISSING_REQUIRED.
	 * @param item The item number.
	 * @param message The entity name or description.  It's used if an error occurs.
	 * @return The value of the parameter.
	 * @throws ThingsException
	 */
	public String getEntityRequired(int item, String message) throws ThingsException {
		if (currentLine==null) ThingsException.softwareProblem("Command called CommandRoot.getRequiredEntity before initialized.");
		String result = currentLine.getEntity(item);
		
		// Only error check if it isn't set.
		if (result == null) {
			throw new CommandException("Required parameter missing: " + message , CommandException.COMMANDLINE_ERROR_MISSING_REQUIRED_ASSUMED, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER,  Integer.toString(item), ThingsNamespace.ATTR_PARAMETER_HELP, message);
		}
		return result;
	}
	
	
	/**
	 * Get a value.   If it isn't present and it is REQUIRED, it will throw a CommandException with the numeric COMMANDLINE_ERROR_MISSING_REQUIRED.
	 * If it isn't present and isn't required, it will return a null.
	 * @param name The item name.
	 * @return The value of the parameter.
	 * @throws ThingsException
	 */
	public String getValue(String name) throws ThingsException {
		if (name == null) throw new CommandException("Value name is null", CommandException.COMMANDLINE_ERROR_NAME_NULL);
		if (currentLine==null) ThingsException.softwareProblem("Command called CommandRoot.getRequiredValue before initialized.");
		NVImmutable resultNV = currentLine.getValue(name);
		String result = null;
		
		// Only error check if it isn't set.
		if (resultNV == null) {
			
			CommandParameter entityDeclaration = values.get(name);
			if ( entityDeclaration==null)  throw new CommandException("Required value parameter missing and not declared.", CommandException.THING_FAULT_COMMANDLINE_NOT_DECLARED, ThingsNamespace.ATTR_PARAMETER_NAME, name);
	
			// If it isn't required, just return the null
			if (entityDeclaration.required) throw new CommandException("Required value parameter missing.  " + entityDeclaration.name + " - " + entityDeclaration.message, CommandException.COMMANDLINE_ERROR_MISSING_REQUIRED, ThingsNamespace.ATTR_PARAMETER_HELP,  entityDeclaration.message, ThingsNamespace.ATTR_PARAMETER_NAME, name);
		
		} else {
			result = resultNV.getValue();
		}
		return result;
	}
	
	/**
	 * Convenience to check if an option is set.  Return true if it is.  
	 * This method is a pass-through to the currentLine.
	 * @param character
	 * @return true if it is set, otherwise false
	 */
	public boolean	optionIsSet(int character){
		return currentLine.isOptionSet(character);
	}
}