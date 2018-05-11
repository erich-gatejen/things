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
package things.data.processing.rfc822;

import java.io.IOException;
import java.io.InputStream;

import things.data.processing.LexicalTool;
import things.thinger.io.StreamSource;
import things.thinger.io.StreamSourceFromStreamStalled;
import things.thinger.io.StreamSourceFromString;

/**
 * An 822 address parser for MSG headers.
 * <p>
 * The submitted addreses may have whitespace at either end of the strings.  Trim if you wish.  Note that CR or LFs will be converted to spaces.
 * 
 * <p>
 * It isn't as much work as it appears.  It took me about 30 minutes to map it in a spreadsheet.  After another hour, I had 
 * the parse language (as seen in the comments below).  And another hour after that it was coded and done.  I've found only
 * one bug since, which traced back to the original spreadsheet. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 22 JAN 08
 * </pre> 
 */
public class AddressParserMSG extends LexicalTool {

	// =========================================================================================================
	// PUBLIC METHODS
	
	/**
	 * Parse the source for addresses.  All addresses will be put in the map with their internet address as a key.  
	 * Duplicate Internet addresses will overwrite the same record, so only the latest friendly name will be remembered.
	 * @param source the source data.
	 * @param addressListener an address listener for found addresses.
	 * @throws Throwable
	 */
	static public void parseAndSave(StreamSource source, AddressListener addressListener) throws Throwable {
		
		AddressParserMSG instanceParser = new AddressParserMSG();
		instanceParser.parser(source, addressListener);
	}
	
	// =========================================================================================================
	// DATA
	private String			REG_friendly;
	private String			REG_address;
	private String			REG_group;
	private String			REG_busted;
	private boolean			FLAG_GROUP;
	private boolean 		FLAG_GROUP_ENDED;
	private StringBuffer	ACCUMULATOR;
	private StreamSource ios;
	private AddressListener addressTarget;
	
	// =========================================================================================================
	// PRIVATE ENGINE	
	
	/**
	 * Parse engine grammer.<br><code>

Lexical elements:	ASCII (0->127),
			CHAR (32->127 minus WS, SPECIAL), 
			
			QUOTE, AT, COLON, SEMICOLON, DOT, OPENBRACK, CLOSEBRACK, 
                        GT, LT, BACKSLASH, COMMA, OPENPAREN, CLOSEPAREN,
						
			WS (space or tab)
			CR, LF
						
			|SPECIAL| (includes QUOTE, AT, COLON, SEMICOLON, DOT, OPENBRACK, CLOSEBRACK, GT, LT, BACKSLASH, COMMA, OPENPAREN, CLOSEPAREN), 
			!OTHER! (meaning anything not listed).

REG:	$GROUP, $FRIENDLY, $ADDRESS, $FLAG_GROUP, $FLAG_GROUP_ENDED

[START]
	-> NULL->$FRIENDLY
	-> NULL->$ADDRESS
	-> NULL->$GROUP
	-> NULL->$BUSTED
	-> false -> $FLAG_GROUP
	-> false -> $FLAG_GROUP_ENDED
	-> [OPEN]
	-> if (EOF) ^RETURN^

[OPEN]	
	- WS, COMMA	- burn
	- CHAR		- push, [ACCUMULATE], ^RETURN^
	- OPENPAREN	- push, [GATHERCOMMENT]
	- QUOTE		- push, [GATHERQUOTE], [ACCUMULATE], ^RETURN^
	- LT		- [LTADDRESS], ^RETURN^	
	- AT		- [SLURP]	
	- SEMICOLON	- if (true = $FLAG_GROUP) then 
				pop->$ADDRESS, [SUBMIT], false->$FLAG_GROUP_ENDED, ^RETURN^
			  else
			  	error(Character not allowed in DN.)
	- |SPECIAL|	- error(meaningless and unquoted special)
	- !OTHER!	- error(character not allowed in open)
	- EOF		- ^EXIT^

	-> ($FLAG_GROUP_ENDED = true)	- false->$FLAG_GROUP_ENDED, ^RETURN^

[ACCUMULATE]
	- CHAR		- push
	- COMMA		- pop->$ADDRESS, submit, ^RETURN^
	- OPENPAREN	- push, [GATHERCOMMENT]
	- QUOTE		- push, [GATHERQUOTE]
	- AT		- push, [NAKED_ADDRESS_DN_ONLY], ^RETURN^
	- WS		- push(SPACE), [ACCUMULATE_WITH_WS], ^RETURN^
	- LT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^	
	- BACKSLASH - push, [FRIENDLY_ESCAPE], [FRIENDLY], ^RETURN^
	- COLON		- [GROUP], ^RETURN^
	- |SPECIAL|	- error(unquoted special)
	- !OTHER!	- error(character not allowed in open)
	- EOF		- push, pop->$ADDRESS, submit, ^RETURN^
	
[ACCUMULATE_WITH_WS]
	- CHAR		- push, [FRIENDLY], ^RETURN^
	- COLON		- [GROUP], ^RETURN^
	- COMMA		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	- OPENPAREN	- push, [GATHERCOMMENT]
	- AT		- error(bad address with unquoted whitespace)
	- WS		- push
	- GT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^
	- |SPECIAL|	- error(unquoted special)
	- !OTHER!	- error(character not allowed in open)
	- EOF		- pop->$ADDRESS, [SUBMIT], ^RETURN^

[FRIENDLY]
	- CHAR		- push
	- AT		- push
	- BACKSLASH - push, FRIENDLY_ESCAPE
	- QUOTE		- push, [GATHERQUOTE]
	- COLON		- [GROUP], ^RETURN^
	- COMMA		- error(no address present)
	- OPENPAREN	- push, [GATHERCOMMENT]
	- WS		- push(SPACE)
	- LT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^
	- |SPECIAL|	- error(unquoted special)
	- !OTHER!	- error(character not allowed in open)
	- EOF		- error(no address)
	
[FRIENDLY_ESCAPE]
	- !OTHER!	- push, ^RETURN^
	- EOF		- error(no address)

[LTADDRESS]
->[LT_FRONT_ADDRESS_OPEN]
-> NULL->$FRIENDLY
->^RETURN^	

[LT_FRONT_ADDRESS_OPEN]
	- WS		- burn
	- OPENPAREN	- push, [GATHERCOMMENT]
	- CHAR		- push, [LT_FRONT_ADDRESS_NORMAL], ^RETURN^
	- QUOTE		- push, [LT_FRONT_ADDRESS_QUOTED], ^RETURN^
	- !OTHER!	- error(Not allowed character)
	- EOF		- error(no address)

[LT_FRONT_ADDRESS_NORMAL]
	- OPENPAREN	- push, [GATHERCOMMENT]
	- CHAR		- push
	- AT		- push, [LT_ADDRESS_DN_ONLY], ^RETURN^
	- WS		- [LT_CLOSE_ONLY], ^RETURN^
	- GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	- !OTHER!	- error(Character not allowed in name.)
	- EOF		- error(no address)
	
[LT_CLOSE_ONLY]
	- GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^	
	- CHAR		- push, [BUSTEDBRACKETADDRESS], ^RETURN^
	- WS		- burn
	- !OTHER!	- error(Cannot put friendly name in address closure)
	- EOF		- error(must close a non-DN address)

[LT_FRONT_ADDRESS_QUOTED]
-> [GATHERQUOTE]
	- OPENPAREN	- push, [GATHERCOMMENT]
	- AT		- push, [LT_ADDRESS_DN_ONLY], ^RETURN^
	- !OTHER!	- error(broken quoted against @ in address)
	- EOF		- error(no address)
	
[LT_ADDRESS_DN_ONLY] 
-> [REQUIRE_DN]
	- OPENPAREN	- push, [GATHERCOMMENT]
	- DNSCHAR	- push
	- WS		- [SEEK_GT], pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	- GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	- !OTHER!	- error(Character not allowed in DN.)  A single quote will be pushed.
	- EOF		- error(no address)
	
[NAKED_ADDRESS_DN_ONLY] 
-> [REQUIRE_DN]
	- OPENPAREN	- push, [GATHERCOMMENT]
	- DNSCHAR	- push
	- SEMICOLON	- if (true = $FLAG_GROUP) then 
				pop->$ADDRESS, [SUBMIT], true->FLAG_GROUP_ENDED, ^RETURN^
			  else
			  	error(Character not allowed in DN.)
	- COMMA		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	- WS		- pop->$ADDRESS, [MAYBE_NOT_AN_ADDRESS], ^RETURN^
	- !OTHER!	- error(Character not allowed in DN.)
	- EOF		- pop->$ADDRESS, [SUBMIT], ^RETURN^

[MAYBE_NOT_AN_ADDRESS]
	- OPENPAREN	- push, [GATHERCOMMENT], [FRIENDLY], ^RETURN^
	- WS		- burn
	- SEMICOLON	- if (true = $FLAG_GROUP) then 
			     pop->$ADDRESS, [SUBMIT], true->FLAG_GROUP_ENDED, ^RETURN^
		          else
		  	     error(Group teminator when group not defined.)
	- LT		- pop->$FRIENDLY, [LTADDRESS], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	- COMMA		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	- EOF		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	- QUOTE		- push, [GATHERQUOTE], [FRIENDLY], ^RETURN^	
	- CHAR		- push, [FRIENDLY], ^RETURN^	
	- !OTHER!	- error(addresses not delimited.)
	
[EXPECT_SEPERATOR_OR_EOF]
	- WS 		- burn
	- OPENPAREN	- burn, [BURNCOMMENT]
	- AT		- [SLURP]
	- SEMICOLON	- if (true = $FLAG_GROUP) then 
			    true->FLAG_GROUP_ENDED, ^RETURN^
		        else
		  	    error(Group teminator when group not defined.)
	- COMMA		- ^RETURN^
	- EOF		- ^RETURN^
	- !OTHER!	- error(addresses not delimited.)

[SEEK_GT]
	- OPENPAREN	- push, [GATHERCOMMENT]	
	- WS		- burn
	- GT		- ^RETURN^
	- !OTHER!	- error(Character not allowed after whitespace, before '>')
	- EOF		- error(address not closed with a '>')

[BUSTEDBRACKETADDRESS]
	- OPENPAREN	- push, [GATHERCOMMENT]	
	- WS		- push(SPACE)
	- GT		- pop->$BUSTED, [SUBMIT], ^RETURN^
	- !OTHER!	- push
	- EOF		- error(address not closed with a '>')

[REQUIRE_DN]
	- OPENPAREN	- push, [GATHERCOMMENT]	
	- DNSCHAR	- push, ^RETURN^
	- OPENPAREN	- push, [GATHERCOMMENT]
	- !OTHER!	- error(bad domain name)
	- EOF		- error(no address)
	
[GATHERQUOTE]
	- BACKSLASH	= burn, [ESCAPE]
	- QUOTE		= push, ^RETURN^
	- !OTHER!	= push
	- EOF		- error(quote left open)
	
[ESCAPE]
	- ASCII 	= push, ^RETURN^
	- EOF		- error(escape left open)
	
[SLURP]
	- WS,COMMA,EOF	= ^RETURN^
	- !OTHER!	= burn
	
[GATHERCOMMENT]
	- CLOSEPAREN	= push, ^RETURN^
	- BACKSLASH	= burn, [ESCAPE]
	- !OTHER!	= push
	- EOF		- error(comment left dangling.)

[BURNCOMMENT]
	- CLOSEPAREN	= ^RETURN^
	- !OTHER!		= burn
	- EOF			- error(comment left dangling.)

[GROUP]
-> if($FLAG_COLON=true, error(cannot imbed groups)), 
-> pop->$GROUP, 
-> true->$FLAG_GROUP, 
-> [OPEN], 
-> NULL->$GROUP, 
-> false->$FLAG_GROUP,

[SUBMIT]
-> submit($GROUPm, $FRIENDLY, $ADDRESS)
-> NULL->$FRIENDLY
-> NULL->$ADDRESS

	</code> 
	 *
	 * 
	 */
	synchronized public void parser(StreamSource source, AddressListener addressListener) throws Throwable {
		
		// Prepare
		ios = source;
		addressTarget = addressListener;
		
		// Invoke
		try {
			START();
		} catch (IOException ioe) {
			throw new Exception("Failed address parsing due to source IO problem.", ioe);
		} catch (Throwable e) {
			throw new Exception("Failed address parsing due general problem.", e);			
		}
	}
	
	/** 
	 * Call with an InputStream.
	 * @param ins the source stream.
	 * @param addressListener
	 * @throws Throwable
	 */
	synchronized public void parser(InputStream ins, AddressListener addressListener) throws Throwable {
		if (ins==null) throw new Exception("Can't give a null InputStream.");
		this.parser(new StreamSourceFromStreamStalled(ins),addressListener);
	}
	
	/** 
	 * Call with a String.
	 * @param data the String
	 * @param addressListener
	 * @throws Throwable
	 */
	synchronized public void parser(String data, AddressListener addressListener) throws Throwable {
		if (data==null) throw new Exception("Can't give a null String.");
		this.parser(new StreamSourceFromString(data),addressListener);
	}
	
	// == REDUCTIONS ===================================================================
	
	private void START() throws Throwable {
		while (ios.hasMore()) {
			REG_friendly = null;
			REG_address = null;
			REG_group = null;
			REG_busted = null;
			FLAG_GROUP = false;
			FLAG_GROUP_ENDED = false;
			ACCUMULATOR = new StringBuffer();
			OPEN();		
		}
	}
	
//	[OPEN]	
//		- WS, COMMA	- burn
//		- CHAR		- push, [ACCUMULATE], ^RETURN^
//		- OPENPAREN	- push, [GATHERCOMMENT]
//		- QUOTE		- push, [GATHERQUOTE], [ACCUMULATE], ^RETURN^
//		- LT		- [LTADDRESS], ^RETURN^	
//		- AT		- [SLURP]
//		- SEMICOLON	- if (true = $FLAG_GROUP) then 
//					pop->$ADDRESS, [SUBMIT], false->$FLAG_GROUP_ENDED, ^RETURN^
//				  else
//				  	error(Character not allowed in DN.)
//		- |SPECIAL|	- error(meaningless and unquoted special)
//		- !OTHER!	- error(character not allowed in open)
//		- EOF		- ^EXIT^
//
//		-> ($FLAG_GROUP_ENDED = true)	- false->$FLAG_GROUP_ENDED, ^RETURN^
	private void OPEN() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();
			switch(LexicalTool.getName(character)) {
			
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
			case SPECIAL_COMMA:	// burn 
				break;
				
			case CHAR: case CHAR_DNSCHAR_POUND: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR:
				ACCUMULATOR.append((char)character); 
				ACCUMULATE();
				return;
				
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
			
			case SPECIAL_QUOTE:				
				ACCUMULATOR.append((char)character); 
				GATHERQUOTE();
				ACCUMULATE();
				return;
				
			case SPECIAL_LT:
				LTADDRESS();
				return;
				
			case SPECIAL_SEMICOLON:
				if (FLAG_GROUP ==  true) {
					REG_address = pop(); SUBMIT(); FLAG_GROUP_ENDED = false;
				} else {
					error("Character not allowed in DN.");
				}
				return;
				
			case SPECIAL_CLOSEPAREN: case SPECIAL_COLON:
			case SPECIAL_GT: case SPECIAL_OPENBRACK: case SPECIAL_BACKSLASH: case SPECIAL_CLOSEBRACK:
				error("meaningless and unquoted special.");
				break;
				
			case SPECIAL_AT: 
				SLURP();
				break;
				
			default:
				error("character not allowed in OPEN.  character=" + character);
			break;			
				
			}	// end switch

			if (FLAG_GROUP_ENDED == true) {
				FLAG_GROUP_ENDED = false;
				return;
			}
		}	
	} // END OPEN()
	
//	[ACCUMULATE]
//		- CHAR		- push
//		- COMMA		- pop->$ADDRESS, submit, ^RETURN^
//		- QUOTE		- push, [GATHERQUOTE]
//		- OPENPAREN	- push, [GATHERCOMMENT]
//		- AT		- push, [NAKED_ADDRESS_DN_ONLY], ^RETURN^
//		- LT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^
//		- WS		- push(SPACE), [ACCUMULATE_WITH_WS], ^RETURN^
//      - BACKSLASH - push, [FRIENDLY_ESCAPE], [FRIENDLY], ^RETURN^
//		- COLON		- [GROUP], ^RETURN^
//		- |SPECIAL|	- error(unquoted special)
//		- !OTHER!	- error(character not allowed in open)
//		- EOF		- pop->$ADDRESS, submit, ^RETURN^
	private void ACCUMULATE() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
			
			case CHAR: case CHAR_DNSCHAR: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR_POUND:  case SPECIAL_CHAR_DNSCHAR_DOT:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_COMMA:
				REG_address = pop(); SUBMIT(); 
				return;
				
			case SPECIAL_QUOTE:				
				ACCUMULATOR.append((char)character); 
				GATHERQUOTE();
				break;
				
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
				
			case SPECIAL_AT:
				ACCUMULATOR.append((char)character);
				NAKED_ADDRESS_DN_ONLY();
				return;
				
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				ACCUMULATOR.append(' ');				
				ACCUMULATE_WITH_WS();
				return;
			
			case SPECIAL_LT: 
				REG_friendly = pop();
				LTADDRESS();
				return;
				
			case SPECIAL_COLON:
				GROUP(); 
				return;
						
			case SPECIAL_CLOSEPAREN: 
			case SPECIAL_SEMICOLON: case SPECIAL_GT: case SPECIAL_OPENBRACK: case SPECIAL_CLOSEBRACK:
				error("meaningless and unquoted special.");
				break;
				
			case SPECIAL_BACKSLASH:
				ACCUMULATOR.append((char)character); 
				FRIENDLY_ESCAPE();
				FRIENDLY();
				return;
				
			default:
				error("character not allowed in open.  character=" + character);
			break;						
				
			} // end switch
		} // end while
		
		// EOF
		REG_address = pop(); SUBMIT();
	} // end ACCUMULATE
	
	
	// [ACCUMULATE_WITH_WS]
	//	- CHAR		- push, [FRIENDLY], ^RETURN^
	//	- COLON		- [GROUP], ^RETURN^
	//	- COMMA		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	//	- OPENPAREN	- push, [GATHERCOMMENT]
	//	- AT		- error(bad address with unquoted whitespace)
	//	- WS		- push(SPACE)
	//	- GT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^
	//	- |SPECIAL|	- error(unquoted special)
	//	- !OTHER!	- error(character not allowed in open)
	//  - EOF		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	private void ACCUMULATE_WITH_WS() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {	
			
			case CHAR: case CHAR_DNSCHAR: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR_POUND: case SPECIAL_CHAR_DNSCHAR_DOT:
				ACCUMULATOR.append((char)character); 
				FRIENDLY();
				return;	
				
			case SPECIAL_COLON:
				GROUP(); 
				return;			
			
			case SPECIAL_COMMA:
				REG_address = pop(); SUBMIT(); 
				return;
				
			case SPECIAL_QUOTE:				
				ACCUMULATOR.append((char)character); 
				GATHERQUOTE();
				break;				
				
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
				
			case SPECIAL_AT:
				error("bad address with unquoted whitespace");
				break;
				
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				ACCUMULATOR.append(' ');
				break;
				
			case SPECIAL_LT: 
				REG_friendly = pop();
				LTADDRESS();
				return;
								
			case SPECIAL_CLOSEPAREN:
			case SPECIAL_SEMICOLON: case SPECIAL_GT: case SPECIAL_OPENBRACK: case SPECIAL_BACKSLASH: case SPECIAL_CLOSEBRACK:
				error("unquoted special.");
				break;
				
			default:
				error("character not allowed in open (friendly).  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		REG_address = pop(); SUBMIT(); 
		return;
	} // end ACCUMULATE_WITH_WS
	
	// [FRIENDLY]
	//  - CHAR		- push
	//	- AT		- push
	//  - BACKSLASH - push
	//	- COLON		- [GROUP], ^RETURN^
	//	- COMMA		- push
	//	- OPENPAREN	- push, [GATHERCOMMENT]
	//	- WS		- push(SPACE)
	//	- LT		- pop->$FRIENDLY, [LTADDRESS], ^RETURN^
	//	- |SPECIAL|	- error(unquoted special)
	//	- !OTHER!	- error(character not allowed in open)
	//	- EOF		- return - ignore it
	private void FRIENDLY() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {	
			
			case CHAR: case CHAR_DNSCHAR: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR_POUND: case SPECIAL_CHAR_DNSCHAR_DOT:
			case SPECIAL_AT: 
				ACCUMULATOR.append((char)character); 
				break;	
				
			case SPECIAL_BACKSLASH:
				ACCUMULATOR.append((char)character); 
				FRIENDLY_ESCAPE();
				break;	
				
			case SPECIAL_COLON:
				GROUP(); 
				return;			
			
			case SPECIAL_COMMA:
				ACCUMULATOR.append((char)character); 
				break;

			case SPECIAL_QUOTE:				
				ACCUMULATOR.append((char)character); 
				GATHERQUOTE();
				break;
				
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
				
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				ACCUMULATOR.append(' ');
				break;
				
			case SPECIAL_LT: 
				REG_friendly = pop();
				LTADDRESS();
				return;
								
			case SPECIAL_CLOSEPAREN: 
			case SPECIAL_SEMICOLON: case SPECIAL_GT: case SPECIAL_OPENBRACK: case SPECIAL_CLOSEBRACK:
				error("unquoted special.");
				break;
				
			default:
				error("character not allowed in unquoted friendly.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		return;  // Ignore it.
	} // end FRIENDLY
	
	
	//[FRIENDLY_ESCAPE]
	//	- !OTHER!	- push, ^RETURN^
	//	- EOF		- error(no address)
	private void FRIENDLY_ESCAPE() throws Throwable {
		if (ios.hasMore()) {
			ACCUMULATOR.append((char)ios.next());
		} else {
			// EOF
			error("comment left dangling.");
		}
	}
	
	// [LTADDRESS]
	// ->[LT_FRONT_ADDRESS_OPEN]
	// -> NULL->$FRIENDLY
	// ->^RETURN^		
	private void LTADDRESS() throws Throwable {
		LT_FRONT_ADDRESS_OPEN();
		REG_friendly = null;
	}

	// [LT_FRONT_ADDRESS_OPEN]
	// 	- WS		- burn
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- CHAR		- push, [FRONT_ADDRESS_NORMAL], ^RETURN^
	// 	- QUOTE		- push, [LT_FRONT_ADDRESS_QUOTED], ^RETURN^
	// 	- !OTHER!	- error(Not allowed character)
	//	- EOF		- error(no address)	
	private void LT_FRONT_ADDRESS_OPEN() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {	
			
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;			
			
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
				
			case CHAR: case CHAR_DNSCHAR: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR_POUND: case SPECIAL_CHAR_DNSCHAR_DOT:
				ACCUMULATOR.append((char)character); 
				LT_FRONT_ADDRESS_NORMAL();
				return;				
				
			case SPECIAL_QUOTE:				
				ACCUMULATOR.append((char)character); 
				LT_FRONT_ADDRESS_QUOTED();
				return;			
				
			default:
				error("character not allowed unquoted in address.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("no address found");
	}
	
	// [LT_FRONT_ADDRESS_NORMAL]
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- CHAR		- push
	// 	- AT		- push, [LT_ADDRESS_DN_ONLY], ^RETURN^
	//  - WS		- [LT_CLOSE_ONLY], ^RETURN^
	//  - GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^	
	// 	- !OTHER!	- error(Character not allowed in name.)
	// 	- EOF		- error(no address)
	private void LT_FRONT_ADDRESS_NORMAL() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
			
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
				
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				LT_CLOSE_ONLY();
				return;
				
			case SPECIAL_GT:
				REG_address = pop();
				SUBMIT();
				EXPECT_SEPERATOR_OR_EOF();
				return;						
				
			case CHAR: case CHAR_DNSCHAR: case CHAR_DNSCHAR_NUMERIC: case CHAR_DNSCHAR_POUND: case SPECIAL_CHAR_DNSCHAR_DOT:
				ACCUMULATOR.append((char)character); 
				break;				
				
			case SPECIAL_AT:				
				ACCUMULATOR.append((char)character); 
				LT_ADDRESS_DN_ONLY();
				return;			
				
			default:
				// Be forgiving.
				switch (character) {
				case ':':
				case ';':
					ACCUMULATOR.append((char)character); 
					break;
				default:
					error("character not allowed in name.  character=" + character);
					break;
				}

			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("no address found");
	}	
	
	//	[LT_CLOSE_ONLY]
	//		- GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	// 		- !OTHER!	- push, [BUSTEDBRACKETADDRESS], ^RETURN^
	//		- WS		- burn
	//		- EOF		- error(must close a non-DN address)
	private void LT_CLOSE_ONLY() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
						
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;
				
			case SPECIAL_GT:
				REG_address = pop();
				SUBMIT();
				EXPECT_SEPERATOR_OR_EOF();
				return;							
				
			default:
				ACCUMULATOR.append((char)character); 
				BUSTEDBRACKETADDRESS();
				return;
				
			} // end switch
		} // end while
		
		// EOF
		error("must close a non-DN address");
	}	
	
	// [LT_FRONT_ADDRESS_QUOTED]
	// -> [GATHERQUOTE]
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- AT		- push, [LT_ADDRESS_DN_ONLY], ^RETURN^
	// 	- !OTHER!	- error(broken quoted against @ in address)
	// 	- EOF		- error(no address)
	private void LT_FRONT_ADDRESS_QUOTED() throws Throwable {
		GATHERQUOTE();
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
			
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;
					
			case SPECIAL_AT:				
				ACCUMULATOR.append((char)character); 
				LT_ADDRESS_DN_ONLY();
				return;			
				
			default:
				error("broken quoted against @ in address.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("no address found");
	}	// end LT_FRONT_ADDRESS_QUOTED	
	
	// [LT_ADDRESS_DN_ONLY] 
	// -> [REQUIRE_DN]
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- DNSCHAR	- push
	//  - WS		- [SEEK_GT], pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	// 	- GT		- pop->$ADDRESS, [SUBMIT], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	// 	- !OTHER!	- error(Character not allowed in DN.)  A single quote will be pushed.
	// 	- EOF		- error(no address)
	private void LT_ADDRESS_DN_ONLY() throws Throwable {
		REQUIRE_DN();
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getDNSType(character)) {	
			
			case DNSCHAR:
				ACCUMULATOR.append((char)character); 
				break;	
				
			case BREAKING:
				switch(LexicalTool.getName(character)) {
				case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
					SEEK_GT();
					REG_address = pop();
					SUBMIT();
					EXPECT_SEPERATOR_OR_EOF();
					return;	
					
				case SPECIAL_GT:
					REG_address = pop();
					SUBMIT();
					// EXPECT_SEPERATOR_OR_EOF();  this should bubble up?
					return;			
				
				case SPECIAL_OPENPAREN:
					ACCUMULATOR.append((char)character); 
					GATHERCOMMENT();
					break;
					
				default:
					// Be forgiving.
					switch (character) {
					case ':':
					case ';':
						ACCUMULATOR.append((char)character); 
						break;
					default:
						error("character not allowed in DN.  character=" + character);
						break;
					}
				break;		
					
				} // end internal switch
				break;
							
			default:
				if (character=='\'') {
					ACCUMULATOR.append((char)character); 
				} else {
					error("character not allowed in DN.  character=" + character);
				}
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("address specification left dangling with closing '>'.");
	} // end LT_ADDRESS_DN_ONLY
	
	// [NAKED_ADDRESS_DN_ONLY] 
	// -> [REQUIRE_DN]
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- DNSCHAR or '	- push
	//  - SEMICOLON	- if (true = $FLAG_GROUP) then 
	//                  pop->$ADDRESS, [SUBMIT], true->FLAG_GROUP_ENDED, ^RETURN^
    //                else
  	//                  error(Character not allowed in DN.)
	// 	- COMMA		- pop->$ADDRESS, [SUBMIT],	^RETURN^
	//  - WS		- pop->$ADDRESS, [MAYBE_NOT_AN_ADDRES], ^RETURN^
	// 	- !OTHER!	- error(Character not allowed in DN.)
	// 	- EOF		-  pop->$ADDRESS, [SUBMIT], ^RETURN^
	//
	private void NAKED_ADDRESS_DN_ONLY() throws Throwable {
		REQUIRE_DN();
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getDNSType(character)) {	
			
			case DNSCHAR:
				ACCUMULATOR.append((char)character); 
				break;	
				
			case BREAKING:
				switch(LexicalTool.getName(character)) {

				case SPECIAL_OPENPAREN:
					ACCUMULATOR.append((char)character); 
					GATHERCOMMENT();
					break;
				
				case SPECIAL_COMMA:
					REG_address = pop();
					SUBMIT();
					return;	
					
				case SPECIAL_SEMICOLON:
					if (FLAG_GROUP ==  true) {
						REG_address = pop(); SUBMIT(); FLAG_GROUP_ENDED = true;
					} else {
						error("Character not allowed in DN.");
					}
					return;					
					
				case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
					MAYBE_NOT_AN_ADDRESS();
					return;	

				default:
					error("character not allowed in DN.  character=" + character);
					break;				
				} // end switch
				break;
					
			default:
				if (character=='\'') {
					ACCUMULATOR.append((char)character); 
				} else {
					error("character not allowed in DN.  character=" + character);
				}
			break;				
			} // end switch
		} // end while
		
		// EOF
		REG_address = pop();
		SUBMIT();
	} // end NAKED_ADDRESS_DN_ONLY
	
	//	[MAYBE_NOT_AN_ADDRESS]
	//		- OPENPAREN	- push, [GATHERCOMMENT], [FRIENDLY], ^RETURN^
	//		- WS 		- burn
	//		- SEMICOLON	- if (true = $FLAG_GROUP) then 
	//				     pop->$ADDRESS, [SUBMIT], true->FLAG_GROUP_ENDED, ^RETURN^
	//			          else
	//			  	     error(Group teminator when group not defined.)
	//		- LT		- pop->$FRIENDLY, [LTADDRESS], [EXPECT_SEPERATOR_OR_EOF], ^RETURN^
	//		- COMMA		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	//		- EOF		- pop->$ADDRESS, [SUBMIT], ^RETURN^
	//		- QUOTE		- push, [GATHERQUOTE], [FRIENDLY], ^RETURN^	
	//		- CHAR		- push, [FRIENDLY], ^RETURN^	
	//		- !OTHER!	- error(addresses not delimited.)
	private void MAYBE_NOT_AN_ADDRESS() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
			
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				FRIENDLY();
				break;
				
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;
			
			case SPECIAL_SEMICOLON:
				if (FLAG_GROUP ==  true) {
					REG_address = pop(); SUBMIT(); FLAG_GROUP_ENDED = true;
				} else {
					error("Character not allowed in DN.");
				}
				return;	
			
			case SPECIAL_LT:
				REG_friendly = pop();
				LTADDRESS();
				EXPECT_SEPERATOR_OR_EOF();
				return;
					
			case SPECIAL_COMMA:				
				REG_address = pop();
				SUBMIT();
				return;

			case SPECIAL_QUOTE:
				ACCUMULATOR.append((char)character); 	
				GATHERQUOTE();
				FRIENDLY();
				return;
				
			default:
				if (LexicalTool.get822Type(character)==CHAR) {
					ACCUMULATOR.append((char)character); 		
					FRIENDLY();
					return;
					
				} else {
					error("could not detirmine if a friendly name or address.  last address=" + REG_address);
				}
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		REG_address = pop();
		SUBMIT();
		
	} // end EXPECT_COMMA_OR_EOF
	
	//	[EXPECT_SEPERATOR_OR_EOF]
	//		- WS		- burn
	//		- OPENPAREN	- burn, [BURNCOMMENT]
	//		- AT		- [SLURP]
	//		- SEMICOLON	- if (true = $FLAG_GROUP) then 
	//				    	true->FLAG_GROUP_ENDED, ^RETURN^
	//			        else
	//			  	    	error(Group teminator when group not defined.)
	//		- COMMA		- ^RETURN^
	//		- EOF		- ^RETURN^
	//		- !OTHER!	- error(addresses not delimited.)
	private void EXPECT_SEPERATOR_OR_EOF() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			

			case SPECIAL_OPENPAREN:
				BURNCOMMENT();
				break;
			
			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;

			case SPECIAL_SEMICOLON:
				if (FLAG_GROUP ==  true) {
					FLAG_GROUP_ENDED = true;
				} else {
					error("group teminator when group not defined.");
				}
				return;	
				
			case SPECIAL_COMMA:				
				return;			
			
			case SPECIAL_AT:
				SLURP();
				break;
				
			default:
				error("addresses not delimited properly.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
	} // end SEEK_GT
	
	// [BUSTEDBRACKETADDRESS]
	//	- OPENPAREN	- push, [GATHERCOMMENT]	
	//	- WS		- push
	//	- GT		- pop->$BUSTED, [SUBMIT], ^RETURN^
	//	- !OTHER!	- push
	//	- EOF		- error(address not closed with a '>')	
	private void BUSTEDBRACKETADDRESS() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			

			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;

			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				ACCUMULATOR.append((char)character); 
				break;
				
			case SPECIAL_GT:		
				REG_busted = pop();
				SUBMIT();
				return;			
				
			default:
				ACCUMULATOR.append((char)character); 			
				break;
				
			} // end switch
		} // end while
		
		// EOF
		error("address not closed with a '>'");
	} // end SEEK_GT
	
	
	// [SEEK_GT]
	//  - OPENPAREN		- push, [GATHERCOMMENT]	
	// 	- WS		- burn
	// 	- GT		- ^RETURN^
	// 	- !OTHER!	- error(Character not allowed after whitespace, before '>')
	// 	- EOF		- error(address not closed with a '>')
	private void SEEK_GT() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			

			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;

			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;
					
			case SPECIAL_GT:				
				return;			
				
			default:
				error("character not allowed after whitespace, before '>'.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("address not closed with a '>'");
	} // end SEEK_GT
	
	// [REQUIRE_DN]
	//  - OPENPAREN	- push, [GATHERCOMMENT]	
	// 	- DNSCHAR	- push, ^RETURN^
	// 	- OPENPAREN	- push, [GATHERCOMMENT]
	// 	- !OTHER!	- error(bad domain name)
	// 	- EOF		- error(no address)
	private void REQUIRE_DN() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			if (LexicalTool.getDNSType(character) == DNSCHAR) {
				ACCUMULATOR.append((char)character); 
				return; 
			}
			switch(LexicalTool.getName(character)) {			
			
			case SPECIAL_OPENPAREN:
				ACCUMULATOR.append((char)character); 
				GATHERCOMMENT();
				break;

			case WS_SPACE: case WS_LF_CONTROL: case WS_CR_CONTROL: case WS_TAB_CONTROL:
				break;
					
//			case SPECIAL_GT:				
//				return;			
				
			default:
				error("domain name was fouled by the starting character.  character=" + character);
			break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("last address missing DN.");
	} // end REQUIRE_DN
	
	// [GATHERQUOTE]
	// 	- BACKSLASH	= burn, [ESCAPE]
	// 	- QUOTE		= push, ^RETURN^
	//  - !OTHER!   - push
	// 	- EOF		- error(quote left open)
	private void GATHERQUOTE() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			

			case SPECIAL_BACKSLASH:
				ESCAPE();
				break;

			case SPECIAL_QUOTE:
				ACCUMULATOR.append((char)character); 				
				return;	
				
			default:
				ACCUMULATOR.append((char)character); 
				break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("quote left open.");
	} // end GATHERQUOTE
	
	// [ESCAPE]
	// 	- ASCII 	= push, ^RETURN^
	// 	- EOF		- error(escape left open)
	private void ESCAPE() throws Throwable {
		if (ios.hasMore()) {
			ACCUMULATOR.append((char)ios.next()); 	
		} else {
			// EOF
			error("quote left open.");		
		}
	} // end GATHERQUOTE
	
	// [BURNCOMMENT]
	// 	- CLOSEPAREN	= ^RETURN^
	// 	- !OTHER!	= burn
	// 	- EOF		- error(comment left dangling.)
	private void BURNCOMMENT() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			if (LexicalTool.getName(character)==SPECIAL_CLOSEPAREN) return;
		} // end while
		
		// EOF
		error("comment left dangling.");
	} // end BURNCOMMENT
	
	// [SLURP]
	// 	- WS,COMMA,EOF	= ^RETURN^
	// 	- !OTHER!	= burn
	private void SLURP() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {
			case SPECIAL_COMMA:
			case WS_SPACE:
				return;
			}
		} // end while
		
		// EOF
	} // end BURNCOMMENT
	
	// [GATHERCOMMENT]
	// 	- CLOSEPAREN	= push, ^RETURN^
	// 	- BACKSLASH	= burn, [ESCAPE]
	// 	- !OTHER!	= push
	// 	- EOF		- error(comment left dangling.)
	private void GATHERCOMMENT() throws Throwable {
		int character;
		while (ios.hasMore()) {
			character = ios.next();	
			switch(LexicalTool.getName(character)) {			
			
			case SPECIAL_CLOSEPAREN:
				ACCUMULATOR.append((char)character); 
				return;

			case SPECIAL_BACKSLASH:
				ESCAPE();
				break;
				
			default:
				ACCUMULATOR.append((char)character); 
				break;		
				
			} // end switch
		} // end while
		
		// EOF
		error("comment left dangling.");
	} // end GATHERCOMMENT
	
	// [GROUP]
	// -> if($FLAG_COLON=true, error(cannot imbed groups)), 
	// -> pop->$GROUP, 
	// -> true->$FLAG_GROUP, 
	// -> [OPEN], 
	// -> NULL->$GROUP, 
	// ->false->$FLAG_GROUP,
	private void GROUP() throws Throwable {
		
		if (FLAG_GROUP == true) error("cannot imbed groups");
		REG_group = pop();
		FLAG_GROUP = true;
		OPEN();
		REG_group = null;
		FLAG_GROUP = false;		
	}
	
	// [SUBMIT]
	// -> submit($GROUP, $FRIENDLY, $ADDRESS)
	// -> NULL->$FRIENDLY
	// -> NULL->$ADDRESS
	private void SUBMIT() throws Throwable {
		addressTarget.push(REG_address, REG_friendly, REG_group, REG_busted);
		REG_friendly = null;
		REG_address = null;
		REG_busted = null;
	}
	
	// == TOOLS ===================================================================
	
	/**
	 * Throw an error with consistent formatting.
	 * @param text the text of the error.
	 * @throws Throwable
	 */
	private void error(String text) throws Throwable {
		if (ACCUMULATOR != null) {
			throw new Exception("Parsing: " + text + " acc=" + ACCUMULATOR.toString());
		} else {
			throw new Exception("Parsing: " + text);
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


