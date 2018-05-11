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
package things.thinger.io.conduits.basic;

import java.util.HashSet;

import things.thinger.SystemException;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.InjectionInterface;
import things.thinger.io.conduits.Injector;
import things.thinger.io.conduits.PullDrainContainer;
import things.thinger.io.conduits.PushDrain;

/**
 * Basic implementation of a conduit.
 * <p>
 * NOTES<br>
 * - Using or deregistering an already deregistered Injector will not cause any errors.<br> 
 * <p>
 * Drains are registered and deregistered.  Push drains will call the drain object from a system owned Thread.  Pull drains are polled
 * from outside the system Threads
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public class BasicConduit implements Conduit, InjectionInterface {
	
	// ===============================================================================================
	// DATA
	private ConduitID myId;						// Not NULL indicates the Conduit was initialized.
	private HashSet<BasicInjector> injectors;
	private HashSet<PullDrainContainer> pullDrains;
	private HashSet<PushDrain> pushDrains;
	
	private static final String		DRAIN_NAME="basic";
	
	// ===============================================================================================
	// METHODS
	
    /**
     * Initialize the Conduit.  This will be called by it's controller.  Any subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException{
    	myId = yourId;
		
		injectors = new HashSet<BasicInjector>();
		pullDrains = new HashSet<PullDrainContainer>();
		pushDrains = new HashSet<PushDrain>();
	}
	    
    /**
     * Get the id.
     * @return The Conduit ID.
     */   
    public ConduitID getId() {
    	return myId;
    }
	
    /**
     * This will get an injector to the conduit.
     * @param theType The type of injector to get.  If the underlying implementation does not support that type, it should throw a ThingsException. 
     * @throws things.thinger.SystemException
     * @return An injector.
     */   
    public synchronized Injector getInjector(InjectorType theType) throws SystemException {
    	
    	// bad hax0r.  Assumes the method is synchronized.
    	return this.getInjector(theType, Long.toString(System.currentTimeMillis()) );	
    }
    
    /**
     * This will get an injector to the conduit, named.
     * @param theType The type of injector to get.  If the underlying implementation does not support that type, it should throw a ThingsException.
     * @param name the name of the Injector 
     * @throws things.thinger.SystemException
     * @return An injector.
     */   
    public synchronized Injector getInjector(InjectorType theType, String name) throws SystemException {
    	checkIfInitialized();
    	
    	BasicInjector newInjector = new BasicInjector(this);
    	newInjector.init(myId.birthMyChild(name),theType);
    	injectors.add(newInjector);
    	return newInjector;	 	
    }
    
    /**
     * Explicitly dispose of an Injector.
     * @param theInjector The Injector object to dispose. 
     * @throws things.thinger.SystemException
     */   
    public void disposeInjector(Injector theInjector) throws SystemException {
    	checkIfInitialized();
    	
    	synchronized(injectors) {
    		if (injectors.contains(theInjector)) injectors.remove(theInjector);
    	}	
    }
    
    /**
     * Register a PullDrainContainer instance with the Conduit.
     * @param theDrain The drain to register. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PullDrainContainer
     */ 
    public void registerPullDrain(PullDrainContainer	theDrain) throws SystemException {
    	checkIfInitialized();
    	
    	theDrain.init(myId.birthMyChild(DRAIN_NAME));
    	synchronized (pullDrains) {
    		pullDrains.add(theDrain);
    	}
    }
    
    /**
     * Deregister a PullDrainContainer instance with the Conduit.  This will not empty any queued items.
     * @param theDrain The drain to deregister. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PullDrainContainer
     */ 
    public void deRegisterPullDrain(PullDrainContainer	theDrain) throws SystemException {
    	checkIfInitialized();
    	
    	synchronized(pullDrains) {
    		if (pullDrains.contains(theDrain)) pullDrains.remove(theDrain);
    	}	
    }
   
    /**
     * Register a PushDrain instance with the Conduit.  It may be immediately subject to pushed items.
     * @param theDrain The drain to register. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PushDrain
     */ 
    public void registerPushDrain(PushDrain	theDrain) throws SystemException {
    	checkIfInitialized();
    	
    	theDrain.init(myId.birthMyChild(DRAIN_NAME));
    	synchronized (pushDrains) {
    		pushDrains.add(theDrain);
    	}
    }

    /**
     * Deregister a PushDrain instance with the Conduit.  If the Conduit is in the middle of a push, it may block this call until done.
     * @param theDrain The drain to deregister. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PushDrain
     */ 
    public void deRegisterPushDrain(PushDrain	theDrain) throws SystemException {
    	checkIfInitialized();
    
    	synchronized(pushDrains) {
    		if (pushDrains.contains(theDrain)) pushDrains.remove(theDrain);
    	}	
    }
    
    // INJECTION INTERFACE =================================================
    /**
     * Get the Pull Drains (in their containers).  Access to the Set is not synchronized, as it need not be, but you should
     * be adding any new injectors through this interface.  Doing so will give undefined results.
     * @return A HashSet of PullDrainContainers.
     * @see things.thinger.io.conduits.PullDrainContainer
     * @throws things.thinger.SystemException
     * @see java.util.HashSet
     */   
    public HashSet<PullDrainContainer> getPullDrains() throws SystemException {
		if (myId == null) throw new SystemException("Conduit (id=" + myId.toString() + ") was not initalized before use of InjectionInterface (getPullDrains).",SystemException.IO_CONDUIT_FAULT_NOT_INITIALIZED);
    	return pullDrains;
    }
    
    /**
     * Get the Push Drains (in their containers).  Access to the Set is not synchronized, as it need not be, but you should
     * be adding any new injectors through this interface.  Doing so will give undefined results.
     * @return A HashSet of PushDrains.
     * @see things.thinger.io.conduits.PushDrain
     * @see java.util.HashSet
     * @throws things.thinger.SystemException
     */   
    public HashSet<PushDrain> getPushDrains() throws SystemException {
		if (myId == null) throw new SystemException("Conduit (id=" + myId.toString() + ") was not initalized before use of InjectionInterface (getPushDrains).",SystemException.IO_CONDUIT_FAULT_NOT_INITIALIZED);
    	return pushDrains;
    }
    
    
    // PRIVATE METHODS =====================================================
    
    private void checkIfInitialized() throws SystemException {
		if (myId == null) throw new SystemException("Conduit (id=null) was not initalized before use.",SystemException.IO_CONDUIT_FAULT_NOT_INITIALIZED);
    }

}
