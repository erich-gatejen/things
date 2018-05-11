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
package things.data;

import things.common.ThingsException;

/**
 * A two way channel for getting or putting properties.  Useful for load and save.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 02
 * </pre> 
 */
public interface ThingsPropertyTrunk {
	
	/**
	 * The mode for this trunk.
	 */
	public enum Mode { IDLE, LOADING, SAVING };
	
	/**
	 * Get the current Mode.
	 * @return the Mode.
	 */
	public Mode getMode();
	
	/**
	 * Sets the  ID.  It's really up to the implementation as to what the ID means.  It may be ignored and all will be transfered.
	 * @param id An id
	 * @param accessItem A way to read and write the data.
	 * @throws things.common.ThingsException 
	 */ 	
	public void init(String  id, Accessor  accessItem) throws ThingsException;
	
	/**
	 * Start a read.
	 * @throws things.common.ThingsException 
	 */ 	
	public void startRead() throws ThingsException;
	
	/**
	 * End a read transfer.  If a read hasn't started, nothing bad will happen.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endRead() throws ThingsException;
	
	/**
	 * Start a write transfer.
	 * @throws things.common.ThingsException 
	 */ 	
	public void startWrite() throws ThingsException;
	
	/**
	 * End a wrote transfer.  If a write hasn't started, nothing bad will happen.
	 * It should flush and close the destination.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endWrite() throws ThingsException;
	
	/**
	 * Write the next property
	 * @param name The property name as a string
	 * @param value The property value as a string
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNext(String name, String value) throws ThingsException;
	
	/**
	 * Write the next property
	 * @param item the next property as an NVImmutable.
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */ 	
	public void writeNext(NVImmutable item) throws ThingsException;
	
	/**
	 * Write the next property that is a multivalue.
	 * @param name The property name as a string
	 * @param values The property values
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNextMultivalue(String name, String... values) throws ThingsException;
	
	/**
	 * Read the next property.  Returns null when there is nothing left to read.
	 * @throws things.common.ThingsException 
	 * @return NV or null when done.
	 */ 	
	public NV readNext() throws ThingsException;
	
}
