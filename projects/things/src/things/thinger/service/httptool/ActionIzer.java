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
package things.thinger.service.httptool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import things.common.PuntException;
import things.common.ThingsCodes;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsExceptionBundle;
import things.common.ThingsNamespace;
import things.common.tools.Plato;
import things.data.Accessor;
import things.data.ThingsProperty;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyView;
import things.data.impl.StringAccessor;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTrunkIO;
import things.thinger.SystemInterface;

/**
 * ActionIzer helper wrapper for Actions.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 2 NOV 08
 * EPG - Make Save and Load files man-readable. - 11 Oct 09
 * </pre> 
 */
public abstract class ActionIzer extends Action {

	// =================================================================================================
	// == FIELDS
	public final static String CHECKED_VALUE = "checked";
	public final static String VERSION_NOTE = "#ActionIzerversion 1.0";
	public final static String COMMENT_LINE = "# ";
	public final static String PROP_LINE_START = "\\START:";
	public final static String PROP_LINE_END = "\\END:";
	public final static char NAME_VALUE_SNAP = ':';
	
	// =================================================================================================
	// == DATA
	
	// -- Static per class data used for the declarations.
	private static HashMap<String,ActionIzerItem> items;
	private static Collection<ActionIzerItem> itemsCollection;	// Since the collection is fixed after declaration, go ahead and make a collection ready.
	private static HashSet<String>	allActionValues;
	private static String	actionParam = null;
	private static boolean  errorOnUnknown = false;
	private static String	saveActionValue = null;
	private static String	loadActionValue = null;
	private static String	loadsavePathParam = null;
	private static String	loadsaveTag = null;
	private static String 	universeName = null;
	
	// -- Data used per usage.  This data should be considered invalid once exiting process().

	
	// =================================================================================================
	// == ABSTRACT METHODS

	/**
	 * Give the sub class a chance to make declarations.  Never call this directly.   It'll be called once when the class is loaded.
	 * @throws Throwable
	 */
	public abstract void declarations() throws Throwable;
	
	/**
	 * Allow the subclass to process undeclared parameters.  
	 * @param actualParameters Use this instead of the superclass 'parameter's so that load functionality will work.
	 * @param localProperties local properties.
	 * @param saveParameters all parameters that may be saved should be set here.   Remember, these are PARAMETERS, as found in actualParameters, not Properties, as found in localProperties.
	 * @param exceptions Add non-fatal exceptions here, things you'd rather have rendered within the page rather than given to the platform.
	 * @throws Throwable an fatal exceptions.  It'll let the platform give the error screen,
	 */
	protected abstract void processUndeclaredParameters(ThingsPropertyView actualParameters, ThingsPropertyView localProperties, ThingsPropertyView saveParameter, ThingsExceptionBundle<ThingsException> exceptions) throws Throwable;
	
	/**
	 * Allow the subclass render undeclared.
	 * @throws Throwable any exception  (exception interruptions) will be trapped in the bundle for later examination.  Exceptions that aren't ThingsException will be promoted to a FAULT.
	 */
	protected abstract void renderUndeclared() throws Throwable;
		
	/**
	 * Allow the subclass render undeclared default values.
	 * @throws Throwable any exception (exception interruptions) will be trapped in the bundle for later examination.  Exceptions that aren't ThingsException will be promoted to a FAULT.
	 */
	protected abstract void renderUndeclaredDefault() throws Throwable;
	
	/**
	 * Render any message for a save.  Keep all errors and exceptions and render them if you want.
	 * @param exceptions problems that may have happened during automated processing.
	 * @return the action result
	 */
	protected abstract ActionResult renderSave(ThingsExceptionBundle<ThingsException> exceptions);
	
	/**
	 * Render any message for a load.  Keep all errors and exceptions and render them if you want.
	 * @param exceptions problems that may have happened during automated processing.
	 * @return the action result
	 */
	protected abstract ActionResult renderLoad(ThingsExceptionBundle<ThingsException> exceptions);
	
	/**
	 * Do the action.
	 * @param action The action parameter's value.  Null if it wasn't set or found in the post.
	 * @param localProperties local properties derived from the items.
	 * @param values the values of the declared items.
	 * @param exceptions problems that may have happened during automated processing.
	 * @return the action result.
	 * @throws Throwable these exceptions will not be trapped.
	 */
	protected abstract ActionResult doAction(String action, ThingsPropertyView localProperties, HashMap<String, ActionIzerItemValue> values, ThingsExceptionBundle<ThingsException> exceptions) throws Throwable;
	
	/**
	 * Get the default action.
	 * @return the default action result.
	 * @throws Throwable any exception (exception interruptions) will be trapped in the bundle for later examination.
	 */
	protected abstract ActionResult defaultAction() throws Throwable;
	
	/**
	 * Manage errors.  Render them yourself or let them fly to the system itself.
	 * @param exceptions the final exception bundle.
	 * @param result the current result.
	 * @throws Throwable unlike the other methods, this will let exceptions fly back to the Action.  
	 */
	protected abstract void manageErrors(ThingsExceptionBundle<ThingsException> exceptions, ActionResult result) throws Throwable;
	
	
	// =================================================================================================
	// == DECLARATION METHODS

	/**
	 * Declare a string parameter.
	 * @param name.  it just be unique per ActionIzer class.
	 * @param defaultValue
	 * @param required
	 * @param tag tag name.  If null, there is no associated tag name.
	 * @param propertyName property name.  If null, there is no associated property name.
	 * @throws Throwable
	 */
	protected void DECLARE(String name, String defaultValue, boolean required, String tag, String propertyName) throws Throwable {
		DECLARE_TYPED(name, defaultValue, required, ActionIzerItem.Type.STRING, tag, propertyName);
	}
	
	/**
	 * Declare a checked boolean parameter.
	 * @param name.  it just be unique per ActionIzer class.
	 * @param defaultValue
	 * @param required
	 * @param trueValue the text value that indicates true
	 * @param falseValue the text value that indicates true
	 * @param trueTag associated tag name for the true value
	 * @param falseTag associated tag name for the false value
	 * @param tag tag name.  If null, there is no associated tag name.
	 * @param propertyName property name.  If null, there is no associated property name.
	 * @throws Throwable
	 */
	protected void DECLARE_BOOLEAN(String name, boolean defaultValue, boolean required, String trueValue, String falseValue, String trueTag, String falseTag, String propertyName) throws Throwable {
		ActionIzerItem item = DECLARE_TYPED(name, trueValue, required, ActionIzerItem.Type.BOOLEAN, trueTag, propertyName);
		item.tag_false = falseTag;
		item.valueFalse = falseValue;
		item.booleanValue = defaultValue;
	}
	
	/**
	 * Declare a property parameter.
	 * @param name.  it just be unique per ActionIzer class.
	 * @param defaultValue
	 * @param required
	 * @param tag tag name.  If null, there is no associated tag name.
	 * @throws Throwable
	 */
	protected void DECLARE_PROPS(String name, boolean defaultValue, boolean required, String tag) throws Throwable {
		DECLARE_TYPED(name, "", required, ActionIzerItem.Type.PROPERTIES, tag, null);
	}
	
	/**
	 * Declare by type.
	 * @param name the name.  it just be unique per ActionIzer class.
	 * @param defaultValue
	 * @param required
	 * @param type the type.
	 * @param tag tag name.  If null, there is no associated tag name.
	 * @param propertyName property name.  If null, there is no associated property name.
	 * @throws Throwable
	 */
	public ActionIzerItem DECLARE_TYPED(String name, String defaultValue, boolean required, ActionIzerItem.Type type, String tag, String propertyName) throws Throwable {
		if (itemsCollection!=null) throw new ThingsException("Declaration is closed.  You cannot call DECLARE_TYPED.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (name==null) throw new ThingsException("Cannot have a null name.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (name.trim().length()<1) throw new ThingsException("Cannot have an empty name.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (defaultValue==null) throw new ThingsException("Cannot have a null defaultValue.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (items.containsKey(items)) throw new ThingsException("Name has already been declared.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE, ThingsNamespace.ATTR_DATA_ATTRIBUTE_NAME, name);
		if ((tag!=null)&&(tag.trim().length()<1)) throw new ThingsException("Cannot have an empty tag.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		ActionIzerItem item = new ActionIzerItem(name,defaultValue,required, type, tag, propertyName);
		items.put(name,item);
		return item;
	}
	
	/**
	 * Set the action parameter.
	 * @param action the action parameter
	 * @param errorOnUnknown if true, it will generate an error if it encounters and unknown param.
	 * @param actions possible values.
	 * @throws Throwable
	 */
	public void SET_ACTION_PARAM(String action, boolean errorOnUnknown, String... actions) throws Throwable {
		if (itemsCollection!=null) throw new ThingsException("Declaration is closed.  You cannot call DECLARE_TYPED.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (action==null) throw new ThingsException("Cannot have a null action.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (action.trim().length()<1) throw new ThingsException("Cannot have an empty action.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		actionParam = action;
		if (actions!=null) {
			for (String item : actions) {
				if ((item!=null)&&(item.trim().length()>0)) allActionValues.add(item.trim());
			}
		}
	}
	
	/**
	 * Set the action parameter value to indicate a save as well as the name of the parameter that holds the path for the save.
	 * @param actionValue the value to indicate the save action.
	 * @param universe name of the universe in which we will save.  This will overlap with SET_ACTION_LOAD, so keep them in sync.
	 * @param pathParam the parameter with the path information.  This will overlap with SET_ACTION_LOAD, so keep them in sync.
	 * @param tag the tag for rendering the path information.  .  This will overlap with SET_ACTION_LOAD, so keep them in sync.
	 * @throws Throwable
	 */
	public void SET_ACTION_SAVE(String actionValue, String universe, String pathParam, String tag) throws Throwable {
		if (itemsCollection!=null) throw new ThingsException("Declaration is closed.  You cannot call DECLARE_TYPED.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (actionValue==null) throw new ThingsException("SET_ACTION_SAVE: Cannot have a null actionValue.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (actionValue.trim().length()<1) throw new ThingsException("SET_ACTION_SAVE: Cannot have a empty actionValue.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (universe==null) throw new ThingsException("SET_ACTION_SAVE: Cannot have a null universe.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (universe.trim().length()<1) throw new ThingsException("SET_ACTION_SAVE: Cannot have a empty universe.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (pathParam==null) throw new ThingsException("SET_ACTION_SAVE: Cannot have a null param.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (pathParam.trim().length()<1) throw new ThingsException("SET_ACTION_SAVE: Cannot have a empty pathParam.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (tag==null) throw new ThingsException("SET_ACTION_SAVE: Cannot have a null tag.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (tag.trim().length()<1) throw new ThingsException("SET_ACTION_SAVE: Cannot have a empty tag.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		saveActionValue = actionValue.trim();
		loadsavePathParam = pathParam.trim();
		loadsaveTag = tag.trim();
		allActionValues.add(actionValue.trim());
		universeName = universe.trim();
	}
	
	/**
	 * Set the action parameter value to indicate a save as well as the name of the parameter that holds the path for the save.
	 * @param actionValue the value to indicate the save action.
	 * @param universe name of the universe in which we will save.  This will overlap with SET_ACTION_LOAD, so keep them in sync.
	 * @param pathParam the parameter with the path information.  This will overlap with SET_ACTION_SAVE, so keep them in sync.
	 * @param tag the tag for rendering the path information.  .  This will overlap with SET_ACTION_SAVE, so keep them in sync.
	 * @throws Throwable
	 */
	public void SET_ACTION_LOAD(String actionValue, String universe, String pathParam, String tag) throws Throwable {
		if (itemsCollection!=null) throw new ThingsException("Declaration is closed.  You cannot call DECLARE_TYPED.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (actionValue==null) throw new ThingsException("SET_ACTION_LOAD: Cannot have a null actionValue.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (actionValue.trim().length()<1) throw new ThingsException("SET_ACTION_LOAD: Cannot have a empty actionValue.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (universe==null) throw new ThingsException("SET_ACTION_SAVE: Cannot have a null universe.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (universe.trim().length()<1) throw new ThingsException("SET_ACTION_SAVE: Cannot have a empty universe.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (pathParam==null) throw new ThingsException("SET_ACTION_LOAD: Cannot have a null param.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (pathParam.trim().length()<1) throw new ThingsException("SET_ACTION_LOAD: Cannot have a empty pathParam.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (tag==null) throw new ThingsException("SET_ACTION_LOAD: Cannot have a null tag.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		if (tag.trim().length()<1) throw new ThingsException("SET_ACTION_LOAD: Cannot have a empty tag.", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE);
		loadActionValue = actionValue.trim();
		loadsavePathParam = pathParam.trim();
		loadsaveTag = tag.trim();
		allActionValues.add(actionValue.trim());
		universeName = universe.trim();
	}
	
	
	// =================================================================================================
	// == METHODS
	
	/**
	 * Constructor.
	 * @throws Throwable if the declarations failed.
	 */
	public ActionIzer() throws Throwable {
		
		// Make sure this guy has declared.
		synchronized(this.getClass()) {
			if (items==null) {
				try {
					
					// Set it up
					items = new HashMap<String,ActionIzerItem>();
					allActionValues = new HashSet<String>();
					declarations();
					itemsCollection = items.values();
					
					// Qualify it
					if (actionParam==null) throw new Exception("You must set an action parameter during declarations with the SET_ACTION_PARAM method.");
					
				} catch (Throwable t) {
					throw new ThingsException("Could not process declatations for this ActionIzer", ThingsCodes.SERVICE_HTTPTOOL_ACTIONIZER_DECLARATION_FAILED, t, ThingsNamespace.ATTR_PLATFORM_CLASS, this.getClass().getCanonicalName());
				}
			}
		}
		
	}
	
	// =================================================================================================
	// == OVERLOADED / IMPLEMENTED METHODS
	
	private ThingsPropertyTreeBASIC localProperties;
	private ThingsPropertyView localPropertiesView;
	private ThingsPropertyView savePropertiesView;
	private ThingsExceptionBundle<ThingsException> exceptions;
	private HashMap<String, ActionIzerItemValue> itemValues;
	
	/**
	 * The implementation.  Assume the DATA is available.
	 */
	protected ActionResult process() throws Throwable {
		
		exceptions = new ThingsExceptionBundle<ThingsException>();
		ActionResult result = null;
		
 		String currentActionParam = parameters.getProperty(actionParam);
		if ((currentActionParam==null)||(currentActionParam.trim().length()<1)) {
			
			// DEFAULT page
			for (ActionIzerItem item : itemsCollection ) {
				
				if (item.tag!=null) {
					
					switch(item.type) {
					
					case STRING:
					case PROPERTIES:
						if (item.value != null) {
							tags.setProperty(item.tag, item.value);		
						}
						break;
					
					case BOOLEAN:
						if (item.booleanValue) {
							tags.setProperty(item.tag, CHECKED_VALUE);
							tags.setProperty(item.tag_false, "");		
						} else {
							tags.setProperty(item.tag, "");
							tags.setProperty(item.tag_false, CHECKED_VALUE);				
						}
						break;
						
					case CLASSED:
					case CHECKED:
					default:
						// Not implemented
						break;
						
					} // end switch
				
				} // end if has a tag

			}
			if (loadsavePathParam!=null) tags.setProperty(loadsaveTag, "");
			
			// Let sub kick in the undeclared
			renderUndeclaredDefault();
			
			// Result
			result = defaultAction();
			
		} else {
			
			// -- Process declared, set up local values, and process undeclared.  ----------------------------------------
			localProperties = new ThingsPropertyTreeBASIC();
			localPropertiesView = localProperties.getRoot();
			savePropertiesView = new ThingsPropertyTreeBASIC().getRoot();
			
			itemValues = processDeclared(parameters);
			processUndeclaredParameters(parameters, localPropertiesView, savePropertiesView, exceptions);
			
			// -- Do action  ----------------------------------------
			String actionValue = null;
			if (actionParam!=null) {
				actionValue = parameters.getProperty(actionParam);
				if ((errorOnUnknown)&&(!allActionValues.contains(actionValue))) {
					exceptions.add(new ThingsException("Unknown ACTION.", ThingsCodes.SERVICE_HTTPTOOL_BAD_REQUEST, ThingsNamespace.ATTR_DATA_VALUE, actionValue));
				}
			}
			
			try {

				if (actionValue.equals(saveActionValue)) {
					doSave(parameters.getProperty(loadsavePathParam), exceptions);
					result = renderSave(exceptions); 
				} else if (actionValue.equals(loadActionValue)) {
					String lspath = parameters.getProperty(loadsavePathParam);
					exceptions = new ThingsExceptionBundle<ThingsException>();			// For load only, forgive all previous exceptions, since we're reloading the page.
					doLoad(lspath, exceptions);
					if (lspath!=null) parameters.setProperty(loadsavePathParam, lspath);  // The load will have wiped it out.  
					result = renderLoad(exceptions); 
				} else {
					result = doAction(actionValue, localPropertiesView, itemValues, exceptions);
				}
				
			} catch (ThingsException te) {
				exceptions.add(te);
			} catch (InterruptedException ie) {
				throw  ie;
			} catch (Throwable t) {
				exceptions.add(new ThingsException("Spurious exception during processing.", ThingsCodes.SERVICE_FAULT_HTTPTOOL_SPURIOUS, t));

			}
			
			// -- render managed  ---------------------------------------------------------
			if (loadsavePathParam!=null) {
				String lsValue = parameters.getProperty(loadsavePathParam);
				if (lsValue==null)	lsValue = "";
				tags.setProperty(loadsaveTag, lsValue.trim());
			}

			// -- render declared  ---------------------------------------------------------
			for (ActionIzerItemValue item : itemValues.values() ) {
				
				switch(item.type) {
				
				case BOOLEAN:
					if (item.tag!=null) {
						tags.setProperty(item.tag, ActionIzerItem.BOOLEAN__SELECTED_VALUE);
					}
					break;
					
				case STRING:
				case PROPERTIES:
					if (item.tag!=null) {
						tags.setProperty(item.tag, item.value);
					}
					break;

				case CLASSED: 
				case CHECKED:
				default:
					// Do nothing.
					break;
				}
	
			} // end for items to process
			
			// -- render undeclared  ---------------------------------------------------------
			try {
				renderUndeclared();
			} catch (ThingsException te) {
				exceptions.add(te);
			} catch (InterruptedException ie) {
				throw  ie;
			} catch (Throwable t) {
				exceptions.add(new ThingsException("Spurious exception during rendering.", ThingsCodes.SERVICE_FAULT_HTTPTOOL_SPURIOUS, t));
			}
			
			// -- manage mode renderings  ---------------------------------------------------------
			manageErrors(exceptions, result);

		}
		
		return result;
	}
	
	/**
	 * Overload this if you want to manage HEAD processing on your own.  Be careful with these!  They will not render a default page if the
	 * head processing fails.
	 * @param parameters This is what comes from the commands.
	 * @param tags This is what will be merged to the result page.
	 * @param si The system interface.
	 * @return the head.  The default implementation returns a null which will let the server manage it (and almost always invalidate the cache).
	 * @throws Throwable
	 */
	public Head head(ThingsPropertyView  parameters, ThingsPropertyView  tags, SystemInterface si) throws Throwable {
		return null;
	}
	

	// =================================================================================================
	// == INTERNAL

	
	/**
	 * Process the declared.
	 * @param actualParameters
	 * @return the processed items
	 * @throws Throwable
	 */
	private HashMap<String, ActionIzerItemValue> processDeclared(ThingsPropertyView actualParameters) throws Throwable {
		
		// -- Process declared and set up local values.  ----------------------------------------
		itemValues = new HashMap<String, ActionIzerItemValue>();
		ActionIzerItemValue valueItem;
		String data;
		
		for (ActionIzerItem item : itemsCollection ) {
			
			switch(item.type) {
			case STRING:
				data = actualParameters.getProperty(item.name);
				if ((item.tag!=null)&&(data != null)) {
					tags.setProperty(item.tag, data);	
					itemValues.put(item.name, new ActionIzerItemValue(item.name, data, false, item.tag, item.type));
					if (item.propertyName!=null) localPropertiesView.setProperty(item.propertyName, data);
				}
				break;
				
			case BOOLEAN:
				data = actualParameters.getProperty(item.name);
				if (data.equals(item.value)) {
					if (item.propertyName!=null) localPropertiesView.setProperty(item.propertyName, Plato.TRUE_STRING);
					valueItem = new ActionIzerItemValue(item.name, data, true, item.tag, item.type);
					itemValues.put(item.name, valueItem);
				} else if (data.equals(item.valueFalse)) {
					if (item.propertyName!=null) localPropertiesView.setProperty(item.propertyName, Plato.FALSE_STRING);
					valueItem = new ActionIzerItemValue(item.name, data, false, item.tag_false, item.type);
					itemValues.put(item.name, valueItem);
				}
				break;
				
			case PROPERTIES:
				data = actualParameters.getProperty(item.name);
				if (data==null) data = "";
				if ( data.trim().length()>0 ){
					try {
						ThingsPropertyTrunkIO trunk = new ThingsPropertyTrunkIO();
						trunk.init("", new StringAccessor(data));
						localProperties.infliltrate(trunk);
						localProperties.load();	
					} catch (Throwable t) {
						exceptions.add(new ThingsException("Bad VALUES FOR VARIABLE REPLACEMENT.", ThingsCodes.SERVICE_HTTPTOOL_ERROR, t, ThingsNamespace.ATTR_PARAMETER_NAME, item.name));
					}
				}  
				if (item.tag!=null) itemValues.put(item.name, new ActionIzerItemValue(item.name, data, false, item.tag, item.type));
				break;
				
			case CLASSED: 
			case CHECKED:
			default:
				// Do nothing.
				break;
			}

		} // end for items to process
		
		return itemValues;
	}
	
	
	/**
	 * Save.
	 * @param path path into the configured universe.
	 * @param exceptions exceptions bundle.
	 * @throws Throwable
	 */
	private void doSave(String path, ThingsExceptionBundle<ThingsException> exceptions) throws Throwable {
		BufferedWriter bw = null;
		OutputStream oos = null;
		
		// We're going to save these our way.
		Accessor objectAccessor = null;
		try {	
			objectAccessor = si.getUniverse(universeName).getObjectAccessor(path);
			oos = objectAccessor.openForWrite();
			bw = new BufferedWriter(new OutputStreamWriter(oos));
		} catch (Throwable t) {
			exceptions.add(new ThingsException("Could not open the save file", ThingsCodes.ACCESS_ERROR_CANNOT_OPEN, t, ThingsNamespace.ATTR_UNIVERSE_NAME, universeName, ThingsNamespace.ATTR_UNIVERSE_NAME));
		}

		// Write 'em
		if (bw!=null) {	

			try {
				bw.write(VERSION_NOTE);
				bw.newLine();
				bw.write(COMMENT_LINE);
				bw.newLine();
				
				for (ActionIzerItemValue item : itemValues.values()) {
					
					if (item.type==ActionIzerItem.Type.PROPERTIES) {
						
						// Header
						bw.write(PROP_LINE_START);
						bw.write(item.name);
						bw.newLine();
								
						// Data
						bw.write(item.value.trim());
						bw.newLine();					
						
						// Footer
						bw.write(PROP_LINE_END);
						bw.write(item.name);
						bw.newLine();	
						
					} else {
						// Just plain
						bw.write(item.name);
						bw.write(':');
						ThingsPropertyReaderToolkit.encodeString(bw, item.value);
						bw.newLine();
						
					}
				}
				for (String itemName : savePropertiesView.sub(null)) {
					bw.write(itemName);
					bw.write(':');
					ThingsPropertyReaderToolkit.encodeString(bw, savePropertiesView.getProperty(itemName));	
					bw.newLine();
				}
				bw.newLine();
				
			} catch (Throwable t) {
				exceptions.add(new ThingsException("Error while writing save file", ThingsCodes.IO_FILESYSTEM_FAULT_GENERAL, t, ThingsNamespace.ATTR_UNIVERSE_NAME, universeName, ThingsNamespace.ATTR_UNIVERSE_NAME));
			}
		}

		try {
			bw.close();
		} catch (Throwable t) {
			// Ignore
		}
		try {
			objectAccessor.doneWithWrite(oos);
		} catch (Throwable t) {
			// Ignore
		}
	}

	/**
	 * Load.
	 * @param path path into the configured universe.
	 * @param exceptions exceptions bundle.
	 * @throws Throwable
	 */
	private void doLoad(String path, ThingsExceptionBundle<ThingsException> exceptions) throws Throwable {
		BufferedReader br = null;
		InputStream is = null;
		Accessor objectAccessor = null;
		
		// Save these in case something goes wrong
		ThingsPropertyTreeBASIC localProperties_backup = localProperties;
		ThingsPropertyView localPropertiesView_backup = localPropertiesView;
		//ThingsPropertyView savePropertiesView_backup = savePropertiesView;
		HashMap<String, ActionIzerItemValue> itemValues_backup = itemValues;
		
		// New local props
		localProperties = new ThingsPropertyTreeBASIC();
		localPropertiesView = localProperties.getRoot();
		
		// New parameters
		ThingsPropertyTreeBASIC localParameters= new ThingsPropertyTreeBASIC();
		ThingsPropertyView localParametersView = localParameters.getRoot();
		
		// Outer
		try {
			
			// Access the load file
			try {	
				objectAccessor = si.getUniverse(universeName).getObjectAccessor(path);
				is = objectAccessor.openForRead();
				br = new BufferedReader(new InputStreamReader(is));
			} catch (Throwable t) {
				exceptions.add(new ThingsException("Could not open the load file", ThingsCodes.ACCESS_ERROR_CANNOT_OPEN, t, ThingsNamespace.ATTR_UNIVERSE_NAME, universeName, ThingsNamespace.ATTR_UNIVERSE_NAME));
				throw new PuntException();
			}
			
			// Build the new parameters
			String current = br.readLine();
			String name = null;
			int snap;
			while(current!=null) {
				
				if (current.indexOf(PROP_LINE_START)==0) {
					// Props
					name = current.substring(PROP_LINE_START.length()).trim();
					StringBuffer accumulation = new StringBuffer();
					
					current = br.readLine();
					if (current==null) throw new Exception("Bad properties.  Left dangling without any entries for " + name);
					while(current.indexOf(PROP_LINE_END)!=0)  {
						accumulation.append(current);
						accumulation.append(ThingsConstants.CRLF);
						current = br.readLine();
						if (current==null) throw new Exception("Bad properties.  Left dangling for " + name);
					}
					
					localParametersView.setProperty(name, accumulation.toString());
					
				} else {
				
					// Normal line
					current = current.trim();
					snap = current.indexOf(NAME_VALUE_SNAP);
//					if ( (snap>0) && (current.charAt(0)!= ThingsProperty.PROPERTY_COMMENT_CHARACTER) && (snap<(current.length()-1))) {
					// Change this to clear out empty fields.
					if ( (snap>0) && (current.charAt(0)!= ThingsProperty.PROPERTY_COMMENT_CHARACTER)) {		
						
						name = current.substring(0, snap).trim();
						if (snap==(current.length()-1)) {
							// Empty.
							localParametersView.setPropertyMultivalue(name, "");							
						} else {
	 						// snap is greater than zero, so that means it is not an empty string.  
							current = current.substring(snap+1).trim();
							localParametersView.setPropertyMultivalue(name, ThingsPropertyReaderToolkit.decodeString(current));
						}
					}
				}
				
				current = br.readLine();
			}
			
			// Process new parameters
			itemValues = processDeclared(localParametersView);
			processUndeclaredParameters(localParametersView, localPropertiesView, savePropertiesView, exceptions);
			
			// Accept the load
			parameters = localParametersView;
			
		} catch (Throwable t) {
			
			// do not report PuntException as they already have been
			if ( ! (t instanceof PuntException) ) {
				exceptions.add(new ThingsException("Could process load file", ThingsCodes.ACCESS_ERROR_CANNOT_OPEN, t, ThingsNamespace.ATTR_UNIVERSE_NAME, universeName, ThingsNamespace.ATTR_UNIVERSE_NAME));			
			}
			
			// Restore
			localProperties = localProperties_backup;
			localPropertiesView = localPropertiesView_backup;
			itemValues = itemValues_backup;
			
		} finally {
			try {
				br.close();
			} catch (Throwable t) {
				// Ignore
			}
			try {
				objectAccessor.doneWithRead(is);
			} catch (Throwable t) {
				// Ignore
			}
		}
	
	}
	
}
