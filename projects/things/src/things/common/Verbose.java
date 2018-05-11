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
 * Verbose interface.  It's a way to tell the subcomponent to be verbose about what 
 * it is doing.  This is useful for second-layer debugging and should not be used
 * by everything.  All modules and applications should use the event system instead.
 * However, this is a useful way to get information from the underlying system.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 1 JAN 02
 * </pre> 
 */
public interface Verbose {

	/**
	 * Turn on.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException;
    
	/**
	 * Turn off the verbose mode.
	 */
	public void verboseOff();
	
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation
	 * may find a  way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message);
	
	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose();
	
}
