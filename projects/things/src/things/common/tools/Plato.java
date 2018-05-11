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


/**
 * Plato and truth.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 NOV 06
 * </pre> 
 */
public class Plato  {
	
	// =======================================================================================
	// DATA
	public final static String TRUE_STRING = "TRUE";
	public final static String FALSE_STRING = "FALSE";
	
	// =======================================================================================
	// TRUTH ENGINE
	
	/**
	 * Decide truth only if target is not null and is one or more characters (any value).  If it is null, the defaultValue will be returned as the truth.
	 * This is not case sensitive.  Leading and trailing whitespace is ignored.
	 * The following is true: 'TRUE', 'T', 'YES', 'y', integer numbers 1 or more.
	 * The following is false: 'FALSE', 'F', 'NO', 'n', number 0 or negative, floating point numbers.
	 * <br>
	 * It will never throw an exception, since any problem means it is false.
	 * @param target The statement to examine.  This will be treated like a 7-bit ascii string!
	 * @param defaultValue the default value.
	 * @return How true (in English) the statement is.
	 */
	public static boolean decideTruth(String target, boolean defaultValue) {
		
		// Qualify
		if ((target==null)||(target.length()<1)) return defaultValue;
		
		// parse it.
		int state = START;
		try {
			// Since we are ignoring double-byte, let the exception end extended character state mapping--they will cause
			// in array out of bounds.
			int length = target.length();
			for (int index = 0; index < length; index++) {
				// Map the character to the state table and resolve the new state.
				state = STATE_TABLE[CharacterMap[target.charAt(index)]][state];
			}
			
		} catch (Throwable t) {
			// Also, a null target will get caught here too.  null is always false.
			state = END_FALSE;
		}
		
		// Return the truth
		switch (state) {
		
		// true
		case NUMERIC:	
		case NUM_LEAD:
		case Y:
		case Y_LEAD:
		case T:
		case T_LEAD:		
		case TRUE:
			return true;
			
		// false
		case START:
		case ZERO:
		case YE:
		case TR:
		case TRU:
		case END_FALSE:
		default:
			return false;
		}
	}
	
	/**
	 * Decide truth.  This is not case sensitive.  Leading and trailing whitespace is ignored.
	 * The following is true: 'TRUE', 'T', 'YES', 'y', integer numbers 1 or more.
	 * The following is false: 'FALSE', 'F', 'NO', 'n', number 0 or negative, floating point numbers, and null.
	 * <br>
	 * It will never throw an exception, since any problem means it is false.
	 * @param target The statement to examine.  This will be treated like a 7-bit ascii string!
	 * @return How true (in English) the statement is.
	 */
	public static boolean decideTruth(String target) {
		return decideTruth(target, false);
	}

	// =======================================================================================
	// ENGINE DATA
	
	/**
	 * Internal parse states.
	 * Forget enums for this.  Too much pain.
	 */
	private final static int START = 0;
	private final static int ZERO = 1;
	private final static int NUMERIC = 2;	
	private final static int NUM_LEAD = 3;
	private final static int Y = 4;
	private final static int Y_LEAD = 5;
	private final static int YE = 6;
	private final static int T = 7;
	private final static int T_LEAD = 8;
	private final static int TR = 9;
	private final static int TRU = 10;
	private final static int TRUE = 11;
	private final static int END_FALSE = 12;
		
	/**
	 * Types of characters.
	 */
	private final static int C_POS = 0;
	private final static int C_ZERO = 1;
	private final static int C_Y = 2;
	private final static int C_E = 3;
	private final static int C_S = 4;
	private final static int C_T = 5;
	private final static int C_R = 6;
	private final static int C_U = 7;
	private final static int C_SPACE = 8;
	private final static int C_OTHER = 9;
	
	/** 
	 * State table.  [CHARACTER_TYPE][STATE_TRANSITION]
	 *
	 *<p><pre>
			START		ZERO		NUMERIC		NUM-LEAD	Y			Y-LEAD		YE			T			T-LEAD		TR			TRU			TRUE		END_FALSE
	POS		NUMERIC		NUMERIC		NUMERIC		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	ZERO	ZERO		ZERO		NUMERIC		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	Y		Y			END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	E		END_FALSE	END_FALSE	END_FALSE	END_FALSE	YE			END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	TRUE		END_FALSE	END_FALSE
	S		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	TRUE		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	T		T			END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	R		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	TR			END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE
	U		END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	TRU			END_FALSE	END_FALSE	END_FALSE
	SPACE	START		END_FALSE	NUM-LEAD	NUM-LEAD	Y-LEAD		Y-LEAD		END_FALSE	T-LEAD		T-LEAD		END_FALSE	END-FALSE	TRUE		END_FALSE
	OTHER	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE	END_FALSE 	END_FALSE	END_FALSE	END_FALSE
</pre>
	 * 
	 */
	private final static int STATE_TABLE[][] = {
		{	NUMERIC		,	NUMERIC		,	NUMERIC		,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	ZERO		,	ZERO		,	NUMERIC		,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	Y			,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	YE			,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	TRUE		,	END_FALSE	,	END_FALSE	},
		{	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	TRUE		,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	T			,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	TR			,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	TRU,		END_FALSE	,	END_FALSE	,	END_FALSE	},
		{	START		,	END_FALSE	,	NUM_LEAD	,	NUM_LEAD	,	Y_LEAD		,	Y_LEAD		,	END_FALSE	,	T_LEAD		,	T_LEAD		,	END_FALSE,	END_FALSE	,	TRUE		,	END_FALSE	},
		{	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE	,	END_FALSE,	END_FALSE	,	END_FALSE	,	END_FALSE	}
	};

	/**
	 * Character map.
	 */
	private static final int CharacterMap[] = {
		C_OTHER,	// ^@	CTRL	0	0	0	NUL	null
		C_OTHER,	// ^A	CTRL	1	1	1	SOH	start of heading
		C_OTHER,	// ^B	CTRL	2	2	2	STX	start of text
		C_OTHER,	// ^C	CTRL	3	3	3	ETX	end of text
		C_OTHER,	// ^D	CTRL	4	4	4	EOT	end of transmission
		C_OTHER,	// ^E	CTRL	5	5	5	ENQ	enquiry
		C_OTHER,	// ^F	CTRL	6	6	6	ACK	acknowledge
		C_OTHER,	// ^G	CTRL	7	7	7	BEL	bell
		C_OTHER,	// ^H	CTRL	8	8	10	BS	backspace
		C_SPACE,	// ^I	WS	9	9	11	TAB	horizontal tab
		C_SPACE,	// ^J	LF	10	A	12	LF	new line
		C_OTHER,	// ^K	CTRL	11	B	13	VT	vertical tab
		C_SPACE,	// ^L	CTRL	12	C	14	FF	new page
		C_SPACE,	// ^M	CR	13	D	15	CR	carriage return
		C_OTHER,	// ^N	CTRL	14	E	16	SO	shift out
		C_OTHER,	// ^O	CTRL	15	F	17	SI	shift in
		C_OTHER,	// ^P	CTRL	16	10	20	DLE	data link escape
		C_OTHER,	// ^Q	CTRL	17	11	21	DC1	device CONTROL, 1
		C_OTHER,	// ^R	CTRL	18	12	22	DC2	device CONTROL, 2
		C_OTHER,	// ^S	CTRL	19	13	23	DC3	device CONTROL, 3
		C_OTHER,	// ^T	CTRL	20	14	24	DC4	device CONTROL, 4
		C_OTHER,	// ^U	CTRL	21	15	25	NAK	negative acknowledge
		C_OTHER,	// ^V	CTRL	22	16	26	SYN	synchronous idle
		C_OTHER,	// ^W	CTRL	23	17	27	ETB	end of trans. block
		C_OTHER,	// ^X	CTRL	24	18	30	CAN	cancel
		C_OTHER,	// ^Y	CTRL	25	19	31	EM	end of medium
		C_OTHER,	// ^Z	CTRL	26	1A	32	SUB	substitute
		C_OTHER,	// ^[	CTRL	27	1B	33	ESC	escape
		C_OTHER,	// ^	CTRL	28	1C	34	FS	file separator
		C_OTHER,	// ^]	CTRL	29	1D	35	GS	group separator
		C_OTHER,	// ^^	CTRL	30	1E	36	RS	record separator
		C_OTHER,	// ^_	CTRL	31	1F	37	US	unit separator
		C_SPACE,	// 	WS		32	20	40	Space	space
		C_OTHER,	// 	CHAR	33	21	41	!	
		C_OTHER,	//  CHAR	34	22	42	quote (double)
		C_OTHER,	//  CHAR	35	23	43	#
		C_OTHER,	//  CHAR	36	24	44	$
		C_OTHER,	//  CHAR	37	25	45	%
		C_OTHER,	// 	CHAR	38	26	46	&	
		C_OTHER,	// 	CHAR	39	27	47	'	
		C_OTHER,	// 	CHAR	40	28	50	(	
		C_OTHER,	// 	CHAR	41	29	51	)	
		C_OTHER,	// 	CHAR	42	2A	52	*	
		C_OTHER,	// 	CHAR	43	2B	53	+	
		C_OTHER,	// 	CHAR	44	2C	54	,	comma
		C_OTHER,	// 	CHAR	45	2D	55	-	
		C_OTHER,	// 	CHAR	46	2E	56	.	
		C_OTHER,	//	CHAR	47	2F	57	/	slash
		C_ZERO,	// 	CHAR	48	30	60	0	
		C_POS,	// 	CHAR	49	31	61	1	
		C_POS,	// 	CHAR	50	32	62	2	
		C_POS,	// 	CHAR	51	33	63	3	
		C_POS,	// 	CHAR	52	34	64	4	
		C_POS,	// 	CHAR	53	35	65	5	
		C_POS,	// 	CHAR	54	36	66	6	
		C_POS,	// 	CHAR	55	37	67	7	
		C_POS,	// 	CHAR	56	38	70	8	
		C_POS,	// 	CHAR	57	39	71	9	
		C_OTHER,	// 	COLON	58	3A	72	:	colon
		C_OTHER,	// 	CHAR	59	3B	73	;	semicolon
		C_OTHER,	// .	CHAR	60	3C	74	<
		C_OTHER,	// 	CHAR	61	3D	75	=
		C_OTHER,	// .	CHAR	62	3E	76	>
		C_OTHER,	// 	CHAR	63	3F	77	?
		C_OTHER,	// .	CHAR	64	40	100	@
		C_OTHER,	// 	CHAR	65	41	101	A
		C_OTHER,	// 	CHAR	66	42	102	B
		C_OTHER,	// 	CHAR	67	43	103	C
		C_OTHER,	// 	CHAR	68	44	104	D
		C_E,	// 	CHAR	69	45	105	E
		C_OTHER,	// 	CHAR	70	46	106	F
		C_OTHER,	// 	CHAR	71	47	107	G
		C_OTHER,	// 	CHAR	72	48	110	H
		C_OTHER,	// 	CHAR	73	49	111	I
		C_OTHER,	// 	CHAR	74	4A	112	J
		C_OTHER,	// 	CHAR	75	4B	113	K
		C_OTHER,	// 	CHAR	76	4C	114	L
		C_OTHER,	// 	CHAR	77	4D	115	M
		C_OTHER,	// 	CHAR	78	4E	116	N
		C_OTHER,	// 	CHAR	79	4F	117	O
		C_OTHER,	// 	CHAR	80	50	120	P
		C_OTHER,	// 	CHAR	81	51	121	Q
		C_R,	// 	CHAR	82	52	122	R
		C_S,	// 	CHAR	83	53	123	S
		C_T,	// 	CHAR	84	54	124	T
		C_U,	// 	CHAR	85	55	125	U
		C_OTHER,	// 	CHAR	86	56	126	V
		C_OTHER,	// 	CHAR	87	57	127	W
		C_OTHER,	// 	CHAR	88	58	130	X
		C_Y,		// 	CHAR	89	59	131	Y
		C_OTHER,	// 	CHAR	90	5A	132	Z
		C_OTHER,	// 	CHAR	91	5B	133	[
		C_OTHER,	// 	CHAR	92	5C	134	\	
		C_OTHER,	// 	CHAR	93	5D	135	]	
		C_OTHER,	// 	CHAR	94	5E	136	^	carot
		C_OTHER,	// 	CHAR	95	5F	137	_	
		C_OTHER,	// 	CHAR	96	60	140	`	
		C_OTHER,	// 	CHAR	97	61	141	a	
		C_OTHER,	// 	CHAR	98	62	142	b	
		C_OTHER,	// 	CHAR	99	63	143	c	
		C_OTHER,	// 	CHAR	100	64	144	d	
		C_E,		// 	CHAR	101	65	145	e	
		C_OTHER,	// 	CHAR	102	66	146	f	
		C_OTHER,	// 	CHAR	103	67	147	g	
		C_OTHER,	// 	CHAR	104	68	150	h	
		C_OTHER,	// 	CHAR	105	69	151	i	
		C_OTHER,	// 	CHAR	106	6A	152	j	
		C_OTHER,	// 	CHAR	107	6B	153	k	
		C_OTHER,	// 	CHAR	108	6C	154	l
		C_OTHER,	// 	CHAR	109	6D	155	m
		C_OTHER,	// 	CHAR	110	6E	156	n
		C_OTHER,	// 	CHAR	111	6F	157	o
		C_OTHER,	// 	CHAR	112	70	160	p
		C_OTHER,	// 	CHAR	113	71	161	q
		C_R,	// 	CHAR	114	72	162	r
		C_S,	// 	CHAR	115	73	163	s
		C_T,	// 	CHAR	116	74	164	t
		C_U,	// 	CHAR	117	75	165	u
		C_OTHER,	// 	CHAR	118	76	166	v
		C_OTHER,	// 	CHAR	119	77	167	w
		C_OTHER,	// 	CHAR	120	78	170	x
		C_Y,	// 	CHAR	121	79	171	y
		C_OTHER,	// 	CHAR	122	7A	172	z
		C_OTHER,	// 	CHAR	123	7B	173	{ 
		C_OTHER,	// 	CHAR	124	7C	174						
		C_OTHER,	// 	CHAR	125	7D	175	}					
		C_OTHER,	// 	CHAR	126	7E	176	~					
		C_OTHER,	// 	CHAR	127	7F	177	DEL					
		C_OTHER,	//	HIGH	128								
		C_OTHER,	//	HIGH	129								
		C_OTHER,	//	HIGH	130								
		C_OTHER,	//	HIGH	131								
		C_OTHER,	//	HIGH	132								
		C_OTHER,	//	HIGH	133								
		C_OTHER,	//	HIGH	134								
		C_OTHER,	//	HIGH	135								
		C_OTHER,	//	HIGH	136								
		C_OTHER,	//	HIGH	137								
		C_OTHER,	//	HIGH	138								
		C_OTHER,	//	HIGH	139								
		C_OTHER,	//	HIGH	140								
		C_OTHER,	//	HIGH	141								
		C_OTHER,	//	HIGH	142								
		C_OTHER,	//	HIGH	143								
		C_OTHER,	//	HIGH	144								
		C_OTHER,	//	HIGH	145								
		C_OTHER,	//	HIGH	146								
		C_OTHER,	//	HIGH	147								
		C_OTHER,	//	HIGH	148								
		C_OTHER,	//	HIGH	149								
		C_OTHER,	//	HIGH	150								
		C_OTHER,	//	HIGH	151								
		C_OTHER,	//	HIGH	152								
		C_OTHER,	//	HIGH	153								
		C_OTHER,	//	HIGH	154								
		C_OTHER,	//	HIGH	155								
		C_OTHER,	//	HIGH	156								
		C_OTHER,	//	HIGH	157								
		C_OTHER,	//	HIGH	158								
		C_OTHER,	//	HIGH	159								
		C_OTHER,	//	HIGH	160								
		C_OTHER,	//	HIGH	161								
		C_OTHER,	//	HIGH	162								
		C_OTHER,	//	HIGH	163								
		C_OTHER,	//	HIGH	164								
		C_OTHER,	//	HIGH	165								
		C_OTHER,	//	HIGH	166								
		C_OTHER,	//	HIGH	167								
		C_OTHER,	//	HIGH	168								
		C_OTHER,	//	HIGH	169								
		C_OTHER,	//	HIGH	170								
		C_OTHER,	//	HIGH	171								
		C_OTHER,	//	HIGH	172								
		C_OTHER,	//	HIGH	173								
		C_OTHER,	//	HIGH	174								
		C_OTHER,	//	HIGH	175								
		C_OTHER,	//	HIGH	176								
		C_OTHER,	//	HIGH	177								
		C_OTHER,	//	HIGH	178								
		C_OTHER,	//	HIGH	179								
		C_OTHER,	//	HIGH	180								
		C_OTHER,	//	HIGH	181								
		C_OTHER,	//	HIGH	182								
		C_OTHER,	//	HIGH	183								
		C_OTHER,	//	HIGH	184								
		C_OTHER,	//	HIGH	185								
		C_OTHER,	//	HIGH	186								
		C_OTHER,	//	HIGH	187								
		C_OTHER,	//	HIGH	188								
		C_OTHER,	//	HIGH	189								
		C_OTHER,	//	HIGH	190								
		C_OTHER,	//	HIGH	191								
		C_OTHER,	//	HIGH	192								
		C_OTHER,	//	HIGH	193								
		C_OTHER,	//	HIGH	194								
		C_OTHER,	//	HIGH	195								
		C_OTHER,	//	HIGH	196								
		C_OTHER,	//	HIGH	197								
		C_OTHER,	//	HIGH	198								
		C_OTHER,	//	HIGH	199								
		C_OTHER,	//	HIGH	200								
		C_OTHER,	//	HIGH	201								
		C_OTHER,	//	HIGH	202								
		C_OTHER,	//	HIGH	203								
		C_OTHER,	//	HIGH	204								
		C_OTHER,	//	HIGH	205								
		C_OTHER,	//	HIGH	206								
		C_OTHER,	//	HIGH	207								
		C_OTHER,	//	HIGH	208								
		C_OTHER,	//	HIGH	209								
		C_OTHER,	//	HIGH	210								
		C_OTHER,	//	HIGH	211								
		C_OTHER,	//	HIGH	212								
		C_OTHER,	//	HIGH	213								
		C_OTHER,	//	HIGH	214								
		C_OTHER,	//	HIGH	215								
		C_OTHER,	//	HIGH	216								
		C_OTHER,	//	HIGH	217								
		C_OTHER,	//	HIGH	218								
		C_OTHER,	//	HIGH	219								
		C_OTHER,	//	HIGH	220								
		C_OTHER,	//	HIGH	221								
		C_OTHER,	//	HIGH	222								
		C_OTHER,	//	HIGH	223								
		C_OTHER,	//	HIGH	224								
		C_OTHER,	//	HIGH	225								
		C_OTHER,	//	HIGH	226								
		C_OTHER,	//	HIGH	227								
		C_OTHER,	//	HIGH	228								
		C_OTHER,	//	HIGH	229								
		C_OTHER,	//	HIGH	230								
		C_OTHER,	//	HIGH	231								
		C_OTHER,	//	HIGH	232								
		C_OTHER,	//	HIGH	233								
		C_OTHER,	//	HIGH	234								
		C_OTHER,	//	HIGH	235								
		C_OTHER,	//	HIGH	236								
		C_OTHER,	//	HIGH	237								
		C_OTHER,	//	HIGH	238								
		C_OTHER,	//	HIGH	239								
		C_OTHER,	//	HIGH	240								
		C_OTHER,	//	HIGH	241								
		C_OTHER,	//	HIGH	242								
		C_OTHER,	//	HIGH	243								
		C_OTHER,	//	HIGH	244								
		C_OTHER,	//	HIGH	245								
		C_OTHER,	//	HIGH	246								
		C_OTHER,	//	HIGH	247								
		C_OTHER,	//	HIGH	248								
		C_OTHER,	//	HIGH	249								
		C_OTHER,	//	HIGH	250								
		C_OTHER,	//	HIGH	251								
		C_OTHER,	//	HIGH	252								
		C_OTHER,	//	HIGH	253								
		C_OTHER,	//	HIGH	254								
		C_OTHER		//	HIGH	255								
	};
	
	
	
	
	
	
	
	// =======================================================================================
	// STATIC METHODS
	


}
