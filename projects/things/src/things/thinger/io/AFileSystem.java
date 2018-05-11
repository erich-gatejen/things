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
package things.thinger.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import things.thinger.SystemException;

/**
 * Abstract File System Interface.  This is not meant to replace the {@link things.universe Universes}.
 * I regret the necessity of this, but it would take me a long time to implement everything I need
 * in the universes just so I can get the kernel up.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 FEB 05
 * </pre> 
 */
public interface AFileSystem {

    /**
     * This is the assumed mount root for any filesystem.
     */
	public static final String ROOT = "/";
	
    /**
     * Describe the root to this filesystem.  It may be an empty String.
     * @return A textual description for the root.
     */
    public String describeRoot();
    
    /**
     * Report if a file exists at the path given.
     * @param path The path
     * @return True if the file exists, otherwise false.
     */
    public boolean exists(String  path);
    
    /**
     * Current size of the file.  It can change.
     * @param path The path
     * @return the size or 0 if the file doesn't exist or is invalid.
     */
    public long size(String  path);
	
  /**
     * Delete a file or directory if it exists at the path given.
     * @param path An absolute path to the item.
     * @param recurse If set true, it will delete sub-directories on a directory delete, otherwise it will just delete the files.
     * @return true if the file existed, otherwise false.
     * @throws things.common.SystemException
     */
	public boolean delete(String  path, boolean recurse) throws SystemException;   
    
    /**
     * Is this a file?
     * @param path The path
     * @return True if is a file, otherwise false.
     */   
    public boolean isFile(String path);
    
    /**
     * Is this a directory?
     * @param path The path
     * @return True if is a director, otherwise false.
     */   
    public boolean isDirectory(String path);
    
    /**
     * Paths within the given root path.  It's how to find a directories contents.
     * @return An array of paths as Strings or null if the path is invalid.
     * @throws things.common.SystemException
     */   
    public List<String> paths(String path) throws SystemException;
    
    /**
     * Copy an InputStream to a file designated by the path. It'll throw a ThingsException if something goes wrong.
     * It will return true if it overwrote an existing file.
     * @param path The path to the destination.  If the file exists, it will be overwritten.  If the path does not exist, it will be created.
     * @param is An InputStream to the source.  It should be buffered, so don't bother doing it.
     * @return true if it overwrote a file, otherwise false
     * @throws things.common.SystemException
     */   
    public boolean copy(String path, InputStream  is) throws SystemException;
    
    /**
     * Open a file with an InputStream for input.
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     * @return An InputStream that can read from the file.
     */   
    public InputStream openInputStream(String path) throws SystemException;
    
    /**
     * Open a file with an OutputStream for output
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     * @return An OutputStream that can read from the file.
     */   
    public OutputStream openOutputStream(String path) throws SystemException;    
    
    /**
     * Make the directory.  If it already exists, nothing will happen.  It will make all directories neccessary to finish the job.
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     */   
    public void mkdir(String path) throws SystemException;
   
}
