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
package things.thinger.kernel.basic;

import java.io.PrintWriter;
import java.util.Collection;

import things.common.ThingsCodes;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsUtilityBelt;
import things.common.WhoAmI;
import things.data.AttributeCodec;
import things.data.Data;
import things.data.Entry;
import things.data.NVImmutable;
import things.data.Receipt;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.Logger;
import things.thinger.io.conduits.ConduitID;
import things.thinger.io.conduits.PushDrain;
import things.thinger.kernel.ResourceInterface;

/**
 * Implements a Writer based logger.  It will implement PushDrain in case the Kernel wants to use it as a conduit terminus.<br>
 * The default Logger.TYPE will be BROADCAST.  The default debugging state is off.
 * <p>
 * Changing the LEVEL through Logger or Debuggable interface will remember the LEVEL before transition.  Calling debuggingOff() will restore
 * whatever the remember LEVEL is.  So, it is possible it could return it to an unintended state, depending on how you use it.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 FEB 06
 * </pre> 
 */
public class KernelBasic_WriterLogger implements Logger, PushDrain, ResourceInterface {
	
	// ==========================================================================================
	// DATA
	private State			state = State.NEW;
	private PrintWriter 	wout;
	private String			prefixId;
	private ConduitID 		conduitId;
	private WhoAmI			resourceId	= null;
	//private Logger.TYPE		myType = Logger.TYPE.BROADCAST;   	// Not currently used.  Commented out in init() below.
	private Logger.LEVEL	currentLevel = Logger.LEVEL.TOP;
	private Logger.LEVEL	previousLevel = Logger.LEVEL.TOP;
	
	private Receipt 		deliveryReceipt;						// Cached common receipts.				
	private Receipt			irreleventReceipt;

	private Logger.LEVEL	defaultPostLevel = INITIAL_DEFAULT_POST_LEVEL;
	
	// ==========================================================================================
	// STATIC CONFIGURATION
	private final static Logger.LEVEL	INITIAL_DEFAULT_POST_LEVEL = Logger.LEVEL.DATA;
	private final static long			DISPOSING_PAUSE  =	25;				
		
	// ==========================================================================================
	// UNIQUE METHODS
	
    /**
     * Initialize the LoggerWriter with a Writer and a prefix ID.  The prefix ID will be overridden by a conduit ID, if a PushDrain
     * tries to initialize this.<br>
     * You cannot call this more than once.
     * <p>
     * @param out A writer.  The implementation doesn't care what kind.  If the Logger type is set as reliable, then every write will be flushed.
     * @param ownerId Owner id.  The TAG will be used only.
     * @param level The logging level.  Note that the debug methods will override any value passed here.
     * @throws things.thinger.SystemException
     */   
    public void init(PrintWriter		out, WhoAmI		ownerId,  Logger.LEVEL		level) throws SystemException {
    	if (wout != null) SystemException.softwareProblem("KernelBasic_Logger2File.init() cannot be called twice for the same object.");
    	wout = out;
    	prefixId = ownerId.toTag();
    	currentLevel = level;
    	previousLevel = level;
    	state = State.RUNNING;
    }
	
	// =====================================================================================================================
	// =====================================================================================================================
	// STRING POSTER IMPLEMENTATION
	/**
	 * Post as a message.
	 * @param message String to post
	 * @throws ThingsException
	 */
	public void post(String message) throws ThingsException {
		try {
			if (state != State.RUNNING) errorLoggerNotReady();
			if (currentLevel.dontpass(defaultPostLevel)) return;
			this.post(System.currentTimeMillis(),defaultPostLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,message,ThingsConstants.A_NOTHING);			
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
			this.post(System.currentTimeMillis(),defaultPostLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,message,ThingsConstants.A_NOTHING);			
		} catch (Throwable t) {
			// Dismiss
		}
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
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PUSH DRAIN IMPLEMENTATION
	
    /**
     * Initialize the PushDrain.  This will be called by it's controller.  An subsequent calls may result in a PANIC SystemException.  
     * Don't do it!
     * @param yourId The ConduitID for this PushDrain.
     * @see things.thinger.io.conduits.ConduitID
     * @throws things.thinger.SystemException
     */   
    public void init(ConduitID	yourId) throws SystemException {
    	synchronized(this) {
    		if (conduitId != null) throw new SystemException("LoggerWriter initialized more than once.  This is likely a software bug.", SystemException.PANIC_THINGER_INITIALIZATION_VIOLATION);
    		conduitId = yourId;
    		prefixId = yourId.toString();
    		try {
    			deliveryReceipt = new Receipt(conduitId, Receipt.Type.DELIVERY);
    			irreleventReceipt = new Receipt(conduitId, Receipt.Type.IRRELEVENT);
    		} catch (Throwable t) {
    			throw new SystemException("KernelBasic_Logger2File.init() failed trying to create stock reciepts.  This is likely a software bug.", SystemException.PANIC_THINGER_INITIALIZATION_FAULT, SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
    		}
    	}
    }
	
	/**
	 * Listen for a post.  Consumers should implement this.
	 * @param n The data to post.
	 * @return a receipt
	 * @throws things.thinger.SystemException
	 */
	public Receipt postListener(Data		n) throws SystemException {
		if (state != State.RUNNING) throw new SystemException("Drain (Logger2File) is not a running resource.",SystemException.IO_CONDUIT_FAULT_POST_FAILED_ON_ENDPOINT_NOT_READY_RESOURCE,"Resource state",state.toString());
		try {
			if ((n.getType()==Data.Type.ENTRY)&&(currentLevel.pass(defaultPostLevel))) {
				this.post((Entry)n);
				return deliveryReceipt;
			} else {
				return irreleventReceipt;
			}
		} catch (Throwable t) {
			throw new SystemException("Logger failed while listening to a conduit.", SystemException.SYSTEM_ERROR_LOGGING_FAILED_ON_CONDUIT,t);
		}
	}
	
	// ==========================================================================================
	// Local log writing
	private void post(long timestamp, Logger.LEVEL level, Data.Priority priority, int numeric, String text, String attributes) throws SystemException {
		
		wout.println(prefixId + ThingsConstants.CODEC_SEPARATOR_CHARACTER + 
				ThingsUtilityBelt.timestampFormatterDDDHHMMSS(timestamp) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
				level.toString5() + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
				ThingsUtilityBelt.hexFormatter16bit(numeric) + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
				text + ThingsConstants.CODEC_SEPARATOR_CHARACTER +
				attributes
				);
		// I don't know if this will be tramatic!
		wout.flush();
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
			this.post(e.getStamp(), defaultPostLevel, e.getPriority(), e.getNumeric(), e.toText(), ThingsConstants.A_NOTHING);
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
		this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,ThingsConstants.A_NOTHING);	
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
		this.post(System.currentTimeMillis(),LEVEL.ERROR,Data.Priority.ROUTINE,numeric,msg,ThingsConstants.A_NOTHING);
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
		this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,ThingsConstants.A_NOTHING);	
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
		this.post(System.currentTimeMillis(),LEVEL.WARNING,Data.Priority.ROUTINE,numeric,msg,ThingsConstants.A_NOTHING);	
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
		this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,ThingsConstants.A_NOTHING);
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
		this.post(System.currentTimeMillis(),LEVEL.INFO,Data.Priority.ROUTINE,numeric,msg,ThingsConstants.A_NOTHING);	
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
		this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,ThingsConstants.A_NOTHING);
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
		this.post(System.currentTimeMillis(),LEVEL.DEBUG,Data.Priority.ROUTINE,numeric,msg,ThingsConstants.A_NOTHING);		
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
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,ThingsCodes.NO_NUMERIC,msg,ThingsConstants.A_NOTHING);
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
			this.post(System.currentTimeMillis(),theLevel,Data.Priority.ROUTINE,numeric,msg,ThingsConstants.A_NOTHING);		
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
	// RESOURCE INTERFACE
	
	// == KERNEL METHODS =============================================================================
	
	/**
	 * Initialize the resource.  This must be called before use.  It should be called by the Kernel.
	 * <p>
	 * @param id The id of the resource.
	 * @see things.common.WhoAmI
	 * @throws things.thinger.SystemException
	 */
	public void initResource(WhoAmI  id) throws SystemException {
		resourceId = id;
	}
	
	/**
	 * This is how the system tells a resource he is about to be destroyed.  Typically, the system will allow the resource some time to 
	 * clean up, however the implementation should assume that the system can become impatient at any moment and summerily execute it.
	 * <br>
	 * This may be called asynchronously by the system at any time and by any thread, so a smart system would manage conflicts and thread issues.
	 * <p>
	 * @throws things.thinger.SystemException
	 */
	public void disposeResource() throws SystemException {
    	state = State.DISPOSING;
   	
    	// Pause to let any writer finish.  Assume any interrupt is an impatient Kernel.
    	try { 
    		Thread.sleep(DISPOSING_PAUSE);
    	} catch (Throwable t) {
    		// Don't care--move along little doggie
    	}
    	
    	// Flush and detatch the output.
    	this.flush();
    	wout = null;
    	
    	// Done
    	state = State.DEAD;
	}
	
	// == RESOURCE METHODS ===========================================================================
	
	/**
	 * Lock an object.  This is blocking.  Locks will prevent any other thread from accessing the resource.  However, the
	 * resource may not allow locks, and it will return false if the lock was disallowed.
	 * @return true if the lock was completed, false if locks are not allowed.
	 * @throws things.thinger.SystemException
	 */
	public boolean lock() throws SystemException {
		// Not supported.
		return false;
	}

	/**
	 * This call is only meaningful if the thread holds the lock.  The resource could choose to ignore it, but locking is not supported.
	 * <br>
	 * If locks are supported and the thread does not own the lock, the resource may throw an exception.
	 * <p>
	 * @throws things.thinger.SystemException
	 */
	public void unlock() throws SystemException {
		// Not supported.
	}
	
	/**
	 * Get ID of the resource.  If the resource has not been initResource(), this should throw an exception.
	 * <p>
	 * @return WhoAmI
	 * @see things.common.WhoAmI
	 * @throws things.thinger.SystemException
	 */
	public WhoAmI getId() throws SystemException {
		return resourceId;
	}
	
	/**
	 * Get the current state.
	 * <p>
	 * @return State
	 * @throws things.thinger.SystemException
	 */
	public State getState() throws SystemException {
		return state;
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