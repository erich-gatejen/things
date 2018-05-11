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

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import things.common.StringPoster;
import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsExceptionBundle;
import things.common.ThingsReportingThreshold;
import things.common.Verbose;
import things.common.WhoAmI;
import things.thinger.SystemException;
import things.thinger.SystemInterface;

/**
 * A resource manager.  It will manage a single resource.    
 * <p>
 * Note that the resource listeners will be given a chance to respond to resource revocation.  However, the Kernel may interrupt the thread at
 * its whim.  This manager will catch the interruptions.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 06
 * </pre> 
 */
public class ResourceManager implements Verbose, ThingsReportingThreshold {

	// ========================================================================================
	// DATA
	
	private final ReentrantLock lock = new ReentrantLock();
	private ResourceInterface myResource;
	private WhoAmI	myId;
	private HashSet<ResourceListener> listenerSet;
	private StringPoster debugPoster;
	private int	reportingThreshold = SystemInterface.DEFAULT_REPORTING_THRESHOLD;
	
	// ========================================================================================
	// METHODS
	
	/**
	 * Constructor.  It will put the resource under management.
	 * @param resource The resource to put under management.
	 * @see things.thinger.kernel.ResourceInterface
	 * @throws things.thinger.SystemException
	 */
	public ResourceManager(ResourceInterface resource) throws SystemException{
		myResource = resource;
		if (resource == null) SystemException.softwareProblem("A null resource object was put under management of ResourceManager()."); 

		myId = resource.getId();
		if (myId == null) SystemException.softwareProblem("You must initialize a resource before putting it under management of ResourceManager()."); 
		
		listenerSet = new HashSet<ResourceListener>();
		debugPoster = null;
	}
	
	/**
	 * Get the resource.
	 * @return The resource.
	 */
	public ResourceInterface getResource() {
		return myResource;
	}
	
	/**
	 * Get the id.
	 * @return The id.
	 */
	public WhoAmI getId() {
		return myId;
	}
	
	/** 
	 * Add listener for this resource.  It will throw a SystemException is the resource is disposed.
	 * @param listener A listener implementation.
	 * @throws things.thinger.SystemException
	 */
	public void addListener(ResourceListener listener) throws SystemException {
		lock.lock();
		try {
			if (myResource!=null) {
				listenerSet.add(listener);			
			} else {
				throw new SystemException("Resource " + myId.toString() + " has been disposed.  Cannot add listener.", SystemException.RESOURCE_ERROR_ALREADY_DISPOSED);
			}

		} finally {
			lock.unlock();
		}
	}

	/** 
	 * Remove listener for this resource.  It will attempt to call the revocation calls for the listener, both resourceRevocation and resourceRevoked, but it will not fail
	 * if anything bad happens.
	 * @param listener A listener implementation.
	 * @throws things.thinger.SystemException
	 */
	public void revokeListener(ResourceListener listener) throws SystemException {
		lock.lock();
		try {
			this.priviledged_revokingListener(listener);
			this.priviledged_revokedListener(listener);
		} catch (SystemException se) {
			se.addAttribute("resource",myId.toString());		// Add this resource id to the exception so we know where it came from.
			if (se.pass(reportingThreshold)) throw se;
			screech(se.toStringSimple());
		} finally {
			lock.unlock();
		}
	}
	
	/** 
	 * Dispose the resource.  This is death of the resource.  It will revoke all listeners too.<br>
	 * If any listener fails revocation, it will try to finish the others before throwing an exception, if the threshold warrants.
	 * @throws things.thinger.SystemException
	 */
	public void dispose() throws SystemException {
		ThingsExceptionBundle<SystemException> bundle = new ThingsExceptionBundle<SystemException>();
		lock.lock();
		try {
			
			// Revokes first
			for (ResourceListener listener : listenerSet) {
				if (debugPoster!=null) debugPoster.postit("Revoking " + listener.toString() + " from resrource " + myId.toString());
				
				try {
					priviledged_revokingListener(listener);		
				} catch (SystemException se) {
					se.addAttribute("resource",myId.toString());		// Add this resource id to the exception so we know where it came from.
					if (se.pass(reportingThreshold)) bundle.add(se);
					else screech(se.toStringSimple());					
				}
			}
			
			// Report revoked next
			for (ResourceListener listener : listenerSet) {
				if (debugPoster!=null) debugPoster.postit("Revoked " + listener.toString() + " from resrource " + myId.toString());
				try {
					priviledged_revokedListener(listener);			
				} catch (SystemException se) {
					se.addAttribute("resource",myId.toString());		// Add this resource id to the exception so we know where it came from.
					if (se.pass(reportingThreshold)) bundle.add(se);
					else screech(se.toStringSimple());					
				}
			}
			
			// Dispose it.
			try {
				if (debugPoster!=null) debugPoster.postit("Disposing resource " + myId.toString());

				myResource.disposeResource();			
			} catch (SystemException se) {
				se.addAttribute("resource",myId.toString());
				if (se.pass(reportingThreshold)) bundle.add(se);
				else screech(se.toStringSimple());					
			}
			
			// Throw any exception if we have one.
			bundle.throwResolved();

		} catch (SystemException se) {
			throw se;
		} finally {
			lock.unlock();
		}
	}
	
	// ===============================================================================
	// INTERAL PRIVILEDGED
	// These are not locking.
	
	private void priviledged_revokingListener(ResourceListener listener) throws SystemException {
		
		// Validate
		if (listener == null) throw new SystemException("Listener is null.");
		
		// Make sure the resource is not revoked
		if (myResource!=null) {
			
			if (listenerSet.contains(myResource)) {
				
				// Process revocation first.
				try {
					listener.resourceRevocation(myId);
				} catch (InterruptedException ie) {	
					throw new SystemException("resourceRevocation was interrupted by the system.",ThingsCodes.SYSTEM_ERROR_COMPONENT_INTERRUPTED,ie,"listener",listener.getListenerId().toString());
				} catch (Throwable t) {
					throw new SystemException("Error from the listner during resourceRevocation.",ThingsCodes.INFO,t,"listener",listener.getListenerId().toString());
				}
								
			} else {	
				throw new SystemException("Listener is not registered for this resource.",ThingsCodes.INFO,"listener",listener.getListenerId().toString());			
			}

		} else {
			throw new SystemException("Resource is already revoked.");
		}
	}
	
	private void priviledged_revokedListener(ResourceListener listener) throws SystemException {
		
		// Validate
		if (listener == null) throw new SystemException("Listener is null.");
		
		// Make sure the resource is not revoked
		if (myResource!=null) {
			
			if (listenerSet.contains(myResource)) {
				
				// Process revocation first.
				try {
					listener.resourceRevoked(myId);
				} catch (InterruptedException ie) {	
					throw new SystemException("resourceRevoked was interrupted by the system.",ThingsCodes.SYSTEM_ERROR_COMPONENT_INTERRUPTED,ie,"listener",listener.getListenerId().toString());
				} catch (Throwable t) {
					throw new SystemException("Error from the listner while reporting revoked.",ThingsCodes.INFO,t,"listener",listener.getListenerId().toString());
				}
								
			} else {	
				throw new SystemException("Listener is not registered for this resource.",ThingsCodes.INFO,"listener",listener.getListenerId().toString());			
			}

		} else {
			throw new SystemException("Resource is already revoked.");
		}
	}
	
	// ===============================================================================
	// VERBOSE INTERFACE
	
	/**
	 * Turn on.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
    	debugPoster = poster;
    }
    
	/**
	 * Turn it off
	 */
	public void verboseOff() {
		debugPoster = null;
	}
	
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation may find a 
	 * way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message){
		if (debugPoster != null) debugPoster.postit(message);
	}
	
	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose() {
		 if (debugPoster==null) return false;
		 return true;
	}
	
	// ===============================================================================
	// ThingsReportingThreshold INTERFACE
	
	/**
	 * It will set the threshold for reporting.  What this means to the components may be different, but 
	 * any internal exceptions below the threshold will likely be propagated out of the component.
	 * @param threshold Threshold level.  This should be a numeric value from ThingsCodes.  The one word levels should be good enough, such as WARNING, ERROR, etc.
	 * @see things.common.ThingsCodes
	 */  
    public void set(int		threshold) {
    	reportingThreshold = threshold;
    }
	
}
