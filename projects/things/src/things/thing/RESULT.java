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
package things.thing;

import java.util.Collection;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.WhoAmI;
import things.data.AttributeReader;
import things.data.Attributes;
import things.data.Entity;
import things.data.NVImmutable;
import things.data.impl.ReadWriteableAttributes;

/**
 * An result set.  It is an entity with a metric.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 FEB 07
 * </pre> 
 */
public class RESULT extends Entity<Metrics> {
	
	final static long serialVersionUID = 1;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DATA

	// ====================================================================================================================================
	// ====================================================================================================================================
	// == IMPLEMENTATION METHODS. 
	
	/**
	 * Constructor.  For Type.
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(Type theType) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(ThingsCodes.USER_RESULT_DEFAULT, theType, Priority.ROUTINE, null, null, null);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics and the type.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(Metrics theMetrics, Type theType) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(ThingsCodes.USER_RESULT_DEFAULT, theType, Priority.ROUTINE, theMetrics, null, null);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, and the Ids.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param imposedId the imposed id.
	 * @param creatorId the creator id.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(Metrics theMetrics, Type theType, WhoAmI imposedId, WhoAmI creatorId) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(ThingsCodes.USER_RESULT_DEFAULT, theType, Priority.ROUTINE, theMetrics, imposedId, creatorId);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}

	/**
	 * Constructor.  Evertying but new attributes..
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param imposedId the imposed id.
	 * @param creatorId the creator id.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType, WhoAmI imposedId, WhoAmI creatorId) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, imposedId, creatorId);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	
	/**
	 * Constructor.  Everything.
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param imposedId the imposed id.
	 * @param creatorId the creator id.
	 * @param additionalAttributes attributes.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType, WhoAmI imposedId, WhoAmI creatorId, String... additionalAttributes) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, imposedId, creatorId, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  Everything.
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param imposedId the imposed id.
	 * @param creatorId the creator id.
	 * @param additionalAttributes attributes.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType, WhoAmI imposedId, WhoAmI creatorId, AttributeReader additionalAttributes) throws ThingsException {
		//super(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, AttributeReader a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, imposedId, creatorId, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, and attributes.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param additionalAttributes attributes.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(Metrics theMetrics, Type theType, String... additionalAttributes) throws ThingsException {
		//Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a)
		super(ThingsCodes.USER_RESULT_DEFAULT, theType, Priority.ROUTINE, theMetrics, null, null, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, and attributes.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param additionalAttributes attributes through a reader.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(Metrics theMetrics, Type theType, AttributeReader additionalAttributes) throws ThingsException {
		//Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a)
		super(ThingsCodes.USER_RESULT_DEFAULT, theType, Priority.ROUTINE, theMetrics, null, null, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, and numeric.
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType) throws ThingsException {
		//Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, null, null);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}
	
	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, numeric, and attributes.
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param additionalAttributes attributes.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType, String... additionalAttributes) throws ThingsException {
		//Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, null, null, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}

	/**
	 * Constructor.  It uses defaults for everything the metrics, the type, numeric, and attributes.
	 * @param numeric the numeric code for the operation.
	 * @param theMetrics metrics for this operation
	 * @param theType the Data type.  It must be a result type or an exception will be thrown.
	 * @param additionalAttributes attributes through a reader.  Usually for error or exception reporting.
	 * @see things.data.Data
	 * @throws things.common.ThingsException
	 */
	public RESULT(int numeric, Metrics theMetrics, Type theType, AttributeReader additionalAttributes) throws ThingsException {
		//Entity(int n, Type t, Priority p, O thing, WhoAmI imposedId, WhoAmI creatorId, String... a)
		super(numeric, theType, Priority.ROUTINE, theMetrics, null, null, additionalAttributes);
		if (!theType.isResult()) throw new ThingsException("RESULT created with a Data type other than Result.", ThingsException.SYSTEM_FAULT_THING_RESULT_MISUSED);
	}

	
	// =============================================================================================================================
	// == OVERRIDES
		
	
	// =============================================================================================================================
	// == ORGANIC
	
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == LOCAL METHODS.  
	
	/**
	 * Get a NVImmutable collection for all attributes and metrics.
	 * @return all attributes
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */
	public Collection<NVImmutable> getAllAttributes() throws ThingsException {
		return getAllLocalAttributesWritable().getAttributes();
	}
	
	/**
	 * Get a NVImmutable collection for all attributes and metrics.
	 * @param additionalAttributes to add to the set before returning them.
	 * @return all attributes plus any just added.
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */
	public Collection<NVImmutable> getAllAttributes(String... additionalAttributes) throws ThingsException {

		// Get and add.
		Attributes localAttribs = getAllLocalAttributesWritable();
		localAttribs.addMultiAttributes(additionalAttributes);
		
		// return them
		return localAttribs.getAttributes();
	}
	

	/**
	 * Get a reader for all attributes and metrics.
	 * @return the reader
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */
	public AttributeReader getAllAttributesReader() throws ThingsException {
		return getAllLocalAttributesWritable();
	}
	
	/**
	 * Get a reader for all attributes and metrics.
	 * @param additionalAttributes to add to the set before returning them.
	 * @return the reader
	 * @throws things.common.ThingsException 
	 */
	public AttributeReader getAllAttributesReader(String... additionalAttributes) throws ThingsException {
		// Get and add.
		Attributes localAttribs = getAllLocalAttributesWritable();
		localAttribs.addMultiAttributes(additionalAttributes);
		
		return localAttribs;
	}
	
	/**
	 * Forge a child.  It will have all the same data except the numeric and type.
	 * @param numeric
	 * @param theType
	 * @return the child.
	 * @throws ThingsException
	 */
	public RESULT forgeChild(int numeric, Type theType) throws ThingsException {
		RESULT newResult = new RESULT(numeric, getTypedThing(), theType, getID(), this.getCreatorID(), attributes);
		return newResult;
	}
	
	/**
	 * Forge a child.  It will have all the same data except the numeric, type, and additional attributes..
	 * @param numeric
	 * @param theType
	 * @param additionalAttributes
	 * @return the child.
	 * @throws ThingsException
	 */
	public RESULT forgeChild(int numeric, Type theType, String... additionalAttributes) throws ThingsException {
		ReadWriteableAttributes tempAttributes = new ReadWriteableAttributes();
		tempAttributes.addAttribute(attributes);	// Don't get the metrics expressions.  Just the real attributes.
		tempAttributes.addMultiAttributes(additionalAttributes);
		RESULT newResult = new RESULT(numeric, getTypedThing(), theType, getID(), this.getCreatorID(), tempAttributes);
		return newResult;
	}
	
	/**
	 * Forge a child.  It will have all the same data except the numeric, type, and additional attributes..
	 * @param numeric
	 * @param theType
	 * @param metrics 
	 * @param t throwable.  If null, it is ignored.  If ThingsException, it will extract the attributes.
	 * @param additionalAttributes
	 * @return the new result.
	 * @throws ThingsException
	 */
	public static RESULT forgeResult(int numeric, Type theType, Metrics metrics, Throwable t, String... additionalAttributes) throws ThingsException {
		
		AttributeReader masterReader;
		if (t instanceof ThingsException) {
			ThingsException workingException = (ThingsException)t;
			workingException.addAttribute(ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
			workingException.addAttributes(additionalAttributes);
			masterReader = workingException.getAttributesReader();
			
		} else {
			ReadWriteableAttributes attributes = new ReadWriteableAttributes();
			attributes.addMultiAttributes(additionalAttributes);
			if (t !=null) {
				attributes.addAttribute(ThingsNamespace.ATTR_PLATFORM_MESSAGE_COMPLETE, ThingsException.toStringCauses(t));
			}
			masterReader = attributes;
		}
		
		return new RESULT(numeric, metrics, theType, masterReader);
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == INTERNAL METHODS
	
	/**
	 * Get the local attributes as a new, writable set.
	 * @throws things.common.ThingsException 
	 * @return The attributes.
	 */
	private Attributes getAllLocalAttributesWritable() throws ThingsException {
		
		// Fix attributes
		Attributes localAttribs = getAttributes().getPrivateSet();
		Metrics myMetrics = getTypedThing();
		myMetrics.writeAsAttributes(localAttribs);
		localAttribs.addAttribute(ThingsNamespace.ATTR_THING_RESULT_TYPE, this.getType().toString());
		return localAttribs;
	}
	
}
