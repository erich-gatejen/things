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
package things.data;

import things.common.IAmNobody;
import things.common.ThingsException;
import things.common.WhoAmI;
import things.data.impl.ReadWriteableAttributes;

/**
 * This is a nubblet, the simplest form of Data.  Nubblets have no creator.
 * <p>
 * @author Erich P. Gatejen<br>
 * @version 1.0<br>
 * <i>Version History</i><br>
 * <code>EPG - Rewrite from another - 22May04</code> 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 22 MAY 04
 * </pre> 
 */
public class Nubblet implements Data {

	final static long serialVersionUID = 1;

	/**
	 * time stamp
	 */
	private long stamp;

	/**
	 * time stamp
	 */
	private Type type;
	
	/**
	 * thing
	 */
	protected Object thing;

	/**
	 * Numeric
	 */
	public int numeric;
	
	/**
	 * Numeric
	 */
	public Priority priority;	
	
	// Private data
	private static WhoAmI cachedNobody = new IAmNobody();

	/**
	 *  Default constructor is not allowed.  Don't let the user grab the super by accident.
	 */
	public Nubblet() throws Throwable {
		ThingsException.softwareProblem("Nubbet() instantiated with default constructor.");
	}

	/**
	 * Constructor.  Sets everything but ID and timestamps it.
	 * @param t the type
	 * @param p the priority
	 * @param n the numeric
	 * @param o the object
	 */
	public Nubblet(Type t, Priority p, int n, Object o) {
		stamp = System.currentTimeMillis();
		type = t;
		priority = p;
		thing = o;
		numeric = n;
	}
	
	/**
	 * Create a child ID using the given name.  Nubblets cannot have children, so this will return an IAmNobody.
	 * @param childsName the given name for the child.
	 * @return the textual representation of the ID.
	 * @see things.common.IAmNobody
	 */
	 public WhoAmI birthMyChild(String  childsName) {
		 return cachedNobody;
	}

	/**
	 * Create a child ID using the given name and tag.  It must yield the same ID if the same value is used for childsName.  
	 * @param childsName the given name for the child.
	 * @param childsTag the tag for the child.
	 * @return the id
	 */
	 public WhoAmI birthMyChild(String  childsName, String childsTag) {
		 return cachedNobody;	 
	 }
	 
	/**
	 * Give a textual ID.
	 * @return the textual representation of the ID.
	 * @see things.common.WhoAmI
	 */
	 public String toString() {
		 return stamp + ":" + thing.hashCode();
	 }
	 
	/**
	 * Give a TAG version of the ID.  This cannot be used for positive ID, but may make a convenient mnemonic.  An implementation
	 * may return the same value as toString().
	 * @return the tag representation of the ID.
	 */
	 public String toTag()  {
		 return Integer.toString(thing.hashCode());
	 }

	/**
	 * Get the objects ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getID() {
		return cachedNobody;
	}
	 
	/**
	 * Get the creator's ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getCreatorID() {
		return cachedNobody;
	}

	/**
	 * Get the numeric value.  It is completely up to the setter as to what this means.
	 * @return value as an int
	 */
	public int getNumeric() {
		return numeric;
	}
	
	/**
	 * Get the numeric value.  It is completely up to the setter as to what this means.
	 * @return value as a string
	 */
	public String getNumericString() {
		return Integer.toString(numeric);
	}

	/**
	 * Get the priority.ed.
	 * @return the priority.
	 */
	public Priority getPriority() {
		return priority;
	}

	/**
	 * Get the timestamp.
	 * @return timestamp as a long
	 */
	public long getStamp() {
		return stamp;
	}

	/**
	 * Get the thing (Object).
	 * @return the thing
	 */
	public Object getThing() {
		return thing;
	}

	/**
	 * Get the type.
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Get the attributes.
	 * @return the attributes associated with the Data, if any.
	 */
	public AttributeReader getAttributes() {
		return ReadWriteableAttributes.permanentlyEmptyAttribute;
	}
}
