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

import things.common.WhoAmI;
import things.thinger.SystemException;

/**
 * Interface to the conduit controller.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public interface ConduitController {

    /**
     * This will attempt to use a common name to find a specific conduit.  You will
     * need to tell the Conduit system who you are with a WhoAmI.
     * @param channel A conduit id.
     * @param callerId Your WhoAmI.
	 * @throws things.thinger.SystemException
     * @return A Conduit if successful, otherwise null.
     */   
    public Conduit tune(ConduitID channel, WhoAmI   callerId) throws SystemException;
    
}
