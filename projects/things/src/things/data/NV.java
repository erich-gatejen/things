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

/**
 * This is a name/value pair as strings.
 * <p>
 * A value can be more than one item.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 22 MAY 042
 * </pre> 
 */
public class NV extends NVImmutable {

	final static long serialVersionUID = 1;
	
    /**
     * Setting constructor. 
     * @param n the name 
     * @param v the value
     */	
	public NV(String n, String v) {
		super(n,v);
	}
	
    /**
     * Setting constructor. 
     * @param n the name 
     * @param v the value
     */	
	public NV(String n, String... v) {
		super(n,v);
	}
    
    /**
     * Set the name.
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Set the value.
     * @param v the value.
     */	
	public void setValue(String v) {
	    value = new String[1];
	    value[0] = v;
	}
	
    /**
     * Set the values.
     * @param v The values.
     */	
	public void setValues(String... v) {
	    value = v;
	}
    
}
