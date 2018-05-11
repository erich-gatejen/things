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

import things.common.StringPoster;
import things.common.ThingsException;
import things.data.NV;

/**
 * An implementation for expressing test results.  It will express them as text to
 * a provided StringPoster.  Note that all colons in the message or name will be replaced
 * with an underscore to ensure the colon is a field separator.
 * <p>
 * This expresser does not support more than 99 levels.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 19 JUN 04
 * </pre> 
 */
public class ResultExpresserText extends ResultExpresser {

	// PRIVATE FIELDS
	StringPoster poster = null;
	
	public void init(StringPoster posterIn) {
		poster = posterIn;
	}
	
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
	public void express(
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
	) throws ThingsException {

		// Make sure init was called
		if (poster==null) ThingsException.softwareProblem("ResultsExpressionText used express() before init() was called.");
	
		try {
			
			// All colons will be replaced
			String newMessage = TestCommon.normalizeNames(message);
			
			// Build it
			StringBuffer report = new StringBuffer(date + ":"); 
				
			String typeString = "XXXX";

			// Append tabs
			for (int i=0; i<index; i++) report.append("    :");
			
			// Unique per type
			switch(type) {
			case GROUP:
				typeString = ResultExpresser.expressionType.GROUP.getToken();
				report.append(String.format("%s:%s:%s:PASS=%d,FAIL=%d,INCO=%d,EXCP=%d,ABRT=%d:%s",
						typeString,theResult.getShortResult(),name,
						numberPass,numberFail,numberInconclusive,numberException,numberAbort,newMessage));
				break;
				
			case TEST:
				typeString = ResultExpresser.expressionType.TEST.getToken();
				report.append(String.format("%s:%s:%s:PASS=%d,FAIL=%d,INCO=%d,EXCP=%d,ABRT=%d:%s",
						typeString,theResult.getShortResult(),name,
						numberPass,numberFail,numberInconclusive,numberException,numberAbort,newMessage));
				break;
				
			case CASE:
				typeString = ResultExpresser.expressionType.CASE.getToken();
				report.append(String.format("%s:%s:%s:%s",
						typeString,theResult.getShortResult(),name,
						newMessage));
	
				break;
				
			default:
				ThingsException.softwareProblem("expressionType unexpected in express().  type="+type);
			}
			
			// Additions
			if (time>=0) report.append(":time=" + time);
			if (size>0) report.append(":size=" + size);
			if (code!=ResultExpresser.NO_CODE) report.append(":code=" + code);
			if (values!=ResultExpresser.NO_LIST) {
				int step=0;
				report.append(":");
				for (NV value : values) {
					// This cheesy step valuable is to make sure we put commas between fields.
					if (step>0) report.append(',');
					report.append(value.getName() + "=" + value.getValue());
					step++;
				}
			}
			
			// Post it
			poster.post(report.toString());
			
		} catch (ThingsException te) {
			throw te;
		} catch (Throwable ee) {
			throw new ThingsException("Could not express with ResultExpresserText because of a spurious exception.  message=" + ee.getMessage(),
					ThingsException.SYSTEM_ERROR, ee);	 
		} 
	}

	
}