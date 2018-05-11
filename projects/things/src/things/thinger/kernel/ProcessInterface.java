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
package things.thinger.kernel;

import things.common.ThingsException;
import things.common.WhoAmI;
import things.thing.RESULT;

/**
 * A process interface for process access.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 25 JUN 05
 * </pre> 
 */
public interface ProcessInterface {

	/**
	 * Get the process state.
	 * <p>
	 * @return the state
	 * @throws things.thinger.ThingsException
	 */
	public ThingsState getThingsState() throws ThingsException;
	
	/**
	 * Request that the process is paused. It will return true if it is already
	 * paused (but not if a request is just pending). The request will be
	 * ignored if the current state cannot be paused. Only a RUNNING process
	 * will respond to a pause. There is no count on this.
	 * <p>
	 * @return true if the process is already paused, else false
	 * @throws things.thinger.ThingsException
	 */
	public boolean requestPause() throws ThingsException;

	/**
	 * Allow it to resume. If the process is not in a state that can be run, it
	 * will ignore this release. It will not override a halt request.
	 * @throws things.thinger.ThingsException
	 */
	public void releasePause() throws ThingsException;

	/**
	 * Request halt. It will interrupt a pause.  It will ultimately cause a PANIC
	 * to propagate through the process user.  If the process isn't running or
	 * paused, it will be ignored.
	 * @throws things.thinger.ThingsException
	 */
	public void requestHalt() throws ThingsException;
		
	/**
	 * @return Returns the name.
	 */
	public String getProcessName();
	
	/**
	 * @return Returns the ID.
	 */
	public WhoAmI getProcessId();

	/**
	 * Force halt.
	 * @throws things.thinger.ThingsException
	 */
	public void forceHalt() throws ThingsException;

	// === IMPLEMENTED IN BASE BUT MAY BE OVERRIDDEN ===================
	
	/**
	 * Get the latest result.  Overrides the basic implementation in the ThingsProcess.
	 * @return the latest or the last result for the thread.  It is completely up to the implementation how to implement this.
	 * @throws ThingsException for whatever reason. It may come from the THING itself.
	 */
	public RESULT getResult() throws ThingsException;
	
}
