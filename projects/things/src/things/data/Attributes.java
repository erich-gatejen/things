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

import java.io.Serializable;

import things.common.ThingsException;

/**
 * This defines something that has attributes and read access only.
 * <p>
 * <h2>IMPORTANT NOTE: THIS SUCKS</h2>
 * This turned out to be a gigantic pain in the ass.  We should have stuck with
 * properties for everything.  Oh, the irony.  This came over from another project 
 * to support the error management.  It should have been stopped at the door.  It 
 * created two completely different ways to handle name/value pairs and a huge pile of
 * crap code to transfer bits back and forth.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project. - 25 APR 05
 * </pre> 
 */
public interface Attributes extends AttributeReader, Serializable {
	
	/**
	 * Allow multi-attributes with the same name.
	 */
	public void allowMulti();
	
	/**
	 * Disallow multi-attributes with the same name.  If the attribute set already has multi-attributes attributes, it should trim the attributes to 
	 * a single value.  The interface does not define how this will happen.
	 * <br>
	 * The implementation may choose to ignore this state and merely trim the duplicates whne this method is called.
	 */
	public void disallowMulti();
	
	/**
	 * Add an attribute in the native NV.  If attribute is already set, it will overwrite it.  The NV value may have multi-values.
	 * @param attribute the attribute
	 * @see things.data.NV
	 */	
	public void addAttribute(NVImmutable		attribute) throws ThingsException;
		
	/**
	 * Add an attribute--single name to single value.  If attribute is already set, it will overwrite it.

	 * @param v value of the attribute
	 */	
	public void addAttribute(String n, String v) throws ThingsException;
	
	/**
	 * Add all the attributes that can be read from the reader.
	 * @param reader a reader
	 * @throws ThingsException
	 */	
	public void addAttribute(AttributeReader reader) throws ThingsException;
	
	/**
	 * Add multiple attributes--single name to single value.  If attribute is already set, it will overwrite it.
	 * @param attributes The attributes in pairs.
	 * @throws things.common.ThingsException
	 */
	public void addMultiAttributes (String... attributes) throws ThingsException;
	
	/**
	 * Add multiple attributes--single name to single value.  If attribute is already set, it will overwrite it.
	 * The first attribute name will come from the name parameter.  Then the first attribute value will head the attribute parameter.
	 * All following will be name/value pairs.
	 * @param attributes The attributes in pairs, except the first which will be the value pairing with the parameter name.
	 * @throws things.common.ThingsException
	 */
	public void addMultiAttributes (String   name, String... attributes) throws ThingsException;

	/**
	 * Remove the named attribute.  If the attribute existed, it will be deleted (along with multi-values), it will return true.  If it does not exist, it will return false.  
	 * @param n name of the attribute
	 * @throws things.common.ThingsException
	 */
	public boolean removeAttribute (String n) throws ThingsException;
	
	/**
	 * Get the underlying attribute data.  This can be dangerous!  The data is alterable.  We will protect it some by passing it as an Object.
	 * @return An object for the underlying data structure.
	 */
	public Object get();
	
	/**
	 * Get the underlying attribute data.  This can be dangerous!  The data is alterable.  We will protect it some by passing it as an Object.  If it is not
	 * the proper type, it will throw a ThingsException.
	 * @throws things.common.ThingsException
	 */
	public void set(Object o) throws ThingsException;
	
}
