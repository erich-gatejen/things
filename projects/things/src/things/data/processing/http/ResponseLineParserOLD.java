/**
 * THINGS/THINGER 2004, 2005, 2006, 2006, 2007
 * Copyright Erich P Gatejen (c) 2004, 2005, 2006, 2007 ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.data.processing.http;

import java.io.IOException;
import java.io.InputStream;

import things.common.ThingsException;
import things.data.processing.LexicalTool;
import things.thinger.io.StreamSource;
import things.thinger.io.StreamSourceFromStreamStalled;
import things.thinger.io.StreamSourceFromString;

/**
 * An HTTP Response Line parser.  Uses the same method as the AddressParser.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 07
 * </pre> 
 */
public class ResponseLineParserOLD extends LexicalTool {

	// =========================================================================================================
	// PUBLIC METHODS
	
	/**
	 * Parse the source as an HTTP request.
	 * @param source the source data.
	 * @param response put values into this object.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 */
	//static public void parseAndSave(StreamSource source, HttpResponse response) throws ThingsException {
		
	//	ResponseLineParser instanceParser = new ResponseLineParser();
	//	instanceParser.parser(source, response);
	//}
	
	// =========================================================================================================
	// DATA
	private HttpResponse 	REG_response;
	private StringBuffer	ACCUMULATOR;
	private StreamSource ios;
	
	// =========================================================================================================
	// PRIVATE ENGINE	
	
	/**
	 * Parse engine grammar.<br><pre>
	 * HTTP/1.1 200 OK

Lexical elements:	
		URLCHAR = Letters (A-Z and a-z), numbers (0-9) and the characters '.', '-', '~' and '_', plus we treat # as a character because
                  we don't distinguish is for processing.
 		WS = whitespace not including CR/LF
 		SLASH = forward slash.
		!OTHER! (meaning anything not listed).
		
Registers
		Version			- response.version
		Code			- response.code
		Text			- response.text
		
HTTP/1.1<SPACE>200<SPACE>OK...<CR><LF>

[START]
	-> [OPEN]
	-> ^RETURN^

[OPEN]
	- URLCHAR	- push, VERSION, ^RETURN^
	- !OTHER!	- [DEPLETE], error(must start with a character)
	- !EOF!		- fault(No request present)
	
[VERSION]
	- URLCHAR	- push
	- SLASH		- push
	- WS		- pop->$Version, [CODE], ^RETURN^
	- !OTHER!	- [DEPLETE], error(expecting code)
	- !EOF!		- fault(Only VERSION present)	
	
[CODE]
	- URLCHAR	- push
	- WS		- pop->$Code, [TEXT], ^RETURN^
	- !OTHER!	- [DEPLETE], error(bad code)
	- !EOF!		- error(No TEXT present)		
	
[TEXT]
	- URLCHAR	- push
	- WS		- push
	- CR 	 	- pop->$Text, [DEPLETE], ^RETURN^	
	- !OTHER!	- [DEPLETE], errorbad characters in Text.
	- !EOF!		- error(status line not terminated)				
	
[DEPLETE]
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- error(missing CR at end of line)
	
[DEPLETE_CR]
	- LF		- ^RETURN^
	- !EOF!		- error(missing LF after CR at end of line: truncated.)
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)

	 * @param source the stream source.  
	 * @param response the response object to fill.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 */
	synchronized public void parser(StreamSource source, HttpResponse response) throws ThingsException {
		
		// Prepare
		ios = source;
		this.REG_response = response;
		
		// Invoke
		try {
			START();
		} catch (ThingsException te) {
			throw te;
		} catch (IOException ioe) {
 			throw new ThingsException("Failed body parsing due to source IO problem.", ThingsException.GENERAL_PARSER_FAULT, ioe);
		} catch (Throwable e) {
			throw new ThingsException("Failed body parsing due general problem.", ThingsException.GENERAL_PARSER_FAULT, e);			
		}
	}
	
	/** 
	 * Call with an InputStream.
	 * @param ins the source stream.
	 * @param response the response object to fill.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 */
	synchronized public void parser(InputStream ins, HttpResponse response) throws ThingsException {
		if (ins==null) ThingsException.softwareProblem("Can't give a null input stream.");
		StreamSourceFromStreamStalled source;
		try {
			source = new StreamSourceFromStreamStalled(ins);
			if (source.available()<1) throw new ThingsException("Timeout reading input.", ThingsException.GENERAL_PARSER_FAULT);
		} catch (ThingsException te) {
			throw te;
		} catch (Throwable t) {
			throw new ThingsException("Failed body parsing because of a problem with the stream source.", ThingsException.GENERAL_PARSER_FAULT, t);
		}

		this.parser(source,response);
	}
	
	/** 
	 * Call with a String.
	 * @param data the String
	 * @param response the response object to fill.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 */
	synchronized public void parser(String data, HttpResponse response) throws ThingsException {
		if (data==null) ThingsException.softwareProblem("Can't give a null String.");
		this.parser(new StreamSourceFromString(data), response);
	}
	
	// == REDUCTIONS ===================================================================
	
	/**
[START]
	-> [OPEN]
	-> ^RETURN^
	 */
	private void START() throws Throwable {
		ACCUMULATOR = new StringBuffer();
		OPEN();		
	}
	
/**
 * [OPEN]
	- URLCHAR	- push, VERSION, ^RETURN^
	- !OTHER!	- [DEPLETE], error(must start with a character)
 */
	private void OPEN() throws Throwable {
		if (!ios.hasMore()) error("Timeout waiting for client request.");  // Happens when we starve the connection and the stall isn't enough.
		int character = ios.next();
		if (LexicalTool.getURIType(character)==URLCHAR) {
			ACCUMULATOR.append((char)character); 
			VERSION();
			return;
		} else {
			DEPLETE();
			fault("Must start with a character");
		}
	} // END OPEN()
	
	/**
	 * [VERSION]
	- URLCHAR	- push
	- SLASH		- push
	- WS		- pop->$Version, [CODE], ^RETURN^
	- !OTHER!	- [DEPLETE], error(expecting code)
	- !EOF!		- fault(Only VERSION present)	
	 * @throws Throwable
	 */
	private void VERSION() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
			case SPECIAL_SLASH:
				ACCUMULATOR.append((char)character); 
				break;
				
			case WS:			
				REG_response.httpVersion = pop();
				CODE();
				return;

			default:
				DEPLETE();
				error("Excepting Code.  character=" + character);
			break;						
				
			} // end switch
		} // end while
		
		// !EOF!
		fault("Only VERSION present.");
		
	} // end VERSION
	
	/**
	 * [CODE]
	- URLCHAR	- push
	- WS		- pop->$Code, [TEXT], ^RETURN^
	- !OTHER!	- [DEPLETE], error(bad code)
	- !EOF!		- error(No TEXT present)	
	 * @throws Throwable
	 */
	private void CODE() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				break;
				
			case WS:			
				REG_response.codeText = pop();
				try {
					REG_response.code = Integer.parseInt(REG_response.codeText);
				} catch (Throwable t) {
					error("Bad Code.  Note a numeric.  value=" + REG_response.codeText);		
				}
				TEXT();
				return;

			default:
				DEPLETE();
				error("Bad Code.  character=" + character);
			break;						
				
			} // end switch
		} // end while
		
		// !EOF!
		fault("No TEXT present.");
		
	} // end CODE

	/**
	 * [TEXT]
	- URLCHAR	- push
	- WS		- push
	- CR 	 	- pop->$Text, [DEPLETE], ^RETURN^	
	- !OTHER!	- [DEPLETE], error(bad character in Text.
	- !EOF!		- error(status line not terminated)		
	 * @throws Throwable
	 */
	private void TEXT() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
			case WS:		
				ACCUMULATOR.append((char)character); 
				break;

			case WS_CR_CONTROL:
				REG_response.reasonPhrase = pop();
				DEPLETE_CR();
				return;

			default:
				DEPLETE();
				error("Bad character in Text.  character=" + character);
			break;						
				
			} // end switch
		} // end while
		
		// !EOF!
		fault("Status line not terminated.");
		
	} // end TEXT
	
	/**
	 * [DEPLETE]
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- error(missing CR at end of line)	
	 * @throws Throwable
	 */
	private void DEPLETE() throws Throwable {
		while (ios.hasMore()) {
			
			if (ios.next() == WS_CR_CONTROL) {
				DEPLETE_CR();
				return;
			} 
			// ELSE BURN IT
		} // end while
		
		// !EOF!
		error("Missing CR at end of line.");
		
	} // end VERSION
	
	/**
	 * [DEPLETE_CR]
	- LF		- [CR], ^RETURN^
	- !EOF!		- error(missing LF after CR at end of line: truncated.)
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)
	 * @throws Throwable
	 */
	private void DEPLETE_CR() throws Throwable {
		if (!ios.hasMore()) error("Missing LF after CR at end of line--truncated.");
		
		if (ios.next() == WS_LF_CONTROL) {
			return;
		} 
		
		fault("Missing LF after CR at end of line: odd characters found, so stream is unreliable.");
		
	} // end DEPLETE_CR	
	
	// == TOOLS ===================================================================
	
	/**
	 * Throw an error with consistent formatting and the code GENERAL_PARSER_ERROR.
	 * @param text the text of the error.
	 * @throws Throwable (though it will always be a ThingsException).
	 */
	private void error(String text) throws Throwable {
		if (ACCUMULATOR != null) {
			throw new ThingsException("Error: " + text + " acc=" + ACCUMULATOR.toString(), ThingsException.GENERAL_PARSER_ERROR);
		} else {
			throw new ThingsException("Error: " + text, ThingsException.GENERAL_PARSER_ERROR);
		}
	}
	
	/**
	 * Throw a fault with consistent formatting and the code GENERAL_PARSER_FAULT.
	 * @param text the text of the error.
	 * @throws Throwable
	 */
	private void fault(String text) throws Throwable {
		if (ACCUMULATOR != null) {
			throw new ThingsException("Fault: " + text + " acc=" + ACCUMULATOR.toString(), ThingsException.GENERAL_PARSER_FAULT);
		} else {
			throw new ThingsException("Fault: " + text, ThingsException.GENERAL_PARSER_FAULT);
		}
	}


	/**
	 * Pop the accumulator.  The accumulator will be empty when this is done.
	 * @throws Throwable
	 * @return the contents of the accumulator.
	 */
	private String pop() throws Throwable {
		String result = ACCUMULATOR.toString();
		ACCUMULATOR = new StringBuffer();
		return result;
	}
	
}


