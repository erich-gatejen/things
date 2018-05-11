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
package things.thinger.io.conduits;

import things.data.Data;
import things.data.ReceiptList;
import things.thinger.SystemException;

/**
 * A conduit injector interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public interface Injector {

    /**
     * Initialize the Injector.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this injector.
     * @param theType the type of controller.
	 * @see things.thinger.io.conduits.Conduit
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId, Conduit.InjectorType  theType) throws SystemException;
    
	/**
	 * Post an item.
	 * @throws things.thinger.SystemException
	 * @return a receipt list
	 * @see things.data.ReceiptList
	 */
	public ReceiptList post(Data		item) throws SystemException;
	
	/**
	 * Get the injector type.
	 * @return the type 
	 * @see things.thinger.io.conduits.Conduit
	 */
	public Conduit.InjectorType getMyType();
		
}
