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
package things.common.impl;

import java.io.EOFException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsMarkup;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeRAM;

/**
 * This is a simple processor for markup'd strings.  It assumes all data is Java String worthy,
 * and as such is fully UTF-16 compliant.  
 * <p>
 * Do note that these methods do full copy of data from source to destination.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 MAR 05
 * </pre> 
 */
public class ThingsMarkup2Printable {

	/**
	 * This will make a printable string from a string.  Any variables will be ignored.
	 * @param text The text of the string to be processed.
	 * @return The processed string.
	 * @throws things.common.ThingsException
	 */
	public static String makePrintable(String text) throws ThingsException {
		return makePrintable(text,new ThingsPropertyTreeRAM());
	}
	
	/**
	 * This will make a printable string from a string.  Variables will be matched to the passed ThingsPropertyView.
	 * @param text The text of the string to be processed.
	 * @param props Property view for variables.
	 * @return The processed string.
	 * @throws things.common.ThingsException
	 * @see things.data.ThingsPropertyView
	 */
	public static String makePrintable(String text, ThingsPropertyView props) throws ThingsException {
		String	result = null;
		try {
			StringWriter wout = new StringWriter(text.length()+16);
			ThingsMarkup2Printable.engine(new StringReader(text),wout,props);
			result = wout.toString();
			
		} catch (ThingsException te) {
			throw te;
		} catch (Throwable tw) {
			throw new ThingsException("General error while makePrintable().  message=" + tw.getMessage(),ThingsException.PROCESSING_ERROR_TEXT_GENERAL,tw);
		}
		return result;
	}
	
	// States for the engine.
	private enum EngineState { START, OPEN, ESCAPE }

	/**
	 * This is the processing engine.  In general, you shouldn't call this directly.
	 * @param in An input reader that is the source of data.  If you need it buffered, do it yourself.
	 * @param out An output writer that is the destination for data.  If you need it buffered, do it yourself.
	 * @param props Property view for variables.
	 * @throws things.common.ThingsException
	 * @see things.data.ThingsPropertyView
	 */
	public static void engine(Reader  in, Writer  out, ThingsPropertyView props) throws ThingsException  {
	
		try {
			EngineState state = EngineState.START;
			StringBuffer accumulator = null;
			String working = null;
			int tabSpacing = 0;
			int tabSetting = ThingsMarkup.DEFAULT_TAB_LENGTH;
			
			int current =  in.read();
			while (current >= 0) {
				
				switch (state) {
				
				// START THE RUN
				case START:
					switch (current) {
					case ThingsMarkup.OPEN_TAG:	
						accumulator = new StringBuffer();
						break;
						
					case ThingsMarkup.ESCAPE:
						accumulator = new StringBuffer();
						break;
						
					case ThingsConstants.CR:
						out.write(current);
						tabSpacing = 0;
						break;
						
					case ThingsConstants.LF:
						out.write(current);
						tabSpacing = 0;
						break;
						
					default:
						out.write(current);
						tabSpacing++;
						break;
					
					} // end switch character
					break;
					
				// OPENED BRACKET
				case OPEN:		
					switch (current) {
					case ThingsMarkup.OPEN_TAG:	
						// break the bracket.  emit as a literal
						out.write(ThingsMarkup.OPEN_TAG);
						out.write(ThingsMarkup.OPEN_TAG);
						tabSpacing+=2;
						break;
							
					case ThingsMarkup.CLOSE_TAG:
						// well formed tag.  see if it is supported.
						working = accumulator.toString().toLowerCase();
						try {
							// Do this ugly style
							if (working.charAt(0) == ThingsMarkup.VARIABLE) {
								// If the variable is present, emit it.  Otherwise, don't write anything.
								working = working.substring(1);
								working = props.getProperty(working);
								if (working !=null) {
									out.write(working);
									tabSpacing = tabSpacing + working.length();
								} 
								
							} else if (working.equals(ThingsMarkup.NEW_LINE)) {
								out.write(ThingsMarkup.EMIT_NEW_LINE);
								tabSpacing = 0;
								
							} else if (working.equals(ThingsMarkup.NEW_PARAGRAPH)) {
								out.write(ThingsMarkup.EMIT_NEW_PARAGRAPH);
								tabSpacing = 0;
								
							} else if (working.equals(ThingsMarkup.META)) {
								// Manage the META.  Ignore if not known.
								String metaName = working.substring(working.lastIndexOf(ThingsMarkup.META_NAME_SPLIT)+1, working.lastIndexOf(ThingsMarkup.META_VALUE_SPLIT)).toLowerCase();
								if (metaName.equals(ThingsMarkup.META_TAB)) {
									tabSetting = Integer.parseInt(working.substring(working.lastIndexOf(ThingsMarkup.META_VALUE_SPLIT)+1));
								}
								
							} else if (working.equals(ThingsMarkup.TAB)) {
								int tabbing = tabSpacing - (tabSpacing % tabSetting);
								while (tabbing > 0) {
									out.write(ThingsMarkup.EMIT_SPACE);
									tabSpacing++;
									tabbing--;
								}
							}
							
						} catch (Throwable tee) {
							// abort and emit it
							out.write(ThingsMarkup.OPEN_TAG);
							out.write(accumulator.toString());
							out.write(ThingsMarkup.CLOSE_TAG);
							tabSpacing++;
						}	
						break;
						
					case ThingsConstants.CR:
						accumulator.append((char)current);
						tabSpacing = 0;
						break;
						
					case ThingsConstants.LF:
						accumulator.append((char)current);						
						tabSpacing = 0;
						break;
						
					default:
						accumulator.append((char)current);
						tabSpacing++;
						break;
					
					} // end switch character
					break;					
					
				// ESCAPING
				case ESCAPE:		
					switch (current) {
						
					case ThingsMarkup.ESCAPE:
						// break it.  this is poorly formed.
						out.write(ThingsMarkup.ESCAPE);
						if (accumulator.length()>0) out.write(accumulator.toString());
						out.write(ThingsMarkup.ESCAPE);
						tabSpacing = tabSpacing + accumulator.length() + 2;
						break;

					case ThingsMarkup.COMPELTE_ESCAPE:
						working = accumulator.toString().toLowerCase();
						try {						
							if (working.equals(ThingsMarkup.ESCAPE_LT)) {
								out.write(ThingsMarkup.EMIT_ESCAPE_LT);
								tabSpacing++;	
							} else if (working.equals(ThingsMarkup.ESCAPE_GT)) {
								out.write(ThingsMarkup.EMIT_ESCAPE_GT);
								tabSpacing++;									
							} else if (working.equals(ThingsMarkup.ESCAPE_AMP)) {
								out.write(ThingsMarkup.EMIT_ESCAPE_AMP);
								tabSpacing++;								
							} else if (working.equals(ThingsMarkup.ESCAPE_APOS)) {
								out.write(ThingsMarkup.EMIT_ESCAPE_APOS);
								tabSpacing++;		
							} else if (working.equals(ThingsMarkup.ESCAPE_QUOT)) {
								out.write(ThingsMarkup.EMIT_ESCAPE_QUOT);
								tabSpacing++;								
							} 
										
						} catch (Throwable tee) {
							// abort and emit it
							out.write(ThingsMarkup.ESCAPE);
							out.write(accumulator.toString());
							out.write(ThingsMarkup.COMPELTE_ESCAPE);
							tabSpacing++;
						}	
						break;			

					case ThingsConstants.CR:
						accumulator.append((char)current);
						tabSpacing = 0;
						break;
						
					case ThingsConstants.LF:
						accumulator.append((char)current);						
						tabSpacing = 0;
						break;
						
					default:
						accumulator.append((char)current);
						tabSpacing++;
						break;
					
					} // end switch character
					break;		
					
				// VERY BAD THING!
				default:
					throw new ThingsException("Undefined state in ThingsMarkup2Printable.engine().  state=" + state,ThingsException.PANIC_THINGS_COMMON_BUG);
					
				} // end switch state
				
				// DO NOT EDIT BELOW THIS
				current =  in.read();
			}			

		//} catch (ThingsException te) {
		//	throw te;
		} catch (EOFException eof) {	
			// ignore it
		} catch (Throwable tw) {
			throw new ThingsException("General error in Markup2Printable engine.  message=" + tw.getMessage(),ThingsException.PROCESSING_ERROR_TEXT_GENERAL,tw);
		}
	}
	
}