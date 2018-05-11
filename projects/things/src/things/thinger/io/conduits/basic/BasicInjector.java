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

import things.common.ThingsCodes;
import things.data.Data;
import things.data.ReceiptList;
import things.thinger.SystemException;
import things.thinger.io.conduits.Conduit;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.InjectionInterface;
import things.thinger.io.conduits.Injector;
import things.thinger.io.conduits.PullDrainContainer;
import things.thinger.io.conduits.PushDrain;

/**
 * A basic implementation of the conduit injector interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from autohit - 29 JUN 05
 * </pre> 
 */
public class BasicInjector implements Injector {

	// ===============================================================================================
	// DATA
	
	private Conduit.InjectorType  myType = Conduit.InjectorType.UNSPECIFIED;
	@SuppressWarnings("unused")
	private ConduitID myID = null;
	private InjectionInterface injectionIfx;
	
	
	// ===============================================================================================
	// METHODS
	
	// IMPLEMENTATION SPECIFIC INTERFACES
    /**
     * Initialize the Injector.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param ii InjectionInterface for the owner Conduit.
     * @see things.thinger.io.conduits.InjectionInterface
     */   
    public BasicInjector(InjectionInterface  ii) {
    	injectionIfx = ii;
    }
	
	// IMPLEMENTATIONS =================================================
	
    /**
     * Initialize the Injector.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this injector.
     * @param theType the type of controller.
	 * @see things.thinger.io.conduits.Conduit
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId, Conduit.InjectorType  theType) throws SystemException {
    	myID = yourId;
		myType = theType;
    }
	
	/**
	 * Post an item
	 * @throws things.thinger.SystemException
	 * @see things.data.Data
	 * @return a receipt list.
	 */
	public ReceiptList post(Data		item) throws SystemException {
		ReceiptList result = null;
		try {
			switch (myType) {
			case UNSPECIFIED:
			case BROADCAST:
				result = broadcast(item);
				break;
			case REQUIRE_FIRST_DRAIN:
				result = firstDrain(item);
				break;
			case REQUIRE_ALL_DRAIN:
				result = allDrain(item);
				break;
			default:
				SystemException.softwareProblem("BasicInjector.post encountered an unknown Conduit.InjectorType.  name=" + myType.toString()); 
			}
			
		} catch (NullPointerException ne) {
			SystemException.softwareProblem("BasicInjector was likely not init() before the first post().",ne); 
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Post failed.  message=" + t.getMessage(),ThingsCodes.IO_CONDUIT_FAULT_POST_FAILED,t);
		}
		return result;
	}
	
	/**
	 * Get the injector type.
	 * @return the type 
	 * @see things.thinger.io.conduits.Conduit
	 */
	public Conduit.InjectorType getMyType() {
		return myType;
	}	
	
	// PRIVATE METHODS =====================================

	/**
	 * Broadcast a data.
	 * @param n The data to broadcast.
	 * @return A receipt list.
	 * @see things.data.Data
	 * @throws SystemException
	 */
	private ReceiptList broadcast(Data n) throws SystemException {
		ReceiptList result = new ReceiptList();		
		
		try {
		
			// Pull drains first
			for (PullDrainContainer pull : injectionIfx.getPullDrains()) {
				result.add(pull.postListener(n));
			}
			
			// Then push drains
			for (PushDrain push : injectionIfx.getPushDrains()) {
				result.add(push.postListener(n));
			}		

		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Broadcast failed to uncaptured exception (might be a bug).", ThingsCodes.IO_CONDUIT_FAULT_POST_FAILED,t);
		}
		return result;
	}
	
	/**
	 * Post a nubblet and wait for the first drain.
	 * @param n The data to post.
	 * @return A receipt list.
	 * @see things.data.Data
	 * @throws SystemException
	 */
	private ReceiptList firstDrain(Data n) throws SystemException {
		ReceiptList result = new ReceiptList();		
		
		try {
		
			int drains = 0;
			
			// Make sure there are some injectors
			if ((injectionIfx.getPushDrains().size()<1)&&(injectionIfx.getPullDrains().size()<1)) throw new SystemException("Post to firstDrain injector failed because there are no drains registered on this conduit.",ThingsCodes.IO_CONDUIT_ERROR_POSTED_TO_NO_DRAINS);
			
			// Then push drains
			for (PushDrain push : injectionIfx.getPushDrains()) {
				result.add(push.postListener(n));
				drains++;
			}		
			
			// Pull drains first
			for (PullDrainContainer pull : injectionIfx.getPullDrains()) {
				result.add(pull.postListener(n));
			}
			
			while (drains < 1) {
				for (PullDrainContainer pull : injectionIfx.getPullDrains()) {
					if (pull.isDrained(n)) {
						drains++;
						break;
					} // end if		
				} // end for	
			} // end while

		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("FirstDrain failed to uncaptured exception (might be a bug).", ThingsCodes.IO_CONDUIT_FAULT_POST_FAILED, t );
		}
		return result;
	}
	
	/**
	 * Post a nubblet.  Wait until they all drain.
	 * @param n The data to post.
	 * @return A receipt list.
	 * @see things.data.Data
	 * @throws SystemException
	 */
	private ReceiptList allDrain(Data n) throws SystemException {
		ReceiptList result = new ReceiptList();		
		
		try {
		
			int drainsLeft = injectionIfx.getPushDrains().size() + injectionIfx.getPullDrains().size();
			
			// Make sure there are some injectors
			if (drainsLeft < 1) throw new SystemException("Post to allDrain injector failed because there are no drains registered on this conduit.", ThingsCodes.IO_CONDUIT_ERROR_POSTED_TO_NO_DRAINS);
			
			// Then push drains
			for (PushDrain push : injectionIfx.getPushDrains()) {
				result.add(push.postListener(n));
				drainsLeft--;
			}		
			
			// Until there are no more drains.  This could take a while.
			while (drainsLeft > 0) {
				for (PullDrainContainer pull : injectionIfx.getPullDrains()) {
					if (pull.isDrained(n)) {
						drainsLeft--;
						break;
					} else {
						pull.waitForDrain();	
					} // end if
					
				} // end for	
				
			} // end while

		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("AllDrain failed to uncaptured exception (might be a bug).", ThingsCodes.IO_CONDUIT_FAULT_POST_FAILED,t);
		}
		return result;
	}
				
}
