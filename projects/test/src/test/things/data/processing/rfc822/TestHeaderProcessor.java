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
package test.things.data.processing.rfc822;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import things.common.ThingsException;
import things.data.ThingsPropertyView;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.processing.rfc822.HeaderProcessor;

/**
 * Test subclass for the HeaderProcessor tool.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 JAN 07
 * </pre> 
 */
public class TestHeaderProcessor extends HeaderProcessor {

	// ================================================================================================================
	// PUBLIC DATA 
	
	public ThingsPropertyView 	matchedValues;		// Things we find.  It is reset with every process.
	public ThingsPropertyView 	unmatchedValues;	// Unmatched headers.  It is reset with every process.
	
	// ================================================================================================================
	// INTERNAL DATA 

	// HEADERS THAT WE CARE ABOUT
	public final static int HEADER_DA	=	1;
	public final static String HEADER_DA_STRING	= "Da";
	public final static int HEADER_FA	=	2;
	public final static String HEADER_FA_STRING	= "Fa";
	public final static int HEADER_SUBJECT	=	3;
	public final static String HEADER_SUBJECT_STRING	= "SUBJECT";
		
	// ================================================================================================================
	// CONSTRUCTOR
	public TestHeaderProcessor() throws Throwable {
		super();
	}
	
	// ================================================================================================================
	// ================================================================================================================
	// ABSTRACT METHODS
	
	/**
	 * All declarations should be put here, so they are done with any initialization.
	 * @throws Throwable
	 */
	protected void declarations() throws Throwable {
		declare(HEADER_DA_STRING,	HEADER_DA, 	 false);
		declare(HEADER_FA_STRING,	HEADER_FA,	false);
		declare(HEADER_SUBJECT_STRING,	HEADER_SUBJECT,	false);		
	}
	
	/**
	 * Start on a specific header.  This gives the implementation a chance to initialize.
	 * @param messageId the id for the message being processed.  The implementation may choose to ignore it.
	 * @throws Throwable
	 */
	protected void start(String messageId) throws Throwable {
		matchedValues  = new ThingsPropertyTreeBASIC();
		unmatchedValues  = new ThingsPropertyTreeBASIC();
	}
	/**
	 * This method will be called when a header is matched.  Be sure to write to outs if you want anything preserved!  This includes the CRLF.
	 * The read() method will supply the read of the header line.  
	 * @param id The defined id.
	 * @param out OutputStream to write the processed data.
	 * @throws Throwable
	 */
	protected void match(int	id, OutputStream 	out) throws Throwable {
		matchReadOnly(id);
	}
	
		/**
	 * This method will be called when a header is unmatched.  Be sure to write to outs if you want anything preserved!  This includes the CRLF (or double CRLF if it is the last match).
	 * The read() method will supply the read of the header line.  
	 * @param out OutputStream to write the processed data.
	 * @param headerBuffer What we read about the header already.
	 * @throws Throwable
	 */
	protected void unmatch(byte[] headerBuffer, 		int size, 	OutputStream 	out) throws Throwable {
		try  {
			String name = new String(headerBuffer, 0, size);
			String value = rest(this);
			unmatchedValues.setProperty(name, value);
		} catch (Throwable t) {
			throw new ThingsException("Processing broke on unrecognized header.", t);
		}
	}

	/**
	 * This method will be called when a header is matched.  This will be called when a header is matched for a read-only operation.
	 * @param id The defined id.
	 * @throws Throwable
	 */
	protected void matchReadOnly(int	id) throws Throwable {
		String value = null;
		try {
			value = rest(this);
		} catch (Throwable t) {
			throw new ThingsException("Bad header.", t);
		}
		
		switch(id) {
		case HEADER_DA: 
			matchedValues.setProperty(HEADER_DA_STRING, value);
			break;
			
		case HEADER_FA: 
			matchedValues.setProperty(HEADER_FA_STRING, value);
			break;
			
		case HEADER_SUBJECT:
			matchedValues.setProperty(HEADER_SUBJECT_STRING, value);
			break;
		
		default:
			ThingsException.softwareProblem("Matched but not declared.  This is a bug.");
		}
	}
	
	/**
	 * Complete on a specific header.  This gives the implementation a chance to return entries.  If anything is returned at all
	 * is up to the implementation.  It can give a null if it wants to.
	 * @return A list of header fields.
	 * @throws Throwable
	 */
	protected List<String> complete(OutputStream 	out) throws Throwable {
		return null;
	}
	
	// ========================================================================================
	// METHODS
	
	/**
	 * Read the rest of the stream into a new string.
	 * BE VERY BAD AND ASSUME 8bit characters!
	 * @return A String with the rest of the stream.
	 * @param is the stream to deplete.
	 */
	private String rest(InputStream is) throws Throwable {
		StringBuffer buffer = new StringBuffer();
		int current = is.read();		
		while (current >= 0) {
			buffer.append((char)current);    // bad programmer!
			current = is.read();
		}
		return buffer.toString();
	}
	
}


