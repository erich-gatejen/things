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

package things.data;

import things.common.ThingsConstants;

/**
 * General property interface that defines the text format.
 * <p>
 * The general escape character is a question mark '?'.<br>
 * The amperstand '&amp;' will fold the next line.<br>
 * The # as the first non-whitespace character will dismiss the line as a commend.<br>
 * There should be a single naked (not-escaped) equal sign '=' to split the name from the value.<br>
 * Values can be separated (for a multi-value) with a naked comma ','.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 16 MAY 04
 * </pre> 
 */
public interface ThingsProperty  {
	
	public final static char PROPERTY_ESCAPE_CHARACTER = '?';	
	public final static char PROPERTY_COMMENT_CHARACTER = '#';	
	public final static char PROPERTY_PATH_SEPARATOR = '.';	
	public final static String PROPERTY_LINE_SEPARATOR_ASSTRING = ",";
	public final static char PROPERTY_LINE_SEPARATOR = ',';	
	public final static char PROPERTY_LINE_CONTINUATION = '&';	
	public final static String PROPERTY_LINE_CONTINUATION_STRING = "&";	
	public final static char PROPERTY_LINE_EQUALITY = ThingsConstants.CODEC_EQUALITY;
	public final static String PROPERTY_EMPTY = " ";
	public final static String PROPERTY_LINE_TERMINATION = "\r\n";
	public final static char PROPERTY_CR = '\r';
	public final static char PROPERTY_LF = '\n';
	public final static char PROPERTY_ESCAPED_CR = 'r';
	public final static char PROPERTY_ESCAPED_LF = 'n';
}
