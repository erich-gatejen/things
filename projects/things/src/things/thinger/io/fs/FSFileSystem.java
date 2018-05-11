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
package things.thinger.io.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import things.common.ThingsException;
import things.common.tools.FileTools;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.io.AFileSystem;
import things.thinger.io.FileSystemTools;

/**
 * A disk File System implementation.  You must specify the root during construction.
 * Relative paths are not allowed and will result in an Error.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 MAR 05
 * </pre> 
 */
public class FSFileSystem implements AFileSystem {

	// ===============================================================================================
	// DATA
	
	private String myRoot;
	private final static int BUFFER_SIZE = 2048;
	private final static int FILE_DELETE_RETRIES = 10;

	
	// ===============================================================================================
	// METHODS
	
    /**
     * Proper constructor.
     * @param root the path tot he root 
     * @throws things.common.ThingsException
     */
	public FSFileSystem(String root) throws Throwable {
		
		// Qualify it
		if (root==null) throw new ThingsException("Null path given as root.",ThingsException.FILESYSTEM_ERROR_BAD_PATH);
		if (FileTools.detectRelativePath(root)) {
			throw new SystemException("Relative path given as root.", ThingsException.FILESYSTEM_ERROR_BAD_PATH, SystemNamespace.ATTR_PLATFORM_FS_PATH, root);
		} 
		File checkFile = new File(root);
		if (!checkFile.isDirectory()) {
			SystemException se = new SystemException("Path is not a valid directory on filesystem.", ThingsException.FILESYSTEM_ERROR_BAD_PATH, SystemNamespace.ATTR_PLATFORM_FS_PATH, root, SystemNamespace.ATTR_PLATFORM_FS_PATH_ABSOLUTE, checkFile.getAbsoluteFile().toString());
			throw se;
		}
		
		// Save it
		myRoot = root.trim();
	}
	
    /**
     * Describe the root to this filesystem.  It may be an empty String.
     * @return A textual description for the root.
     */
    public String describeRoot() {
    	return myRoot;
    }
    
    /**
     * Report if a path exists at the path given.  It can be a file or a directory.
     * @param path the path
     * @return True if the file exists, otherwise false.
     */
    public boolean exists(String  path) {
    	try {
    		File testFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));
    		if ((testFile.isFile())||(testFile.isDirectory())) return true;
    		else return false;
    		
    	} catch (Throwable ee) {
    		// Don't care.  False will return.
    	}
    	return false;
    }
    /**
     * Current size of the file.  It can change.
     * @param path the path
     * @return the size or 0 if the file doesn't exist or is invalid.
     */
    public long size(String  path) {
    	long result = 0;
    	try {
    		File testFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));
    		if (testFile.isFile()) {
    			result = testFile.length();
    		}    		
    	} catch (Throwable ee) {
    		// Don't care.  False will return.
    	}
    	return result;
    }
    
    /**
     * Delete a file or directory if it exists at the path given.
     * @param path An absolute path to the item.
     * @param recurse If set true, it will delete sub-directories on a directory delete, otherwise it will just delete the files.
     * @return true if the file existed, otherwise false.
     * @throws things.common.SystemException
     */
    public boolean delete(String  path, boolean recurse) throws SystemException {  
    	boolean result = false;
    	try {
    		File testFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));  	
    		if (testFile.exists()) {
    			completeDelete(testFile, recurse);
    		} 
    		
    		// Even if it isn't there, it's TRUE
    		result = true;
    		
    	} catch (SecurityException se) { 
			throw new SystemException("Security access denied for delete().",SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, SystemNamespace.ATTR_PLATFORM_FS_PATH, path);   	
    	} catch (Throwable e) {
    		// Don't care.  Let false return
    	}
    	return result;
    }  
    
    /**
     * Is this a file?
     * @param path the path to the file
     * @return True if is a file, otherwise false.
     */   
    public boolean isFile(String path) {
    	try {
    		File testFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));
    		if (testFile.isFile()) return true;
    		else return false;
    		
    	} catch (Throwable ee) {
    		// Don't care.  False will return.
    	}
    	return false;
    }
    
    /**
     * Is this a directory?
     * @param path the path
     * @return True if is a directory, otherwise false.
     */   
    public boolean isDirectory(String path) {
    	try {
    		String npath = FileSystemTools.normalizePath(myRoot + "/" + path);
    		File testFile = new File(npath);
    		if (testFile.isDirectory()) return true;
    		else return false;	
    	} catch (Throwable ee) {
    		// Don't care.  False will return.
    	}
    	return false;
    }
    
    /**
     * Paths within the given root path.  It's how to find a directories contents.
     * @param path the path
     * @return An array of paths as Strings or null if the path is invalid.
     * @throws things.common.SystemException
     */   
    public List<String> paths(String path) throws SystemException {
    	List<String> listResult = null;
    	try {
    		File pathFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));
    		String[] listFiles = pathFile.list();
    		List<String> tempResult = new LinkedList<String>();
    		for (int index = 0; index <listFiles.length;index++) {
    			tempResult.add(new String(path + File.separator + listFiles[index]));
    		}
    		listResult = tempResult;
    	} catch (SecurityException se) { 
			throw new SystemException("Security access denied for paths()." + path,SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, SystemNamespace.ATTR_PLATFORM_FS_PATH, myRoot);   	
    	} catch (Throwable e) {
    		// Don't care.  Let false return
    	}
    	return listResult;
    }
    
    /**
     * Copy an InputStream to a file designated by the path. It'll throw a ThingsException if something goes wrong.
     * It will return true if it overwrote an existing file.
     * @param path The path to the destination.  If the file exists, it will be overwritten.  If the path does not exist, it will be created.
     * @param is An InputStream to the source.  It should be buffered, so don't bother doing it.
     * @return true if it overwrote a file, otherwise false
     * @throws things.common.SystemException
     */   
    public boolean copy(String path, InputStream  is) throws SystemException {
    	boolean result = false;
    	
        BufferedInputStream bis = null;
        BufferedOutputStream bout = null;
    	try {  	
    		
    		// Source buffered
    		bis = new BufferedInputStream(is);
    		
    		// Destination
    		File pathFile = new File(FileSystemTools.normalizePath(myRoot + "/" + path));
    		
    		// Is there a file?  If not, do we need to build path to destination
			if (pathFile.exists()) {
				pathFile.delete();
				result = true;
			} else {
	    		File pathParent = new File(pathFile.getParent());
	    		if (!pathParent.exists()) {
	    			pathParent.mkdirs();
	    		}				
			}

    		// Destination buffered
    		bout = new BufferedOutputStream(new FileOutputStream(pathFile));

    		// Copy
			byte[] buf = new byte[BUFFER_SIZE];
			int sbuf;
			sbuf = bis.read(buf, 0, BUFFER_SIZE);
			while (sbuf > 0) {
				bout.write(buf, 0, sbuf);
				sbuf = bis.read(buf, 0, BUFFER_SIZE);
			}
    		
    	} catch (SecurityException se) { 
			throw new SystemException("Security access denied for READ during copy().", SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, se, SystemNamespace.ATTR_PLATFORM_FS_PATH, path);
    	} catch (FileNotFoundException  fnfe) {	
			throw new SystemException("File not found during copy().", SystemException.FILESYSTEM_ERROR_BAD_PATH, fnfe, SystemNamespace.ATTR_PLATFORM_FS_PATH, path, SystemNamespace.ATTR_PLATFORM_MESSAGE, fnfe.getMessage());
    	} catch (Throwable e) {
			throw new SystemException("General FAULT during copy().", SystemException.IO_FILESYSTEM_FAULT_GENERAL, e, SystemNamespace.ATTR_PLATFORM_FS_PATH, path);
    	} finally {
    		try {
    			bis.close();
    		} catch (Exception e) {
    			// Don't care
    		}
    		try {
    			bout.close();
    		} catch (Exception e) {
    			// Don't care
    		}
    	}
    	return result;
    }
    
    /**
     * Open a file with an InputStream for input. It will throw an exception for any problem.
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     * @return An InputStream that can read from the file.
     */   
    public InputStream openInputStream(String path) throws SystemException {
    	InputStream result = null;
    	String fullPath = "[ERRORED PATH]";
    	try {
    		fullPath = FileSystemTools.normalizePath(myRoot + "/" + path);
    		result = new FileInputStream(fullPath);
    	} catch (SecurityException se) { 
    		throw new SystemException("Security access denied for openInputStream().", SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, se, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	} catch (FileNotFoundException  fnfe) {	
			throw new SystemException("File not found during openInputStream().", SystemException.FILESYSTEM_ERROR_FILE_NOT_FOUND, fnfe, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath, SystemNamespace.ATTR_PLATFORM_MESSAGE, fnfe.getMessage());
    	} catch (Throwable e) {
			throw new SystemException("General FAULT during openInputStream().", SystemException.IO_FILESYSTEM_FAULT_GENERAL, e, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	}
    	return result;
    }
    
    /**
     * Open a file with an OutputStream for output
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     * @return An OutputStream that can read from the file.
     */   
    public OutputStream openOutputStream(String path) throws SystemException {
    	OutputStream result = null;
    	String fullPath = "[ERRORED PATH]";
    	try {
    		fullPath = FileSystemTools.normalizePath(myRoot + "/" + path);
    		result = new FileOutputStream(fullPath);
    	} catch (SecurityException se) { 
    		throw new SystemException("Security access denied for openOutputStream().", SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, se, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	} catch (FileNotFoundException  fnfe) {	
			throw new SystemException("File not found during openOutputStream().", SystemException.FILESYSTEM_ERROR_FILE_NOT_FOUND, fnfe, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath, SystemNamespace.ATTR_PLATFORM_MESSAGE, fnfe.getMessage());
    	} catch (Throwable e) {
			throw new SystemException("General FAULT during openOutputStream().", SystemException.IO_FILESYSTEM_FAULT_GENERAL, e, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	}
    	return result;
    }
    
    /**
     * Make the directory.  If it already exists, nothing will happen.  It will make all directories neccessary to finish the job.
     * @param path The past to the file to open.
     * @throws things.common.SystemException
     */   
    public void mkdir(String path) throws SystemException {
    	String fullPath = "[ERRORED PATH]";
    	try {
    		fullPath = FileSystemTools.normalizePath(myRoot + "/" + path);   	
    		File testFile = new File(fullPath);
    		// Not a current directory
    		if (!testFile.isDirectory()) {
    			// not a current file?
    			if (!testFile.isFile()) {
        			if(!testFile.mkdirs()) {
        				// I failed!
        				throw new SystemException("Unable to complete mkdir().", SystemException.FILESYSTEM_ERROR_FAILED_DIRECTORY_OPERATION, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
        			}
    			} else {
    				throw new SystemException("Path specified in mkdir() points to an existing file.",ThingsException.FILESYSTEM_ERROR_BAD_PATH, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    			}
    		}
    		
    	} catch (SystemException te) {    
    		throw te;
    	} catch (SecurityException se) { 
    		throw new SystemException("Security access denied for mkdir()", SystemException.FILESYSTEM_ERROR_ACCESS_DENIED, se, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	} catch (Throwable e) {
			throw new SystemException("General FAULT during mkdir().", SystemException.IO_FILESYSTEM_FAULT_GENERAL, e, SystemNamespace.ATTR_PLATFORM_FS_PATH, fullPath);
    	}   		
    }
    

	// =====================================================================================================================
	// =====================================================================================================================
	// INTERNAL TOOLS
    
	// Private method for deleting.  It will assume the file is free to whack.
	public static void completeDelete(File theFile, boolean recurse) throws Throwable  {
		
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
						completeDelete(subpaths[index],true);
					}
				} else {
					
					// Files only
					for (int index = 0; index < subpaths.length; index++) {
						if (subpaths[index].isFile()) {
							completeDelete(subpaths[index],false);
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


}




