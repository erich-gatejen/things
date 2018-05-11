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
package things.common.tools;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.configuration.ConfigureConstants;
import things.data.ThingsPropertyViewReader;

/**
 * Tools for stream manipulation.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 14 MAR 05
 * </pre> 
 */
public class StreamTools {
	
	// ENGINE VALUES
	private enum Operation { START, OPEN, READ, CLOSE }
	private final static int MAX_16_VALUE = 65534;

	/**
	 *  Merge a reader with properties to writer.  It will not buffer either, so pass buffered
	 *  implementations, if that matters.  It will only work properly with 16-bit UTF-16 (which should cover typical Unicode 4.0).<br>
	 *  You can escape property names with double characters.<br>
	 *  "Test $$ text" = "Test $ text"<br>
	 *  "Test $aa$$a$ is a test" = "Test {value of 'aa$a') is a test" without the paren.<br>
	 *  <p>
	 *  Exceptions are logged, not thrown.
	 *  <p>
	 * @param input A Reader source.
	 * @param output A Writer destination.
	 * @param props Properties that are candidates for substitution.
	 * @param log A StringPoster for posting error or information messages.
	 * @return true if it completed without error, otherwise false
	 * @see things.common.StringPoster
	 */
	public static boolean merge(
		Reader input, Writer output,
		ThingsPropertyViewReader props,
		StringPoster log ) {

		Operation state = Operation.START;
		int workingCharacter;
		StringBuffer substitutionBuffer = null;
		String propertyName = null;
		String propertyValue = null;
		boolean result = false;
		
		try {
			
			// Start the engine
			workingCharacter = input.read();
			while ((workingCharacter >= 0)&& (workingCharacter < MAX_16_VALUE)) {

				switch (state) {

					case START :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER)
							state = Operation.OPEN;
						else
							output.write(workingCharacter);
						break;

					case OPEN :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							// Escaped property character
							state = Operation.START;
							output.write(ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
						} else {
							state = Operation.READ;
							substitutionBuffer = new StringBuffer();
							substitutionBuffer.append((char) workingCharacter);
						}
						break;

					case READ :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							state = Operation.CLOSE;
						} else {
							substitutionBuffer.append((char) workingCharacter);
						}
						break;

					case CLOSE :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							// Escaped property character in the property name
							state = Operation.READ;
							substitutionBuffer.append((char) ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
							substitutionBuffer.append((char) workingCharacter);
							
						} else {
							// An actual substitution is named
							propertyName = substitutionBuffer.toString();
							propertyValue = props.getProperty(propertyName);
							if (propertyValue!=null) {
								// The substitution exists in the property set
								output.write(propertyValue);
								substitutionBuffer = null;
							} else {
								output.write(ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER + propertyName + ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
							}
							// Dont forget this character
							output.write(workingCharacter);
							
							// Do this last so we can detect any property bufoonery that causes a bug.
							state = Operation.START;
						}
						break;
				} // end case
				workingCharacter = input.read();
			}
		} catch (ThingsException te) {
			log.postit("Property system error while merging.  message=" + te.getMessage());
		} catch (EOFException eofe) {
			// Ignore 
		} catch (Exception ee) {		
			log.postit("Catastrophic merging error.  message=" + ee.getMessage());
		} finally {
			try {
				if (state != Operation.START) {
					log.postit("Incomplete substitution during merge.  Destination file might be corrupt.");
				} else {
					log.postit("Merge complete.");
					result = true;
				}
				
			} catch (Exception eee) {
				log.postit("Catastrophic error message=" + eee.getMessage());
			}
			
		}
		return result;
	}
	
	/**
	 *  Merge a reader with properties to writer.  It will not buffer either, so pass buffered
	 *  implementations, if that matters.  It will only work properly with 16-bit UTF-16 (which should cover typical Unicode 4.0).<br>
	 *  You can escape property names with double characters.<br>
	 *  "Test $$ text" = "Test $ text"<br>
	 *  "Test $aa$$a$ is a test" = "Test {value of 'aa$a') is a test" without the paren.<br>
	 *  <p>
	 *  Any error will throw an exception.
	 * @param input A Reader source.
	 * @param output A Writer destination.
	 * @param props Properties that are candidates for substitution.
	 * @return true if it completed without error, otherwise false
	 * @throws Throwable
	 * @see things.common.StringPoster
	 */
	public static boolean merge(
		Reader input, Writer output,
		ThingsPropertyViewReader props) throws Throwable {
		Operation state = Operation.START;
		int workingCharacter;
		StringBuffer substitutionBuffer = null;
		String propertyName = null;
		String propertyValue = null;
		boolean result = false;
		
		try {
			
			// Start the engine
			workingCharacter = input.read();
			while ((workingCharacter >= 0)&& (workingCharacter < MAX_16_VALUE)) {

				switch (state) {

					case START :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER)
							state = Operation.OPEN;
						else
							output.write(workingCharacter);
						break;

					case OPEN :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							// Escaped property character
							state = Operation.START;
							output.write(ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
						} else {
							state = Operation.READ;
							substitutionBuffer = new StringBuffer();
							substitutionBuffer.append((char) workingCharacter);
						}
						break;

					case READ :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							state = Operation.CLOSE;
						} else {
							substitutionBuffer.append((char) workingCharacter);
						}
						break;

					case CLOSE :
						if (workingCharacter == ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER) {
							// Escaped property character in the property name
							state = Operation.READ;
							substitutionBuffer.append((char) ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
							substitutionBuffer.append((char) workingCharacter);
							
						} else {
							// An actual substitution is named
							propertyName = substitutionBuffer.toString();
							propertyValue = props.getProperty(propertyName);
							if (propertyValue!=null) {
								// The substitution exists in the property set
								output.write(propertyValue);
								substitutionBuffer = null;
							} else {
								output.write(ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER + propertyName + ConfigureConstants.CONFIGURE_PROPERTY_CHARACTER);
							}
							// Dont forget this character
							output.write(workingCharacter);
							
							// Do this last so we can detect any property bufoonery that causes a bug.
							state = Operation.START;
						}
						break;
				} // end case
				workingCharacter = input.read();
			}
		} catch (ThingsException te) {
			throw new ThingsException("Property system error while merging.", ThingsException.PROCESSING_ERROR_MERGE, te);
		} catch (EOFException eofe) {
			// Ignore 
		} catch (Exception ee) {		
			throw new ThingsException("Merging error.", ThingsException.PROCESSING_ERROR_MERGE, ee);
		} finally {
			try {
				if (state != Operation.START) {
					throw new ThingsException("Incomplete substitution during merge.  Destination file might be corrupt.", ThingsException.PROCESSING_ERROR_MERGE_INCOMPLETE);
				} 
				
			} catch (Exception eee) {
				throw new ThingsException("Catastrophic error", ThingsException.PROCESSING_ERROR_MERGE, eee);
			}
			
		}
		return result;
	}
	
	/**
	 * Read a stream into a String.  This should be used only is absolutely needed.
	 * @param is The stream to load.  It should be buffered before calling this,
	 * @return String if successful or otherwise null
	 * @throws Certain IOException, like if the file cannot be found or opened.
	 */
	public static String loadStream2String(InputStream   is) throws IOException {
		String result = null;
		
		try {
			InputStreamReader isr = new InputStreamReader(is);
			StringWriter sw = new StringWriter();
			char[] cbuffer = new char[4096];
			
			int size = isr.read(cbuffer);
			while (size >= 0) {
				if (size > 0) sw.write(cbuffer, 0 , size);
				size = isr.read(cbuffer);
			}
			
			result =  sw.toString();
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			//throw new IOException("Failed to loadFile2String.", t);
			throw new IOException("Failed to loadStream2String.");
		}
		return result;
	}
	
	/**
	 * Write a string to file using default encoding.
	 * @param theString The string
	 * @param destination The file destination
	 * @throws Certain IOException, like if the file cannot be found or opened.
	 */
	public static void string2File(String theString, File   destination) throws IOException {
		BufferedOutputStream bos = null;
		
		try {
			bos = new BufferedOutputStream(new FileOutputStream(destination));
			byte[] data = theString.getBytes();
			bos.write(data);
			bos.flush();
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			throw new IOException("Failed to string2File.");
		} finally {
			try {
				bos.close();
			} catch (Throwable t) {
				// Ot well.
			}
		}
	}
	
	
}