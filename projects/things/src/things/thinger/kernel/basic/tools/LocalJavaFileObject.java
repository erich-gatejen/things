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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.tools.SimpleJavaFileObject;

import things.common.tools.FileTools;
import things.thinger.SystemException;

/**
 * This will be a local file object.

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 8 JUL 07
 * </pre> 
 */
public class LocalJavaFileObject extends SimpleJavaFileObject {

	// My file
	File	myFile;
	
	// My actual name
	String myActualName;
	
	/**
	 * Constructor.
	 * @param name Simple name for the object
	 * @param actualName The actual name of the file from the anchor.  This will include the extension and stuff if it was actually there.
	 * @param kind The kind of JavaFileObject.
	 * @param theFile The file that represents this object.
	 * @see javax.tools.SimpleJavaFileObject
	 * @throws Throwable
	 */
	public LocalJavaFileObject(String name, String actualName, Kind kind, File theFile) throws Throwable {
		super(new URI(name), kind);
		if (theFile==null) SystemException.softwareProblem("Cannot create a LocalJavaFileObject witha null file.");
		myFile = theFile;
		myActualName = actualName;
	}

	/**
	 * We need to do this for the damn compiler.
	 */
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException, IllegalStateException,	UnsupportedOperationException {
		return FileTools.loadFile2String(myFile);
	}

	/**
	 * Get the file as an InputStream.
	 * @return an input stream to the file.  You can assume it is buffered already.
	 */
	public InputStream openInputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
		return new BufferedInputStream(new FileInputStream(myFile));
	}

	/**
	 * Get the file as an OutputStream.
	 * @return an input stream to the file.  You can assume it is buffered already.
	 */
	public OutputStream openOutputStream() throws IOException, IllegalStateException, UnsupportedOperationException {
		return new BufferedOutputStream(new FileOutputStream(myFile));
	}
	
	/**
	 * Gets a user-friendly name for this file object.  
	 * @return the name
	 */
	public String getName() {
		return myActualName;
	}

}
