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
package things.thinger.io;

import things.common.ThingsException;

/**
 * File System tools.  Static tools for all types of file systems.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 JUN05
 * </pre> 
 */
public class FileSystemTools {

    /**
     * This is the assumed mount root for any filesystem.
     */
	public static final String ROOT = "/";
	
    /**
     * This will normalize the path to make sure it doesn't refer to non-filesystem paths.  A null will result in a root path.  Any other exceptions will propagate.
     * @param in the path to normalize
     * @return the normalize path
     * @throws Exception
     */   
    public static String normalizePath(String in) throws ThingsException {
    	String result = null;
    	int count = 0;
    	try {
    		StringBuffer accumulator = new StringBuffer();
        	
    		// Make sure the path starts with a slash
    		if ((in.charAt(0)!='/')&&(in.charAt(0)!='\\'))accumulator.append('/');
        	
    		// Scrub
    		for (int rover =0; rover < in.length(); rover++) {
    			if ((in.charAt(rover)=='\\')||(in.charAt(rover)=='/')) {
    				if (count==0) {
    					count++;
    					accumulator.append('/');
    				} 
    			} else {
    				accumulator.append(in.charAt(rover));
    				count = 0;
    			}
    		}
    		result =  accumulator.toString();
    		
    	} catch (NullPointerException e) {
    		// Exceptions mean a problem and thus return a null.
    	}
    	return result;
    } 
	
	
}
