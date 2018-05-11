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
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.service.command.Command;

/**
 * Set a user global property.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JAN 07
 * </pre> 
 */
public class Command_SETPROP extends Command {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	final static long serialVersionUID = 1;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == I/O NAMES
	public final static String NAME = "things.setprop";
	public final static String PARAMETER_NAME = "name";
	public final static String PARAMETER_VALUE = "value";

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Command declaration.  Do not call this directly!
	 * @throws things.thinger.SystemException
	 */
	public void declare() throws SystemException{
		DECLARE_NAME(NAME, this.getClass().getName());
		DECLARE_PARAMETER(PARAMETER_NAME,	Requirement.REQUIRED,	Occurrence.ONLYONE, DataType.VALUE	);	
		DECLARE_PARAMETER(PARAMETER_VALUE,	Requirement.REQUIRED,	Occurrence.ONLYONE, DataType.VALUE	);	
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
		
		// Get info
		NVImmutable theName = GET_PARAMETER(PARAMETER_NAME);
		if (theName==null) throw new SystemException("Could not get required parameter.", SystemException.SYSTEM_COMMAND_FAULT_PARAMETER_MISSING_AFTER_TRANSMISSION, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, NAME);
		String theNameString = theName.getValue();
		NVImmutable theValue = GET_PARAMETER(PARAMETER_VALUE);

		// Put it
		try {
			ThingsPropertyView propView = GET_SYSTEM_INTERFACE().requestSuperSystemInterface().getUserGlobalProperties();
			if (theValue.isMultivalue()) {
				propView.setPropertyMultivalue(theNameString, theValue.getValues());
			} else {
				propView.setPropertyMultivalue(theNameString, theValue.getValue());			
			}
		} catch (SystemException se) {
			throw new SystemException("Could not set property.", SystemException.SYSTEM_COMMAND_ERROR_PROPERTY_PROBLEM, se, SystemNamespace.ATTR_PROPERTY_NAME, theNameString, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, NAME);
		} catch (ThingsException te) {
			throw new SystemException("Could not set property.", SystemException.SYSTEM_COMMAND_ERROR_PROPERTY_PROBLEM, te, SystemNamespace.ATTR_PROPERTY_NAME, theNameString, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, NAME);			
		}
		
		// Done
		DONE();
		
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ORGANIC
	
	
	
}
