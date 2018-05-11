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
package things.thinger.kernel.basic;

import things.common.ThingsConstants;
import things.common.WhoAmI;

/**
 * Naming reactors specific to KernelBasic.  This ensures consistent naming.
 * <p>
 * I used this far less than I should have to have made it useful.  Perhaps this is why a mixin is 
 * a good addition to a language.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 APR 06
 * </pre> 
*/
public class KernelBasic_NamingReactors implements KernelBasic_Constants {


	/*
	 * Get the same resource name for a logger.
	 */
	public static final String rLOGGER_RESOURCE_ID(WhoAmI owner) {
		return new String((owner.toString() + ThingsConstants.PATH_SEPARATOR + RESOURCENAME_LOGGER_PREFIX));
	}

	/*
	 * Get the same resource name for a logger.
	 */
	public static final String rLOGGER_NAMED_RESOURCE_ID(String name) {
		return new String(name + ThingsConstants.PATH_SEPARATOR + RESOURCENAME_LOGGER_PREFIX);
	}


	/*
	 * Get the same resource name for a logger.
	 */
	public static final String rLOGGER_LOCATION(WhoAmI owner) {
		return new String(USER_LOG_PREFIX + owner.toTag() + LOG_SUFFIX);
	}
	
}
