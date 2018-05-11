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

package things.thinger.kernel.basic;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.thing.RESULT;
import things.thinger.ExpressionInterface;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;

/**
 * This implements a logging based expressor.  This doesn't really belong here. 
 * <p>
 * This might a tinge slow, since we are asking the SI for the calling process ID.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 JUN 07
 * </pre> 
 */
public class KernalBasic_LoggingExpressor implements ExpressionInterface {

	// ==========================================================================================================
	// == DATA
	private Logger myLogger;
	private SystemInterface si;
	
	// ==========================================================================================================
	// == METHODS
	
	/**
	 * Constructor.
	 * @param logger the logger to which we express.
	 * @param si the system interface.
	 * @throws ThingsException if either parameter is null.
	 */
	public KernalBasic_LoggingExpressor(Logger logger, SystemInterface si) throws ThingsException {
		if (logger==null) ThingsException.softwareProblem("KernalBasic_LoggingExpressor cannot be created with null logger.");
		if (si==null) ThingsException.softwareProblem("KernalBasic_LoggingExpressor cannot be created with null si.");
		myLogger = logger;
		this.si = si;
	}
	
	// ==========================================================================================================
	// == DEFAULT EXPRESSION INTERFACE IMPLEMENTATION
	
	/**
	 * Express a RESULT.  Reliability is up to the kernel; you will not get a receipt.
	 * @param theResult The result to express.
	 * @throws SystemException
	 * @see things.thing.RESULT
	 */
	public void expressResult(RESULT theResult) throws SystemException {
		try {
			// Find out who we are.	
			myLogger.shout("RESULT", ThingsCodes.USER_RESULT_DEFAULT, Logger.LEVEL.DATA,
				 theResult.getAllAttributes(SystemNamespace.ATTR_PROCESS_ID, si.getCallingProcessId().toString()));		
			myLogger.flush();

		} catch (ThingsException te) {
			throw new SystemException("Failed THING expression.", ThingsException.ERROR_THING_EXPRESSION_DEFAULT_ERROR, te);
		}
	}
	
}
