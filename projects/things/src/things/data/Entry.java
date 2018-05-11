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

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.data.impl.ReadWriteableAttributes;

/**
 * An entry is an a data with attributes but no identity.  This is used for logging or data exchange.  They 
 * are timestamped at creation.
 * <p>
 * This component has been quite a showcase of where Java Generics fail.  Ultimately, this may be what 
 * drives me back to C++ or somewhere else.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Rewrite from another project - 22 MAY 04
 * </pre> 
 */
public class Entry extends Nubblet {

	final static long serialVersionUID = 1;
	
	/**
	 * Attributes, if needed.
	 */
	public AttributeReader attributes = ReadWriteableAttributes.permanentlyEmptyAttribute;

	/**
	 *  Default constructor.  Default priority of ROUTINE.  Null object.
	 *  Generic type.  Timestamped.
	 */
	public Entry() throws Throwable {
		ThingsException.softwareProblem("Entry() instantiated with default constructor.");
	}

	/**
	 * Constructor.  Sets everything but the text, priority, and the numeric.
	 * @param p the priority
	 * @param n the numeric
	 * @param text the text information for the entry
	 * @throws things.common.ThingsException
	 */
	public Entry(Priority p, int n, String text) {
		super(Type.ENTRY,p,n,text);
	}
	
	/**
	 * Constructor.  Sets everything but the text, priority, and the numeric.
	 * @param p the priority
	 * @param n the numeric
	 * @param text the text information for the entry
	 * @param a attributes.  Cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader 
	 */
	public Entry(Priority p, int n, String text, AttributeReader a) throws ThingsException {
		super(Type.ENTRY,p,n,text);

		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;	
	}
	
	/**
	 * Constructor.  Sets everything but the text and the numeric.
	 * @param n the numeric
	 * @param text the text information for the entry
	 * @throws things.common.ThingsException
	 */
	public Entry(int n, String text) {
		super(Type.ENTRY,Priority.ROUTINE,n,text);
	}
	
	/**
	 * Constructor.  Sets everything but the text and the numeric.
	 * @param n the numeric
	 * @param text the text information for the entry
	 * @param a attributes.  Cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader 
	 */
	public Entry(int n, String text, AttributeReader a) throws ThingsException {
		super(Type.ENTRY,Priority.ROUTINE,n,text);
		
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;	
	}
	
	/**
	 * Constructor.  Sets everything but the text.
	 * @param text the text information for the entry
	 * @throws things.common.ThingsException
	 */
	public Entry(String text) throws ThingsException {
		super(Type.ENTRY,Priority.ROUTINE,ThingsCodes.DEFAULT_NUMERIC,text);
	}
	
	/**
	 * Constructor.  Sets everything but the text.
	 * @param text the text information for the entry
	 * @param a attributes.  Cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 */
	public Entry(String text, AttributeReader a) throws ThingsException {
		super(Type.ENTRY,Priority.ROUTINE,ThingsCodes.DEFAULT_NUMERIC,text);
		
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;			
	}
	
	/**
	 * Constructor.  Explicit.  This can morph the entry into another type.
	 * @param text the text information for the entry
	 * @param type the type.
	 * @param p priority 
	 * @param n numeric
	 * @throws things.common.ThingsException
	 */
	public Entry(String text, Type type, Priority p, int n) throws ThingsException {
		super(type,p,n,text);
	}
	
	/**
	 * Constructor.  Explicit.  This can morph the entry into another type.
	 * @param text the text information for the entry
	 * @param type the type.
	 * @param p priority 
	 * @param n numeric
	 * @param a attributes.  Cannot be null.
	 * @throws things.common.ThingsException
	 * @see things.data.AttributeReader
	 */
	public Entry(String text, Type type, Priority p, int n, AttributeReader a) throws ThingsException {
		super(type,p,n,text);
		
		if (a==null) throw new ThingsException("Cannot set attribute object as null.", ThingsException.DATA_ATTRIBUTE_OBJECT_NULL);
		attributes = a;
	}
	
	// =============================================================================================================================
	// == OVERRIDES
		
	/**
	 * Get the attributes.
	 * @return the attributes associated with the Data, if any.
	 */
	public AttributeReader getAttributes() {
		return attributes;
	}
	
	// =============================================================================================================================
	// == ORGANIC
	
	/**
	 * Get a textual representation of this entry with attributes.
	 * @return the text.
	 */
	 public String toString() {
		 String attr = "";
		 try {
			attr =  AttributeCodec.encode2String(attributes.getAttributes());
		 } catch (Throwable e) {
			 // Dont' care.
		 }
		 return super.toString() + " Attributes:" + attr;
	 }
	
	/**
	 * Get a textual representation of this entry without attributes.
	 * @return the text.
	 */
	 public String toText() {
		 return super.toString();
	 }
	 	
}
