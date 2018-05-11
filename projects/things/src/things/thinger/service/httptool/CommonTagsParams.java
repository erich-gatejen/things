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
package things.thinger.service.httptool;

/**
 * Common merge tags for pages.
* <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * EPG - Add helper - 12 Nov 08
 * </pre> 
 */
public interface CommonTagsParams  {

	// =================================================================================================
	// == TAGS
	
	// SYSTEM - These may be overwritten by the system, so be careful.
	public final static String TAG_ERROR_DESCRIPTION = "error.description";
	public final static String TAG_ERROR_MESSAGE= "error.message";
	
	// SYSTEM - These may be overwritten by the system, so be careful.
	public final static String TAG_PROCESS_LIST	= "process.list";
	
	// HELPER
	public final static String TAG_HELPER__FOR = "helper.for";
	public final static String TAG_HELPER__NAME = "helper.name";
	public final static String TAG_HELPER__HELP = "helper.help";
	
	// =================================================================================================
	// == PARAMETERS
	
	// HELPER
	public final static String PARAM_HELPER__FOR = "FOR";
	public final static String PARAM_HELPER__NAME = "NAME";
	public final static String PARAM_HELPER__PAGE = "OPTIONAL";
	
}
