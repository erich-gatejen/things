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
package things.universe;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Universe object address.
 * <p>
 * This defines a UADDY format, which expressed the address as a string:<br>
 * [UNIVERENAME]:[OBJECT_PATH]<br>
 * [UNIVERENAME] = The registered local universe name.<br>
 * [OBJECT_PATH] = The path to the object.<br>
 * Neither value is validated.  It only looks for the ':';
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 SEP 07
 * </pre> 
 */
public class UniverseAddress implements Serializable {

	public static final long serialVersionUID=1;
	
	/**
	 * Our internal split pattern.
	 */
	private Pattern p = Pattern.compile(UADDY_SEPARATOR);
	
	/**
	 * Separator between the name and the path.
	 */
	public final static String UADDY_SEPARATOR = ":";
	
	/**
	 * The local universe name.
	 */
	public String  universeName;
	
	/**
	 * The path into the universe.
	 */
	public String  path;
	
	/**
	 * Construct.
	 * @param uname The local universe name.
	 * @param upath The path to the object.
	 * @throws Throwable if null parameters.
	 */
	public UniverseAddress(String uname, String upath) throws Throwable {
		
		// Validate
		if ((uname==null)||(upath==null)) throw new Exception("Malformed uaddy.  Null term.");
		
		// Trim and save
		universeName = uname.trim();
		path = upath.trim();
	}

	/**
	 * Construct using an UADDY
	 * @param uaddy The string UADDY
	 * @throws Throwable if malformed.
	 */
	public UniverseAddress(String uaddy) throws Throwable {
		if (uaddy==null) throw new Exception("Null uaddy not allowed in UniverseAddress(String uaddy)");
		
		// Find the split.
		String[] splitted = p.split(uaddy);
		
		// Validate
		if (splitted.length != 2) throw new Exception("Malformed uaddy.  There must be a single " + UADDY_SEPARATOR + " character to separate uname from path.");
		if ((splitted[0]==null)||(splitted[1]==null)) throw new Exception("Malformed uaddy.  Null term.");
		
		// Trim and save
		universeName = splitted[0].trim();
		path = splitted[1].trim();
	}

	public String getUaddy() {
		return new String(universeName + UADDY_SEPARATOR + path);
	}

}