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
import things.common.Stamp;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;

/**
 * This is standard receipt.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 JUL 05
 * </pre> 
 */
public class Receipt extends Stamp {

	// Type
	public enum Type {		// RESULT	-	STATE
		NOT_VALID,			// BAD		-	TERMINAL
		UNSPECIFIED,		// OK		-	PENDING
		PREPARATION,		// OK		-	PENDING
		COLLECTION,			// OK		-	PENDING
		UNWANTED,			// BAD		-	TERMINAL
		DELIVERY,			// OK		-	TERMINAL
		ACCEPTANCE,			// OK		-	TERMINAL
		UNDERWAY,			// OK		-	PENDING
		COMPLETION,			// OK		-	TERMINAL
		REJECTED,			// BAD		-	TERMINAL
		ERRORED,			// BAD		-	TERMINAL
		IRRELEVENT,			// OK    	-	TERMINAL
		ETERNAL_HAPPINESS;	// OK		- 	TERMINAL
		
		/**
		 * Returns true the RESULT is both complete and OK, as opposed to BAD and/or not complete.
		 * @return true if it is, otherwise false.
		 */
		public boolean isCompleteAndOk() {
			return (isOk() && isTerminal());
		}
		
		/**
		 * Returns true the RESULT is OK, as opposed to BAD.
		 * @return true if it is RESULT=OK, else false.
		 */
		public boolean isOk() {
			switch(this) {
			case UNSPECIFIED:
			case PREPARATION:
			case COLLECTION:
			case DELIVERY:
			case ACCEPTANCE:
			case UNDERWAY:
			case COMPLETION:
			case IRRELEVENT:
			case ETERNAL_HAPPINESS:
					return true;
			default:
					return false;
			}
		}
		
		/**
		 * Returns true the STATE is TERMINAL, as opposed to PENDING.
		 * @return true if it is STATE=TERMINAL, else false.
		 */
		public boolean isTerminal() {
			switch(this) {
			case NOT_VALID:
			case UNWANTED:
			case DELIVERY:
			case ACCEPTANCE:
			case COMPLETION:
			case REJECTED:
			case ERRORED:
			case IRRELEVENT:
			case ETERNAL_HAPPINESS:
					return true;
			default:
					return false;
			}
		}
	}
	
	// Version it
	final static long serialVersionUID = 1;
	
	// Various private
	private boolean testFromSomebody;
	private boolean testHasToken;
	private Receipt.Type myType; 
	private String optionalNote;
	
	/*
	 * Default constructor.  It will build a generic receipt without an ID or token.  The ID will be set to IAmNobody object.  The token will be set to ThingsConstants.A_NOTHING.
	 * It will set the type as Unspecified.
	 * @see things.common.ThingsConstants
	 * @see things.common.IAmNobody
	 * @throws things.ThingsException
	 */
	public Receipt() throws ThingsException {
		super(new IAmNobody(),ThingsConstants.A_NOTHING);
		testFromSomebody = false;
		testHasToken = false;
		myType = Type.UNSPECIFIED;
	}
	
	/*
	 * Token constructor.  It will build a receipt without an ID but with a token.  The ID will be set to a IAmNobody object.
	 * @param theToken A string token.  Becareful of size. It'll handle anything a string can, but rendering might suck.
	 * @param theType The Receipt.Type for this Receipt.
	 * @see things.common.IAmNobody
	 * @throws things.ThingsException
	 */
	public Receipt(String theToken, Receipt.Type theType) throws ThingsException {
		super(new IAmNobody(),theToken);
		testFromSomebody = false;
		testHasToken = true;
		myType = theType;
	}
	
	/*
	 * Token constructor.  It will build a receipt without an ID but with a token.  The ID will be set to a IAmNobody object.
	 * @param theToken A string token.  Becareful of size. It'll handle anything a string can, but rendering might suck.
	 * @param theType The Receipt.Type for this Receipt.
	 * @param note Optional note.
	 * @see things.common.IAmNobody
	 * @throws things.ThingsException
	 */
	public Receipt(String theToken, Receipt.Type theType, String note) throws ThingsException {
		super(new IAmNobody(),theToken);
		testFromSomebody = false;
		testHasToken = true;
		myType = theType;
		optionalNote = note;
	}
	
	/*
	 * callerID constructor.  It will build a receipt without a token but with a callerID.  The token will be set to ThingsConstants.A_NOTHING.
	 * @param callerID Any unchecked WhoAmI.
	 * @param theType The Receipt.Type for this Receipt.
	 * @see things.common.WhoAmI
	 * @see things.common.ThingsConstants
	 * @throws things.ThingsException
	 */
	public Receipt(WhoAmI callerID, Receipt.Type theType) throws ThingsException {
		super(callerID,ThingsConstants.A_NOTHING);
		testFromSomebody = true;
		testHasToken = false;
		myType = theType;
	}
	
	/*
	 * Full constructor.  It will build a receipt with a token and an ID.
	 * @param callerID Any unchecked WhoAmI.
	 * @param theToken A string token.  Becareful of size. It'll handle anything a string can, but rendering might suck.
	 * @param theType The Receipt.Type for this Receipt.
	 * @see things.common.WhoAmI
	 * @see things.common.ThingsConstants
	 * @throws things.ThingsException
	 */
	public Receipt(WhoAmI callerID, String theToken, Receipt.Type theType) throws ThingsException {
		super(callerID,theToken);
		testFromSomebody = true;
		testHasToken = true;
		myType = theType;
	}
	
	/*
	 * Full constructor.  It will build a receipt with a token and an ID.
	 * @param callerID Any unchecked WhoAmI.
	 * @param theToken A string token.  Becareful of size. It'll handle anything a string can, but rendering might suck.
	 * @param theType The Receipt.Type for this Receipt.
	 * @param note Optional note.
	 * @see things.common.WhoAmI
	 * @see things.common.ThingsConstants
	 * @throws things.ThingsException
	 */
	public Receipt(WhoAmI callerID, String theToken, Receipt.Type theType, String note) throws ThingsException {
		super(callerID,theToken);
		testFromSomebody = true;
		testHasToken = true;
		myType = theType;
		optionalNote = note;
	}

	/*
	 * Report if this Receipt is from somebody, rather than IAmNobody().
	 * @return true if it is from somebody.
	 * @see things.common.IAmNobody
	 */
	public boolean isFromSomebody() {
		return testFromSomebody;
	}
	
	/*
	 * Report if this Receipt has a token, rather than ThingsConstants.A_NOTHING.
	 * @return true if it is from somebody.
	 * @see things.common.ThingsConstants
	 * @see things.common.IAmNobody
	 */
	public boolean hasToken() {
		return testHasToken;
	}
		
	/*
	 * Get the type of receipt.
	 * @return the type
	 */
	public Receipt.Type getType() {
		return myType;
	}
	
	/*
	 * Get the optional note.
	 * @return the note or null if it is empty
	 */
	public String getNote() {
		return optionalNote;
	}
	
	/*
	 * A string representation of this Reciept.  It is not reliable for validation, but it will alwatts report the same value.
	 * @return The string
	 */
	public String toString() {
		 String result = null;
		 try {
			 result = AttributeCodec.encode2String(
					 ThingsNamespace.ATTR_DATA_RECEIPT_STAMP, super.toString(),
					 ThingsNamespace.ATTR_DATA_RECEIPT_TYPE, myType.toString(),
					 ThingsNamespace.ATTR_DATA_RECEIPT_NOTE, optionalNote
			 );
		 } catch (Throwable e) {
			 // Dont' care.
		 }
		 return result;
	}
	
}
