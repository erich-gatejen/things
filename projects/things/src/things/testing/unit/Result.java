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

/**
 * A generic result enumeration.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <br>
 * <code></code>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial  - 30 NOV 04
 * EPG - Re-write - 18 JUN 05
 * </pre>   
 */
public enum Result {
	WAITING(0),
	COMMENT(1),
	PASS(2),
	FAIL(3),
	INCONCLUSIVE(4),
	EXCEPTION(5),
	ABORT(6);	
	
	// Private managers
	private final int value;   
	private Result(int v) { this.value = v; }
	final static private String shortNames[] = { "WAIT", "###", "PASS", "FAIL", "INCO", "EXCP", "ABRT" };
	public String getShortResult(){return shortNames[value];}
	final static private String longNames[] = {
		"Waiting for run", "# Comment #", "Passed", "Failed", "Inconclusive", "Generated an exception", "Aborted on purpose" };
	public String getLongResult() {return longNames[value];};
}
	
