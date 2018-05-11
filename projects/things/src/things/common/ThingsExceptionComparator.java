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

import java.util.Comparator;

/**
 * Compares ThingsExceptions based in their numerics.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 05
 * </pre> 
 */
public class ThingsExceptionComparator implements Comparator<ThingsException> {

		// -- INTERFACE IMPLEMENTATION -----------------------
	
		/**
		 * Compare.  It will use the numeric as the value.  A lower number is
		 * considered a worse exception.
		 * @param o1 exception 1
		 * @param o2 exception 2
		 * @return sort order compare.
		 */
		public int compare(ThingsException o1, ThingsException o2) {
            	if (o1==null) {
            		if (o2==null) return 0;
            		return 1;
            	}
            	if (o2==null) return -1;
            	return o1.numeric - o2.numeric;
        }
        
}
