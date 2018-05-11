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
package things.thinger.service.local;

import things.common.ThingsException;
import things.common.tools.Tender;
import things.thinger.service.Service;

/**
 * A CLI Backbone.  I'll pull some of the other functions in here later.  Meanwhile, it just allows
 * the various CLI users to see the transaction interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 06
 * </pre> 
 */
public abstract class CLIBackbone extends Service implements CLIServiceConstants {
	
	// ===================================================================================================
	// EXPOSED DATA

	/**
	 * The tender transaction point and it's lock.
	 */
	protected final static Tender<String, String>	transactionInterface = new Tender<String, String>();
	
	// ===================================================================================================
	// INTERNAL DATA

	// ===================================================================================================
	// CONSTRUCTOR
	public CLIBackbone() {
		super();
	}
	
	// ==========================================================================================================
	// == METHODS
	
	// ===================================================================================================
	// SERVICE IMPLEMENTATION
	
	// Up to the sub classes
	
	// ==========================================================================================================
	// == RESOURCE LISTENER IMPLEMENTATIONS
	
	// Up to the sub classes
	
	// ==========================================================================================================
	// == TOOLS

	// ==========================================================================================================
	// == TRANSACTION INTERFACE - expose tender anyone.
	
	/**
	 * Tender a transaction.
	 * @param input The input object.
	 * @return the output object.
	 * @throws ThingsException
	 */
	public String tender(String input) throws ThingsException {
		return transactionInterface.tender(input);
	}
	
}
