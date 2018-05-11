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

import java.util.LinkedList;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.AttributeReader;
import things.data.Attributes;
import things.data.NV;
import things.data.NVImmutable;

/**
 * A set of writable attributes.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 23 AUG 05
 * </pre> 
 */
public class ReadWriteableAttributes extends ReadableAttributes implements Attributes {

	final static long serialVersionUID = 1;
	 
	// ===============================================================================================================================
	// == CONSTRUCTORS
	
	// ===============================================================================================================================
	public ReadWriteableAttributes() {
		super();
	}
	
	// == ATTRIBUTOR INTERFACE
	
	/**
	 * Add an attribute in the native NV.  If attribute is already set, it will overwrite it.  The NV value may have multi-values.
	 * @param attribute the attribute
	 * @throws ThingsException
	 * @see NVImmutable
	 */	
	public void addAttribute(NVImmutable		attribute) throws ThingsException {
		
		// Create attributes only if appropriate.
		validateAttributes();
		
		// Protect it.  Only add if it is good.
		if ( (attribute!=null) && (attribute.isValid()) ) {
			if (mYAttributes.containsKey(attribute.getName())) {
				// Has entry, so use it.
				mYAttributes.get(attribute.getName()).add(attribute);

			} else {
				// Does not have entry, so create and add it.
				LinkedList<NVImmutable> item = new LinkedList<NVImmutable>();
				item.add(attribute);
				mYAttributes.put(attribute.getName(), item);
			}

		} else {
			throw new ThingsException("Attempted to add a null'd attribute.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		}
	}

	/**
	 * Add an attribute--single name to single value.  If attribute is already set, it will overwrite it.
	 * @param n name of the attribute
	 * @param v value of the attribute
	 */	
	public void addAttribute(String n, String v) throws ThingsException {

		// Create attributes only if appropriate.
		addAttribute(new NV(n,v));
	}
	
	/**
	 * Add all the attributes that can be read from the reader.
	 * @param reader a reader
	 * @throws ThingsException
	 */	
	public void addAttribute(AttributeReader reader) throws ThingsException {
		for (NVImmutable attrib : reader.getAttributes()) {
			addAttribute(attrib);
		}
	}
	
	/**
	 * Add multiple attributes--single name to single value.  If attribute is already set, it will overwrite it.
	 * @param attributes The attributes in pairs.
	 * @throws things.common.ThingsException
	 */
	public void addMultiAttributes (String... attributes) throws ThingsException {
		
		// Validate the attributes.  Don't accept null or odd numbered.
		if (attributes==null) throw new ThingsException("Attempted to add a null'd attribute.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		if (attributes.length%2 == 1) throw new ThingsException("Odd number of name/attributes Strings.", ThingsException.SYSTEM_INFRA_BAD_DATA_ODD);
	
		// Run 'em
		int rover = 0;
		while(rover < attributes.length) {
			
			// Validate it.
			if (attributes[rover]==null) throw new ThingsException("Attempted to add a null'd attribute name.", ThingsException.SYSTEM_INFRA_NULLED_DATA, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(rover));
			if (attributes[rover+1]==null) {
				throw new ThingsException("Attempted to add a null'd attribute value.", ThingsException.SYSTEM_INFRA_NULLED_DATA, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(rover+1), ThingsNamespace.ATTR_DATA_ATTRIBUTE_NAME, attributes[rover]);
			}
			
			// Put it and move on.
			addAttribute(new NV(attributes[rover], attributes[rover+1]));
			rover = rover + 2;
		}
	}
	
	/**
	 * Add multiple attributes--single name to single value.  If attribute is already set, it will overwrite it.
	 * The first attribute name will come from the name parameter.  Then the first attribute value will head the attribute parameter.
	 * All following will be name/value pairs.
	 * @param name The name
	 * @param attributes The attributes in pairs, except the first which will be the value pairing with the parameter name.
	 * @throws things.common.ThingsException
	 */
	public void addMultiAttributes (String   name, String... attributes) throws ThingsException {
		
		// Validate the attributes.  Don't accept null or odd numbered.
		if ((attributes==null)||(attributes.length<1)) throw new ThingsException("Attempted to add a null'd attribute.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		if (name==null) throw new ThingsException("Attempted to add a null'd attribute name.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		if (attributes.length%2 != 1) throw new ThingsException("Odd number of name/attributes Strings.", ThingsException.SYSTEM_INFRA_BAD_DATA_ODD);

		// Do the head
		addAttribute(new NV(name, attributes[0]));
		
		// Run 'em
		int rover = 1;
		while(rover < attributes.length) {
			
			// Validate it.
			if (attributes[rover]==null) throw new ThingsException("Attempted to add a null'd attribute name.", ThingsException.SYSTEM_INFRA_NULLED_DATA, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(rover));
			if (attributes[rover+1]==null) throw new ThingsException("Attempted to add a null'd attribute value.", ThingsException.SYSTEM_INFRA_NULLED_DATA, ThingsNamespace.ATTR_DATA_INDEX, Integer.toString(rover+1), ThingsNamespace.ATTR_DATA_ATTRIBUTE_NAME, attributes[rover]);
			
			// Put it and move on.
			addAttribute(new NV(attributes[rover], attributes[rover+1]));
			rover = rover + 2;
		}	
	}

	/**
	 * Remove the named attribute.  If the attribute existed, it will be deleted (along with multi-values), it will return true.  If it does not exist, it will return false.  
	 * @param n name of the attribute
	 * @throws things.common.ThingsException
	 */
	public boolean removeAttribute (String n) throws ThingsException {
		
		// Validate the name.
		if (n==null) throw new ThingsException("Attempted to remove a null'd attribute name.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		
		boolean result = false;
		
		// Is it there?
		if ((mYAttributes!=null)&&(mYAttributes.containsKey(n))) {
			result = true;
			mYAttributes.remove(n);
		}
		
		return result;
	}
	
	/**
	 * Allow multi-attributes with the same name.
	 * This is ignored.
	 */
	public void allowMulti() {
		// Ignore.
	}
	
	/**
	 * Disallow multi-attributes with the same name.  If the attribute set already has multi-attributes attributes, it should trim the attributes to 
	 * a single value.  The interface does not define how this will happen.
	 * <br>
	 * This is ignored.
	 * <br>
	 * The implementation may choose to ignore this state and merely trim the duplicates whne this method is called.
	 */
	public void disallowMulti() {
		// ignore
	}
	
	// ===============================================================================================================================
	// == SPECIAL
	
	/**
	 * This is a static attribute object that will forever be empty.
	 */
	public static final ReadableAttributes permanentlyEmptyAttribute = new ReadWriteableAttributes();
}
