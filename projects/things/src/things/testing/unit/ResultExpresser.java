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
package things.testing.unit;

import java.util.List;

import things.common.ThingsException;
import things.data.NV;

/**
 * Interface for expressing test results.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 JUN 04
 * </pre> 
 */
public abstract class ResultExpresser {

	public enum expressionType {
		GROUP(0), TEST(1), CASE(2); 
		private final int value;   
		private expressionType(int v) { this.value = v; }
		final static private String token[] = { "GRP ", "TEST", "CASE" };
		public String getToken(){return token[value];}

	}
	
	// Constants
	static final int NO_NUMERIC_RESULT = -1;
	static final String NO_CODE = null;
	static final List<NV>	NO_LIST = null;
	
	/**
	 * Submit a full expression.
	 * @param date	Date in string format.
	 * @param type  The type as an expresisonType
	 * @param theResult The result as Result
	 * @param index The index ply of this result (useful for nesting groups).
	 * @param name The name of test.
	 * @param message The message from the result.
	 * @param numberPass Number of passes (as a roll-up if a group).
	 * @param numberFail Number of fails (as a roll-up if a group).
	 * @param numberInconclusive Number of inconclusive (as a roll-up if a group).
	 * @param numberException Number of exception (as a roll-up if a group).
	 * @param numberAbort Number of abort (as a roll-up if a group).
	 * @param time Time in miliseconds to run.  Negative number means no result.
	 * @param size Size of the resulting data.  Negative number means no reault.
	 * @param code Error or success code from the operation (as a String).  null means no code.
	 * @param values A list of name/value pairs.
	 * @see things.testing.unit.Result
	 * @throws things.common.ThingsException
	 */
	public abstract void express(
			String	date,
			expressionType	type,
			Result	theResult,
			int		index,
			String	name,
			String	message,
			int		numberPass,
			int		numberFail,
			int 	numberInconclusive,
			int 	numberException,
			int		numberAbort,
			long	time,
			long	size,
			String	code,
			List<NV>	values
	) throws ThingsException;
	
	// The following are convenience cases
	
	/**
	 * Submit a CASE expression.
	 * @param date	Date in string format.
	 * @param theResult The result as Result
	 * @param index The index ply of this result (useful for nesting groups).
	 * @param name The name of test.
	 * @param message The message from the result.
	 * @param time Time in miliseconds to run.  Negative number means no result.
	 * @param size Size of the resulting data.  Negative number means no reault.
	 * @param code Error or success code from the operation (as a String).  null means no code.
	 * @param values A list of name/value pairs.
	 * @see things.testing.unit.Result
	 * @throws things.common.ThingsException
	 */
	public void expressCase(
			String	date,
			Result	theResult,
			int		index,
			String	name,
			String	message,
			long	time,
			long	size,
			String	code,
			List<NV>	values) throws ThingsException {
		
		switch (theResult) {
		case ABORT: 
			this.express(date,expressionType.CASE,theResult,index,name,message,0,0,0,0,1,time,size,code,values);
			break;
		case PASS: 
			this.express(date,expressionType.CASE,theResult,index,name,message,1,0,0,0,0,time,size,code,values);
			break;
		case FAIL: 
			this.express(date,expressionType.CASE,theResult,index,name,message,0,1,0,0,0,time,size,code,values);
			break;
		case EXCEPTION: 
			this.express(date,expressionType.CASE,theResult,index,name,message,0,0,1,0,0,time,size,code,values);
			break;	
		case INCONCLUSIVE: 
			this.express(date,expressionType.CASE,theResult,index,name,message,0,0,0,1,0,time,size,code,values);
			break;				
		default:		
			this.express(date,expressionType.CASE,theResult,index,name,message,0,0,0,0,0,time,size,code,values);
			break;
		} // end switch
	}
	
	/**
	 * Submit a simple CASE expression.
	 * @param date	Date in string format.
	 * @param theResult The result as Result
	 * @param index The index ply of this result (useful for nesting groups).
	 * @param name The name of test.
	 * @param message The message from the result.
	 * @see things.testing.unit.Result
	 * @throws things.common.ThingsException
	 */
	public void expressCaseSimple(
			String	date,
			Result	theResult,
			int		index,
			String	name,
			String	message) throws ThingsException {
			this.expressCase(date,theResult,index,name,message,NO_NUMERIC_RESULT,NO_NUMERIC_RESULT,NO_CODE,NO_LIST);
	}
}