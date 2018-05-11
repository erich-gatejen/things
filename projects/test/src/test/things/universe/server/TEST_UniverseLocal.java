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
package test.things.universe.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import things.testing.unit.Test;
import things.testing.unit.TestLocalException;
import things.universe.Universe;
import things.universe.UniverseException;

/**
 * TEST a Local Universe Server implementation.  Tests the following:<br>
 * things.universe.server.UniverseLocal<br>
 * things.universe.server.UniverseLocalServer<br>
 * things.universe.server.UniverseRegistry_Simple<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 DEV 04
 * </pre>
 */
public class TEST_UniverseLocal extends Test {
	
	// TESTS
	public final static String CONSTRUCT_REGISTRY = "Construct UniverseRegistry_Simple";
	public final static String GET_ACCESSORS = "Get accessors";	
	public final static String RESERVE_UNIQUE = "Reserve a unique object";	
	public final static String PUT_STREAMS = "Get and use a Put Stream and Put Stream Appender";
	public final static String READS_1 = "First read verification";
	public final static String LOCKS = "Lock mechanism";
	public final static String READS_2 = "Second read verification";
	public final static String DELETES = "Delete unlocked and by lock.";

	
	// tokens
	private final static String TOKEN_1 = "1TOKEN1";
	private final static String TOKEN_2 = "2TOKEN2";	
	private final static String TOKEN_VALIDATION = "1TOKEN12TOKEN2";	
	
	/**
	 * prepare for the test. Overload this with the test implementation.
	 */
	public void  test_prepare() throws Throwable {
		
		SET_LONG_NAME("things.universe.server.UniverseLocal,UniverseLocalServer,UniverseRegistry_Simple");
		
	    DECLARE(CONSTRUCT_REGISTRY);
	    DECLARE(GET_ACCESSORS);
	    DECLARE(RESERVE_UNIQUE);
	    DECLARE(PUT_STREAMS);
	    DECLARE(READS_1);
	    DECLARE(LOCKS);
	    DECLARE(READS_2);
	    DECLARE(DELETES);
	}

	/**
	 * Run the test. Overload this with the test implementation.
	 */
	public void test_execute() throws Throwable {
		
		// use the  common infrastructure.
		CommonUniverseTestInfrastructure infr = new CommonUniverseTestInfrastructure();

		// local
	    Universe		 primaryUni = null;	 // AKA root
	    Universe		 secondaryUni = null;   // AKA secondary
	    String			 primaryUnique = null; 
	    String			 secondaryUnique = null;
	    
		// Construct UniverseRegistry_Simple
		try {
			infr.init(properties);
			PASS(CONSTRUCT_REGISTRY,"OK");
		} catch (Throwable e) {
		    ABORT(CONSTRUCT_REGISTRY,e.getMessage());
		}
			
		// Get accessors
		try {
			primaryUni = infr.getA();
			secondaryUni = infr.getB();
			if (primaryUni==null) {
				PUNT("Primary (root) null");
			}
			if (secondaryUni==null) {
				PUNT("Secondary (test) null");
			}
			PASS(GET_ACCESSORS,"OK");
		} catch (Throwable e) {
		    ABORT(GET_ACCESSORS,e.getMessage());
		}
		
		// Get a unique files
		try {
			primaryUnique = primaryUni.reserveUnique("test");
			secondaryUnique = secondaryUni.reserveUnique("/level2");			
			PASS(RESERVE_UNIQUE,"OK");
		} catch (Throwable e) {
			ABORT(RESERVE_UNIQUE,e.getMessage());
		}
		
		// Write to 
		try {			
			// FIRST WRITES
			Writer outPri
			  = new BufferedWriter(new OutputStreamWriter(primaryUni.putStream(primaryUnique)));
			Writer outSec
			  = new BufferedWriter(new OutputStreamWriter(secondaryUni.putStream(secondaryUnique)));
			outPri.write(TOKEN_1);
			outSec.write(TOKEN_1);
			outPri.close();
			outSec.close();
			 
			// APPEND WRITES			 
			outPri  = new BufferedWriter(new OutputStreamWriter(primaryUni.putStreamAppender(primaryUnique)));
			outSec  = new BufferedWriter(new OutputStreamWriter(secondaryUni.putStreamAppender(secondaryUnique)));
			outPri.write(TOKEN_2);
			outSec.write(TOKEN_2);
			outPri.close();
			outSec.close();			 
			 
			PASS(PUT_STREAMS,"OK");
		} catch (Throwable e) {
			ABORT(PUT_STREAMS,e.getMessage());
		}
		
		// Reads 
		try {			
			// FIRST READS
			BufferedReader inPri
			  = new BufferedReader(new InputStreamReader(primaryUni.getStream(primaryUnique)));
			BufferedReader inSec
			  = new BufferedReader(new InputStreamReader(secondaryUni.getStream(secondaryUnique)));
			String priToken = inPri.readLine();
			String secToken = inSec.readLine();

			if ( priToken.indexOf(TOKEN_VALIDATION) < 0 ) PUNT("Primary read did not read tokens.");
			if ( secToken.indexOf(TOKEN_VALIDATION) < 0 ) PUNT("Secondary read did not read tokens.");
			 
			PASS(READS_1,"OK");
		} catch (TestLocalException e) {
			FAIL(READS_1,e.getMessage());
		} catch (Throwable t) {
			EXCEPTION(READS_1,t.getMessage());		
		}		
		
		// Test lock mechanism
		try {
			String key = primaryUni.lock(primaryUnique);
			// Try to open.  This should fail.
			try {
				primaryUni.delete(primaryUnique);
			
				// This is bad
				PUNT("Lock did not protect against a delete.");
			} catch (UniverseException uee) {
				// This is good
			} 
			
			// FORCE GARBAGE COLLECTION to make sure the file delete is finalized
			//System.gc();
			//Thread.sleep(10);
			
			// Write by lock
			Writer outPri
			  = new BufferedWriter(new OutputStreamWriter(primaryUni.putStreamByKey(key)));
			outPri.write(TOKEN_1);
			outPri.close();		
			
			// Write append by lock
			outPri
			  = new BufferedWriter(new OutputStreamWriter(primaryUni.putStreamAppenderByKey(key)));
			outPri.write(TOKEN_2);
			outPri.close();		
			
			// detect lock and remove lock
			if (!primaryUni.isLocked(primaryUnique)) PUNT("isLocked() did not detect a lock.");
			primaryUni.unlock(key);
			
			PASS(LOCKS,"OK");
		} catch (TestLocalException e) {
			FAIL(LOCKS,e.getMessage());
		} catch (Throwable t) {
			EXCEPTION(LOCKS,t.getMessage());		
		}	
			
		// Reads 2
		try {			
			// SECOND READS
			BufferedReader inPri
			  = new BufferedReader(new InputStreamReader(primaryUni.getStream(primaryUnique)));
			BufferedReader inSec
			  = new BufferedReader(new InputStreamReader(secondaryUni.getStream(secondaryUnique)));
			String priToken = inPri.readLine();
			String secToken = inSec.readLine();
			inPri.close();
			inSec.close();

			if ( priToken.indexOf(TOKEN_VALIDATION) < 0 ) PUNT("Primary read2 did not read tokens.");
			if ( secToken.indexOf(TOKEN_VALIDATION) < 0 ) PUNT("Secondary read2 did not read tokens.");
			 
			PASS(READS_2,"OK");
		} catch (TestLocalException e) {
			FAIL(READS_2,e.getMessage());
		} catch (Throwable t) {
			EXCEPTION(READS_2,t.getMessage());		
		}
				
		// Deletes
		try {
			String key = primaryUni.lock(primaryUnique);
			primaryUni.deleteByKey(key);
			secondaryUni.delete(secondaryUnique);
			
			if (primaryUni.isLocked(primaryUnique)) PUNT("Primary did not unlock.");
			if (primaryUni.exists(primaryUnique)) PUNT("Primary did not delete.");
			if (secondaryUni.exists(secondaryUnique)) PUNT("Secondary did not delete.");
			
			PASS(DELETES,"OK");
		} catch (TestLocalException e) {
			FAIL(DELETES,e.getMessage());
		} catch (Throwable t) {
			EXCEPTION(DELETES,t.getMessage());		
		}
		
	} // end test
} // end class
