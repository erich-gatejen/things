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
package things.thinger.kernel.basic.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.JavaFileObject.Kind;

import things.universe.UniverseAnchor;

/**
 * A java file manager for handling universe objects.
 * <p>
 * NOTE: This is a big problem with the captured files.  I don't make sure they are actually closed from whatever operation when they are
 * all released.  This is something to handle later.

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 18 NOV 02
 * </pre> 
 */
public class UniverseFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

	// This universe anchor.
	UniverseAnchor	myUa;
	
	// Remember which we've captured
	HashMap<String, File> captured;
	
	// Our classloader
	ClassLoader	internalClassLoader;

	/**
	 * Construct
	 * @param rootManager The root manager from which we will take forwards.
	 * @param aClassLoader The class loader for this manager
	 * @param ua The UniverseAnchor for this manager.  All objects will be relative to this anchor.
	 * @see things.universe.UniverseAnchor
	 */
	public UniverseFileManager(StandardJavaFileManager rootManager,  UniverseAnchor ua, ClassLoader aClassLoader) {
		super(rootManager);
		captured =  new HashMap<String, File>();
		myUa = ua;
		internalClassLoader = aClassLoader;
	}

	/**
	 * Get the registered universe.
	 * @return the universe anchor.
	 */
	public UniverseAnchor getUniverse() {
		return myUa;
	}
	
	// ==============================================================================================================================
	// == OVERRIDES
	
	/**
	 * Get a JavaFileObject for output.  It isn't much different than the output method--for now.
	 * If it is writing a class, it'll always make sure there is a .class at the end.
	 * @param location the location type
	 * @param name the class name
	 * @param kind the kind
	 * @param sibling the sibling
	 */
	public synchronized JavaFileObject getJavaFileForOutput(Location location, String name, Kind kind, FileObject sibling) throws IOException {
				
		JavaFileObject result = null;
		File capturedFile = null;
		String actual = null;
		String normalized = null;
		
		// Normalize name.  the sibling should be hiding the actual name, unless it is a sub class.
		if (name==null) throw new IOException("Null file name.");
		if (name.indexOf('$') > 0) {
			normalized = name;
		} else {
			normalized = sibling.getName();
		}
		if (normalized != null) normalized = normalized.replace('.', '/');
		else normalized = name.replace('.', '/');
		
		// Qualify.  Cannot let it be opened again while captured.
		if (captured.containsKey(normalized)) throw new IOException("Already open for input or output.  item=" + normalized );
		
		try {
		
			// How we look depends on what location.  Delegate what we don't know.
			if ((location == StandardLocation.CLASS_PATH)||(location == StandardLocation.CLASS_OUTPUT)||(location == StandardLocation.PLATFORM_CLASS_PATH)) {
				
				// Make sure we are outputting class files.
				capturedFile = myUa.getLocal(normalized);
	
			} else if ((location == StandardLocation.SOURCE_PATH)||(location == StandardLocation.SOURCE_OUTPUT)) {
				
				// Make sure we are outputting class files.
				// WHY TWO IFS?  I'll be playing with this later.
				capturedFile = myUa.getLocal(normalized);
				
			} else {
				// Delegate the rest
				result = super.getJavaFileForOutput(location, name, kind, sibling);
			}
			
			// Do we have to do something?
			if (capturedFile != null) {
				
				// Make the java object
				result = new LocalJavaFileObject(name, actual, kind, capturedFile);
				captured.put(normalized, capturedFile);
			}
		
		} catch (IOException ioe) {
			try {
				myUa.releaseLocal(capturedFile);			// These seem to be staying locked sometimes.
			} catch (Throwable tt) {
				// Best effort.
			}
			throw ioe;
		} catch (Throwable t) {
			try {
				myUa.releaseLocal(capturedFile);			// These seem to be staying locked sometimes.
			} catch (Throwable tt) {
				// Best effort.
			}
			throw new IOException("Failed to get file.", t);
		}
			
		return result;
	}

	/**
	 * Always return it.
	 */
	public ClassLoader 	getClassLoader(JavaFileManager.Location location) {
		return internalClassLoader;
	}
	
	/**
	 * Get a JavaFileObject for input.  It isn't much different than the output method--for now.
	 * @param location the location type
	 * @param name the class name
	 * @param kind the kind
	 * @param sibling the sibling
	 */
	/*
	public synchronized JavaFileObject getJavaFileForInput(Location location, String name, Kind kind) throws IOException {
				
		JavaFileObject result = null;
		File capturedFile = null;
		String actual = null;
		
		// Normalize name
		if (name==null) throw new IOException("Null file name.");
		String normalized = name.replace('.', '/');
		
		// Qualify.  Cannot let it be opened again while captured.
		if (captured.containsKey(normalized)) throw new IOException("Already open for input or output.  item=" + normalized );
		
		try {
		
			// How we look depends on what location.  Delegate what we don't know.
			if ((location == StandardLocation.CLASS_PATH)||(location == StandardLocation.CLASS_OUTPUT)||(location == StandardLocation.PLATFORM_CLASS_PATH)) {
				
				// We're looking for a class.  Normalize the name and see if it is there with the name as is.
				if (myUa.hasObject(normalized)) {
					capturedFile = myUa.getLocal(normalized);
					actual = normalized;
						
				} else if (myUa.hasObject(normalized + ".class")) {
					actual = normalized + ".class";
					capturedFile = myUa.getLocal(actual);
	
				} else {
					throw new IOException("Class not found.");
				}
				
	
			} else if ((location == StandardLocation.SOURCE_PATH)||(location == StandardLocation.SOURCE_OUTPUT)) {
				
				// We're looking for a source.
				if (myUa.hasObject(normalized)) {
					capturedFile = myUa.getLocal(normalized);
					actual = normalized;
						
				} else if (myUa.hasObject(normalized + ".java")) {
					actual = normalized + ".java";
					capturedFile = myUa.getLocal(normalized + ".java");
	
				} else {
					throw new IOException("Java source not found.");
				}
				
			} else {
				// Delegate the rest
				result = super.getJavaFileForInput(location, name, kind);
			}
			
			// Do we have to do something?
			if (capturedFile != null) {
				result = new LocalJavaFileObject(name, actual, kind, capturedFile);
				captured.put(normalized, capturedFile);
			}
		
		} catch (IOException ioe) {
			throw ioe;
		} catch (Throwable t) {
			throw new IOException("Failed to get file.", t);
		}
			
		return result;
	}
	
	*/

	/**
	 * Infer the binary name.  Do in this subclass if it is a kind we handle.
	 * @param location the location type
	 * @param item the file object
	 * @return 
	 */
	/*
	public String inferBinaryName(Location location, JavaFileObject item) {
		String result;

		if ((location == StandardLocation.CLASS_PATH)||(location == StandardLocation.CLASS_OUTPUT)||(location == StandardLocation.PLATFORM_CLASS_PATH)||(location == StandardLocation.SOURCE_PATH)||(location == StandardLocation.SOURCE_OUTPUT)) 
			result = item.getName();
		else
			result = super.inferBinaryName(location, item);

		return result;
	}
	
	*/
	
	// ==============================================================================================================================
	// == METHODS
	
	/**
	 * Release all captured files.  It is important to do this when you are done.  It'll happen with finalization, but that might be a bit late
	 * in the game.
	 */
	public void releaseAllCaptured() {
		for (File item : captured.values()) {
			myUa.releaseLocal(item);
		}
	}
	
	
	// ==============================================================================================================================
	// == FINALIZATION
	
	/**
	 * Finalize.  Make sure we released the captured.
	 */
	protected void finalize() throws Throwable {
		releaseAllCaptured();
	}
	

}