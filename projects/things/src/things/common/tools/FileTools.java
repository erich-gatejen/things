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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;

import org.apache.commons.io.CopyUtils;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;

/**
 * File manipulation utilities.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 24 NOV 04
 * </pre> 
 */
@SuppressWarnings("deprecation")
public class FileTools {

	/**
	 * Max number of delete retries.
	 */
	private final static int FILE_DELETE_RETRIES = 10;
	
	/**
	 * Copy a file.
	 * @param source
	 * @param destination
	 * @throws Throwable
	 */
	public static void copy(File source, File destination) throws Throwable {
		
		InputStream ins = null;
		OutputStream outs = null;
		try {
			
			ins = new FileInputStream(source);
			outs = new FileOutputStream(destination);
			CopyUtils.copy(ins, outs);
			
		} catch (Throwable t) {
			throw t;
		} finally {
			try {
				ins.close();
			} catch (Throwable tt) {}
			try {
				outs.close();
			} catch (Throwable tt) {}		
		}
	}
	
	/**
	 * Copy a stream.  I'll implement this myself some day.
	 * @param source
	 * @param destination
	 * @throws Throwable
	 */
	public static void copy(InputStream source, OutputStream destination) throws Throwable {
		try {
			CopyUtils.copy(source, destination);	
		} catch (Throwable t) {
			throw t;
		} 
	}
	
	/**
	 * Delete a file.
	 * @param theFile the file.  Null will be ignored.
	 * @throws Throwable
	 */
	public static void delete(File theFile) throws Throwable  {
		if ((theFile==null)||(!theFile.exists())) return;// Dismiss null or not there.
	    try {
		    int tries = 5;
	        while( theFile.exists() && !theFile.delete() ) {
	              System.gc();
                  try
                  {
                    Thread.sleep(10);
                  } catch(InterruptedException ex) {}
                  tries--;
                  if (tries == 0) throw new Exception("Exceeded delete try.  File doesn't want to die.");
	        }
	    } catch (Exception te) {
	        throw new ThingsException("Delete failed.",ThingsException.FILESYSTEM_ERROR_DELETE_FAILED, te, SystemNamespace.ATTR_PLATFORM_FILE_PATH, theFile.getAbsolutePath());
	    }
	}
	
	/**
	 * Destroy a file or directory and all it's contents (including sub directories).
	 * @param theFile the file to destroy (it can be a real file or a directory).
	 * @throws Throwable
	 */
	public static void destroy(File theFile) throws Throwable  {
		destroy(theFile, true);
	}
	
	/**
	 * Destroy a file or directory and all it's contents (including sub directories).
	 * @param theFile the file to destroy (it can be a real file or a directory).
	 * @param recurse if true, recurse into directories
	 * @throws Throwable
	 */
	private static void destroy(File theFile, boolean recurse) throws Throwable  {
		
		// If it is a file, kill it.
		if (theFile.isFile()) {
			int tries = FILE_DELETE_RETRIES;
	        while( !theFile.delete() ) {
	            System.gc();			// Give it a breather
	            tries--;
	            if (tries == 0) {
	            	if (theFile.exists()) throw new SystemException("Exceeded delete try.  File doesn't want to die.", SystemException.FILESYSTEM_ERROR_FILE_WONT_DELETE, SystemNamespace.ATTR_PLATFORM_FS_PATH_ABSOLUTE, theFile.getAbsolutePath());
	            }
	        }
			
		} else {
			
			// Must be a directory
			File[] subpaths = theFile.listFiles();
			if (subpaths == null) SystemException.softwareProblem("FSFileSystem.internalDelete got a File that isn't isFile() or isDirectory().  wtf?", new Exception(), SystemNamespace.ATTR_PLATFORM_FS_PATH_ABSOLUTE, theFile.getAbsolutePath());

			// Whack any subs.  We'll hazard some recursion for this.
			if (subpaths.length > 0) {
				
				// make the recursion decision here to save time
				if (recurse) {
					for (int index = 0; index < subpaths.length; index++) {
						destroy(subpaths[index],true);
					}
				} else {
					
					// Files only
					for (int index = 0; index < subpaths.length; index++) {
						if (subpaths[index].isFile()) {
							destroy(subpaths[index],false);
						}
					}				
				}
				
			} // end IF SUBDIRECTORIES
			
			// Get rid of the directory (the error means it wasn't empty)
			// Try once 
			if (!theFile.delete()) {
				
				// Is it really there?
				if (theFile.exists()) {
					
					// Is it empty?  Check above first as a fast fail, before we hit the filesystem again.
					if ((subpaths.length > 0)&&(theFile.listFiles().length <= 0)) {
						
						// Ok, go to work on it, because it is supposed to be gone
						int tries = FILE_DELETE_RETRIES;
				        while( !theFile.delete() ) {
				            System.gc();			// Give it a breather
				            tries--;
				            if (tries == 0) throw new SystemException("Exceeded delete try.  File doesn't want to die.",SystemException.FILESYSTEM_ERROR_FILE_WONT_DELETE, SystemNamespace.ATTR_PLATFORM_FS_PATH, theFile.getAbsolutePath());
				        }
				        
					} else {
						if (recurse) throw new SystemException("Directory will not delete because it is not empty (though it should have been emptied already).", SystemException.FILESYSTEM_ERROR_FILE_WONT_DELETE, SystemNamespace.ATTR_PLATFORM_FS_PATH, theFile.getAbsolutePath());
					}
					
				} 
				
			} // end kill DIRECTORY
			
		} // end if FILE
	}

	
	/**
	 * Make a file.  If the file already exists, delete it.  
	 * It will let any exception escape.
	 * @param name Path to the file
	 * @return A file object
	 * @throws Throwable
	 */
	public static File makeFile(String name) throws Throwable {

		File directory;
		String directoryPath;
		int fileSegment;

		// construct the path
		File target = new File(name);

		if (target.exists()) {
			int tries = ThingsConstants.FS_FILE_DELETE_RETRIES;
			boolean status = target.delete();
			while(status != true) {
				tries--;
				if (tries == 0) throw new ThingsException("Could not delete existing file where call wanted to makeFile().", ThingsException.FILESYSTEM_ERROR_COULD_NOT_REPLACE, ThingsNamespace.ATTR_PLATFORM_FILE_PATH);
				System.gc();
				Thread.yield();
				status = target.delete();
			}
		} else {
		    fileSegment = name.lastIndexOf('/');
			if (fileSegment > 1) {
			    directoryPath = name.substring(0, fileSegment);
			    directory = new File(directoryPath);
				if (!directory.exists()) {
				    directory.mkdirs();
				}
			}
		}
		return target;
	}

	/**
	 * Make a file.  If the file already exists, it will be available for write.
	 * It will let any exception escape.
	 * @param name Path to the file
	 * @return A file object
	 * @throws Exception
	 */
	public static File appendFile(String name) throws Exception {

		File directory;
		String directoryPath;
		int fileSegment;

		// construct the path
		File target = new File(name);

		if (!target.exists()) {

		    fileSegment = name.lastIndexOf('/');
			if (fileSegment > 1) {
			    directoryPath = name.substring(0, fileSegment);
			    directory = new File(directoryPath);
				if (!directory.exists()) {
				    directory.mkdirs();
				}
			}
		}
		return target;
	}

	/**
	 * This will detect if the path has any relative pointers (such as ..).  
	 * @param path The path
	 * @return true if it does, otherwise false.
	 */
	public static boolean detectRelativePath(String path) {
		if (path!=null) {
			try {
				// detect no root
				String trimmed = path.trim();
				if ( ((trimmed.charAt(1)!=':')&&(trimmed.charAt(0)!='/')&&(trimmed.charAt(0)!='\\'))||
					 (trimmed.charAt(0)=='.')) return true;
				
				// find dots
				StringReader rin = new StringReader(path);
				State rpState = State.SEEK;
				int currentChar = rin.read();
				while (currentChar >= 0) {
					switch (rpState) {
						case SEEK:
							if (currentChar=='\\' || currentChar=='/') rpState = State.START;
							break;
						case START:
							if (currentChar=='.') rpState = State.DOT;
							else if (currentChar!='\\' && currentChar!='/') rpState = State.SEEK;
							break;
						case DOT:
							if (currentChar=='\\' || currentChar=='/') return true;
							if (currentChar!='.') rpState = State.START;
							break;
					}
					currentChar = rin.read();
				}
			} catch (Exception ee) {
				// Don't care.  Should only be a spurious EOF
			}
		}
		return false;
	}
	private enum State { SEEK, START, DOT }
	
	
	
	/**
	 * Read a file into a String.  This should be used only is absolutely needed.
	 * @param path File to load
	 * @return String if successful or otherwise null
	 * @throws Certain IOException, like if the file cannot be found or opened.
	 */
	public static String loadFile2String(File   path) throws IOException {
		String result = null;
		if (path == null) throw new IOException("Path is null in loadFile2String(path).");
		
		try {
			// Setup
			StringBuffer	buffer = new StringBuffer();
			char[] buf = new char[1024];
			int sbuf;
			BufferedReader bis = new BufferedReader(new FileReader(path));
	
			// Run it
			sbuf = bis.read(buf, 0, 1024);
			while (sbuf > 0) {
				buffer.append(buf, 0, sbuf);
				sbuf = bis.read(buf, 0, 1024);
			}
			
			result =  buffer.toString();
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			//throw new IOException("Failed to loadFile2String.", t);
			throw new IOException("Failed to loadFile2String." + t.getMessage());
		}
		
		return result;
	}
	
	/**
	 * Read a stream into a buffer, as much as can fit in the buffer, .  This should be used only is absolutely needed.
	 * @param stream the input stream
	 * @param buffer the buffer;
	 * @return the number of bytes read intot he buffer.
	 * @throws Certain IOException, like if the file cannot be found or opened.
	 */
	public static int loadStream2Buffer(InputStream   stream, byte[] buffer) throws IOException {
		int bufferCursor = 0;
		try {
			int actualRead = stream.read(buffer, bufferCursor, buffer.length-bufferCursor);
			bufferCursor = actualRead;
			while ((actualRead > 0)&&(bufferCursor < buffer.length)) {
				actualRead = stream.read(buffer, bufferCursor, buffer.length-bufferCursor);
				bufferCursor+=actualRead;
			}
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			//throw new IOException("Failed to loadFile2String.", t);
			throw new IOException("Failed to loadStream2Buffer. " + t.getMessage());
		}
		return bufferCursor;	
	}
	
	/**
	 * Save a string to a file.  This should be used only is absolutely needed.
	 * @param path File to save
	 * @param theString the string.
	 * @throws Certain IOException, like if the file cannot be found or opened.
	 */
	public static void saveString2File(File   path, String theString) throws IOException {
		if (path == null) throw new IOException("Path is null in saveString2File(path).");
		if (theString == null) throw new IOException("theString is null in saveString2File(path).");
		OutputStream os = null;
		
		try {
			os = new BufferedOutputStream(new FileOutputStream(path));
			os.write(theString.getBytes());
			
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			throw new IOException("Failed to saveString2File." + t.getMessage(), t);
		} finally {
			try {
				os.close();
			} catch (Throwable tt) {
				// Oh well.
			}
		}
	}
}