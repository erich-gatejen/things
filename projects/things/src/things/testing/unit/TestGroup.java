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
 * UNIT testing tool.  An group of tests.  Collect test suites with this.  You can
 * flag whether to throw exceptions on aborts or fails (both are on by default).
 * Aborts will always be thrown before fails, even if a condition would cause both 
 * (such as an initialization or framework problem).
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 1 AUG 04
 * </pre> 
 */
public abstract class TestGroup {

	// GROUPS MUST IMPLEMENT THE FOLLOWING TWO METHOD
	/**
	 * prepare for the group run.
	 */
	public abstract void  group_prepare() throws Throwable;

	/**
	 * Run the group. Overload this with the group implementation.
	 */
	public abstract void group_execute() throws Throwable;

	// ==================================================================
	// FIELDS AND METHODS USEABLE BY THE TEST IMPLIMENTATION
	public String groupName;
	public String groupLongName;
	public ThingsPropertyView properties;
	public String runnerName;
	public StringPoster logger;

    // ==================================================================
	// RESULT FIELDS
    public int total = 0;
    public long time;
    public int pass = 0;
    public int fail = 0;
    public int abort = 0;
    public int inconclusive = 0;
    public int exception = 0;
    public long totalSize = 0;
    public Result	result = Result.WAITING;
    
    // ==================================================================
	// USABLE GADGETS ============================================
	/**
	 * Don't let any throws or aborts escape.  this is a top-level grouping.
	 * The default is the exact opposite.  This can be called in group_prepare() or group_execute().
	 */
	public void THE_BUCK_STOPS_HERE()  {
		this.throwFails = false;
		this.throwAborts = false;
	}
	
	/**
	 * Set the Throw Exception on Test Fail on (true) or off(false). This can be called in group_prepare() or group_execute().
	 * @param flag true is on, false is off
	 */
	public void SET_THROW_FAILS(boolean flag)  {
		throwFails = flag;
	}
	
	/**
	 * Set the Throw Exception on Test Aborts on (true) or off(false). This can be called in group_prepare() or group_execute().
	 * @param flag true is on, false is off
	 */
	public void SET_THROW_ABORTS(boolean flag)  {
		throwAborts = flag;
	}
	
	/**
	 * Set the long name for this group.  This can be useful for more information.
	 * @param name the long name as a string.
	 */
	public void SET_LONG_NAME(String name)  {
		groupLongName= name;
	}
	
	/**
	 * DECLARE a test.  This should be called by group_prepare() to define
	 * the various tests.  They will then be RUN() in the group_execute().
	 * 
	 * @param name
	 *            the name of the test.  It should be unique.
	 * @param className
	 * 			  the name of the class that implements this test.            
	 * @throws ThingsException
	 */
	public void DECLARETEST(String name, String className) throws ThingsException {
	    DeadTest test = new DeadTest(name,className);
	    if ((className == null)||(className.length()<1)) throw new
    		ThingsException("Test implementation error.  group="
    			+ groupName + " declared the test " + name + " with a null or empty classname",
    			ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 
	    if (declared.containsKey(name)) throw new
	    	ThingsException("Test implementation error.  group="
	    			+ groupName + " declared " + name + " more than once.",
	    			ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 
	    declared.put(name,(Object)test);
	}
	
	/**
	 * DECLARE a group.  This should be called by group_prepare() to define
	 * the various tests.  They will then be RUN() in the group_execute().
	 * 
	 * @param name
	 *            the name of the group.  It should be unique.
	 * @param className
	 * 			  the name of the class that implements this group.
	 * @throws ThingsException
	 */
	public void DECLAREGROUP(String name, String className) throws ThingsException {
	    DeadGroup group = new DeadGroup(name,className);
	    if ((className == null)||(className.length()<1)) throw new
		ThingsException("Test implementation error.  group="
			+ groupName + " declared the group " + name + " with a null or empty classname",
			ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 	    
	    if (declared.containsKey(name)) throw new
	    	ThingsException("Test implementation error.  group="
	    			+ groupName + " declared " + name + " more than once.",
	    			ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 
	    declared.put(name,(Object)group);
	}
	
	/**
	 * Run a test class as named.
	 * @param name of the class to run
	 * @return the result
	 * @throws ThingsException
	 */
	public Result RUN(String  name) throws ThingsException {
		
		// Is it declared and runable?
		if (runnerName==null) throw new ThingsException("TestGroup not properly init()'d", ThingsException.TEST_FAULT_NOT_INITIALIZED);
		if (!declared.containsKey(name)) throw new
		ThingsException("Test implementation error.  group=" + groupName + " did not declare " + name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 
		Object declaredTest = declared.get(name);
		Test theTest = null;
	
		Result result = Result.ABORT;

		// process it
		try {
			
			if (declaredTest instanceof DeadTest) {
						
				// In case we can't load.  The DeadTest will automatically abort.
				theTest = (Test)declaredTest;
				
				// try to load it
				Class<?> t = Class.forName(  ((DeadTest)declaredTest).getClassName() );
				Object tmpTest = t.newInstance();
				if (tmpTest instanceof Test) theTest = (Test)tmpTest;
				else if (tmpTest instanceof TestGroup) throw new TestLocalException("Class is a TestGroup instead of a Test");
				else throw new TestLocalException ("Loaded an unknown class:" + t.getName());
				
			} else if ( declaredTest instanceof Test ) {
				// cast it
				theTest = (Test)declaredTest;
			} else if ( declaredTest instanceof TestGroup ) {
				// A testgroup instead of a test 
				throw new ThingsException("Test implementation error.  test=" + name + " declared a TestGroup class for implementation (instead of a Test class).", ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);
			} else {
				// not a test group at all
				throw new ThingsException("Test implementation error.  test=" + name + " did not declare a Test class for implementation.", ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);
			}
			
			// Prepare it.  Mind you, this may be re-running it.
			theTest.prepare(runnerName, name, expresser, logger, properties, depth+1);

			// Save it
			declared.put(name,theTest);	

		} catch (ThingsException te) {
			throw te;
			
		} catch (Throwable e) {
			// This is always bad
			logger.post(TestCommon.formGroupLog(name, "Aborting test(serious, could not load)=" + name + " message=")+e.getMessage());
		}
		
		
		// Run it
		try {
		    result = theTest.execute();
		} catch (TestAbortException  tae) {   
			// Really, this shouldn't happen
		    throw tae;
		} catch (TestFailException tfe) {
			// Really, this shouldn't happen
		    throw tfe;		    
		} catch (ThingsException  te) {
		    te = new TestAbortException("Spurious abort.  message=" + te.getMessage(),te);
		} 
		
		if (throwAborts && (result == Result.ABORT))  {
		    throw new TestAbortException("Test aborted. name= " + name);
		} else if (throwFails && (result == Result.FAIL)) {
		    throw new TestFailException("Test failed. name= " + name);
		}	
		return result;		
		
	}
	
	/**
	 * Run a test class as named.
	 * @param name of the class to run
	 * @throws ThingsException
	 */
	public void RUNGROUP(String  name) throws ThingsException {

		// Is it declared and runable?   It will be listed as a Ghost if it is 
		if (runnerName==null) throw new ThingsException("TestGroup not properly init()'d", ThingsException.TEST_FAULT_NOT_INITIALIZED);
		if (!declared.containsKey(name)) throw new
    			ThingsException("Test implementation error.  group=" + groupName + " did not declare " + name, ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM); 
		Object declaredGroup = declared.get(name);
		TestGroup theTestGroup = null;

		// process it
		try {
			
			if (declaredGroup instanceof DeadGroup) {
						
				// In case we can't load.  The DeadGroup will automatically abort.
				theTestGroup = (TestGroup)declaredGroup;
				
				// try to load it
				Class<?> t = Class.forName(  ((DeadGroup)declaredGroup).getClassName() );
				Object tmpTest = t.newInstance();			
				if (tmpTest instanceof TestGroup) theTestGroup = (TestGroup)tmpTest;
				else if (tmpTest instanceof Test) throw new TestLocalException("Class is a Test instead of a TestGroup");
				else throw new TestLocalException ("Loaded an unknown class:" + t.getName());
				
			} else if ( declaredGroup instanceof TestGroup ) {
				// cast it
				theTestGroup = (TestGroup)declaredGroup;
			} else if ( declaredGroup instanceof Test ) {
				// A test instead of a test group
				throw new ThingsException("Group implementation error.  group=" + groupName + " declared a Test class for implementation (instead of a TestGroup class).", ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);
			} else {
				// not a test group at all
				throw new ThingsException("Group implementation error.  group=" + groupName + " did not declare a TestGroup class for implementation.", ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM);
			}
			
			// Prepare it.  Mind you, this may be re-running it.
			theTestGroup.prepare(runnerName, name, expresser, logger, properties, depth+1);

			// Save it
			declared.put(name,theTestGroup);	

		} catch (ThingsException te) {
			throw te;
			
		} catch (Throwable e) {
			// This is always bad
			logger.post(TestCommon.formGroupLog(name, "Aborting group(serious, could not load)=" + name + " message=")+e.getMessage());
		}
	
		// Run it
		try {
		    theTestGroup.execute();
		} catch (TestAbortException  tae) {    
		    if (throwAborts) throw tae;
		} catch (TestFailException tfe) {
		    if (throwFails) throw tfe;
		} catch (Throwable  te) {
		    te = new TestAbortException("Superious abort.  message=" + te.getMessage(),te);
		} 	
	}
	
	/**
	 * Get the name of this group
	 * @return the name of this group
	 */
	public String getName() throws ThingsException {
		return groupName;
	}	
	
	/**
	 * Get the long name of this group.
	 * @return the name of this group
	 */
	public String getLongName() throws ThingsException {
		return groupLongName;
	}	
	
	// ==================================================================	
	// PRIVATE FIELDS
	private boolean throwFails;
	private boolean throwAborts;
	private ResultExpresser expresser;
	private int depth;
	private LinkedHashMap<String,Object> declared;
			
	// ==================================================================	
	// INVOKER METHODS
	
	/**
	 * Init this group
	 * @param runnerNaming
	 *            is the name giving to the test run by the running agent
	 * @param givenName
	 *            the name given to this group.             
	 * @param expresserIn
	 *            is the ResultExpresser to use.            
	 * @param logIn
	 *            is a StringPoster for logging.
	 * @param view
	 *            is a properties view that will be avaible to the test
	 *            implementation
	 * @param inDepth
	 * 			  the depth of this group.
	 *            
	 * @see things.data.ThingsPropertyView
	 * @throws things.common.ThingsException
	 */
	public void prepare(String runnerNaming, String  givenName, ResultExpresser expresserIn, StringPoster logIn,
			ThingsPropertyView view, int	inDepth) throws ThingsException {

		// locals
		runnerName = runnerNaming;
		groupName = givenName;
		groupLongName = givenName;
		expresser = expresserIn;
		logger = logIn;
		properties = view;
		throwFails = true;
		throwAborts = true;
		depth = inDepth;

		declared = new LinkedHashMap<String,Object>();
		
		// chain prepare
		try {
			this.group_prepare();
		} catch (Throwable ee) {
			throw new ThingsException("Could not prepare group.  group="
					+ groupName + "msg=" + ee.getMessage(),
					ThingsException.TEST_ERROR_COULD_NOT_PREP_TEST, ee);
		}
	}

	/**
	 * Run this group
	 * @throws things.common.ThingsException
	 */
	public void execute() throws Throwable {
		
		long timer = 0;
		
		// start the timer, chain execution, stop the timer.
		try {

			// EXECUTE IT
			logger.post(TestCommon.formGroupLog(groupName, "Starting group=" + groupName));
			
			// This will register all the desired runs.
			timer = System.currentTimeMillis();
			this.group_execute();
			
			logger.post(TestCommon.formGroupLog(groupName, "Ending group=" + groupName));
			
		} catch (TestAbortException tae) {
			logger.post(TestCommon.formGroupLog(groupName, "Aborting group=" + groupName+ ". message="+tae.getMessage()));
		    throw tae;
		    
		} catch (TestFailException fte) {
			logger.post(TestCommon.formGroupLog(groupName, "Failing group=" + groupName + ". message="+fte.getMessage()));
		    throw fte;
		    
		} catch (ThingsException te) {
			logger.post(TestCommon.formGroupLog(groupName, "Aborting group(serious)=" + groupName + ". message="+te.getMessage()));	    
		    
		} catch (Throwable ee) {
			logger.post(TestCommon.formGroupLog(groupName, "Aborting group(panic)=" + groupName));	    
			throw new ThingsException("Serious Test Group implementation error.  test="
					+ groupName + "msg=" + ee.getMessage(),
					ThingsException.TEST_ERROR_TEST_IMPL_PROBLEM, ee);	 
		} finally {
			time = System.currentTimeMillis() - timer;
			this.groupReport();
		}
	}
	
	/**
	 * GROUP report.
	 * @throws things.common.ThingsException
	 */
	private void groupReport() throws ThingsException {
		
		Result subResult = Result.WAITING;
		Test subTest;
		TestGroup subTestGroup;
		
	    // Iterate on the declared
	    Collection<Object> tcs = declared.values();
	    for (Object tc : tcs) {
	        total++;
	        
	        if (tc instanceof Test) {
	        	subTest = (Test)tc;
	        	if (subTest.totalSize>0) totalSize += subTest.totalSize;
	        	subResult = subTest.result;
	        	
	        	// Catch any that weren't run because of an abort
	        	if (subResult==Result.WAITING) {
	        		subResult=Result.ABORT;
		    		expresser.express(ThingsUtilityBelt.timestampFormatterHHMMSS(),
							   ResultExpresser.expressionType.TEST, Result.ABORT, depth+1, subTest.getName(),
							   "Aborted before run", 0,0,0,0,1,
							   0,0,ResultExpresser.NO_CODE,ResultExpresser.NO_LIST);
	        	}

	        } else if (tc instanceof TestGroup) {
	        	subTestGroup = (TestGroup)tc;
	        	if (subTestGroup.totalSize>0) totalSize += subTestGroup.totalSize;
	        	subResult = subTestGroup.result;	        	
	        	
	        	// Catch any that weren't run because of an abort
	        	if (subResult==Result.WAITING) {
	        		subResult=Result.ABORT;
		    		expresser.express(ThingsUtilityBelt.timestampFormatterHHMMSS(),
							   ResultExpresser.expressionType.GROUP, Result.ABORT, depth+1, subTestGroup.getName(),
							   "Aborted before run", 0,0,0,0,1,
							   0,0,ResultExpresser.NO_CODE,ResultExpresser.NO_LIST);
	        	}
	        	
	        } else {
	        	ThingsException.softwareProblem("An Object that isn't a Test or TestGroup got into the declared list in TestGroup.");
	        }
	        
	        switch(subResult) {
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
	    		    ThingsException.softwareProblem("groupReport() encountered unexpected case type.  number="+ subResult);
	    			break;
	    			
	        } // end switch
	    }  // end for

	    // Result for this
		if (total==0) {
			// Bufoonery.  No declared cases
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
						   ResultExpresser.expressionType.GROUP, result, depth, groupName,
						   "Complete group", pass,fail,inconclusive,exception,abort,
						   time,totalSize,ResultExpresser.NO_CODE,ResultExpresser.NO_LIST);
	}
}