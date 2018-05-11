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

import things.data.NVImmutable;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.service.command.Command;

/**
 * An implemented command.  This will run a THING.
 * <br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 07
 * </pre> 
 */
public class Command_RUN extends Command {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	final static long serialVersionUID = 1;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == I/O NAMES
	public final static String NAME = "things.run";
	public final static String PARAMETER_NAME = "name";
	public final static String RESPONSE_ID = "id";
	public final static String RESPONSE_TAG = "tag";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Command declaration.  Do not call this directly!
	 * @throws things.thinger.SystemException
	 */
	public void declare() throws SystemException{
		DECLARE_NAME(NAME, this.getClass().getName());
		DECLARE_RESPONSE(RESPONSE_ID, 	Requirement.REQUIRED, 	Occurrence.ONLYONE, DataType.VALUE	);
		DECLARE_RESPONSE(RESPONSE_TAG, 	Requirement.REQUIRED, 	Occurrence.ONLYONE, DataType.VALUE	);
		DECLARE_PARAMETER(PARAMETER_NAME,	Requirement.REQUIRED,	Occurrence.ONLYONE, DataType.VALUE	);
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
			NVImmutable theThing = GET_PARAMETER(PARAMETER_NAME);
			
			// Get the system interface
			SystemInterface si = GET_SYSTEM_INTERFACE();
			String result = si.runThing(theThing.getValue());		// runThing yields the process id.

			// Get the process ID and thus the tag
			RESPOND(RESPONSE_TAG, si.getProcessInterface(result).getProcessId().toTag());		
				
			// Set response
			RESPOND(RESPONSE_ID, result);
	
		} catch (SystemException se) {
			throw se;
					
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
