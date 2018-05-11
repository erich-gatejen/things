/**
 * THINGS/THINGER 2004
 * Copyright Erich P Gatejen (c) 2004, 2005  ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.testing.unit;

import things.common.ThingsUtilityBelt;

/**
 * Common tools.<br>
 * group logger > date:GRP:    :name:<br>
 * test logger  > date:   :TEST:name:<br>
 *  
 * 
 * 
 * 
 * group message > date:GRP:name<br>
 * test message  > date:   :TEST:resu:name:PASS=#,FAIL=#,INCO=#,EXCP=#,ABRT=#<br>
 * case message  > date:   :    :CASE:resu:name:#:message<br>
 * comment msg   > date:COMMENT:<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <br>
 *          <code>EPG - Initial - 30 NOV 04</code>
 */
public class TestCommon {
	
	/**
	 * Logger for a test.
	 * <p>
	 * @param name group name
	 * @return a properly formatted message.
	 */
	public static String formTestLog(String name, String msg) {
		if ((name==null)||(msg==null)) return "NULL";
		String scrubMessage = normalizeNames(msg);
		String scrubName = normalizeNames(name);
		return new String(ThingsUtilityBelt.timestampFormatterHHMMSS() + 
		        ":logT:" + scrubName + ":" + scrubMessage);		
	}

	/**
	 * Logger for a group.
	 * <p>
	 * @param name group name
	 * @return a properly formatted message.
	 */
	public static String formGroupLog(String name, String msg) {
		if ((name==null)||(msg==null)) return "NULL";
		String scrubMessage = normalizeNames(msg);
		String scrubName = normalizeNames(name);
		return new String(ThingsUtilityBelt.timestampFormatterHHMMSS() + 
		        ":logG:" + scrubName + ":" + scrubMessage);		
	}
	
	/**
	 * Normalize names all the same way.  Basically, replace any colons with underbars.
	 * <p>
	 * @param in what to normalize
	 * @return the normalized.  Nulls will be returned as nulls;
	 */
	public static String normalizeNames(String in) {
		if (in==null) return null;
			return in.replace(':','_');
	}
	
}