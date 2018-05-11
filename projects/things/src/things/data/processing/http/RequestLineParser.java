/**
 * THINGS/THINGER 2004, 2005, 2006, 2006, 2007
 * Copyright Erich P Gatejen (c) 2004, 2005, 2006, 2007 ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.data.processing.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import things.common.ThingsException;
import things.data.processing.LexicalTool;

/**
 * An HTTP Request Line parser.  Uses the same method as the AddressParser.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 FEB 07
 * EPG - Got rid of the Stream Source.  It might cause longer hangs, but the source implementation will lose the tee character. 
 * EPG - Allowed a lot of characters not normally allowed by spec because a lot of web folks out there are very naughty.
 * </pre> 
 */
public class RequestLineParser extends LexicalTool {

	// =========================================================================================================
	// PUBLIC METHODS
	
	/**
	 * Parse the source as an HTTP request.
	 * @param source the source data.
	 * @param request put values into this object.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 */
	//static public void parseAndSave(StreamSource source, HttpRequest request) throws ThingsException {
	//	
	//	RequestLineParser instanceParser = new RequestLineParser();
	//	instanceParser.parser(source, request);
	//}
	
	// =========================================================================================================
	// DATA
	private String			REG_name;
	private String			REG_value;
	private HttpRequest 	REG_request;
	private	int				REG_sixteens;
	private boolean			FLAG_DoneURI;
	private StringBuffer	ACCUMULATOR;
	private InputStream ios;
	
	// =========================================================================================================
	// PRIVATE ENGINE	
	
	/**
	 * Parse engine grammar.<br><pre>

Lexical elements:	
		URLCHAR = Letters (A-Z and a-z), numbers (0-9) and the characters '.', '-', '~' and '_', plus we treat # as a character because
                  we don't distinguish is for processing.
 
		!OTHER! (meaning anything not listed).
		PERCENT = %
		SPLAT	= *
		FSLASH	= /
		PLUS	= +
		QUEST	= ?
		AMP		= &
		EQU		= =
		
Registers
		Method			- request.method
		Server			- request.server
		Path			- request.path
		(Hex)Sixteens
		
Flags	
		DoneURI

GET<SPACE>http://www.yahoo.com/monkey%20head/?l=1207011680%20monke<SPACE>HTTP-Version<CR><LF>
method
<SPACE>

[START]
	-> NULL->$Method
	-> NULL->$Server
	-> NULL->$Path
	-> NULL->$Version
	-> NULL->$(Hex)Sixteens
	-> FALSE->!DoneURI
	-> [OPEN]
	-> ^RETURN^

[OPEN]
	- URLCHAR	- push, METHOD, ^RETURN^
	- !OTHER!	- [DEPLETE], error(must start with a character)
	- !EOF!		- fault(No request present)
	
[METHOD]
	- URLCHAR	- push
	- PERCENT	- [ESCAPE]
	- WS		- pop->$Method, [POSTMETHOD], ^RETURN^
	- CR | LF 	- error(terminated after METHOD)
	- !OTHER!	- [DEPLETE], error(expecting method)
	- !EOF!		- fault(Only METHOD present)	
	
[POSTMETHOD]
	- WS		- burn
	- SPLAT		- ""->$Server, ->$Path, [STAR], ^RETURN^
	- FSLASH	- ""->$Server, push, [PATH], ^RETURN^
	- URLCHAR	- push, [SERVER], ^RETURN^
	- PERCENT	- [ESCAPE]	
	- QUEST		- [DEPLETE], error(expecteding PATH before URI)
	- CR | LF 	- [DRAIN], error(terminated before URI)
	- !OTHER!	- [DEPLETE], error(bad characters)
	- !EOF!		- error(No URI present)		
	
[STAR]
	- WS		- burn, [POSTSTAR], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without version))		
	- !OTHER!	- [DEPLETE], error(excess characters after SPLAT)
	- !EOF!		- error(No URI present)			
	
[POSTSTAR]
	- WS		- burn
	- CR | LF 	- [DRAIN], error(terminated without version))	
	- URLCHAR	- push, [VERSION], ^RETURN^
	- !OTHER!	- [DEPLETE], error(bad characters)	
	- !EOF!		- error(No Version present)				
	
[SERVER] 
	- PERCENT	- [ESCAPE]
	- WS		- pop->$Server, ""->$Path, [VERSION], ^RETURN^
	- FSLASH	- pop->$Server, push, [PATH], ^RETURN^
	- QUEST		- [DEPLETE], error(expecteding PATH before URI query)
	- URLCHAR	- push
	- CR | LF 	- [DRAIN], error(terminated before finishing URI)
	- !OTHER!	- [DEPLETE], error(bad characters)
	- !EOF!		- error(No compelted URI)		
	
[PATH]
	- PERCENT	- [ESCAPE]
	- URLCHAR	- push
	- FSLASH	- push
	- AMP		- push
	- PLUS		- push(" ")
	- WS		- pop->$Path, [VERSION], ^RETURN^
	- QUEST		- pop->$Path, [START_URI], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without version)	
	- !OTHER!	- [DEPLETE], error(bad characters in path)
	- !EOF!		- error(No completed URI)
	
[START_URI]
	- PERCENT	- [ESCAPE], [NAME]
	- URLCHAR	- push, [NAME], if(!DoneURL==TRUE) ^RETURN^
	- PLUS		- push(" "), [NAME], if(!DoneURL==TRUE) ^RETURN^
	- WS		- [POST_URI], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without version)	
	- !OTHER!	- [DEPLETE], error(bad characters in URI)
	- !EOF!		- error(No completed URI)		
	
[NAME]
	- PERCENT	- [ESCAPE]
	- WS		- [DEPLETE], error(broken name in URI)
	- URLCHAR	- push
	- SPECIAL_SLASH	- push("/");
	- PLUS		- push(" ")	
	- EQU		- pop->$Name, [START_VALUE], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without completing query name)	
	- !OTHER!	- [DEPLETE], error(bad characters in URI query name)
	- !EOF!		- error(Truncated URI)			

[START_VALUE]
	- PERCENT	- [ESCAPE], [VALUE], ^RETURN^
	- WS		- TRUE->!DoneURI, [SETNV_EMPTY], [POST_URI], ^RETURN
	- EQ	- push, [VALUE], ^RETURN^
	- URLCHAR	- push, [VALUE], ^RETURN^
	- SPECIAL_SLASH	- push("/"), [VALUE], ^RETURN^;	
	- PLUS		- push(" "), [VALUE], ^RETURN^
	- AMP		- [SETNV], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated completing name/value)	
	- !OTHER!	- [DEPLETE], error(bad characters when starting value in URI)	
	- !EOF!		- error(Truncated URI missing value for query item.)		
	
[VALUE]
	- PERCENT	- [ESCAPE]
	- URLCHAR	- push
	- EQUAL		- push		
	- SPECIAL_SLASH	- push("/")
	- PLUS		- push(" ")		
	- WS		- TRUE->!DoneURI, [SETNV], [POST_URI], ^RETURN^
	- AMP		- [SETNV], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated while completing query value)	
	- !OTHER!	- [DEPLETE], error(bad characters for value in URI)	
	- !EOF!		- error(Truncated URI)		
	
[SETNV]
-> pop->$Value
-> (Set request NV to $Name/$Value
-> ^RETURN^

[SETNV_EMPTY]
-> (Set request NV to $Name/"" (blank)
-> ^RETURN^
	
[POST_URI]
	- WS		- burn
	- URLCHAR	- push, [VERSION], ^RETURN^	
	- SPECIAL_SLASH	- push("/")	, ^RETURN^	
	- CR | LF 	- [DRAIN], error(terminated without completing VERSION)	
	- !OTHER!	- [DEPLETE], error(bad character starting VERSION)		
	- !EOF!		- error(No version present.)			
	
[VERSION]
	- URLCHAR	- push
	- FSLASH	- push
	- CR | LF 	- [DRAIN], pop->$Version, ^RETURN^	
	- WS		- [DEPLETE], error(spaces after VERSION)
	- !OTHER!	- [DEPLETE], error(bad character in VERSION)		
	- !EOF!		- error(Truncated VERSION.)		
	
[ESCAPE]
	- HEX		- ->$Sixteens, ESCAPEONES, ^RETURN^
	- !OTHER!	- error(broken escape)
	- !EOF!		- error(Truncated line with dangling escape.)
	
[ESCAPEONES]
	- HEX		- push( ($SixteensSPLAT16)PLUSHEX ), ^RETURN^
	- !OTHER!	- error(broken escape)	
	- !EOF!		- error(Truncated line with dangling escape.)			
	
[DEPLETE]
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- error(missing CR at end of line)
	
[DEPLETE_CR]
	- LF		- ^RETURN^
	- !EOF!		- error(missing LF after CR at end of line: truncated.)
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)

[DRAIN]
	- LF		- burn, ^RETURN^
	- CR		- burn, ^RETURN^
	- !EOF!		- error(bad CR/LF line termination: truncated.)	
	- !OTHER!	- fault(bad CR/LF line termination: odd characters found, so stream is unreliable.)	
	
	</pre> 
	 * <p>
	 * @param source the stream source.  
	 * @param request the request object to fill.
	 * @throws ThingsException If it is a fault, the request should be considered completely invalid.  If it is an error, whatever was set in the request might be useful.
	 * @throws IOException if the source is dead.
	 */
	synchronized public void parser(InputStream source, HttpRequest request) throws Throwable {
		
		// Prepare
		ios = source;
		this.REG_request = request;
		
		// Invoke
		try {
			START();
		} catch (ThingsException te) {
			throw te;
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable e) {
			throw new ThingsException("Failed body parsing due general problem.", ThingsException.GENERAL_PARSER_FAULT, e);			
		}
	}

	// == REDUCTIONS ===================================================================
	
	/**
	 * [START]
	-> NULL->$Method
	-> NULL->$Server
	-> NULL->$Path
	-> NULL->$(Hex)Sixteens
	-> FALSE->!DoneURI
	-> [OPEN]
	-> ^RETURN^
	 */
	private void START() throws Throwable {
		REG_name = null;
		REG_value = null;
		// REG_request = null; // We don't do this.
		REG_sixteens = 0;
		FLAG_DoneURI = false;
		ACCUMULATOR = new StringBuffer();
		OPEN();		
	}
	
/**
 * [OPEN]
	- URLCHAR	- push, METHOD, ^RETURN^
	- !OTHER!	- [DEPLETE], error(must start with a character)
	- !EOF!		- fault(No request present)
 */
	private void OPEN() throws Throwable {
		int character = ios.read();
		if (character<0) throw new EOFException();
		if (LexicalTool.getURIType(character)==URLCHAR) {
			ACCUMULATOR.append((char)character); 
			METHOD();
			return;
		} else {
			DEPLETE();
			fault("Must start with a character");
		}
	} // END OPEN()
	
	/**
	 * [METHOD]
	 * 	- URLCHAR	- push
		- PERCENT	- [ESCAPE]
		- WS		- push, pop->$Method, [POSTMETHOD], ^RETURN^
		- CR | LF 	- [DRAIN], error(terminated after METHOD)
		- !OTHER!	- [DEPLETE], error(expecting method)
		- !EOF!		- fault(Only method present)	
	 * @throws Throwable
	 */
	private void METHOD() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case WS:			
				String match = pop();
				REG_request.method = HttpRequest.Method.match(match);
				if (REG_request.method==HttpRequest.Method.UNSUPPORTED) {
					DEPLETE();
					error("unsupported method.  text='" + match + "'");
				}
				POSTMETHOD();
				return;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated after METHOD");
				break;

			default:
				DEPLETE();
				error("character not allowed in open.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end METHOD
	
	/**
	 * [POSTMETHOD]
	- URLCHAR	- push, [SERVER], ^RETURN^
	- PERCENT	- [ESCAPE]	
	- WS		- burn
	- SPLAT		- ""->$Server, ->$Path, [STAR], ^RETURN^
	- FSLASH	- ""->$Server, push, [PATH], ^RETURN^
	- QUEST		- [DEPLETE], error(expecting PATH before URI)
	- CR | LF 	- [DRAIN], error(terminated before URI)
	- !OTHER!	- [DEPLETE], error(bad characters)
	- !EOF!		- error(No URI present)	
	 * @throws Throwable
	 */
	private void POSTMETHOD() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				SERVER();
				return;
	
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case WS:			
				break;
				
			case SPECIAL_SPLAT:
				REG_request.server = "";
				REG_request.path = "*";
				STAR();
				return;
		
			case SPECIAL_SLASH:
				REG_request.server = "";
				ACCUMULATOR.append((char)character); 
				PATH();
				return;
				
			case SPECIAL_QUEST:
				DEPLETE();
				error("Expecting PATH before URI");
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated before URI.");
				break;

			default:
				DEPLETE();
				error("Bad characters.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end POSTMETHOD
	
	/**
	 * [STAR]
	- WS		- burn, [POSTSTAR], ^RETURN^
	- CR | LF 	- [DRAIN], error(tterminated without version))		
	- !OTHER!	- error(excess characters after SPLAT)
	- !EOF!		- error(No URI present)	
	 * @throws Throwable
	 */
	private void STAR() throws Throwable {
		int character = ios.read();
		if (character<0) throw new EOFException();
		
		switch(LexicalTool.getURIType(character)) {				
			
		case WS:		
			POSTSTAR();
			return;
			
		case WS_LF_CONTROL:
		case WS_CR_CONTROL:
			DRAIN();
			error("Terminated without VERSION.");
			break;

		default:
			DEPLETE();
			error("Excess characters after '*'.");
		break;						
			
		} // end switch	
		
	} // end STAR
	
	/**
	 * [POSTSTAR]
	- WS		- burn
	- CR | LF 	- [DRAIN], error(Terminated without version))	
	- URLCHAR	- push, [VERSION], ^RETURN^
	- !EOF!		- error(No Version present)		
	 * @throws Throwable
	 */
	private void POSTSTAR() throws Throwable {
		int character = ios.read();
		if (character<0) throw new EOFException();
		switch(LexicalTool.getURIType(character)) {				
			
		case WS:		
			break;
			
		case WS_LF_CONTROL:
		case WS_CR_CONTROL:
			error("Terminated without VERSION.");
			break;
			
		case URLCHAR:
			ACCUMULATOR.append((char)character); 
			VERSION();
			return;

		default:
			DEPLETE();
			error("Bad characters after '*'.");
		break;						
			
		} // end switch	
		
	} // end POSTMETHOD
	
	/**
	 * [SERVER] 
	- URLCHAR	- push
	- PERCENT	- [ESCAPE]
	- WS		- pop->$Server, ""->$Path, [VERSION], ^RETURN^
	- FSLASH	- pop->$Server, push, [PATH], ^RETURN^
	- QUEST		- [DEPLETE], error(Expecting PATH before URI query)
	- CR | LF 	- [DRAIN], error(terminated before finishing URI)
	- !OTHER!	- [DEPLETE], error(bad characters)
	- !EOF!		- error(No completed URI)	
	 * @throws Throwable
	 */
	private void SERVER() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				break;
	
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case WS:
				REG_request.server = pop();
				REG_request.path = "";
				VERSION();
				return;
				
			case SPECIAL_SLASH:
				REG_request.server = "";
				ACCUMULATOR.append((char)character); 
				PATH();
				return;				
				
			case SPECIAL_QUEST:
				DEPLETE();
				error("Expecting PATH before URI query");	
				break;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated before finishing URI.");
				break;

			default:
				DEPLETE();
				error("Bad characters.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end SERVER	
	
	/**
	 * [PATH] 
	- URLCHAR	- push
	- FSLASH	- push
	- AMP		- push	
	- PERCENT	- [ESCAPE]
	- PLUS		- push(" ")
	- WS		- pop->$Path, [VERSION], ^RETURN^
	- QUEST		- pop->$Path, [START_URI], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without version)	
	- !OTHER!	- [DEPLETE], error(bad characters in path)
	- !EOF!		- error(No completed URI)	
	 * @throws Throwable
	 */
	private void PATH() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case URLCHAR:
			case SPECIAL_SLASH:		
			case SPECIAL_AMP:
				ACCUMULATOR.append((char)character); 
				break;
	
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' '); 
				break;			

			case WS:
				REG_request.path = pop();
				VERSION();
				return;
				
			case SPECIAL_QUEST:
				REG_request.path = pop();				
				START_URI();
				return;				
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated without version.");
				break;

			default:
				DEPLETE();
				error("Bad character in path.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end PATH		
	
	/**
	 * [START_URI]
	- PERCENT	- [ESCAPE], [NAME]
	- URLCHAR	- push, [NAME], if(!DoneURL==TRUE) ^RETURN^
	- PLUS		- push(" "), [NAME], if(!DoneURL==TRUE) ^RETURN^
	- WS		- [POST_URI], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without version)	
	- !OTHER!	- [DEPLETE], error(bad characters in URI)
	- !EOF!		- error(No completed URI)
	 * @throws Throwable
	 */
	private void START_URI() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				NAME();
				if (FLAG_DoneURI==true) return;
				break;
		
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' '); 
				NAME();
				if (FLAG_DoneURI==true) return;
				break;		

			case WS:
				POST_URI();
				return;		
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated without version.");
				break;

			default:
				DEPLETE();
				error("Bad character in path.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end START_URI		

	/**
	 * [NAME]
	- PERCENT	- [ESCAPE]
	- WS		- [DEPLETE], error(broken name in URI)
	- URLCHAR	- push
	- SPECIAL_SLASH	- push("/");
	- PLUS		- push(" ")	
	- EQU		- pop->$Name, [START_VALUE], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated without completing query name)	
	- !OTHER!	- [DEPLETE], error(bad characters in URI)
	- !EOF!		- error(Truncated URI)			
	 * @throws Throwable
	 */
	private void NAME() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case WS:
				DEPLETE();
				error("Broken name in URI.");
				break;
			
			case URLCHAR:
			case SPECIAL_COLON:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_SLASH:
				ACCUMULATOR.append('/');
				break;
		
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' '); 
				break;			
				
			case SPECIAL_EQ:
				REG_name = pop();
				START_VALUE();
				return;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated without completing query name.");
				break;

			default:
				DEPLETE();
				error("Bad character in URI query name.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end NAME		
	
	/**
	 * [START_VALUE]
	- PERCENT	- [ESCAPE], [VALUE], ^RETURN^
	- WS		- TRUE->!DoneURI, [SETNV_EMPTY], [POST_URI], ^RETURN
	- EQ	- push, [VALUE], ^RETURN^	
	- URLCHAR	- push, [VALUE], ^RETURN^
	- SPECIAL_SLASH	- push("/"), [VALUE], ^RETURN^;	
	- PLUS		- push(" "), [VALUE], ^RETURN^
	- AMP		- [SETNV], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated completing name/value)	
	- !OTHER!	- [DEPLETE], error(bad characters when starting value in URI)	
	- !EOF!		- error(Truncated URI)				
	 * @throws Throwable
	 */
	private void START_VALUE() throws Throwable {
		int character = ios.read();
		if (character<0) throw new EOFException();
		switch(LexicalTool.getURIType(character)) {			
		
		case SPECIAL_PERCENT:
			ESCAPE(); 
			VALUE();
			return;
			
		case WS:
			FLAG_DoneURI = true;
			SETNV_EMPTY();
			POST_URI();
			return;
		
		case URLCHAR:
		case SPECIAL_EQ:
			ACCUMULATOR.append((char)character); 
			VALUE();
			return;
			
		case SPECIAL_SLASH:
			ACCUMULATOR.append('/'); 
			VALUE();
			return;			
	
		case SPECIAL_PLUS:
			ACCUMULATOR.append(' '); 
			VALUE();
			return;	
			
		case SPECIAL_AMP:
			SETNV();			// Empty value
			return;
			
		case WS_LF_CONTROL:
		case WS_CR_CONTROL:
			DRAIN();
			error("Terminated without completing query name/value.");
			break;

		default:
			DEPLETE();
			error("Bad character when starting value in URI.");
		break;						
			
		} // end switch
		
	} // end START_VALUE		

	/**
	 * [VALUE]
	- PERCENT	- [ESCAPE]
	- EQUAL		- push			Some assbandits are emitting this raw
	- URLCHAR	- push
	- SPECIAL_SLASH	- push("/")	
	- PLUS		- push(" ")		
	- WS		- TRUE->!DoneURI, [SETNV], [POST_URI], ^RETURN^
	- AMP		- [SETNV], ^RETURN^
	- CR | LF 	- [DRAIN], error(terminated completing value)	
	- !OTHER!	- [DEPLETE], error(bad characters for value in URI)	
	- !EOF!		- error(Truncated URI)			
	 * @throws Throwable
	 */
	private void VALUE() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
			
			case SPECIAL_PERCENT:
				ESCAPE(); 
				break;
				
			case SPECIAL_EQ:
			case URLCHAR:
			case SPECIAL_COLON:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_SLASH:
				ACCUMULATOR.append('/'); 
				break;
		
			case SPECIAL_PLUS:
				ACCUMULATOR.append(' ');
				break;
				
			case WS:
				FLAG_DoneURI = true;
				SETNV();
				POST_URI();
				return;
	
			case SPECIAL_AMP:
				SETNV();
				return;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated while completing query value.");
				break;

			default:
				DEPLETE();
				error("Bad character in URI query value.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end VALUE
	
	/**
	 * [SETNV]
-> pop->$Value
-> (Set request NV to $Name/$Value
-> ^RETURN^	
	 * @throws Throwable
	 */
	private void SETNV() throws Throwable {
		REG_value = pop();
		REG_request.urlValues.setProperty(this.REG_name, REG_value);
		
	} // end SETNV		
	
	/**
	 * [SETNV_EMPTY]
	-> (Set request NV to $Name/"" (blank)
	-> ^RETURN^
	 * @throws Throwable
	 */
	private void SETNV_EMPTY() throws Throwable {
		REG_request.urlValues.setProperty(this.REG_name, "");
		
	} // end SETNV_EMPTY		
	
	/**
	[POST_URI]
	- WS		- burn
	- URLCHAR	- push, [VERSION], ^RETURN^	
	- SPECIAL_SLASH	- push("/"), [VERSION], ^RETURN^			
	- CR | LF 	- [DRAIN], error(terminated without completing VERSION)	
	- !OTHER!	- [DEPLETE], error(bad character starting VERSION)		
	- !EOF!		- error(No version present.)		
	 * @throws Throwable
	 */
	private void POST_URI() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
				
			case WS:
				break;
			
			case URLCHAR:
				ACCUMULATOR.append((char)character); 
				VERSION();
				return;
			
			case SPECIAL_SLASH:
				ACCUMULATOR.append('/'); 
				VERSION();
				return;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				error("Terminated without completing VERION");
				return;

			default:
				DEPLETE();
				error("Bad character starting VERSION.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end POST_URI
	
	/**
	 * [VERSION]
	- URLCHAR	- push
	- FSLASH	- push
	- CR | LF 	- [DRAIN], pop->$Version, ^RETURN^	
	- WS		- [DEPLETE], error(spaces after VERSION)
	- !OTHER!	- [DEPLETE], error(bad character in VERSION)		
	- !EOF!		- error(Truncated VERSION.)		
	 * @throws Throwable
	 */
	private void VERSION() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			switch(LexicalTool.getURIType(character)) {			
				
			case URLCHAR:
			case SPECIAL_SLASH:
				ACCUMULATOR.append((char)character); 
				break;
				
			case WS:
				DEPLETE();
				error("Spaces after VERSION.");
				return;
				
			case WS_LF_CONTROL:
			case WS_CR_CONTROL:
				DRAIN();
				REG_request.httpVersion = pop();
				return;

			default:
				DEPLETE();
				error("Bad character in VERSION.  character=" + character);
			break;						
				
			} // end switch
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end VERSION
		
	/**
	 * [ESCAPE]
	- (HEX)		- ->$Sixteens, ESCAPEONES, ^RETURN^
	- !OTHER!	- error(broken escape)
	- !EOF!		- error(Truncated line with dangling escape.)			
	 * @throws Throwable
	 */
	private void ESCAPE() throws Throwable {
		int character = ios.read();	
		if (character<0) throw new EOFException();
		REG_sixteens = LexicalTool.getHexValue(character);
		if (REG_sixteens < 0) error("Broken escape.");
		else {
			ESCAPEONES();
			return;
		}
		
	} // end ESCAPE	
	
	/**
	 * [ESCAPEONES]
	- HEX		- push( ($SixteensSPLAT16)PLUSHEX ), ^RETURN^
	- !OTHER!	- error(broken escape)	
	- !EOF!		- error(Truncated line with dangling escape.)		
	 * @throws Throwable
	 */
	private void ESCAPEONES() throws Throwable {
		int character = ios.read();	
		if (character<0) throw new EOFException();
		int ones = LexicalTool.getHexValue(character);
		if (ones < 0) error("BTruncated line with dangling escape.");
		else {
			ACCUMULATOR.append((char) ((REG_sixteens * 16)+ones) ); 
			return;
		}
		
	} // end ESCAPEONES	
	
	/**
	 * [DEPLETE]
	- CR		- [DEPLETE_CR], ^RETURN^
	- !OTHER!	- burn
	- !EOF!		- error(missing CR at end of line)	
	 * @throws Throwable
	 */
	private void DEPLETE() throws Throwable {
		int character = ios.read();	
		while (character>=0) {
			if (character == WS_CR_CONTROL) {
				DEPLETE_CR();
				return;
			} 
			// ELSE BURN IT
			character = ios.read();
		} // end while
		
		// !EOF!
		throw new EOFException();
		
	} // end VERSION
	
	/**
	 * [DEPLETE_CR]
	- LF		- [CR], ^RETURN^
	- !EOF!		- error(missing LF after CR at end of line: truncated.)
	- !OTHER!	- fault(missing LF after CR at end of line: odd characters found, so stream is unreliable.)
	 * @throws Throwable
	 */
	private void DEPLETE_CR() throws Throwable {
		int character = ios.read();	
		if (character<0) throw new EOFException();
		if (character == WS_LF_CONTROL) {
			return;
		} 
		
		fault("Missing LF after CR at end of line: odd characters found, so stream is unreliable.");
		
	} // end DEPLETE_CR	
	
	/**
	 * [DRAIN]
	- LF		- burn, ^RETURN^
	- CR		- burn, ^RETURN^
	- !EOF!		- error(bad CR/LF line termination: truncated.)	
	- !OTHER!	- fault(bad CR/LF line termination: odd characters found, so stream is unreliable.)	
	 * @throws Throwable
	 */
	private void DRAIN() throws Throwable {
		int character = ios.read();	
		if (character<0) throw new EOFException();
		if ((character == WS_LF_CONTROL)||(character == WS_CR_CONTROL)) {
			return;
		} 
		
		fault("Bad CR/LF line termination: odd characters found, so stream is unreliable.");
		
	} // end DRAIN	
	
	
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


