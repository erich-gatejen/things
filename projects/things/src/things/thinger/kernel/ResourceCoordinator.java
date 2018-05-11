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
package things.thinger.kernel;

import java.util.HashMap;

import things.common.WhoAmI;
import things.thinger.SystemException;

/**
 * A resource coordinator.  This is mostly a helper for the Kernel.  This is where use of IDs went bad in this project.
 * It would have taken a lot of work to fully abstract id operations, so we pretty much just use string representations.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 06
 * </pre> 
 */
public class ResourceCoordinator {

	// ================================================================================================
	// DATA
	
	private HashMap<String, ResourceManager>	resources;
	private WhoAmI								myId;
	
	// ================================================================================================
	// METHODS
	
	/**
	 * Constructor.  Create the coordinator.
	 * @param id The ID of the ResourceCoordinator.
	 * @see things.common.WhoAmI
	 */
	public ResourceCoordinator(WhoAmI	id) throws SystemException {
		if (id==null) SystemException.softwareProblem("ResourceCoordinator constructed with a null id.");
		resources = new HashMap<String, ResourceManager>();
		myId = id;
	}
	
	/**
	 * Register a resource.  It will throw an exception for any error, including a duplicate registration.
	 * @param aResource The resource to register.
	 * @param name The name of the resource.  This can be unique to this registration.
	 * @throws things.thinger.SystemException 
	 * @return te resource manager.
	 * @see things.common.WhoAmI
	 */
	public ResourceManager registerResource(ResourceInterface	aResource, String	name) throws  SystemException {
		
		ResourceManager rManager = null;
		
		// Validation step 1
		WhoAmI resourceID = aResource.getId();
		if (resourceID != null) throw new SystemException("Cannot register a resource that has already been initialized through initResource().",SystemException.RESOURCE_ERROR_ALREADY_REGISTERED,"resource.id",resourceID.toString());
		if (name == null) throw new SystemException("Resource name cannot be null for registerResource().",SystemException.RESOURCE_ERROR_BAD_REGISTRATION);
	
		// Propose an ID.
		WhoAmI proposedID = myId.birthMyChild(name);
		synchronized(resources) {
			
			// Make sure it is uniquely named.
			if (resources.containsKey(proposedID.toString())) 
				 throw new SystemException("Cannot register a resource since another resource has already been given the same ID (named).",SystemException.RESOURCE_ERROR_IDNAMED_USED,"resource.id",proposedID.toString());
			
			// Init it and put it under management.
			try {
				aResource.initResource(proposedID);	
				rManager = new ResourceManager(aResource);
			} catch (Throwable t) {
				 throw new SystemException("Failed to register resource due to an error.",SystemException.RESOURCE_ERROR_DURING_REGISTRATION,t,"resource.id",proposedID.toString(),"error message",t.getMessage());
			}
			
			// Register it.
			resources.put(proposedID.toString(),rManager);
		}
		return rManager;
	}
	
	/**
	 * Get a resource by ID.  
	 * @param id  The WhoAmI ID of the resource.
	 * @return the ResourceManager for the resouce.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.ResourceManager
	 */
	public ResourceManager getResource(WhoAmI	id) throws SystemException {

		ResourceManager result = null;
		
		// Validate id
		if (id == null) throw new SystemException("Resource id cannot be null for getResource().",SystemException.RESOURCE_ERROR_DURING_LOOKUP);
		
		// Lock the map and find it.
		synchronized(resources) {
			if (resources.containsKey(id.toString())) {
				result = resources.get(id.toString());
			} else {
				throw new SystemException("Resource not registered.",SystemException.RESOURCE_ERROR_RESOURCE_NOT_REGISTERED,"resource.id",id.toString());
			}
		}
		
		// Done
		return result;
	}	
	
	/**
	 * Get a resource by name.  
	 * @param name The name of the resource.  This should be the name used to register it and not a WhoAmI string.
	 * @return the ResourceManager for the resouce.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.ResourceManager
	 */
	public ResourceManager getResource(String	name) throws SystemException {

		// Validate name
		if (name == null) throw new SystemException("Resource name cannot be null for getResource().",SystemException.RESOURCE_ERROR_DURING_LOOKUP);
		
		// Find it.
		WhoAmI lookupID = myId.birthMyChild(name);
		return this.getResource(lookupID);
	}	

	
	/**
	 * Check to see if the resource is registered.  
	 * @param id  The WhoAmI ID of the resource.  This is the ID provided by the coordinator in the ResourceManager.
	 * @return true if it is registered, otherwise false.
	 */
	public boolean isRegistered(WhoAmI	id) throws SystemException {
		
		// Validate id
		if (id == null) throw new SystemException("Resource id cannot be null for isRegistered().",SystemException.RESOURCE_ERROR_DURING_LOOKUP);
		
		// Lock the map and find it.
		synchronized(resources) {
			if (resources.containsKey(id.toString())) {
				return true;
			} else {
				return false;
			}
		}
	}	
	
	/**
	 * Check to see if the resource is registered.  Use the name.
	 * @param name The name of the resource.  This should be the name used to register it and not a WhoAmI string.
	 * @return true if it is registered, otherwise false.
	 */
	public boolean isRegistered(String	name) throws SystemException {

		// Validate name
		if (name == null) throw new SystemException("Resource name cannot be null for isRegistered().",SystemException.RESOURCE_ERROR_DURING_LOOKUP);
		
		// Find it.
		WhoAmI lookupID = myId.birthMyChild(name);
		return this.isRegistered(lookupID);
	}	
	
}
