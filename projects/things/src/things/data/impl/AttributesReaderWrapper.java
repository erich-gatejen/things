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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import things.data.AttributeReader;
import things.data.Attributes;
import things.data.NV;
import things.data.NVImmutable;

/**
 * A set of readable attributes from a simple hashmap.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 23 AUG 05
 * </pre> 
 */
public class AttributesReaderWrapper implements AttributeReader  {

	final static long serialVersionUID = 1;
	
	/**
	 * Attributes, if needed.
	 */
	protected HashMap<String, String> mYAttributes = null;
	
	/**
	 * Constructor.
	 */
	public AttributesReaderWrapper(HashMap<String,String> attributes) {
		if (attributes==null) {
			mYAttributes = new HashMap<String,String>();
		} else {
			mYAttributes = attributes;
		}
		
	}
	
	// ===============================================================================================================================
	// ==  ATTRIBUTE READER INTERFACE
	
	/**
	 * Ask if multi-value is allowed.
	 * @return true if it is allowed.
	 */
	public boolean isMultivalueAllowed() {
		// It is always ok.
		return true;
	}
	
	/**
	 * Get an attribute.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @return the value or null if not set.
	 */	
	public NVImmutable getAttribute(String n) {
		NVImmutable result = null;
		
		if ( (mYAttributes != null) && (n != null) && (mYAttributes.containsKey(n)) ) {
			 result = new NV(n, mYAttributes.get(n));
		}
	    return result;
	}
	
	/**
	 * Get an attribute value rendered as a string.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @return the value or null if not set.
	 */	
	public String getAttributeValueToString(String n) {
	    return getAttributeValueToString(n, null);
	}
	
	/**
	 * Get an attribute value rendered as a string.  If the name of the attribute is null or the attribute does not exist, it should return null. 
	 * If it is a multi, it will return the first.
	 * @param n name of the attribute
	 * @param defaultValue the value it will return if the name maps to a null.
	 * @return the value or null if not set.
	 */	
	public String getAttributeValueToString(String n, String defaultValue) {
		String result = defaultValue;
		try {
			if ( (mYAttributes != null) && (n != null) && (mYAttributes.containsKey(n)) ) {
				 result = mYAttributes.get(n);
			}
		} catch (Throwable t) {
			// Let it stay the default.
		}
	    return result;
	}
	
	/**
	 * Check if it has an attribute.  It will return false if the name is null.
	 * @param n name of the attribute
	 * @return true if it has the attribute, otherwise false.
	 */	
	public boolean hasAttribute(String n) {
		boolean result = false;
		if ( (mYAttributes != null) && (n != null) ) {
			result = mYAttributes.containsKey(n);
		}	
		return result;
	}
	
	/**
	 * Get a collection of the attributes for the name.
	 * @param n name of the attribute
	 * @return The collection.
	 */
	public Collection<NVImmutable> getAttributes(String n) {
		LinkedList<NVImmutable> result = new LinkedList<NVImmutable>();
		
		if ( (mYAttributes != null) && (n != null) && (mYAttributes.containsKey(n)) ) {
			result.add( new NV(n, mYAttributes.get(n)) );
		} 

		return result;
	}
	
	
	/**
	 * Get an attribute from a multi-attribute by index.  This will work with single value items, as long as the index is zero.  This is
	 * not a fast operation, so don't use it in time critical areas.  It will always return null rather than throw an exception.
	 * @param n name of the attribute
	 * @param index the index of the attribute from 0.
	 * @return The NV or a null if it doesn't exist.
	 */
	public NVImmutable getAttribute(String n, int index) {
		NVImmutable result = null;
		
		if ( (mYAttributes != null) && (n != null) && (mYAttributes.containsKey(n)) ) {
			result = new NV(n, mYAttributes.get(n));
		} 
	    return result;
	}
	
	/**
	 * Get a collection of attribute names. 
	 * @return The collection.
	 */	
	public Collection<String> getAttributeNames() {
		if (mYAttributes ==  null) return new ArrayList<String>();
		return mYAttributes.keySet();
	}
	
	/**
	 * Get a collection of the attributes.
	 * <p>
	 * This version is heavy.
	 * @return The collection
	 */
	public Collection<NVImmutable> getAttributes() {
		
		// What we will return.
		LinkedList<NVImmutable> result = new LinkedList<NVImmutable>();
		
		// If there are no attributes, return a blank iterator to an empty list, otherwise fill it
		if (mYAttributes !=  null) {		
		
			// Ok, create a collection from all the items.  This is gonna be painful.
			for (String listItem : mYAttributes.keySet()) {
				result.add( new NV(listItem, mYAttributes.get(listItem)) );
			
			} // end for list items
		
		} // end if there are attributes

		return result;
	}
	
	/**
	 * Get attribute count.
	 * @return count, zero or greater
	 */
	public int getAttributeCount() {
		if (mYAttributes ==  null) return 0;
		return mYAttributes.size();
	}
	
	/**
	 * Get the total attribute count for a specific name.  It will return 0 if the name is not found.
	 * @param n The name of the attribute.
	 * @return count, zero or greater
	 */
	public int getAttributeCount(String n){
		if ((mYAttributes ==  null)||(!mYAttributes.containsKey(n))) return 0;
		return 1;
	}
	
	/**
	 * Get a private set that can be manipulated.  This will make a copy and will not affect the original.
	 * This is a slow, heavy operation.
	 * @return the HashSet of attributes.
	 */
	public Attributes getPrivateSet() {
		
		// We'll create a mirrow Entry.
		Attributes result = new ReadWriteableAttributes();
		
		// Run the attributes, if we dare bother.
		Collection<NVImmutable> theGoods = getAttributes();
		if (theGoods.size() > 0) {
			for (NVImmutable item : theGoods) {

				// toss exceptions as dead entries
				try {
					result.addAttribute(item.getName(), item.getValue());
				} catch (Throwable t) {
					// Toss...
				}

			} // end for items
		}
		
		return result;
	}
	
}
