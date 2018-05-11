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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import things.common.ThingsConstants;
import things.thinger.SystemException;

/**
 * Static tools for the Kernel
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 OCT 04
 * </pre> 
 */
public class KernelTools {

	/**
	 *  Acquire the lock on the Kernel set time limit.  If it fails, it'll throw
	 *  an serious FAULT.
	 *  @param theLock the lock to try
	 *  @throws things.thinger.SystemException
	 *  @see java.util.concurrent.locks.Lock
	 */
	public static void lockOnTimer(Lock  theLock) throws SystemException {
		boolean lockResult = false;
		try {
			lockResult= theLock.tryLock(ThingsConstants.KERNEL_LOCK_TRY_LIMIT_MILLIS,TimeUnit.MILLISECONDS);
		} catch (InterruptedException ie) {
			throw new SystemException("Critical Kernel lock failed in lockOnTimer(), LOCK INTERRUPTED.",SystemException.KERNEL_FAULT_INTERNAL_LOCK_FAILURE,ie);
		} catch (Exception e) {
			throw new SystemException("Critical Kernel lock failed in lockOnTimer(), prossibly due to a Kernel or Kernel client bug.", SystemException.SYSTEM_FAULT_SOFTWARE_PROBLEM,e);
		}
		if (lockResult==false)throw new SystemException("Critical Kernel lock failed in lockOnTimer(), LOCK TIMEOUT.",SystemException.KERNEL_FAULT_INTERNAL_LOCK_FAILURE);
	}
	
	/**
	 *  Carefree unlock.  Don't care if there is any error.
	 *  @param theLock the lock to unlock
	 */
	public static void unlockCarefree(Lock  theLock) {
		try {
			theLock.unlock();
		} catch (Exception e) {
		}
	}
	
}
