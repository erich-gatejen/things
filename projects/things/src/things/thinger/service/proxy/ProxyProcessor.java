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
package things.thinger.service.proxy;


/**
 *A proxy processor interface.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request.  This was part of a stand-alone lib for a while. - 10 DEC 08
 * </pre> 
 */
public interface ProxyProcessor {
	
	// ===================================================================================================
	// DATA

	// ===================================================================================================
	// METHODS

	/**
	 * The processor.
	 * @param context the context.
	 * @throws Throwable for any problem.
	 */
	public void process(ProxyContext context) throws Throwable;
	
}
