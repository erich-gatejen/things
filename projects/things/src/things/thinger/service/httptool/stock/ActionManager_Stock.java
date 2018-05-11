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
package things.thinger.service.httptool.stock;

import java.util.Hashtable;

import things.common.ThingsException;
import things.data.ThingsPropertyView;
import things.thinger.SystemException;
import things.thinger.SystemSuperInterface;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitController;
import things.thinger.io.conduits.ConduitID;
import things.thinger.service.ServiceConstants;
import things.thinger.service.command.local.LocalCommander;
import things.thinger.service.httptool.Action;
import things.thinger.service.httptool.ActionManager;

/**
 * This is a stock implementation of the action manager.
 * <p>
 * This will cache a local commander for general use by the actions.  For this to work, the service has to have command and response
 * channels set in the local properties, like this (or similar):<br><pre>
 * 	kb.core.service.httptoolservice.local.command_channel=kb.core.service.localcommandservice
 *	kb.core.service.httptoolservice.local.response_channel=kb.core.service.httptoolservice
 *</pre>
 * <p>
 * Internally, the names are uppercase.  Externally, the names are not case sensitive.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 07
 * </pre> 
 */
public class ActionManager_Stock extends ActionManager {

	// =================================================================================================
	// == STATIC COMMON USE LOCAL COMMANDER
	private static LocalCommander	myCommander;
	
	
	// =================================================================================================
	// == DATA
	private Hashtable<String,String>	actionMap;
	
	// =================================================================================================
	// == METHODS
	
	/**
	 * Get the local commander.  This assumes it will have been init'd already.  Maybe I need to make it check here too.  
	 * This can be accessed statically by all the action implementations.  LocalCommander is currently synchronized, so it 
	 * can be a big, fat bottleneck.
	 * @return the commander.
	 */
	public static LocalCommander getCommander() {
		return myCommander;
	}
	
	/**
	 * Add a mapping.  This is so people can use the stock and add their own mappings too.
	 * @param actionName The name of the action.
	 * @param actionClassName The action class name.
	 * @throws Throwable if either parameter is null.
	 */
	public void addMapping(String actionName, String actionClassName) throws Throwable {
		if (actionName==null) ThingsException.softwareProblem("Cannot call ActionManager_Stock.addMapping with a null actionName.");
		if (actionClassName==null) ThingsException.softwareProblem("Cannot call ActionManager_Stock.addMapping with a null actionClassName.");		
		actionMap.put(actionName, actionClassName);
	}
	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	// == IMPLEMENTED FOR GENERAL USE  ===================
	
	/**
	 * Get the main action. Consider this the index.html of the system.
	 * @return the name of the main action.
	 * @throws ThingsException for any problem.
	 */
	public String getMainName() throws ThingsException {
		return StockConstants.ACTION_MAIN;
	}
	
	// == IMPLEMENTED FOR INTERNAL USE ===================
	
	/**
	 * Load an action by name.  This will not come from the cache.  Use get() if you want the cache. 
	 * Most user applications should use get() instead.
	 * @param actionName the name of the action.
	 * @return the action or null if it could not be found.  
	 * @throws Throwable for any problem (except for it not being found).  This is usually fatal.
	 */
	protected Action load(String  actionName) throws Throwable {
		
		Action result = null;
		try {
			
			String normalName = actionName.trim().toUpperCase();
			if (actionMap.containsKey(normalName)) {
				
				// Build one
				Class<?> actionClass = Class.forName(actionMap.get(normalName));
				result = (Action)actionClass.newInstance();
				
			} 	//else we'll let the null stand since it isn't in the map and thus unknown.
			
		} catch (Throwable t) {
			throw new ThingsException("Could not load Action", ThingsException.SERVICE_HTTPTOOL_ACTION_LOAD_FAILED, t);
		}
		return result;
	}
	
	/**
	 * The initialization chain.  This will be called when the system inits the manager.  It gives the base class
	 * a chance to init.  The si (SystemInterface) will be set and usable.  The system may call this more than once.  If it
	 * does, the state should be reset and the initialization done again.
	 * <p>
	 * It is synchronized because we can't let anyone else muddle with myCommander.
	 * @throws ThingsException for any problem.
	 */
	protected synchronized void init_chain() throws ThingsException {
		
		// Do this the lame way, since there are so few.
		actionMap = new Hashtable<String,String>();
		actionMap.put(StockConstants.ACTION_MAIN, StockConstants.ACTION_MAIN_CLASS);
		actionMap.put(StockConstants.ACTION_PROCESSLIST, StockConstants.ACTION_PROCESSLIST_CLASS);
		actionMap.put(StockConstants.ACTION_HELPER, StockConstants.ACTION_HELPER_CLASS);
			
		// Make sure we have a local commander.
		if (myCommander == null) {
			try {
				// Get local command info
				ThingsPropertyView localProperties = si.getLocalProperties();
				String commandChannelName = localProperties.getProperty(ServiceConstants.CHANNEL_COMMAND);
				String responseChannelName = localProperties.getProperty(ServiceConstants.CHANNEL_RESPONSE);
							
				// Create our conduits
				SystemSuperInterface ssi = si.requestSuperSystemInterface();
				ConduitController systemController = ssi.getSystemConduits();
				Conduit commandChannel = systemController.tune(new ConduitID(commandChannelName), ssi.getCallingProcessId());
				Conduit responseChannel = systemController.tune(new ConduitID(responseChannelName), ssi.getCallingProcessId());
				
				// Attach to a new LocalCommander
				myCommander = new LocalCommander(commandChannel, responseChannel, ssi.getCallingProcessId());
				
			} catch (Throwable t) {
				throw new SystemException("Failed to construct action manager.", SystemException.SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT, t);
			}
		}
		
	}

	
}
