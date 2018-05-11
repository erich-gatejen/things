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
package things.common.tools;

/**
 * A simple stopwatch.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 AUG 05
 * </pre>  
 */
public class StopWatch {

	// ==================================================================================
	// DATA
	
    private long watchStart;
    private long mark;			// < 1 means it is stopped.
    
	// ==================================================================================
	// METHODS
    
    /**
     * Default constructor.  It will start the stopwatch running.  (Don't worry--no CPU us used.)
     */
    public StopWatch() {
    	watchStart = 0;	
    }
    
    /**
     * Restart the stopwatch.
     */
    public void start() {
    	watchStart = System.currentTimeMillis();
    	mark =  -1;
    }
    
    /**
     * Get the time in milliseconds since the watch was started.  It does not stop watch from running.
     * @return  the time
     */
    public long time() { 
    	if (watchStart == 0) throw new Error("Watch not started.");
    	if (mark < 0) 
    		return System.currentTimeMillis() - watchStart;
    	else 
    		return mark;	
    }
    
    /**
     * Stop the watch.  All subsiquent calls to time will yield this moment in time, until start is called again.
     */
    public void stop() {
    	mark = System.currentTimeMillis() - watchStart;
    }
    
}