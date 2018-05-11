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
package things.data;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.thinger.SystemNamespace;

/**
 * Defines standard methods for encoding and decoding attributes as other data types.  All are static methods.
 * <p>
 * ||name=value|va\|lue||n\=ame=value||name=valu\\e|value|val\=ue<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 JAN 05
 * </pre> 
 */
public class AttributeCodec {
	
	// =========================================================================================
	// FIELDS

	
	// =========================================================================================
	// METHODS
	
    /**
     * Encode a collection of s to a string.
     * @param a An array of s.
     * @return the encoded string
     */
	public static String 	encode2String(NVImmutable...	a) throws ThingsException {
		//if (a==null) 
		//	throw new ThingsException("AttributeCodec failed encode2String().  NV is null.", ThingsException.SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED);
		return encode2String(Arrays.asList(a));
	}
	
    /**
     * Encode a single NV object.
     * @param a the NV
     * @return the encoded string
     */
	public static String 	encode2String(NVImmutable	a) throws ThingsException {
		StringWriter result = new StringWriter();
		try {
			if (a!=null) {
				encodeString(a.getName(),result);
				result.append(ThingsConstants.CODEC_EQUALITY);
				if (a.isMultivalue())
					encodeMultiString(a.getValuesAsList(),result);
				else 
					encodeString(a.getValue(),result);		
			}
		} catch (Throwable e) {
			throw new ThingsException("AttributeCodec failed encode2String().", ThingsException.SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED, e, SystemNamespace.ATTR_PLATFORM_MESSAGE, e.getMessage());
		}
		return result.toString();
	}
	
    /**
     * Encode a collection of String that represent an .
     * @param a An arracy of Strings.
     * @return the encoded string
     */
	public static String 	encode2String(String... a) throws ThingsException {
		StringWriter result = new StringWriter();
		try {
			if (a!=null) {
				int step = 0;
				while (true) {
					result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER); 
					result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER);
					if (step >= a.length) break;
					encodeString(a[step],result);
					result.append(ThingsConstants.CODEC_EQUALITY);
					step++;
					if (step >= a.length) throw new Exception("Odd name/value Strings.");
					encodeString(a[step],result);
					step++;			
				}
			}
		} catch (Throwable e) {
			throw new ThingsException("AttributeCodec failed encode2String().", ThingsException.SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED, e, SystemNamespace.ATTR_PLATFORM_MESSAGE, e.getMessage());
		}
		return result.toString();
	}
	
    /**
     * Encode a collection of s to a string.
     * @param c the collection
     * @return the encoded string
     */
	public static String 	encode2String(Collection<NVImmutable>	c) throws ThingsException {
		StringWriter result = new StringWriter();
		try {
			if (c!=null) {
				for ( NVImmutable n : c) {
					if (n!=null) {
						result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER); 
						result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER);							
						encodeString(n.getName(),result);
						result.append(ThingsConstants.CODEC_EQUALITY);
						if (n.isMultivalue()) 
							encodeMultiString(n.getValuesAsList(),result);			
						else 
							encodeString(n.getValue(),result);			
					}
				}
			}
		} catch (Throwable e) {
			throw new ThingsException("AttributeCodec failed encode2String().", ThingsException.SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED, e, SystemNamespace.ATTR_PLATFORM_MESSAGE, e.getMessage());
		}
		return result.toString();
	}
	
    /**
     * Encode a hashtable of String,String pairs.
     * @param c the map
     * @return the encoded string
     */
	public static String 	encode2String(Map<String,String>	c) throws ThingsException {
		StringWriter result = new StringWriter();
		try {
			if (c!=null) {
				for (String n : c.keySet()) {
					result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER); 
					result.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER);							
					encodeString(n,result);
					result.append(ThingsConstants.CODEC_EQUALITY);
					encodeString(c.get(n),result);
				}
			}
		} catch (Throwable e) {
			throw new ThingsException("AttributeCodec failed encode2String().", ThingsException.SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED, e, SystemNamespace.ATTR_PLATFORM_MESSAGE, e.getMessage());
		}
		return result.toString();
	}
	
    /**
     * Encode a collection of String that represent an .
     * @param a An arracy of Strings.
     * @param existing The existing map of attributes.   Passing null will create a new one.
     * @return the encoded string
     */	
	public static HashMap<String, String> encode2Map(HashMap<String, String> existing, String... a ) {
		String pop = null;
		HashMap<String, String> result;
		if (existing==null) result = new HashMap<String, String>();
		else result = existing;
		
		if (a != null) {
			for (String i : a) {
				if (pop == null) {
					pop = i;
				} else {
					if (i == null) {
						result.put(pop, pop);
					} else {
						result.put(pop, i);
					}
					pop = null;
				}
			}

			// dangling?
			if (pop != null)
				result.put(pop, pop);
		}
		
		return result;
	}
	
	// =========================================================================================
	// PRIVATE METHODS
	
	/**
	 * Encode a a string.  Null strings will write nothing.
	 * @param in The string to encode.
	 * @param out The target writer.
	 * @throws Exception Bad strings will cause exceptions.
	 */
	private static void encodeString(String in, Writer out) throws Exception {
		
		// validate
		if (in == null) return;
		
		// run it
		try {
			StringReader sin = new StringReader(in);
			int current = sin.read();
			while (current >=0) {
				switch(current) {
				case ThingsConstants.CODEC_ESCAPE_CHARACTER:
					out.append(ThingsConstants.CODEC_ESCAPE_CHARACTER);
					out.append(ThingsConstants.CODEC_ESCAPE_CHARACTER);
					break;
				case ThingsConstants.CODEC_SEPARATOR_CHARACTER:
					out.append(ThingsConstants.CODEC_ESCAPE_CHARACTER);
					out.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER);
					break;			
				case ThingsConstants.CODEC_EQUALITY:
					out.append(ThingsConstants.CODEC_ESCAPE_CHARACTER);
					out.append(ThingsConstants.CODEC_EQUALITY);
					break;					
				default:
					out.write(current);
				}
				current = sin.read();
			}
			out.flush();
		} catch (Throwable t) {
			// Ignore encoding issues and dump it.
		} 
	}
	
	/**
	 * Encode a multi-string.
	 * @param in The list of Strings.
	 * @param out The target writer.
	 * @throws Exception It'll propagate any encoding exceptions.
	 */
	private static void encodeMultiString(List<String> in, Writer out) throws Exception {
		
		// First item
		Iterator<String> items = in.iterator();
		if (!items.hasNext()) throw new Exception ("Empty multi-list submitted for encoding.");
		encodeString(items.next(),out);
		
		// Subsequent items
		while (items.hasNext()) {
			out.append(ThingsConstants.CODEC_SEPARATOR_CHARACTER);
			encodeString(items.next(),out);
		}
	}
}



