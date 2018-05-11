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
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsUtilityBelt;
import things.common.WhoAmI;
import things.data.impl.ReadWriteableAttributes;

/**
 * An entry is with object data and attributes and and has an identity.<br>
 * The metadata and attributes are immutable, but the object data is not.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 12 MAY 06
 * </pre> 
 */
public class Entity<O> implements Data  {

	final static long serialVersionUID = 1;
	
	// =================================================================================
	// DATA
	private WhoAmI myId;
	private WhoAmI myCreatorId;	
	private long stamp;
	private O myThing;
	private int numeric;
	private Priority myPriority;	
	private Type myType;
	
	// Private data
	private static WhoAmI cachedNobody = new IAmNobody();
	
	// =================================================================================
	// FIELDS
	
	/**
	 * Attributes, if needed.
	 */
	public AttributeReader attributes = ReadWriteableAttributes.permanentlyEmptyAttribute;

	// =================================================================================
	// METHODS
	
	/**
	 *  The default constructor is not allowed.
	 */
	public Entity() throws Throwable {
		ThingsException.softwareProblem("Entry() instantiated with default constructor.");
	}

	/**
	 * Constructor.  Sets everything but the text, priority, and the numeric.
	 * @param n the numeric
	 * @param thing The thing to keep
	 */
	public Entity(int n, O thing) {	
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = Priority.ROUTINE;
		myType = Type.GENERIC;
		myId = cachedNobody;
		myCreatorId = cachedNobody;
		
		// Stamp the time.
		stamp = System.currentTimeMillis();
	}
	
	/**
	 * Constructor.  Sets everything but the text, priority, and the numeric.
	 * @param n the numeric
	 * @param thing The thing to keep.
	 * @param a attributes.  This cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 */
	public Entity(int n, O thing, AttributeReader a) throws ThingsException {	
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = Priority.ROUTINE;
		myType = Type.GENERIC;
		myId = cachedNobody;
		myCreatorId = cachedNobody;
		
		// Stamp the time.
		stamp = System.currentTimeMillis();
		
		// Attributes
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;	
	}	
	
	/**
	 * Constructor.  Sets everything but the text, priority, and the numeric.
	 * @param n the numeric
	 * @param thing The thing to keep.
	 * @param a attributes in name value pairs.  This cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 */
	public Entity(int n, O thing, String... a) throws ThingsException {	
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = Priority.ROUTINE;
		myType = Type.GENERIC;
		myId = cachedNobody;
		myCreatorId = cachedNobody;
		
		// Stamp the time.
		stamp = System.currentTimeMillis();
		
		// Attributes
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		ReadWriteableAttributes tempAttributes = new ReadWriteableAttributes();
		tempAttributes.addMultiAttributes(a);
		attributes = tempAttributes;
	}
	
	/**
	 * Constructor.  Sets everything except attributes.  Note, if either WhoAmI is null, they will be stuffed with a Nobody id.
	 * @param n the numeric
	 * @param t the type
	 * @param p the priority
	 * @param thing The thing to keep
	 * @param imposedId The id of the new Entity.
	 * @param creatorId The if of the caller (or other creator).
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 * @see things.common.WhoAmI
	 * @see things.common.IAmNobody
	 */
	public Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId) throws ThingsException {
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = p;
		myType = t;
		myId = imposedId;
		myCreatorId = creatorId;
		
		// Fix ids?
		if (myId == null) myId = cachedNobody;
		if (myCreatorId == null) myCreatorId = cachedNobody;	
		
		// Stamp the time.
		stamp = System.currentTimeMillis();
	}
		
	/**
	 * Constructor.  Sets everything.  Note, if either WhoAmI is null, they will be stuffed with a Nobody id.
	 * @param n the numeric
	 * @param t the type
	 * @param p the priority
	 * @param thing The thing to keep
	 * @param imposedId The id of the new Entity.
	 * @param creatorId The if of the caller (or other creator).
	 * @param a attributes.  This cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 * @see things.common.WhoAmI
	 * @see things.common.IAmNobody
	 */
	public Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a) throws ThingsException {
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = p;
		myType = t;
		myId = imposedId;
		myCreatorId = creatorId;
		
		// Fix ids?
		if (myId == null) myId = cachedNobody;
		if (myCreatorId == null) myCreatorId = cachedNobody;	
		
		// Stamp the time.
		stamp = System.currentTimeMillis();
		
		// Attributes
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;	
	}

	/**
	 * Constructor.  Sets everything.  Note, if either WhoAmI is null, they will be stuffed with a Nobody id.
	 * @param n the numeric
	 * @param t the type
	 * @param p the priority
	 * @param thing The thing to keep
	 * @param imposedId The id of the new Entity.
	 * @param creatorId The if of the caller (or other creator).
	 * @param a attributes in name value pairs.  This cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 * @see things.common.WhoAmI
	 * @see things.common.IAmNobody
	 */
	public Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a) throws ThingsException {
		
		// Set fields
		numeric = n;
		myThing = thing;
		myPriority = p;
		myType = t;
		myId = imposedId;
		myCreatorId = creatorId;
		
		// Fix ids?
		if (myId == null) myId = cachedNobody;
		if (myCreatorId == null) myCreatorId = cachedNobody;	
		
		// Stamp the time.
		stamp = System.currentTimeMillis();

		// Attributes
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		ReadWriteableAttributes tempAttributes = new ReadWriteableAttributes();
		tempAttributes.addMultiAttributes(a);
		attributes = tempAttributes;
	}
	
	// =============================================================================================================================
	// == ORGANIC
	
	/**
	 * Get the thing (Object).
	 * @return the thing
	 */
	public O getTypedThing() {
		return myThing;
	}
	
	
	// ===============================================================================================================================
	// == DATA INTERFACE
	
	/**
	 * Create a child ID using the given name.
	 * @param childsName the given name for the child.
	 * @return the textual representation of the ID.
	 * @see things.common.WhoAmI
	 */
	 public WhoAmI birthMyChild(String  childsName) {
		return myId.birthMyChild(childsName);
	 }

	/**
	 * Create a child ID using the given name and tag.  It must yield the same ID if the same value is used for childsName.  
	 * @param childsName the given name for the child.
	 * @param childsTag the tag for the child.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	 public WhoAmI birthMyChild(String  childsName, String childsTag) {
			return myId.birthMyChild(childsName,childsTag);
	 }
	 
	/**
	 * Give a textual representation of the data.
	 * @return the textual representation.
	 */
	 public String toString() {
		 StringBuffer result = new StringBuffer();

		 try {
			 result.append("TIMESTAMP: ");
			 result.append(ThingsUtilityBelt.timestampFormatterDDDHHMMSS(getStamp()));
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);
			 result.append("NUMERIC: ");
			 result.append(numeric);
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);
			 result.append("TYPE: ");
			 result.append(myType.name());
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);
			 result.append("PRIORITY: ");
			 result.append(myPriority.name());
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);
			 result.append("SOURCE ID: ");
			 result.append(myCreatorId.toString());
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);			 
			 result.append("ATTRIBUTES: ");
			 result.append(AttributeCodec.encode2String(attributes.getAttributes()));
			 result.append(ThingsConstants.CHEAP_LINESEPARATOR);

		 } catch (Throwable e) {
			 // Dont' care.
		 }
		 return result.toString();
	 }

	/**
	 * Get the objects ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getID() {
		return myId;
	}
	 
	/**
	 * Get the creator's ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getCreatorID() {
		return myCreatorId;
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
		return myPriority;
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
		return myThing;
	}
	
	/**
	 * Get the type.
	 * @return the type
	 */
	public Type getType() {
		return myType;
	}
	
	/**
	 * Get the attributes.
	 * @return the attributes associated with the Data, if any.
	 */
	public AttributeReader getAttributes() {
		return attributes;
	}
	
}
