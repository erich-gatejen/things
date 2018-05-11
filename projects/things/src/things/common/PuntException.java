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
 * A very simple PUNT.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 JAN 03
 * </pre>          
 */
public class PuntException extends Exception {

	final static long serialVersionUID = 1;
	
	public PuntException() {
		super("NO MESSAGE");
	}
	
	public PuntException(String message) {
		super(message);
	}
	
	public PuntException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
