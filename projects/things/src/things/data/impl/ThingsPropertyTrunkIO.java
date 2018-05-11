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
package things.data.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.Accessor;
import things.data.NV;
import things.data.NVImmutable;
import things.data.ThingsProperty;
import things.data.ThingsPropertyReaderToolkit;
import things.data.ThingsPropertyTrunk;

/**
 * An IO implementation for a property trunk.<p>
 * The id is the file path, which is the usual contract for a ThingsPropertyTrunk.  However, you can also 
 * init() it yourself and use your own id.  It uses readers through the Accessor, so it can come
 * from any source and it should handle any character encoding.  (NOTE: When using UTF-8 from a file,
 * sometimes you have to deal with the marker yourself.  Just try it and see what happens.)<p>
 * <p>
 * <H2>THE RULES</H2>
 * Each property is a name/value pair, separated by a equal sign.<br>
 * Comment lines start with a # and are ignored.<br>
 * Lines can be continued with a backslash character '&'<br>
 * Every line will be trimmed of whitespace, but intra-token whitespace will be preserved.<br>
 * ThingsPropertyReaderToolkit determines any other encoding.  It is assumed to be the default character set.<br>
 * <br>
 * <b>For values (right fo the first = sign) there are additional rules:<b><br>
 * Escape with ?.<br>
 * Separate multivalues with ,.<br>
 * The , may be escaped with ?.<br>
 * The = indicates equality.  It may be escaped with the ?.<br>
 * <p>
 * The value rules are inherited by ThingsPropertyReaderToolkit.decodeString.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 NOV 04
 * </pre> 
 */
public class ThingsPropertyTrunkIO implements ThingsPropertyTrunk {
	
    private BufferedReader bir;
    private InputStream readerStream;
    private BufferedWriter bor;
    private OutputStream writerStream;
    //private String	myId;
    private Mode myMode;
    private Accessor myAccessor;
    
    /**
     * Constructor.
     */
    public ThingsPropertyTrunkIO() {
    	// Set the mode.
    	myMode = Mode.IDLE;
    }
    
	/**
	 * Sets the  ID.  It's really up to the implementation as to what the ID means.  It may be ignored and all will be transfered.
	 * @param id An id
	 * @param accessItem A way to read and write the data.
	 * @throws things.common.ThingsException 
	 */ 	
	public void init(String  id, Accessor  accessItem) throws ThingsException {
		synchronized(myMode) {
			if (myMode != Mode.IDLE) throw new ThingsException("Cannot init() if Mode isn't IDLE.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
					ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());
			if (accessItem==null) throw new ThingsException("Cannot use a null accessItem.", ThingsException.SYSTEM_FAULT_PROPERTIES_BAD_ACCESS);
	//		myId = id;
			myAccessor = accessItem;
		} 
	}
    
	/**
	 * Get the current Mode.
	 * @return the Mode.
	 */
	public Mode getMode() {
		return myMode;
	}
	
	/**
	 * Start a read.
	 * @throws things.common.ThingsException 
	 */ 
	public void startRead() throws ThingsException {
	    try {
			synchronized(myMode) {
				if (myMode != Mode.IDLE) throw new ThingsException("Cannot start a read if Mode isn't IDLE.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());		
				readerStream = myAccessor.openForRead();
				bir = new BufferedReader(new InputStreamReader(readerStream));
				myMode = Mode.LOADING;
			} 		
	    } catch (Exception e) {
	        throw new ThingsException("Could not open property file.  General exception.", ThingsException. SYSTEM_FAULT_PROPERTIES_SOURCE_NOT_FOUND);    
	    }
	}
	
	/**
	 * End a read transfer.  If a read hasn't started, nothing bad will happen.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endRead() throws ThingsException {
	    try {
			synchronized(myMode) {
				if (myMode != Mode.LOADING) throw new ThingsException("Cannot end a read if Mode isn't LOADING.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());	
				myAccessor.doneWithRead(readerStream);
				bir.close();
			}
	    } catch (ThingsException te) {
			throw te;
	    } catch (Exception e) {
	        // don't care
	    } finally {
			myMode = Mode.IDLE;	    	
	    }
	}
	
	/**
	 * Start a write transfer.
	 * @throws things.common.ThingsException 
	 */ 	
	public void startWrite() throws ThingsException {
	    try {
			synchronized(myMode) {
				if (myMode != Mode.IDLE) throw new ThingsException("Cannot start a write (load) if Mode isn't IDLE.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());			
				writerStream = myAccessor.openForWrite();
				bor = new BufferedWriter(new OutputStreamWriter(writerStream));
				
				myMode = Mode.SAVING;
			} 		
	    } catch (Exception e) {
	        throw new ThingsException("Could not open property file.  General exception.", ThingsException. SYSTEM_FAULT_PROPERTIES_SOURCE_NOT_FOUND);    
	    }
	}
	
	/**
	 * End a wrote transfer.  If a write hasn't started, nothing bad will happen.
	 * It should flush and close the destination.
	 * @throws things.common.ThingsException 
	 */ 	
	public void endWrite() throws ThingsException {
	    try {
			synchronized(myMode) {
				if (myMode != Mode.SAVING) throw new ThingsException("Cannot end a write (save) if Mode isn't SAVING.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());					
				bor.close();
				myAccessor.doneWithWrite(writerStream);
			}
	    } catch (ThingsException te) {
			throw te;
	    } catch (Exception e) {
	        // don't care
	    } finally {
			myMode = Mode.IDLE;	    	
	    }
	}
	
	/**
	 * Write the next property
	 * @param name The property name as a string
	 * @param value The property value as a string
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNext(String name, String value) throws ThingsException {
	    try {
	        
			synchronized(myMode) {
				
				// QUALIFY
				if (myMode != Mode.SAVING) throw new ThingsException("Cannot write (save) if Mode isn't SAVING.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());
				
				// Write it
				bor.write(name + ThingsProperty.PROPERTY_LINE_EQUALITY + ThingsPropertyReaderToolkit.encodeString(value));
				bor.write(ThingsProperty.PROPERTY_LINE_TERMINATION);
			}
	        
	    } catch (ThingsException te) {
	        throw te;   
	    } catch (Exception ee) {
            throw new ThingsException("Bad property write.", ThingsException.SYSTEM_ERROR_BAD_PROPERTY_WRITE, ee, ThingsNamespace.ATTR_PROPERTY_NAME, name,
            		ThingsNamespace.ATTR_PROPERTY_VALUE, value);
	    }
	}
	
	/**
	 * Write the next property
	 * @param item the next property as an NVImmutable.
	 * @throws things.common.ThingsException 
	 * @see things.data.NVImmutable
	 */ 	
	public void writeNext(NVImmutable item) throws ThingsException {
		writeNextMultivalue(item.getName(), item.getValues());
	}
	
	/**
	 * Write the next property that is a multivalue.
	 * @param name The property name as a string
	 * @param values The property values
	 * @throws things.common.ThingsException 
	 */ 	
	public void writeNextMultivalue(String name, String... values) throws ThingsException {
    	String decodeValues = "Broken encoding.";
		
	    try {
	        
			synchronized(myMode) {
				
				// Values?
				 decodeValues = ThingsPropertyReaderToolkit.encodeString(values);
				
				// QUALIFY
				if (myMode != Mode.SAVING) throw new ThingsException("Cannot write (save) if Mode isn't SAVING.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());
				
				// Write it
				bor.write(name + ThingsProperty.PROPERTY_LINE_EQUALITY + decodeValues);
				bor.write(ThingsProperty.PROPERTY_LINE_TERMINATION);
			}
	        
	    } catch (ThingsException te) {
	        throw te;   
	    } catch (Exception ee) {
            throw new ThingsException("Bad property write.", ThingsException.SYSTEM_ERROR_BAD_PROPERTY_WRITE, ee, ThingsNamespace.ATTR_PROPERTY_NAME, name,
            		ThingsNamespace.ATTR_PROPERTY_VALUE, decodeValues);
	    }
	}
	
	/**
	 * Read the next property.  It will return null if there are none left.
	 * @return NV
	 * @throws things.common.ThingsException 
	 */ 	
	public NV readNext() throws ThingsException {

	    NV result = null;
	    String entry = null;
	    
	    try {
	        
			synchronized(myMode) {
				
				// QUALIFY
				if (myMode != Mode.LOADING) throw new ThingsException("Cannot read if Mode isn't LOADING.", ThingsException.SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION,
						ThingsNamespace.ATTR_PROPERTY_TRUNK_MODE, myMode.toString());
				
		        // try to get the next valid property line
		        StringBuffer buffer = new StringBuffer();
		        String currentLine = bir.readLine();		// Assumes PROPERTY_LINE_TERMINATION = \r\n
		        while(currentLine != null) {
		        	currentLine = currentLine.trim();
		            if ( (currentLine.length()>0) && (currentLine.charAt(0) != ThingsProperty.PROPERTY_COMMENT_CHARACTER)) {
		                if (currentLine.endsWith(ThingsProperty.PROPERTY_LINE_CONTINUATION_STRING)) {
	                        buffer.append(currentLine.substring(0, currentLine.length() - 1));	                    
		                } else {
	                        buffer.append(currentLine);
	                        break;                    
		                }
		            }
		            currentLine = bir.readLine();
		        }
		        
		        // Empty check.  The result will be left as null if the size is 0.
		        if (buffer.length() > 0) {
		        	
			        entry = buffer.toString();
			        
			        // split the name from the value       
			        int spot = entry.indexOf(ThingsProperty.PROPERTY_LINE_EQUALITY);
			        if (spot < 1) {
			            throw new ThingsException("Bad property.  No equals (=) found.", ThingsException.SYSTEM_ERROR_BAD_PROPERTY_TEXT, ThingsNamespace.ATTR_PROPERTY_ENTRY, entry);
			        }
			        String name = entry.substring(0,spot).trim();
			        try {
			        	result = new NV(name, ThingsPropertyReaderToolkit.decodeString(entry.substring(spot+1).trim()));
			        } catch (Exception iae) {
			        	// We blew bounds, so it is empty.
			        	result = new NV(name, ThingsProperty.PROPERTY_EMPTY);
			        }
		        	
		        }	// end if greater than 0
	        
			} // end synchronize
	        
	    } catch (ThingsException te) {
	        throw te;   
	    } catch (Exception ee) {
            throw new ThingsException("Bad property.", ThingsException.SYSTEM_ERROR_BAD_PROPERTY_TEXT, ee, ThingsNamespace.ATTR_PROPERTY_ENTRY, entry);
	    }
        return result;
	}	
	
}
