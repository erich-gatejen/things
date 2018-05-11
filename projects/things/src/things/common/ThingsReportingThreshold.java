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
package things.common;

/**
 * ThingsReportingThreshold interface.  It's a way to tell the subcomponent what it's reporting threshold 
 * should be for exception and error reporting and propagation.
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 JUL 05
 * </pre> 
 */
public interface ThingsReportingThreshold {

	/**
	 * It will set the threshold for reporting.  What this means to the components may be different, but 
	 * any internal exceptions below the threshold will likely be propagated out of the component.
	 * @param threshold Threshold level.  This should be a numeric value from ThingsCodes.  The one word levels should be good enough, such as WARNING, ERROR, etc.
	 * @see things.common.ThingsCodes
	 */  
    public void set(int		threshold);

}
