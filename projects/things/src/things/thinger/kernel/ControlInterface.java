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

/**
 * A control interface for subunits to deal with process methods.  This is so you can control your own process, not someone else's.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 JUN 05
 * </pre> 
 */
public interface ControlInterface {

	// **********************************************************
	// * STATE MANAGEMENT

	/**
	 * This thread will accept a pause request.
	 * @throws things.thinger.ThingsException
	 */
	public void acceptPause() throws ThingsException;
	
	/**
	 * This will accept the halt if a halt is pending.  THIS WILL THROW A 
	 * SystemException.PANIC_PROCESS_RESPONDING_TO_HALT_OK if the halt is accepted!
	 * @throws things.thinger.ThingsException
	 */
	public void acceptHalt() throws ThingsException;
	
}
