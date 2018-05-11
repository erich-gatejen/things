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

import java.util.HashSet;

import things.thinger.SystemException;

/**
 * System-level interface into the conduit.  This is how an Injector will drive the conduit thought the drains.  Most consumers need not use this.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 9 OCT 05
 * </pre> 
 */
public interface InjectionInterface {
   
    /**
     * Get the Pull Drains (in their containers).  Access to the Set is not synchronized, as it need not be, but you should
     * be adding any new injectors through this interface.  Doing so will give undefined results.
     * @return A HashSet of PullDrainContainers.
     * @see things.thinger.io.conduits.PullDrainContainer
     * @see java.util.HashSet
     * @throws things.thinger.SystemException
     */   
    public HashSet<PullDrainContainer> getPullDrains() throws SystemException ;
    
    /**
     * Get the Push Drains (in their containers).  Access to the Set is not synchronized, as it need not be, but you should
     * be adding any new injectors through this interface.  Doing so will give undefined results.
     * @return A HashSet of PushDrains.
     * @see things.thinger.io.conduits.PushDrain
     * @see java.util.HashSet
     * @throws things.thinger.SystemException
     */   
    public HashSet<PushDrain> getPushDrains() throws SystemException ;
    
}
