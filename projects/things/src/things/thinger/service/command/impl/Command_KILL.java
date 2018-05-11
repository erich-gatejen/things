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
package things.thinger.service.command.impl;

import things.common.ThingsException;
import things.data.NVImmutable;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.kernel.ProcessInterface;
import things.thinger.service.command.Command;

/**
 * An implemented command.  This will kill a process.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JUN 07
 * </pre> 
 */
public class Command_KILL extends Command {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	final static long serialVersionUID = 1;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == I/O NAMES
	public final static String NAME = "things.run";
	public final static String PARAMETER_PID = "pid";

	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Command declaration.  Do not call this directly!
	 * @throws things.thinger.SystemException
	 */
	public void declare() throws SystemException{
		DECLARE_NAME(NAME, this.getClass().getName());
		DECLARE_PARAMETER(PARAMETER_PID,	Requirement.REQUIRED,	Occurrence.ONLYONE, DataType.VALUE	);
	}
	
	/**
	 * Return the official name of this command.  If there is another command named the same of  different class signature, it will cause
	 * a significant system fault.
	 * @return The official name of the command.
	 */
	public String named() {
		return NAME;
	}
	
	/**
	 * This will be called when the command is called.
	 * @throws things.thinger.SystemException
	 */
	public void accept() throws SystemException {
		
		try {
		
			// Get the parameter
			NVImmutable theThing = GET_PARAMETER(PARAMETER_PID);
			
			// Get the system interface and process interface
			SystemInterface si = GET_SYSTEM_INTERFACE();
			ProcessInterface pi = si.getProcessInterface(theThing.getValue());
			pi.requestHalt();
	
		} catch (ThingsException te) {
			throw new SystemException("Could not stop process", SystemException.SYSTEM_COMMAND_ERROR_OPERATION_FAILED, te);
					
		// } catch (ThingsException te) {
		//	throw new SystemException(te.getMessage(),te.numeric,te);
		}
		
		// Done
		DONE();
		
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ORGANIC
	
	
}
