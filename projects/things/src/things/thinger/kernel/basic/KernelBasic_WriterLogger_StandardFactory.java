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

import java.io.PrintWriter;

import things.common.WhoAmI;
import things.thinger.SystemException;
import things.thinger.io.AFileSystem;
import things.thinger.io.Logger.LEVEL;
import things.thinger.io.Logger.TYPE;

/**
 * The standard log factory for the kernel.

 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 NOV 05
 * </pre> 
 */
public class KernelBasic_WriterLogger_StandardFactory  implements KernelBasic_WriterLogger_Factory {
	
	/**
	 * Create a filesystem based logger.
	 * @param owner The owner of the log.
	 * @param path The path into the filesystem for the log.
	 * @param fs a filesystem
	 * @param configuredLevel The configured logging level.
	 */
	public KernelBasic_WriterLogger forgeFileLogger(WhoAmI owner, String path, AFileSystem fs, LEVEL configuredLevel) throws SystemException {
		KernelBasic_WriterLogger candidate = null;
		
		try {
					
			// Build it
			candidate =  new KernelBasic_WriterLogger();
			PrintWriter pow = new PrintWriter(fs.openOutputStream(path));	
			candidate.init(pow, owner, configuredLevel);
			candidate.init(TYPE.BROADCAST);
			
		} catch (SystemException se) {
			throw new SystemException("Could not forge a file Logger.",SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER,se);			
		} catch (Throwable t) {
			throw new SystemException("Could not forge a file Logger due to spurious exception.",SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER,t);
		}
		
		// Done
		return candidate;
	}

}
