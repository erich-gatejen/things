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
package things.testing.unit;

import java.util.Collection;
import java.util.LinkedHashMap;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.ThingsUtilityBelt;
import things.data.ThingsPropertyView;

/**
 * UNIT testing tool. This is a test.  It will run a set of test cases.  A test is implemented
 * by the following steps:<br>
 * 1- Implement the test_prepare() method.  Test cases will be DECLARED(String) by name.<br>
 * 2- Implement the test_execute() method.  Each case should be reported with a PASS(), FAIL(),
 * INCONCLUSIVE(), or EXCEPTION().  You may use ABORT() at any time to abort the remaining
 * tests.<br>
 * 3- Call the test in a TestGroup<p>
 * You should not call any other method in this class.
 * <p>
 * The result fields are readable after the test is run.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 26 JUL 04
 * EPG - Re-write - 19 JUN 05
 * </pre> 
 */
public abstract class Test {

	// TESTS MUST IMPLEMENT THE FOLLOWING TWO METHOD.
	/**
	 * Prepare for the test. Overload this with the test implementation.
	 */
	public abstract void test_prepare() throws Throwable;

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public abstract void test_execute() throws Throwable;
	
    // ==================================================================
	// RESULT FIELDS
    public int total = 0;
    public int pass = 0;
    public int fail = 0;
    public int abort = 0;
    public int inconclusive = 0;
    public int exception = 0;
    public long time;
    public long totalSize;
    public Result	result = Result.WAITING;

	// ==================================================================
	// FIELDS AND METHODS USEABLE BY THE TEST IMPLIMENTATION
	
	/**
	 * Properties passed to the test by the harness.  Feel free to use them.
	 */	
	protected ThingsPropertyView properties;
	
	/**
	 * Log a comment.  This is not a way to report results!  Use these sparingly.  It
	 * is perfectly acceptable to use a COMMENT instead.
	 * @param text
	 *            the text of the comment
	 * @throws things.common.ThingsException
	 */
	public void LOG(String text) throws ThingsException {
		try {
			logger.post(TestCommon.formTestLog(testName, "ABORT due to infrastructure problem.  message=" + text));
		} catch (ThingsException e) {
			// at this point, we're screwed
			e.panicReport();
		}		
	}
	
	/**
	 * DECLARE a test case.  This should be called by test_prepare() to define
	 * the various tests.
	 * 
	 * @param name
	 *            the name of the test.  It should be unique
	 * @throws ThingsException
	 */
	public void DECLARE(String name) throws ThingsException {
	    TestCase tc = new TestCase(name);
	    definedCases.put(name,tc);
	}

	/**
	 * PASS a test case report with a message.
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws things.common.ThingsException
	 */
	public void PASS(String name, String message) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.pass(message);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to PASS a test case that isn't defined.  test="
					+ name + "msg=" + message, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}

	/**
	 * PASS a test case report.
	 * @param name test name
	 * @throws things.common.ThingsException
	 */
	public void PASS(String name) throws ThingsException {
		PASS(name,"ok");
	}
	
	/**
	 * EXCEPTION test case report
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws ThingsException
	 */
	public void EXCEPTION(String name, String message) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.exception(message);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to EXCEPTION a test case that isn't defined.  test="
					+ name + "msg=" + message, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * EXCEPTION test case report
	 * @param name
	 * 			  test name
	 * @param theException
	 *            the exception that caused it
	 * @throws ThingsException
	 */
	public void EXCEPTION(String name, Throwable theException) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.exception(theException.getMessage());
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to EXCEPTION a test case that isn't defined.  test="
					+ name + "msg=" + theException.getMessage(), ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM,theException);	
	    }
	}
	
	/**
	 * FAIL a test case report
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws ThingsException
	 */
	public void FAIL(String name, String message) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.fail(message);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to FAIL a test case that isn't defined.  test="
					+ name + "msg=" + message, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * ABORT THIS TEST.  It will halt the test.  All unexecuted tests will be marked INCONCLUSIVE.
	 * It will still report on the test.  Do NOT catch this exception in the test_execute() 
	 * method.  Let it bubble out.
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws things.testing.unit.TestAbortException
	 */
	public void ABORT(String name, String message) throws TestAbortException, ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.abort(message);
	        throw new TestAbortException("Abort in " + name);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to ABORT a test case that isn't defined.  test="
					+ name + "msg=" + message);	
	    }
	}
	
	/**
	 * INCONCLUSIVE test case report.
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws things.common.ThingsException
	 */
	public void INCONCLUSIVE(String name, String message) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.inconclusive(message);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to INCONCLUSIVE a test case that isn't defined.  test="
					+ name + "msg=" + message, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * Set the TIME for the case.  The time should be in milliseconds.
	 * @param name
	 * 			  test name
	 * @param time
	 * 			  the time in milliseconds
	 * @throws things.common.ThingsException
	 */
	public void TIME(String name, long time) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.setTime(time);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to set TIME for a test casethat isn't defined.  test="
					+ name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}

	/**
	 * Set the SIZE for the case.  The time should be in milliseconds.
	 * @param name
	 * 			  test name
	 * @param size
	 * 			  the size
	 * @throws things.common.ThingsException
	 */
	public void SIZE(String name, long size) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.setSize(size);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to set SIZE for a test casethat isn't defined.  test="
					+ name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * Set the CODE for the case.
	 * @param name
	 * 			  test name
	 * @param code
	 * 			  the code
	 * @throws things.common.ThingsException
	 */
	public void CODE(String name, String code) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.setCode(code);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to set CODE for a test casethat isn't defined.  test="
					+ name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * Set a VALUE for the case.
	 * @param name
	 * 			  test name
	 * @param n
	 * 			  the name for the value
	 * @param v
	 *            the value
	 * @throws things.common.ThingsException
	 */
	public void VALUE(String name, String n, String v) throws ThingsException {
	    if (definedCases.containsKey(name)) {
	        TestCase tc = definedCases.get(name);
	        tc.addValue(n,v);
	    } else {
	        throw new ThingsException("Test implementation error.  Tried to set a VALUE for a test casethat isn't defined.  test="
					+ name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);	
	    }
	}
	
	/**
	 * PUNT is a simple convenience that tosses a test fail exception.  It's useful for 
	 * escaping local test case blocks.  You MUST not let this exception escape the 
	 * test_excute() method.  This is a convenience for you to handle.
	 * @param text the text of the punt
	 * @throws things.testing.unit.TestLocalException
	 */
	public static void PUNT(String text) throws TestLocalException {
	    throw new TestLocalException(text);
	}
	
	/**
	 * COMMENT into the test report
	 * @param name
	 * 			  test name
	 * @param message
	 *            information
	 * @throws ThingsException
	 */
	public void COMMENT(String name, String message) throws ThingsException {
		expresser.expressCaseSimple(ThingsUtilityBelt.timestampFormatterHHMMSS(),Result.COMMENT,depth,name,message);
	}
	
	/**
	 * Set the long name for this test.  This can be useful for more information.
	 * @param name the long name as a string.
	 */
	public void SET_LONG_NAME(String name)  {
		testLongName= name;
	}
	
	/**
	 * Get the name of this test.
	 * @return the name of this test
	 */
	public String getName() throws ThingsException {
		return testName;
	}	
	
	/**
	 * Get the long name of this test.
	 * @return the name of this test
	 */
	public String getLongName() throws ThingsException {
		return testLongName;
	}	
	
	// =====================================================================	
	// =====================================================================
	// FIELDS AND MESTHODS THAT SHOULD NOT BE TOUCHED
	private LinkedHashMap<String,TestCase>	definedCases;
	protected String testName = "unknown"; // Don't modify directly !!
	private String testLongName;
	private int depth; 				 // Don't modify directly !!
	//private String runnerName;
	private ResultExpresser expresser;
	private StringPoster	logger;
	
	/**
	 * Prepare for the test.  This will be called by the Group.  Do not
	 * call it directly.
	 * @param runnerNaming
	 *            is the name giving to the test run by the running agent
	 * @param givenName
	 *            the name given to this test.           
	 * @param expresserIn
	 *            is the ResultExpresser to use.
	 * @param log
	 * 			  a string poster used for logging non-result information.
	 * @param view
	 *            is a preperties view that will be avaible to the test
	 *            implementation
	 * @param inDepth
	 * 			  the depth of this test.
	 * @see things.data.ThingsPropertyView
	 * @throws things.common.ThingsException
	 */
	public void prepare(String runnerNaming, String givenName, ResultExpresser expresserIn,
			StringPoster log, ThingsPropertyView view, int	inDepth) throws ThingsException {

		// locals
		testName = givenName;
		testLongName = givenName;
		//runnerName = runnerNaming;
		expresser = expresserIn;
		properties = view;
		depth = inDepth;
		
		definedCases = new LinkedHashMap<String,TestCase>();

		// chain prepare
		try {
			this.test_prepare();
		} catch (Throwable ee) {
			throw new ThingsException("Could not prepare test.  test="
					+ testName + "msg=" + ee.getMessage(),
					ThingsException.TEST_ERROR_COULD_NOT_PREP_TEST, ee);
		}
	}

	/**
	 * Run the test.
	 * @return a Result for this test.
	 * @throws ThingsException
	 */
	public synchronized Result execute() throws ThingsException {
		
		// Prep local data
		long timer = System.currentTimeMillis();

		// start the timer, chain execution, stop the timer.
		try {

			// EXECUTE IT
			this.test_execute();
			time = System.currentTimeMillis() - timer;			
			result = testReport(false);	
			
		} catch (TestAbortException tae) {	
		    result = testReport(true);
		    time = System.currentTimeMillis() - timer;	    
		} catch (TestLocalException te) {
			result = testReport(true);
		    testSpuriousAbortReport(te.getMessage());
			throw new ThingsException("Test implementation error.  TestLocalException was allowed to leave a Test implementation.  test="
					+ testName,	ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM, te);	 
		} catch (ThingsException te) {
			result = testReport(true);
		    testSpuriousAbortReport(te.getMessage());
			throw (ThingsException) te;
		} catch (Throwable ee) {
			result = testReport(true);
		    testSpuriousAbortReport(ee.getMessage());
			throw new ThingsException("Test implementation error.  test="
					+ testName + "msg=" + ee.getMessage(),
					ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM, ee);	 
		} 
		return result;
	}

	// ==================================================================
	// PRIVATE METHODS

	/**
	 * TEST report.  It'll report to the poster and return the result.
	 * Each case will be expressed, but the overall test will not.
	 * @param abortFlag Force an abort report if true
	 * @return return the result for the test overall.
	 * @throws things.common.ThingsException
	 */
	private Result testReport(boolean abortFlag) throws ThingsException {
		String message;
	    // Iterate on the defined test cases
	    Collection<TestCase> tcs = definedCases.values();
	    for (TestCase tc : tcs) {
	        total++;
	        
	       message = tc.getMessage();
	       if (message==null) message = "Not run.";
	        expresser.expressCase(ThingsUtilityBelt.timestampFormatterHHMMSS(),
	        		tc.getResult(),depth+1,tc.name,message,tc.getTime(),
	        		tc.getSize(),tc.getCode(),tc.getValues());
	        if (tc.getSize()>0) totalSize += tc.getSize();
	        switch(tc.theResult) {
	    		case PASS:
	    		    pass++;
	    		    break;
	    		case FAIL:
	    		    fail++;
	    		    break;	    		    
	    		case INCONCLUSIVE:
	    		    inconclusive++;
	    		    break;	  	    		    
	    		case EXCEPTION:
	    		    exception++;
	    		    break;	  	    		    
	    		case ABORT:	 
	    		case WAITING:
	    		    abort++;
	    		    break;	  	    		    
	    		case COMMENT:
	    		    total--;
	    		   break;
	    		default:
	    		    ThingsException.softwareProblem("testReport() encountered unexpected case type.  number="+ tc.theResult);
	    			break;
	    			
	        } // end switch
	    }  // end for

	    // Result for this
		if (abortFlag) {
			result = Result.ABORT;
		} else if (pass == total) {
			result = Result.PASS;
		} else if (abort > 0) {
			result = Result.ABORT;
		} else if ((fail == 0)&&(exception ==0 )) {
			result = Result.ABORT;
		} else {
			result = Result.FAIL;
		}

		// Express the whole test
		expresser.express(ThingsUtilityBelt.timestampFormatterHHMMSS(),
						   ResultExpresser.expressionType.TEST, result, depth, testName,
						   "Complete", pass,fail,inconclusive,exception,abort,
						   time,totalSize,ResultExpresser.NO_CODE,ResultExpresser.NO_LIST);
		
		return result;
	}

	/**
	 * TEST abort report.  It'll still try to report it.
	 * @param message a message to report.  It's a good place to put an exception message.
	 */
	private void testSpuriousAbortReport(String message) {
		try {
			logger.post(TestCommon.formTestLog(testName, "ABORT due to infrastructure problem.  message=" + message));
			testReport(true);
		} catch (ThingsException e) {
			// at this point, we're screwed
			e.panicReport();
		}
	}

}