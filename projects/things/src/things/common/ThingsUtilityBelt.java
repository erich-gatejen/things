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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.TimeZone;

import things.thinger.SystemNamespace;

/**
 * A grab bag of static utilities.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 16 JUN 01
 * EPG - add snap strings 20 AUG 04
 * EPG - added load file to array - 9 JUL 07
 * EPG - make everything GMT - 10 AUG 08
 * </pre> 
 */
public class ThingsUtilityBelt  {

	// Time zone.  Hardcoded for now to GMT.
	private static TimeZone tz = TimeZone.getTimeZone("GMT-0000");
	
	/**
	 * A timestamp for matter for hour,minutes,seconds.  This is an expensive
	 * implementation.  No extra characters--HHMMSS
	 * It uses the current time.
	 * @return A string representation of the timestamp.
	 */
	public static String timestampFormatterHHMMSS() {
		return timestampFormatterHHMMSS(System.currentTimeMillis());
	}
	
	/**
	 * Parse a YYYYMMDD string into a timestamp (millis from epoch).
	 * @param text
	 * @return the time
	 * @throws Throwable if the format is null or bad.
	 */
	public static long timestampYYYYMMDD(String text) throws Throwable {
		if (text==null) ThingsException.softwareProblem("Cannot call timestampYYYYMMDD with a null string.");
		String normalized = text.trim(); 
		if (normalized.length()<8) throw new ThingsException("Truncated YYYYMMDD format", ThingsCodes.DATA_ERROR_BAD_DATE_FORMAT, ThingsNamespace.ATTR_DATA_VALUE, text);
		
		int year;
		int month;
		int day;
		try {
			year = Integer.parseInt(normalized.substring(0,4));
			month = Integer.parseInt(normalized.substring(4,6))-1;	// Month is zero based.
			if ((month < 0)||(month>11)) throw new PuntException("Not a valid month.");
			day = Integer.parseInt(normalized.substring(6,8));
			if ((day < 1)||(day>31)) throw new PuntException("Not a valid day.");
		} catch (Throwable t) {
			throw new ThingsException("Bad month in YYYYMMDDHHMMSS format", ThingsCodes.DATA_ERROR_BAD_DATE_FORMAT, t, ThingsNamespace.ATTR_DATA_VALUE, text);
		}
		GregorianCalendar calendar = new GregorianCalendar(year, month, day, 0, 0, 0);
		calendar.setTimeZone(tz);
		
		return calendar.getTimeInMillis();
	}
	
	/**
	 * Parse a YYYYMMDDHHMMSS string into a timestamp (millis from epoch).
	 * @param text
	 * @return the time
	 * @throws Throwable if the format is null or bad.
	 */
	public static long timestampYYYYMMDDHHMMSS(String text) throws Throwable {
		if (text==null) ThingsException.softwareProblem("Cannot call timestampYYYYMMDDHHMMSS with a null string.");
		String normalized = text.trim(); 
		if (normalized.length()!=14) throw new ThingsException("Truncated YYYYMMDDHHMMSS format", ThingsCodes.DATA_ERROR_BAD_DATE_FORMAT, ThingsNamespace.ATTR_DATA_VALUE, text);
		
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		try {
			year = Integer.parseInt(normalized.substring(0,4));
			month = Integer.parseInt(normalized.substring(4,6))-1;	// Month is zero based.
			if ((month < 0)||(month>11)) throw new PuntException("Not a valid month.");
			day = Integer.parseInt(normalized.substring(6,8));
			if ((day < 1)||(day>31)) throw new PuntException("Not a valid day.");
			hour = Integer.parseInt(normalized.substring(8,10));
			if ((hour < 0)||(hour>23)) throw new PuntException("Not a valid hour.");
			minute = Integer.parseInt(normalized.substring(10,12));
			if ((minute < 0)||(minute>59)) throw new PuntException("Not a valid minute.");
			second = Integer.parseInt(normalized.substring(12));
			if ((second < 0)||(second>59)) throw new PuntException("Not a valid second.");
		} catch (Throwable t) {
			throw new ThingsException("Bad month in YYYYMMDDHHMMSS format", ThingsCodes.DATA_ERROR_BAD_DATE_FORMAT, t, ThingsNamespace.ATTR_DATA_VALUE, text);
		}
	
		GregorianCalendar calendar = new GregorianCalendar(year, month, day, hour, minute, second);
		calendar.setTimeZone(tz);
		return calendar.getTimeInMillis();
	}
	
	
	/**
	 * A timestamp for matter for hour,minutes,seconds.  This is an expensive
	 * implementation.  No extra characters--HHMMSS
	 * @param time timestamp in milliseconds
	 * @return A string representation of the timestamp
	 */
	public static String timestampFormatterHHMMSS(long time) {

		int t;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time);
		calendar.setTimeZone(tz);
		StringBuffer buf = new StringBuffer();

		t = calendar.get(GregorianCalendar.HOUR_OF_DAY);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.MINUTE);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.SECOND);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		return buf.toString();
	}
	
	/**
	 * A timestamp for matter for day:hour,minutes,seconds.  This is an expensive
	 * implementation.  No extra characters--DDDHHMMSS
	 * @param time timestamp in milliseconds
	 * @return A string representation of the timestamp
	 */
	public static String timestampFormatterDDDHHMMSS(long time) {

		int t;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time);
		calendar.setTimeZone(tz);
		StringBuffer buf = new StringBuffer();

		t = calendar.get(GregorianCalendar.DAY_OF_YEAR);
		if (t < 10) {
			buf.append("00" + t);
		} else if (t < 100)  {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.HOUR_OF_DAY);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.MINUTE);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.SECOND);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		return buf.toString();
	}
	
	/**
	 * A timestamp for matter for year,day,hour,minutes,seconds.  This is an expensive
	 * implementation.  No extra characters--YYYYDDDHHMMSSmmmm.
	 * It uses the current time.
	 * @return A string representation of the timestamp.
	 */
	public static String timestampFormatterYYYYDDDHHMMSSmmmm() {
		return timestampFormatterYYYYDDDHHMMSSmmmm(System.currentTimeMillis());
	}
	
	/**
	 * A timestamp for matter for year,day,hour,minutes,seconds.  This is an expensive
	 * implementation.  No extra characters--YYYYDDDHHMMSSmmmm.
	 * @param time timestamp in milliseconds.
	 * @return A string representation of the timestamp.
	 */
	public static String timestampFormatterYYYYDDDHHMMSSmmmm(long time) {

		int t;
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(time);
		calendar.setTimeZone(tz);
		StringBuffer buf = new StringBuffer();

		buf.append(calendar.get(GregorianCalendar.YEAR));
		
		t = calendar.get(GregorianCalendar.DAY_OF_YEAR);
		if (t < 10) {
			buf.append("00" + t);
		} else if (t < 100)  {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		buf.append(':');
		t = calendar.get(GregorianCalendar.HOUR_OF_DAY);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.MINUTE);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.SECOND);
		if (t < 10) {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		t = calendar.get(GregorianCalendar.MILLISECOND);
		if (t < 10) {
			buf.append("000" + t);
		} else if (t < 100)  {
			buf.append("00" + t);
		} else if (t < 1000)  {
			buf.append("0" + t);
		} else {
			buf.append(t);
		}
		return buf.toString();
	}
	
	/**
	 * A timestamp for matter for year,day,hour,minutes,seconds.  This is an expensive
	 *                  01234567890123456                    012345678901234567
	 * implementation.  YYYYDDDHHMMSSmmmm (17 characters) or YYYYDDD:HHMMSSmmmm (18 characters).
	 * @param stamp timestamp in milliseconds.
	 * @return A string representation of the timestamp.
	 */
	public static long parseTimestampYYYYDDDHHMMSSmmmm(String stamp) throws Throwable {
		long result = -1;
		
		try {
			GregorianCalendar gcal = new GregorianCalendar();
			
			if (stamp.length()==17) {
				gcal.set(Calendar.YEAR, Integer.parseInt(stamp.substring(0,4)));
				gcal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(stamp.substring(4,7)));		
				gcal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stamp.substring(7,9)));			
				gcal.set(Calendar.MINUTE, Integer.parseInt(stamp.substring(9,11)));		
				gcal.set(Calendar.SECOND, Integer.parseInt(stamp.substring(11,13)));
				gcal.set(Calendar.MILLISECOND, Integer.parseInt(stamp.substring(13)));			
			} else if (stamp.length()==18) {
				gcal.set(Calendar.YEAR, Integer.parseInt(stamp.substring(0,4)));
				gcal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(stamp.substring(4,7)));		
				gcal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(stamp.substring(8,10)));			
				gcal.set(Calendar.MINUTE, Integer.parseInt(stamp.substring(10,12)));		
				gcal.set(Calendar.SECOND, Integer.parseInt(stamp.substring(12,14)));
				gcal.set(Calendar.MILLISECOND, Integer.parseInt(stamp.substring(14)));		
			} else {
				throw new ThingsException("Malformed date.  It must be YYYYDDDHHMMSSmmmm or YYYYDDD:HHMMSSmmmm.", ThingsException.DATA_ERROR_BAD_DATE_FORMAT,  ThingsNamespace.ATTR_DATA_ARGUMENT, stamp);
			}
			gcal.setTimeZone(tz);
			result = gcal.getTimeInMillis();
			
		} catch (ThingsException te) {
			throw te;
		} catch (Throwable t) {
			throw new ThingsException("Bad date.  it is not parsable.", ThingsException.DATA_ERROR_BAD_DATE_FORMAT,  t, ThingsNamespace.ATTR_DATA_ARGUMENT, stamp);		
		}
		
		return result;
	}
	
	/**
	 * Format a integer as a 4 digit hex.  It will not gracefully handle values over 64k.
	 * @param value the value to render as hex.
	 * @return A a 4 digit hex value.  
	 */
	public static String hexFormatter16bit(int value) {
		char[] result = new char[4];
		result[3] = HEX_VALUES[(value & 0x000F)];		
		result[2] = HEX_VALUES[(value & 0x00F0)/16];			// Don't bother with shifts.  Own me later if neccessary, but I suspect this is fast in the vm.
		result[1] = HEX_VALUES[(value & 0x0F00)/(16*16)];
		result[0] = HEX_VALUES[(value & 0xF000)/(16*16*16)];
		return new String(result);	
	}
	
	/**
	 * Format a byte array as 2 digit hex.
	 * @param value bytes to format.
	 * @return A string of hex values.
	 */
	public static String hexFormatter8bit(byte[] value) {
		StringBuffer result = new StringBuffer();
		for (byte item : value) {
			result.append(HEX_VALUES[(item & 0x0F)]);		
			result.append(HEX_VALUES[(item & 0xF0)/16]);		
		}
		return result.toString();	
	}
	
	/*
	 * Array of HEX values as characters.
	 */
	public static final char HEX_VALUES[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Load a file into an array.  There is a practical limit to this, so limit yourself to 1meg.  Even then, you might get
     * Out of Memory, so don't use willy-nilly.
     * @param path the String path to the file.
     * @return the array
     * @throws IOException
     */
    public static byte[] loadFileToArray(String path) throws IOException {
    	if (path==null) throw new IOException("Null path passed to loadFileToArray(String)");
    	return loadFileToArray(new File(path));
    }
	
    /**
     * Load a file into an array.  There is a practical limit to this, so limit yourself to 1meg.  Even then, you might get
     * Out of Memory, so don't use willy-nilly.
     * @param file
     * @return the array
     * @throws IOException
     */
    public static byte[] loadFileToArray(File file) throws IOException {
    	if (file==null) throw new IOException("Null file passed to loadFileToArray(File)");
    	
        // Setup 
    	FileInputStream ios = new FileInputStream(file);
        long length = file.length();
        int step = 0;
        int rover = 0;
        
        // Qualify.
        if (length > (1024*1024)) throw new IOException("Too big.  Must be under 1MB.  size =" + length);
  
        // Create result
        byte[] result = new byte[(int)length];
    
        // Read in the bytes
        while (step < result.length
               && (rover=ios.read(result, step, result.length-step)) >= 0) {
        	step += rover;
        }
    
        // Ensure all the bytes have been read in
        if (step < result.length) throw new IOException("Failed to read all.  expected=" + length + " actual=" + step);
    
        // Done!
        ios.close();
        return result;
    }
    
	/**
	 * Convert the binary name to the file name.
	 * @param name The binary name.
	 * @throws an Exception if it encounters disallowed character--slashes, CR, LF, Space, or TAB.
	 */
	public static String binaryNameToFileName(String name) throws Exception {
		int length = name.length();
		StringBuffer result = new StringBuffer(length);
		
		for (int rover = 0; rover < length ; rover++) {
			switch(name.charAt(rover)) {
			case '/':
			case '\\':
			case ThingsConstants.CR:
			case ThingsConstants.LF:	
			case '\t':
			case ' ':
				throw new Exception("Bad character in name.  character=" + name.charAt(rover));
				
			case '.':
				result.append('/');
				break;
				
			default:
				result.append(name.charAt(rover));
				break;			
			}
		}
		
		return result.toString();
	}
	
	// The current Integer for the nextInteger operation.
	static private int currentInteger = 0;
	
	/**
	 * Next integer starting with 1.  As long as this class is loaded, it'll give the next integer.  If it wraps, it'll throw an exception, but then troop along
	 * This is not universally safe, so use it for light-lifting stuff.
	 * @return the next integer
	 * @throws ArithmeticException if it overflows.
	 */
	public static synchronized int nextInteger() throws ArithmeticException {
		currentInteger++;
		if (currentInteger<=0) {
			currentInteger = 0;
			throw new ArithmeticException("nextInteger() overflow.  It has been reset.");
		}
		return currentInteger;
	}

	/**
	 * Make sure a file is deleted for sure.
	 * @param theFile The file to smack!
	 * @throws ThingsException if it wouldn't deleted after several attempts.
	 */
	public static void destroyFile(File theFile) throws ThingsException  {
	    try {
		    int tries = 3;
	        while( theFile.exists() && !theFile.delete() ) {
	              System.gc();
                  try
                  {
                    Thread.sleep(15);
                  } catch(InterruptedException ex) {}
                  tries--;
                  if (tries == 0) throw new Exception("Exceeded delete try.  File doesn't want to die.");
	        }
	    } catch (Exception se) {
	        throw new ThingsException("Delete failed.",ThingsException.FILESYSTEM_ERROR_FILE_WONT_DELETE,se,SystemNamespace.ATTR_PLATFORM_FS_PATH, theFile.getAbsolutePath(), SystemNamespace.ATTR_PLATFORM_MESSAGE,se.getMessage());
	    }
	}
	
	/**
	 * Snap a string into sized pieces.
	 * @param target the string to snap.
	 * @param firstRun the size in characters of the first run.
	 * @param followingRuns the size of all following runs.
	 * @return an ordered collection of snapped strings.  It may be empty, if the target is null or empty or either run size is less than 1.
	 */
	public static String[] snapStrings(String target, int firstRun, int followingRuns) {
		LinkedList<String> result = new LinkedList<String>();
		
		// Qualify
		if ((target!=null)&&(target.length()>0)&&(firstRun>0)&&(followingRuns>0)) {
		
			// Trivial case
			if (target.length() <= firstRun) {
				result.add(target);

			} else {
			
				// First case
				result.add(target.substring(0, firstRun));
				String rest = target.substring(firstRun).trim();
				while(rest.length()>0) {
					if (rest.length() <= followingRuns) {
						result.add(rest);
						break;
						
					} else {
						result.add(rest.substring(0, followingRuns));
						rest = rest.substring(followingRuns).trim();
					}
				}
			
			} // end if trivial case
		
		}
		
		// put in the array.
		String[] finalResult;
		if (result.size()==0) {
			finalResult = new String[0];
		} else {
			finalResult = new String[result.size()];
			result.toArray(finalResult);
		}
		return finalResult;	
	}
	
	/**
	 * A copier.  
	 * @param is input stream.
	 * @param out output stream.
	 * @param bufferSize The buffer size.
	 * @throws Exception
	 */
	public static void copy(InputStream is, OutputStream out, int bufferSize) throws Exception {		
		byte[] buffer = new byte[bufferSize];
		int size;
		while ((size = is.read(buffer)) > 0) {
			out.write(buffer, 0, size);
		}
	}
	
}