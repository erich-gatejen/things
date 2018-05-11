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
package things.universe.server;

import java.io.File;
import java.util.HashMap;
import java.util.ListIterator;

import things.common.StringPoster;
import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyView;
import things.thinger.kernel.Clearance;
import things.universe.Universe;
import things.universe.UniverseException;
import things.universe.UniverseID;
import things.universe.UniverseRegistry;
import things.universe.UniverseServer;

/**
 * A simple universe registry. It'll manage a properties based registry. The
 * configuration will snapshot'd when the registry starts. It will never save.
 * <p>
 * <b>REGISTRY DATA for ALL: </b> <br>
 * CONFIG_LIST list=u1,u2   List of names.  Names may not have whitespace.<br> 
 * CONFIG_ROOT root=u1 <br>
 * <p>
 * <b>REGISTRY DATA for UNIVERSE_LOCAL: </b> <br>
 * u1.id= <br>
 * u1.type=local <br>
 * u1.path= <br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 MAY 04
 * </pre> 
 */
public class UniverseRegistry_Simple implements UniverseRegistry {

    HashMap<String,UniverseServer> universeMapByName;
    HashMap<String,UniverseServer> universeMapById;
    boolean verbose = false;
    StringPoster vp = null;
    
    /**
     * Manufacture an accessor to a universe.
     * @param theUniverse ID for the universe to access
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
     */
    public Universe getAccessor(UniverseID theUniverse) throws UniverseException {
        Universe result = null;
        if (universeMapById==null) throw new UniverseException("Tried to get an accessor before the registry was loaded.",UniverseException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
        try {
            UniverseServer us = universeMapById.get(theUniverse.toString());
            result = us.getAccessor();
        } catch (Exception ee) {
            // Don't care.  We'll be returning null
        }
        return result;
    }

	/**
	 * Get an accessor to a universe by local name.
	 * @param name the name
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Universe getAccessor(String name) throws UniverseException {
        Universe result = null;
        if (universeMapByName==null) throw new UniverseException("Tried to get an accessor before the registry was loaded.",UniverseException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
        try {
            UniverseServer us = universeMapByName.get(name);
            result = us.getAccessor();
        } catch (Exception ee) {
            // Don't care.  We'll be returning null
        }
        return result;
	}
    
	/**
	 * Get an clearance for a universe by id.
	 * 
	 * @param theUniverse  ID for the universe to access
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Clearance getClearance(UniverseID theUniverse) throws UniverseException {
		Clearance result = Clearance.UNKNOWN;
        if (universeMapById==null) throw new UniverseException("Tried to get an Clearance before the registry was loaded.",UniverseException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
        try {
            UniverseServer us = universeMapById.get(theUniverse.toString());
            result = us.getClearance();
        } catch (Exception ee) {
            // Don't care.  We'll be returning null
        }
        return result;
	}
	
	/**
	 * Get an clearance for a universe by localName
	*
	 * @param name the local name
	 * @return a Universe or null if the universe doesn't exist ( or isn't loaded ).
	 */
	public Clearance getClearance(String name) throws UniverseException {
		Clearance result = Clearance.UNKNOWN;
        if (universeMapByName==null) throw new UniverseException("Tried to get an Clearance before the registry was loaded.",UniverseException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
        try {
            UniverseServer us = universeMapByName.get(name);
            result = us.getClearance();
        } catch (Exception ee) {
            // Don't care.  We'll be returning null
        }
        return result;	
	}
	
    /**
     * Register a universe.
     * 
     * @param type
     *            numeric as specific in this class
     * @param config
     *            information
     * @throws UniverseException
     */
    public void register(int type, ThingsPropertyView config) throws UniverseException {
        throw new UniverseException("UniverseRegistry_Simple does not allow new registration.", ThingsCodes.UNIVERSE_ERROR_REGISTRATION_NOT_ALLOWED);
    }

    /**
     * Load a registry from a configuration node. See documentation above for
     * expected properties.
     * 
     * @param config
     *            a configuration node
     * @throws UniverseException
     * @see things.data.ThingsPropertyView
     */
    public void loadRegistry(ThingsPropertyView config) throws UniverseException {

        if (verbose) vp.postit("UniverseRegistry_Simple loading registry");
        universeMapByName = new HashMap<String,UniverseServer>();
        universeMapById = new HashMap<String,UniverseServer>();
        
        // load the universe list
        try {
            String listProperty = config.getProperty(Universe.CONFIG_LIST);
            if (listProperty == null)
                throw new UniverseException("Configuration for UniverseRegistry_Simple requires the '" + Universe.CONFIG_LIST + "' property.",
                        UniverseException.UNIVERSE_ERROR_CONFIG_MISSING_REQUIRED);
            
            ListIterator<String> listOfUniverses = ThingsPropertyReaderToolkit.propertyList(listProperty);
            if (!listOfUniverses.hasNext()) 
                throw new UniverseException("Configuration for UniverseRegistry_Simple '" + Universe.CONFIG_LIST + "' property empty.  There are no universe specified.",
                        UniverseException.UNIVERSE_ERROR_CONFIG_BAD_REQUIRED);
            
            // process each list item
            String current;
            String name;
            String type;
            String path;
            String clearanceString;
            Clearance clearanceValue;
            while(listOfUniverses.hasNext()) {
            	current = listOfUniverses.next().trim();
            	name =  getRequiredProp(current,Universe.CONFIG_NAME,config);
                type = getRequiredProp(current,Universe.CONFIG_TYPE,config);
                path = getRequiredProp(current,Universe.CONFIG_PATH,config);
                
                // handle clearance
                clearanceString = getRequiredProp(current,Universe.CONFIG_CLEARANCE,config);
                try {
                	clearanceValue = Clearance.valueOf(clearanceString.toUpperCase());
                } catch (Throwable t) {
                    throw new UniverseException("Configuration for UniverseRegistry_Simple has a bad Clearance value.",
                            UniverseException.UNIVERSE_ERROR_CONFIG_BAD_VALUE, ThingsNamespace.ATTR_PROPERTY_NAME, current, ThingsNamespace.ATTR_PROPERTY_VALUE, clearanceString);           	
                }
                
                // Check for duplicate.
                if (universeMapByName.containsKey(name)) throw new UniverseException("Configuration for UniverseRegistry_Simple has a duplicate universe name", UniverseException.UNIVERSE_ERROR_REGISTRATION_DUPLICATE, 
                		ThingsNamespace.ATTR_PROPERTY_NAME, current, ThingsNamespace.ATTR_PROPERTY_PATH, path);

                // Only do local
                if (type.toLowerCase().contentEquals(Universe.CONFIG_TYPE_LOCAL)) {          
                    // A LOCAL

                    File directoryToVerify = new File(path);
                    if (!directoryToVerify.isDirectory()) {
                        throw new UniverseException("Configuration for UniverseRegistry_Simple has a bad path value", UniverseException.UNIVERSE_ERROR_CONFIG_BAD_VALUE, ThingsNamespace.ATTR_PROPERTY_NAME, current, ThingsNamespace.ATTR_PROPERTY_PATH, path);                       
                    }
                    UniverseLocalServer uls = new UniverseLocalServer();
                    uls.root = path;
                    uls.id = new UniverseID(name);
                    uls.requiredClearance = clearanceValue;
                    universeMapByName.put(name,uls);
                    universeMapById.put(uls.id.toString(),uls);
                    if (verbose) vp.postit("UniverseRegistry_Simple loaded " + name + " successfully." );
                    
                } else {
                    // NOT DEFINED
                    throw new UniverseException("Configuration for UniverseRegistry_Simple has a bad type value.",
                            UniverseException.UNIVERSE_ERROR_CONFIG_BAD_VALUE, ThingsNamespace.ATTR_PROPERTY_NAME, current, ThingsNamespace.ATTR_PROPERTY_VALUE, type);
                }
                if (verbose) vp.postit("UniverseRegistry_Simple done loading registry." );
            }
            
        } catch (UniverseException uue) {
            throw uue;

        } catch (Throwable ee) {
            throw new UniverseException("Configuration for UniverseRegistry_Simple general failure.", UniverseException.UNIVERSE_ERROR_CONFIG_BAD_VALUE, ee);           	
        }
    }

    // HELPER
    private static String getRequiredProp(String base, String specific, ThingsPropertyView config) throws UniverseException {
        String result = null;
        try {
            result = config.getProperty(base + "." + specific);
        } catch (Throwable ee) {
            throw new UniverseException("Serious properties problem.  message=" + ee.getMessage(), UniverseException.SYSTEM_FAULT_WITH_PROPERTIES,ee);
        }
        if (result==null)
            throw new UniverseException("Configuration for UniverseRegistry_Simple universe named '" + base + " ' missing '" + specific + "' property (" + base + "." + specific + ").",
                 UniverseException.UNIVERSE_ERROR_CONFIG_MISSING_REQUIRED);      
        return result;
    }

    
    /**
     * Safety the Registry. If the confiration node supports it, the registry
     * will be checkpointed and/or saved. This effectively sets the last known
     * good configuration.
     * 
     * @throws UniverseException
     */
    public void safetyTheRegistry() throws UniverseException {
        // Don't. We'll never save it.
    }
    
    // VERBOSE
    
	/**
	 * Turn on.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
        try {
            poster.post("UniverseRegistry_Simple turning on VERBOSE.");
        } catch (Exception e) {
            throw new UniverseException("Turning on VERBOSE failed.",UniverseException.VERBOSE_FAILED_TO_START, e );
        }
        verbose = true;
        vp = poster;
    }
    
	/**
	 * Turn it off
	 */
	public void verboseOff() {
	    verbose = false;
	}
	
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation may find a 
	 * way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message){
		if (vp != null) vp.postit(message);
	}
	
	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose() {
		 if (vp==null) return false;
		 return true;
	}
}