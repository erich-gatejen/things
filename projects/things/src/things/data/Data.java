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

import java.io.Serializable;

import things.common.WhoAmI;

/**
 * This defines a piece of data and the methods of managing it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Rewrite from another project - 22 MAY 04
 * EPG - Weld in result types - 7 JUN 05
 * </pre> 
 */
public interface Data extends Serializable {

	final static long serialVersionUID = 1;

	/**
	 * Priority of the data.
	 */
	public enum Priority {
		P_MAXIMUM,
		FLASH,
		P1,
		IMMEDIATE,
		P2,
		PRIORITY,
		P3,
		ROUTINE,
		P4,
		DEBUG,
		P5,
		FLOOD,
		P_ALL,
		P_MINIMUM
	}
	
	/**
	 * Type of Data.  This can be used to give meaning to results, since most Data objects will come as a 
	 * result to some operation.  Having different purposes for General and Result types is probably
	 * a bad idea, since it forks the purpose of this class.  However, too much work was done in other
	 * areas to separate them.
	 */
	public enum Type {
		
		// General types
		GENERIC,
		ENTRY,
		COMMAND,
		COMMAND_RESPONSE,
		COMMENT,
		KEEPALIVE,	
		
		// Result types
		METRIC,
		INFO,
		PASS,
		INCONCLUSIVE,
		FAIL,
		EXCEPTION,
		ABORT,
		WAITING;
	
		// Methods
		/**
		 * Get the shortname for the outcome.  It will be only 4 characters.  Do this programatically to keep 
		 * it completely static.  No need to take up memory with the enum instance itself (yes, I know that
		 * can technically be wrong).
		 * @return the short name as a String.
		 */
		public String getShortName() {
			switch(this) {
			case GENERIC: 	return "GENR";
			case ENTRY:		return "ENTR";
			case COMMAND:	return "CMND";
			case COMMAND_RESPONSE: return "RESP";
			case COMMENT:	return "####";
			case KEEPALIVE:	return "KEEP";
			case METRIC:	return "METR";
			case INFO:		return "INFO";
			case PASS:		return "PASS";
			case INCONCLUSIVE: return "INCO";
			case FAIL:		return "FAIL";
			case EXCEPTION:	return "EXCP";
			case ABORT:		return "ABRT";
			case WAITING:	return "WAIT";
			default: return "UNKN";
			}
		}
		
		/**
		 * Is this a result type?
		 * @return true if it is, otherwise false.
		 */
		public boolean isResult() {
			switch(this) {
			case GENERIC:
			case ENTRY:	
			case COMMAND:
			case COMMAND_RESPONSE: 
			case COMMENT:
			case KEEPALIVE:	return false;
			case METRIC:
			case INFO:
			case PASS:
			case INCONCLUSIVE:
			case FAIL:
			case EXCEPTION:
			case ABORT:
			case WAITING:	return true;
			default: return false;
			}
		}
	}
	
	/**
	 * Create a child ID using the given name.
	 * @param childsName the given name for the child.
	 * @return the textual representation of the ID.
	 * @see things.common.WhoAmI
	 */
	 public WhoAmI birthMyChild(String  childsName);

	/**
	 * Create a child ID using the given name and tag.  It must yield the same ID if the same value is used for childsName.  
	 * @param childsName the given name for the child.
	 * @param childsTag the tag for the child.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	 public WhoAmI birthMyChild(String  childsName, String childsTag);
	 
	/**
	 * Give a textual representation of the data.
	 * @return the textual representation.
	 */
	 public String toString();
	 
	/**
	 * Get the objects ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getID();
	 
	/**
	 * Get the creator's ID.
	 * @return the id
	 * @see things.common.WhoAmI
	 */
	public WhoAmI getCreatorID();
	
	/**
	 * Get the numeric value.  It is completely up to the setter as to what this means.
	 * @return value as an int
	 */
	public int getNumeric();
	
	/**
	 * Get the numeric value.  It is completely up to the setter as to what this means.
	 * @return value as a string
	 */
	public String getNumericString();
	/**
	 * Get the priority.ed.
	 * @return the priority.
	 */
	public Priority getPriority();

	/**
	 * Get the timestamp.
	 * @return timestamp as a long
	 */
	public long getStamp();

	/**
	 * Get the thing (Object).
	 * @return the thing
	 */
	public Object getThing();
	
	/**
	 * Get the type.
	 * @return the type
	 */
	public Type getType();
	
	/**
	 * Get the attributes.
	 * @return the attributes associated with the Data, if any.
	 */
	public AttributeReader getAttributes();
	
}
