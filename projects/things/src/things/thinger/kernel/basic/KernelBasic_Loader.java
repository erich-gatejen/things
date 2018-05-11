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
package things.thinger.kernel.basic;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject.Kind;

import things.common.StringPoster;
import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.common.ThingsUtilityBelt;
import things.common.Verbose;
import things.thing.MODULE;
import things.thing.THING;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.kernel.Loader;
import things.thinger.kernel.basic.tools.LocalJavaFileObject;
import things.thinger.kernel.basic.tools.UniverseClassLoader;
import things.thinger.kernel.basic.tools.UniverseFileManager;
import things.universe.Universe;
import things.universe.UniverseAnchor;

/**
 * A THINGS loader implementation for KernelBasic.  This one will always take the cached version, even if it is aged.   
 * <p>
 * It also load modules, but we do not allow the loader to compile new versions.  The modules need to be in the classpath.
 * I'll add the dynamic loading later it if it becomes desirable.
 * <p>
 * There is a lot of extra stuff here for more dynamic class management and caching.  Of course, java isn't a big fan of multiple versions
 * of the same named class, so I don't know if I'll ever get around to playing with it.
 * <p>
 * To be honest, this never really worked right.  The library compiler and command line compiler named the inner classes differently, 
 * causing mayhem when encountered with a mix-match of dependent classes with inner classes.  It is all fixable, but it just wasn't important
 * at the time.  I don't know anyone that actually used the dynamic loading anyway.  (Too bad, because that was part of
 * the reason for building a new kernel in the first place.
 * <p>
 * Trying to mesh the library compiler with the abstract universe was a gigantic pain in the ass.  Thus was born the 'makeLocal' methods,
 * which was a very unfortunate compromise.  To a large degree, this one class alone might lead me to abandon Java for future
 * personal projects.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 8 JUL 07
 * </pre> 
 */
public class KernelBasic_Loader implements Loader, Verbose {
	
	// ===========================================================================================
	// INTERNAL DATA
	
	private LinkedList<UniverseAnchor>  source;
	private HashSet<String>  sourceIds;				// Duplicate protection.
	private HashMap<String, CachedItem> internalTHINGCache;
	private UniverseAnchor universeCacheDescriptor;

	private UniverseClassLoader uLoaderForCompile;
	private UniverseClassLoader uLoaderForExecution;
	private JavaCompiler compiler;
	
	// ===========================================================================================
	// METHODS
	
	/**
	 * Constructor.
	 */
	public KernelBasic_Loader() throws SystemException {
		try {
			compiler = ToolProvider.getSystemJavaCompiler();
			if (compiler==null) throw new ClassNotFoundException("Could not find the SystemJavaCompiler.  Is tools in the classpath?");
			uLoaderForCompile = new UniverseClassLoader();
			uLoaderForExecution = new UniverseClassLoader();
		} catch (Throwable t) {
			throw new SystemException("PANIC: Could not start the loader.", SystemException.PANIC_SYSTEM_LOADER_STARTUP_FAILED, t);
		}
	}
	
	/**
	 * Initialize the loader.  It will clear out any previous configuration, including the added sources.
	 * It should be harmless to do this whenever.
	 * @param cacheUniverse The universe in which to cache.
	 * @param cacheRoot The root for cached items.
	 * @throws SystemException
	 */
	public void init(Universe   cacheUniverse,   String  cacheRoot) throws SystemException {
		if (cacheUniverse==null) throw new SystemException("cacheUniverse cannot be null.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG);
		if (cacheRoot==null) throw new SystemException("cacheRoot cannot be null.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG);
		
		source = new  LinkedList<UniverseAnchor>();
		sourceIds = new HashSet<String>();
		internalTHINGCache = new HashMap<String, CachedItem>();
		try {
			universeCacheDescriptor = new UniverseAnchor(cacheRoot, cacheUniverse);
			
			// Associate out class loader with this anchor.
			uLoaderForCompile.init(universeCacheDescriptor, false);
			uLoaderForExecution.init(universeCacheDescriptor, true);
			
		} catch (Throwable t) {
			throw new SystemException("Failed to init loader.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG, t);
		}
	}
	
	/**
	 * Add a source universe.
	 * @param sourceUniverse The universe from where to load items.
	 * @param sourceRoot The root within the universe.
	 * @throws SystemException
	 */
	public void addSource(Universe   sourceUniverse,  String  sourceRoot) throws SystemException {
		if (sourceUniverse==null) throw new SystemException("sourceUniverse cannot be null.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG);
		if (sourceRoot==null) throw new SystemException("root addSource be null.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG);

		try {
			// Duplicate check
			String idString = sourceUniverse.getId().toString();
			if (sourceIds.contains(idString)) throw new SystemException("Duplicate source added.", SystemException.SYSTEM_LOADER_FAULT_DUPLICATE_CONFIG,
					ThingsNamespace.ATTR_UNIVERSE_ID, idString);
			
			// Add it.
			sourceIds.add(idString);
			UniverseAnchor newDescriptor = new UniverseAnchor(sourceRoot, sourceUniverse);
			source.add(newDescriptor);
		
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Failed to add source.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG, t);
		}
	}
	
	/**
	 * Purge the loader.  Any cached things will be dumped.  This affects the internal cache only.  The compiled cache in the system
	 * universe is left alone.
	 * @throws SystemException
	 */
	public synchronized void purge() throws SystemException {
		if (internalTHINGCache == null) SystemException.softwareProblem("KernelBasic_Loader was not init() before use.");
		internalTHINGCache = new HashMap<String, CachedItem>();
	}
	
	/**
	 * Purge a specific thing from the loader.  If cached, it will be cleared. This affects the internal cache only.  The compiled cache in the system
	 * universe is left alone.  
	 * This will never cause an error, unless there is an init() problem.
	 * @throws SystemException
	 */
	public synchronized void purgeThing(String path) throws SystemException {
		if (internalTHINGCache == null) SystemException.softwareProblem("KernelBasic_Loader was not init() before use.");
		if (internalTHINGCache.containsKey(path)) internalTHINGCache.remove(path);
	}
	
	/**
	 * Load a THING class.  It will take the cached version first.
	 * @param name to the THING.
	 * @return A class for that thing.  This is the binary name and it should reside in one of the registered 
	 * @throws SystemException
	 * @see things.thing.THING
	 */
	@SuppressWarnings("unchecked")
	public synchronized Class<THING> loadThing(String name) throws SystemException {
		Class<THING> result;
		File	sourceCandidate = null;
		File	systemCached = null;
		UniverseAnchor actualSourceUniverse = null;
		
		// Qualify.
		if (internalTHINGCache == null) SystemException.softwareProblem("KernelBasic_Loader was not init() before use.");
		if (name==null) throw new SystemException("Path cannot be null.", SystemException.SYSTEM_LOADER_ERROR_BAD_THING_NAME);
		if (source.size()<1) throw new SystemException("No source Universes added.", SystemException.SYSTEM_LOADER_FAULT_BAD_CONFIG);
	
		// Always read the cache first
		if (internalTHINGCache.containsKey(name)) {
			if (isVerbose()) screech("Loading from cache" + name);
			return internalTHINGCache.get(name).item;
		}
		
		try {
		
			// Go through the loader process.
			if (isVerbose()) screech("Loading " + name);
			
			// Qualify and transmogrify
			String fileName = ThingsUtilityBelt.binaryNameToFileName(name);
			
			// Find the source.
			try {		
				
				// Find it in the source and date it.  try with and without .java
				for (UniverseAnchor ud : source) {
					if ( ud.hasObject(fileName) ) {
						actualSourceUniverse = ud;
						sourceCandidate = ud.getLocal(fileName);
						break;	
					} else if (ud.hasObject(fileName + ".java")) {
						actualSourceUniverse = ud;
						sourceCandidate = ud.getLocal(fileName + ".java");
						break;
					}
				}
				
			} catch (Throwable t) {
				throw new SystemException("Loader failed inspecting sources.", SystemException.SYSTEM_LOADER_ERROR_UNIVERSE_PROBLEM, t, ThingsNamespace.ATTR_THING_NAME, name);
			} 
			
			// Does the source exist???  If not, we're done here.
			if (sourceCandidate==null) throw new SystemException("Thing does not exist.", SystemException.SYSTEM_LOADER_ERROR_CANT_FIND_THING, ThingsNamespace.ATTR_THING_NAME, name);
			
			// See if it is cached in the system.
			//String 	compiledName = name + ".class";
			if (universeCacheDescriptor.hasObject(fileName)) {
				
				// Has been compiled at some point
				try {
					systemCached = universeCacheDescriptor.getLocal(fileName);
				} catch (Throwable t) {
					throw new SystemException("Loader failed inspecting system cache.", SystemException.SYSTEM_LOADER_ERROR_UNIVERSE_PROBLEM, t, ThingsNamespace.ATTR_THING_NAME, name);
				}
				
				// See if it is up to date.
				if (systemCached.lastModified() < sourceCandidate.lastModified() ) {
					// In the system cache, but needs to be rebuilt.
					compile(name, sourceCandidate);
				}
					
			} else {
				
				// Never been compiled.
				systemCached = universeCacheDescriptor.getLocal(fileName);
				compile(name, sourceCandidate);			
			}
			
			// Load from system cache.	
			Class<?> loaded = uLoaderForExecution.findClass(name);
			
			// Validate
			boolean foundTHING = false;
			try {
				if (loaded.getSuperclass().getCanonicalName().equalsIgnoreCase("things.thing.THING")) foundTHING = true; 
			} catch (Throwable t) {
				// It'll leave foundTHING as false.
			}
			
			// If we indeed loaded an actual THING, then use it and cache it.
			if (foundTHING) {
				result = (Class<THING>)loaded;
				CachedItem newItem = new CachedItem(result, System.currentTimeMillis());
				internalTHINGCache.put(name, newItem);
				
			} else {
				throw new ClassNotFoundException("Thing name is not actually a THING.");
			}
			
		} catch (SystemException se) {
			throw se;
		} catch (Throwable  t) {
			throw new SystemException("Loader failed to load THING.", SystemException.SYSTEM_LOADER_ERROR_COULD_NOT_LOAD, t, ThingsNamespace.ATTR_THING_NAME, name);
		} finally {
			if (actualSourceUniverse!=null) actualSourceUniverse.releaseLocal(sourceCandidate);
			universeCacheDescriptor.releaseLocal(systemCached);
		}

		return result;
	}
	
	/**
	 * Load a MODULE class.
	 * @param name the name/path to the MODULE.  Depends on the loader being used, but typically it's a classpath.
	 * @return A class for that module.
	 * @throws SystemException
	 * @see things.thing.MODULE
	 */
	@SuppressWarnings("unchecked")
	public Class<MODULE> loadModule(String name) throws SystemException {
		// Let the 
		Class<MODULE> result = null ;
		
		try {		
			// Let the classpath find it.
			result = (Class<MODULE>)Class.forName(name);

		} catch (Throwable  t) {
			throw new SystemException("Loader failed to load MODULE (IDE).", SystemException.SYSTEM_LOADER_ERROR_COULD_NOT_LOAD, t, ThingsNamespace.ATTR_THING_NAME, name);
		} 
		return result;
	}
	
	// ===========================================================================================
	// TOOLS
	
	/**
	 * Run a compile.  Any error will result in an exception.  Code errors will result in an error, whereas compiler processing problems are faults.
	 * @param source The source file.  It CANNOT be null.
	 * @param name the thing name (class name)
	 * @throws SystemException
	 */
	private void compile(String name, File sourceFile) throws SystemException {
		boolean result;
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		UniverseFileManager universeManager = null;
		String intermediateSourceName = null;		// In case we have to whack the end result.
		
		try {

			compiler = ToolProvider.getSystemJavaCompiler();
			StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);
			universeManager = new UniverseFileManager(fileManager, universeCacheDescriptor, uLoaderForCompile);
	
			// Build the source ourselves.  Add .java to the source name if it isn't there.  We do this because the compiler is a goat.
			String sourceName;
			intermediateSourceName = name.replace('.', '/');
			if (!name.endsWith(".java")) {
				sourceName = intermediateSourceName + ".java";
			} else {
				sourceName = intermediateSourceName;
			}
			LinkedList<JavaFileObject> compilationUnits = new LinkedList<JavaFileObject>();
			compilationUnits.add( new LocalJavaFileObject(sourceName, name, Kind.SOURCE, sourceFile) );
			
			// Point it to the cache.
			//fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections.singleton(new File("C:\\dev\\things\\test\\universe\\system\\cache")));

			// Compile it
			JavaCompiler.CompilationTask task = compiler.getTask(null, universeManager, diagnostics, null, null, compilationUnits);
			result = task.call();
			
		} catch (Throwable t) {
			if (intermediateSourceName != null) {
				try {
					universeManager.releaseAllCaptured();		// Super important that this happens first
					thwackFailed(universeManager, intermediateSourceName);
				} catch (Throwable tt) {
					throw new SystemException("Compiler failed.  Clean-up failed.", SystemException.SYSTEM_LOADER_FAULT_COMPILER_FAILED, t, SystemNamespace.ATTR_PLATFORM_FS_PATH, sourceFile.getPath(), SystemNamespace.ATTR_DANGLE_FILE,intermediateSourceName, SystemNamespace.ATTR_DANGLE_CAUSE, ThingsException.toStringCauses(tt));					
				}
			}
			throw new SystemException("Compiler failed.", SystemException.SYSTEM_LOADER_FAULT_COMPILER_FAILED, t, SystemNamespace.ATTR_PLATFORM_FS_PATH, sourceFile.getPath());

		} finally {
			
			// Finish with the file manager
			try {
				universeManager.releaseAllCaptured();		// Super important that this happens.
				universeManager.close();
			} catch (Throwable t) {
				// Courtesy, so ignore problems.
			}
		}
			
		// Did we fail?
		if (result == false) {
			
			// Create report
			StringBuffer report = new StringBuffer();
			
			for (Diagnostic<? extends JavaFileObject> item : diagnostics.getDiagnostics()) {
				report.append(item.getKind().toString());
				report.append(':');
				report.append(item.getSource().getName());
				report.append(',');	
				report.append(item.getLineNumber());
				report.append(',');	
				report.append(item.getColumnNumber());
				report.append(':');
				report.append(item.getMessage(null));
				report.append(ThingsConstants.NEWLINE);
			}
			
			// Make sure we whack any leftover files if it failed.
			if (intermediateSourceName != null) 
				try {
					thwackFailed(universeManager, intermediateSourceName);
				} catch (Throwable tt) {
					throw new SystemException("Compiler failed.  Clean-up failed.", SystemException.SYSTEM_LOADER_ERROR_COMPILE_ERRORS, tt, SystemNamespace.ATTR_ERROR_COMPILATION,  report.toString(), SystemNamespace.ATTR_DANGLE_FILE,intermediateSourceName, SystemNamespace.ATTR_DANGLE_CAUSE, ThingsException.toStringCauses(tt));					
				}
			throw new SystemException("Compilation errors.", SystemException.SYSTEM_LOADER_ERROR_COMPILE_ERRORS, SystemNamespace.ATTR_ERROR_COMPILATION, report.toString());
		}		
		
	}
	
	// ===========================================================================================
	// PRIVATE TOOLS

	/**
	 * Thwack a failed compilation.  This is VERY serious, since we'll leave crap on the disk.  It's ok if the object was never there.
	 * @param ua the universe manager for where the object may be.
	 * @param name the object
	 * @throws SystemException if it can't be removed.
	 */
	private void thwackFailed(UniverseFileManager ua, String name) throws SystemException {
		
		// Finish with the file manager
		try {
			ua.releaseAllCaptured();		// Super important that this happens.
			ua.close();
		} catch (Throwable t) {
			// Courtesy, so ignore problems.
		}
		
		try {
			UniverseAnchor uaa = ua.getUniverse();
			uaa.getUniverseAccessor().delete(uaa.resolvePath(name));
		} catch (Throwable ue) {
			throw new SystemException("Failed to clean up failed compilation.", SystemException.PANIC_SYSTEM_LOADER_COULD_NOT_CLEAN_FAILED_COMPILE, ue, SystemNamespace.ATTR_SYSTEM_OBJECT_NAME, name);			
		}
		
	}
	
	// ===========================================================================================
	// INNER CLASSES
	
	/**
	 * An internal descriptor.
	 */
	private class CachedItem {
		Class<THING> item;
		//long	stamp;
		public CachedItem(Class<THING> item, long	stamp) {
			this.item = item;
//			this.stamp = stamp;
		}
		//public long getStamp() {
		//	return stamp;
		//}
	}
	
	// ===========================================================================================
	// VERBOSE IMPLEMENTATION
	
	// My poster.
	private StringPoster myPoster;
	
	/**
	 * Turn on.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
    	myPoster = poster;
    }
	/**
	 * Turn off the verbose mode.
	 */
	public void verboseOff() {
		myPoster = null;
	}
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation may find a 
	 * way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message) {
		if (myPoster!=null)	myPoster.postit(message);
	}
	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose() {
		if (myPoster == null) return false;
		return true;
	}

}
