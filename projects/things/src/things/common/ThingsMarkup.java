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
package things.common;

/**
 * Markup for Strings.  String printers and processors may support this markup.<br>
 * P = New Paragraph  It will be two CR/LF pairs.<br>
 * BR = New Line.  It will be a CR/LF pair.<br>
 * TAB = Make tab spacing.  Every character counts as tab spacing, unless it is part of markup.<br>
 * $var = Variable replacement for named 'var'.  If not present, it should be left blank.<br>
 * <p>
 * XML escaping can be used.  If it is not well formed, it will emit the whole string as a literal.  If it is well formed, but the name isn't recognized, it will not be emitted at all.<br>
 * &amp;lt	<br>
 * &amp;gt	<br>
 * &amp;amp	<br>
 * &amp;apos <br>
 * &amp;quot <br>
 * <p>
 * You can pass information to the formatter with the meta tag.  It should look like this:<br>
 * &lt;meta.name=variable&gt;<br>
 * The following names are supported:<br>
 * tab = the tab length.  The default is 8.<br>
 * <p>
 * The tags will be ignored if not well formed or supported.<br> 
 * &lt;P&gt; = Valid and would cause a new paragraph.<br>
 * &lt;P&lt; = This would break on the second &lt; and would emit the string as a literal.<br>
 * &lt;FAKE_TAG&lt; = This is not a tag, so it will be emitted as a literal.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 MAR 05
 * </pre> 
 */
public interface ThingsMarkup {

	/**
	 * 	Markup Strings
	 */
	public final static String NEW_LINE = "br";
	public final static String NEW_PARAGRAPH = "p";
	public final static char VARIABLE = '$';
	public final static String TAB = "tab";
	public final static String META = "meta";
	
	public final static String META_TAB = "tab";
	
	/**
	 * 	Markup Characters
	 */
	public final static char  	OPEN_TAG	= '<';
	public final static char  	CLOSE_TAG	= '>';
	public final static char	ESCAPE 		= '&';
	public final static char	COMPELTE_ESCAPE 		= ';';
	public final static char	META_NAME_SPLIT 		= '.';
	public final static char	META_VALUE_SPLIT 		= '=';
	
	/**
	 * 	Escaped
	 */	
	public final static String 	ESCAPE_LT	=	"lt";
	public final static String 	ESCAPE_GT	=	"gt";
	public final static String 	ESCAPE_AMP	= 	"amp";
	public final static String 	ESCAPE_APOS =   "apos";
	public final static String 	ESCAPE_QUOT	=	"qout";

	/**
	 * 	DEFAULTS
	 */
	public final static int		DEFAULT_TAB_LENGTH = 8;
	
	/**
	 * 	EMITTABLE
	 */	
	public final static char EMIT_SPACE = ' ';
	public final static String EMIT_NEW_LINE = ThingsConstants.CRLF;
	public final static String EMIT_NEW_PARAGRAPH  = ThingsConstants.CRLF + ThingsConstants.CRLF;	
	public final static char EMIT_ESCAPE_LT		=	'<';
	public final static char EMIT_ESCAPE_GT		=	'>';
	public final static char EMIT_ESCAPE_AMP	= 	'&';
	public final static char EMIT_ESCAPE_APOS 	=   '\'';
	public final static char EMIT_ESCAPE_QUOT	=	'"';
	
}