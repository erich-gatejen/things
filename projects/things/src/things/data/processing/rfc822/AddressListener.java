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
package things.data.processing.rfc822;

/**
 * An address listener for what will come out of the AddressParser.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 05
 * </pre> 
 */
public interface AddressListener {

	/**
	 * Push an address.  Any element not present will be passed as null.  The content is up to the implementation.  There maybe be whitespace at
	 * either end of the strings--trim if you want.
	 * @param address an 822 complient address.
	 * @param friendly a friendly name or address.  This is optional.
	 * @param group a group name.  This is optional.
	 * @param busted a busted address component.
	 */
	public void push(String address, String friendly, String group, String busted) throws Throwable;

	/**
	 * Just add an add
	 * @param item
	 * @return return true if an original address.  Return false if it is a duplicate address.  it's up to the implementation to decide what is a duplicate.
	 */
	public boolean add(FullAddress item) throws Throwable;
	
}


