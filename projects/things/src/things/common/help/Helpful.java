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
package things.common.help;


/**
 * Helpful interface.  It's a way to let a subcomponent be helpful.
 * <p>
 * Anything that implements should have a default constructor, since when the helpers are getting built, one may be instantiated.  For this process, it doesn't matter
 * if the constructor does nothing.  This is just another sucky java thing.  However, if you can't tolerate a default constructor, at least have a PUBLIC static instance of Helper.  the system
 * will try to grab that without creating and instance object.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 SEP 08
 * </pre> 
 */
public interface Helpful {

	/**
	 * Provide basic help as a string.  It should use ThingsMarkup for gimmicks.
	 * @see things.common.ThingsMarkup
	 * @return The text of the help or null if there is none.
	 */  
    public String help();
    
	/**
	 * Provide detailed information.  It should use ThingsMarkup for gimmicks.
	 * @see things.common.ThingsMarkup
	 * @return The text of the information or null if there is none.
	 */  
    public String information();
    
	/**
	 * Provide a helper for the help.  This allows you to create a richer set of property help.  It may return null if there is no more help.
	 * @return the helper
	 */  
    public Helper helper();
    
}
