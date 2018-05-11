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

import java.io.StringReader;
import java.io.StringWriter;

import things.common.ThingsException;
import things.common.tools.StreamTools;
import things.data.ThingsPropertyViewReader;

/**
 * A page.  Pages use the StreamTools.merge for templating--the book ended question marks around property names.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 07
 * </pre> 
 */
public class Page  {

	// =================================================================================================
	// == INTERNAL DATA
	private String 	sourceString;
	private String 	pageName;
	
	// =================================================================================================
	// == METHODS


	/**
	 * Constructor.  Makes good for template work.
	 * @param pageName the page name.
	 * @param sourceString
	 * @throws Throwable
	 */
	public Page(String 	pageName, String sourceString) throws Throwable {
		if (sourceString==null) throw new ThingsException("Input source string cannot be null.", ThingsException.SERVICE_HTTPTOOL_PAGE_MERGE_FAILED_USAGE);
		this.pageName = pageName;
		this.sourceString = sourceString;
	}

	/**
	 * Run the process.
	 * @param tags tags for replacement.
	 * @return return the completed page.
	 * @throws Throwable
	 */
	public synchronized String process(ThingsPropertyViewReader tags) throws Throwable {
		if (tags==null) throw new ThingsException("Input tags cannot be null.", ThingsException.SERVICE_HTTPTOOL_PAGE_MERGE_FAILED_USAGE);
		StringWriter sw = new StringWriter();
		
		try {
			StreamTools.merge(new StringReader(sourceString), sw, tags);
		} catch (Throwable t) {
			throw new ThingsException("Failed to merge page.", ThingsException.SERVICE_HTTPTOOL_PAGE_MERGE_FAILED);
		}
		return sw.toString();
	}

	/**
	 * Get the page name.
	 * @return the name
	 */
	public String getPageName() {
		return pageName;
	}
	
}
