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
package things.thinger.service.local;

import things.data.Receipt;
import things.thinger.service.command.CommandResponse.CompletionDisposition;

/**
 * Static tools for dealing with CLIService data.  These can be used outside the server itself.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 1 SEP 07
 * </pre> 
 */
public class CLIServiceTools implements CLIServiceConstants {
	
	// ===================================================================================================
	// EXPOSED DATA

	/*
	 * The character used to separate tokens in a formatted response.
	 */
	public final static char RESPONSE_TOKEN_SEPERATOR = ':';
	public final static String RESPONSE_SINGLE_SEPERATOR = "::";
	public final static String RESPONSE_MULTI_SEPERATOR = ":>";
	public final static String RESPONSE_LEADER = " >";

	
	// ===================================================================================================
	// RESPONSE INFO
	// 
	// Format: (Command Name):(Result):(Receipt)::(Text):  				= single line of text
	//         (Command Name):(Result):(Receipt):>(Text):(Additional)	= multi line of text
	
	/**
	 * The classification of responses.
	 */
	public enum Result {
		OK,
		FAIL,
		DYING,
		FAULT;
	}
	
	/**
	 * The specific responses.
	 */
	public enum Responses {
	
		FAIL_BAD_COMMAND_PARSE(Result.FAIL, "Could not parse command."),
		FAIL_BAD_COMMAND(Result.FAIL, "Bad command."),
		FAIL_BAD_UNKNOWN_COMMAND(Result.FAIL, "Unknown command."),
		FAIL_BAD_COULD_NOT_CREATE(Result.FAIL, "Could not create command."),
		FAIL_BAD_COULD_NOT_ISSUE(Result.FAIL, "Could not issue command."),
		FAIL_BAD_GENERAL_PROBLEM(Result.FAIL, "General failure."),
		FAIL_EXECUTION(Result.FAIL, "Failed execution."),
		OK_NO_COMMAND(Result.OK, "Command ok, but does nothing."),
		OK_BUT_NOT_DONE(Result.OK, "Command ok but not done."),
		OK(Result.OK, "OK."),
		DYING(Result.DYING, "Dying."),
		FAULT(Result.FAULT, "Serious problem.");
	
		// Enumeration Data 
        private final Result theClass;    
        private final String stockText;  
        private Responses(Result theClass, String stockText) { this.theClass = theClass; this.stockText = stockText;}
        
        // --- Tools ---------------------------------------
     
        /**
         * Format the response.
         * @param commandName the command name.
         * @param finalReceipt the final receipt for the command.  If it is null, that it put nothing.
         * @param text The text output.  If null, it will use the stock text.
         * @param additional The additional text.  If null, it will be a single line of text.
         * @return the formatted response string.
         */
        public String format(String commandName, Receipt finalReceipt, String text, String additional) {
        	String recieptText = "";
        	String actualText = text;
        	if ((text==null)||(text.length()<1)) {
        		if (finalReceipt==null) 
        			actualText = "NOT ISSUED";
        		else
        			actualText = finalReceipt.getNote();
        	}
        	if (finalReceipt!=null) recieptText = finalReceipt.getType().name();
        	if (additional==null) {
        		return commandName + RESPONSE_TOKEN_SEPERATOR + this.theClass.toString() + RESPONSE_TOKEN_SEPERATOR + recieptText + RESPONSE_SINGLE_SEPERATOR + actualText + RESPONSE_TOKEN_SEPERATOR;
        	} else {
          		return commandName + RESPONSE_TOKEN_SEPERATOR + this.theClass.toString() + RESPONSE_TOKEN_SEPERATOR + recieptText + RESPONSE_MULTI_SEPERATOR + actualText + RESPONSE_TOKEN_SEPERATOR + additional;
        	}
        }
        
        /**
         * Format the response, simple.
         * @param commandName the command name.
         * @return the formatted response string.
         */
        public String format(String commandName) {
        	return commandName + RESPONSE_TOKEN_SEPERATOR + this.theClass.toString() + RESPONSE_TOKEN_SEPERATOR + "" + RESPONSE_SINGLE_SEPERATOR + stockText;
        }

	}
	
	// ============================================================================================================
	// ============================================================================================================
	// TOOLS
	
    /**
     * Get the appropriate response given the disposition.  
     * @param cd the complesion disposition.
     * @return the responses value
     */
    public static Responses getResponse(CompletionDisposition cd) {
    	switch(cd) {
    	case OPEN:
    		return Responses.OK_BUT_NOT_DONE;
    	case GOOD:
    		return Responses.OK;    		
    	case BAD:
    		return Responses.FAIL_EXECUTION;
    	default:
    		// This should never happen.
    		return Responses.FAULT;
    	}
    }
    
    /**
     * Get a complete response string.
     * @param cd the completion disposition.
     * @param commandName the textual command name
     * @param finalReceipt the final receipt.  It can be null.
     * @param text the text message.  It can be null.
     * @param additional any additional response text.  It cna be null.
     * @return the full and complete response text.
     */
    public static String getCompleteResponse(CompletionDisposition cd, String commandName, Receipt finalReceipt, String text, String additional) {
    	Responses theProperResponse = getResponse(cd);
    	String response = theProperResponse.format(commandName, finalReceipt, text, additional);
    	return response;
    }
	
	// ============================================================================================================
	// ============================================================================================================
	// EXTERNAL TOOLS.  These are in no way optimized and should really only be used for testing or 
    // other external use.  Note that the text part of the responses can break these.
    
    /**
     * Is the command response indicate it's complete and ok. 
     * @param responseString the response string.
     * @return true if complete and ok, otherwise false (including if this method fails).
     */
    public static boolean isResponseOkAndComplete(String responseString) {
    	try {
    		if ( (responseString != null) && (getResultFromResponse(responseString) == Result.OK) 
    				&& (getReceiptFromResponse(responseString).isCompleteAndOk()) ) return true;
    	} catch (Throwable t) {
    		// Don't care.  We'll return false.
    	}
		return false;
    }
    
    
    /**
     * Get the Command Name from the response.  If it can't, it'll just return null.
     * @param responseString the string to examine.
     * @return the command name or null.
     */
    public static String getCommandNameFromResponse(String responseString) {
    	if (responseString == null) return null;
    	int rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR);
    	if (rover > 0) return responseString.substring(rover);
    	return null;
    }
    
    /**
     * Get the Result from the response.  If it can't, it'll just return null.
     * @param responseString the string to examine.
     * @return the Result or null.
     */
    public static Result getResultFromResponse(String responseString) {
    	Result result = null;
    	try {
	    	int rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR);
	    	int rover2 = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+1);
	    	result = Result.valueOf(responseString.substring(rover+1, rover2));
	    	
    	} catch (Throwable t) {
    		// Just let the null return.
    	}
    	return result;
    }
    
    /**
     * Get the Receipt Type from the response.  If it can't, it'll just return null.
     * @param responseString the string to examine.
     * @return the Receipt Type or null.
     * @see things.data.Receipt
     */
    public static Receipt.Type getReceiptFromResponse(String responseString) {
    	Receipt.Type result = null;
    	try {
	    	int rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR);
	    	rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+1);
	    	int rover2 = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+1);
	    	String stuff = responseString.substring(rover+1, rover2);  // For debugging
	    	result = Receipt.Type.valueOf(stuff);
	    	
    	} catch (Throwable t) {
    		// Just let the null return.
    	}
    	return result;
    }
    
    /**
     * Get the Receipt Type from the response.  If it can't, it'll just return null.
     * @param responseString the string to examine.
     * @return the Text or null.
     * @see things.data.Receipt
     */
    public static String getTextFromResponse(String responseString) {
    	String result = null;
    	try {
    		// (Command Name):(Result):(Receipt)::(Text):
	    	int rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR);
	    	rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+1);
	    	rover = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+1);
	    	int rover2 = responseString.indexOf(RESPONSE_TOKEN_SEPERATOR, rover+2);
	    	result = responseString.substring(rover+2, rover2);
	    	
    	} catch (Throwable t) {
    		// Just let the null return.
    	}
    	return result;
    }
    
}
