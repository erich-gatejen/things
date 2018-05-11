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
package things.common.commands;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.tools.StringArrayReader;
import things.data.NVImmutable;
import things.data.ThingsPropertyView;

/**
 * A fancy command line processor.  The rules are:  There are values, options, and entities.  
 * <p>
 * Entities are numbered by position and accessible from an ArrayList by number.
 * <p>
 * Values are not numbered, but are available in the entity map.  Values differ from entities in that they have an exposed (not escaped or quoted) 
 * = (equal) sign.  They are split into name/value pairs.  If an = precedes or suffixes the term, it'll assume it is a name only-the value
 * will be the same as the name.  VALUES ARE NOT CASE SENSITIVE and the names are stored as LOWER CASE.
 * <p>
 * Options are one or more character flags preceded by a single dash.  They are stored in their own map. 
 * <p>
 * Quotes can enclose terms.  Accent '`' (ASCII #96) is the escape.  Double accent will escape itself.
 * Carot (^) enclosed terms will be resolved against the property view.  Backslash is the escape.  Double backslash will escape itself.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 07
 * </pre> 
 */
public class CommandLineProcessor { 
	
	// =====================================================================================================================
	// == FIELDS
	
	/**
	 * This is the property character.
	 */
	public final static int PROPERTY_CHARACTER = '^';
	/**
	 * This is the escape character.
	 */
	public final static int ESCAPE_CHARACTER = '`';
	
	/**
	 * This is the option character.
	 */
	public final static int OPTION_CHARACTER = '-';
	
	/**
	 * This is the largest (by value) character recognized.  Anything else will be viewed as a NORMAL_CHARACTER during processing.
	 * This should not cause any problems for other character sets (as long as the functionally significant characters are the same--such as the carot).
	 */
	public final static int MAX_CHARACTER = 127;
	
	/**
	 * A NORMAL_CHARCTER.  This really doesn't effect output.
	 */
	public final static int NORMAL_CHARACTER = 126;
	
	// =====================================================================================================================
	// == METHODS
	
	/**
	 *  Process a command line.  It'll process any 7-bit ASCII ok.  It'll ignore all other characters.  You should avoid the first
	 *  character being a 0 byte (the value 0, not the character 0).  This is a static method that will create an engine as needed.
	 *  If you plan on doing this often, instantiate one yourself and use process() instead.
	 * @param target A Reader source.
	 * @param properties Properties for replacements.
	 * @return A CommandLine object
	 * @see things.common.commands.CommandLine
	 */
	public static CommandLine processStatic(Reader target, ThingsPropertyView properties) throws ThingsException {
		CommandLine result;
		try {
			CommandLineProcessor myProcessor = new CommandLineProcessor();
			result = myProcessor.run(target, properties);
		} catch (Throwable t) {
			throw new ThingsException("Error parsing command line due to property problem", ThingsException.THING_FAULT_COMMANDLINE_PROCESSOR_STARTUP, t);
		}
		return result;
	}
	
	/**
	 *  Process a command line.  It'll process any 7-bit ASCII ok.  It'll ignore all other characters.  You should avoid the first
	 *  character being a 0 byte (the value 0, not the character 0).  This is a static method that will create an engine as needed.
	 *  If you plan on doing this often, instantiate one yourself and use process() instead.
	 * @param target A Reader source.
	 * @param properties Properties for replacements.
	 * @return A CommandLine opbject
	 * @see things.common.commands.CommandLine
	 */
	public synchronized CommandLine process(Reader target, ThingsPropertyView properties) throws ThingsException {
		CommandLine result;
		try {
			result = run(target, properties);
		} catch (Throwable t) {
			throw new ThingsException("Error parsing command line due to property problem", ThingsException.THING_FAULT_COMMANDLINE_PROCESSOR_STARTUP, t);
		}
		return result;
	}
	
	/**
	 *  Process a command line.  It'll process any 7-bit ASCII ok.  It'll ignore all other characters.  You should avoid the first
	 *  character being a 0 byte (the value 0, not the character 0).  This is a static method that will create an engine as needed.
	 *  If you plan on doing this often, instantiate one yourself and use process() instead.
	 * @param target As an array of strings.
	 * @param properties Properties for replacements.
	 * @return A CommandLine opbject
	 * @see things.common.commands.CommandLine
	 */
	public synchronized CommandLine process(String[] target, ThingsPropertyView properties) throws ThingsException {
		CommandLine result;
		try {
			result = run(new StringArrayReader(target, true), properties);
		} catch (Throwable t) {
			throw new ThingsException("Error parsing command line due to property problem", ThingsException.THING_FAULT_COMMANDLINE_PROCESSOR_STARTUP, t);
		}
		return result;
	}
	
	// =====================================================================================================================
	// == INTERNAL ENGINE.  They are not reentrant, but since everything is private, that is A-OK.
	
	/**
	 * Character markers for the lookup.
	 */
	private final static int CHAR_TYPE_OTHER = 0;
	private final static int CHAR_TYPE_WHITESPACE = 1;
	private final static int CHAR_TYPE_EQUAL = 2;
	private final static int CHAR_TYPE_QUOTE = 3;
	private final static int CHAR_TYPE_ESCAPE = 4;
	private final static int CHAR_TYPE_PROPERTY = 5;
	private final static int CHAR_TYPE_OPTION = 6;
	

	/**
	 * Values processed as a map.  Names are normalized to lower case.
	 */
	private HashMap<String, NVImmutable> values;
	
	/**
	 * Options processed as a map.
	 */
	private boolean[] options;

	/**
	 * Entities in a list.
	 */
	private ArrayList<String> entities;
	
	/**
	 * Working data.
	 */
	private int workingCharacter;
	private Reader targetReader;
	private ThingsPropertyView targetProperties;
	
	/**
	 * Run entry.
	 * @param target A Reader source.
	 * @param properties Properties for replacements.
	 * @return a CommandLine object
	 * @see things.common.commands.CommandLine
	 */
	private CommandLine run(Reader target, ThingsPropertyView properties) throws ThingsException {
		
		values = new HashMap<String, NVImmutable>();
		options = new boolean[MAX_CHARACTER];
		entities = new ArrayList<String>();
		CommandLine result = new CommandLine(values, entities, options);
		
		StringBuffer 	acc;
		targetReader = target;
		targetProperties = properties;
		
		try {
			
			workingCharacter = targetReader.read();
			while (workingCharacter >= 0) {
			
				// START STATE
				switch (mapIt(workingCharacter)) {
							
				case CHAR_TYPE_OTHER:
					acc = new StringBuffer();
					acc.append((char)workingCharacter);
					accumulate(acc);
					break;
					
				case CHAR_TYPE_WHITESPACE:
					break;
					
				case CHAR_TYPE_EQUAL:
					token();
					break;
					
				case CHAR_TYPE_OPTION:
					option();
					break;
					
				case CHAR_TYPE_QUOTE:
					acc = new StringBuffer();
					quote(acc);
					accumulate(acc);
					break;					
					
				case CHAR_TYPE_ESCAPE:
					acc = new StringBuffer();
					acc.append(escape());
					break;
					
				case CHAR_TYPE_PROPERTY:
					acc = new StringBuffer();
					acc.append(property());
					accumulate(acc);
					break;
					
				}
				// DO NOT CHANGE BELOW THIS
				workingCharacter = targetReader.read();
			}
			
		} catch (ThingsException te) {
			throw new ThingsException("Error parsing command line due to property problem", ThingsException.DATA_ERROR_PARSING_ERROR, te);
		} catch (Throwable ee) {		
			throw new ThingsException("Error parsing command line due to spurious exception.", ThingsException.DATA_ERROR_PARSING_ERROR, ee);
		} 	
		return result;
	}
	
	/**
	 * Accumulate without knowing if it is a token or a nv.
	 * @param acc
	 */
	private void accumulate(StringBuffer acc) throws Throwable {
		workingCharacter = targetReader.read();
		while (workingCharacter >= 0) {
		
			// START STATE
			switch (mapIt(workingCharacter)) {
			
			case CHAR_TYPE_OPTION:
			case CHAR_TYPE_OTHER:
				acc.append((char)workingCharacter);
				break;
				
			case CHAR_TYPE_WHITESPACE:
				// It's just an entity
				String item = acc.toString();
				entities.add(item);
				return;
				
			case CHAR_TYPE_EQUAL:
				nv(acc.toString());
				return;
				
			case CHAR_TYPE_QUOTE:
				quote(acc);
				break;
				
			case CHAR_TYPE_ESCAPE:
				acc.append(escape());				
				
			case CHAR_TYPE_PROPERTY:
				acc.append(property());
				break;
			}
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
		// Leftover will be a entity.
		if (acc.length()>0) {
			String item = acc.toString();
			entities.add(item);
		}
	}
	
	/**
	 * This will be a set of character option flags.
	 */
	private void option() throws Throwable{

		workingCharacter = targetReader.read();
		while (workingCharacter >= 0) {
		
			// START STATE
			switch (mapIt(workingCharacter)) {
			
			case CHAR_TYPE_OTHER:
				// Flag it as set only if an allowable char.
				if (workingCharacter <= MAX_CHARACTER )
					options[workingCharacter] = true;
				break;
				
			case CHAR_TYPE_WHITESPACE:
				// We're done.
				return;
				
			case CHAR_TYPE_EQUAL:
			case CHAR_TYPE_OPTION:
			case CHAR_TYPE_QUOTE:
			case CHAR_TYPE_ESCAPE:
			case CHAR_TYPE_PROPERTY:
				// Ignore
				break;
			}
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
	}
		
	/**
	 * This will be a single token value.
	 */
	private void token() throws Throwable{
		StringBuffer acc = new StringBuffer();
		workingCharacter = targetReader.read();
		while (workingCharacter >= 0) {
		
			// START STATE
			switch (mapIt(workingCharacter)) {
			
			case CHAR_TYPE_OPTION:
			case CHAR_TYPE_OTHER:
				acc.append((char)workingCharacter);
				break;
				
			case CHAR_TYPE_WHITESPACE:
				// It's just a token
				if (acc.length() > 0) {
					String item = acc.toString();
					NVImmutable nvItem = new NVImmutable(item);
					values.put(item.toLowerCase(), nvItem);
				}
				return;
				
			case CHAR_TYPE_EQUAL:
				acc.append((char)workingCharacter);
				return;
				
			case CHAR_TYPE_QUOTE:
				quote(acc);
				break;
				
			case CHAR_TYPE_ESCAPE:
				acc.append(escape());				
				
			case CHAR_TYPE_PROPERTY:
				acc.append(property());
				break;
			}
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
		// Leftover will be a token.
		if (acc.length()>0) {
			String item = acc.toString();
			NVImmutable nvItem = new NVImmutable(item);
			values.put(item.toLowerCase(), nvItem);
		}
	}
	
	/**
	 * This will be an NV.
	 * @param name The name of the NV.  It will be normalized to lower case.
	 * @throws Throwable
	 */
	private void nv(String name) throws Throwable {
		StringBuffer valueBuffer = new StringBuffer();
		workingCharacter = targetReader.read();
		while (workingCharacter >= 0) {
		
			// START STATE
			switch (mapIt(workingCharacter)) {
			
			case CHAR_TYPE_OPTION:
			case CHAR_TYPE_OTHER:
				valueBuffer.append((char)workingCharacter);
				break;
				
			case CHAR_TYPE_WHITESPACE:
				if (valueBuffer.length() > 0) {
					String theValue = valueBuffer.toString();
					values.put(name.toLowerCase(), new NVImmutable(name, theValue));
				} else {
					// Just a token
					values.put(name.toLowerCase(), new NVImmutable(name));				
				}
				return;
				
			case CHAR_TYPE_EQUAL:
				valueBuffer.append((char)workingCharacter);
				return;
				
			case CHAR_TYPE_QUOTE:
				quote(valueBuffer);
				break;
				
			case CHAR_TYPE_ESCAPE:
				valueBuffer.append(escape());				
				
			case CHAR_TYPE_PROPERTY:
				valueBuffer.append(property());
				break;
			}
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
		// Leftover will be a token.
		if (valueBuffer.length()>0) {
			String theValue = valueBuffer.toString();
			values.put(name.toLowerCase(), new NVImmutable(name, theValue));
		} else {
			// A token
			values.put(name.toLowerCase(), new NVImmutable(name));		
		}
	}	
	
	/**
	 * Run a quote to completion.
	 * @param acc
	 * @throws Throwable
	 */
	private void quote(StringBuffer acc) throws Throwable {
		workingCharacter = targetReader.read();
		while ((workingCharacter >= 0)&&(workingCharacter != '"')) {
			acc.append(workingCharacter);
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
	}
	
	/**
	 * Enter a property resolution.  We'll try a real reduction here.
	 * @return the value of the property.
	 * @throws Throwable
	 */
	private String property() throws Throwable {
		String result = ThingsConstants.EMPTY_STRING;
		
		// Peal it off.
		StringBuffer nameBuffer = new StringBuffer();
		workingCharacter = targetReader.read();
		while ((workingCharacter >= 0)&&(workingCharacter!=PROPERTY_CHARACTER)) {
			nameBuffer.append((char)workingCharacter);
			// DO NOT CHANGE BELOW THIS
			workingCharacter = targetReader.read();
		}	
		
		// Look it up.
		if (nameBuffer.length() > 0) {
			String value = targetProperties.getProperty(nameBuffer.toString());
			if (value != null) result = value;
		} 
		return result;
	}
	
	/**
	 * Get the escaped character.
	 * @return the character
	 * @throws Throwable
	 */
	private char escape() throws Throwable {
		return (char)targetReader.read();
	}
	
	// ===================================================================================================
	// ENGINE DATA

	private static final int CLI_CHAR_MAP[] = {
		
		CHAR_TYPE_OTHER	,		// ^@	CTRL	0	0	0	NUL	null	
		CHAR_TYPE_OTHER	,		// ^A	CTRL	1	1	1	SOH	start of heading	
		CHAR_TYPE_OTHER	,		// ^B	CTRL	2	2	2	STX	start of text	
		CHAR_TYPE_OTHER	,		// ^C	CTRL	3	3	3	ETX	end of text	
		CHAR_TYPE_OTHER	,		// ^D	CTRL	4	4	4	EOT	end of transmission	
		CHAR_TYPE_OTHER	,		// ^E	CTRL	5	5	5	ENQ	enquiry	
		CHAR_TYPE_OTHER	,		// ^F	CTRL	6	6	6	ACK	acknowledge	
		CHAR_TYPE_OTHER	,		// ^G	CTRL	7	7	7	BEL	bell	
		CHAR_TYPE_OTHER	,		// ^H	CTRL	8	8	10	BS	backspace	
		CHAR_TYPE_WHITESPACE	,		// ^I	WS	9	9	11	TAB	horizontal tab	
		CHAR_TYPE_WHITESPACE	,		// ^J	LF	10	A	12	LF	new line	
		CHAR_TYPE_WHITESPACE	,		// ^K	CTRL	11	B	13	VT	vertical tab	
		CHAR_TYPE_OTHER	,		// ^L	CTRL	12	C	14	FF	new page	
		CHAR_TYPE_WHITESPACE	,		// ^M	CR	13	D	15	CR	carriage return	
		CHAR_TYPE_OTHER	,		// ^N	CTRL	14	E	16	SO	shift out	
		CHAR_TYPE_OTHER	,		// ^O	CTRL	15	F	17	SI	shift in	
		CHAR_TYPE_OTHER	,		// ^P	CTRL	16	10	20	DLE	data link escape	
		CHAR_TYPE_OTHER	,		// ^Q	CTRL	17	11	21	DC1	device CONTROL, 1	
		CHAR_TYPE_OTHER	,		// ^R	CTRL	18	12	22	DC2	device CONTROL, 2	
		CHAR_TYPE_OTHER	,		// ^S	CTRL	19	13	23	DC3	device CONTROL, 3	
		CHAR_TYPE_OTHER	,		// ^T	CTRL	20	14	24	DC4	device CONTROL, 4	
		CHAR_TYPE_OTHER	,		// ^U	CTRL	21	15	25	NAK	negative acknowledge	
		CHAR_TYPE_OTHER	,		// ^V	CTRL	22	16	26	SYN	synchronous idle	
		CHAR_TYPE_OTHER	,		// ^W	CTRL	23	17	27	ETB	end of trans. block	
		CHAR_TYPE_OTHER	,		// ^X	CTRL	24	18	30	CAN	cancel	
		CHAR_TYPE_OTHER	,		// ^Y	CTRL	25	19	31	EM	end of medium	
		CHAR_TYPE_OTHER	,		// ^Z	CTRL	26	1A	32	SUB	substitute	
		CHAR_TYPE_OTHER	,		// ^[	CTRL	27	1B	33	ESC	escape	
		CHAR_TYPE_OTHER	,		// ^	CTRL	28	1C	34	FS	file separator	
		CHAR_TYPE_OTHER	,		// ^]	CTRL	29	1D	35	GS	group separator	
		CHAR_TYPE_OTHER	,		// ^^	CTRL	30	1E	36	RS	record separator	
		CHAR_TYPE_OTHER	,		// ^_	CTRL	31	1F	37	US	unit separator	
		CHAR_TYPE_WHITESPACE	,		// 	WS	32	20	40	Space	space	
		CHAR_TYPE_OTHER	,		// 	CHAR	33	21	41	!		
		CHAR_TYPE_QUOTE	,		// 	CHAR	34	22	42	.	quote (double)	
		CHAR_TYPE_OTHER	,		// 	CHAR	35	23	43	#		
		CHAR_TYPE_OTHER	,		// 	CHAR	36	24	44	$		
		CHAR_TYPE_OTHER	,		// 	CHAR	37	25	45	%		
		CHAR_TYPE_OTHER	,		// 	CHAR	38	26	46	&		
		CHAR_TYPE_OTHER	,		// 	CHAR	39	27	47	'		
		CHAR_TYPE_OTHER	,		// 	CHAR	40	28	50	(		
		CHAR_TYPE_OTHER	,		// 	CHAR	41	29	51	)		
		CHAR_TYPE_OTHER	,		// 	CHAR	42	2A	52	*		
		CHAR_TYPE_OTHER	,		// 	CHAR	43	2B	53	+		
		CHAR_TYPE_OTHER	,		// 	CHAR	44	2C	54	,	comma	
		CHAR_TYPE_OPTION,		// 	CHAR	45	2D	55	-		
		CHAR_TYPE_OTHER	,		// 	CHAR	46	2E	56	.		
		CHAR_TYPE_OTHER	,		//	CHAR	47	2F	57	/	slash	
		CHAR_TYPE_OTHER	,		// 	CHAR	48	30	60	0		
		CHAR_TYPE_OTHER	,		// 	CHAR	49	31	61	1		
		CHAR_TYPE_OTHER	,		// 	CHAR	50	32	62	2		
		CHAR_TYPE_OTHER	,		// 	CHAR	51	33	63	3		
		CHAR_TYPE_OTHER	,		// 	CHAR	52	34	64	4		
		CHAR_TYPE_OTHER	,		// 	CHAR	53	35	65	5		
		CHAR_TYPE_OTHER	,		// 	CHAR	54	36	66	6		
		CHAR_TYPE_OTHER	,		// 	CHAR	55	37	67	7		
		CHAR_TYPE_OTHER	,		// 	CHAR	56	38	70	8		
		CHAR_TYPE_OTHER	,		// 	CHAR	57	39	71	9		
		CHAR_TYPE_OTHER	,		// 	COLON	58	3A	72	:	colon	
		CHAR_TYPE_OTHER	,		// 	CHAR	59	3B	73	;	semicolon	
		CHAR_TYPE_OTHER	,		// .	CHAR	60	3C	74	<		
		CHAR_TYPE_EQUAL	,		// 	CHAR	61	3D	75	=		
		CHAR_TYPE_OTHER	,		// .	CHAR	62	3E	76	>		
		CHAR_TYPE_OTHER	,		// 	CHAR	63	3F	77	?		
		CHAR_TYPE_OTHER	,		// .	CHAR	64	40	100	@		
		CHAR_TYPE_OTHER	,		// 	CHAR	65	41	101	A		
		CHAR_TYPE_OTHER	,		// 	CHAR	66	42	102	B		
		CHAR_TYPE_OTHER	,		// 	CHAR	67	43	103	C		
		CHAR_TYPE_OTHER	,		// 	CHAR	68	44	104	D		
		CHAR_TYPE_OTHER	,		// 	CHAR	69	45	105	E		
		CHAR_TYPE_OTHER	,		// 	CHAR	70	46	106	F		
		CHAR_TYPE_OTHER	,		// 	CHAR	71	47	107	G		
		CHAR_TYPE_OTHER	,		// 	CHAR	72	48	110	H		
		CHAR_TYPE_OTHER	,		// 	CHAR	73	49	111	I		
		CHAR_TYPE_OTHER	,		// 	CHAR	74	4A	112	J		
		CHAR_TYPE_OTHER	,		// 	CHAR	75	4B	113	K		
		CHAR_TYPE_OTHER	,		// 	CHAR	76	4C	114	L		
		CHAR_TYPE_OTHER	,		// 	CHAR	77	4D	115	M		
		CHAR_TYPE_OTHER	,		// 	CHAR	78	4E	116	N		
		CHAR_TYPE_OTHER	,		// 	CHAR	79	4F	117	O		
		CHAR_TYPE_OTHER	,		// 	CHAR	80	50	120	P		
		CHAR_TYPE_OTHER	,		// 	CHAR	81	51	121	Q		
		CHAR_TYPE_OTHER	,		// 	CHAR	82	52	122	R		
		CHAR_TYPE_OTHER	,		// 	CHAR	83	53	123	S		
		CHAR_TYPE_OTHER	,		// 	CHAR	84	54	124	T		
		CHAR_TYPE_OTHER	,		// 	CHAR	85	55	125	U		
		CHAR_TYPE_OTHER	,		// 	CHAR	86	56	126	V		
		CHAR_TYPE_OTHER	,		// 	CHAR	87	57	127	W		
		CHAR_TYPE_OTHER	,		// 	CHAR	88	58	130	X		
		CHAR_TYPE_OTHER	,		// 	CHAR	89	59	131	Y		
		CHAR_TYPE_OTHER	,		// 	CHAR	90	5A	132	Z		
		CHAR_TYPE_OTHER	,		// 	CHAR	91	5B	133	[		
		CHAR_TYPE_OTHER	,		// 	CHAR	92	5C	134	\		
		CHAR_TYPE_OTHER	,		// 	CHAR	93	5D	135	]		
		CHAR_TYPE_PROPERTY	,		// 	CHAR	94	5E	136	^	carot	
		CHAR_TYPE_OTHER	,		// 	CHAR	95	5F	137	_		
		CHAR_TYPE_ESCAPE	,		// 	CHAR	96	60	140	`		
		CHAR_TYPE_OTHER	,		// 	CHAR	97	61	141	a		
		CHAR_TYPE_OTHER	,		// 	CHAR	98	62	142	b		
		CHAR_TYPE_OTHER	,		// 	CHAR	99	63	143	c		
		CHAR_TYPE_OTHER	,		// 	CHAR	100	64	144	d		
		CHAR_TYPE_OTHER	,		// 	CHAR	101	65	145	e		
		CHAR_TYPE_OTHER	,		// 	CHAR	102	66	146	f		
		CHAR_TYPE_OTHER	,		// 	CHAR	103	67	147	g		
		CHAR_TYPE_OTHER	,		// 	CHAR	104	68	150	h		
		CHAR_TYPE_OTHER	,		// 	CHAR	105	69	151	i		
		CHAR_TYPE_OTHER	,		// 	CHAR	106	6A	152	j		
		CHAR_TYPE_OTHER	,		// 	CHAR	107	6B	153	k		
		CHAR_TYPE_OTHER	,		// 	CHAR	108	6C	154	l		
		CHAR_TYPE_OTHER	,		// 	CHAR	109	6D	155	m		
		CHAR_TYPE_OTHER	,		// 	CHAR	110	6E	156	n		
		CHAR_TYPE_OTHER	,		// 	CHAR	111	6F	157	o		
		CHAR_TYPE_OTHER	,		// 	CHAR	112	70	160	p		
		CHAR_TYPE_OTHER	,		// 	CHAR	113	71	161	q		
		CHAR_TYPE_OTHER	,		// 	CHAR	114	72	162	r		
		CHAR_TYPE_OTHER	,		// 	CHAR	115	73	163	s		
		CHAR_TYPE_OTHER	,		// 	CHAR	116	74	164	t		
		CHAR_TYPE_OTHER	,		// 	CHAR	117	75	165	u		
		CHAR_TYPE_OTHER	,		// 	CHAR	118	76	166	v		
		CHAR_TYPE_OTHER	,		// 	CHAR	119	77	167	w		
		CHAR_TYPE_OTHER	,		// 	CHAR	120	78	170	x		
		CHAR_TYPE_OTHER	,		// 	CHAR	121	79	171	y		
		CHAR_TYPE_OTHER	,		// 	CHAR	122	7A	172	z		
		CHAR_TYPE_OTHER	,		// 	CHAR	123	7B	173	{ 		
		CHAR_TYPE_OTHER	,		// 	CHAR	124	7C	174			
		CHAR_TYPE_OTHER	,		// 	CHAR	125	7D	175	}		
		CHAR_TYPE_OTHER	,		// 	CHAR	126	7E	176	~		
		CHAR_TYPE_OTHER			// 	CHAR	127	7F	177	DEL		

	};
	
	/**
	 * Map it.  Anything larger that the MAX_CHAR will be mapped as a NORMAL_CHARACTER.
	 * @param item
	 */
	private int mapIt(int item) {
		int shift = item;
		if (item > MAX_CHARACTER) {
			shift = NORMAL_CHARACTER;
		}
		return CLI_CHAR_MAP[shift];
	}
	
}