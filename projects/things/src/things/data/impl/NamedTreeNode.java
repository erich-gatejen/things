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

import java.util.HashMap;
import java.util.Set;

/**
 * Tree nodes.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 SEP 02
 * </pre> 
 */
public class NamedTreeNode  {

	private HashMap<String,NamedTreeNode> table;
	private Object localValue;
	
	/**
	 * CONSTRUCTOR
	 */
	public NamedTreeNode() {
		table = new HashMap<String,NamedTreeNode>();
		localValue=null;
	}

	/**
	 * Get the value.  If it has no value, it will return null
	 * @return the terminal or null if this is a set
	 */ 
	public Object getValue() {
		return localValue;
	}	
	
	/**
	 * Set the value.  A null will clear the value
	 * @param value set the value of this node
	 */ 
	public void setValue(Object value) {
		localValue = value;
	}	

	/**
	 * Remove any value to this node
	 */ 
	public void clearValue() {
		localValue = null;
	}		
	
	/**
	 * Does this node have a value?. 
	 * @return return true is this node has a value, otherwise false
	 */ 
	public boolean hasValue() {
		if (localValue==null)
			return false;
		else
			return true;
	}		
	
	/**
	 * Get the named node from the set.  If it is a terminal or the node does
	 * not exist, it will return null;
	 */ 
	public NamedTreeNode getSubnode(String  name)  {
		if (table.containsKey(name)) {
			return table.get(name);
		} else {
			return null;
		}
	}

	/**
	 * Remove a named node from the set.  If it doesn't exist, don't worry about it!
	 */ 
	public void removeSubnode(String  name) {
		if (table.containsKey(name)) {
			table.remove(name);
		}
	}
	
	/**
	 * Get the named node from the set.  If it doesn't exist, create it!
	 */ 
	public NamedTreeNode getOrCreateSubnode(String  name)  {
		NamedTreeNode result = null;
		if (table.containsKey(name)) {
			result =  table.get(name);
		} else {
			result = new NamedTreeNode();
			table.put(name,result);
		}
		return result;
	}	
	
	/**
	 * Get a set of subnodes.
	 * @return a Set of Strings
	 */ 
	public Set<String> getSubnodeNames() {
		return table.keySet();
	}
}
