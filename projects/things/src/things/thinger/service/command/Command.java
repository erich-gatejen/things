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
package things.thinger.service.command;

import java.util.LinkedHashMap;

import things.common.ThingsException;
import things.data.AttributeReader;
import things.data.NV;
import things.data.NVImmutable;
import things.data.Receipt;
import things.data.ReceiptList;
import things.data.impl.ReadWriteableAttributes;
import things.thinger.SystemException;
import things.thinger.SystemInterface;
import things.thinger.SystemNamespace;
import things.thinger.service.command.local.LocalCommandRegistry;

/**
 * A command.  It is an entity with a String object.  The string is the text associated with the command.  It's up to the command processor
 * to decide what to do with it.  The numeric defined the actual command.  Parameters should be passed through the attributes.
 * <p>
 * Since commands are Entities, they have an Id and a creators Id.
 * <p>
 * Note that the Command validates PARAMETERs, where the CommandResponse will validate the RESPONSEs.  This means it is possible for a responder to flood
 * a CommandResponse.  Later we may abstract Command enough to let a specific implementation handle the RESPONSE validation too.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 06
 * </pre> 
 */
public abstract class Command {

	final static long serialVersionUID = 1;
	final static String NOT_NAMED = null;
	
	// =======================================================================================================
	// == DATA - These REALLY should be kept to a minimum.
	
	private static CommandRegistry mYCommandRegistry;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == DECLARATIONS.
	
	/**
	 * The requirement for a parameter.
	 */
	public enum Requirement { REQUIRED, OPTIONAL };
	
	/**
	 * The occurence of a parameter.
	 */
	public enum Occurrence { NEVER, ONLYONE,  MANY, WHATEVER };
	
	/**
	 * The type of the parameter.
	 */
	public enum DataType { VALUE, LIST };
	
	/**
	 * Are we checking a command or a response.
	 */
	public enum CheckType { COMMAND, RESPONSE };
		
	/**
	 * Declare the name of the command.
	 * @param theName The name.
	 * @param className The class name.  Chose to do this since you can't trust 'this'.
	 * @throws SystemException For any missuse.
	 */
	public final void DECLARE_NAME(String theName, String className) throws SystemException {
		synchronized (mYCommandRegistry) {
			
			// Already named?
			if (cachedDefinition != null) {
				throw new SystemException("This has already been defined.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
			}
			
			// Is it already in the global registry?
			if (mYCommandRegistry.has(theName)) {
				throw new SystemException("Name already registered.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, theName);			
			}
			
			// Make sure the sub was well formed
			String me = this.named();
			if ( (me==null) || (!me.equals(theName))) {
				throw new SystemException("Name DECLARED not the same as named.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, theName);
			}
			
			// Put this in the global registry and cache a reference to it while we declare.
			cachedDefinition = new CommandDefinition();
			cachedDefinition.name = theName;
			cachedDefinition.className = className;
			mYCommandRegistry.register(me,cachedDefinition);
			
		}
	}
	
	// PARAMETER(NAME,	REQUIRED,	ONLYONE		VALUE);
	/**
	 * Declare a parameter.  If a parameter by the same name is already declared, it will throw an error Exception.
	 * @param parameterName The parameter name.
	 * @param theRequirement The requirement for the item.
	 * @param theOccurence The occurence for the item.
	 * @param theDataType The data type for the item.
	 * @throws things.thinger.SystemException
	 */
	public final void DECLARE_PARAMETER(String parameterName,	Requirement theRequirement,	Occurrence theOccurence,	DataType theDataType) throws SystemException {
		synchronized (this) {
				
			// Validate
			if (cachedDefinition == null) {
				throw new SystemException("DECLARE_PARAMETER faulty.  You must DECLARE_NAME before any other declaration.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION);
			}
			if (cachedDefinition.parameters.containsKey(parameterName)) {
				throw new SystemException("DECLARE_PARAMETER faulty.  Parameter already declared by that name.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, parameterName, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());				
			}
			
			// declare
			CommandItem item = new CommandItem(parameterName, theRequirement, theOccurence, theDataType);
			cachedDefinition.parameters.put(parameterName,item);
		}
	}
	 
	// RESPONSE(NAME, 	OPTIONAL, 	MANY, 	LIST);
	/**
	 * Declare a response.  If a parameter by the same name is already declared, it will throw an error Exception.
	 * @param responseName The response name.
	 * @param theRequirement The requirement for the item.
	 * @param theOccurence The occurence for the item.
	 * @param theDataType The data type for the item.
	 * @throws things.thinger.SystemException
	 */
	public void DECLARE_RESPONSE(String responseName, Requirement theRequirement,	Occurrence theOccurence,	DataType theDataType) throws SystemException {
		synchronized (this) {
			
			// Validate
			if (cachedDefinition == null) {
				throw new SystemException("DECLARE_PARAMETER faulty.  You must DECLARE_NAME before any other declaration.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION);
			}
			if (cachedDefinition.responses.containsKey(responseName)) {
				throw new SystemException("DECLARE_RESPONSE faulty.  Response already declared by that name.", SystemException.SYSTEM_COMMAND_ERROR_BAD_DECLARATION, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, responseName, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());				
			}
			
			// declare
			CommandItem item = new CommandItem(responseName, theRequirement, theOccurence, theDataType);
			cachedDefinition.responses.put(responseName,item);
		}	
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == COMMAND DIRECTIVES
	
	/**
	 * Set a parameter.  If it is a multi-value.
	 * @param name the name of the parameter.
	 * @param value the values associated with the parameter.  It can be one or more.
	 * @throws SystemException
	 */
	public void SET_PARAMETER(String name, String... value) throws SystemException {
		
		// make sure this is done right.  Kinda sucks that Java can't separate those items.
		if (name == null) throw new SystemException("SET_PARAMETER faulty.  Parameter passed with a null name.", SystemException.SYSTEM_COMMAND_ERROR_TYPE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
		if (value == null) throw new SystemException("SET_PARAMETER faulty.  Parameter set with a null value.", SystemException.SYSTEM_COMMAND_ERROR_TYPE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
		
		// Instance check.
		isCommandInstance();
		
		// Can I set it?
		if (cachedDefinition.parameters.containsKey(name)) {
			
			CommandItem thisItem = cachedDefinition.parameters.get(name);
			try {
				
				// Enforce type
				if ((thisItem.myDataType == DataType.VALUE)&&(value.length > 1)) 
					throw new SystemException("SET_PARAMETER faulty.  Parameter defined with as date type VALUE, but a list was passed.", SystemException.SYSTEM_COMMAND_ERROR_TYPE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
					
				// Save by occurence
				switch (thisItem.myOccurence) {
					case NEVER:
						throw new SystemException("SET_PARAMETER faulty.  Parameter defiend with an occurence of NEVER.", SystemException.SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
				
					case ONLYONE:
						// Replace
						commandInstance.removeAttribute(name);
						commandInstance.addAttribute(new NV(name,value));
						break;
						
					case MANY:
					case WHATEVER:					
						// Simply add it.
						commandInstance.addAttribute(new NV(name,value));
						break;
						
					default:
						throw new SystemException("SET_PARAMETER FAULT.  Parameter occurence value not defined.  This is a bug.", SystemException.SYSTEM_COMMAND_FAULT_UNDEFINED_STATE, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
				}
			
			} catch (SystemException se) {
				throw se;
			} catch (ThingsException te) {
				throw new SystemException("SET_PARAMETER faulty.  Could not set it.", SystemException.SYSTEM_COMMAND_ERROR_SET_FAILED, te, SystemNamespace.ATTR_PLATFORM_MESSAGE,te.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
			} catch (Throwable t) {
				throw new SystemException("SET_PARAMETER FAULT.  Parameter occurence value not defined.  This is a bug.", SystemException.SYSTEM_COMMAND_FAULT_SET_PROBLEM, t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
			}
		
			// else if parameter not defined
		} else {
			throw new SystemException("SET_PARAMETER faulty.  Parameter not defined.", SystemException.SYSTEM_COMMAND_ERROR_PARAMETER_NOT_DEFINED, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
		}
		
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == RESPONSE DIRECTIVES
	
	/**
	 * Get the system interface.
	 * @return the system interface.
	 */
	public SystemInterface GET_SYSTEM_INTERFACE() throws SystemException {
		return commandResponder.getSystemInterface();
	}
	
	/**
	 * Get a parameter.  If it isn't set and required, it'll throw an exception.  If it isn't set but not required, it'll just return a null.
	 * @param name the name of the parameter
	 * @return an NVImmutable containing the parameter.
	 */
	public NVImmutable GET_PARAMETER(String name) throws SystemException {
		
		NVImmutable result = null;
		
		// Instance check and validate
		definitionCheck();
		isResponseInstance();
		if (name == null) throw new SystemException("GET_PARAMETER cannot specify a null name.", SystemException.SYSTEM_COMMAND_ERROR_NULL_PARAMETER_NAME, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
		
		// Can I get it?
		if (commandResponder.getCommandAttributes().hasAttribute(name)) {
			
			result = commandResponder.getCommandAttributes().getAttribute(name);

		} else {

			// definition check so we can check if it required
			try {
				definitionCheck();
			} catch (Throwable t) {
				throw new SystemException("Serious cascading problem while doing a definition check in GET_PARAMETER.", SystemException.PANIC_SYSTEM_COMMAND_CASCADING_PROBLEMS, t, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
			}
				
			// Is it defined and is it required?  If required (and it isn't present), that's an exception.
			if (cachedDefinition.parameters.containsKey(name)) {
				if (cachedDefinition.parameters.get(name).myRequirement == Requirement.REQUIRED) {
					// Defined, required, but not present.
					throw new SystemException("Command missing required parameter after transmission.  Something bad happened!", SystemException.SYSTEM_COMMAND_FAULT_PARAMETER_MISSING_AFTER_TRANSMISSION, 
							SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, cachedDefinition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name);		
				}
				
			} else {
				// Not defined
				throw new SystemException("GET_PARAMETER faulty.  Parameter not defined.", SystemException.SYSTEM_COMMAND_ERROR_PARAMETER_NOT_DEFINED, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());		
			}
			
		} // end if got it
		
		return result;
	}	
	
	/**
	 * Set a response.  If it is a multi-value, it'll be handled appropriately.
	 * @param name the name of the response item.
	 * @param value the values associated with the response.  It can be one or more.
	 * @throws SystemException
	 */
	public void RESPOND(String name, String... value) throws SystemException {
		this.RESPOND(new NV(name, value));
	}
	
	/**
	 * Set a response.  If it is a multi-value, it'll be handled appropriately.
	 * @param item the NV item with name/values associated with the response.  It can be one or more.
	 * @see things.data.NV
	 * @throws SystemException
	 */
	public void RESPOND(NV item) throws SystemException {
		
		// Instance check.
		isResponseInstance();
		
		// Can I set it?
		if (cachedDefinition.responses.containsKey(item.getName())) {
			
			CommandItem thisItem = cachedDefinition.responses.get(item.getName());
			try {
				
				// Enforce type
				if ((thisItem.myDataType == DataType.VALUE)&&(item.isMultivalue())) 
					throw new SystemException("RESPOND faulty.  Response defiend with as date type VALUE, but a list was passed.", SystemException.SYSTEM_COMMAND_ERROR_TYPE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_RESPONSE_NAME, item.getName(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
					
				
				// Save by occurence
				switch (thisItem.myOccurence) {
					case NEVER:
						throw new SystemException("RESPOND faulty.  Response defiend with an occurence of NEVER.", SystemException.SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION, SystemNamespace.ATTR_SYSTEM_COMMAND_RESPONSE_NAME, item.getName(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
				
					case ONLYONE:
						// Replace
						commandResponder.remove(item.getName());
						commandResponder.add(item);
						break;
						
					case MANY:
					case WHATEVER:					
						// Simply add it.
						commandResponder.add(item);
						break;
						
					default:
						throw new SystemException("RESPOND FAULT.  Response occurence value not defined.  This is a bug.", SystemException.SYSTEM_COMMAND_FAULT_UNDEFINED_STATE, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
				}
			
			} catch (SystemException se) {
				throw se;
			} catch (Throwable t) {
				throw new SystemException("SET_PARAMETER FAULT.  Parameter occurence value not defined.  This is a bug.", SystemException.SYSTEM_COMMAND_FAULT_SET_PROBLEM, t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
			}
		
			// else if parameter not defined
		} else {
			throw new SystemException("RESPOND faulty.  Response not defined.", SystemException.SYSTEM_COMMAND_ERROR_RESPONSE_NOT_DEFINED, SystemNamespace.ATTR_SYSTEM_COMMAND_RESPONSE_NAME, item.getName(), SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());
		}
		
	}
	
	/**
	 * Force the response to flush.
	 * @throws SystemException
	 */
	public void FLUSH() throws SystemException {
		
		// Instance check.
		isResponseInstance();
		commandResponder.flush();
	}
	
	/**
	 * Set the response as done.  This should be the LAST thing you do.
	 * @throws SystemException
	 */
	public void DONE() throws SystemException {
		
		// Instance check.
		isResponseInstance();
		commandResponder.done();
	}

	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == ABSTRACT

	/**
	 * Return the official name of this command.  If there is another command named the same of  different class signature, it will cause
	 * a significant system fault.
	 * @return The official name of the command.
	 */
	abstract public String named();
	
	/**
	 * Command declaration.  This will be called when the definition is needed.
	 * @throws things.thinger.SystemException
	 */
	abstract public void declare() throws SystemException;
	
	/**
	 * This will be called when the command is called.
	 * @throws things.thinger.SystemException
	 */
	abstract public void accept() throws SystemException;
	
	// =======================================================================================================
	// == DEFINITION
	
	// The command definition.
	private CommandDefinition 	cachedDefinition = null;
	
	/**
	 * Check to see if the object has definition.
	 * @throws SystemException As a fault.
	 */
	private void definitionCheck() throws SystemException {
		
		// Has it been declared?
		if (cachedDefinition == null) {
			
			// Try to get an instance.
			if (mYCommandRegistry.has(this.named())) {
				
				// It's defined.  Use it.
				cachedDefinition = mYCommandRegistry.get(this.named());
				
			} else {
				
				// It's not defined.  Declare it.  Any problem here is a fault.
				try {
					this.declare();
							
				} catch (Exception e) {
					throw new SystemException("Instance fault; could not define.", SystemException.SYSTEM_COMMAND_FAULT_CANNOT_DEFINE, e, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());					
				}
				
			} // end if
			
		} // end if definition	
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == INSTANCE

	// The command instance.  A null entry used to capture the attributes.
	private ReadWriteableAttributes  	commandInstance = null;
	
	// The command responder.
	private CommandResponder  			commandResponder = null;
	
	/**
	 *  Instantiate a new instance.  This is what a commander should do.  Technically, you can reinstantiate an object and use it again.  
	 *  This should ONLY be done by a commander.
	 * @throws SystemException As a fault.
	 */
	public void instantiateCommand() throws SystemException {
		// Check the definitation.
		definitionCheck();
		
		// Create the fresh instance.
		commandInstance = new ReadWriteableAttributes();
	}
		
	/**
	 *  Create a new instance object and instantiate.  This requires the named command to have been defined by something 
	 *  else in the system already.  This is what a command service should do when it receives a Command.  This should ONLY be done by a command service.
	 * @param commandName The command class name.
	 * @param responder the responder.
	 * @throws SystemException As a fault.
	 */
	public static Command instantiateResponse(String commandName, CommandResponder responder) throws SystemException {
		
		// Validate
		if (commandName==null) 
			throw new SystemException("Command Name cannot be null.", SystemException.SYSTEM_COMMAND_FAULT_INSTANCE_DATA_BAD);
		if (responder==null) 
			throw new SystemException("CommandResponder cannot be null.", SystemException.SYSTEM_COMMAND_FAULT_INSTANCE_DATA_BAD, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, commandName);
		
		// Do it
		Command resultCommand = null;
		
		// Try to get a definition.  This should exist, since another ob
		if (mYCommandRegistry.has(commandName)) {
			
			CommandDefinition theDefinition = null; 	// This should be vetted for null on the forName.
			try  {
			
				// It's defined.  Try to make one.
				theDefinition = mYCommandRegistry.get(commandName);	
				Class<?> t = Class.forName(theDefinition.className);
				resultCommand = (Command) t.newInstance();
				
				// Instantiate it
				resultCommand.instantiateResponse(responder);
	
			} catch (NullPointerException  npe) {
				throw new SystemException("Command could not instantiate because command not defined.  This is likely a bug.",SystemException.SYSTEM_COMMAND_FAULT_NOT_DEFINED,SystemNamespace.ATTR_SYSTEM_COMMAND_NAME,commandName);				
				
			} catch (ClassNotFoundException  cnf) {
				throw new SystemException("Command could not instantiate object because class not found.",SystemException.SYSTEM_COMMAND_FAULT_CLASS_NOT_FOUND,SystemNamespace.ATTR_PLATFORM_CLASS_PROPOSED,theDefinition.className,SystemNamespace.ATTR_SYSTEM_COMMAND_NAME,commandName);				
				
			} catch (ClassCastException cce) {
				throw new SystemException("Command could not instantiate object because specified class not a Command.",SystemException.SYSTEM_COMMAND_FAULT_CLASS_NOT_FOUND,SystemNamespace.ATTR_PLATFORM_CLASS_PROPOSED,theDefinition.className,SystemNamespace.ATTR_SYSTEM_COMMAND_NAME,commandName);				
				
			} catch (SystemException se) {
				throw se;
				
			} catch (Throwable t) {
				throw new SystemException("Command could not instantiate due to general exception.",SystemException.SYSTEM_COMMAND_FAULT,t,SystemNamespace.ATTR_SYSTEM_COMMAND_NAME,commandName);								
			}
	
		} else {
			throw new SystemException("Instance fault; could not find definition.  This is a LOCAL implementation, so it should have been defined by a Commander somewhere in the VM.", SystemException.SYSTEM_COMMAND_FAULT_NOT_DEFINED, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, commandName);					
		} // end if

		return resultCommand;
	}
		
	/**
	 *  Create a new response instance.  This is what a command service should do.
	 *  This should ONLY be done by a command service.
	 *  @param responder the responder.
	 *  @throws SystemException For any problems, though it is likely a fault.
	 */
	private void instantiateResponse(CommandResponder responder) throws SystemException {
		
		// Validate
		if (responder==null) 
			throw new SystemException("CommandResponder cannot be null.", SystemException.SYSTEM_COMMAND_FAULT_INSTANCE_DATA_BAD, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, this.named());		
		// Check the definitation.
		definitionCheck();
		
		// Create the fresh instance.
		commandResponder = responder;
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == INSTANCE CHECKS
	
	/**
	 * Is this a command instance?  Check to see if it has been make a response instance and throw an exception.  Otherwise, define and instantiate it.
	 */
	private void isCommandInstance() throws SystemException {
		if (commandInstance != null) return;
		if (commandResponder != null) SystemException.softwareProblem("This was not a command instance.  There is a bug.  Either the super for the command is not well formed or it called a method it shouldn't.");
		instantiateCommand();
	}

	/**
	 * Is this a response instance?  If not, it will throw an exception.
	 */
	private void isResponseInstance() throws SystemException {
		if (commandResponder == null) SystemException.softwareProblem("This was not a command responder instance.  There is a bug.  The command service should not have let a reponse method be called without instantiating this as a command response.");
	}
	
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Get the instance data for the command.  it will validate it.  It will check the data and throw an exception if it is not good. 
	 * @throws things.thinger.SystemException
	 * @return an Attribute reader that will transport the command.
	 */
	public AttributeReader getInstanceData() throws SystemException {
		
		// Validate we even have an instance (or make it if we have to).  This will cause a definition check.
		isCommandInstance();
		
		// Validate the command.
		// Given the SET-time check, we only need to check for REQUIRED parameters.
		checkRequiredData(commandInstance, CheckType.COMMAND, cachedDefinition);
		
		// Ok, return it.
		return commandInstance;
	}
	
	/**
	 * Get the command definition.  Don't monkey with it!
	 * @return the definition.
	 * @throws SystemException
	 */
	public CommandDefinition getDefinition() throws SystemException {
		definitionCheck();
		return cachedDefinition;
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == TOOLS
	
	/**
	 * Validate data for required items.  It will throw an exception for any problem.  The following codes are significant:<p>
	 * <pre>
	 * SYSTEM_COMMAND_FAULT_INSTANCE_USED_BEFORE_READY          : Null attributes.<br>
	 * SYSTEM_COMMAND_ERROR_INSTANCE_MISSING_REQUIRED_PARAMETER : Missing required parameter.<br>
	 * </pre>
	 * @param attributes Attributes to check.  
	 * @param type Are we checking the parameters or the responses?
	 * @param definition The command definition to check against.
	 * @throws things.thinger.SystemException
	 */
	public static void checkRequiredData(AttributeReader attributes, CheckType	type, CommandDefinition definition) throws SystemException {
		
		// Validate we even have an instance.
		if ((attributes==null)||(type==null)||(definition==null)) 
			throw new SystemException("Null attributes not allowed.", SystemException.SYSTEM_COMMAND_FAULT_INSTANCE_USED_BEFORE_READY, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name);
		
		// What type are we validating
		LinkedHashMap<String, CommandItem>	typeToValidate;
		if (type == CheckType.COMMAND) typeToValidate = definition.parameters;
		else typeToValidate = definition.responses;
		
		// Validate the command.
		for (CommandItem item : typeToValidate.values()) {
			if (item.myRequirement == Requirement.REQUIRED) {
				// It is required.  If it is not there, that's a BAD THING.
				if ((attributes==null)||(!attributes.hasAttribute(item.name))) {
					// Not there.  This is an error
					throw new SystemException("Missing required.  " + item.name, SystemException.SYSTEM_COMMAND_ERROR_INSTANCE_MISSING_REQUIRED_PARAMETER, 
							SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, item.name);
				}
			}
		}
	}
	
	/**
	 * Validate data for occurence and type.  It will throw an exception for any problem.  The following codes are significant:<p>
	 * <pre>
	 * SYSTEM_COMMAND_FAULT_INSTANCE_USED_BEFORE_READY          : Null attributes.<br>
	 * SYSTEM_COMMAND_ERROR_INSTANCE_MISSING_REQUIRED_PARAMETER : Missing required parameter.<br>
	 * </pre>
	 * @param attributes Attributes to check.  
	 * @param type Are we checking the parameters or the responses?
	 * @param definition The command definition to check against.
	 * @throws things.thinger.SystemException
	 */
	public static void checkDataForm(AttributeReader attributes, CheckType	type, CommandDefinition definition) throws SystemException {
		
		// Validate we even have an instance.
		if ((attributes==null)||(type==null)||(definition==null)) 
			throw new SystemException("Null attributes.", SystemException.SYSTEM_COMMAND_FAULT_INSTANCE_USED_BEFORE_READY, SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name);
		
		// What type are we validating
		LinkedHashMap<String, CommandItem>	typeToValidate;
		if (type == CheckType.COMMAND) typeToValidate = definition.parameters;
		else typeToValidate = definition.responses;
		
		// Validate the command.
		int valueCount = 0;
		CommandItem cachedItem = null;
		for (String name : attributes.getAttributeNames()) {
			valueCount = attributes.getAttributeCount(name);
			cachedItem = typeToValidate.get(name);
			if (valueCount > 1) {
				// It's a multi and see if it is allowed.
				switch (cachedItem.myOccurence) {
				case NEVER: 
					throw new SystemException("Found an attribute defined as NEVER.", SystemException.SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION_NEVER, 
							SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name);
				case ONLYONE:
					throw new SystemException("Found multiple attribute values for attribute defined as ONLYONE.", SystemException.SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION_ONLYONE, 
							SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_DATA_ATTRIBUTE_VALUE_COUNT, Integer.toString(valueCount));				
				case MANY:
				case WHATEVER:
				default:
					// OK - check type.  We only care if it has been declared a VALUE but has multivalue.
					if (cachedItem.myDataType == DataType.VALUE) {
						for (NVImmutable cachedAttrib : attributes.getAttributes(name)) {
							if (cachedAttrib.isMultivalue()) 
								throw new SystemException("Found a LIST attribute values for attribute defined as VALUE.", SystemException.SYSTEM_COMMAND_ERROR_DATATYPE_VIOLATION_VALUE, 
										SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_DATA_ATTRIBUTE_VALUE_COUNT );		
						}
					} // end if
					break;
				} // end switch
				
			} else {
				// It's a single
				switch (cachedItem.myOccurence) {
				case NEVER: 
					throw new SystemException("Found an attribute defined as NEVER.", SystemException.SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION_NEVER, 
							SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name);
				case ONLYONE:
				case MANY:
				case WHATEVER:
				default:
					// OK - check type.  We only care if it has been declared a VALUE but has multivalue.
					if (cachedItem.myDataType == DataType.VALUE) {
						for (NVImmutable cachedAttrib : attributes.getAttributes(name)) {
							if (cachedAttrib.isMultivalue()) 
								throw new SystemException("Found a LIST attribute values for attribute defined as VALUE.", SystemException.SYSTEM_COMMAND_ERROR_DATATYPE_VIOLATION_VALUE, 
										SystemNamespace.ATTR_SYSTEM_COMMAND_NAME, definition.name, SystemNamespace.ATTR_SYSTEM_COMMAND_PARAMETER_NAME, name, SystemNamespace.ATTR_DATA_ATTRIBUTE_VALUE_COUNT );		
						}
					} // end if
					break;
				} // end switch

			} // end if

		} // end for
	}
	
	// =======================================================================================================
	// == CONSTRUCTORS
	
	/**
	 *  Default constructor.  This will use the system global registry.
	 */
	public Command() {
		// If default, use a local one.
		mYCommandRegistry = LocalCommandRegistry.getGlobalRegistryStatic();
	}	

	/**
	 *  Default constructor.
	 *  @param imposedRegistry This will set which registry to use.
	 */
	public Command(CommandRegistry imposedRegistry) {
		mYCommandRegistry = imposedRegistry;
	}	
	

	// =======================================================================================================
	// == HELP
	
	/**
	 * Validate this is a valid receipt list.  If it isn't, it will throw an exception.
	 * @param result The receipt list to examine.
	 * @throws things.thinger.SystemException
	 */
	public void validateReceiptList(ReceiptList result) throws SystemException {
		
		// Iterate the list
		for (Receipt item : result) {
			
			// Every receipt must be terminal and ok
			if (!item.getType().isOk()||!item.getType().isTerminal());
			throw new SystemException("Command response failed while sending.  The transmission receipt was not OK or TERMINAL.  This is a very bad Thinger problem.", SystemException.PANIC_SYSTEM_COMMAND_GENERAL_RESPONSE_TRANSMISSION,
					SystemNamespace.ATTR_SYSTEM_COMMAND_RESPONSE_TRANSMISSION_RECEIPT, item.toString());	
		}
	}
	
	// =======================================================================================================
	// == PRIVATE HELPERS
	

	
}
