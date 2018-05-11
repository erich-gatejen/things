/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.thinger.kernel;

import things.thinger.SystemException;
import things.common.WhoAmI;

/**
 * A resource interface.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i><br>
 * <code>EPG - Initial - 01FEB06</code>
 */
public interface ResourceInterface {

	/** 
	 * The states are provided as a convenience.  The resource may or may not implement them.<br>
	 * <pre>
	 * NEW       : Instantiated but not initialized.<br>
	 * RUNNING   : Active and healthy.<br>
	 * UNHEALTHY : Active but not healthy.<br>
	 * DISPOSING : Being disposed.<br>
	 * DEAD      : Dead and gone.<br>
	 * </pre>
	 */
	public enum State { NEW, RUNNING, UNHEALTHY, DISPOSING, DEAD };
	
	// == NAMED CONSTANTS ===========================================================================
	public final static String	NULL_ID = null;
	
	// == KERNEL METHODS =============================================================================
	
	/**
	 * Initialize the resource.  This must be called before use.  It should be called by the Kernel.  The id must be immutable.  If
	 * the id should change, it could cause mayhem for the Kernel.  If the Kernel should ever detect a change, it should ruthlessly 
	 * deregister and destroy the resource.
	 * <p>
	 * @param id The id of the resource.
	 * @see things.common.WhoAmI
	 * @throws things.thinger.SystemException
	 */
	public void initResource(WhoAmI  id) throws SystemException;
	
	// == RESOURCE METHODS ===========================================================================
	
	/**
	 * This is how the system tells a resource he is about to be destroyed.  Typically, the system will allow the resource some time to 
	 * clean up, however the implementation should assume that the system can become impatient at any moment and summerily execute it.
	 * <br>
	 * This may be called asynchronously by the system at any time and by any thread, so a smart system would manage conflicts and thread issues.
	 * <p>
	 * @throws things.thinger.SystemException
	 */
	public void disposeResource() throws SystemException;
	
	/**
	 * Lock an object.  This is blocking.  Locks will prevent any other thread from accessing the resource.  However, the
	 * resource may not allow locks, and it will return false if the lock was disallowed.
	 * @return true if the lock was completed, false if locks are not allowed.
	 * @throws things.thinger.SystemException
	 */
	public boolean lock() throws SystemException;

	/**
	 * This call is only meaningful if the thread holds the lock.  The resource could choose to ignore it, but locking is not supported.
	 * <br>
	 * If locks are supported and the thread does not own the lock, the resource may throw an exception.
	 * <p>
	 * @throws things.thinger.SystemException
	 */
	public void unlock() throws SystemException;
	
	/**
	 * Get ID of the resource.  If the resource has not been initResource(), this must return a null.  Return anything else than a null or the exact WhoAmI that 
	 * was set during initResource() is likely to cause the Resource to be brutally killed by the Kernel.
	 * <p>
	 * @return WhoAmI
	 * @see things.common.WhoAmI
	 * @throws things.thinger.SystemException
	 */
	public WhoAmI getId() throws SystemException;
	
	/**
	 * Get the current state.
	 * <p>
	 * @return State
	 * @throws things.thinger.SystemException
	 */
	public State getState() throws SystemException;

}
