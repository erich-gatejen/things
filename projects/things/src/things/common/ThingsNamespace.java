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
 * Things name definitions.  These should not collide with the {@link things.thinger.SystemNamespace thinger} names.
 * @see things.thinger.SystemNamespace
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 15 NOV 02
 * Updated constantly through the project.
 * </pre> 
*/
public interface ThingsNamespace {

	// ===========================================================================================
	// == VALUES
	public static final String VALUE_OK = "OK";
	
	
	// ===========================================================================================
	// == ATTRIBUTES
	
	// General
	public static final String ATTR_MESSAGE = "message";
	public final static String ATTR_ID = "id";
	public final static String ATTR_STATE = "state";
	public final static String ATTR_ACTUAL_STATE = "state.actual";
	
	public static final String ATTR_LINE_NUMERIC = "numeric";
	public static final String ATTR_LINE_NUMERIC_CAUSE = "numeric.cause";
	
	public static final String ATTR_LINE_NUMBER = "line.number";
	public static final String ATTR_LINE_VALUE = "line.value";
	
	public final static String ATTR_RETRIES = "retries";
	
	// Data
	public static final String ATTR_DATA_TYPE = "data.type";
	public static final String ATTR_DATA_INDEX = "data.index";		
	public static final String ATTR_DATA_INDEX_OFFSET = "data.index.offset";	
	public static final String ATTR_DATA_SIZE = "data.size";	
	public static final String ATTR_DATA_TARGET = "data.target";	
	public static final String ATTR_DATA_INDEX_BOUNDS = "data.index.bounds";
	public static final String ATTR_DATA_INDEX_BOUNDS_START = "data.index.bounds.start";
	public static final String ATTR_DATA_INDEX_BOUNDS_END = "data.index.bounds.end";
	public static final String ATTR_DATA_ATTRIBUTE_NAME = "data.attribute.name";	
	public static final String ATTR_DATA_ATTRIBUTE_VALUE = "data.attribute.value";	
	public static final String ATTR_DATA_ATTRIBUTE_VALUE_COUNT = "data.attribute.value.count";	
	public static final String ATTR_DATA_ARGUMENT = "data.argument";	
	public static final String ATTR_DATA_VALUE = "data.value";
	
	public static final String ATTR_DATA_CLASSIFICATION = "data.classification";
	public static final String ATTR_DATA_CLASSIFICATION_CHAR = "data.classification.char";
	
	public static final String ATTR_DATA_RECEIPT = "data.receipt";
	public static final String ATTR_DATA_RECEIPT_TYPE = "data.receipt.type";
	public static final String ATTR_DATA_RECEIPT_NOTE = "data.receipt.note";
	public static final String ATTR_DATA_RECEIPT_STAMP = "data.receipt.stamp";
	
	public static final String ATTR_PLATFORM_TRACE = "platform.trace";
	public static final String ATTR_PLATFORM_CLASS = "platform.class";	
	public static final String ATTR_PLATFORM_CLASS_ACTUAL = "platform.class.actual";	
	public static final String ATTR_PLATFORM_CLASS_PROPOSED = "platform.class.proposed";	
	public static final String ATTR_PLATFORM_MESSAGE = "platform.message";
	public static final String ATTR_PLATFORM_MESSAGE_COMPLETE = "platform.message.complete";
	public static final String ATTR_PLATFORM_MESSAGE_ORIGINAL = "platform.message.original";
	public static final String ATTR_PLATFORM_MESSAGE_COMPLETE_ORIGINAL = "platform.message.complete.original";
	
	public static final String ATTR_PLATFORM_FILE_NAME = "file.name";
	public static final String ATTR_PLATFORM_FILE_PATH = "file.path";

	public static final String ATTR_PROPERTY_NAME = "property.name";
	public static final String ATTR_PROPERTY_VALUE = "property.value";	
	public static final String ATTR_PROPERTY_PATH = "property.path";
	public static final String ATTR_PROPERTY_ENTRY = "property.entry";	
	public static final String ATTR_PROPERTY_VALIDATION = "property.validation";	
	
	public static final String ATTR_PROPERTY_TRUNK_MODE = "property.turnk.mode";		
	
	public static final String ATTR_STATE_VALUE = "state.value";
	
	public static final String ATTR_UNIVERSE_NAME = "universe.name";
	public static final String ATTR_UNIVERSE_ID = "universe.id";
	public static final String ATTR_UNIVERSE_PATH = "universe.path";
	public static final String ATTR_UNIVERSE_UADDY = "universe.uaddy";

	public static final String ATTR_REASON_NUMBER = "reason.";
	
	public static final String ATTR_SEQUENCE_NUMBER = "sequence.number";
	public static final String ATTR_SEQUENCE_NUMBER_EXPECTED = "sequence.number.expected";
	public static final String ATTR_SEQUENCE_NUMBER_ACTUAL = "sequence.number.actual";

	public static final String ATTR_THING_NAME = "thing.name";
	public static final String ATTR_THING_PROCESS_ID = "thing.process.id";
	public static final String ATTR_THING_NAME_ACTUAL = "thing.name.actual";
	public static final String ATTR_THING_PROPOSED = "thing.name.proposed";
	public static final String ATTR_THING_RESULT_NAME = "thing.result.name";
	public static final String ATTR_THING_RESULT_DESCRIPTION = "thing.result.description";
	public static final String ATTR_THING_RESULT_TYPE = "thing.result.type";
	
	public static final String ATTR_THING_CLUSTER_NODE_NAME = "thing.cluster.node.name";
	
	public static final String ATTR_MODULE_NAME = "module.name";
	
	public static final String ATTR_METRIC_COUNT = "metric.count";
	public static final String ATTR_METRIC_MILLIS = "metric.millis";
	public static final String ATTR_METRIC_DIFFERENCE = "metric.difference";
	public static final String ATTR_METRIC_CASES = "metric.cases";
	public static final String ATTR_METRIC_PASS = "metric.pass";
	public static final String ATTR_METRIC_FAIL = "metric.fail";
	public static final String ATTR_METRIC_INCONCLUSIVE = "metric.inconclusive";
	public static final String ATTR_METRIC_ABORT = "metric.abort";
	public static final String ATTR_METRIC_EXCEPTION = "metric.exception";
	
	public static final String ATTR_PARAMETER_NAME = "parameter.name";
	public static final String ATTR_PARAMETER_VALUE = "parameter.value";
	public static final String ATTR_PARAMETER_HELP = "parameter.help";
	public static final String ATTR_PARAMETER_ITEM_NUMBER = "parameter.item.number";
	public static final String ATTR_PARAMETER_ITEM_NUMBER_EXPECTED = "parameter.item.number.expected";
	public static final String ATTR_PARAMETER_META = "parameter.meta";	
	
	public final static String ATTR_PROCESSING_HTTP_ACTION = "http.action";
	public final static String ATTR_PROCESSING_HTTP_METHOD = "http.method";	
	public final static String ATTR_PROCESSING_HTTP_PAGE = "http.page";
	public final static String ATTR_PROCESSING_HTTP_PAGE_LOCATION = "http.page.location";
	public final static String ATTR_PROCESSING_HTTP_PATH = "http.path";
	
	public static final String ATTR_ERROR_PROCESSING = "error.processing";
	public static final String ATTR_ERROR_COMPILATION = "error.compilation";
	
	public final static String ATTR_TRANSPORT_ADDRESS = "transport.address";
	public final static String ATTR_TRANSPORT_PORT = "transport.port";
	public final static String ATTR_TRANSPORT_CLIENT_NAME = "transport.client.name";
	public final static String ATTR_TRANSPORT_CODE = "transport.code";
	public final static String ATTR_TRANSPORT_SENT = "transport.sent";
	public final static String ATTR_TRANSPORT_SENT_SIZE = "transport.sent.size";
	public final static String ATTR_TRANSPORT_SENT_SIZE_ACTUAL = "transport.sent.size.actual";
	public final static String ATTR_TRANSPORT_SEQUENCE = "transport.sequence";
	
}
