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
 * Standard static codes for the system.
 * <p>
 * These should only be used by system implementations.  Eventually, this needs to be an enum.
 * <p>
 * The threshold levels are as follows.  They match the general logging scheme.  Anything the level below (higher on the list, lower numberically) is considered to pass the threshold.
 * That is, if the threshold is ERROR, then only FAULTs or lower will pass; the threshold level is not inclusive.<br>
 * PANIC<br>
 * FAULT<br>
 * ERROR<br>
 * WARNING<br>
 * INFO<br>
 * DEBUG<br>
 * ALL<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 15 NOV 02
 * Modified constantly through the project.
 * </pre> 
 */
public interface ThingsCodes {

	/**
	 * Numeric lower bounderies. <br>
	 * 0x0000 - system panic <br>
	 * 0x0500 - module panic <br>
	 * 0x1000 - system fault <br>
	 * 0x2000 - module fault <br>
	 * 0x3000 - user fault <br>
	 * 0x4000 - system error <br>
	 * 0x5000 - module error <br>
	 * 0x6000 - user error <br>
	 * 0x7000 - system warning <br>
	 * 0x8000 - module warning <br>
	 * 0x9000 - user warning <br>
	 * 0xA000 - system info <br>
	 * 0xB000 - module info <br>
	 * 0xC000 - user info <br>
	 * 0xD000 - system debug <br>
	 * 0xE000 - module debug <br>
	 * 0xF000 - user debug <br>
	 * 0xFFFF - ALL
	 */
	public final static int INVALID_NUMERIC = -1;
	public final static int FLOOR_NUMERIC = 0;
	public final static int NO_NUMERIC = 0;
	public final static int DEFAULT_NUMERIC = 1;

	/**
	 * EVENTS and CODES. They share number space.
	 */
	public final static int CODE_NONE = FLOOR_NUMERIC;
	
	// ===================================================================
	// == PANIC ==========================================================
	public final static int PANIC = 0x0000;
	
	// 0x0000 - system panic <br>
	public final static int PANIC_PROCESS_RESPONDING_TO_HALT_OK = 0x0100;

	public final static int PANIC_THINGS_BUG  = 0x0200;
	public final static int PANIC_THINGS_COMMON_BUG  = 0x0210;	
	public final static int PANIC_THINGER_BUG = 0x0250;
	public final static int PANIC_THINGER_INITIALIZATION_VIOLATION = 0x0260;
	public final static int PANIC_THINGER_INITIALIZATION_FAULT = 0x0261;
	
	public final static int PANIC_KERNEL_GENERAL = 0x0275;
	public final static int PANIC_KERNEL_ID_ALREADY_USED = 0x0276;
	
	public final static int PANIC_REQUIRED_PROPERTY_NOT_SET = 0x0300;
	
	public final static int PANIC_SYSTEM_FAILURE = 0x0400;
	
	public final static int PANIC_SYSTEM_KERNEL_FAILURE = 0x0410;
	public final static int PANIC_SYSTEM_STARTUP_KERNEL_FAILURE = 0x0411;
	public final static int PANIC_SYSTEM_KERNEL_NO_LOGGER_FOR_PROCESS = 0x0412;
	public final static int PANIC_SYSTEM_STARTUP_KERNEL_CONSTRUCTION_FAILURE = 0x0413;
	
	public final static int PANIC_SYSTEM_LOADER_STARTUP_FAILED = 0x0414;
	public final static int PANIC_SYSTEM_LOADER_BAD_STATE = 0x0415;
	public final static int PANIC_SYSTEM_LOADER_COULD_NOT_CLEAN_FAILED_COMPILE = 0x0416;
	
	public final static int PANIC_SYSTEM_STARTUP_SERVICE_FAILURE = 0x0421;

	public final static int PANIC_SYSTEM_SERVICE_FAILURE = 0x0440;
	public final static int PANIC_SYSTEM_SERVICE_FAILURE_DURING_CONSTRUCTION = 0x0441;
	public final static int PANIC_SYSTEM_SERVICE_UNRECOVERABLE = 0x0442;

	public final static int PANIC_SYSTEM_COMMAND_GENERAL = 0x0450;
	public final static int PANIC_SYSTEM_COMMAND_GENERAL_RESPONSE_TRANSMISSION = 0x0451;
	public final static int PANIC_SYSTEM_COMMAND_INTERRUPTED_AND_UNRELIABLE = 0x0452;
	public final static int PANIC_SYSTEM_COMMAND_CASCADING_PROBLEMS = 0x0453;
	
	public final static int PANIC_SYSTEM_REINIT_NOT_ALLOWED = 0x0460;
	public final static int PANIC_SYSTEM_REINIT_CONDUIT_NOT_ALLOWED = 0x0461;
	public final static int PANIC_SYSTEM_CONDUIT_UNRELIABLE = 0x0462;
	
	// 0x0500 - module panic <br>
	
	// 0x0600 - user panic <br>
	public final static int USER_PANIC_RESERVED_END = 0x06FF;
	
	public final static int PANIC_TOP = 0x0FFF;	
	
	// ===================================================================
	// == FAULT ==========================================================
	public final static int FAULT = 0x1000;	
	
	// 0x1000 - system fault <br>
	public final static int SYSTEM_FAULT = 0x1000;
	public final static int SYSTEM_FAULT_STAMP_CREATION_FAILED = 0x1005;
	public final static int SYSTEM_FAULT_EXTERNAL_INTERRUPTION = 0x1006;

	public final static int KERNEL_FAULT = 0x1100;
	public final static int KERNEL_FAULT_INTERNAL_LOCK_FAILURE = 0x1101;
	public final static int KERNEL_FAULT_CLASS_ISSUE = 0x1105;
	public final static int KERNEL_FAULT_COULD_NOT_FORGE_LOGGER = 0x1106;
	
	public final static int KERNEL_FAULT_PROCESS_FAULT = 0x1150;
	public final static int KERNEL_FAULT_PROCESS_ILL_CONSTRUCTED = 0x1151;
	public final static int KERNEL_FAULT_PROCESS_ALREADY_INITIALIZED = 0x1152;	
	public final static int KERNEL_FAULT_PROCESS_REGISTRATION_BAD = 0x1153;
	public final static int KERNEL_FAULT_PROCESS_ALREADY_NAMED = 0x1160;	
	public final static int KERNEL_FAULT_PROCESS_NOT_RECOGNIZED = 0x1162;
	public final static int KERNEL_FAULT_PROCESS_CREDENTIAL_FAULT = 0x1165;
	public final static int KERNEL_FAULT_PROCESS_MANAGEMENT = 0x1166;
	
	public final static int KERNEL_FAULT_PROCESS_INTERRUPTED = 0x1167;
	
	public final static int IO_FAULT = 0x1200;
	public final static int IO_FAULT_POSTING_FAULT = 0x1210;
	public final static int IO_FAULT_ABSTRACT_STREAM = 0x1220;
	
	public final static int IO_FILESYSTEM_FAULT_DEFAULT = 0x1250;
	public final static int IO_FILESYSTEM_FAULT_GENERAL = 0x1251;
	
	public final static int IO_CONDUIT_FAULT_GENERAL = 0x1300;	
	public final static int IO_CONDUIT_FAULT_NOT_INITIALIZED = 0x1301;
	public final static int IO_CONDUIT_FAULT_POST_FAILED = 0x1305;
	public final static int IO_CONDUIT_FAULT_POST_FAILED_ON_ENDPOINT_NOT_READY = 0x1310;
	public final static int IO_CONDUIT_FAULT_POST_FAILED_ON_ENDPOINT_NOT_READY_RESOURCE = 0x1311;
	
	public final static int TEST_FAULT_DEFAULT = 0x1400;
	public final static int TEST_FAULT_NOT_INITIALIZED = 0x1401;	
	
	public final static int SYSTEM_FAULT_PROPERTYVIEW_NOT_INITIALIZED = 0x1500;
	public final static int SYSTEM_FAULT_WITH_PROPERTIES = 0x1510;
	public final static int SYSTEM_FAULT_PROPERTIES_SOURCE_NOT_FOUND = 0x1515;
	public final static int SYSTEM_FAULT_PROPERTIES_LOAD_FAILED = 0x1520;
	public final static int SYSTEM_FAULT_PROPERTIES_SAVE_FAILED = 0x1525;
	public final static int SYSTEM_FAULT_PROPERTIES_MODE_VIOLATION = 0x1526;
	public final static int SYSTEM_FAULT_TOOLKIT_FAILED = 0x1527;
	public final static int SYSTEM_FAULT_PROPERTIES_BAD_ACCESS = 0x1528;
	
	public final static int SYSTEM_FAULT_PROCESS = 0x1550;
	public final static int SYSTEM_FAULT_PROCESS_INIT_FAILED = 0x1551;
	public final static int SYSTEM_FAULT_PROCESS_REGISTER_FAILED = 0x1552;
	public final static int SYSTEM_FAULT_PROCESS_UNHANDLED = 0x1553;
	public final static int SYSTEM_FAULT_PROCESS_INTERRUPTED = 0x1554;
	public final static int SYSTEM_FAULT_PROCESS_WAIT_INTERRUPTED = 0x1555;
	
	public final static int	SYSTEM_COMMAND_FAULT = 0x1600;	
	public final static int	SYSTEM_COMMAND_FAULT_RESPONSE_FAILED = 0x1601;	
	public final static int	SYSTEM_COMMAND_FAULT_SERVICE_ABORTED = 0x1602;	
	public final static int	SYSTEM_COMMAND_FAULT_RESPONSE_FAILED_BAD_RECIEPT = 0x1603;	
	public final static int	SYSTEM_COMMAND_FAULT_CANNOT_DEFINE = 0x1604;	
	public final static int	SYSTEM_COMMAND_FAULT_UNDEFINED_STATE = 0x1610;	
	public final static int	SYSTEM_COMMAND_FAULT_SET_PROBLEM = 0x1611;	
	public final static int	SYSTEM_COMMAND_FAULT_INSTANCE_USED_BEFORE_READY = 0x1612;	
	public final static int	SYSTEM_COMMAND_FAULT_INSTANCE_TYPE_MIXING = 0x1613;	
	public final static int	SYSTEM_COMMAND_FAULT_INSTANCE_DATA_BAD = 0x1614;	
	public final static int	SYSTEM_COMMAND_FAULT_DURING_ISSUANCE = 0x1615;	
	public final static int	SYSTEM_COMMAND_FAULT_RESPONSE_OBJECT_BAD = 0x1616;		
	public final static int	SYSTEM_COMMAND_FAULT_RESPONSE_PROCESSING = 0x1617;		
	public final static int	SYSTEM_COMMAND_FAULT_SPURIOUS = 0x1618;
	public final static int	SYSTEM_COMMAND_FAULT_NOT_DEFINED = 0x1619;	
	public final static int	SYSTEM_COMMAND_FAULT_CLASS_NOT_FOUND = 0x1620;	
	public final static int	SYSTEM_COMMAND_FAULT_PARAMETER_MISSING_AFTER_TRANSMISSION = 0x1621;	
	public final static int	SYSTEM_COMMAND_FAULT_COULD_NOT_BUILD_RESPONSE = 0x1622;	
	public final static int	SYSTEM_COMMAND_FAULT_COULD_NOT_BUILD_COMMANDER = 0x1623;	
	public final static int	SYSTEM_COMMAND_FAULT_COULD_NOT_BUILD_CLI_COMMAND = 0x1624;	
	public final static int	SYSTEM_COMMAND_FAULT_COULD_NOT_ISSUE_CLI_COMMAND = 0x1625;	
	
	public final static int	SYSTEM_LOADER_FAULT = 0x1640;	
	public final static int	SYSTEM_LOADER_FAULT_BAD_CONFIG = 0x1641;	
	public final static int	SYSTEM_LOADER_FAULT_DUPLICATE_CONFIG = 0x1642;
	public final static int	SYSTEM_LOADER_FAULT_COMPILER_FAILED = 0x1643;	
	
	public final static int UNIVERSE_FAULT_DEFAULT = 0x1700;	
	public final static int UNIVERSE_FAULT_BAD_CALL = 0x1705;	
	public final static int UNIVERSE_FAULT_UNEXPECTED = 0x1710;
	public final static int UNIVERSE_FAULT_NAMING_FAILED = 0x1711;
	public final static int UNIVERSE_FAULT_NOT_REGISTERED = 0x1712;
	public final static int UNIVERSE_FAULT_LOCAL_MANAGEMENT = 0x1713;
	public final static int UNIVERSE_FAULT_LOCAL_PATH_MANAGEMENT = 0x1714;
	public final static int UNIVERSE_FAULT_COULD_NOT_ACCESS = 0x1715;
	public final static int UNIVERSE_FAULT_ACCESSOR_PROBLEM = 0x1716;
	
	public final static int SYSTEM_FAULT_SERVICE_GERERAL = 0x1750;
	public final static int SYSTEM_FAULT_SERVICE_FAILED_TO_CONSTRUCT = 0x1751;
	public final static int SYSTEM_FAULT_SERVICE_PROBLEM = 0x1752;
	
	public final static int CONFIGURATION_FAULT_DEFAULT = 0x1800;	
	
	public final static int SYSTEM_FAULT_DATA_DEFAULT = 0x1851;
	public final static int SYSTEM_FAULT_DATA_VIOLATE_NATIVE = 0x1852;	
	
	public final static int SYSTEM_FAULT_TEST_SUITE = 0x1900;
	
	public final static int SYSTEM_FAULT_THING_PROBLEM = 0x1950;	
	public final static int SYSTEM_FAULT_THING_RESULT_BAD = 0x1951;	
	public final static int SYSTEM_FAULT_THING_RESULT_MISUSED = 0x1952;	
	public final static int SYSTEM_FAULT_THING_FAILED_INIT = 0x1953;
	public final static int SYSTEM_FAULT_THING_FAULT = 0x1954;
	public final static int SYSTEM_FAULT_THING_SPURIOUS_EXCEPTION = 0x1955;
	public final static int SYSTEM_FAULT_THING_CONSTRUCTION_SPURIOUS_EXCEPTION = 0x1956;
	public final static int SYSTEM_FAULT_THING_CONSTRUCTION_NULLED = 0x1957;
	public final static int SYSTEM_FAULT_THING_CONSTRUCTION_BAD_CLASS = 0x1958;
	public final static int SYSTEM_FAULT_THING_FAILED_DEFINITION = 0x1959;
	
	public final static int SYSTEM_FAULT_FAILED_INIT = 0x1980;
	
	public final static int SYSTEM_FAULT_SOFTWARE_PROCESS_DISALLOWED = 0x1997;
	public final static int SYSTEM_FAULT_SOFTWARE_PROBLEM = 0x1998;
	public final static int SYSTEM_FAULT_SOFTWARE_DETECTED = 0x1999;
	
	// 0x2000 - module fault <br>
	
	public final static int THING_FAULT_MODULE_DEFAULT = 0x2000;	
	
	public final static int THING_FAULT_COMMANDLINE_BAD_DECLARATION = 0x2010;
	public final static int THING_FAULT_COMMANDLINE_NOT_DECLARED = 0x2011;
	public final static int THING_FAULT_COMMANDLINE_PROCESSOR_STARTUP = 0x2020;	
	
	public final static int THING_FAULT_RESULT_MANAGEMENT = 0x2030;	
	public final static int THING_FAULT_RESULT_COLLECTION = 0x2031;	
	
	public final static int MODULE_FAULT_NULL_PARAMETER = 0x2050;
	public final static int MODULE_FAULT_BAD_USAGE = 0x2051;
	
	public final static int MODULE_FAULT_FAILED_INIT = 0x2070;
	
	public final static int GENERAL_PARSER_FAULT = 0x2080;
	
	public final static int PROCESSING_FAULT = 0x2100;	
	public final static int PROCESSING_FAULT_MATCHER = 0x2105;	
	public final static int PROCESSING_FAULT_HTTP = 0x2110;	
	
	public final static int SERVICE_FAULT_HTTPTOOL= 0x2200;
	public final static int SERVICE_FAULT_HTTPTOOL_SPURIOUS= 0x2201;
	public final static int SERVICE_FAULT_HTTPTOOL_STOCK_SETUP = 0x2202;
	
	public final static int SMTPCLIENT_FAULT_GENERAL = 0x2220;
	public final static int SMTPCLIENT_FAULT_STARTUP = 0x2221;
	public final static int SMTPCLIENT_FAULT_NOT_STARTED = 0x2222;
	public final static int SMTPCLIENT_FAULT_BAD_STATE = 0x2223;
	public final static int SMTPCLIENT_FAULT_SPURIOUS = 0x2224;
	public final static int SMTPCLIENT_FAULT_CANNOT_CONNECT = 0x2225;
	public final static int SMTPCLIENT_FAULT_CANNOT_COMPLETE = 0x2226;
	public final static int SMTPCLIENT_FAULT_CANNOT_COMPLETE_NOT_RETRYABLE = 0x2227;
	
	public final static int ACTORSERVICE_FAULT = 0x2300;
	public final static int ACTORSERVICE_FAULT_BAD_MESSAGE = 0x2301;
	public final static int ACTORSERVICE_FAULT_BAD_USAGE = 0x2302;
	public final static int ACTORSERVICE_FAULT_BAD_MESSAGE_FIELD = 0x2303;
	public final static int ACTORSERVICE_FAULT_STARTUP = 0x2304;
	public final static int ACTORSERVICE_FAULT_MESSAGE = 0x2305;

	public final static int DECOMPOSER_FAULT = 0x2350;

	public final static int PROXY_FAULT = 0x2350;
	public final static int PROXY_FAULT_SOCKET_PREPARE = 0x2351;	
	public final static int PROXY_FAULT_SESSION = 0x2352;
	
	// 0x3000 - user fault <br>

	public final static int THING_FAULT_DEFAULT = 0x3000;
	public final static int THING_FAULT_DEFINITION = 0x3010;	
	public final static int THING_FAULT_DEFINITION_ALREADY_NAMED = 0x3011;	
	public final static int THING_FAULT_DEFINITION_BAD = 0x3012;	
	public final static int THING_FAULT_RESULT_FUNDIMENTAL = 0x3013;
	
	public final static int THING_FAULT_SERVICE_COULD_NOT_GET_UNIVERSE = 0x3040;
	public final static int THING_FAULT_SERVICE_COULD_NOT_GET_UNIVERSE_ACCESSOR = 0x3041;
	
	public final static int USER_FAULT_RESERVED_END = 0x30FF;
	
	public final static int FAULT_TOP = 0x3FFF;
	
	// ===================================================================
	// == ERROR ==========================================================
	public final static int ERROR = 0x4000;	
	
	// 0x4000 - system error <br>
	public final static int SYSTEM_ERROR = 0x4000;
	public final static int SYSTEM_ERROR_COMPONENT_INTERRUPTED = 0x4005;
	public final static int SYSTEM_ERROR_TOP = 0x4FFF;	
	
	public final static int FILESYSTEM_ERROR_DEFAULT = 0x4010;
	public final static int FILESYSTEM_ERROR_NOT_A_KNOWN_TYPE = 0x4011;
	public final static int FILESYSTEM_ERROR_BAD_PATH = 0x4012;
	public final static int FILESYSTEM_ERROR_FILE_NOT_FOUND = 0x4013;
	public final static int FILESYSTEM_ERROR_ACCESS_DENIED = 0x4015;
	public final static int FILESYSTEM_ERROR_FILE_WONT_DELETE = 0x4016;
	public final static int FILESYSTEM_ERROR_CONFUSED = 0x4017;
	public final static int FILESYSTEM_ERROR_FAILED_DIRECTORY_OPERATION = 0x4020;
	public final static int FILESYSTEM_ERROR_COULD_NOT_DELETE = 0x4021;
	public final static int FILESYSTEM_ERROR_COULD_NOT_REPLACE = 0x4022;
	public final static int FILESYSTEM_ERROR_DELETE_FAILED = 0x4023;
	
	public final static int PROCESS_ERROR_DEFAULT = 0x4030;	
	public final static int PROCESS_ERROR_ID_NULL = 0x4035;
	public final static int PROCESS_ERROR_NOT_FOUND = 0x4035;	
	public final static int PROCESS_ERROR_BAD_CALL = 0x4036;	
	public final static int PROCESS_ERROR_FINALIZATION_FAILED = 0x4037;	
	
	public final static int PROCESS_ERROR_PROCESS = 0x4050;
	public final static int PROCESS_ERROR_CREDENTIAL_FAILED = 0x4055;
	
	public final static int PROCESSING_ERROR_GENERAL = 0x4070;
	public final static int PROCESSING_ERROR_TEXT_GENERAL = 0x4071;
	public final static int PROCESSING_ERROR_MERGE = 0x4072;	
	public final static int PROCESSING_ERROR_MERGE_INCOMPLETE = 0x4073;	
	
	public final static int PROCESSING_ERROR_SETUP = 0x4080;
	public final static int PROCESSING_ERROR_DEFINITION = 0x4081;
	
	public final static int IO_CONDUIT_ERROR_POSTED_TO_NO_DRAINS = 0x40A0;
	
	public final static int UNIVERSE_ERROR_DEFAULT = 0x4100;
	public final static int UNIVERSE_ERROR_REGISTRATION_BAD = 0x4101;
	public final static int UNIVERSE_ERROR_REGISTRATION_NOT_ALLOWED = 0x4102;
	public final static int UNIVERSE_ERROR_REGISTRATION_DUPLICATE = 0x4103;
	public final static int UNIVERSE_ERROR_CONFIG_MISSING_REQUIRED = 0x4110;	
	public final static int UNIVERSE_ERROR_CONFIG_BAD_REQUIRED = 0x4120;	
	public final static int UNIVERSE_ERROR_CONFIG_BAD_VALUE = 0x4130;	
	public final static int UNIVERSE_ERROR_BAD_KEY = 0x4140;
	public final static int UNIVERSE_ERROR_OBJECT_LOCKED = 0x4150;	
	public final static int UNIVERSE_ERROR_DELETE_FAILED = 0x4160;	
	public final static int UNIVERSE_ERROR_OBJECT_DOESNT_EXIST = 0x4170;
	public final static int UNIVERSE_ERROR_IO_PROBLEM = 0x4180;
	public final static int UNIVERSE_ERROR_DISALLOWED_NAME = 0x4190;
	public final static int UNIVERSE_ERROR_EMPTY_NAME = 0x4195;
	public final static int UNIVERSE_ERROR_GET_MODIFIED_DATE_FAILED = 0x4196;
	public final static int UNIVERSE_ERROR_GET_MATCHES = 0x4197;
	
	public final static int TEST_ERROR_DEFAULT = 0x4200;
	public final static int TEST_ERROR_COULD_NOT_PREP_TEST = 0x4201;
	public final static int TEST_ERROR_TEST_IMPL_PROBLEM = 0x4202;
	public final static int TEST_ERROR_FAIL = 0x4203;	
	public final static int TEST_ERROR_ABORT = 0x4204;	
	public final static int TEST_ERROR_GROUP_ERROR= 0x4205;	
	
	public final static int CONFIGURATION_FAILED_DEFAULT = 0x4300;	
	public final static int CONFIGURATION_FAILED_CHECKPOINT = 0x4301;
	public final static int CONFIGURATION_ERROR_BAD_CONFIGURATION = 0x4301;
	
	public final static int	SYSTEM_COMMAND_ERROR_GENERAL = 0x4400;
	public final static int	SYSTEM_COMMAND_ERROR_MALFORMED = 0x4405;
	public final static int	SYSTEM_COMMAND_ERROR_BAD_PROCESSING = 0x4406;
	public final static int	SYSTEM_COMMAND_ERROR_ALREADY_RESPONDED = 0x4407;	
	public final static int	SYSTEM_COMMAND_ERROR_BAD_PARAMETER = 0x4408;	 
	public final static int	SYSTEM_COMMAND_ERROR_BAD_DECLARATION = 0x4409;	
	public final static int	SYSTEM_COMMAND_ERROR_NULL_PARAMETER_NAME = 0x4410;
	
	public final static int	SYSTEM_COMMAND_ERROR_TYPE_VIOLATION = 0x4411;	
	public final static int	SYSTEM_COMMAND_ERROR_SET_FAILED = 0x4412;	
	public final static int	SYSTEM_COMMAND_ERROR_INSTANCE_MISSING_REQUIRED_PARAMETER = 0x4413;	
	public final static int	SYSTEM_COMMAND_ERROR_NOT_DECLARED = 0x4414;	
	public final static int	SYSTEM_COMMAND_ERROR_NULL_COMMAND = 0x4415;	
	public final static int	SYSTEM_COMMAND_ERROR_NULL_RESPONSE_RECEIPT = 0x4418;	
	public final static int	SYSTEM_COMMAND_ERROR_ISSUANCE_FAILED = 0x4416;	
	public final static int	SYSTEM_COMMAND_ERROR_ISSUANCE_NO_RECEIPTS = 0x4417;	
	public final static int	SYSTEM_COMMAND_ERROR_COMMAND_RESPONSE_PROCESSING = 0x4418;
	public final static int	SYSTEM_COMMAND_ERROR_COMMAND_NOT_FOUND = 0x4419;
	public final static int	SYSTEM_COMMAND_ERROR_COMMAND_ALREADY_DONE = 0x4420;	
	public final static int	SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION = 0x4421;
	public final static int	SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION_NEVER = 0x4422;	
	public final static int	SYSTEM_COMMAND_ERROR_OCCURANCE_VIOLATION_ONLYONE = 0x4423;	
	public final static int	SYSTEM_COMMAND_ERROR_DATATYPE_VIOLATION_VALUE = 0x4424;	
	public final static int	SYSTEM_COMMAND_ERROR_PARAMETER_NOT_DEFINED = 0x4425;	
	public final static int	SYSTEM_COMMAND_ERROR_RESPONSE_NOT_DEFINED = 0x4425;
	
	public final static int	SYSTEM_COMMAND_ERROR_PROPERTY_PROBLEM = 0x4426;
	public final static int	SYSTEM_COMMAND_ERROR_OPERATION_FAILED = 0x4430;
	
	public final static int SYSTEM_ERROR_BAD_PROPERTY = 0x4500;
	public final static int SYSTEM_ERROR_BAD_PROPERTY_TEXT = 0x4501;
	public final static int SYSTEM_ERROR_BAD_PROPERTY_NAME_NULL = 0x4502;
	public final static int SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SET = 0x4503;
	public final static int SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_SINGLE = 0x4504;
	public final static int SYSTEM_ERROR_OPTIONAL_PROPERTY_NOT_SINGLE = 0x4505;
	public final static int SYSTEM_ERROR_BAD_PROPERTY_NOT_DEFINED = 0x4506;
	public final static int SYSTEM_ERROR_BAD_PROPERTY_WRITE = 0x4507;
	public final static int SYSTEM_ERROR_REQUIRED_PROPERTY_NOT_AN_INTEGER = 0x4508;
	
	public final static int SYSTEM_ERROR_MESSAGE_ENCODING_FAILED = 0x4510;
	public final static int SYSTEM_ERROR_ATTRIBUTE_CODEC_FAILED = 0x4515;
	
	public final static int SYSTEM_ERROR_LOGGING_FAILED = 0x4520;
	public final static int SYSTEM_ERROR_LOGGING_FAILED_ON_CONDUIT = 0x4521;
	public final static int SYSTEM_ERROR_LOGGING_FAILED_ON_RESOURCE_NOT_READY = 0x4522;
	public final static int SYSTEM_ERROR_LOGGING_LOG_LEVEL_INVALID = 0x4525;
	public final static int SYSTEM_ERROR_LOGGING_LOGGER_ALREADY_ISSUED = 0x4526;
	
	public final static int RESOURCE_ERROR_GENERAL = 0x4555;	
	public final static int RESOURCE_ERROR_ALREADY_DISPOSED = 0x4551;	
	public final static int RESOURCE_ERROR_ALREADY_REGISTERED = 0x4552;	
	public final static int RESOURCE_ERROR_BAD_REGISTRATION = 0x4553;	
	public final static int RESOURCE_ERROR_IDNAMED_USED = 0x4554;	
	public final static int RESOURCE_ERROR_DURING_REGISTRATION = 0x4555;	
	public final static int RESOURCE_ERROR_DURING_LOOKUP = 0x4556;
	public final static int RESOURCE_ERROR_RESOURCE_NOT_REGISTERED = 0x4557;

	public final static int SYSTEM_INFRA_ERROR = 0x4600;	
	public final static int SYSTEM_INFRA_ATTR_CODEC = 0x4601;		
	public final static int SYSTEM_INFRA_BAD_DATA = 0x4610;	
	public final static int SYSTEM_INFRA_BAD_DATA_ODD = 0x4611;	
	public final static int SYSTEM_INFRA_NULLED_DATA = 0x4614;	
	
	public final static int SYSTEM_CALL_ERROR_GENERAL = 0x4800;	
	public final static int SYSTEM_CALL_ERROR_BAD_CALL = 0x4801;
	public final static int SYSTEM_CALL_ERROR_GET_SYSTEM_LOGGER = 0x4810;
	public final static int SYSTEM_CALL_ERROR_GET_LOCAL_PROPERTIES = 0x4811;
	public final static int SYSTEM_CALL_ERROR_GET_CONFIG_PROPERTIES = 0x4812;
	public final static int SYSTEM_CALL_ERROR_RUN_THING = 0x4813;
	public final static int SYSTEM_CALL_ERROR_LOAD_THING = 0x4814;
	public final static int SYSTEM_CALL_ERROR_LOAD_MODULE = 0x4815;
	public final static int SYSTEM_CALL_ERROR_GET_EXPRESSOR = 0x4816;
	public final static int SYSTEM_CALL_ERROR_GET_LOCAL_LOG = 0x4817;
	
	public final static int SYSTEM_LOADER_ERROR_CANT_FIND_THING = 0x4820;
	public final static int SYSTEM_LOADER_ERROR_UNIVERSE_PROBLEM = 0x4821;
	public final static int SYSTEM_LOADER_ERROR_BAD_THING_NAME = 0x4822;
	public final static int SYSTEM_LOADER_ERROR_COMPILE_ERRORS = 0x4823;
	public final static int SYSTEM_LOADER_ERROR_COULD_NOT_LOAD = 0x4824;
	
	public final static int SYSTEM_SERVICE_ERROR_COMMAND_GENERAL = 0x4850;
	public final static int SYSTEM_SERVICE_ERROR_COMMAND_MALFORMED = 0x4851;	
	
	public final static int DATA_ERROR_GENERAL = 0x4900;
	public final static int DATA_ERROR_INDEX_OUTOFBOUNDS = 0x4901;
	public final static int DATA_ATTRIBUTE_OBJECT_NULL = 0x4910;
	public final static int DATA_ERROR_PROPERTY_PATH_NULL = 0x4920;
	public final static int DATA_ERROR_PROPERTY_VALUE_NULL = 0x4921;
	public final static int DATA_ERROR_PATH_NULL = 0x4922;
	public final static int DATA_ERROR_PROPERTY_ENCODING_PROBLEM = 0x4923;
	public final static int DATA_ERROR_PROPERTY_DECODING_PROBLEM = 0x4924;
	public final static int DATA_ERROR_PARSING_ERROR = 0x4925;
	public final static int DATA_ERROR_CANNOT_BE_NULL_OR_EMPTY = 0x4926;
	public final static int DATA_ERROR_BRANCH_PROCESSING_ERROR = 0x4927;
	public final static int DATA_ERROR_PROPERTY_EMPTY_PLY = 0x4928;
	public final static int DATA_ERROR_PROPERTY_BAD_PLY = 0x4929;
	public final static int DATA_ERROR_PROPERTY_FAILED_VALIDATION = 0x4930;
	
	public final static int DATA_ERROR_EXPECTED_NUMBER= 0x4930;
	public final static int DATA_ERROR_BAD_DATE_FORMAT = 0x4931;
	
	public final static int DATA_ERROR_MATCHER_GENERAL = 0x4940;
	public final static int DATA_ERROR_MATCHER_CANNOT_ADD = 0x4941;
	public final static int DATA_ERROR_MATCHER_PATTERN_NOT_ADDED = 0x4942;
	public final static int DATA_ERROR_MATCHER_FAILED = 0x4943;
	
	public final static int KERNEL_ERROR_DEATH_NOTICE_FAILED = 0x4950;
	
	public final static int VERBOSE_FAILED_TO_START = 0x4981;	
	
	public final static int ACCESS_ERROR_NULL_ITEM = 0x4990;
	public final static int ACCESS_ERROR_CONCURRENT_NOT_ALLOWED = 0x4991;
	public final static int ACCESS_ERROR_CANNOT_OPEN = 0x4992;
	public final static int ACCESS_ERROR_STREAM_NOT_RECOGNIZED = 0x4993;
	
	public final static int ERROR_THING_ERROR = 0x4A00;
	public final static int ERROR_THING_RESULT_NOT_DEFINED = 0x4A01;
	public final static int ERROR_THING_RESULT_CALCULATION_FAILED = 0x4A02;
	public final static int ERROR_THING_CALL_FAILED = 0x4A10;
	public final static int ERROR_THING_CALL_IMPLEMENTATION_NOT_FOUND = 0x4A11;
	public final static int ERROR_THING_REQUIRED_PROPERTY_MISSING = 0x4A12;
	public final static int SYSTEM_FAULT_THING_DIED = 0x4A13;
	public final static int ERROR_THING_RUN_FAILED = 0x4A14;
	public final static int ERROR_THING_BAD_VALUE = 0x4A15;
	
	public final static int ERROR_THING_CLUSTER = 0x4A25;
	public final static int ERROR_THING_CLUSTER_NODE_ALREADY_DEFINED = 0x4A26;
	public final static int ERROR_THING_CLUSTER_NODE_START_ERROR = 0x4A27;
	public final static int ERROR_THING_CLUSTER_CHECK_FAILED = 0x4A28;
	
	public final static int ERROR_THING_EXPRESSION_DEFAULT_ERROR = 0x4A50;
	public final static int ERROR_THING_RESULT_SPURIOUS_ERROR = 0x4A51;
	
	public final static int ERROR_THING_EXPRESSION_COULD_NOT_SET = 0x4A55;
	
	public final static int PROCESSOR_HTTPHEADER_FAILED = 0x4B10;
	public final static int PROCESSOR_HTTPREQUEST_FAILED = 0x4B11;
	
	public final static int BAD_DSN = 0x4C20;
	
	// 0x5000 - module error <br>
	public final static int MODULE_ERROR = 0x5000;
	
	public final static int ERROR_MODULE_IMPLEMENTATION_NOT_FOUND = 0x5001;
	public final static int ERROR_MODULE_INSTANTIATION_FAILED = 0x5002;
	
	public final static int SERVICE_CLI_BAD_COMMAND_LINE = 0x5100;
	
	public final static int COMMANDLINE_ERROR_MISSING_REQUIRED = 0x5110;
	public final static int COMMANDLINE_ERROR_NAME_NULL = 0x5111;
	public final static int COMMANDLINE_ERROR_MISSING_REQUIRED_ASSUMED = 0x5112;
	
	public final static int GENERAL_PARSER_ERROR = 0x5180;
	
	public final static int MODULE_BUILTIN_UPA_SAVE_FAILED = 0x5210;
	public final static int MODULE_BUILTIN_UPA_LOAD_FAILED = 0x5211;
	
	public final static int SERVICE_HTTPTOOL_ERROR = 0x5200;
	public final static int SERVICE_HTTPTOOL_ACTION_LOAD_FAILED = 0x5201;
	public final static int SERVICE_HTTPTOOL_PAGE_MERGE_FAILED = 0x5202;
	public final static int SERVICE_HTTPTOOL_PAGE_MERGE_FAILED_USAGE = 0x5203;
	public final static int SERVICE_HTTPTOOL_PAGE_LOAD_FAILED = 0x5204;
	public final static int SERVICE_HTTPTOOL_ERROR_UNKNOWN_METHOD = 0x5205;
	public final static int SERVICE_HTTPTOOL_PAGE_RENDER_FAILED = 0x5206;
	public final static int SERVICE_HTTPTOOL_BAD_REQUEST = 0x5207;
	public final static int SERVICE_HTTPTOOL_CONVERSION_ERROR = 0x5208;
	public final static int SERVICE_HTTPTOOL_MANAGE_HEAD_FAILED = 0x5209;
	public final static int SERVICE_HTTPTOOL_RESPONSE_FAILED = 0x5210;
	public final static int SERVICE_HTTPTOOL_SERVE_FAILED = 0x5211;
	public final static int SERVICE_HTTPTOOL_ACTIONIZER_DECLARATION_FAILED = 0x5212;
	public final static int SERVICE_HTTPTOOL_ACTIONIZER_BAD_DECLARE = 0x5213;
	
	public final static int SMTPCLIENT_ERROR_ALREADY_CONNECTED = 0x5230;
	public final static int SMTPCLIENT_ERROR_LOGIN_FAILED = 0x5231;
	public final static int SMTPCLIENT_ERROR_MAILFROM_FAILED = 0x5232;
	public final static int SMTPCLIENT_ERROR_RCPTTO_FAILED = 0x5233;
	public final static int SMTPCLIENT_ERROR_SEND_FAILED = 0x5234;
	public final static int SMTPCLIENT_ERROR_SEND_TIMEOUT = 0x5235;
	
	public final static int CRAWLER_ERROR_DURING_SETUP = 0x5250;
	public final static int CRAWLER_ERROR = 0x5251;
	
	public final static int SERVICE_THINGER_ERROR = 0x5300;
	public final static int SERVICE_THINGER_BAD_CONSTRUCTION = 0x5301;
	
	public final static int ACTORSERVICE_ERROR = 0x5400;	
	public final static int ACTORSERVICE_PROTOCOL_ERROR = 0x5401;	
	public final static int ACTORSERVICE_LINK_ERROR = 0x5402;	
	public final static int ACTORSERVICE_SEND_ERROR = 0x5403;	
	
	public final static int DECOMPOSER_LOOKUP_ERROR = 0x5420;	
	
	public final static int PROXY_ERROR = 0x5500;	
	public final static int PROXY_SESSION_ERROR = 0x5501;	
	public final static int PROXY_CONNECTION_ERROR = 0x5501;
	
	// 0x6000 - user error <br>
	public final static int USAGE_ERROR = 0x6001;
	public final static int USER_EXPRESSION_FAILED = 0x6100;
	public final static int USER_COMMAND_ERROR = 0x6110;
	
	public final static int USER_UNSUPPORTED_FUNCTION_ERROR = 0x6120;
	
	public final static int USER_BAD_CONFIGURATION_ERROR = 0x6125;
	
	public final static int USER_ERROR_RESERVED_END = 0x61FF;
	
	public final static int ERROR_TOP = 0x6FFF;	
	
	// ===================================================================
	// == WARNING  =======================================================
	public final static int WARNING = 0x7000;
	
	// 0x7000 - system warning <br>
	public final static int	SYSTEM_COMMAND_WARNING_GENERAL = 0x7400;
	public final static int	SYSTEM_COMMAND_WARNING_NO_ONE_LISTENING = 0x7401;

	
	// 0x8000 - module warning <br>
	public final static int PROCESSOR_HTTPHEADER_UNHANDLED = 0x8010;
	public final static int PROCESSOR_HTTP_GHOSTED_PROCESS = 0x8020;
	
	// 0x9000 - user warning <br>
	
	public final static int USER_WARNING_RESERVED_END = 0x90FF;
	
	public final static int WARNING_TOP = 0x9FFF;
	
	// ===================================================================
	// == INFO  ==========================================================	
	public final static int INFO = 0xA000;
	
	// 0xA000 - system info <br>
	
	public final static int SYSTEM_SERVICE_NOTIFICATION = 0xA000;	
	public final static int SYSTEM_SERVICE_DONE = 0xA001;		
	public final static int SYSTEM_SERVICE_RESPONSE_TIMEOUT = 0xA002;
	
	public final static int KERNEL_PROCESS_DONE = 0xA010;
	public final static int KERNEL_PROCESS_FINALIZATION = 0xA011;
	public final static int KERNEL_PROCESS_INTERRUPTED = 0xA012;
	public final static int KERNEL_PROCESS_STARTED = 0xA013;
	public final static int KERNEL_PROCESS_THING_STARTED = 0xA014;
	public final static int KERNEL_PROCESS_THING_WAITING_START = 0xA015;
	
	public final static int THING_CLUSTER_NODE_STARTED = 0xA100;	
	
	// 0xB000 - module info <br>
	public final static int SERVICE_HTTPTOOL_OK = 0xB050;

	public final static int SMTPCLIENT_SEND_OK = 0xB060;
	public final static int SMTPCLIENT_BENIGN_DISCONNECT_ERROR = 0xB061;
	public final static int SMTPCLIENT_INTERRUPTED_AND_QUITTING = 0xB062;
	
	public final static int SERVICE_ACTOR_OK = 0xB070;
	public final static int SERVICE_ACTOR_CONNECTION_ACCEPTED = 0xB071;
	
	public final static int SERVICE_PROXY_ACCEPT = 0xB101;
	public final static int SERVICE_PROXY_SR_COMPLETE = 0xB102;
	public final static int SERVICE_PROXY_CONTEXT_START_PROCESS = 0xB103;
	
	// 0xC000 - user info <br>
	public final static int USER_DEFAULT_INFO	=	0xC000;
	public final static int USER_RESULT_COMPLETE = 0xC001;
	public final static int USER_RESULT_DEFAULT = 0xC002;
	public final static int USER_RESULT_ERRORED = 0xC003;
	public final static int USER_THING_MANAGEMENT = 0xC010;
	
	public final static int USER_INFO_RESERVED_END = 0xC0FF;
	
	public final static int INFO_TOP = 0xCFFF;
	
	// ===================================================================
	// == DEBUG  =========================================================	
	public final static int DEBUG = 0xA000;
	
	// 0xD000 - system debug <br>
	public final static int SYSTEM_DEBUG = 0xD000;	
	public final static int SYSTEM_DEBUG_COMMAND = 0xD001;		
	
	public final static int DEBUG_THING_CALL_GENERAL = 0xD100;		
	public final static int DEBUG_THING_CALL_SETUP = 0xD101;
	public final static int DEBUG_THING_CALL_ENTER = 0xD102;	
	public final static int DEBUG_THING_CALL_DONE = 0xD103;	
	public final static int DEBUG_THING_CALL_DONE_EXCEPTION = 0xD104;	
	public final static int DEBUG_THING_CALL_DONE_INTERRUPTION = 0xD105;
	
	public final static int DEBUG_THING_ECLIPSE_PARENT_EXPRESSOR = 0xD111;
	
	public final static int DEBUG_THING_RUN_GENERAL = 0xD110;		
	public final static int DEBUG_THING_RUN_DONE_EXCEPTION = 0xD111;			
	
	public final static int DEBUG_THING_MODULE_INSTANCE = 0xD110;
	
	public final static int DEBUG_THING_LOADED = 0xD120;
	
	public final static int DEBUG_PROPERTY_VALUE = 0xD200;
	
	// 0xE000 - module debug <br>
	public final static int DEBUG_MODULE_SETUP = 0xE010;
	public final static int DEBUG_MODULE_LOADED = 0xE011;
	
	public final static int DEBUG_SMTP_RETRIABLE_ERROR = 0xE111;
	
	// 0xF000 - user debug <br>
	
	public final static int USER_DEBUG_RESERVED_END = 0xF0FF;

	public final static int DEBUG_TOP = 0xFFFE;
	
	// ===================================================================
	// == ALL  =========================================================	
	public final static int ALL = 0xFFFF;

}