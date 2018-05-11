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
package test.system.testtools;

import things.thinger.service.local.CLIService;

/**
 * A CLI Service  wrapper.  This will let us inject command lines directly into the CLIService.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 6 FEB 07
 * </pre> 
 */
public class CLIServiceWrapper extends CLIService {
	
	// ===================================================================================================
	// EXPOSED DATA
	
	/**
	 * The static reference to the service.  We're doing this so the testing system can access.  Eventually
	 * we'll move this behind the kernel for security.
	 */
	private static CLIService thisService;
		
	// ===================================================================================================
	// INTERNAL DATA
	
	// ===================================================================================================
	// CONSTRUCTOR
	public CLIServiceWrapper() {
		super();
		thisService = this;
	}
	
	// ==================================================================================================
	// METHODS
	
	public static CLIService getService() {
		return thisService;
	}
	
}
