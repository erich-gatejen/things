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

import java.io.PrintWriter;
import java.util.Collection;

import things.common.ThingsCodes;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsUtilityBelt;
import things.data.AttributeCodec;
import things.data.Data;
import things.data.Entry;
import things.data.NVImmutable;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.kernel.ResourceInterface.State;

/**
 * Command logger.  It is a wrapper around the simple Thinger logger.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 06
 * </pre> 
 */
public class CommandLogger implements Logger {
	
	// ==========================================================================================
	// DATA
	private State			state = State.NEW;
	
	// whichever isn't null will be used.
	private PrintWriter 	wout;
	private Logger			logger;
	
	private String			prefixId;
	private Logger.LEVEL	currentLevel = Logger.LEVEL.TOP;
	private Logger.LEVEL	previousLevel = Logger.LEVEL.TOP;

	private Logger.LEVEL	defaultPostLevel = INITIAL_DEFAULT_POST_LEVEL;
	
	// ==========================================================================================
	// STATIC CONFIGURATION
	private final static Logger.LEVEL	INITIAL_DEFAULT_POST_LEVEL = Logger.LEVEL.DATA;		
		
	// ==========================================================================================
	// UNIQUE METHODS
	
    /**
     * Initialize the LoggerWriter with a Writer and a prefix ID.  The prefix ID will be overridden by a conduit ID, if a PushDrain
     * tries to initialize this.<br>
     * You cannot call this more than once.
     * <p>
     * @param out A writer.  The implementation doesn't care what kind.  If the Logger type is set as reliable, then every write will be flushed.
     * @param tag A tag for the line.
     * @param level The logging level.  Note that the debug methods will override any value passed here.
     * @throws things.thinger.SystemException
     */   
    public void init(PrintWriter		out, String		tag,  Logger.LEVEL		level) throws SystemException {
    	if (wout != null) SystemException.softwareProblem("CommandLogger.init() cannot be called twice for the same object.");
    	if (tag == null)  SystemException.softwareProblem("CommandLogger.init() cannot be called with a null tag.");
    	wout = out;
    	prefixId = tag;
    	currentLevel = level;
    	previousLevel = level;
    	state = State.RUNNING;
    }
    
    /**
     * Initialize the LoggerWriter with a Writer and a prefix ID.  The prefix ID will be overridden by a conduit ID, if a PushDrain
     * tries to initialize this.<br>
     * You cannot call this more than once.
     * <p>
     * @param logger A logger where to post.  The implementation doesn't care what kind.  If the Logger type is set as reliable, then every write will be flushed.
     * @param tag A tag for the line.
     * @param level The logging level.  Note that the debug methods will override any value passed here.
     * @throws things.thinger.SystemException
     */   
    public void init(Logger	logger, String		tag,  Logger.LEVEL		level) throws SystemException {
    	if (logger != null) SystemException.softwareProblem("CommandLogger.init() cannot be called twice for the same object.");
    	if (tag == null)  SystemException.softwareProblem("CommandLogger.init() cannot be called with a null tag.");
    	this.logger = logger;
    	prefixId = tag;
    	currentLevel = level;
    	previousLevel = level;
    	state = State.RUNNING;
    }
	
    /**
     * Create a clone of this logger, but with a new tag.  It'll log to the same place as the original, but the tag will be different.
     * All the methods (like level) will be independent.  It must be init'd already..
     * <p>
     * @param tag A tag for the line.
     * @return the clone.
     * @throws things.thinger.SystemException
     */   
    public CommandLogger clone(String tag) throws SystemException {
    	if (wout == null) SystemException.softwareProblem("CommandLogger.clone()  called before CommandLogger.init() .");
       	if (tag == null)  SystemException.softwareProblem("CommandLogger.clone() cannot be called with a null tag.");
    	CommandLogger result = new CommandLogger();
    	result.init(wout, tag,  currentLevel);
    	return result;
    }
	
    
	// =====================================================================================================================
	// =====================================================================================================================
	// STRING POSTER IMPLEMENTATION
	/**
	 * Post as a message.
	 * @param message String to post
	 */
	public void post(String message) throws ThingsException {
		try {
			if (state != State.RUNNING) errorLoggerNotReady();
			if (currentLevel.dontpass(defaultPostLevel)) return;
			this.post(System.currentTimeMillis(),defaultPostLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,message,null);			
		} catch (Throwable t) {
			throw new ThingsException("Primal posting fault.",ThingsException.IO_FAULT_POSTING_FAULT, t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}	
	}
	
	/**
	 * Post as a message.  Best effort.  Ignore errors.
	 * @param message String to post
	 */
	public void postit(String message) {
		if (state != State.RUNNING) return;
		try {
			if (currentLevel.dontpass(defaultPostLevel)) return;
			this.post(System.currentTimeMillis(),defaultPostLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,message,null);			
		} catch (Throwable t) {
			// Dismiss
		}
	}
	
	/**
	 * This will set the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @param newLevel the new level.
	 */
	public void setLevel(LEVEL	newLevel) {
		previousLevel = currentLevel;
		currentLevel = newLevel;
	}
	
	/**
	 * This will get the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @return the level.
	 */
	public LEVEL getLevel() {
		return currentLevel;
	}
    
	/**
	 * This will set the default level of StringPoster posted entries.  The default starts as DATA.  That means, all posted strings will be
	 * treated as DATA level.
	 * @param newLevel the new default level.
	 * @throws things.thinger.SystemException
	 */
	public void setPostLevel(LEVEL	newLevel) {
		defaultPostLevel = newLevel;
	}
	
	// ==========================================================================================
	// Local log writing
	
	/**
	 * Internal print process.
	 * @param timestamp
	 * @param level
	 * @param priority
	 * @param numeric
	 * @param text
	 * @param attributes if null, it will not print the field.
	 * @throws SystemException
	 */
	private void post(long timestamp, Logger.LEVEL level, Data.Priority priority, int numeric, String text, String attributes) throws SystemException {
		
		if (wout != null) {
			if (attributes==null) {
				wout.println(prefixId + ThingsConstants.CODEC_SEPARATOR_CHARACTER + 
						ThingsUtilityBelt.timestampFormatterDDDHHMMSS(timestamp) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
						ThingsUtilityBelt.hexFormatter16bit(numeric) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
						text) ;
			} else {
				wout.println(prefixId + ThingsConstants.CODEC_SEPARATOR_CHARACTER + 
						ThingsUtilityBelt.timestampFormatterDDDHHMMSS(timestamp) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
						ThingsUtilityBelt.hexFormatter16bit(numeric) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
						text + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
						attributes);			
			}
			wout.flush();	
		}
		if (logger!=null) {
			if (attributes==null)
				logger.shout(text, numeric, level);
			else
				logger.shout(text, numeric, level, attributes);
		}

	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// LOGGER INTERFACE
	
	/**
	 * Initialized the logger.  This will be done by the constructing system, typically the System or Kernel, so most users should
	 * not call this directly.
	 * @param loggerType The type of logger this should be.   This is more a request than a demand.
	 * @throws things.thinger.SystemException
	 */
	public void init(TYPE loggerType) throws SystemException {
		//myType = loggerType;
	}
	
	/**
	 * Turn debugging on.  Logs with debug level priority will be passed.
	 */
	public void debuggingOn() {
		previousLevel = currentLevel;
		currentLevel = LEVEL.DEBUG;
	}
	
	/**
	 * Turn debugging off.  Logs with debug level priority will not be passed.
	 */
	public void debuggingOff() {
		currentLevel = previousLevel;
	}
	
	/**
	 * Get the current debugging state.
	 * @return debugging state
	 */
	public boolean debuggingState() {
		if (currentLevel.pass(LEVEL.DEBUG)) return true;
		return false;
	}
	
	/**
	 * Tell the underlying system to flush any entries.
	 */
	public void flush() {
		try {
			wout.flush();
		} catch (Throwable t) {
			// Not our repsonsibility.
		}
	}
	
	/**
	 * Post an Entry.
	 * @param e The entry.  
	 * @throws things.thinger.SystemException
	 * @see things.data.Entry
	 */
	public void post(Entry e) throws SystemException {
		if (state != State.RUNNING) 
		if(e.attributes.getAttributeCount()>0) {
			try {
				this.post(e.getStamp(),defaultPostLevel,e.getPriority(), e.getNumeric(), e.toText(), AttributeCodec.encode2String(e.attributes.getAttributes()));
			} catch (SystemException se) { 
				throw se;
			} catch (Throwable t) {
				throw new SystemException("Failed to encode attributes in post log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
			}
		} else {
			this.post(e.getStamp(), defaultPostLevel, e.getPriority(), e.getNumeric(), e.toText(), null);
		}
	}
	
	/**
	 * Log an exception.  The implementation should try to deal with the ThingsException features.
	 * @param tr The Exception.  
	 * @throws things.thinger.SystemException
	 */
	public void exception(Throwable tr) throws SystemException {
		if (tr instanceof ThingsException) {
			ThingsException te = (ThingsException) tr;
			error(te.getMessage(), te.numeric, te.getAttributesNVDecorated());
		} else {
			error(tr.getMessage(), ThingsCodes.ERROR);
		}
	}	
	
	/**
	 * Log a trivial error entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.ERROR)) return;
		this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,null);	
	}

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg, int numeric) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.ERROR)) return;
		this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,numeric,msg,null);
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.ERROR)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in error log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}	
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void error(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.ERROR)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in error log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}	
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg, int numeric, String... attributes) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.ERROR)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in error log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}	
	}
	
	/**
	 * Log a trivial warning entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.WARNING)) return;
		this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,null);	
	}

	/**
	 * Log a trivial warning entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.WARNING)) return;
		this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,numeric,msg,null);	
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.WARNING)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in warning log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.WARNING)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in warning log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}

	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric, String... attributes) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.WARNING)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in warning log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}	
	}
	
	/**
	 * Log a trivial information entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.INFO)) return;
		this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,null);
	}

	/**
	 * Log a trivial information entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.INFO)) return;
		this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,numeric,msg,null);	
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.INFO)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in info log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.INFO)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in info log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}
	
	/**
	 * Log a complex info entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric, String... attributes) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.INFO)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in info log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}		
	}
	
	/**
	 * Log a trivial debug entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.DEBUG)) return;
		this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,null);
	}

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg, int numeric) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.DEBUG)) return;
		this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,numeric,msg,null);		
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.DEBUG)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in debug log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,
					                  SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
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
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.DEBUG)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in debug log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,
					                  SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg, int numeric, String... attributes) throws SystemException {
		if (state != State.RUNNING) errorLoggerNotReady();
		if (currentLevel.dontpass(LEVEL.DEBUG)) return;
		try {
			this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to encode attributes in debug log.", SystemException.SYSTEM_ERROR_MESSAGE_ENCODING_FAILED,t,
					                  SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}	
	}
	
	/**
	 * Shout a log entry.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param theLevel The level of the message.
	 */
	public void shout(String msg, LEVEL theLevel) {
		if (currentLevel.dontpass(theLevel)) return;
		try {
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,null);
		} catch (Throwable t) {
			// NEVER LET IT OUT
		}
	}

	/**
	 * Shout a log entry with numerics.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 */
	public void shout(String msg, int numeric, LEVEL theLevel) {
		if (currentLevel.dontpass(theLevel)) return;
		try {
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,numeric,msg,null);		
		} catch (Throwable t) {
			// NEVER LET IT OUT
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
	public void shout(String msg, int numeric, LEVEL theLevel, NVImmutable... attributes) {
		if (currentLevel.dontpass(theLevel)) return;
		try {
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (Throwable t) {
			// NEVER LET IT OUT
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
		if (currentLevel.dontpass(theLevel)) return;
		try {
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (Throwable t) {
			// NEVER LET IT OUT
		}
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
		if (currentLevel.dontpass(theLevel)) return;
		try {
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,numeric,msg,AttributeCodec.encode2String(attributes));
		} catch (Throwable t) {
			// NEVER LET IT OUT
		}	
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// TOOLS
	
	// Put this here to enforce message consistency.
	private void errorLoggerNotReady() throws SystemException {
		throw new SystemException("Logger is not a running resource.",SystemException.SYSTEM_ERROR_LOGGING_FAILED_ON_RESOURCE_NOT_READY,
				                  SystemNamespace.ATTR_SYSTEM_RESOURCE_STATE,state.toString());
	}
		
}