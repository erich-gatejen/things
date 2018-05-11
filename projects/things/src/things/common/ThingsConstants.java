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
 * Immutable constants of the universe, from plank's to PI.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 14 NOV 02
 * </pre> 
 */
public interface ThingsConstants {

	public final static int TRUE = 1;
	public final static int FALSE = 0;
	public final static boolean TROOF = true;
	public final static boolean WAYSA = false;	

	public final static char PATH_SEPARATOR = '/';	
	public final static String PATH_SEPARATOR_STRING = "/";	
	
	public final static char CODEC_SEPARATOR_CHARACTER = '|';
	public final static String CODEC_SEPARATOR_CHARACTER_ESCAPED = "||";
	public final static char CODEC_ESCAPE_CHARACTER = '\\';   			// The fricken windows paths makes this hard to use for property management
	public final static char CODEC_PROPERTY_ESCAPE_CHARACTER = '^';		// so use a carot for escaping property chars.
	public final static char CODEC_EQUALITY = '=';	
	public final static char CODEC_QUOTING = '"';	
	
	public final static char CR = '\r';
	public final static char LF = '\n';
	public final static String CRLF = "\r\n";
	public final static String NEWLINE = "\r\n";
	public final static String CHEAP_LINESEPARATOR = "\n";
	public final static String EMPTY_STRING = "";
	
	public final static String A_NOBODY = "nobody";
	public final static String A_NOTHING = "nothing";
	public final static String AN_UNKNOWN = "unknown";
	
	public final static String EPIC_FAIL = "Epic fail";

	public final static String COPYRIGHT_NOTICE = "Copyright (c) 2006, 2007, 2008 Erich P Gatejen.  All rights reserved.";
	
	/**
	 * System constants.
	 */
	public final static int KERNEL_LOCK_TRY_LIMIT_MILLIS = 4000;
	
	/** 
	 * Platform constants.
	 */
	public final static int FS_FILE_DELETE_RETRIES = 100;		// Number of times to try the delete operation before giving up.
	
	/** 
	 * Transport constants.
	 */
	public final static int DEFAULT_TRANSPORT_CONNECT_RETRIES = 100;
	public final static int DEFAULT_TRANSPORT_CONNECT_RETRY_DELAY = 1500;	// In milliseconds

}