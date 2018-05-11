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
package things.thinger.service.httptool;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;

import things.common.ThingsConstants;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.data.ThingsPropertyReaderToolkit;
import things.thinger.SystemInterface;
import things.thinger.service.ServiceConstants;
import things.universe.Universe;
import things.universe.UniverseAddress;

/**
 * A serve manager.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 NOV 07
 * </pre> 
 */
public class ServeManager  {
	
	// =================================================================================================
	// == DATA
	//private SystemInterface	si;
	private Universe serveUniverse;
	private String root;

	// =================================================================================================
	// == METHODS=

	/**
	 * Constructor.  Makes good for template work.
	 * @throws Throwable for type mapping error.  Making this Throwable instead of error because eventually it'll be a configurable map.
	 */
	public ServeManager() throws Throwable {
		typeMapsCheck();
	}

	/**
	 * Initialize the manager.  This will be called by the system, so there is no need to do it yourself.
	 * @throws ThingsException by the init_chain if applicable.  The base will never throw it.
	 */
	public void init(SystemInterface	si) throws ThingsException {
	//	this.si = si;
		
		// Set the source.
		try {
			ThingsPropertyReaderToolkit propToolkit = new ThingsPropertyReaderToolkit(si.getLocalProperties());
			UniverseAddress uAddy = new UniverseAddress(propToolkit.getRequired(ServiceConstants.PAGE_MANAGER_UNIVERSE_ADDRESS));
			serveUniverse = si.getUniverse(uAddy.universeName);
			root = uAddy.path;
			
		} catch (Throwable t) {
			throw new ThingsException("Could not find or prepare for stock pages", ThingsException.SERVICE_FAULT_HTTPTOOL_STOCK_SETUP, t);
		}
	}
	
	/**
	 * Get the serve file that matches this path
	 * @param path the path.
	 * @return The file or null if there is no match.
	 * @throws Throwable
	 */
	public ServeFile get(String  path) throws Throwable {
		ServeFile result = null;
		InputStream source = null;
		String type = null;
		
		// Need to load it.
		try {
			if (serveUniverse.exists(root, path)) {
				source = new BufferedInputStream(serveUniverse.getStream(root, path));
				type = lookupExtension(path);
				result = new ServeFile(source, type, serveUniverse.size(root, path));
			}

		} catch (Throwable t) {
			throw new ThingsException("Could not get serve file.", ThingsException.SERVICE_HTTPTOOL_SERVE_FAILED, ThingsNamespace.ATTR_PROCESSING_HTTP_PATH, path);
		}
		return result;
	}
	
	
	// =============================================================================
	// TYPE ENGINE
	
	/**
	 * Categories by extensions.
	 */
	private static HashMap<String, FileType> extMap;
	
	/**
	 * A File Type.  Matches type to extensions.
	 */
	public class FileType  {
		String type;
		HashSet<String> extensions;
		
		public FileType(String type, String... extensions) {
			this.type = type;
			this.extensions = new HashSet<String>();
			for (String item : extensions) {
				this.extensions.add(item);
			}
		}
	}

	/*
	 * Look up an extension to see if it has a file type.
	 * @param path the path.  If null, the method will return the default..
	 * @return the type.  the default is "application/octet-stream" if lookup fails.
	 * @throws Throwable
	 */
	public String lookupExtension(String path) {
		String result= null;
		if (path!=null) { 
			try {
				String clipped = path;
				int index = path.lastIndexOf(ThingsConstants.PATH_SEPARATOR);
				if (index>0) clipped = path.substring(index+1);
				index = path.lastIndexOf('.');
				if (index>=0) {
					clipped = path.substring(index+1);
					result = extMap.get(clipped.toLowerCase()).type;
				}

			} catch (Throwable t) {
				// Oh well.  Let result stay null;
			}
		}
		if (result == null) result =  "application/octet-stream";
		return result;
	}
	
	// =============================================================================
	// INTERNAL	
	
	/**
	 * Do we have the type map?  Ff not, build it.  This hardcoded hax0r for now.
	 */
	private synchronized void typeMapsCheck() throws Throwable {
		if (extMap==null) {
			// Ugly hax hardcode for now.
			extMap = new HashMap<String, FileType>();

			putMap(new FileType("application/octet-stream", "exe", "bin"));
			putMap(new FileType("application/x-msdos-program", "bat", "cmd"));
			
			putMap(new FileType("text/plain", "txt", "text", "c", "asm", "java", "h", "cpp", "hpp", "ini"));
			putMap(new FileType("text/rtf", "rtf"));
			putMap(new FileType("application/msword", "doc", "docx"));
			putMap(new FileType("application/excel", "xls"));
			putMap(new FileType("application/vnd.ms-powerpoint", "ppt"));
			putMap(new FileType("application/pdf", "pdf"));

			putMap(new FileType("text/html", "html", "htm", "xhtml"));
			putMap(new FileType("text/xml", "xml"));
			putMap(new FileType("text/css", "css"));
			putMap(new FileType("application/xml-dtd", "dtd"));
			
			putMap(new FileType("application/java", "class"));
			putMap(new FileType("application/java-archive", "jar"));
			putMap(new FileType("application/zip", "zip"));

			putMap(new FileType("message/rfc822", "eml"));
			
			putMap(new FileType("image/gif", "gif"));
			putMap(new FileType("image/x-vga-bitmap", "vga"));
			putMap(new FileType("image/jpeg", "jpg", "jpeg", "jpe", "jpm"));
			putMap(new FileType("image/png", "png"));			
			
			putMap(new FileType("video/mp4", "mp4"));	
			putMap(new FileType("video/mpeg", "mpeg", "mpg"));	
			putMap(new FileType("video/ogg", "ogg", "ogg"));	

			putMap(new FileType("audio/mpeg", "mp3"));	
			putMap(new FileType("audio/wave", "wav"));	

		}
	}
	
	/**
	 * Put into the map helper.
	 * @param type the file type
	 * @throws Throwable
	 */
	private void putMap(FileType type) throws Throwable {
		String extensionNormal;
		for (String item : type.extensions) {
			// All extensions are normalized to lowercase
			extensionNormal = item.toLowerCase();
			if (extMap.containsKey(extensionNormal))  ThingsException.softwareProblem("Type Manager already has an entry with that file extension", ThingsNamespace.ATTR_DATA_ARGUMENT, extMap.get(item).toString());
			extMap.put(extensionNormal, type);
		}
	}
}
