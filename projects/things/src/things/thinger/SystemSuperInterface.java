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

import things.common.ThingsException;
import things.common.WhoAmI;
import things.data.ThingsPropertyView;
import things.thinger.io.conduits.ConduitController;
import things.thinger.kernel.Clearance;
import things.thinger.kernel.PCB;
import things.thinger.kernel.ThingsProcess;

/**
 * System Interface used by internal systems.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 8 OCT 03
 * EPG - Add user global properties - 19 JUL 07
 * </pre> 
*/
public interface SystemSuperInterface extends SystemInterface {

	/**
	 * Get system global property view.  This is primarily for system work.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getGlobalProperties() throws SystemException;
	
	/**
	 * Get user global property view.  These properties are copied into a user process as local properties when the process is created.  It's a
	 * snapshot, updated to the original global properties and the new local properties will not affect each other.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getUserGlobalProperties() throws SystemException;
	
	/**
	 * Get the configuration properties that are writable.  Anything that has access to the SSI can touch these.
	 * @return a property view
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getConfigPropertiesWritable() throws SystemException;
	
	/**
	 * Get local property view for the given  id.  
	 * @param id String id of the process.
	 * @return a property view or null if the id doesn't identify any known process.
	 * @throws things.thinger.SystemException
	 */
	public ThingsPropertyView getLocalProperties(String id) throws SystemException;
	
	/**
	 * Typically, this is a last ditch way for a process or
	 * module to pass info to the kernel when something very
	 * bad is happening.  There is no feedback loop.
	 * @param te a Things exception
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void flingException(ThingsException te);
	
	/**
	 * Typically, this is how a process will tell the kernel it
	 * is dying, so that the kernel can clear resources.  This really should be the LAST thing a process
	 * does before exiting run().  If can be used instead of flingException.
	 * @param te a Things exception that indicates the reason for the death.  It may be null if it was normal termination.
	 * @throws things.thinger.SystemException
	 * @see things.common.WhoAmI
	 */
	public void deathNotice(ThingsException te);
	
	/**
	 * Ask the server to quit.
	 */
	public void requestQuit();
	
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
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties, Clearance	processClearance) throws Throwable;
	
	/**
	 * Start the passed process.  Assume a loader will be doing this.  The process should
	 * be loaded, constructed, but not initialized.   If the state is not STATE_CONSTRUCTION, it
	 * will throw an exception.
	 * <br>
	 * All processes started with this will have DEFAULT_USER_CLEARANCE.
	 * <p>
	 * @param processObject This will be a ThingsProcess or subclass.
	 * @param properties These are properties to add (or supplant) to the processes specific view before starting the process.  It is ok
	 * to pass null if there are none.
	 * @throws Throwable
	 * @return the ID of the started process.
	 * @see things.common.WhoAmI
	 */
	public WhoAmI startProcess(ThingsProcess processObject, ThingsPropertyView properties) throws Throwable;

	/**
	 * Register a ready-made PCB.  It'll allows you to set the clearance level.  The
	 * kernel may choose to deny the operation.  So normally, use startProcess instead.  The process will
	 * not be given standard property paths and initialization, as you get with the startProcess method.
	 * This exists mostly for debugging and testing, but may be useful for processes that live outside
	 * the normal processing framework.
	 * <br>
	 * @param processPCB This will be a ready-made PCB.
	 * @param processClearance The clearance level.  This will be immutable.
	 * @throws things.common.ThingsException
	 */
	public void registerProcess(PCB processPCB, Clearance		processClearance) throws ThingsException;
	
	/**
	 * Get the system conduit controller.  These are for conduits between privileged services.
	 * @return a ConduitController
	 * @throws things.thinger.SystemException
	 * @see things.thinger.io.conduits.ConduitController
	 */
	public ConduitController getSystemConduits() throws SystemException;

}
