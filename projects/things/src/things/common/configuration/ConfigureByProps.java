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
package things.common.configuration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Pattern;

import things.common.StringPoster;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsMarkup;
import things.common.ThingsUsageException;
import things.common.help.Helper;
import things.common.help.Helpful;
import things.common.impl.StringPosterConsole;
import things.common.impl.StringPosterStringBuffer;
import things.common.tools.StreamTools;
import things.data.LineFeeder;
import things.data.ThingsPropertyView;
import things.data.impl.FileAccessor;
import things.data.impl.LineFeederFromStream;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTrunkIO;
import things.data.impl.ThingsPropertyTrunkInMemory;
import things.thinger.io.AFileSystem;
import things.thinger.io.FileSystemTools;
import things.thinger.io.fs.FSFileSystem;

/**
 * A configuration system that uses a SPECIFICATION file and properties.
 * <p>
 * Each line is a whitespace entry in the following format:<br>
 * <token1_action> <token2_modifier> <token3_scope> <path><p>
 * Command lines begin with a hash (#).
 * <p>
 * <pre>
 * token1_action:
 *    copy		- Copy only
 *    config	- Configure
 *    
 * token2_modifier:
 *    only		- If copy, do not touch if configuring.  If config, do not touch if checkpointing.
 *    either	- Do whenever
 *    
 * token3_scope:
 *    single
 *    recurse   
 * </pre>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 JAN 05
 * </pre> 
 */
public class ConfigureByProps implements Helpful {

	// ==============================================================================================
	// FIELDS

	public final static char	COMMENT_CHARACTER = '#';
	public final static String	ACTION_COPY = "copy";
	public final static String	ACTION_CONFIG = "config";
	public final static String	MODIFIER_ONLY = "only";	
	public final static String	MODIFIER_EITHER = "either";	
	public final static String	SCOPE_SINGLE = "single";	
	public final static String	SCOPE_RECURSE = "recurse";	
	
	public final static String	OPERATION_NAME_CHECKPOINT = "checkpoint";	
	public final static String	OPERATION_NAME_CONFIGURE = "configure";		
	

	// ==============================================================================================
	// PRIVATE FIELDS
	
	// Separation pattern.  Whitespaces
	private static Pattern sepPattern = Pattern.compile("\\s");
	private enum ConfigLine { ACTION(0), MODIFIER(1), SCOPE(2), PARAM1(3) ;
        private final int position;    
        private ConfigLine(int position) { this.position = position; }
        @SuppressWarnings("unused")
		public String value(String[] pos) { return pos[this.position]; }
        public boolean is(String term, String[] data) { return data[this.position].toLowerCase().equals(term.toLowerCase()); }
        static public boolean isRecurse(String[] data) { return data[SCOPE.position].toLowerCase().equals(SCOPE_RECURSE);}
        static public String getParam(String[] data) { return data[PARAM1.position]; }
        @SuppressWarnings("unused")
		public int max() { return PARAM1.position; }     
	}
	private enum Operation { CHECKPOINT, RESTORE, CONFIGURE }
	
	// LOCAL NON-STATIC
	private AFileSystem source;
	private AFileSystem destination;
	private StringPoster logger;
	private LineFeeder feeder;
	private int	currentLineNumber;
	private ThingsPropertyView myProps;
	private Operation currentOperation;
	
	// ==============================================================================================
	// METHODS
	
	/**
	 *  Checkpoint the system.
	 *  @param props A property view.
	 *  @param configAsLines A line for the configuration specification.
	 *  @param log A String poster for logging issues.  It is assumed to be verbose.
	 *  @param src An abstract file system for the source location.
	 *  @param dest An abstract file system for the destination location.
	 *  @exception Throwable
	 */	
	public static void checkpoint(ThingsPropertyView props, LineFeeder configAsLines, StringPoster log, AFileSystem  src, AFileSystem  dest) throws Throwable {
		ConfigureByProps meThingie = new ConfigureByProps();
		meThingie.init(Operation.CHECKPOINT.name(), props, configAsLines, log, src, dest);
		meThingie.currentOperation = Operation.CHECKPOINT;
		meThingie.engine();
	}
	
	/**
	 *  Configure the system.
	 *  @param props A property view.
	 *  @param configAsLines A line for the configuration specification.
	 *  @param log A String poster for logging issues.  It is assumed to be verbose.
	 *  @param src An abstract file system for the source location.
	 *  @param dest An abstract file system for the destination location.
	 *  @exception Throwable
	 */	
	public static void configure(ThingsPropertyView props, LineFeeder configAsLines, StringPoster log, AFileSystem  src, AFileSystem  dest) throws Throwable {
		ConfigureByProps meThingie = new ConfigureByProps();
		meThingie.init(Operation.CONFIGURE.name(), props, configAsLines, log, src, dest);
		meThingie.currentOperation = Operation.CONFIGURE;
		meThingie.engine();
	}
	
	
	/**
	 * Provide basic help as a string.  It should use ThingsMarkup for gimmicks.
	 * @see things.common.ThingsMarkup
	 * @return The text of the help.
	 */  
    public String help() {
    	return "This is a configuration system that uses a SPECIFICATION file and properties." + ThingsMarkup.NEW_PARAGRAPH;
   }
    
	/**
	 * Provide detailed information.  It should use ThingsMarkup for gimmicks.
	 * @see things.common.ThingsMarkup
	 * @return The text of the information.
	 */  
    public String information() {
 
    	return "This is a configuration system that uses a SPECIFICATION file and properties." + ThingsMarkup.NEW_PARAGRAPH +
    	"Each line is a whitespace entry in the following format:" + ThingsMarkup.NEW_LINE +
    	"<tab>token1_action<tab>token2_modifier<tab>token3_scope<tab>path" + ThingsMarkup.NEW_LINE +
    	"Command lines begin with a hash (#)." + ThingsMarkup.NEW_LINE +
    	"<tab>token1_action can be one of the following:" + ThingsMarkup.NEW_LINE +
    	"<tab><tab>copy<tab>= Copy only" + ThingsMarkup.NEW_LINE +
    	"<tab><tab>config<tab>= Configure:" + ThingsMarkup.NEW_PARAGRAPH +
    	"<tab>token2_modifier can be one of the following:" + ThingsMarkup.NEW_LINE +
    	"<tab><tab>only<tab>= If copy, do not touch if configuring." + ThingsMarkup.NEW_LINE +
    	"<tab><tab>    <tab>  If config, do not touch if checkpointing." + ThingsMarkup.NEW_LINE +
    	"<tab><tab>either<tab>= Do whenever" + ThingsMarkup.NEW_PARAGRAPH +
    	"<tab>token3_scope can be one of the following:" + ThingsMarkup.NEW_LINE +
    	"<tab><tab>single<tab>= Apply to listed path only." + ThingsMarkup.NEW_LINE +
    	"<tab><tab>recurse<tab>= Apply to current path and all subdirectories." + ThingsMarkup.NEW_PARAGRAPH;	
    }
    
	/**
	 * Provide a helper for the help.  This allows you to create a richer set of property help.  It may return null if there is no more help.
	 * @return the helper or null.
	 */  
    public Helper helper() {
    	return null;
    }
	
	// ==============================================================================================
	// PRIVATE METHODS
	
    /**
     * Internal engine.
     * @throws Throwable
     */
	private void engine() throws Throwable {
		logit("Starting.");
		try {
			String[] configs = nextConfigLine();
			while (configs!=null) {
				
				if (ConfigLine.ACTION.is(ACTION_COPY,configs)) {
					
					if (ConfigLine.MODIFIER.is(MODIFIER_ONLY,configs)) {
						
						if (currentOperation == Operation.CHECKPOINT) {
							process(ConfigLine.getParam(configs),ConfigLine.isRecurse(configs),currentOperation);
						} else {
							logline("Ignoring line, Copy Only during checkpoint.");
						}
						
					} else if (ConfigLine.MODIFIER.is(MODIFIER_EITHER,configs)) {
						process(ConfigLine.getParam(configs),ConfigLine.isRecurse(configs),currentOperation);
					} else {
						logline("Bad line, unknown modifer for action Copy.");
					} // end if MODIFIER
					
				} else if (ConfigLine.ACTION.is(ACTION_CONFIG,configs)) {
					
					if (ConfigLine.MODIFIER.is(MODIFIER_ONLY,configs)) {
						
						if (currentOperation == Operation.CHECKPOINT) {
							logline("Ignoring line, Config Only during checkpoint.");
						} else {			
							process(ConfigLine.getParam(configs),ConfigLine.isRecurse(configs),currentOperation);
						}
						
					} else if (ConfigLine.MODIFIER.is(MODIFIER_EITHER,configs)) {
						process(ConfigLine.getParam(configs),ConfigLine.isRecurse(configs),currentOperation);
					} else {
						logline("Bad line, unknown modifer for action Config.");
					} // end if MODIFIER
					
				} else {
					logline("Bad line, unknown Action.");
				} // end if ACTION
				
				// DONT EDIT AFTER!
				configs = nextConfigLine();
			} // end while
		} catch (ThingsException te) {
			throw new ThingsException("Failed to " + currentOperation.toString() + ".  cause=" + te.numeric + " message=" + te.getMessage(),ThingsException.CONFIGURATION_FAILED_CHECKPOINT,te);
		} catch (Throwable e) {
			throw new ThingsException("Failed to " + currentOperation.toString() + " due to low-level fault.  message=" + e.getMessage(),ThingsException.CONFIGURATION_FAULT_DEFAULT,e);		
		}			
		logit("Done.");
	}
	
	
	/**
	 * Process an entry.
	 * @param root
	 * @param recurse
	 * @param op
	 * @throws ThingsException
	 */
	private void process(String root, boolean recurse, Operation op) throws ThingsException {

		// Remember the line for error reporting
		String working = null;
		
		try {
		
			if (source.isDirectory(root)) {
				List<String> listDirs = source.paths(root);
				if (listDirs==null) return;
				for (String item : listDirs) {
					working = item;
					if (source.isFile(item)) {
						if ((op==Operation.CHECKPOINT)||(op==Operation.RESTORE)) {
							destination.copy(item,source.openInputStream(item));
							logline("Copied " + FileSystemTools.normalizePath(item));
						} else {
							configurator(item);
						}
						
					} else if (source.isDirectory(item)) {
						if (recurse) {
							this.process(item,recurse,op);
						} else {
							logline("Ignore directory " + FileSystemTools.normalizePath(item));
						}
						
					} else {
						throw new ThingsException(root + " is neither a file nor directory.",ThingsException.FILESYSTEM_ERROR_NOT_A_KNOWN_TYPE);
					}
					
				} // end for
			
			} else {	
				working = root;	// In case we have to print the exception below.
				if ((op==Operation.CHECKPOINT)||(op==Operation.RESTORE)) {
					destination.copy(root,source.openInputStream(root));
					logline("Copied " + FileSystemTools.normalizePath(root));
				} else {
					configurator(root);				 
				}
			}	
		
		} catch (ThingsException te) {
			logline("Failed " + FileSystemTools.normalizePath(working));
			throw te;
		}
	}
	
	/**
	 * Perform a configuration.
	 * @param item
	 * @throws ThingsException
	 */
	private void configurator(String item) throws ThingsException {
		
		StringPosterStringBuffer reporter = new StringPosterStringBuffer();
		BufferedReader bin = null;
		BufferedWriter bout = null;
		try {
			bin = new BufferedReader(new InputStreamReader(source.openInputStream(item)));
			bout  = new BufferedWriter(new OutputStreamWriter(destination.openOutputStream(item)));				
			if (!StreamTools.merge(bin,bout,myProps,reporter)) {
					throw new ThingsException(reporter.toString(),ThingsException.CONFIGURATION_ERROR_BAD_CONFIGURATION);
			} 
			logline("Configured " + FileSystemTools.normalizePath(item));
			
		} catch (ThingsException tee) {
			throw tee;
		} catch (Exception ee) {
			throw new ThingsException("Configurator exception.  " + ee.getMessage(),ThingsException.CONFIGURATION_ERROR_BAD_CONFIGURATION,ee);			
		} finally {
			try {
				bout.close();			
			} catch (Exception e) {
				// ignore
			}
			try {
				bin.close();			
			} catch (Exception e) {
				// ignore
			}
		}
	}
		
	// ==============================================================================================
	// PRIVATE TOOLS
	
	/**
	 *  Default internal initializer
	 */
	private void init(String name, ThingsPropertyView props, LineFeeder configAsLines, StringPoster log, AFileSystem  src, AFileSystem  dest) throws Throwable  {
		source = src;
		destination = dest;
		logger = log;
		myProps = props;	
		startParseConfigLine(configAsLines);
	}
	
	/**
	 *  Start the line parser.  It will set the line number.
	 *  @param feed A line feeder from the configuration source.
	 *  @exception Throwable
	 */
	private void startParseConfigLine(LineFeeder feed) throws Throwable {
		currentLineNumber = feed.lineNumber();
		feeder = feed;
	}
	
	/**
	 *  Parse the next config line.  Comment lines (starting with #) are ignored.  The first two entries will be automatically converted to lowercase.
	 *  @return The split line config line that is not a comment.  It does not validate the line.  If there are no more lines, it will return null.
	 *  @exception Throwable
	 */
	private String[] nextConfigLine() throws Throwable {
		
		if (feeder==null) throw new Exception("INTERNAL SOFTWARE PROBLEM (a bug): startParseConfigLine not called before nextConfigLine.");
		String returnVal[] = null;
		try {
			String candidate[];
			String currentLine = feeder.getNextLine();
			while (currentLine != null) {
				candidate = sepPattern.split(currentLine);	
				if ((currentLine.length()>0)&&(candidate!=null)&&(candidate.length>0)) {
					if (candidate[0].charAt(0)!=COMMENT_CHARACTER) {
						candidate[0] = candidate[0].toLowerCase();
						if (candidate.length>1) candidate[1] = candidate[1].toLowerCase();
						returnVal = candidate;
						break;
					}
				}
				currentLine = feeder.getNextLine();
			}
			currentLineNumber = feeder.lineNumber();

		} catch (Exception e) {
			// Quit for ANY exception
			throw new ThingsException("Low-level configuration FAULT.  message=" + e.getMessage(),ThingsException.CONFIGURATION_FAULT_DEFAULT,e);
		}
		return returnVal;
	}
	
	/**
	 *  Log a message for a line.  Any error will be quietly ignored.
	 *  @param message A message to log.
	 */
	private void logline(String message) {
		logger.postit(formLogline(message));
	}	
	
	/**
	 *  Form a log a message for a line.
	 *  @param message A message to log.
	 *  @return the formed log line.
	 */
	private String formLogline(String message) {
		if (message==null) return null;
		return new String(currentOperation.toString() + ": line=" + currentLineNumber + " " + message);
	}	
	
	/**
	 *  Log a general message.  Any error will be quietly ignored.
	 *  @param message A message to log.  
	 */
	private void logit(String message) {
		logger.postit(currentOperation.toString() + ": " + message);
	}	
	
	// ==============================================================================	
	// ==============================================================================
	// CHEAP IMPLEMENTATION FOR FILE BASED IMPLEMENTATION

	/**
	 *  Print usage to a poster
	 *  @param log A String poster for logging issues.  It is assumed to be verbose.
	 */	
	static public void usage(StringPoster log) {
		log.postit("Configuration for Things.");
		log.postit(ThingsConstants.COPYRIGHT_NOTICE);
		log.postit("<command> <operation> <config file> <source> <destination> <?properties?>");
		log.postit("config " + OPERATION_NAME_CHECKPOINT + " [config file] [root source] [root destination]");
		log.postit("   Run a checkpoint.  It'll save files for source to destination.");
		log.postit("config " + OPERATION_NAME_CONFIGURE + " [config file] [root source] [root destination] [property ini]");
		         //--------------------------------------------------------------------------------
		log.postit("   Run a configuration.  It'll load the property ini from a file and use it");
		log.postit("   to run the configuration.");		
		log.postit("help");
		log.postit("   Show this information.");
	} 
	
	/**
	 * Cheap main() implementation for command line usage.
	 * @param args The arguments.
	 */
	public static void main(String[] args) {
		
		StringPosterConsole logger = new StringPosterConsole();
		
		try {

			// Validate and fetch required
			String configFile = Argument.CONFIG_FILE.getRequired(args);
			String source = Argument.SOURCE.getRequired(args);			
			String destination = Argument.DESTINATION.getRequired(args);	
			
			// Show config.
			logger.postit("CONFIGURATION TOOL ConfigureByProps - THINGS 2007");
			logger.postit("Configuration file = " + configFile);
			logger.postit("Source directory   = " + source);
			logger.postit("Target directory   = " + destination);
			
			// Set up the property tree
			ThingsPropertyTreeBASIC propTree = new ThingsPropertyTreeBASIC();
			
			// build our file systems and prep the source file 
			FSFileSystem workingFS = new FSFileSystem(AFileSystem.ROOT);
			FSFileSystem destinationFS = new FSFileSystem(destination);
			FSFileSystem sourceFS = new FSFileSystem(source);
			LineFeederFromStream inLines = new LineFeederFromStream();
			inLines.init(workingFS.openInputStream(configFile));
			
			// prep the properties.  Init it the file, if passes, otherwise leave it in-memory.
			String propertyIniFile = Argument.PROPERTY_INI_FILE.getOptional(args);
			if (propertyIniFile != null) {
				logger.postit("Config props       = " + propertyIniFile);
				
				// If init has a problem, it will throw a ThingsException and call the whole show off.
				ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
				trunk.init(propertyIniFile, new FileAccessor(new File(propertyIniFile)));
				propTree.init(trunk);			
				propTree.load();

			} else {
				propTree.init(new ThingsPropertyTrunkInMemory());			
			}
			
			// Validate and direct OPERATION
			if (Argument.OPERATION.has(args)) {
				
				if (Argument.OPERATION.areTheSame(args,OPERATION_NAME_CHECKPOINT)) {
					
					// CHECKPOINT
					checkpoint(propTree.getRoot(), inLines, logger, sourceFS, destinationFS);
					
				} else if (Argument.OPERATION.areTheSame(args,OPERATION_NAME_CONFIGURE)) {
					
					// CONFIGURE
					configure(propTree.getRoot(), inLines, logger, sourceFS, destinationFS);
					
				} else {
					throw new ThingsUsageException("Command not recognized.  command=" + Argument.OPERATION.query(args));
				} 
				
			} else {
				throw new ThingsUsageException("Missing <operation> parameter.");
			}
			
			logger.postit("DONE.");
			
		} catch (ThingsUsageException tue) {
			logger.postit("USAGE ERROR:  " + tue.getMessage());			
			usage(logger);
			
		} catch (ThingsException te) {
			// logger.postit("Failure.  Error number=" + te.numeric);
			logger.postit(te.toStringComplex());
			
		} catch (Throwable e) {
			logger.postit("Serious failure.  This is likely a bug in the system.");
			logger.postit(e.getMessage());
			e.printStackTrace();
		}
	}
		
	/**
	 * A special enum to handle the arguments.
	 */
	private enum Argument { 
		OPERATION(0,"<operation>"), CONFIG_FILE(1,"<config file>"), 
		SOURCE(2,"<source>"), DESTINATION(3,"<destination>"), PROPERTY_INI_FILE(4,"<?properties?>"), MAX(4,"ERROR");
	    private final int position; 
		public  final String name;
	    private Argument(int iposition, String iname) { this.position = iposition; this.name= iname; }	
	    @SuppressWarnings("unused")
		static public int max() { return MAX.position; }
		public String query(String[] args) {
			if (args.length > position)
				return ThingsConstants.EMPTY_STRING;
			else
				return args[position];
		}	
		public boolean has(String[] args) {
			if (args.length > position)
				return true;
			return false;
		}	
		public boolean areTheSame(String[] args, String comparitor) {
			try {
				if (args[position].equalsIgnoreCase(comparitor)) return true;	
			} catch (Throwable te) {
				// pass through to false
			}
			return false;
		}	
		public String getRequired(String[] args) throws ThingsUsageException {
			if (args.length > position)
				return args[position];
			throw new ThingsUsageException("Missing required parameter '" + name + "'");
		}
		public String getOptional(String[] args) throws ThingsUsageException {
			if (args.length > position)
				return args[position];
			return null;
		}
	}
}