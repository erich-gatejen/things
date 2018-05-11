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
import java.util.Arrays;
import java.util.List;

import things.common.ThingsException;

/**
 * This is a name/value pair as strings.
 * <p>
 * A value can be more than one item.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 22 MAY 04
 * </pre> 
 */
public class NVImmutable implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * The Name.
	 */
	protected String name;

	/**
	 * The Values
	 */
	protected String[] value;
	
    /**
     * Setting constructor with no value.  The value will be the same as the name. 
     * @param n the name 
     */	
	public NVImmutable(String n) {
	    name = n;
	    value = new String[1];
	    value[0] = n;
	}
	
    /**
     * Setting constructor. 
     * @param n the name 
     * @param v the value
     */	
	public NVImmutable(String n, String v) {
	    name = n;
	    value = new String[1];
	    value[0] = v;
	}
	
    /**
     * Setting constructor. 
     * @param n the name 
     * @param v the value
     */	
	public NVImmutable(String n, String... v) {
	    name = n;
	    value = v;
	}
	
    /**
     * Get the name.
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
       
    /**
     * Get the first (and perhaps only) value.  If the value is null, it will return null.
     * @return The value.
     */
    public String getValue() {
    	if (value==null) return null;
        return value[0];
    }
    
    /**
     * Get flat.  Name and values in a singel array.
     * @return The name and values  (the name is first).
     */
    public String[] getFlat() {
    	String[] thang = new String[value.length+1];
    	thang[0] = name;
    	for (int index = 0; index < value.length; index++) {
    		thang[index+1] = value[index];
    	}
        return thang;
    }
    
    /**
     * Get all the values as an array.
     * @return An array of values.
     */
    public String[] getValues() {
        return value;
    }
    
    /**
     * Get all the values as a List.
     * @return a List of the contents.
     */
    public List<String> getValuesAsList() {
        return Arrays.asList(value);
    }
    	
    /**
     * Is multivalue.
     * @return true if it has more than one item for the value, otherwise false.
     */	
	public boolean  isMultivalue() {
	    if (value.length>1) return true;
	    return false;
	}
	
	/**
	 * Validate.  It will make sure the fields are not null.
	 * @return true if valid, otherwise false.
	 */
	public boolean isValid() {
		if ((name==null)||(value==null)||(value[0]==null)) return false;
		return true;
	}
	
	/**
	 * Returns a string representation of the object. In general, the toString method returns a string that "textually represents" this object.
	 * @return the textual representation.
	 */
	public String toString() {
		try {
			return AttributeCodec.encode2String(this);
		} catch (Throwable t) {
			return "Failed to encode NV : " + ThingsException.toStringCauses(t);
		}
	}
	
}
