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
package things.thinger.service.httptool.stock;

/**
 * Common names for the stock implementation,
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * EPG - Add tags - 12 Sep 08
 * </pre> 
 */
public interface StockConstants {

	// ========================================================================================================
 	// == PAGES
	
	public static final String	PAGE_ERROR = "error.html";
	
	public static final String ACTION_MAIN = "MAIN";
	public static final String ACTION_MAIN_CLASS = "things.thinger.service.httptool.stock.Action_Main";
	public static final String ACTION_MAIN__PAGE = "index.html";
	
	public static final String ACTION_PROCESSLIST = "PS";
	public static final String ACTION_PROCESSLIST_CLASS = "things.thinger.service.httptool.stock.Action_ProcessList";
	public static final String ACTION_PROCESSLIST__PAGE = "processlist.html";
	
	public static final String ACTION_HELPER = "HELPER";
	public static final String ACTION_HELPER_CLASS = "things.thinger.service.httptool.stock.Action_Helper";
	public static final String ACTION_HELPER__PAGE = "helper.html"; 
	
	
}