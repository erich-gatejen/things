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
 * The basic property view.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 16 MAY 04
 * </pre> 
 */
public interface ThingsPropertyView extends ThingsPropertyViewReader {
	
	/**
	 * Set a property value as a string.  The property will be from the root
	 * of the view.
	 * @param path the property path (relative to the view root)
	 * @param value the property value as a string
	 * @throws things.common.ThingsException 
	 */	
	public void setProperty(String path, String value) throws ThingsException;
	
	/**
	 * Set a property value as a string.  The property will be from the root
	 * of the view.  If the value is null, it will set it as the defaultValue instead.
	 * @param path the property path (relative to the view root)
	 * @param value the property value as a string
	 * @param defaultValue the default value.
	 * @throws things.common.ThingsException 
	 */	
	public void setProperty(String path, String value, String defaultValue) throws ThingsException;
	
	/**
	 * Set a property value from an NVImmutable item.  The property will be from the root
	 * of the view.
	 * @param item the item
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */	
	public void setProperty(NVImmutable  item) throws ThingsException;
	
	/**
	 * Set a property value as a multivalue.  The property will be from the root
	 * of the view.
	 * @param path the property path (relative to the view root)
	 * @param values the property values as Strings.
	 * @throws things.common.ThingsException 
	 */	
	public void setPropertyMultivalue(String path, String... values) throws ThingsException;
	
	/**
	 * Remove a specific property without subverting a tree.
	 * @param path the property path (relative to the view root).  A null or bogus path will be ignored.
	 * @throws things.common.ThingsException 
	 */	
	public void removeProperty(String path) throws ThingsException;
	
	/**
	 * A cutting to get a new a new view.  The new view will have the path as its root.
	 * @param path the property path (relative to the view root).  A null is not allowed.
	 * @return The new view.
	 * @throws things.common.ThingsException 
	 */	
	public ThingsPropertyView cutting(String path) throws ThingsException;
	
	/**
	 * Prune off the path.  Properties will be removed and gone forever.
	 * @param path the property path (relative to the view root).  A null is not allowed.
	 * @throws things.common.ThingsException 
	 */	
	public void prune(String path) throws ThingsException;

	/**
	 * Graft one view onto this view.  All properties will be added.  The values will be copies, so the original tree will
	 * be left unaltered.
	 * @param sourceView view to graft on.
	 * @throws things.common.ThingsException 
	 */	
	public void graft(ThingsPropertyView sourceView) throws ThingsException;
	
	/**
	 * Ask the underlying tree to save itself.  NO GUARANTEE the implementation actually will.
	 * @throws things.common.ThingsException 
	 */ 
	public void save() throws ThingsException;
	

}
