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
package things.thing;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.Attributes;

/**
 * Metrics values.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 26 FEB 07
 * </pre> 
 */
public class Metrics  {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA
	
	/**
	 * Stock instances for convenience.
	 */
	public static Metrics STOCK_Pass = new Metrics(1, 0, 0, 0, 1, 0, 0, 0, 0);
	public static Metrics STOCK_Fail = new Metrics(1, 0, 0, 0, 0, 1, 0, 0, 0);
	public static Metrics STOCK_Inconclusive = new Metrics(1, 0, 0, 0, 0, 0, 1, 0, 0);
	public static Metrics STOCK_Abort = new Metrics(1, 0, 0, 0, 0, 0, 0, 1, 0);
	public static Metrics STOCK_Exception = new Metrics(1, 0, 0, 0, 0, 0, 0, 0, 1);
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == FIELDS

	/**
	 * Integer counter.
	 */
	public int	count;
	
	/**
	 * Time taken in milliseconds.
	 */
	public long		millis;
	
 	/**
 	 * Difference.  A difference value.  Use is up to the implementation.
 	 */	
	public long	difference;
	
	/**
	 * Case counts
	 */
	public int	cases;
	public int	pass;
	public int  fail;
	public int	inconclusive;
	public int 	abort;
	public int  exception;
	
	// ====================================================================================================================================
	// ===================================================================================================================================
	// == METHODS.  
	
	/**
	 * Default constructor.
	 */
	public Metrics() {
		// NOP
	}
	
	/**
	 * Full constructor.
	 * @param count
	 * @param millis
	 * @param difference
	 * @param cases
	 * @param pass
	 * @param fail
	 * @param inconclusive
	 * @param abort
	 * @param exception
	 */
	public Metrics(int count, long millis, long difference, int cases, int pass, int fail, int inconclusive, int abort, int exception) {
		this.count = count;
		this.millis = millis;
		this.difference = difference;
		this.cases = cases;
		this.pass = pass;
		this.fail = fail;
		this.inconclusive = inconclusive;
		this.abort = abort;
		this.exception = exception;
	}
	
	/**
	 * Write as attributes.
	 * @param attr where to the attributes.
	 * @throws ThingsExeption
	 */
	public void writeAsAttributes(Attributes attr) throws ThingsException {
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_COUNT, Integer.toString(count));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_MILLIS, Long.toString(millis));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_DIFFERENCE, Long.toString(difference));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_CASES, Integer.toString(cases));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_PASS, Integer.toString(pass));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_FAIL, Integer.toString(fail));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_INCONCLUSIVE, Integer.toString(inconclusive));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_ABORT, Integer.toString(abort));
		attr.addAttribute(ThingsNamespace.ATTR_METRIC_EXCEPTION, Integer.toString(exception));
	}
	
	/**
	 * Accumulate.  Susceptible to overflow.
	 * @param metrics the metrics to add to this one.
	 */
	public void accumulate(Metrics metrics) {
		this.count += metrics.count;
		this.millis += metrics.millis;
		this.difference += metrics.difference;
		this.pass += metrics.pass;
		this.fail += metrics.fail;
		this.inconclusive += metrics.inconclusive;
		this.abort += metrics.abort;
		this.exception += metrics.exception;
	}	
	
	
}
