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
package things.common.help;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import things.data.NVImmutable;

/**
 * A rich helper.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 SEP 08
 * </pre> 
 */
public class Helper {

	
	// ====================================================================================================================================
	// == DATA
	private List<HelpItem> help = new LinkedList<HelpItem>();
	private String information;
	private String user;
	private List<Helper> references = new LinkedList<Helper>();

	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Default constructor.
	 */
	public Helper() {
	}
	
	/**
	 * Constructor.
	 * @param user the user of this help.  All added items will be tagged with this user.
	 * @param title the title for this area.
	 * @param information helpful text about the user.
	 */
	public Helper(String user, String title, String information) {
		this.user = user;
		this.information = information;
		help.add(new HelpUser(user, title, information));
	}
	
	/**
	 * Get the help list.
	 * @see things.common.ThingsMarkup
	 * @return the properties
	 */  
    public List<HelpItem> getHelp() {
    	return help;
    }
    
	/**
	 * Get the reference list.  Teh referenced help should be treated as a second level during rendering.
	 * @see things.common.ThingsMarkup
	 * @return the properties
	 */  
    public List<Helper> getReferences() {
    	return references;
    }
    
    
	/**
	 * Provide detailed information.  It should use ThingsMarkup for gimmicks.
	 * @see things.common.ThingsMarkup
	 * @return The text of the information.
	 */  
    public String information () {
    	return information;
    }
    
	/**
	 * Get the user.
	 * @return The user or null if one is not set.
	 */  
    public String getUser () {
    	return user;
    }
    
   /**
    * Add a property.
    * @param required
    * @param name
    * @param helpText
    */
    public void add(boolean required, String name, String helpText) {
    	if (user==null) help.add(new HelpProperty(required,name,helpText));
    	else help.add(new HelpProperty(required,name,user,helpText));
    }
    
    /**
     * Add a property with value suggestions.
     * @param required
     * @param name
     * @param values values as name/description pairs.  This really should be an even number.  An odd number leave drop the final name string.
     * @param helpText
     */
     public void add(boolean required, String name, String helpText, String... values) {
    	 HelpProperty desc;
    	if (user==null) desc = new HelpProperty(required,name,helpText);
    	else desc = new HelpProperty(required,name,user,helpText);
     	help.add(desc);
     	try {
     		desc.values = new LinkedList<NVImmutable>();
     		for (int index = 0 ; index < values.length; index=index+2) {
     			desc.values.add(new NVImmutable(values[index], values[index+1]));
     		}	
     	} catch (Throwable t) {
     		// This will punt on odd name string.
     	}
     }
     
     /**
      * Inherit help from another class' helper, including references.  Call this and add in order, since they will be presented as an ordered list.
      * @param helper the helper.
      */
      public void inherit(Helper helper) {
     	 for (HelpItem desc : helper.help) {
     		 help.add(desc);
     	 }
     	 for (Helper ihelpers : helper.references) {
     		references.add(ihelpers);
     	 }
      }
      
      /**
       * Inherit help from another class' helper.  Call this and add in order, since they will be presented as an ordered list.
       * @param helpful the helpful class from which we can get a helper.  Sucks I have to instantiate one of these.
       */
       public void inherit(Class<?> helpful) {
    	   try {
    		   Object helpfulObject = helpful.newInstance();
    		   if (helpfulObject instanceof Helpful) {
	    		   inherit(((Helpful)helpfulObject).helper());
    		   }
    	   } catch (Throwable ie) {
    		   try {
	    			Method m = helpful.getMethod("values", new Class[0]);
	    			Object[] o = (Object[]) m.invoke(null, new Object[0]);
	     		   if (o[0] instanceof Helpful) {
		    		   inherit(((Helpful)o[0]).helper());
	    		   }
    		   } catch (Throwable tt) {
    			   // Last change.  See if we can get the static reference
    			   // 	private static Helper helper;
    			   try {
    				   Field field = helpful.getField("helper");
    				   Object actual = field.get(null);
    	     		   if (actual instanceof Helper) {
    	     			   inherit(((Helper)actual));
    	     		   }
    				   
    			   } catch (Throwable ttt) {
    				   throw new Error("Failed to reference a helpful.", ttt);
    			   }
    		   }
    	   } 
       }
       
   /**
    * Reference help from another class' helper.  Call this and add in order, since they will be presented as an ordered list.  
    * @param helper the helper.
    */
    public void reference(Helper helper) {
    	references.add(helper);
    }
        
    /**
     * Reference help from another class' helper.  Call this and add in order, since they will be presented as an ordered list.
     * @param helpful the helpful class from which we can get a helper.  Sucks I have to instantiate one of these.
     */
     public void reference(Class<?> helpful) {
  	   try {
  		   Object helpfulObject = helpful.newInstance();
  		   if (helpfulObject instanceof Helpful) {
    		   reference(((Helpful)helpfulObject).helper());
  		   }
	   } catch (Throwable ie) {
		   // Maybe an enum.
		   try {
    			Method m = helpful.getMethod("values", new Class[0]);
    			Object[] o = (Object[]) m.invoke(null, new Object[0]);
     		   if (o[0] instanceof Helpful) {
     			  reference(((Helpful)o[0]).helper());
    		   }
		   } catch (Throwable tt) {
			   // Last change.  See if we can get the static reference
			   // 	private static Helper helper;
			   try {
				   Field field = helpful.getField("helper");
				   Object actual = field.get(null);
	     		   if (actual instanceof Helper) {
	     			   reference(((Helper)actual));
	     		   }
				   
			   } catch (Throwable ttt) {
				   throw new Error("Failed to reference a helpful.", ttt);
			   }
		   }
  	    } 
     }
    
 	// ====================================================================================================================================
 	// == TOOLS
    
    /**
     * Get a helper from a class name.  It'll return null if it isn't found.
     * @return the helper.
     */
    public static Helper getHelper(String className) {
    	Helper result = null;
    	Class<?> theClass = null;
   	   try {
   		   theClass = Class.forName(className);
  		   Object helpfulObject = theClass.newInstance();
  		   if (helpfulObject instanceof Helpful) {
    		   result = ((Helpful)helpfulObject).helper();
  		   }
	   } catch (Throwable ie) {
		   // Maybe an enum.
		   try {
    			Method m = theClass.getMethod("values", new Class[0]);
    			Object[] o = (Object[]) m.invoke(null, new Object[0]);
     		   if (o[0] instanceof Helpful) {
        		   result = ((Helpful)o[0]).helper();
    		   }
		   } catch (Throwable tt) {
			   // Last change.  See if we can get the static reference
			   // 	private static Helper helper;
			   try {
				   Field field = theClass.getField("helper");
				   Object actual = field.get(null);
	     		   if (actual instanceof Helper) {
	     			  result = (Helper)actual;
	     		   }
				   
			   } catch (Throwable ttt) {
				   // oh well, let it stay null.
			   }
		   }
  	    } 
	   return result;
    }
         
}
