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

import java.util.PriorityQueue;

/**
 * Creates a bundle of exceptions and methods for accessing them.  You can use this 
 * to collect errors without interrupting processing.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 MAY 05
 * </pre> 
 */
public class ThingsExceptionBundle<E extends ThingsException> {

	final static long serialVersionUID = 1;
	
	// =================================================================================
	// == DATA
	
	/**
	 * The registered exceptions held in a queue.  they will be prioritized by severity, which is
	 * determined by the ThingsException numeric.  The lower the numeric, the worse the exception.
	 * @see things.common.ThingsExceptionComparator
	 */
	private PriorityQueue<E>	queue;

	// =================================================================================
	// == METHODS
	
	/**
	 * Default Constructor.
	 */
	public ThingsExceptionBundle() {	
		queue = new PriorityQueue<E>(2,new ThingsExceptionComparator());
	}

	/**
	 * Add an exception.
	 * @param exp The exception to add.
	 */
	public void add(E exp) {
		queue.add(exp);
	}

	/** 
	 * Resolve to single exception.  If there are no exceptions in the list, it'll return a null.  If 
	 * there is only one in the list, it will return the one.  If there are more than one, it will 
	 * return the worse exception (numerically) with the other exceptions as attributes.  Generally, the 
	 * attributes will be made unique so none are lost.
	 * @return The single exception or null if there are none.
	 */
	public E resolve() {
		
		// Trivial cases
		if (queue.size()<=0) return null;
		if (queue.size()==1) return queue.peek();
		
		// Get primary
		E result = queue.peek();
		
		// Add others as attributes
		int index = 1;
		for (E current : queue) {
			result.addAttribute(ThingsNamespace.ATTR_REASON_NUMBER + index,current.toStringSimple());
			index++;
		}
		
		return result;
	}
	
	/** 
	 * Resolve to single exception and throws it.  See resolve() for the method.  If there is no exception
	 * in the bundle, it quietly returns.
	 * @throws E the resolved exception.
	 */
	public void throwResolved() throws E {
		E result = resolve();
		if (result != null) throw result;
	}
	
	/**
	 * Get the number of exceptions in the bundle.
	 * @return the number (or zero if there are none).
	 */
	public int size() {
		return queue.size();
	}
}
