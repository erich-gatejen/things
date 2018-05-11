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
package test.things;

import test.things.universe.server.CommonUniverseTestInfrastructure;
import things.common.StringPoster;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.common.impl.StringPosterConsole;
import things.common.impl.WhoAmISimple;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTreeRAM;
import things.data.tables.Table;
import things.thing.MODULE;
import things.thing.THING;
import things.thinger.ExpressionInterface;
import things.thinger.SystemException;
import things.thinger.SystemSuperInterface;
import things.thinger.io.FileSystemLocator;
import things.thinger.io.Logger;
import things.thinger.io.conduits.ConduitController;
import things.thinger.kernel.Clearance;
import things.thinger.kernel.PCB;
import things.thinger.kernel.ProcessInterface;
import things.thinger.kernel.ThingsProcess;
import things.thinger.kernel.ThingsState;
import things.thinger.kernel.basic.KernalBasic_LoggingExpressor;
import things.universe.Universe;

/**
* A stub for testing.  Only implemented as far as needed by the tests.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 NOV 06 2
 * </pre> 
*/
public class STUB_SystemSuperInterface implements SystemSuperInterface {

	// =================================================================================
	// == FIELDS
	
	public ThingsPropertyView properties;
	public Logger stubLogger;
	public StringPoster postLogger;
	public CommonUniverseTestInfrastructure universeInfrastructure;
	public WhoAmI myFakeId;
	
	// =================================================================================
	// == STATIC TOOLS 
	
	private static STUB_SystemSuperInterface currentStub;
	
	/**
	 * Get the global stub.  It'll use a simple console logger.
	 * @param properties
	 * @return
	 * @throws Throwable
	 */
	public static STUB_SystemSuperInterface getStub(ThingsPropertyView properties) throws Throwable {
		if (currentStub==null) {
			currentStub = new STUB_SystemSuperInterface(properties, new StringPosterConsole());
		} 
		return currentStub;
	}
	
	/**
	 * Get the global stub.
	 * @param properties
	 * @param logger
	 * @return
	 * @throws Throwable
	 */
	public static STUB_SystemSuperInterface getStub(ThingsPropertyView properties, StringPoster logger) throws Throwable  {
		if (currentStub==null) {
			currentStub = new STUB_SystemSuperInterface(properties, logger);
		} 
		return currentStub;
	}
	
	/**
	 * Get the global stub.  This assumes it has already been built.
	 * @return
	 * @throws Throwable
	 */
	public static STUB_SystemSuperInterface getStub() throws Throwable {
		if (currentStub==null) {
			throw new Exception("Static global stub not created yet, so can't use getStub() method.");
		} 
		return currentStub;
	}
	
	// =================================================================================
	// == METHODS 
	
	/**
	 * Create the stub using a real properties file.
	 * @param properties
	 * @param logger A simple logger.
	 * @throws Throwable
	 */
	public STUB_SystemSuperInterface(ThingsPropertyView properties, StringPoster logger) throws Throwable { 
		this.properties = properties;	
		constructorCommon(logger);
	}
	
	/**
	 * Create the stub using a real properties file.
	 * @param propertiesFilePath
	 * @param logger A simple logger.
	 * @throws Throwable
	 */
	public STUB_SystemSuperInterface(String  propertiesFilePath, StringPoster logger) throws Throwable { 
		properties = ThingsPropertyTreeBASIC.getExpedientFromFile(propertiesFilePath);	
		constructorCommon(logger);
	}
	
	/**
	 * Create the stub using in memory properties.  You can play with them with the 'properties' field.
	 * @param propertiesFilePath
	 * @param logger A simple logger.
	 * @throws Throwable
	 */
	public STUB_SystemSuperInterface(StringPoster logger) throws Throwable { 
		properties = new ThingsPropertyTreeRAM();	
		constructorCommon(logger);
	}
	
	/**
	 * Common construction tasks.
	 * @param logger
	 */
	private void constructorCommon(StringPoster logger) throws Throwable {
		postLogger = logger;
		stubLogger =  new STUB_Logger(postLogger);
		myFakeId = new WhoAmISimple();
	}
	
	// =================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Get system global property view.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getGlobalProperties() throws SystemException {
		return properties;
	}
	
	/**
	 * Get user global property view. 
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getUserGlobalProperties() throws SystemException {
		return properties;		
	}
	
		/**
	 * Get shared property view for this server.  Anyone can read and write to them.  
	 * @return a property view
	 * @throws things.thinger.ThingsException
	 */
	public ThingsPropertyView getSharedProperties() throws ThingsException {
		return properties;		
	}
	
	/**
	 * Get the configuration properties that are writable.  Anything that has access to the SSI can touch these.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getConfigPropertiesWritable() throws SystemException {
		return properties;
	}
	
	/**
	 * Get local property view for the given  id.  
	 * @param id String id of the process.
	 * @return a property view or null if the id doesn't identify any known process.
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties(String id) throws SystemException {
		return properties;
	}
	
	/**
	 * Typically, this is a last ditch way for a process or
	 * module to pass info to the kernel when something very
	 * bad is happening.  There is no feedback loop.
	 * @param te a Things exception
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void flingException(ThingsException te) {
		// Nothing.
	}
	
	/**
	 * Typically, this is how a process will tell the kernel it
	 * is dying, so that the kernel can clear resources.  This really should be the LAST thing a process
	 * does before exiting run().  If can be used instead of flingException.
	 * @param te a Things exception that indicates the reason for the death.  It may be null if it was normal termination.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void deathNotice(ThingsException te) {
		// Nothing
	}
	
	/**
	 * Ask the server to quit.
	 */
	public void requestQuit() {
		// Nothing/
	}
	
	/**
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, contructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * <br>
	 * All processes started with this will have DEFAULT_USER_CLEARANCE.
	 * <p>
	 * @param processObject This will be a ThingsProcess or subclass.
	 * @param properties These are properties to add (or supplant) to the processes specific view before starting the process.  It is ok
	 * to pass null if there are none.
	 * @throws things.thinger.ThingsException
	 * @return the ID of the started process.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties) throws ThingsException {
		return null;
	}
	
	/**
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, constructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * <p>
	 * @param processObject This will be a ThingsProcess or subclass.
	 * @param properties These are properties to add (or supplant) to the processes specific view before starting the process.  It is ok
	 * to pass null if there are none.
	 * @param processClearance specify the process clearance level.  This must be at or lower than the calling process's clearance.
	 * @throws Throwable
	 * @return the ID of the started process.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties, Clearance	processClearance) throws Throwable {
		return null;
	}
	
	/**
	 * Register a ready-made PCB.  It'll allows you to set the clearance level.  The
	 * kernel may choose to deny the operation.  So normally, use startProcess instead.  The process will
	 * not be given standard property paths and initialization, as you get with the startProcess method.
	 * This exists mostly for debugging and testing, but may be useful for processes that live outside
	 * the normal processing framework.
	 * <br>
	 * @param processPCB This will be a ready-made PCB.
	 * @param processClearance The clearance level.  This will be immutable.
	 * @throws things.thinger.ThingsException
	 */
	public void registerProcess(PCB processPCB, Clearance		processClearance) throws ThingsException {
		
	}

	/**
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, contructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * @return a ConduitController
	 * @throws things.thinger.SystemException
	 * @see things.thinger.io.conduits.ConduitController
	 */
	public ConduitController getSystemConduits() throws SystemException {
		return null;
	}

	/**
	 * Get a system logger for the process.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.SystemException
	 */
	public Logger getSystemLogger() throws SystemException {
		return stubLogger;
	}
	
	/**
	 * Forge a new named logger.  If a logger already exists for that name, it may cause an exception, depending on how it resolves.
	 * <br>
	 * @param name the name.  It will be unique.  It's up to kernel on how the name is resolved.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.ThingsException
	 */
	public Logger getNamedLogger(String name) throws ThingsException {
		return stubLogger;
	}
	
	/**
	 * Forge a new named expressor.
	 * <br>
	 * @param name the name.  Generally, it should be unique.  It's up to kernel on how the name is resolved and if name reuse is allowed.
	 * @return an expression interface.
	 * @see things.thinger.ExpressionInterface
	 * @throws things.thinger.ThingsException
	 */
	public ExpressionInterface getNamedExpressor(String name) throws ThingsException {
		return new KernalBasic_LoggingExpressor(stubLogger, this);
	}
	
	
	/**
	 * Get local property view for the caller only.  
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties() throws SystemException {
		return properties;
	}
	
	/**
	 * Get the read only properties for this for the caller only.  
	 * @return a property view reader
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyViewReader getConfigProperties() throws SystemException {
		return properties;
	}
	
	/**
	 * Get the process list.<p>
	 * The process list will be a Table.
	 * @return A table representing the process list.
	 * @see things.data.tables.Table
	 * @throws things.thinger.SystemException
	 */
	public Table<String> getProcessList() throws SystemException {
		return new Table<String>();
	}
	
	/**
	 * Get the state of a specific process.<p>
	 * If the process is not found, the state is ProcessInterface.ThingsState.STATE_INVALID.
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @return The state.
	 * @throws things.thinger.SystemException
	 */
	public ThingsState getProcessState(String id) throws SystemException{
		return ThingsState.STATE_RUNNING;
	}
	
	/**
	 * Get a process interface.  You can only get a process of equal or less clearance.
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @return The interface
	 * @throws things.thinger.SystemException
	 */
	public ProcessInterface getProcessInterface(String id) throws SystemException {
		return null;
	}
	
	/**
	 * Wait until the named process if done (meaning any state that satisfies ProcessInterface.ThingsState.isDeadOrDying()==true).<p>
	 * If the process is not found, it will quietly return.
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @throws things.thinger.SystemException for general errors or InterruptedException for thread control.  Always let the InterruptedException out.
	 */
	public void waitProcessDone(String id) throws SystemException, InterruptedException {
		// NOP
	}
	
	/**
	 * Get a local reference to the log if possible.  This is totally up to the implementation.  It may be the whole log, a snippet, or null (no log at all).
	 * <p>
	 * <b>NO CLEARANCE REQUIRED.</b>
	 * <p>
	 * @param id String id of the process.
	 * @return log file locator or null
	 * @see things.thinger.kernel.ProcessInterface
	 * @see things.thinger.io.FileSystemLocator
	 * @throws things.thinger.ThingsException for general errors or InterruptedException for thread control.  Always let the InterruptedException out.
	 */
	public FileSystemLocator getLogLocal(String id) throws ThingsException, InterruptedException {
		return null;
	}
	
	/**	 
	* Load a thing but don't run it.  It will only construct.  It's up to initialize and call it.   Typically, the user should
	 * call .init() and then .call_chain().  This is mostly so that THINGs can calll other THINGs, so perhaps it is best to 
	 * just use THING.CALL instead--if you can.
	 * @return The constructed thing.
	 * @throws things.thinger.SystemException
	 */
	public THING loadThing(String name) throws SystemException {
		return null;
	}
	
	/**
	 * Load a module but don't do anything with it.  It will only construct.  It's up to the user to initialize it. 
	 * @return The constructed MODUKE
	 * @param the name that the loader can use to find it.  Typically, the full class name.
	 * @throws things.thinger.SystemException
	 */
	public MODULE loadModule(String name) throws SystemException {
		return null;
	}
	
	/**
	 * @param name the resolvable name of the thing.oad and run a thing in a new process.
	 * @return The name of the thing. 
	 * @throws things.thinger.SystemException
	 */
	public String runThing(String name) throws SystemException {
		return "";
	}
	
	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.
	 * @return The name of the thing. 
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.
	 * @throws things.thinger.ThingsException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor) throws ThingsException {
		return "";	
	}

	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.  This will let you add properties to the THING's view before it starts.
	 * @return The name of the thing. 
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.  Set to null if there is no parent.
	 * @param properties properties to add to the THING processes specific view.
	 * @throws things.thinger.ThingsException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor, ThingsPropertyView	properties) throws ThingsException {
		return "";
	}
	

	/**
	 * Ask the kernel for a SuperSystemInterface.  If you can't have it, you'll get a SystemException.  Generally, only services are allowed to have it.
	 * @return The super system interface.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.SuperSystemInterface
	 */
	public SystemSuperInterface requestSuperSystemInterface() throws SystemException {
		return this;
	}
	
	/**
	 * Get a universe by the local name.  (This is the local name in the registry.)  It will make sure the requestor has sufficient 
	 * clearance to get it. 
	 * <p>
	 * @param name the local name for the universe
	 * @return The universe.  
	 * @throws things.thinger.SystemException
	 * @see things.universe.Universe
	 */
	public Universe getUniverse(String name) throws SystemException {
		
		Universe result = null;
		
		try {
			
			// Set up the CommonUniverseTestInfrastructure if we haven't already.  This means the CommonUniverseTestInfrastructure has to have
			// the right properties available!   As of writing this method, that's just REQUIRED_PROP_ROOT_DIR.
			if (universeInfrastructure==null) {
				universeInfrastructure =  new CommonUniverseTestInfrastructure();
				universeInfrastructure.init(properties);
			}
			
			if (name.equals(CommonUniverseTestInfrastructure.UNIVERSE_A))  result = universeInfrastructure.getA();
			else if (name.equals(CommonUniverseTestInfrastructure.UNIVERSE_B))   result = universeInfrastructure.getB();

			
		} catch (Throwable t) {
			throw new SystemException("FAULT : " + t.getMessage(), t);
		}
		return result;
	}
	
	/**
	 * Get process ID for the calling prosess. 
	 * <p>
	 * @return The ID.
	 * @throws things.thinger.SystemException
	 */
	public WhoAmI getCallingProcessId() throws SystemException {
		return myFakeId;
	}
	
	/**
	 * Get an empty tree using the preferred, non-persistent implementation for the local host.
	 * @return a new property tree.  
	 */	
	public ThingsPropertyTree getLocalPropertiesImplementation() throws ThingsException {
		return new ThingsPropertyTreeRAM();
	}
}
