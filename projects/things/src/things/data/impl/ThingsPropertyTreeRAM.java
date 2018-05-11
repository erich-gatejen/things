/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.data.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.NVImmutable;
import things.data.ThingsProperty;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyTrunk;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;

/**
 * A very basic property view for an in-memory store using java HashMap. 
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
public class ThingsPropertyTreeRAM implements ThingsPropertyTree, ThingsPropertyView {

	// ===============================================================================================================================
	// ==  DATA
	
	/**
	 * Our Data.
	 */
	private HashMap<String, String[]> myProperties = null;
	private String myRoot = null;
	
	// ===============================================================================================================================
	// ==  CONSTRUCTOR
	
	/**
	 * Default constructor
	 */
	public ThingsPropertyTreeRAM() {
		myRoot = ThingsConstants.EMPTY_STRING;
		myProperties = new HashMap<String, String[]>();		
	}
	
	/**
	 * Construct a child.  Do not use this directly.
	 */
	public ThingsPropertyTreeRAM(HashMap<String, String[]> properties, String root) throws ThingsException {
		if (properties==null) ThingsException.softwareProblem("Passed a null properties when making a child InMemoryPropertyView()");
		if (root==null) ThingsException.softwareProblem("Passed a null root when making a child InMemoryPropertyView()");	
		myRoot = root;
		myProperties = properties;		
	}
	
	// ===============================================================================================================================
	// ==  HELPERS
	
	// ===============================================================================================================================
	// ==  THINGS PROPERTY TREE INTERFACE
	
	/**
	 * Get a view of a branch on the tree.  This view is on a shared tree, so any changes
	 * will be visible to every view.  The branch will always be relative to the root.
	 * @param path path to the branch.  null we return the root
	 * @return a view of the branch
	 */
	public ThingsPropertyView getBranch(String path) throws ThingsException {
		if (path==null) return cutting("");
		return cutting(path);
	}
	
	/**
	 * Get a view from the root.  This view is on a shared tree, so any changes
	 * will be visible to every view
	 * @return a view 
	 */
	public ThingsPropertyView getRoot() throws ThingsException {
		return new ThingsPropertyTreeRAM(myProperties, ThingsPropertyReaderToolkit.fixPath(myRoot, ThingsConstants.EMPTY_STRING));		
	}
	
	/**
	 * Copy a branch of the tree as a new tree.  The new tree will have no connection
	 * to the original.
	 * @param branchPath path to the branch from where to start the copy.  An empty or null value will return the root.
	 * @return a new property tree starting from the branch
	 */	
	@SuppressWarnings("unchecked")
	public ThingsPropertyTree copyBranch(String branchPath) throws ThingsException {
		if (branchPath==null) throw new ThingsException("branchPath cannot be null", ThingsException.DATA_ERROR_PROPERTY_PATH_NULL, ThingsNamespace.ATTR_PROPERTY_NAME);
		return new ThingsPropertyTreeRAM((HashMap<String, String[]>)myProperties.clone(), branchPath);
		
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
			
			// The painful truth, we'll need to list the nodes first to avoid concurrent mod exceptions.  This kinda sucks. 
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
					 (  (itemPath.length() > path.length()) &&  (path.charAt(itemPath.length()+1)==ThingsProperty.PROPERTY_PATH_SEPARATOR) )  
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
	 * Tell the tree to load fresh.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 	
	public void load() throws ThingsException {
		// Ignore since this is ram
	}
	
	/**
	 * Tell the tree to save itself.  The actual action is up to the implementation.
	 * @throws things.common.ThingsException
	 */ 
	public void save() throws ThingsException {
		// Ignore since this is ram
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
		// Ignore the tio
		myRoot = ThingsConstants.EMPTY_STRING;
		myProperties = new HashMap<String, String[]>();	
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
		// Ignore since this is ram
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
		return new ThingsPropertyTreeRAM(myProperties, ThingsPropertyReaderToolkit.fixPath(myRoot, path));
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
		String[] values = myProperties.get(ThingsPropertyReaderToolkit.fixPath(myRoot, path));
	
		// Is it already single or do we need to encode it?  Assume it'll be one or more.  (The validateValues during put should ensure this.)
		if (values==null) return null;
		if (values.length==1) {
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
		return new NVImmutable(path, getPropertyMultivalue(path));
	}
	
	/**
	 * Get all property names under this at this path.
	 * @param path a offset path.
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
					// punt on this entry
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
		
		// This is a really slow implementation.  Check every item.  IT should not have another level
		// to count.
		for (String itemPath : myProperties.keySet()) {
		
			// Give up if any can't parse.
			try {
				// base path matches.
				if (itemPath.indexOf(cPath)==0) {
					
					// Peal off what is past the base path.  The +1 is for the sperator.
					String front = itemPath.substring(cPath.length()+1);
					
					// Disallow extended. 
					rover = front.indexOf(ThingsProperty.PROPERTY_PATH_SEPARATOR);
					if (rover>=0) {
						result.add(front.substring(0,rover));
					} else {
						result.add(front);
					}
				}
				
			} catch (Exception e) {
				// punt on this entry
			}
			
		} // end for
		
		// DONE
		return result;
	}
	
}
