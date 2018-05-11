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
package things.thinger;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;
import things.data.tables.Table;
import things.thing.MODULE;
import things.thing.THING;
import things.thinger.io.FileSystemLocator;
import things.thinger.io.Logger;
import things.thinger.kernel.ProcessInterface;
import things.thinger.kernel.ThingsState;
import things.universe.Universe;

/**
* System Interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 7 OCT 03
 * </pre> 
*/
public interface SystemInterface {

	/**
	 * General constants
	 */
	public final static int	DEFAULT_REPORTING_THRESHOLD	=	ThingsCodes.INFO;
	
	/**
	 * Get a system logger for the process.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.SystemException
	 */
	public Logger getSystemLogger() throws SystemException;
	
	/**
	 * Forge a new named logger.  If a logger already exists for that name, it may cause an exception, depending on how it resolves.
	 * <br>
	 * @param name the name.  It will be unique.  It's up to kernel on how the name is resolved.
	 * @return A logger.
	 * @see things.thinger.io.Logger
	 * @throws things.thinger.ThingsException
	 */
	public Logger getNamedLogger(String name) throws ThingsException;
	
	/**
	 * Forge a new named expressor.
	 * <br>
	 * @param name the name.  Generally, it should be unique.  It's up to kernel on how the name is resolved and if name reuse is allowed.
	 * @return an expression interface.
	 * @see things.thinger.ExpressionInterface
	 * @throws things.thinger.ThingsException
	 */
	public ExpressionInterface getNamedExpressor(String name) throws ThingsException;	

	/**
	 * Get local property view for the caller only.  
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties() throws SystemException;
	
	/**
	 * Get shared property view for this server.  Anyone can read and write to them.  
	 * @return a property view
	 * @throws things.thinger.ThingsException
	 */
	public ThingsPropertyView getSharedProperties() throws ThingsException;
	
	/**
	 * Get the read only properties for this for the caller only.  These are configuration properties managed by the system.
	 * @return a property view reader
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyViewReader getConfigProperties() throws SystemException;
	
	/**
	 * Get the process list.<p>
	 * The process list will be a Table.
	 * @return A table representing the process list.
	 * @see things.data.tables.Table
	 * @throws things.thinger.SystemException
	 */
	public Table<String> getProcessList() throws SystemException;
	
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
	public ThingsState getProcessState(String id) throws SystemException;
	
	/**
	 * Get a process interface.  You can only get a process of equal or less clearance.
	 * <p>
	 * @param id String id of the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @return The interface
	 * @throws things.thinger.SystemException
	 */
	public ProcessInterface getProcessInterface(String id) throws SystemException;
	
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
	public void waitProcessDone(String id) throws SystemException, InterruptedException;
	
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
	public FileSystemLocator getLogLocal(String id) throws ThingsException, InterruptedException;

	/**
	 * Load a thing but don't run it.  It will only construct.  It's up to the user to initialize and call it.   Typically, the user should
	 * call .init() and then .call_chain().  This is mostly so that THINGs can calll other THINGs, so perhaps it is best to 
	 * just use THING.CALL instead--if you can.
	 * @param name the name that the loader can use to find it.  Typically, the full class name.
	 * @return The constructed thing.
	 * @throws things.thinger.SystemException
	 */
	public THING loadThing(String name) throws SystemException;
	
	/**
	 * Load a module but don't do anything with it.  It will only construct.  It's up to the user to initialize it. 
	 * @return The constructed MODULE
	 * @param name the name that the loader can use to find it.  Typically, the full class name.
	 * @throws things.thinger.SystemException
	 */
	public MODULE loadModule(String name) throws SystemException;
	
	/**
	 * @param name the resolvable name of the thing.oad and run a thing in a new process.
	 * @return The process id of the THING.
	 * @throws things.thinger.SystemException
	 */
	public String runThing(String name) throws SystemException;

	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.
	 * @return The process id of the THING.
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.
	 * @throws things.thinger.ThingsException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor) throws ThingsException;
	/**
	 * Load and run a thing in a new process, giving an expression parent.  All expressions will go to the parent, plus whatever local
	 * mechanism the kernel decides.  This will let you add properties to the THING's view before it starts.
	 * @return The process id of the THING.
	 * @param name the resolvable name of the thing.
	 * @param parentExpressor the parent expressor.  Set to null if there is no parent.
	 * @param properties properties to add to the THING processes specific view.
	 * @throws things.thinger.ThingsException
	 */
	public String runThing(String name, ExpressionInterface  parentExpressor, ThingsPropertyView	properties) throws ThingsException;
	/**
	 * Ask the kernel for a SuperSystemInterface.  If you can't have it, you'll get a SystemException.  Generally, only services are allowed to have it.
	 * @return The super system interface.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.SystemSuperInterface
	 */
	public SystemSuperInterface requestSuperSystemInterface() throws SystemException;
	
	/**
	 * Get a universe by the local name.  (This is the local name in the registry.)  It will make sure the requestor has sufficient 
	 * clearance to get it. 
	 * <p>
	 * @param name the local name for the universe
	 * @return The universe.  
	 * @throws things.thinger.SystemException
	 * @see things.universe.Universe
	 */
	public Universe getUniverse(String name) throws SystemException;

	/**
	 * Get process ID for the calling prosess. 
	 * <p>
	 * @return The ID.
	 * @throws things.thinger.SystemException
	 */
	public WhoAmI getCallingProcessId() throws SystemException;
	
	/**
	 * Get an empty tree using the preferred, non-persistent implementation for the local host.
	 * @return a new property tree.  
	 */	
	public ThingsPropertyTree getLocalPropertiesImplementation() throws ThingsException;
	
}