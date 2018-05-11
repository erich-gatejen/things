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

import things.common.PuntException;
import things.data.ThingsPropertyView;
import things.thinger.SystemInterface;

/**
 * A base action.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 17 NOV 07
 * </pre> 
 */
public abstract class Action  {

	// =================================================================================================
	// == DATA
	
	/**
	 * Working variables.  Since only one thread is allowed to process at a time, these variables
	 * will be properly set for the one running.
	 */
	public SystemInterface si;
	
	/**
	 * This is what will finally be merged to the result page.
	 */
	public ThingsPropertyView  tags;
	
	/**
	 * This is what was passed in the command.
	 */
	public ThingsPropertyView  parameters;
	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	/**
	 * The implementation.  Assume the DATA is available.
	 */
	protected abstract ActionResult process() throws Throwable;
	

	// =================================================================================================
	// == OVERLOADABLE METHODS
	
	/**
	 * Overload this if you want to manage HEAD processing on your own.  Be careful with these!  They will not render a default page if the
	 * head processing fails.
	 * @param parameters This is what comes from the commands.
	 * @param tags This is what will be merged to the result page.
	 * @param si The system interface.
	 * @return the head.  The default implementation returns a null which will let the server manage it (and almost always invalidate the cache).
	 * @throws Throwable
	 */
	public Head head(ThingsPropertyView  parameters, ThingsPropertyView  tags, SystemInterface si) throws Throwable {
		return null;
	}
	
	// =================================================================================================
	// == METHODS
	
	/**
	 * Call this, not process.
	 * @param parameters This is what comes from the commands.
	 * @param tags This is what will be merged to the result page.
	 * @param si The system interface.
	 * @return the action result.
	 * @throws Throwable
	 */
	public synchronized ActionResult execute(ThingsPropertyView  parameters, ThingsPropertyView  tags, SystemInterface si) throws Throwable {
		this.tags = tags;
		this.si = si;
		this.parameters = parameters;
		return process();
	}
	
	// =================================================================================================
	// == TOOLS
	
	/**
	 * Call this if you get an error and just give up.  The system will render the error for you.
	 * @param message the full message.
	 * @param description the short description. 
	 * @throws ActionException which will be trapped and processed by the service.
	 */
	public static void ERROR(String  description, String  message) throws ActionException {
		throw new ActionException(description, message);
	}
	
	/**
	 * Call this if you want to punt.  This is usually something you want to catch before it makes it back to the action manager.
	 * @param message the full message.
	 * @throws ActionException which will be trapped and processed by the service.
	 * @see things.common.PuntException
	 */
	public static void PUNT(String  message) throws PuntException {
		throw new PuntException(message);
	}
	
}
