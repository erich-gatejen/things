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
 * Property tree interface.  Property trees contained name attributes in a 
 * hierarchical scheme.<br>
 * home:level1.level2.leveln.item
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 24 JUL 04
 * </pre> 
 */
public interface ThingsPropertyTree {

	/**
	 * Get a view of a branch on the tree.  This view is on a shared tree, so any changes
	 * will be visible to every view.  The branch will always be relative to the root.
	 * @param path path to the branch.  Null we return the root.
	 * @throws things.common.ThingsException
	 * @return a view of the branch
	 */
	public ThingsPropertyView getBranch(String path) throws ThingsException;
	
	/**
	 * Get a view from the root.  This view is on a shared tree, so any changes
	 * will be visible to every view
	 * @return a view 
	 */
	public ThingsPropertyView getRoot() throws ThingsException;
	
	/**
	 * Copy a branch of the tree as a new tree.  The new tree will have no connection
	 * to the original.
	 * @param branchPath path to the branch from where to start the copy.  An empty or null value will return the root.
	 * @throws things.common.ThingsException
	 * @return a new property tree starting from the branch
	 */	
	public ThingsPropertyTree copyBranch(String branchPath) throws ThingsException;
	
	/**
	 * Copy a branch and graft it somewhere else.  The new nodes will have no connection to the other branch.
	 * @param sourceBranchPath path to the branch from where to copy.  An empty or null value will return the root.
	 * @param graftRoot the path to where it should be grafted.
	 * @throws things.common.ThingsException
	 */	
	public void copyAndGraftBranch(String sourceBranchPath, String graftRoot) throws ThingsException;
	
	/**
	 * Tell the tree to load fresh.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 	
	public void load() throws ThingsException;
	
	/**
	 * Tell the tree to save itself.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 
	public void save() throws ThingsException;

	/**
	 * Tell the tree to init itself.  It will dump any current properties.  You'll
	 * need to load() new props.  Typically, the ThingsPropertyTrunk needs to be primed
	 * with a ThingsPropertyTrunk.startRead() before the load() method is called.  However
	 * this may be left up to the implementation.
	 * @param tio a trunk to load and save the properties
	 * @throws things.common.ThingsException
	 */ 
	public void init(ThingsPropertyTrunk tio) throws ThingsException;
	
	/**
	 * This is how you load and merge properties into an already populated tree.  
	 * Typically, the ThingsPropertyTrunk needs to be primed
	 * with a ThingsPropertyTrunk.startRead() before the load() method is called.  However
	 * this may be left up to the implementation.
	 * @param tio a trunk to load and save the properties
	 * @throws things.common.ThingsException
	 */ 
	public void infliltrate(ThingsPropertyTrunk tio) throws ThingsException;


}
