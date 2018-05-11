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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import things.common.ThingsException;
import things.data.AttributeReader;
import things.data.Attributes;
import things.data.NVImmutable;

/**
 * A set of readable attributes.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 23 AUG 05
 * </pre> 
 */
public class ReadableAttributes implements AttributeReader, Serializable  {

	final static long serialVersionUID = 1;
	
	/**
	 * Attributes, if needed.
	 */
	protected Hashtable<String, LinkedList<NVImmutable>> mYAttributes = null;
	
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
			 result = mYAttributes.get(n).getFirst();
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
				 result = mYAttributes.get(n).getFirst().getValue();
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
		return mYAttributes.get(n);
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
			LinkedList<NVImmutable> theList = mYAttributes.get(n);
			if ((index > 0)&&(index < theList.size())) {
				return theList.get(index);
			}
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
			for (LinkedList<NVImmutable> listItem : mYAttributes.values()) {
				
				for (NVImmutable nvItem : listItem ) {
					result.add(nvItem);
				} 
				
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
		return mYAttributes.get(n).size();
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
	
	/**
	 * Get the underlying attribute data.  This can be dangerous!  The data is alterable.  We will protect it some by passing it as an Object.
	 * @return An object for the underlying data structure.
	 */
	public Object get() {
		return (Object)mYAttributes; 
	}
	
	/**
	 * Set the underlying attribute data.  This can be dangerous!  The data is alterable.  We will protect it some by passing it as an Object.  If it is not
	 * the proper type, it will throw a ThingsException.
	 * param o The object that will set.
	 * @throws things.common.ThingsException
	 */
	@SuppressWarnings("unchecked")
	public void set(Object o) throws ThingsException {
		
		// Can't allow null
		if (o==null) throw new ThingsException("Attempted to set attribute data as a null object.", ThingsException.SYSTEM_INFRA_NULLED_DATA);
		
		// Validate the table
		try {
			Hashtable<String, LinkedList<NVImmutable>> tempObject = (Hashtable<String, LinkedList<NVImmutable>>)o;
			if (tempObject.size()>0) {
				@SuppressWarnings("unused") LinkedList<NVImmutable> tempEntry = tempObject.get(tempObject.keySet().iterator().next());
			}
			mYAttributes = tempObject;
		} catch (Throwable e) {
			 throw new ThingsException("Attempted to set attributes as something other than the native data type", ThingsException.SYSTEM_FAULT_DATA_VIOLATE_NATIVE, e);
		}

	}
	
	// ===============================================================================================================================
	// ==  ATTRIBUTES HELPER
	
	/**
	 * Check to see if attributes were defined.  If not, do so.
	 */
	protected void validateAttributes() {
		if (mYAttributes==null) mYAttributes = new Hashtable<String,LinkedList<NVImmutable>>();	
	}
	
}
