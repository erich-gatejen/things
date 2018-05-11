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
package things.data;


/**
 * A line feeder mechanism.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 JAN 05
 * </pre> 
 */
public interface LineFeeder {

    /**
     * Get the next line as a String.  It will return null if there
     * is nothing left to give.
     * @return the next line
     */
    public String getNextLine();
    
    /**
     * peek at next line, but don't read it.
     * @return the next line
     */
    public String peekNextLine();
    
    /**
     * Get the number of the current line (last one read), starting from the number one.
     * @return the next line
     */   
    public int lineNumber();
}
