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

import java.util.Hashtable;

import things.common.WhoAmI;
import things.thinger.SystemException;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitController;
import things.thinger.io.conduits.ConduitID;

/**
 * A Basic conduit controller.  If the conduit doesn't exist, it will be created.  BasicConduits are intended to last the life of the server, so
 * don't make them when you expect to dispose them.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre>  
 */
public class BasicConduitController implements ConduitController {

	// ===============================================================================================
	// DATA
	
	/**
	 * Table of conduits.
	 */
	private Hashtable<String,BasicConduit>	 conduits;
	
	
	// ===============================================================================================
	// METHODS
	
	/**
	 * Default constructor.
	 */
	public BasicConduitController() {
		conduits = new Hashtable<String,BasicConduit>();
	}
	
    /**
     * This will attempt to use a common name to find a specific conduit.  you will
     * need to tell the Conduit system who you are with a WhoAmI.
     * @param channel A conduit ID.
     * @param callerId Your WhoAmI.
     * @throws things.thinger.SystemException
     * @return A ConduitID if successful, otherwise null.
     */   
    public Conduit tune(ConduitID channel, WhoAmI   callerId) throws SystemException {
    	BasicConduit result = null;
    	
    	// I'm going to synchronize this so we don't accidently create it twice.
    	synchronized(conduits) {
	    	if (conduits.containsKey(channel.toString())) {
	    		result = conduits.get(channel.toString());
	    	} else {
	    		result = new BasicConduit();
	    		result.init( channel );
	    		conduits.put(channel.toString(),result);
	    	}
    	}
    	return result;
    }
    
}
