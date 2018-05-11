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
package things.thinger.kernel;


/**
 * Things State.  Use for process state management.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 26 JUN 05
 * </pre> 
 */
public enum ThingsState {

	// ITEMS
	STATE_INVALID(0, "Invalid"),
	STATE_CONSTRUCTION(100, "Construction"),
	STATE_NEW(200, "New"),
	STATE_ACTIVE_THRESHOLD(300, "New"),
	STATE_RUNNING(400, "Running"),
	STATE_TRANSIT_THRESHOLD(401, "Running"),
	STATE_PAUSE_REQUESTED(500, "Pause"),
	STATE_PAUSED(501, "Pause"),
	STATE_HALT_REQUESTED(600, "Stopping"),
	STATE_HALT(601, "Stopping"),
	STATE_DESTRUCTION(700, "Done"),
	STATE_FOUL(800, "Done"),
	STATE_KILLED(900, "Done"),
	STATE_DONE(999, "Done" ) ;

	// Enum internal.
    private final int item;    
    private final String text;
    private ThingsState(int item, String text) { 
    	this.item = item; 
    	this.text = text;
    }
    
    /**
     * Get numeric.
     * @return the numeric value for this item.
     */
    public int getNumeric() {
    	return item;
    }
    
    /**
     * Get text.
     * @return the human readable text description for the state.
     */
    public String getText() {
    	return text;
    }
    
    /**
     * Is dead or dying.
     * @return true if so.
     */
    public boolean isDeadOrDying() {
    	if (item >= STATE_HALT.item) return true;
    	return false;
    }
    
    /**
     * Is dead, dying, or a halt is requested.
     * @return true if so.
     */
    public boolean isHalting() {
    	if (item >= STATE_HALT_REQUESTED.item) return true;
    	return false;
    }
}
