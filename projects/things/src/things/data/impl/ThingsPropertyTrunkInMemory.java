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
package things.data.impl;

import things.common.ThingsException;
import things.data.Accessor;
import things.data.NV;
import things.data.NVImmutable;
import things.data.ThingsPropertyTrunk;

/**
 * An in-memory implementation for a property trunk.  Basically, it's just a null implementation.  
 * The ID can be meaningless.<p>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 05
 * </pre> 
 */
public class ThingsPropertyTrunkInMemory implements ThingsPropertyTrunk {
        
	/**
	 * Get the current Mode.
	 * @return the Mode.
	 */
	public Mode getMode() {
		return Mode.IDLE;
	}

	/**
	 * Sets the  ID.  It's really up to the implementation as to what the ID means.  It may be ignored and all will be transfered.
	 * @param id An id
	 * @param accessItem A way to read and write the data.
	 * @throws things.common.ThingsException 
	 */ 	
	public void init(String  id, Accessor  accessItem) throws ThingsException {
		// Ignore it.
	}
	
	/**
	 * Start a read.
	 * @throws things.common.ThingsException 
	 */ 	
	public void startRead() throws ThingsException {
		// Don't care!
	}
	
	/**
	 * End a read transfer.  If a read hasn't started, nothing bad will happen.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endRead() throws ThingsException {
		// Don't care!
	}
	
	/**
	 * Start a write transfer.
	 * @throws things.common.ThingsException 
	 */ 	
	public void startWrite() throws ThingsException {
	    // Ignore it
	}
	
	/**
	 * End a wrote transfer.  If a write hasn't started, nothing bad will happen.
	 * It should flush and close the destination.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endWrite() throws ThingsException {
	    // Ignore it	    
	}
	
	/**
	 * Write the next property
	 * @param name The property name as a string
	 * @param value The property value as a string
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNext(String name, String value) throws ThingsException {
	    // Ignore it
	}
	
	/**
	 * Write the next property
	 * @param item the next property as an NVImmutable.
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */ 	
	public void writeNext(NVImmutable item) throws ThingsException {
		// Ignore it.
	}
	
	/**
	 * Write the next property that is a multivalue.
	 * @param name The property name as a string
	 * @param values The property values
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNextMultivalue(String name, String... values) throws ThingsException {
		// Ignore it
	}
	
	/**
	 * Read the next property.  It will return null if there are non left.
	 * @return NV
	 * @throws things.common.ThingsException 
	 */ 	
	public NV readNext() throws ThingsException {
		return null;
	}	
}
