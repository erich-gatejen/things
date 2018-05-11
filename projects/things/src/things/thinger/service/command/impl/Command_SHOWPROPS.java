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

import java.util.Collection;

import things.common.ThingsException;
import things.data.NVImmutable;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.io.Logger;
import things.thinger.service.command.Command;

/**
 * An implemented command.  Show user global props..
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JAN 07
 * </pre> 
 */
public class Command_SHOWPROPS extends Command {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	final static long serialVersionUID = 1;
	private final static int FLUSH_INTERVAL = 10;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == I/O NAMES
	public final static String NAME = "things.showprops";
	public final static String RESPONSE_HEADER = "header";
	public final static String RESPONSE_ENTRY = "entry";
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT IMPLEMENTATION
	
	/**
	 * Command declaration.  Do not call this directly!
	 * @throws things.thinger.SystemException
	 */
	public void declare() throws SystemException{
		DECLARE_NAME(NAME, this.getClass().getName());
		DECLARE_RESPONSE(RESPONSE_HEADER, 	Requirement.REQUIRED, 	Occurrence.ONLYONE, DataType.LIST	);
		DECLARE_RESPONSE(RESPONSE_ENTRY, 	Requirement.OPTIONAL, 	Occurrence.MANY, 	DataType.LIST	);
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
		
			// Get the view.
			ThingsPropertyView propView = GET_SYSTEM_INTERFACE().requestSuperSystemInterface().getUserGlobalProperties();
			Collection<String> keys = propView.sub(null);
	
			// Put the header.
			RESPOND(RESPONSE_HEADER, "Name", "Value");
			
			// Set up response - flush at the interval.
			int flushThreshold = FLUSH_INTERVAL;

			for (String item : keys) {
				NVImmutable value = propView.getPropertyNV(item);
				if (value != null) {
					RESPOND(RESPONSE_ENTRY,value.getFlat());		
					flushThreshold--;
					if (flushThreshold == 0) {
						FLUSH();
						flushThreshold = FLUSH_INTERVAL;
					}
					
				} else {
					// I don't really know if this will ever be a problem.  I stumbled on this, but it ended up being another bug causing it.
					// Yes, I know the getSystemLogger() could get called a bunch...
					Logger l = GET_SYSTEM_INTERFACE().getSystemLogger();
					if (l.debuggingState()) l.debug("Command_SHOWPROPS.accept() encountered a null property.  item=" + item);
				}
			}

			// Flush
			if (flushThreshold != FLUSH_INTERVAL) FLUSH();
					
		} catch (ThingsException te) {
			throw new SystemException(te.getMessage(),te.numeric,te);
		}
		
		// Done
		DONE();
		
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ORGANIC
	
	
}
