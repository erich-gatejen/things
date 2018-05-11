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
package things.thinger.io.conduits;

import things.data.Nubblet;
import things.thinger.SystemException;

/**
 * Interface to a conduit.
 * <p>
 * Drains are registered and deregistered.  Push drains will call the drain object from a system owned Thread.  Pull drains are polled
 * from outside the system Threads.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 28 JUN 05
 * </pre> 
 */
public interface Conduit {
	
	/**
	 * A nothing nubblet is the same as a null.
	 */
	public final static Nubblet NOTHING = null;
	
    /**
     * The InjectorType specifies how it will behave, as follows:<br>
     * UNSPECIFIED: No preference.  It will likely broadcast.<br>
	 * BROADCAST: Do not block.  Return a receipt for collection.<br>
     * REQUIRE_FIRST_DRAIN: Block until the first drain is complete.   Return a receipt for delivery.<br>
	 * REQUIRE_ALL_DRAIN: Block until all drains are complete.   Return a receipt for delivery.<br>
     */   	
	public enum InjectorType {
		UNSPECIFIED,
		BROADCAST,
		REQUIRE_FIRST_DRAIN,
		REQUIRE_ALL_DRAIN
	}
	
    /**
     * Initialize the Conduit.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException;
	
    /**
     * Get the id.  This will be as the local system sees it.  It cannot be otherwise transported to another system and used
     * as is.  
     * @return The Conduit ID.
     */   
    public ConduitID getId();
    
    /**
     * This will get an injector to the conduit.
     * @param theType The type of injector to get.  If the underlying implementation does not support that type, it should throw a ThingsException. 
     * @throws things.thinger.SystemException
     * @return An injector.
     */   
    public Injector getInjector(InjectorType theType) throws SystemException;
  
    /**
     * This will get an injector to the conduit, named.
     * @param theType The type of injector to get.  If the underlying implementation does not support that type, it should throw a ThingsException.
     * @param name the name of the Injector. 
     * @throws things.thinger.SystemException
     * @return An injector.
     */   
    public Injector getInjector(InjectorType theType, String name) throws SystemException;
    
    /**
     * Explicitly dispose of an Injector.
     * @param theInjector The Injector object to dispose. 
     * @throws things.thinger.SystemException
     */   
    public void disposeInjector(Injector theInjector) throws SystemException;
    
    /**
     * Register a PullDrainContainer instance with the Conduit.
     * @param theDrain The drain to register. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PullDrainContainer
     */ 
    public void registerPullDrain(PullDrainContainer	theDrain) throws SystemException;
    
    /**
     * Deregister a PullDrainContainer instance with the Conduit.  This will not empty any queued items.
     * @param theDrain The drain to deregister. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PullDrainContainer
     */ 
    public void deRegisterPullDrain(PullDrainContainer	theDrain) throws SystemException;
   
    /**
     * Register a PushDrain instance with the Conduit.  It may be immeadiately subject to pushed items.
     * @param theDrain The drain to register. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PushDrain
     */ 
    public void registerPushDrain(PushDrain	theDrain) throws SystemException;

    /**
     * Deregister a PushDrain instance with the Conduit.  If the Conduit is in the middle of a push, it may block this call until done.
     * @param theDrain The drain to deregister. 
     * @throws things.thinger.SystemException
     * @see things.thinger.io.conduits.PushDrain
     */ 
    public void deRegisterPushDrain(PushDrain	theDrain) throws SystemException;
    
}
