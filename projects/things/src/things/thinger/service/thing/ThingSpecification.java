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

import things.common.ThingsException;
import things.data.ThingsPropertyView;

/**
 * A Thing specification.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 AUG 07
 * </pre> 
 */
public class ThingSpecification {

	public String					thingName;
	public ThingsPropertyView		localProperties;

	/**
	 * Construct a things specification.
	 * @param thingName
	 * @param localProperties
	 * @throws Throwable if anything is null or the name is empty.
	 */
	public ThingSpecification(String thingName, ThingsPropertyView localProperties) throws Throwable {
		if ((thingName==null)||(thingName.trim().length()<1)) throw new ThingsException("Thing name null or empty.", ThingsException.SERVICE_THINGER_BAD_CONSTRUCTION);
		if (localProperties==null) throw new ThingsException("Thing local Properties cannot be null.", ThingsException.SERVICE_THINGER_BAD_CONSTRUCTION);
		
		this.thingName = thingName;
		this.localProperties = localProperties;
	}
}
