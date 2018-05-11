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

import java.io.InputStream;
import java.io.OutputStream;

import things.common.ThingsException;

/**
 * This is an access abstraction.
 * <p>
 * You are at the mercy of the implementation in terms of multithreading.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 JAN 05
 * </pre> 
 */
public interface Accessor {

    /**
     * Open the item for read. You can assume it is buffered.
     * @return return an input stream for the item.  It will never return null; problems will cause exceptions.
     * @throws ThingsException for any problem.
     */
    public InputStream openForRead() throws ThingsException;
    
    /**
     * Declare you are done reading from the input stream.  It is a good idea to call this, though it shouldn't be fatal if you don't.
     * @param ios the stream you were given when you openForRead().  You may close the stream already, if you want.
     * @throws ThingsException for any problem.
     */
    public void doneWithRead(InputStream ios) throws ThingsException;
    
    /**
     * Open the item for writing. You can assume it is buffered.
     * @return return an output stream for the item.  It will never return null; problems will cause exceptions.
     * @throws ThingsException for any problem.
     */
    public OutputStream openForWrite() throws ThingsException;
    
    /**
     * Declare you are done reading from the input stream.  It is a good idea to call this, though it shouldn't be fatal if you don't.
     * @param oos the stream you were given when you openForWrite().  You may flush and close the stream already, if you want.
     * @throws ThingsException for any problem.
     */
    public void doneWithWrite(OutputStream oos) throws ThingsException;
    
}
