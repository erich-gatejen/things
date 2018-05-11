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

import java.util.Collection;
import java.util.List;

import things.common.ThingsException;
import things.common.ThingsExceptionBundle;
import things.common.ThingsNamespace;
import things.data.Data;
import things.data.Data.Type;

/**
 * Manage RESULTS.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public abstract class ResultManager {

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS	
	
	/**
	 * Calculate the result based the inherit goodness of the individual results, as captured in Type.  Ignore their definitions.  
	 * The metrics will be a complete rollup all RESULT metrics, not just case by case.  So that means the count could be significantly higher than the number of RESULTS.
	 * There are the following rules:<br>
	 * All results must be PASS for the final result to PASS.<br>
	 * A single ABORT will make the final result an ABORT, otherwise:<br>
	 * A single EXCEPTION will make the final result an EXCEPTION, otherwise:<br>
	 * a single FAIL will make the final result a FAIL, otherwise:<br>
	 * a single INCONCLUSIVE will make the final result a FAIL.<p>
	 * @param results a collection of results.
	 * @param exceptions any exceptions.  This may be null or empty.
	 * @return the overall result.
	 * @throws UserException
	 */
	public static RESULT calculateResult(List<RESULT> results, ThingsExceptionBundle<ThingsException> exceptions) throws UserException {
		RESULT finalResult;
		Metrics	resultMetrics = new Metrics();
		Metrics	totalMetrics = new Metrics();
		
		try { 
			
			// Are there any defined results?
			if (results.size() > 0) {
			
				// Roll them up.
				for (RESULT result : results ) {
					totalMetrics.accumulate(result.getTypedThing());
					
					// It assumes result.actual will never be null (should at least be StockResult.WAITING.
					switch(result.getType()) {

					// Not expected.
					case METRIC:
					case INFO:
					case WAITING:
						// TODO: technically these are fail cases, since it is supposed to be pass or fail.  For now treat as inconclusive.
					case INCONCLUSIVE:			
						resultMetrics.inconclusive++; break;
					case PASS:
						resultMetrics.pass++; break;
					case FAIL:
						resultMetrics.fail++; break;
					case EXCEPTION:
						resultMetrics.exception++; break;				
					case ABORT:
						resultMetrics.abort++; break;
					}
					
				} // Roll up results.
			
			}	
			
			// Add any exceptions
			if ((exceptions!=null)&&(exceptions.size() > 0)) {
				resultMetrics.exception += exceptions.size();
			}
			
			// determine the rollup
			Data.Type	specificResult  = Data.Type.PASS;
			if (resultMetrics.abort > 0) specificResult  = Data.Type.ABORT;
			else if (resultMetrics.exception > 0) specificResult  = Data.Type.EXCEPTION;
			else if (resultMetrics.fail > 0) specificResult  = Data.Type.FAIL;
			else if (resultMetrics.inconclusive > 0) specificResult  = Data.Type.FAIL;
							
			// Force result.
			if ((exceptions!=null)&&(exceptions.size() > 0)) {
				finalResult = new RESULT(totalMetrics, specificResult, ThingsNamespace.ATTR_MESSAGE, exceptions.resolve().toStringCauses());
			} else {
				finalResult = new RESULT(totalMetrics, specificResult, ThingsNamespace.ATTR_MESSAGE, "SUCCESS");
			}
			
		} catch (Throwable t) {
			throw new UserException("Failed to calculate result due to spurious error.", UserException.ERROR_THING_RESULT_CALCULATION_FAILED, t);
		}
		return finalResult;
	}
	
	/**
	 * Calculate the result based on the defined results and their settings.  There are the following rules:<br>
	 * All results must be PASS for the final result to PASS.<br>
	 * A single ABORT will make the final result an ABORT, otherwise:<br>
	 * A single EXCEPTION will make the final result an EXCEPTION, otherwise:<br>
	 * a single FAIL will make the final result a FAIL, otherwise:<br>
	 * a single INCONCLUSIVE will make the final result a FAIL.<p>
	 * @param results a collection of results.
	 * @param exceptions any exceptions.  This may be null or empty.
	 * @return the overall result.
	 * @throws UserException
	 */
	public static RESULT calculateResult(Collection<ResultDefinition> results, ThingsExceptionBundle<ThingsException> exceptions) throws UserException {
		RESULT finalResult;
		Metrics	myMetrics = new Metrics();
		
		try { 
			
			// Are there any defined results?
			if (results.size() > 0) {
			
				// Roll them up.
				for (ResultDefinition result : results ) {
					
					// Process by catagory ---------------------------------------------------------------------
					myMetrics.count++;
					switch(result.expectation) {
					
					case PASS_FAIL:
						// process based result ------------------------------------------------------
						myMetrics.cases++;
						
						// It assumes result.actual will never be null (should at least be StockResult.WAITING.
						switch(result.actual.getType()) {
	
						// Not expected.
						case METRIC:
						case INFO:
						case WAITING:
							// TODO: technically these are fail cases, since it is supposed to be pass or fail.  For now treat as inconclusive.
						case INCONCLUSIVE:			
							myMetrics.inconclusive++; break;
						case PASS:
							myMetrics.pass++; break;
						case FAIL:
							myMetrics.fail++; break;
						case EXCEPTION:
							myMetrics.exception++; break;				
						case ABORT:
							myMetrics.abort++; break;
	
						} // END process based result ---------------------------------------------------
						
					case INFO:		
						break;
						
					case METRIC:
						break;
						
					} // END process by catagory ----------------------------------------------------------------------
				}
				
				// determine the rollup
				Data.Type	specificResult  = Data.Type.PASS;
				if (myMetrics.abort > 0) specificResult  = Data.Type.ABORT;
				else if (myMetrics.exception > 0) specificResult  = Data.Type.EXCEPTION;
				else if (myMetrics.fail > 0) specificResult  = Data.Type.FAIL;
				else if (myMetrics.inconclusive > 0) specificResult  = Data.Type.FAIL;
								
				finalResult = new RESULT(myMetrics, specificResult);
				
			} else {
				// Nothing defined.  So were there any exceptions?
				if ((exceptions!=null)&&(exceptions.size() > 0)) {
					// Yes, it's an exception.	
					finalResult = new RESULT(new Metrics(1, 0, 0, 0, 0, 0, 0, 0, 1), Type.EXCEPTION, ThingsNamespace.ATTR_MESSAGE, exceptions.resolve().toStringCauses());
					
				} else {
					// No, it's a pass.
					finalResult = new RESULT(new Metrics(1, 0, 0, 0, 1, 0, 0, 0, 0), Type.PASS);
				}
			}
			
		} catch (Throwable t) {
			throw new UserException("Failed to calculate result due to spurious error.", UserException.ERROR_THING_RESULT_CALCULATION_FAILED, t);
		}
		return finalResult;
	}

	
}
