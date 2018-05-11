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
package things.common.commands;

import java.util.ArrayList;
import java.util.HashMap;

import things.common.ThingsException;
import things.data.NVImmutable;

/**
 * A parsed command line.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 07
 * </pre> 
 */
public class CommandLine { 

	// =====================================================================================================================
	// == PARAMETER FIELD TYPES

	public enum PARAMETER_TYPES {

		/**
		 * A value gviven as a name/value pair.
		 */
		VALUE,
		
		/**
		 * An entity.  It is an unnamed, ordered value.
		 */
		ENTITY,
		
		/**
		 * An option.  It is a single character that is either present or not present.
		 */
		OPTION
	}
	
	// =====================================================================================================================
	// == PARAMETER FIELDS
	
	/**
	 * Values processed as a map.  Remember, their names are always lowercase.
	 */
	private HashMap<String, NVImmutable> values;
	
	/**
	 * Entities in a list and accessible by number.
	 */
	private ArrayList<String> entities;
	
	/**
	 * Options processed as a map.
	 */
	private boolean[] options;
	
	
	// =====================================================================================================================
	// == METHODS

	/**
	 * The only constructor.  Let the CommandLineProcessor make these for you.
	 * @param valuesData The values.
	 * @param entitiesData The entities.
	 * @param optionsData The options map.
	 * @throws ThingsException
	 */
	public CommandLine(HashMap<String, NVImmutable> valuesData, ArrayList<String>  entitiesData, boolean[] optionsData) throws ThingsException {
		if ((valuesData==null)||(entitiesData==null)) ThingsException.softwareProblem("CommandLine cannot be instantiated with null values.");
		values = valuesData;
		entities = entitiesData;
		options = optionsData;
	}
	
	/**
	 * Get an entity as a String.  If the position is not valid, it'll return null (rather than an arrayoutofbounds).
	 * @param index in the list (counting from ZERO).
	 * @return The entity.
	 */
	public String getEntity(int index) {
		String result = null;
		if (index < entities.size()) result = entities.get(index);
		return result;
	}
	
	/**
	 * Does it have the entity at the index?  If the position is not valid, it'll return false (rather than throw arrayoutofbounds).
	 * @param index in the list (counting from ZERO).
	 * @return true if the entity exists.  
	 */
	public boolean hasEntity(int index) {
		if (index < entities.size()) return true;
		return false;
	}
	
	/**
	 * Get a value as an NVImmutable or null if it isn't present.
	 * @param name name of the value.  It is not case sensitive (normalized to lower-case).
	 * @return true if the value exists.  
	 */
	public boolean hasValue(String name) {
		if (name == null) return false;
		return values.containsKey(name.toLowerCase());
	}
		
	/**
	 * Get a value as an NVImmutable or null if it isn't present.
	 * @param name name of the value.  It is not case sensitive (normalized to lower-case).
	 * @return The value as an NVImmutable.
	 * @see things.data.NVImmutable
	 */
	public NVImmutable getValue(String name) {
		if (name == null) return null;
		return values.get(name.toLowerCase());
	}
	
	/**
	 * Check to see if the character is a set option.
	 * @param character
	 * @return true if it is, otherwise false.
	 */
	public boolean isOptionSet(int character) {
		if (character <= CommandLineProcessor.MAX_CHARACTER )
			return options[character];
		return false;
	}
	
	// =====================================================================================================================
	// == INTERNAL 
	
}