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

import things.common.ThingsException;

/**
 * A locator for a file system item.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 FEB 05
 * </pre> 
 */
public class FileSystemLocator {

		// ============================================================================
		// == DATA
		private AFileSystem theFileSystem;
		private String path;
		
		// ============================================================================
		// == METHODS
		
		/**
		 * Constructor
		 * @param theFileSystem
		 * @param path
		 * @throws Throwable
		 */
		public FileSystemLocator(AFileSystem theFileSystem, String path) throws ThingsException {
			if (theFileSystem==null) ThingsException.softwareProblem("FileSystemLocator constructed with null theFileSystem");
			if (path==null) ThingsException.softwareProblem("FileSystemLocator constructed with null path");			
			
			this.theFileSystem = theFileSystem;
			this.path = path;
		}

		/**
		 * GEt the file system.
		 * @return the file system.  It'll never be null.
		 * @see AFileSystem
		 */
		public AFileSystem getTheFileSystem() {
			return theFileSystem;
		}

		/**
		 * Get the path.
		 * @return the path.  It'll never be null.
		 */
		public String getPath() {
			return path;
		}

}

