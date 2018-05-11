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
package things.thing;

import java.util.HashMap;
import java.util.LinkedList;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsExceptionBundle;
import things.common.ThingsNamespace;
import things.data.ThingsPropertyView;
import things.thinger.ExpressionInterface;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.io.Logger;
import things.thinger.kernel.ThingsState;

/**
 * A cluster of things.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 08
 * </pre> 
 */
public class Cluster  {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	/**
	 * Internal representation of a THING in the cluster.
	 */
	private class Node {
		String name;
		String thingName;
		String processId;
		ThingsException exception;
//		RESULT result;
		ThingsPropertyView overlayProperties;
	}
	
	/**
	 * Cluster nodes.
	 */
	public HashMap<String,Node>	nodes;

	/**
	 * We are not owned by a thing.  The creator will have to provide what we need.
	 */
	private SystemInterface si;
	private ExpressionInterface	parentInterface;
	
	/**
	 * Keep track if the cluster is started.
	 */
	private boolean started = false;
	
	// ====================================================================================================================================
	// ===================================================================================================================================
	// == METHODS.  
	
	/**
	 * Construct a cluster owned by a THING.
	 * @param owner the owner.
	 * @throws Throwable, mostly if the owner is null.
	 */
	public Cluster(THING	owner) throws Throwable {
		if (owner==null) ThingsException.softwareProblem("Cannot create a cluster with a null owner");
		si = owner.mySystemInterface;
		parentInterface = owner.getParentExpressionInterface();
		commonConstructor();
	}
	
	/**
	 * Construct a cluster not owned by a THING.
	 * @param si A system interface.
	 * @param parentInterface  A parent interface.
	 * @throws Throwable, mostly if the si or parentInterface are null.
	 */
	public Cluster(SystemInterface si, ExpressionInterface	parentInterface) throws Throwable {
		if (si==null) ThingsException.softwareProblem("Cannot create a cluster with a null si");
		if (parentInterface==null) ThingsException.softwareProblem("Cannot create a cluster with a null parentInterface");
		this.si = si;
		this.parentInterface = parentInterface;
		commonConstructor();
	}
	
	/**
	 * Common constructor tool.
	 */
	private void commonConstructor() {
		nodes = new HashMap<String,Node>();	
	}
	
	/**
	 * Add a node to the cluster.
	 * @param clusterNodeName a unique name for the node.  
	 * @param thingName the thing name to run as the node.
	 * @throws Throwable if either parameter is null or the node name has already been used.
	 */
	public void add(String clusterNodeName, String thingName) throws Throwable {
		if ((clusterNodeName==null)||(clusterNodeName.length()<1)) ThingsException.softwareProblem("Cannot add to a cluster with a null or empty clusterNodeName");
		if ((thingName==null)||(thingName.length()<1)) ThingsException.softwareProblem("Cannot add to a cluster with a null or empty thingName");
		if (nodes.containsKey(clusterNodeName)) throw new SystemException("Node already defined for cluser.", SystemException.ERROR_THING_CLUSTER_NODE_ALREADY_DEFINED, ThingsNamespace.ATTR_THING_CLUSTER_NODE_NAME, clusterNodeName);
		
		Node theNode = new Node();
		theNode.name = clusterNodeName;
		theNode.thingName = thingName;
		theNode.overlayProperties = null;
		nodes.put(clusterNodeName, theNode);
	}
	
	/**
	 * Add a node to the cluster.
	 * @param clusterNodeName a unique name for the node.  
	 * @param thingName the thing name to run as the node.
	 * @param overlayProperties properties to overlay for that thing only.  May not be null.  (Use the other method).
	 * @throws Throwable if either parameter is null or the node name has already been used.
	 */
	public void add(String clusterNodeName, String thingName, ThingsPropertyView overlayProperties) throws Throwable {
		if ((clusterNodeName==null)||(clusterNodeName.length()<1)) ThingsException.softwareProblem("Cannot add to a cluster with a null or empty clusterNodeName");
		if (overlayProperties==null) ThingsException.softwareProblem("Cannot add to a cluster with a null or empty overlayProperties");
		if ((thingName==null)||(thingName.length()<1)) ThingsException.softwareProblem("Cannot add to a cluster with a null or empty thingName");
		if (nodes.containsKey(clusterNodeName)) throw new SystemException("Node already defined for cluser.", SystemException.ERROR_THING_CLUSTER_NODE_ALREADY_DEFINED, ThingsNamespace.ATTR_THING_CLUSTER_NODE_NAME, clusterNodeName);
		
		Node theNode = new Node();
		theNode.name = clusterNodeName;
		theNode.thingName = thingName;
		theNode.overlayProperties = overlayProperties;
		nodes.put(clusterNodeName, theNode);
	}
	
	/**
	 * Start the cluster.  IF the cluster has already been started, this will have no effect.
	 * @throws Throwable A resolution of all start exceptions.  Note that the exception will not interrupt the start list.  It will finish 
	 * attempting to start all nodes before letting an exception out.  So, your cluster may be running!
	 */
	public void start() throws Throwable {
		if (started) return;
		
		ThingsExceptionBundle<ThingsException> exceptions = new ThingsExceptionBundle<ThingsException>();
		Logger logger = si.getSystemLogger();

		// Start them.  Keep track of each exception for each start.
		started = true;
		for (Node theNode : nodes.values()) {
			try {
				
				if (theNode.overlayProperties==null) 
					theNode.processId = si.runThing(theNode.thingName, parentInterface);
				else  
					theNode.processId = si.runThing(theNode.thingName, parentInterface, theNode.overlayProperties);
				logger.info("Cluser node started.", ThingsCodes.THING_CLUSTER_NODE_STARTED, ThingsNamespace.ATTR_THING_NAME, theNode.thingName, ThingsNamespace.ATTR_THING_CLUSTER_NODE_NAME, theNode.name);
					
			} catch (ThingsException te) {
				theNode.exception = te;
				exceptions.add(te);
				logger.error("Cluser node failed to start.", ThingsCodes.ERROR_THING_CLUSTER_NODE_START_ERROR, ThingsNamespace.ATTR_THING_NAME, theNode.thingName, ThingsNamespace.ATTR_THING_CLUSTER_NODE_NAME, theNode.name, ThingsNamespace.ATTR_PLATFORM_MESSAGE, te.getMessage());
			}  catch (Throwable t) {
				theNode.exception = new ThingsException("Spurious exception during node start.", ThingsException.ERROR_THING_CLUSTER_NODE_START_ERROR, t);
				exceptions.add(theNode.exception);
				logger.error("Cluser node failed to start.", ThingsCodes.ERROR_THING_CLUSTER_NODE_START_ERROR, ThingsNamespace.ATTR_THING_NAME, theNode.thingName, ThingsNamespace.ATTR_THING_CLUSTER_NODE_NAME, theNode.name, ThingsNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
			}
		} 
		logger.info("A nodes in cluster started.");
		
		// Resolve and throw, if we have any exceptions.
		if (exceptions.size()>0) {
			 throw exceptions.resolve();
		}
		
	}
	
	/**
	 * Check the cluster if it is done.  If all nodes are done, it will return true.
	 * @return true if all nodes are done, otherwise false (including if it hasn't been started yet).
	 * @throws Throwable
	 */
	public boolean checkClusterDone() throws Throwable {
		if (!started) return false;
		boolean done = true;

		try {
			
			// Start them.  Keep track of each exception for each start.
			for (Node theNode : nodes.values()) {
				ThingsState state = si.getProcessState(theNode.processId);
				if (!state.isDeadOrDying()) {
					done = false;
					break;  // yeah, I'm a slut for 'proper' code.
				}
			}
			
		} catch (Throwable t) {
			// Be careful here not to let the done=true out accidently.
			throw new SystemException("THING cluster check failed.", SystemException.ERROR_THING_CLUSTER_CHECK_FAILED, t);
		}
		
		return done;
	}
	
	/**
	 * Get the result for the cluster.  the cluster may still be running.
	 * @return the result.
	 * @throws Throwable A fault if there is any troubles.
	 */
	public RESULT getResult() throws Throwable {
		RESULT finalResult;
		LinkedList<RESULT> results = new LinkedList<RESULT>();
		
		try {
			for (Node theNode : nodes.values()) {
				results.add(si.getProcessInterface(theNode.processId).getResult());
			}
		} catch (Throwable t) {
			throw new SystemException("Failed to collect results from cluster member.", SystemException.THING_FAULT_RESULT_COLLECTION, t);
		}
		
		try {
			finalResult =  ResultManager.calculateResult(results, null);
		} catch (Throwable t) {
			throw new SystemException("Failed to calculate results from the cluster.", SystemException.THING_FAULT_RESULT_MANAGEMENT, t);
		}
		
		return finalResult;
	}
}
