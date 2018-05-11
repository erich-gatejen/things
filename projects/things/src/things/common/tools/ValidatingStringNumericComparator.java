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
package things.common.tools;

import java.util.Comparator;

/**
 * A validating string numeric compare.  Compare the numeric value of two Integer strings.  If the numbers 
 * cannt be parsed or bigger than an integer, it will throw a ClassCastException.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 AUG 07
 * </pre> 
 */
public class ValidatingStringNumericComparator implements Comparator<String> {
	
	/**
	 * Compare two string String that should be integers represented by strings.  If either is not a valid integer, it will throw a exception.
	 * @param o1 the first object to be compared
	 * @param o2 the second object to be compared
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
	 * @throws ClassCastException if either string cannot be resolved to an integer.
	 */
	public int compare(String o1, String o2) {
		
		//Validate
		int o1Value;
		int o2Value;
		try {
			o1Value = Integer.parseInt(o1);
		} catch (Throwable t) {
			throw new ClassCastException("Bad left value.  value=" + o1 + " message=" + t.getMessage());
		}
		try {
			o2Value = Integer.parseInt(o2);
		} catch (Throwable t) {
			throw new ClassCastException("Bad left value.  value=" + 02 + " message=" + t.getMessage());
		}
		
		// Compare
		return (o1Value - o2Value);
	}
	
	public boolean equals(Object obj) {
		return true;
	}
	
}