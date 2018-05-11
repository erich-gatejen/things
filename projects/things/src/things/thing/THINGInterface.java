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
package things.thing;

import things.universe.Universe;

/**
 * A thing interface.<br>
 * This can allow non-THING objects access to a THING's services.  It's a bad idea for a non-THING to do anything from a THING 
 * that is not in this interface.
 *<p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 26 FEB 07
 * EPG - Add RUN_TIME - 10 MAR 08
 * </pre> 
 */
public interface THINGInterface extends BASEInterface {
	
	/**
	 * Instantiate a module.  It will seek and create the one as named and init it.  You'll have to cast it to the specific class if
	 * you want to access subclass methods.  
	 * @param moduleName the name of the module.  This must match the class name for the MODULE or it will not be found and run.
	 * @return the MODULE 
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public MODULE INSTANCE(String moduleName) throws UserException, InterruptedException;
	
	/**
	 * Call a THING.  A new instance of it will be created.
	 * @param thingName the name of the thing.  This must match the class name for the THING or it will not be found and run.
	 * @return the RESULT of the call.
	 * @throws UserException or InterruptedException.  It's important to let InterruptedException out.
	 */
	public RESULT CALL(String thingName) throws UserException, InterruptedException;
	
	/**
	 * Express a result.  It is up to the environment as to where it will be expressed, be it to the local log or across the wire.
	 * It is up to the kernel to make this reliable and there will be no receipt for it.  If you absolutely must be certain that your
	 * receipt made it somewhere, use a channel.
	 */
	public void EXPRESS(RESULT  theResult) throws UserException;
	
	/**
	 * Get a universe by the local name.
	 * @param name the local name (as registered)
	 * @return the universe
	 * @throws UserException
	 * @see things.universe.Universe
	 */
	public Universe GET_UNIVERSE(String name) throws UserException;
	
	/**
	 * Get how long the thing has been running in milliseconds.  This is not how much processing time has accumulated,
	 * but from the moment it started running until it stopped, pauses and whatever included.
	 * @return milliseconds.
	 */
	public long RUN_TIME() throws UserException;

}
