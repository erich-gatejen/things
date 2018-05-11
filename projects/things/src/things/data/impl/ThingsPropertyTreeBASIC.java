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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.NV;
import things.data.NVImmutable;
import things.data.ThingsProperty;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyTrunk;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;

/**
 * A basic property tree that supports load and save.  You must INIT the parent before any child operations!
 * Sub and ply collections are slow.  Access is synchronized internally only during loads and saves.  
 * Otherwise, it is up to the users to keep it straight.
 * <p>
 * <h2>SPECIAL NOTE</h2>
 * The real implementation of a property tree remains proprietary.  This is a cheap implementation provided
 * so we can release everything else.  It works well enough for casual use, but don't expect this
 * to be a good example of how to design on code.
 * <p>
 * THIS IMPLEMENTATION IS A BIG FAT BUCKET OF ConcurrentModificationException, since it uses
 * HashMap.  This might cause you some hassle.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 26 MAR 05
 * EPG - Shift implementation to generic HashMap - 14 JUN 05
 * EPG - Added sub - 19 JUL 07
 * </pre> 
 */
public class ThingsPropertyTreeBASIC implements ThingsPropertyTree, ThingsPropertyView {

	// ===============================================================================================================================
	// ==  DATA
	private HashMap<String, String[]> myProperties = null;
	private String myRoot = null;
    private ThingsPropertyTrunk	myTrunk = null;
    private Object loadAndSaveMonitor;  
	
	// ===============================================================================================================================
	// ==  CONSTRUCTOR
	
	/**
	 * Default constructor
	 */
	public ThingsPropertyTreeBASIC() {
		myRoot = ThingsConstants.EMPTY_STRING;
		myProperties = new HashMap<String, String[]>();		
		loadAndSaveMonitor = new Object();
		myTrunk = new ThingsPropertyTrunkInMemory();
	}
	
	/**
	 * Construct a child.  Do not use this directly.
	 */
	public ThingsPropertyTreeBASIC(HashMap<String, String[]> properties, String root, ThingsPropertyTrunk	trunk) throws ThingsException {
		if (properties==null) ThingsException.softwareProblem("Passed a null properties when making a child ThingsPropertyTreeBASIC().  Did you remember to init() the parent?");
		if (root==null) ThingsException.softwareProblem("Passed a null root when making a child ThingsPropertyTreeBASIC().  Did you remember to init() the parent?");
		if (trunk==null) ThingsException.softwareProblem("Passed a null trunk when making a child ThingsPropertyTreeBASIC().  Did you remember to init() the parent?");	
		myRoot = root;
		myProperties = properties;	
		myTrunk = trunk;
		loadAndSaveMonitor = new Object();
	}
	
	// ===============================================================================================================================
	// ==  HELPERS
	
	// ===============================================================================================================================
	// ==  THINGS PROPERTY TREE INTERFACE
	
	/**
	 * Get a view of a branch on the tree.  This view is on a shared tree, so any changes
	 * will be visible to every view.  The branch will always be relative to the root.
	 * @param path path to the branch.  Null we return the root.
	 * @return a view of the branch
	 */
	public ThingsPropertyView getBranch(String path) throws ThingsException {
		if (path == null) return cutting("");
		return cutting(path);
	}
	
	/**
	 * Get a view from the root.  This view is on a shared tree, so any changes
	 * will be visible to every view
	 * @return a view 
	 */
	public ThingsPropertyView getRoot() throws ThingsException {
		return new ThingsPropertyTreeBASIC(myProperties, ThingsPropertyReaderToolkit.fixPath(myRoot, ThingsConstants.EMPTY_STRING), myTrunk);		
	}
	
	/**
	 * Copy a branch of the tree as a new tree.  The new tree will have no connection
	 * to the original.  The trunk will be useless.
	 * @param branchPath path to the branch from where to start the copy.  An empty or null value will return the root.
	 * @return a new property tree starting from the branch
	 * @throws things.common.ThingsException
	 */	
	@SuppressWarnings("unchecked")
	public ThingsPropertyTree copyBranch(String branchPath) throws ThingsException {
		if (branchPath==null) throw new ThingsException("branchPath cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);	

		return new ThingsPropertyTreeBASIC((HashMap<String, String[]>)myProperties.clone(), branchPath, new ThingsPropertyTrunkInMemory());
	}
	
	/**
	 * Prune off the path.  Properties will be removed and gone forever.
	 * @param path the property path (relative to the view root).  A null is not allowed.
	 * @throws things.common.ThingsException 
	 */	
	public void prune(String path) throws ThingsException {
		if (path==null) throw new ThingsException("branchPath cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);	
		if (path.length() <1 ) throw new ThingsException("branchPath cannot be empty", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);	
		
		// More suckiness.  Examine every node for deletion before deletion to avoid concurrent mod.
		LinkedList<String> doomedNodes = new LinkedList<String>();
		for (String itemPath : myProperties.keySet()) { 
			if (itemPath.indexOf(path)==0) {
				// Make sure we only get what we want.
				if ( (  itemPath.length()==path.length() ) || 
					 (  (itemPath.length() > path.length()) &&  (itemPath.charAt(path.length()+1)==ThingsProperty.PROPERTY_PATH_SEPARATOR) )  
					)
				doomedNodes.add(itemPath);
			}
		}
		
		// Destroy!
		for (String itemPath : doomedNodes) { 
			myProperties.remove(itemPath);
		}	
	}
	
	/**
	 * Copy a branch and graft it somewhere else.  The new nodes will have no connection to the other branch.
	 * @param sourceBranchPath path to the branch from where to copy.  An empty or null value will return the root.
	 * @param graftRoot the path to where it should be grafted.
	 * @throws things.common.ThingsException
	 */	
	public void copyAndGraftBranch(String sourceBranchPath, String graftRoot) throws ThingsException {
		if (sourceBranchPath==null) throw new ThingsException("sourceBranchPath cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);
		if (graftRoot==null) throw new ThingsException("graftRoot cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);
		
		// This is like sub, so isn't very fast.
		try {
			String cPath = sourceBranchPath + ThingsProperty.PROPERTY_PATH_SEPARATOR;
			String graftPath;
			String[] value;
			
			// The painful truth, we'll need to list the nodes first to avoid concurrentmod exceptions.  This kinda sucks. 
			LinkedList<String> actualItems = new LinkedList<String>();
			for (String itemPath : myProperties.keySet()) { 
				// base path matches.
				if (itemPath.indexOf(cPath)==0) {
					actualItems.add(itemPath);
				}
			}
			
			// This is a really slow implementation.  Check every item.  IT should not have another level
			// to count.
			for (String itemPath : actualItems ) {
			
				// Give up if any can't parse.
				try {
					// OK, this is a match.  Graft it.
					graftPath = graftRoot + ThingsProperty.PROPERTY_PATH_SEPARATOR + itemPath.substring(cPath.length());
					value = myProperties.get(itemPath).clone();
					//value = value.clone();
					myProperties.put(graftPath, value);
					
				} catch (Exception e) {
					// punt on this entry.  It may just be the root, which we don't care about.
				}
				
			} // end for
			
		} catch (Throwable t) {
			throw new ThingsException("Failed to copy and graft.", ThingsException.DATA_ERROR_BRANCH_PROCESSING_ERROR, t);
		}
	}
	
	/**
	 * Tell the tree to load fresh.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 	
	public void load() throws ThingsException {

		try {
				
			// Dont let anyone else load or save
			synchronized(loadAndSaveMonitor) {
			
				// Start the load.
				myTrunk.startRead();
				
				// Run all the items.  Only load what matches the root.
			    NV current = myTrunk.readNext();
			    while (current != null) {
			    	
			    	// Does it count?
			    	if ( 
			    			// Trival case - Empty root so add everything.
			    			( ( myRoot.length() > 0 ) ||
			    			
			    			//  Not null and it is in the root's path
			    		    (current.getName().indexOf(myRoot)>=0) ) 
			    			
			    		) {
			    	
			    		// it counts.
			    		myProperties.put(current.getName(), current.getValues());
			    	}
			        
			        // Next value
			        current = myTrunk.readNext();
			    }
		    
			} // end synch
			
		} catch (ThingsException te) {
		    throw new ThingsException("Property load failed", ThingsException.SYSTEM_FAULT_PROPERTIES_LOAD_FAILED,te);
		} catch (Exception ee) {
		    throw new ThingsException("Property load failed to unexpected exception or interuption.", ThingsException.SYSTEM_FAULT_PROPERTIES_LOAD_FAILED,ee);		    
		} finally {
		    try {
		    	myTrunk.endRead();
		    } catch (Throwable eee) {
		        // Don't care
		    }
		}
	}
	
	/**
	 * Tell the tree to save itself.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 
	public void save() throws ThingsException {
		
		// See if the trunk was not init().  Likely a clone then.
		if (myTrunk == null) throw new ThingsException("Property save failed.  No where defined to save them.", ThingsException.SYSTEM_FAULT_PROPERTIES_SAVE_FAILED);
				
		try {
			
			// Dont let anyone else load or save
			synchronized(loadAndSaveMonitor) {
			
				// Start the load.
				myTrunk.startWrite();
				
				// Run ALL the items.  Only load what matches the root.
				for (Entry<String,String[]> entry : myProperties.entrySet()) {
					myTrunk.writeNextMultivalue(entry.getKey(), entry.getValue());	
				}
		    
			} // end synch
			
		} catch (ThingsException te) {
		    throw new ThingsException("Property save failed", ThingsException.SYSTEM_FAULT_PROPERTIES_SAVE_FAILED,te);
		} catch (Exception ee) {
		    throw new ThingsException("Property save failed to unexpected exception or interuption.", ThingsException.SYSTEM_FAULT_PROPERTIES_SAVE_FAILED,ee);		    
		} finally {
		    try {
		    	myTrunk.endWrite();
		    } catch (Exception eee) {
		        // Don't care
		    }
		}
	}

	/**
	 * Tell the tree to init itself.  It will dump any current properties.  You'll
	 * need to load() new props.  Typically, the ThingsPropertyTrunk needs to be primed
	 * with a ThingsPropertyTrunk.startRead() before the load() method is called.  However
	 * this may be left up to the implementation.
	 * @param tio a trunk to load and save the properties
	 * @throws things.common.ThingsException
	 */ 
	public void init(ThingsPropertyTrunk tio) throws ThingsException {

		// Dont let anyone else load or save
		synchronized(loadAndSaveMonitor) {
			myRoot = ThingsConstants.EMPTY_STRING;
			myProperties = new HashMap<String, String[]>();	
			myTrunk = tio;
		}
	}
	
	/**
	 * This is how you load and merge properties into an already populated tree.  
	 * Typically, the ThingsPropertyTrunk needs to be primed
	 * with a ThingsPropertyTrunk.startRead() before the load() method is called.  However
	 * this may be left up to the implementation.
	 * @param tio a trunk to load and save the properties
	 * @throws things.common.ThingsException
	 */ 
	public void infliltrate(ThingsPropertyTrunk tio) throws ThingsException {
		// Dont let anyone else load or save
		synchronized(loadAndSaveMonitor) {
			myTrunk = tio;
		}
	}
	
	// ===============================================================================================================================
	// ==  THINGS PROPERTY VIEW INTERFACE
	
	/**
	 * Set a property value as a string.  The property will be from the root
	 * of the view.  Bad parameters will result in ERROR level exceptions.
	 * @param path the property path (relative to the view root)
	 * @param value the property value as a string
	 * @throws things.common.ThingsException 
	 */	
	public void setProperty(String path, String value) throws ThingsException {
		ThingsPropertyReaderToolkit.validatePath(path);
		ThingsPropertyReaderToolkit.validateValue(value);	
		
		// Simple value
		String[] values = new String[1];
		values[0] = value;
		myProperties.put(ThingsPropertyReaderToolkit.fixPath(myRoot, path), values);	
	}
	
	/**
	 * Set a property value as a string.  The property will be from the root
	 * of the view.  If the value is null, it will set it as the defaultValue instead.
	 * @param path the property path (relative to the view root)
	 * @param value the property value as a string
	 * @param defaultValue the default value.
	 * @throws things.common.ThingsException 
	 */	
	public void setProperty(String path, String value, String defaultValue) throws ThingsException {
		if (value==null) setProperty(path, defaultValue);
		else setProperty(path, value);
	}
	
	/**
	 * Set a property value from an NVImmutable item.  The property will be from the root
	 * of the view.
	 * @param item the item
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */	
	public void setProperty(NVImmutable  item) throws ThingsException {
		setPropertyMultivalue(item.getName(), item.getValues());
	}
	
	/**
	 * Set a property value as a multivalue.  The property will be from the root
	 * of the view.
	 * @param path the property path (relative to the view root)
	 * @param values the property values as Strings.
	 * @throws things.common.ThingsException 
	 */	
	public void setPropertyMultivalue(String path, String... values) throws ThingsException {
		ThingsPropertyReaderToolkit.validatePath(path);
		ThingsPropertyReaderToolkit.validateValues(values);	
		
		// All values
		myProperties.put(ThingsPropertyReaderToolkit.fixPath(myRoot, path), values);	
	}
	
	/**
	 * Remove a specific property without subverting a tree.
	 * @param path the property path (relative to the view root).  A null or bogus path will be ignored.
	 * @throws things.common.ThingsException 
	 */	
	public void removeProperty(String path) throws ThingsException {
		String completePath = ThingsPropertyReaderToolkit.fixPath(myRoot, path);
		if (myProperties.containsKey(completePath)) myProperties.remove(completePath);
	}
	
	/**
	 * Pruning to get a new a new view.  The new view will have the path as its root.
	 * @param path the property path (relative to the view root)
	 * @return The new view.
	 * @throws things.common.ThingsException 
	 */	
	public ThingsPropertyView cutting(String path) throws ThingsException {
		return new ThingsPropertyTreeBASIC(myProperties, ThingsPropertyReaderToolkit.fixPath(myRoot, path), myTrunk);
	}
	
	/**
	 * Graft one view onto this view.  All properties will be added.  The values will be copies, so the original tree will
	 * be left unaltered.
	 * <p>
	 * SLOWWWW since it takes too passes.  Yuck!  We'll let the NVImmutable take care of the "copies so the original tree will be left unaltered).
	 * @param sourceView view to graft on.
	 * @throws things.common.ThingsException 
	 */	
	public void graft(ThingsPropertyView sourceView) throws ThingsException {
		if (sourceView==null) return;
		NVImmutable value;
		for (String itemPath : sourceView.sub("")) {
			value = sourceView.getPropertyNV(itemPath);
			this.setProperty(value);
		}
	}
	
	// ===============================================================================================================================
	// ==  THINGS PROPERTY VIEW READER INTERFACE
	
	/**
	 * Branch the view to create a new view.  The new view will have the path as its root.
	 * @param path the property path (relative to the view root)
	 * @return The new view.
	 * @throws things.common.ThingsException 
	 */	
	public ThingsPropertyViewReader branch(String path) throws ThingsException {
		return cutting(path);	
	}
	
	/**
	 * Get a property value as a string.  It will return null if the
	 * property is not set.  If it is a multivalue, it will return it encoded to a single String.
	 * @param path the property name
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 */
	public String getProperty(String path) throws ThingsException {
		ThingsPropertyReaderToolkit.validatePath(path);
		String fixedpath = ThingsPropertyReaderToolkit.fixPath(myRoot, path);
		String[] values = myProperties.get(fixedpath);
	
		// Is it already single or do we need to encode it?  Assume it'll be one or more.  (The validateValues during put should ensure this.)
		if (values== null) {
			return null;
		} else if (values.length==1) {
			return values[0];
		} else {
			return ThingsPropertyReaderToolkit.encodeString(values);
		}
	}
	
	/**
	 * Get a property value as a string.  It will return null if the
	 * property is not set.  If it is a multivalue, it will return it encoded to a single String.
	 * @param pathElements a stitch-able path.
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 */
	public String getProperty(String... pathElements) throws ThingsException {
		return getProperty(ThingsPropertyReaderToolkit.path(pathElements));
	}
	
	/**
	 * Get a property value as a multivalue.  It will return null if the
	 * property is not set.
	 * @param path the property name
	 * @return value A array of the values.
	 * @throws things.common.ThingsException 
	 */
	public String[] getPropertyMultivalue(String path) throws ThingsException {
		ThingsPropertyReaderToolkit.validatePath(path);
		return myProperties.get(ThingsPropertyReaderToolkit.fixPath(myRoot, path));
	}
	
	/**
	 * Get a property value as a multivalue.  It will return null if the
	 * property is not set.
	 * @param path the property name
	 * @return value of the property or null if it does not exist
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */
	public NVImmutable getPropertyNV(String path) throws ThingsException {
		String[] values = getPropertyMultivalue(path);
		if (values==null) return null;
		return new NVImmutable(path, values);
	}
	
	/**
	 * Get all property names under this at this path.
	 * @param path a root path.  If it is null, it'll return everything.
	 * @return a collection of strings
	 * @throws things.common.ThingsException 
	 */
	public Collection<String> sub(String path) throws ThingsException {
		
		HashSet<String> result = new HashSet<String>();
		String cPath = ThingsPropertyReaderToolkit.fixPath(myRoot, path);
		
		// Trivial case for empty path.
		if ((cPath==null)||(cPath.length() < 1)) {
			for (String itemPath : myProperties.keySet()) {
				result.add(itemPath);
			}
		} else {
		
			// This is a really slow implementation.  Check every item.  IT should not have another level
			// to count.
			for (String itemPath : myProperties.keySet()) {
			
				// Give up if any can't parse.
				try {
					// base path matches.
					if (itemPath.indexOf(cPath)==0) {
						
						// Peal off what is past the base path.  The +1 is for the separator.
						result.add(itemPath.substring(cPath.length()+1));
					}
					
				} catch (Exception e) {
					// punt on this entry - I need to start finding these.
					ThingsException.softwareProblem("Failed sub term.", e);
				}
				
			} // end for
		
		}
			
		// DONE
		return result;
	}
	
	/**
	 * Get the ply at this path.  It'll return all the node names at this path but no more.
	 * @param path a root path.
	 * @return a collection of strings
	 * @throws things.common.ThingsException 
	 */
	public Collection<String> ply(String path) throws ThingsException {
		if (path==null) throw new ThingsException("path cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);	
		
		HashSet<String> result = new HashSet<String>();
		String cPath = path + ThingsProperty.PROPERTY_PATH_SEPARATOR;
		int rover;
		String front;
		
		// This is a really slow implementation.  Check every item.  IT should not have another level
		// to count.
		for (String itemPath : myProperties.keySet()) {
		
			// Give up if any can't parse.
			try {
				// base path matches.
				if (itemPath.indexOf(cPath)==0) {
					
					// Peal off what is past the base path.
					front = itemPath.substring(cPath.length());
					
					// Disallow extended. 
					rover = front.indexOf(ThingsProperty.PROPERTY_PATH_SEPARATOR);
					if (rover>=0) {
						result.add(front.substring(0,rover));
					} else {
						result.add(front);
					}
				}
				
			} catch (Exception e) {
				// punt on this entry - I need to start finding these.
				ThingsException.softwareProblem("Failed sub term.", e);
			}
			
		} // end for
		
		// DONE
		return result;
	}
	
	
	// ===============================================================================================================================
	// ==  CONVENIENCE METHODS
	
	/**
	 * This will create a file based property view.  It's purely for convenience and should
	 * not be used for serious applications.
	 * @param path file path to the INI file
	 * @see things.data.impl.ThingsPropertyTrunkIO
	 * @throws  things.common.ThingsException 
	 */		
	public static ThingsPropertyTreeBASIC getExpedientFromFile(String path) throws ThingsException {
		ThingsPropertyTreeBASIC propTree = new ThingsPropertyTreeBASIC();
		ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
		trunk.init(path, new FileAccessor(new File(path)));
		propTree.init(trunk);
		propTree.load();		
		return propTree;
	}
}
