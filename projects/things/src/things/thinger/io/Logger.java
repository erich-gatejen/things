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
package things.thinger.io;

import java.util.Collection;

import things.common.Debuggable;
import things.data.Entry;
import things.data.NVImmutable;
import things.thinger.SystemException;

/**
 * Logging interface for general logging.  The implementations will provide a Debuggable interface for simple stuff.  
 * <p>
 * The interface supports logging/entry management across channels, which is the main reason why I didn't use 
 * one the popular libraries.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 8 JAN 06
 * </pre> 
 */
public interface Logger extends Debuggable {

	/**
	 * Types of loggers.  It is up the the implementation to define the actual behavior.<br>
	 * UNDECLARED = Does not matter.<br>
	 * BROADCAST = No delivery guarantees.<br>
	 * RELIABLE = Reliable delivery.
	 */
	public enum TYPE { UNDECLARED, BROADCAST, RELIABLE }
	
	/**
	 * Level of the logged entry.  These are not valued, since it's up to the final consumer to decide what they mean.<br>
	 * Data and error are the same significance and would normally be retained in the same consumer, assuming a normal logging level. 
	 * The idea is that Data would be result values while Errors would be actual problems.  Info is more of casual information, 
	 * rather than important to result collection.   
	 */
	public enum LEVEL { FLOOR(0), DEBUG(1), INFO(2), WARNING(3), ERROR(4), DATA(4), RESULTS(5), FAULT(5), TOP(5);
		private final int rank;    
		private LEVEL(int rank) { this.rank = rank; }
		protected final int value() {return rank; }
		public boolean pass(LEVEL thanThis) { if (this.rank <= thanThis.value()) return true; return false; }	
		public boolean dontpass(LEVEL thanThis) { if (this.rank > thanThis.value()) return true; return false; }
		public String toString5() {
			switch (this) {
			case FLOOR: return "FLOOR";
			case DEBUG: return "DEBUG";
			case INFO: return "INFO ";
			case WARNING: return "WARNG";
			case ERROR: return "ERROR";
			case DATA: return "DATA ";
			case RESULTS: return "RESLT";
			case FAULT: return "FAULT";
			case TOP: return "TOP  ";
			}
			return "XXXXX";
		}
		
		/**
		 * It will return a level by name.  It is case insensitive.  This is a slow implementation, so should only be used during configuration.
		 * @param name The name.
		 * @return The level or null if it couldn't be matched.
		 */
		static public LEVEL getLevelByName(String name) {
			if (name.equalsIgnoreCase("FLOOR")) return Logger.LEVEL.FLOOR;
			if (name.equalsIgnoreCase("DEBUG")) return Logger.LEVEL.DEBUG;
			if (name.equalsIgnoreCase("INFO")) return Logger.LEVEL.INFO;
			if (name.equalsIgnoreCase("WARNING")) return Logger.LEVEL.WARNING;
			if (name.equalsIgnoreCase("ERROR")) return Logger.LEVEL.ERROR;
			if (name.equalsIgnoreCase("DATA")) return Logger.LEVEL.DATA;
			if (name.equalsIgnoreCase("FAULT")) return Logger.LEVEL.FAULT;
			if (name.equalsIgnoreCase("RESULTS")) return Logger.LEVEL.FAULT;
			if (name.equalsIgnoreCase("TOP")) return Logger.LEVEL.TOP;
			return null;
		}
	}
	
	/**
	 * Initialized the logger.  This will be done by the constructing system, typically the System or Kernel, so most users should
	 * not call this directly.
	 * @param loggerType The type of logger this should be.   This is more a request than a demand.
	 * @throws things.thinger.SystemException
	 */
	public void init(TYPE loggerType) throws SystemException;

	/**
	 * This will set the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @param newLevel the new level.
	 */
	public void setLevel(LEVEL	newLevel);
	
	/**
	 * This will get the level of entries that will pass.  It starts at whatever the implementation sets during construction.
	 * @return the level.
	 */
	public LEVEL getLevel();
	
	/**
	 * This will set the default level of StringPoster posted entries.  The default starts as DATA.  That means, all posted strings will be
	 * treated as DATA level.
	 * @param newLevel the new default level.
	 * @throws things.thinger.SystemException
	 */
	public void setPostLevel(LEVEL	newLevel);
	
	/**
	 * Tell the underlying system to flush any entries.
	 */
	public void flush();
	
	/**
	 * Post an Entry.
	 * @param e The entry.  
	 * @throws things.thinger.SystemException
	 * @see things.data.Entry
	 */
	public void post(Entry e) throws SystemException;
	
	/**
	 * Log an exception.  The implementation should try to deal with the ThingsException features.
	 * @param tr The Exception.  
	 * @throws things.thinger.SystemException
	 */
	public void exception(Throwable tr) throws SystemException;
	
	/**
	 * Log a trivial error entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg) throws SystemException;

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void error(String msg, int numeric) throws SystemException;
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void error(String msg, int numeric, NVImmutable... attributes) throws SystemException;
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes C of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void error(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException;
	
	/**
	 * Log a complex error entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 * @see things.data.NV
	 */
	public void error(String msg, int numeric, String... attributes) throws SystemException;
	
	/**
	 * Log a trivial warning entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg) throws SystemException;

	/**
	 * Log a trivial warning entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric) throws SystemException;
	

	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void warning(String msg, int numeric, String... attributes) throws SystemException;
	
	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void warning(String msg, int numeric, NVImmutable... attributes) throws SystemException;
	
	/**
	 * Log a complex warning entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void warning(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException;	
	
	//
	/**
	 * Log a trivial information entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg) throws SystemException;

	/**
	 * Log a trivial information entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric) throws SystemException;
	
	/**
	 * Log a complex info entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 */
	public void info(String msg, int numeric, String... attributes) throws SystemException;
	
	/**
	 * Log a complex information entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void info(String msg, int numeric, NVImmutable... attributes) throws SystemException;	
	
	/**
	 * Log a complex information entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void info(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException;	
	
	/**
	 * Log a trivial debug entry.
	 * @param msg The text message.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg) throws SystemException;

	/**
	 * Log a trivial error entry with a numeric.
	 * @param msg The text message. 
	 * @param numeric The numeric.
	 * @throws things.thinger.SystemException
	 */
	public void debug(String msg, int numeric) throws SystemException;
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @throws things.thinger.SystemException
	 * @see things.data.NV
	 */
	public void debug(String msg, int numeric, String... attributes) throws SystemException;
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void debug(String msg, int numeric, NVImmutable... attributes) throws SystemException;
	
	/**
	 * Log a complex debug entry with numerics and attributes.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param attributes Sequence of NVs representing attributes.
	 * @throws things.thinger.SystemException
	 * @see things.data.NVImmutable
	 */
	public void debug(String msg, int numeric, Collection<NVImmutable> attributes) throws SystemException;
	
	/**
	 * Shout a log entry.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param theLevel The level of the message.
	 */
	public void shout(String msg, LEVEL theLevel);
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Name/value pairs for attributes.  Must be an even number of Strings.
	 * @see things.data.NV
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, String... attributes);
	
	/**
	 * Shout a log entry with numerics.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @see things.data.NV
	 */
	public void shout(String msg, int numeric, LEVEL theLevel);
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Sequence of NVs representing attributes.
	 * @see things.data.NVImmutable
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, NVImmutable... attributes);
	
	/**
	 * Shout a log entry with numerics and attributes.  This is best effort and it will never return an exception.
	 * @param msg The text message.
	 * @param numeric The numeric.
	 * @param theLevel The level of the message.
	 * @param attributes Sequence of NVs representing attributes.
	 * @see things.data.NVImmutable
	 */
	public void shout(String msg, int numeric, LEVEL theLevel, Collection<NVImmutable> attributes);
}