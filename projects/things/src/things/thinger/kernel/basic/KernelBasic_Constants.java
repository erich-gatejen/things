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

import things.thinger.kernel.Clearance;

/**
 * Immutable constants for a Basic Kernel.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 10 AUG 05
 * </pre> 
 */
public interface KernelBasic_Constants {

	// =====================================================================================================================
	// =====================================================================================================================
	// ROOT CONFIGURATION
	
	// CLEARANCE levels
	public static final Clearance DEFAULT_KERNEL_CLEARANCE = Clearance.ELITE;
	public static final Clearance DEFAULT_SERVICE_CLEARANCE = Clearance.PRIVILEGED;
	public static final Clearance DEFAULT_USER_CLEARANCE = Clearance.OFFICIAL;
	
	/**
	 * Kernel runtime sleep interval in millis.  Don't let this be configurable for now.
	 */
	public static final int	KERNEL_RUNTIME_SLEEP_INTERVAL = 350;
	
	/**
	 * Number of sweeps to allow for shutdown before the kernel just starts killing things.
	 */
	public static final int	KERNEL_SHUTDOWN__SWEEPS = 5;
	
	// =====================================================================================================================
	// =====================================================================================================================
	// CHANNELS
	
	/**
	 *	System backbone logging channel.
	 */
	public static final String	CHANNEL_SYSTEM_BACKBONE = "kb.channel.sb";
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PROPERTY NAMES FOR KERNEL BASIC CONFIGURATION (see etc/basic_config for example).
	
	// The USER FILESYSTEM defines where the Things installation belongs.  Typically, Things should restrict itself to this tree.
	public static final String	USER_FILESYSTEM_ROOT	= "kb.user.filesystem.root";
	
	// The SYSTEM FILESYSTEM defines the root of the underlying system's filesystem.
	public static final String	SYSTEM_FILESYSTEM_ROOT	= "kb.system.filesystem.root";
	
	/**
	 * The default logging level for the whole system.  The levels are named by the Logger.LEVEL enumeration.
	 * @see things.thinger.io.Logger.LEVEL
	 */
	public static final String	LOGGING_LEVEL	= "kb.logging.level";
	
	/**
	 * This this is set to true, it will load the loader for debugging IDE (which will load all classes from the classpath,
	 * rather than the dynamic loader).  It is false by default.
	 */
	public static final String	DEBUGGING_IDE	= "kb.debugging.ide";
	
	/**
	 *  Path to the default universe config from the install root.
	 */
	public static final String	DEFAULT_UNIVERSE_CONFIG	= "kb.universe.config";
	
	// Default universe names
	public static final String	UNIVERSE_SYSTEM_DEFAULT_NAME_CONFIG = "kb.universe.system.default";
	public static final String	UNIVERSE_USER_DEFAULT_NAME_CONFIG = "kb.universe.user.default";

	/**
	 * THINGS Loader configuration.
	 * THING space configuration
	 * kb.things.cache defines the location within universe/system space for the cache
     * kb.things.universe.# lists all the universe(s) where THINGS sit.  They will be search in the numbered (#) order.
	 */
	public static final String	THINGS_CACHE_LOCATION = "kb.things.cache";
	public static final String	THINGS_UNIVERSE_LOCATION_PLY = "kb.things.universe";
	
	/**
	 * Allow the system to change the logging implementation.  This will be a class name for the implementation.
	 * This is done so the testers can intercept the logging.  This is optional.  The default logger will be KernelBasic_Logger2File.
	 */
	public static final String	LOGGING_FACTORY_IMPLEMENTATION	= "kb.logging.factory.implementation";
	public static final String	DEFAULT_LOGGING_FACTORY_IMPLEMENTATION	= "things.thinger.kernel.basic.KernelBasic_WriterLogger_StandardFactory";
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PROPERTY NAMES FOR SERVICE SPECIFICATION (see etc/basic_config for example).
	//
	// kb.core.service.{service name}.name	= NAME
	// kb.core.service.{service name}.class	= CLASS
	// kb.core.service.{service name}.local = Local propery tree, visible to the service by getLocalProperties.
	
	// Service component to the tree.
	public static final String	CORE_SERVICES	= "kb.core.service";
	public static final String	CORE_SERVICES_suffix_NAME	= "name";
	public static final String	CORE_SERVICES_suffix_CLASS	= "class";
	public static final String	CORE_SERVICES_suffix_LOCAL	= "local";
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PROPERTY NAMES FOR USER CONFIGURATION AND LAYOUT (see etc/basic_config for example).
	// The global properties space will be copied to each new user process when it is created.  However, it's a fork, so
	// any new changes to the global space will not change the individual user process spaces.
	public static final String	USER_ROOT = "user";
	public static final String  USER_ROOT_GLOBAL_SPACE = "user.global";
	public static final String  USER_ROOT_PROCESS_SPACE = "user.p";
	
	// Shared by all processes.  There are no protections.
	public static final String  SHARED_ROOT_GLOBAL_SPACE = "shared.global";
	
	// These will be provided as READ ONLY to the system interface.  This branch can be put into the server configuration under this path
	// to be exposed to the users.
	public static final String  USER_ROOT_CONFIGURED_SPACE = "user.config";
	
	public static final String	USER_suffix_KERNEL_NAME = "kernel.name";
	
	// =====================================================================================================================
	// =====================================================================================================================
	// SYSTEM LAYOUT
	
	// NAMING
	public final static String KERNEL_ID_STRING = "kernel";
	public final static String KERNEL_ID_TAG = "K0";
	public final static String USERSPACE_ID_STRING = "user";
	public final static String USERSPACE_ID_TAG = "S0";
	public final static String PROCESS_ID_STRING_PREFIX = "process.";
	public final static String PROCESS_ID_TAG_PREFIX = "P";
	public final static String SERVICE_ID_STRING_PREFIX = "service.";
	public final static String SERVICE_ID_TAG_PREFIX = "S";
	public final static char ID_SEPERATOR = '.';
	
	// LOGGING
	// TODO change LOG_DIRECTORY and KERNEL_LOG_PATH to configurable.
	public final static String LOG_DIRECTORY	= "/log";
	public final static String LOG_SUFFIX	= ".log";
	public final static String KERNEL_LOG_PATH	= "/log/kernel.log";
	public final static String SERVICE_LOG_PREFIX	= LOG_DIRECTORY + "/service.";
	public final static String USER_LOG_PREFIX	= LOG_DIRECTORY + "/user.";
	
	// CONFIG
	public final static String CONFIG_DIRECTORY	= "/etc";	
	

	// RESOURCE NAMING
	public final static String RESOURCENAME_LOGGER_PREFIX = "logger.";
	
}