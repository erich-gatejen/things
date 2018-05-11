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
package test.things;

import java.util.Collection;

import things.common.StringPoster;
import things.common.ThingsException;
import things.data.AttributeCodec;
import things.data.Entry;
import things.data.NVImmutable;
import things.thinger.SystemException;
import things.thinger.io.Logger;

/**
 * Simple implementation that uses a string poster.  This does not test logging!  It just allows tests to log.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 5 JUN 06
 * </pre> 
 */
public class STUB_Logger implements Logger {
	
	// ======================================================================================================
	// == DATA
	private StringPoster poster;
	
	// ======================================================================================================
	// == METHODS
	
	/**
	 * Constructor.
	 * @param poster Where to post all the stuff.
	 */
	public STUB_Logger(StringPoster poster) {
		this.poster = poster;
	}
	
	
	// ======================================================================================================
	// == IMPLEMENTATION METHODS
	
	/**
	 * Initialized the logger.  This will be done by the constructing system, typically the System or Kernel, so most users should
	 * not call this directly.
	 * @param loggerType The type of logger this should be.   This is more a request than a demand.
	 * @throws things.thinger.SystemException
	 */
	public void init(TYPE loggerType) throws SystemException {
		// NOP
	}

	
	/**
	 * This will set the default level of StringPoster posted entries.  The default starts as DATA.  That means, all posted strings will be
	 * treated as DATA level.
	 * @param newLevel the new default level.
	 * @throws things.thinger.SystemException
	 */
	public void setPostLevel(LEVEL	newLevel) {
		// NOP
	}
	
	/**
	 * This will set the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @param newLevel the new level.
	 */
	public void setLevel(LEVEL	newLevel) {
		// NOP
	}
	
	/**
	 * This will get the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @return the level.
	 */
	public LEVEL getLevel() {
		return LEVEL.DEBUG;
	}
	
	/**
	 * Tell the underlying system to flush any entries.
	 */
	public void flush() {
		poster.flush();
	}
	
	/**
	 * Post an Entry.
	 * @param e The entry.  
	 * @throws things.thinger.SystemException
	 * @see things.data.Entry
	 */
	public void post(Entry e) throws SystemException {
		poster.postit(e.toString());
	}
	
	/**
	 * Post as a message.
	 * @param message String to post
	 */
	public void post(String message) throws ThingsException {
		poster.post(message);	
	}
	
	/**
	 * Post as a message.  Best effort.  Ignore errors.
	 * @param message String to post
	 */
	public void postit(String message) {
		poster.postit(message);	
	}
	
    	/**
	 * Log an exception.  The implementation should try to deal with the ThingsException features.
	 * @param tr The Exception.  
	 * @throws things.thinger.SystemException
	 */
	public void exception(Throwable tr) throws SystemException {
		 error(tr.getMessage());
	}	
	
    
	/**
	 * Log a trivial error entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg) throws SystemException {
		poster.postit(msg);
	}

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg, int numeric) throws SystemException {
		poster.postit(numeric + " : " + msg);
	}
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void error(String msg, int numeric, NVImmutable... attributes) throws SystemException {
		poster.postit(numeric + " : " + msg);
		try {
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
	}
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes C of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void error(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException {
		poster.postit(numeric + " : " + msg);
		try {
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
	}
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 * @see things.data.NV
	 */
	public void error(String msg, int numeric, String... attributes) throws SystemException {
		poster.postit(numeric + " : " + msg);
		try {
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
	}
	
	/**
	 * Log a trivial warning entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg) throws SystemException {
		error(msg);
	}

	/**
	 * Log a trivial warning entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric) throws SystemException {
		error(msg, numeric);
	}
	

	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric, String... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void warning(String msg, int numeric, NVImmutable... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void warning(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	//
	/**
	 * Log a trivial information entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg) throws SystemException {
		error(msg);
	}

	/**
	 * Log a trivial information entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric) throws SystemException {
		error(msg, numeric);
	}
	
	/**
	 * Log a complex info entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric, String... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex information entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void info(String msg, int numeric, NVImmutable... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex information entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void info(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a trivial debug entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg) throws SystemException {
		error(msg);
	}

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg, int numeric) throws SystemException {
		error(msg, numeric);
	}
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 * @see things.data.NV
	 */
	public void debug(String msg, int numeric, String... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void debug(String msg, int numeric, NVImmutable... attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void debug(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException {
		error(msg, numeric, attributes);
	}
	
	/**
	 * Shout a log entry.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param theLevel The level of the message.
	 */
	public void shout(String msg, LEVEL theLevel) {
		poster.postit(msg);
	}
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @see things.data.NV
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, String... attributes) {
		try {
			poster.postit(numeric + " : " + msg);
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
	}
	
	/**
	 * Shout a log entry with numerics.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @see things.data.NV
	 */
	public void shout(String msg, int numeric, LEVEL theLevel) {
		poster.postit(numeric + " : " + msg);
	}
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Sequence of NVs representing attributes.
	 * @see things.data.NVImmutable
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, NVImmutable... attributes) {
		try {
			poster.postit(numeric + " : " + msg);
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
	}
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Sequence of NVs representing attributes.
	 * @see things.data.NVImmutable
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, Collection<NVImmutable> attributes) {
		try {
			poster.postit(numeric + " : " + msg);
			poster.postit(AttributeCodec.encode2String(attributes));
		} catch (ThingsException te) {
			poster.postit("Failed posting: " + te.toStringComplex());
		}
		
	}
	
	/**
	 * Turn debugging on.  Logs with debug level priority will be passed.
	 */
	public void debuggingOn() {
		// NOP
	}
	
	/**
	 * Turn debugging off.  Logs with debug level priority will not be passed.
	 */
	public void debuggingOff() {
		// NOP
	}
	
	/**
	 * Get the current debugging state.
	 * @return debugging state
	 */
	public boolean debuggingState() {
		return true;
	}
	
}
	