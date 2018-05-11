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

import java.util.Collection;

import things.common.ThingsException;

/**
 * The basic property view for read only.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 16 MAY 04
 * </pre> 
 */
public interface ThingsPropertyViewReader extends ThingsProperty {

	/**
	 * Branch the view to create a new view.  The new view will have the path as its root.
	 * @param path the property path (relative to the view root)
	 * @return The new view.
	 * @throws things.common.ThingsException 
	 */	
	public ThingsPropertyViewReader branch(String path) throws ThingsException;
	
	/**
	 * Get a property value as a string.  It will return null if the
	 * property is not set.  If it is a multivalue, it will return it encoded to a single String.
	 * @param path the property name
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 */
	public String getProperty(String path) throws ThingsException;
	
	/**
	 * Get a property value as a string.  It will return null if the
	 * property is not set.  If it is a multivalue, it will return it encoded to a single String.
	 * @param pathElements a stitch-able path.
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 */
	public String getProperty(String... pathElements) throws ThingsException;
	
	/**
	 * Get a property value as a multivalue.  It will return null if the
	 * property is not set.
	 * @param path the property name
	 * @return value A array of the values.
	 * @throws things.common.ThingsException 
	 */
	public String[] getPropertyMultivalue(String path) throws ThingsException;
	
	/**
	 * Get a property value as a multivalue.  It will return null if the
	 * property is not set.
	 * @param path the property name
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */
	public NVImmutable getPropertyNV(String path) throws ThingsException;
	
	/**
	 * Get all property names under this at this path.
	 * @param path a root path.  If it is null, it'll return everything.
	 * @return a collection of strings
	 * @throws things.common.ThingsException 
	 */
	public Collection<String> sub(String path) throws ThingsException;
	
	/**
	 * Get the ply at this path.  It'll return all the node names at this path but no more (no sub properties).
	 * @param path a root path.
	 * @return a collection of strings
	 * @throws things.common.ThingsException 
	 */
	public Collection<String> ply(String path) throws ThingsException;

}
