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

import java.util.Set;
import java.util.regex.Pattern;

/**
 * A named tree implementation.  This is NOT indexed and thus should not be used
 * in anything needing speed.  However, the tree can be trimmed, pruned, spliced,
 * and diced at your whim and whenever.  It assumes that the values are immutable!
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 SEP 02
 * </pre> 
 */
public class NamedTree {
	
	// PRIVATE FIELDS
	private NamedTreeNode		rootNode;
	private Pattern 			dotPattern;
	
	/**
	 * CONSTRUCTOR
	 */	
	public NamedTree() {
		rootNode = new NamedTreeNode();
		dotPattern = Pattern.compile("\\x2e");
	}
	
	/**
	 * Set the value of a path.  If the value already exists, it will be changed.
	 * It will automatically fill in nodes to make the tree whole.  If it fails, it
	 * will do so quietly, but return false.
	 * @param path in dot notation (level1.level2.terminal)
	 * @param value as an Object
	 * @return true is successful, otherwise false
	 */
	public boolean set(String path, Object value) {

		try {
			// Split and walk the path.  No recursion!
			String splitPath[] = dotPattern.split(path);
			if (splitPath.length == 0) return false;
			NamedTreeNode rover = rootNode;
			
			// Walk to the terminal.  length-1 is the terminal
			for (int level = 0; level <= splitPath.length-1;level++) {
				rover = rover.getOrCreateSubnode(splitPath[level]);
			}			
						
			// Set the terminal
			rover.setValue(value);	
		
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * It will remove everything at and beyond the named path.  It will
	 * never fail.  If the path doesn't exist, it will simple exit.
	 * @param path in dot notation (level1.level2.terminal)
	 */
	public void remove(String path) {
		
		try {
			// Split and walk the path.  No recursion!
			String splitPath[] = dotPattern.split(path);
			if (splitPath.length == 0) return;
			if (splitPath.length == 1) {
				rootNode.removeSubnode(splitPath[0]);
				return;
			}
			
			// Walk to the terminal container node
			// splitPath.length-1 is the terminal node.
		    // splitPath.length-2 is the terminal container node
			NamedTreeNode rover = rootNode;		
			for (int level = 0; level <= splitPath.length-2;level++) {
				rover = rover.getSubnode(splitPath[level]);
			}
			
			// Drop the pointer to the terminal node from the container node
			rover.removeSubnode(splitPath[splitPath.length-1]);	
		
		} catch (Exception e) {
			// don't care
		}
	}
	
	/**
	 * Get the value at the path
	 * @param path in dot notation (level1.level2.terminal)
	 * @return the value as an Object
	 */
	public Object get(String path) {
		Object result = null;
		
		try {

			// Split and walk the path.  No recursion!
			String splitPath[] = dotPattern.split(path);
			if (splitPath.length == 0) return null;
			
			// Walk to the terminal
			// splitPath.length-1 is the terminal node.
			NamedTreeNode rover = rootNode;		
			for (int level = 0; level <= splitPath.length-1;level++) {
				rover = rover.getSubnode(splitPath[level]);
			}
			
			// Drop the pointer to the terminal node from the container node
			result = rover.getValue();
				
		} catch (Exception e) {
			// don't care
		}		
		return result;
	}	
	
	/**
	 * Mirror the tree to a new tree
	 * @param path root in dot notation (level1.level2.terminal)
	 * @return a new tree or an empty tree if the path has no nodes
	 */
	public NamedTree mirror(String path) {
	    
	    NamedTree result =  null;
	    try {
		    NamedTree newTree = new NamedTree();
		    NamedTreeNode baseNode;
		    
			// Find the base node.  If there are any nulls, abandon, because the 
		    // path doesn't exist
			String splitPath[] = dotPattern.split(path);
		    baseNode = rootNode;
			if (splitPath.length > 0) {
				// Walk to the terminal.  length-1 is the terminal
				for (int level = 0; level <= splitPath.length-1;level++) {
				    baseNode = baseNode.getSubnode(splitPath[level]);
				}				    
			}
			mirror(baseNode,newTree.rootNode);
			result = newTree;
	    } catch (Exception ee) {
	        // don't care.  null will return.
	    }
	    return result;
	}
	
	/**
	 * Mirror the tree to a new tree
	 * @param node the source starting node
	 * @param newNode the new starting node
	 * @return a new tree
	 */
	private void mirror(NamedTreeNode  node, NamedTreeNode  newNode) {
	    newNode.setValue(node.getValue());
	    Set<String> subnodes = node.getSubnodeNames();
	    for (String nodeName : subnodes) {
	        mirror(node.getSubnode(nodeName),newNode.getOrCreateSubnode(nodeName));
	    }
	}
}
