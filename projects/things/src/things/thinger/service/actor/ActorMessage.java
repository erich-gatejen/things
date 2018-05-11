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
package things.thinger.service.actor;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.NV;
import things.data.NVImmutable;

/**
 * A message.<p>
 * MESSAGE = SEQUENCE,TYPE,NUMBER.PARAMETERS,NUMBER.ATTRIBUTES,PARAMETERS,ATTRIBUTES<br>
 * SEQUENCE = int (4-byte, jvm ordered)<br>
 * NUMBER.PARAMETERS = int (4-byte, jvm ordered), number of at PAIRs<br>
 * NUMBER.ATTRIBUTES = int (4-byte, jvm ordered), number of at PAIRs<br>
 * TYPE = STRING (for the message type)<br>
 * <p>
 * PARAMETERS = PAIR.1,PAIR.N<br>
 * NUMBER = int (4-byte, jvm ordered) representing the number of pairs<br>
 * PAIR.x = STRING,STRING (representing name/value)<br>
 * STRING = SIZE,BYTES<br>
 * SIZE = int (4-byte, jvm ordered)<br>
 * BYTES = byte[SIZE]<br>
 * <p>
 * This is not entirely thread safe.  You should add a parameter during load or save which could cause the messages to get munged.  I'm not sure
 * this would ever be used across multiple threads, so I won't synchronize it.  
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 MAY 07
 * </pre> 
 */
public class ActorMessage implements Serializable {

	/**
	 * Serialization version number.
	 */
	static final long serialVersionUID=1;

	/**
	 * The sequence number.
	 */
	public  int		sequence;
	
	/**
	 * The parameters.  If null, there are none.
	 */
	private HashMap<String,NVImmutable>	parameters;
	
	/**
	 * The attributes.  If null, there are none.
	 */
	private HashMap<String,NVImmutable>	attributes;	
	
	/**
	 * Type
	 */
	private String	type;
	
	/**
	 * Default constructor.
	 */
	public ActorMessage() {
		//Nothing
	}
	
	/**
	 * This is the convenience constructor.
	 * @param theSequence A sequence number.  This should be incremented for every order.
	 */
	public ActorMessage(int theSequence) {
		sequence = theSequence;
		type = ActorConstants.MESSAGE_TYPE_NONE;
	}
	
	/**
	 * Add a parameter.  It will be encoded into a PAIR.
	 * @param p A ready made parameter.  It will be treated as a single value.
	 * @throws Throwable
	 */
	public void addParamter(NVImmutable p) throws Throwable {
		if (p==null) ThingsException.softwareProblem("addParamter called with null parameter");
		if (p.getValue()==null)  ThingsException.softwareProblem("addParamter called with null value.  (Not sure how this could happen in the first place.)");
		
		if (parameters == null) {
			parameters = new HashMap<String,NVImmutable>();
		}
		parameters.put(p.getName(),p);
	}
	
	/**
	 * Add a parameter.  It will be encoded into a PAIR.  There are practical and safety limits to the 
	 * name and value String sizes.  Try to keep them under 2k.  If they are too big, they will be rejected by the distant end.
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * @throws Throwable
	 */
	public void addParamter(String name, String value)  throws Throwable  {
		if (name==null) ThingsException.softwareProblem("addParamter called with null name");
		if (value==null) ThingsException.softwareProblem("addParamter called with null value");
		
		NVImmutable p = new NV(name,value); 
		this.addParamter(p);
	}
	
	/**
	 * Get a parameter by name.
	 * @param name The parameter's name.  If null it will return null;
	 * @return The parameter's value or null (ActorConstants.NO_PARAMETER) if it isn't set.
	 */
	public String getParameter(String name) {
		if (name==null) return null;
		String response = ActorConstants.NO_PARAMETER;
		if (parameters!=null) {
			NVImmutable thang = parameters.get(name);
			if (thang != null) response = thang.getValue();
		}
		return response;
	}
	
	/**
	 * Add an attribute.  It will be encoded into a PAIR.
	 * @param p A ready made attribute.  It will be treated as a single value.
	 * @throws Throwable
	 */
	public void addAttribute(NVImmutable p) throws Throwable {
		if (p==null) ThingsException.softwareProblem("addAttribute called with null parameter");
		
		if (attributes == null) {
			attributes = new HashMap<String,NVImmutable>();
		}
		attributes.put(p.getName(),p);
	}
	
	/**
	 * Add an attribute.  It will be encoded into a PAIR.  There are practical and safety limits to the 
	 * name and value String sizes.  Try to keep them under 2k.  If they are too big, they will be rejected by the distant end.
	 * @param name The attribute name.
	 * @param value The attribute value.
	 * @throws Throwable
	 */
	public void addAttribute(String name, String value) throws Throwable {
		if (name==null) ThingsException.softwareProblem("addAttribute called with null name");
		if (value==null) ThingsException.softwareProblem("addAttribute called with null value");
		
		NVImmutable p = new NV(name,value); 
		this.addAttribute(p);
	}
	
	/**
	 * Get a parameter by name.
	 * @param name The parameter's name.  If null., it will return null;
	 * @return The parameter's value or null (ActorConstants.NO_PARAMETER) if it isn't set.
	 */
	public String getAttribute(String name) {
		if (name==null) return null;
		String response = ActorConstants.NO_PARAMETER;
		if (attributes!=null) {
			NVImmutable thang = attributes.get(name);
			if (thang != null) response = thang.getValue();
		}
		return response;
	}
	

	/**
	 * Load this message from a stream.
	 * @param is The stream.
	 * @throws Throwable
	 */
	public synchronized void load(InputStream is) throws Throwable  {
		int numberParameters;	
		int numberAttributes;
		
		 // MESSAGE = SEQUENCE,TYPE,NUMBER.PARAMETERS,NUMBER.ATTRIBUTES,PARAMETERS,ATTRIBUTES<br>
		 // SEQUENCE = int (4-byte, jvm ordered)<br>
		 // NUMBER.PARAMETERS = int (4-byte, jvm ordered), number of at PAIRs<br>
		 // NUMBER.ATTRIBUTES = int (4-byte, jvm ordered), number of at PAIRs<br>
		 // TYPE = STRING (for the message type)<br>
		try {
			sequence = readInt(is);
			numberParameters = readInt(is);
			numberAttributes = readInt(is);
			type = readString(is);
		} catch (Throwable t) {
			throw new ThingsException("Failed reading header.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t);
		}
		
		// Validate
		if (numberParameters > ActorConstants.MAX_PARAMETERS_PER_MESSAGE) throw new ThingsException("Exceeded maximum parameters per message.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, ThingsNamespace.ATTR_METRIC_COUNT, Integer.toString(numberParameters));
		if (numberAttributes > ActorConstants.MAX_PARAMETERS_PER_MESSAGE) throw new ThingsException("Exceeded maximum attributes per message.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, ThingsNamespace.ATTR_METRIC_COUNT, Integer.toString(numberAttributes));
		if ((type==null)||(type.length()<1)) throw new ThingsException("No TYPE given.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE);
		
		// Items
		String name;
		String value;
		
		// PARAMETERS
		if (numberParameters > 0) parameters = new HashMap<String,NVImmutable>();
		for (int index = 0; index < numberParameters; index++) {
			
			try {
				name = readString(is);
			} catch (Throwable t) {
				throw new ThingsException("Failed reading PARAMETER name.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER, Integer.toString(index+1));
			}

			try {
				value = readString(is);
			} catch (Throwable t) {
				throw new ThingsException("Failed reading PARAMETER value.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER, Integer.toString(index+1));
			}
		
			// Save
			addParamter(name, value);
		}	
		
		// ATTRIBUTES
		if (numberAttributes > 0) attributes = new HashMap<String,NVImmutable>();
		for (int index = 0; index < numberAttributes; index++) {
			
			try {
				name = readString(is);
			} catch (Throwable t) {
				throw new ThingsException("Failed reading ATTRIBUTE name.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER, Integer.toString(index+1));
			}

			try {
				value = readString(is);
			} catch (Throwable t) {
				throw new ThingsException("Failed reading ATTRIBUTE value.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_ITEM_NUMBER, Integer.toString(index+1));
			}
		
			// Save
			addAttribute(name, value);
		}	
	}
	
	/**
	 * Save this message to a stream.
	 * @param os The stream.
	 * @throws Throwable
	 */
	public synchronized void save(OutputStream os) throws Throwable  {
		
		int numberParameters = parameters.size();	
		int numberAttributes = attributes.size();
		
		// Validate
		if ( numberParameters > ActorConstants.MAX_PARAMETERS_PER_MESSAGE) throw new ThingsException("Exceeded maximum parameters per message.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, ThingsNamespace.ATTR_METRIC_COUNT, Integer.toString(numberParameters));
		if ( numberAttributes > ActorConstants.MAX_PARAMETERS_PER_MESSAGE) throw new ThingsException("Exceeded maximum attributes per message.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, ThingsNamespace.ATTR_METRIC_COUNT, Integer.toString(numberAttributes));
		if ((type==null)||(type.length()<1)) throw new ThingsException("No TYPE given.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE);
		
		
		 // MESSAGE = SEQUENCE,TYPE,NUMBER.PARAMETERS,NUMBER.ATTRIBUTES,PARAMETERS,ATTRIBUTES<br>
		 // SEQUENCE = int (4-byte, jvm ordered)<br>
		 // NUMBER.PARAMETERS = int (4-byte, jvm ordered), number of at PAIRs<br>
		 // NUMBER.ATTRIBUTES = int (4-byte, jvm ordered), number of at PAIRs<br>
		 // TYPE = STRING (for the message type)<br>
		try {
			writeInt(os, sequence);
			writeInt(os, numberParameters);
			writeInt(os, numberAttributes);
			writeString(os, type);
		} catch (Throwable t) {
			throw new ThingsException("Failed writing header.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t);
		}
		
		// PARAMETERS
		for (NVImmutable item : parameters.values()) {
			
			try {
				writeString(os, item.getName());
			} catch (Throwable t) {
				throw new ThingsException("Failed writing PARAMETER name.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_VALUE, item.getName());
			}

			try {
				writeString(os, item.getValue());
			} catch (Throwable t) {
				throw new ThingsException("Failed writing PARAMETER value.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_PARAMETER_VALUE, item.getValue());
			}
		}	
		
		// ATTRIBUTES
		for (NVImmutable item : attributes.values()) {
			
			try {
				writeString(os, item.getName());
			} catch (Throwable t) {
				throw new ThingsException("Failed writing ATTRIBUTE name.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_DATA_ATTRIBUTE_VALUE, item.getName());
			}

			try {
				writeString(os, item.getValue());
			} catch (Throwable t) {
				throw new ThingsException("Failed writing ATTRIBUTE value.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE, t, ThingsNamespace.ATTR_DATA_ATTRIBUTE_VALUE, item.getValue());
			}
		}	
		
		// Push it.
		os.flush();
	}
	
	/**
	 * Set type.
	 * @param messageType The type. Try to keep them under 2k.  If they are too big, they will be rejected by the distant end.
	 */
	public void setType(String  messageType) {
		type = messageType;
	}
	
	/**
	 * Get type.
	 * @return the type.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Convenient message construction.
	 * @param theSequence
	 * @param messageType
	 * @param name
	 * @param parameter
	 * @return
	 * @throws Throwable
	 */
	static ActorMessage buildQuickie(int theSequence, String messageType, String name, String parameter) throws Throwable {
		ActorMessage result = new ActorMessage(theSequence);
		result.setType(messageType);
		result.addParamter(name,parameter);
		return result;
	}
	
	/**
	 * Convenient message construction.
	 * @param theSequence
	 * @param messageType
	 * @return
	 */
	static ActorMessage buildQuickie(int theSequence, String messageType) {
		ActorMessage result = new ActorMessage(theSequence);
		result.setType(messageType);
		return result;
	}
	
	// ==============================================================================================================================
	// = INTERNAL	
	
	private byte[] loadBuffer = new byte[4];
	private byte[] saveBuffer = new byte[4];
	
	/**
	 * Read an integer.
	 * @param is
	 * @return the integer.
	 * @throws Throwable
	 */
	private int readInt(InputStream is) throws Throwable {
		is.read(loadBuffer,0,4);
		return ActorConstants.decodeInteger(loadBuffer,0);
	}
	
	/**
	 * Read a STRING.
	 * @param is
	 * @return the String.
	 * @throws Throwable
	 */
	private String readString(InputStream is) throws Throwable {
		int size = readInt(is) ;
		if ((size > ActorConstants.MAX_MESSAGE_TOKEN)||(size < 1)) throw new ThingsException("Token exceeds allowable size or is less than 1.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE_FIELD, ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(size));
		byte[] buffer = new byte[size];
		is.read(buffer,0,size);
		return new String(buffer);
	}
	
	/**
	 * Write an integer.
	 * @param os target stream
	 * @param value the integer
	 * @throws Throwable
	 */
	private void writeInt(OutputStream os, int  value) throws Throwable {
		ActorConstants.encodeInteger(saveBuffer, 0, value);
		os.write(saveBuffer);
	}
	
	/**
	 * Write a STRING.
	 * @param os target stream.
	 * @param value the String.
	 * @throws Throwable
	 */
	private void writeString(OutputStream os, String value) throws Throwable {
		int size = value.length();
		if ((size > ActorConstants.MAX_MESSAGE_TOKEN)||(size < 1)) throw new ThingsException("Token exceeds allowable size or is less than 1.", ThingsException.ACTORSERVICE_FAULT_BAD_MESSAGE_FIELD, ThingsNamespace.ATTR_DATA_SIZE, Integer.toString(size));
		writeInt(os, size);
		os.write(value.getBytes()); 
	}
	
}