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
package things.thinger.service;

import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.kernel.PCB;
import things.thinger.kernel.ThingsProcess;

/**
 * Root service.  Adds on() and off() as methods.
 * <p>
 * Like a process, you will need to start() it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 NOV 04
 * </pre> 
 */
public abstract class Service extends ThingsProcess {

	// =========================================================================================================
	// PROTECTED FIELDS
	
	/**
	 * The default logger provided by the system.
	 */
	protected Logger myLogger = null;

	// =========================================================================================================	
	// ABSTRACT METHODS
	
	/**
	 * Called to turn the service on.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public abstract void serviceOn() throws SystemException;
	
	/**
	 * Called to turn the service off.  This may be called by another thread.
	 * @throws things.thinger.SystemException
	 */
	public abstract void serviceOff() throws SystemException;
	
	// =========================================================================================================
	// IMPLEMENTATIONS
	
	/**
	 * Set the logger for this service.  This can only be called once or it will throw an exception.  This WILL be called by the Kernel, so
	 * don't call it yourself.  If the logger is in debug mode, it will push it down to the process itself for internal logging.
	 * @param theLogger The logger to set.
	 * @throws things.thinger.SystemException
	 */
	public synchronized void setLogger(Logger theLogger) throws SystemException {
		if (myLogger != null) SystemException.softwareProblem("Something tried to setLogger() on this service more than once.", new Exception(), SystemNamespace.ATTR_PROCESS_ID_ORGANIC, PCB.getCallerIdentityString());

		myLogger = theLogger;
		if (myLogger.debuggingState()) this.myPostLogger  = theLogger;
	}

}
