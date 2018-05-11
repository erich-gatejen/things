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
package things.thinger.service.thing;

import things.data.Receipt;
import things.data.ThingsPropertyView;
import things.thing.RESULT;
import things.thinger.SystemException;

/**
 * Interface to a thinger.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 06
 * </pre> 
 */
public interface Thinger {
	
	/**
	 * This will issue a thing.  It returns a receipt for the issuance, not for the other all interaction.
	 * @param thingName The command to issue.
	 * @param localProperties local properties for the thing
	 * @throws things.thinger.SystemException
	 */
	public Receipt issueThing(String thingName, ThingsPropertyView localProperties) throws SystemException;
	
	/**
	 * Query the RESULT for a thing.  It maybe done or in progress.  
	 * @param thingReceipt The issuance receipt. 
	 * @return RESULT.
	 * @throws things.thinger.SystemException
	 */
	public RESULT queryResult(Receipt	thingReceipt) throws SystemException;
	
	/**
	 * Dispose of a thing.  Tell the system we are done with it regardless of status.  Note that it is 
	 * up to the specific implementations to either kill the thing or not.   Either way, this interface will lose all knowledge of it.
	 * @param thingReceipt The issuance receipt. 
	 * @throws things.thinger.SystemException
	 */
	public void dispose(Receipt	thingReceipt) throws SystemException;

	
}
