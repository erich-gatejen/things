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

import java.io.Reader;
import java.util.Vector;

import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.ThingsPropertyView;

/**
 * A standard command line tokenizer for single elements.  Quotes can enclose terms.  Backslash is the escape.  
 * Double backslash will escape itself.  Carot (^) enclosed terms will be resolved against the property view. 
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 MAY 06
 * </pre> 
 */
public class CommandLineSingleTokenizer {
	
	/**
	 *  Tokenize a command line of strings to a vector.  It'll process any 7-bit ASCII ok.  It'll ignore all other characters.  You should avoid the first
	 *  character being a 0 byte (the value 0, not the character 0).
	 * @param target A Reader source.
	 * @param properties Properties for replacements.
	 * @return a vector of command line tokens.
	 * @see things.common.StringPoster
	 */
	public static Vector<String> tokenize(Reader target, ThingsPropertyView properties) throws ThingsException {

		int workingCharacter;
		StringBuffer termBuffer = new StringBuffer();
		StringBuffer propertyBuffer = new StringBuffer();
		Vector<String> terms = new Vector<String>();
		int state = S_START;
		int stageState = S_START;
		String propertyValue;
		
		try {
			
			workingCharacter = target.read();
			while (workingCharacter >= 0) {

				// Toss any unallowed.
				if (workingCharacter <= MAX_CHAR) {
					
					// This is to keep state changes from 
					stageState = CLI_OPERATIONSTATE_MAP[workingCharacter][state][S_DATA];
					
					// Operation
					switch(CLI_OPERATIONSTATE_MAP[workingCharacter][state][O_DATA]) {
					case O_NONE:  	// none		
						break;
						
					case O_READ:  	// read		
						workingCharacter = target.read();
						break;
						
					case O_PUSHR:  	// push, read	
						termBuffer.append((char)workingCharacter);
						workingCharacter = target.read();
						break;
						
					case O_ACCR:  	// acc, read	
						propertyBuffer.append((char)workingCharacter);
						workingCharacter = target.read();
						break;
						
					case O_RPLR:  	// replace, read
						propertyValue = properties.getProperty(propertyBuffer.toString());
						if (propertyValue != null) termBuffer.append(propertyValue);
						propertyBuffer = new StringBuffer();
						workingCharacter = target.read();
						break;
						
					case O_CLOSE:  	// close		
						terms.add(termBuffer.toString());
						termBuffer = new StringBuffer();
						break;
						
					case O_CLOSR:  	// close, read	
						terms.add(termBuffer.toString());
						termBuffer = new StringBuffer();
						workingCharacter = target.read();
						break;
						
					case O_ERR:  	// ERROR	
						throw new ThingsException("Partial provided.", ThingsException.DATA_ERROR_PARSING_ERROR, 
								ThingsNamespace.ATTR_STATE_VALUE, Integer.toString(state), ThingsNamespace.ATTR_DATA_TARGET, termBuffer.toString());
					}
					
					// State
					state = stageState;
				}

			}
			
			// State cleanup
			switch(state) {
			case S_START:
			case S_OPEN:
			case S_DONE:
				// Nothing to do.
				break;
				
			case S_READ:
				// close if it has anothing.
				if (termBuffer.length() > 0) terms.add(termBuffer.toString());
				break;
						
			case S_ESC:
			case S_QESC:
				throw new ThingsException("Dangling escape.", ThingsException.DATA_ERROR_PARSING_ERROR, 
						ThingsNamespace.ATTR_STATE_VALUE, Integer.toString(state), ThingsNamespace.ATTR_DATA_TARGET, termBuffer.toString());
				
			case S_QUOT:
				throw new ThingsException("Unclosed quote.", ThingsException.DATA_ERROR_PARSING_ERROR, 
						ThingsNamespace.ATTR_STATE_VALUE, Integer.toString(state), ThingsNamespace.ATTR_DATA_TARGET, termBuffer.toString());
				
			case S_PROP:
			case S_QPRO:
				throw new ThingsException("Incomplete property.", ThingsException.DATA_ERROR_PARSING_ERROR, 
						ThingsNamespace.ATTR_STATE_VALUE, Integer.toString(state), ThingsNamespace.ATTR_DATA_TARGET, termBuffer.toString());
				
			case S_ERR:
				throw new ThingsException("General error.", ThingsException.DATA_ERROR_PARSING_ERROR, 
						ThingsNamespace.ATTR_STATE_VALUE, Integer.toString(state), ThingsNamespace.ATTR_DATA_TARGET, termBuffer.toString());
			}
			
		} catch (ThingsException te) {
			throw new ThingsException("Error parsing command line due to property problem", ThingsException.DATA_ERROR_PARSING_ERROR, te);
		} catch (Exception ee) {		
			throw new ThingsException("Error parsing command line due to spurious exception.", ThingsException.DATA_ERROR_PARSING_ERROR, ee);
		} 
		return terms;
	}
	
	// ===================================================================================================
	// ENGINE DATA

	private final static int MAX_CHAR = 127;
	
	/**
	 * CLI processing map.<p>  
<pre>
-- operations--------------		
none			O_NONE
read			O_READ
push, read		O_PUSHR
acc, read		O_ACCR
replace, read	O_RPLR
close			O_CLOSE
close, read		O_CLOSR
ERROR			O_ERR
		
-- states -----------------		
START		S_START
OPEN		S_OPEN
READ		S_READ
ESCAPING	S_ESC
QUOTE		S_QUOT
Q_ESC		S_QESC
PROPERY		S_PROP
Q_PROP		S_QPRO
DONE		S_DONE
ERROR		S_ERR
</pre>
	 */
	private final static int O_NONE = 0;
	private final static int O_READ = 1;
	private final static int O_PUSHR = 2;
	private final static int O_ACCR = 3;
	private final static int O_RPLR = 4;
	private final static int O_CLOSE = 5;
	private final static int O_CLOSR = 6;
	private final static int O_ERR = 7;
	
	private final static int S_START = 0;
	private final static int S_OPEN = 1;
	private final static int S_READ = 2;
	private final static int S_ESC = 3;
	private final static int S_QUOT = 4;
	private final static int S_QESC = 5;
	private final static int S_PROP = 6;
	private final static int S_QPRO = 7;
	private final static int S_DONE = 8;
	private final static int S_ERR = 9;
	
	private final static int O_DATA = 0;
	private final static int S_DATA = 1;
	
	private static final int CLI_OPERATIONSTATE_MAP[][][] = {
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^@	CTRL	0	0	0	NUL	null
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^A	CTRL	1	1	1	SOH	start of heading
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^B	CTRL	2	2	2	STX	start of text
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^C	CTRL	3	3	3	ETX	end of text
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^D	CTRL	4	4	4	EOT	end of transmission
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^E	CTRL	5	5	5	ENQ	enquiry
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^F	CTRL	6	6	6	ACK	acknowledge
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^G	CTRL	7	7	7	BEL	bell
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^H	CTRL	8	8	10	BS	backspace
		{ { O_READ, S_START}, { O_READ,  S_OPEN }, { O_CLOSR, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^I	WS	9	9	11	TAB	horizontal tab
		{ { O_NONE, S_DONE }, { O_NONE,  S_DONE }, { O_CLOSE, S_DONE }, { O_ERR,   S_ERR  }, { O_ERR,   S_ERR  }, { O_ERR,   S_ERR  }, { O_ERR , S_ERR  }, { O_ERR , S_ERR  } },	// ^J	LF	10	A	12	LF	new line
		{ { O_READ, S_START}, { O_READ,  S_OPEN }, { O_CLOSR, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^K	CTRL	11	B	13	VT	vertical tab
		{ { O_READ, S_START}, { O_READ,  S_OPEN }, { O_CLOSR, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^L	CTRL	12	C	14	FF	new page
		{ { O_NONE, S_DONE }, { O_NONE,  S_DONE }, { O_CLOSE, S_DONE }, { O_ERR,   S_ERR  }, { O_ERR,   S_ERR  }, { O_ERR,   S_ERR  }, { O_ERR , S_ERR  }, { O_ERR , S_ERR  } },	// ^M	CR	13	D	15	CR	carriage return
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^N	CTRL	14	E	16	SO	shift out
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^O	CTRL	15	F	17	SI	shift in
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^P	CTRL	16	10	20	DLE	data link escape
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^Q	CTRL	17	11	21	DC1	device CONTROL, 1
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^R	CTRL	18	12	22	DC2	device CONTROL, 2
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^S	CTRL	19	13	23	DC3	device CONTROL, 3
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^T	CTRL	20	14	24	DC4	device CONTROL, 4
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^U	CTRL	21	15	25	NAK	negative acknowledge
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^V	CTRL	22	16	26	SYN	synchronous idle
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^W	CTRL	23	17	27	ETB	end of trans. block
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^X	CTRL	24	18	30	CAN	cancel
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^Y	CTRL	25	19	31	EM	end of medium
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^Z	CTRL	26	1A	32	SUB	substitute
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^[	CTRL	27	1B	33	ESC	escape
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^	CTRL	28	1C	34	FS	file separator
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^]	CTRL	29	1D	35	GS	group separator
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^^	CTRL	30	1E	36	RS	record separator
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// ^_	CTRL	31	1F	37	US	unit separator
		{ { O_READ, S_START}, { O_READ,  S_OPEN }, { O_CLOSR, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	WS	32	20	40	Space	space
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	33	21	41	!	
		{ { O_READ, S_QUOT }, { O_READ,  S_QUOT }, { O_READ,  S_QUOT }, { O_PUSHR, S_READ }, { O_READ,  S_READ }, { O_PUSHR, S_QUOT }, { O_ERR , S_ERR  }, { O_ERR , S_ERR  } },	// 	CHAR	34	22	42	"	quote (double)
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	35	23	43	#	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	36	24	44	$	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	37	25	45	%	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	38	26	46	&	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	39	27	47	'	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	40	28	50	(	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	41	29	51	)	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	42	2A	52	*	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	43	2B	53	+	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	44	2C	54,	,	comma
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	45	2D	55	-	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	46	2E	56	.	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	//	CHAR	47	2F	57	/	slash
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	48	30	60	0	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	49	31	61	1	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	50	32	62	2	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	51	33	63	3	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	52	34	64	4	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	53	35	65	5	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	54	36	66	6	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	55	37	67	7	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	56	38	70	8	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	57	39	71	9	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	COLON	58	3A	72	:	colon
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	59	3B	73	;	semicolon
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// .	CHAR	60	3C	74	<	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	61	3D	75	=
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// .	CHAR	62	3E	76	>
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	63	3F	77	?
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// .	CHAR	64	40	100	@
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	65	41	101	A
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	66	42	102	B
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	67	43	103	C
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	68	44	104	D
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	69	45	105	E
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	70	46	106	F
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	71	47	107	G
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	72	48	110	H
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	73	49	111	I
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	74	4A	112	J
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	75	4B	113	K
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	76	4C	114	L
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	77	4D	115	M
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	78	4E	116	N
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	79	4F	117	O
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	80	50	120	P
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	81	51	121	Q
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	82	52	122	R
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	83	53	123	S
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	84	54	124	T
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	85	55	125	U
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	86	56	126	V
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	87	57	127	W
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	88	58	130	X
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	89	59	131	Y
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	90	5A	132	Z
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	91	5B	133	[
		{ { O_READ, S_ESC  }, { O_READ,  S_ESC  }, { O_READ,  S_ESC  }, { O_PUSHR, S_READ }, { O_READ,  S_QESC }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	92	5C	134	\
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	93	5D	135	]	
		{ { O_READ, S_PROP }, { O_READ,  S_PROP }, { O_READ,  S_PROP }, { O_PUSHR, S_READ }, { O_READ,  S_QPRO }, { O_PUSHR, S_QUOT }, { O_RPLR, S_READ }, { O_RPLR, S_QUOT } },	// 	CHAR	94	5E	136	^	carot
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	95	5F	137	_	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	96	60	140	`	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	97	61	141	a	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	98	62	142	b	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	99	63	143	c	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	100	64	144	d	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	101	65	145	e	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	102	66	146	f	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	103	67	147	g	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	104	68	150	h	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	105	69	151	i	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	106	6A	152	j	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	107	6B	153	k	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	108	6C	154	l	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	109	6D	155	m
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	110	6E	156	n
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	111	6F	157	o
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	112	70	160	p
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	113	71	161	q
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	114	72	162	r
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	115	73	163	s
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	116	74	164	t
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	117	75	165	u
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	118	76	166	v
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	119	77	167	w
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	120	78	170	x
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	121	79	171	y
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	122	7A	172	z
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	123	7B	173	{ 
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	124	7C	174	
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	125	7D	175	}
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	126	7E	176	~
		{ { O_NONE, S_OPEN }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_READ }, { O_PUSHR, S_QUOT }, { O_PUSHR, S_QUOT }, { O_ACCR, S_PROP }, { O_ACCR, S_QPRO } },	// 	CHAR	127	7F	177	DEL
	
	};
	
}