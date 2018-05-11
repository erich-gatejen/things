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

/**
 * This defines something that has attributes but can only be read.
 * <p>
 * @author Erich P. Gatejen<br>
 * @version 1.0<br>
 * <i>Version History</i><br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 25 APR 05
 * </pre> 
 */
public interface AttributeReader {

	/**
	 * Ask if multi-value is allowed.
	 * @return true if it is allowed.
	 */
	public boolean isMultivalueAllowed();
	
	/**
	 * Get an attribute.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @return the value or null if not set.
	 */	
	public NVImmutable getAttribute(String n);
	
	/**
	 * Get an attribute value rendered as a string.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @return the value or null if not set.
	 */	
	public String getAttributeValueToString(String n);
	
	/**
	 * Get an attribute value rendered as a string.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @param defaultValue the value it will return if the name maps to a null.
	 * @return the value or null if not set.
	 */	
	public String getAttributeValueToString(String n, String defaultValue);
	
	/**
	 * Check if it has an attribute.
	 * @param n name of the attribute
	 * @return true if it has the attribute, otherwise false.
	 */	
	public boolean hasAttribute(String n);
	
	/**
	 * Get a collection of the attributes.
	 * @return The collection.
	 */
	public Collection<NVImmutable> getAttributes();
	
	/**
	 * Get a collection of the attributes for the name.
	 * @param n name of the attribute
	 * @return The collection.
	 */
	public Collection<NVImmutable> getAttributes(String n);
	
	/**
	 * Get an attribute from a multi-attribute by index.  This will work with single value items, as long as the index is zero.  This is
	 * not a fast operation, so don't use it in time critical areas.  It will always return null rather than throw an exception.
	 * @param n name of the attribute
	 * @param index the index of the attribute from 0.
	 * @return The NV or a null if it doesn't exist.
	 */
	public NVImmutable getAttribute(String n, int index);
	
	/**
	 * Get a collection of attribute names. 
	 * @return The collection.
	 */	
	public Collection<String> getAttributeNames();

	/**
	 * Get the total attribute count.
	 * @return count, zero or greater
	 */
	public int getAttributeCount();
	
	/**
	 * Get the total attribute count for a specific name.  It will return 0 if the name is not found.
	 * @param n The name of the attribute.
	 * @return count, zero or greater
	 */
	public int getAttributeCount(String n);
	
	/**
	 * Get a private set that can be manipulated.  This will make a copy and will not affect the original.
	 * @return the HashSet of attributes.
	 */
	public Attributes getPrivateSet();
	
}
