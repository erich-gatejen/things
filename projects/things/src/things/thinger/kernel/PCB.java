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
package things.thinger.kernel;

import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;
import things.thinger.SystemException;

/**
 * A PROCESS CONTROL BLOCK.    
 * <p>
 * This is a process container for the Kernel.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 FEB 06
 * </pre> 
 */
public class PCB {
	
	// ========================================================================================================
	// DATA
	
	private ThingsProcess myProcess;
	private Clearance myClearance;
	private ThingsPropertyView myLocalProperties;
	private ThingsPropertyViewReader myConfigProperties;
	
	// Validators for JAVA Threads
	private long		threadId;
	private String		threadString;
	private int			threadHash;
	
	// ========================================================================================================
	// METHODS
	
	/**
	 * Constructor.  It will put the process under management.
	 * @param process The process to put under management.
	 * @param processClearance The process clearance.
	 * @param localProperties the local properties for the process.
	 * @param configProperties the read only configuration properties for the process.
	 * @see things.thinger.kernel.ProcessInterface
	 * @throws things.thinger.SystemException
	 * @see things.data.ThingsPropertyView
	 * @see things.data.ThingsPropertyViewReader
	 */
	public PCB(ThingsProcess process, Clearance processClearance, ThingsPropertyView localProperties, ThingsPropertyViewReader configProperties) throws SystemException {
		
		myProcess = process;
		if (process == null) SystemException.softwareProblem("A null process object was put under management of PCB()."); 
		myClearance = processClearance;
		if (localProperties == null) SystemException.softwareProblem("A null local properties object was put under management of PCB()."); 
		myLocalProperties = localProperties;
		if (configProperties == null) SystemException.softwareProblem("A null config properties object was put under management of PCB()."); 
		myConfigProperties = configProperties;
		
		// Thread controls
		threadId = process.getId();
		threadString = process.toString();
		threadHash = process.hashCode();
	}
	
	/**
	 * Get the resource.
	 * @return The resource.
	 */
	public ThingsProcess getProcess() {
		return myProcess;
	}
	
	/**
	 * Get the clearance.
	 * @return The clearance.
	 */
	public Clearance getClearance() {
		return myClearance;
	}	
	
	/**
	 * Get the process numeric ID.
	 * @return ID
	 */
	public long getProcessNumericId() {
		return threadId;
	}	
	
	/**
	 * Get the identity string.
	 * @return the identity string.
	 */
	public String getIdentityString() {
		// Use the JAVA Thread scheme.  This must be the same sequence as getCallerIdentityString();
		return new String(threadString + threadId + threadHash);
	}	
	
	/**
	 * Get the caller identity string.  This is how a caller process (thread) can ID itself using the same
	 * scheme that the PCB will use.
	 * @return the identity string.
	 */
	static public String getCallerIdentityString() {
		// Use the JAVA Thread scheme.  This must be the same sequence as getIdentityString();
		Thread me = Thread.currentThread();
		return new String(me.toString() + me.getId() + me.hashCode());
	}

	/**
	 * Get the local properties for this process.
	 * @return ThingsPropertyReader view of the local properties.
	 * @see things.data.ThingsPropertyView
	 */
	public ThingsPropertyView getMyLocalProperties() {
		return myLocalProperties;
	}	

	/**
	 * Get the configuration properties for this process.
	 * @return ThingsPropertyViewReader read only view of the configuration properties.
	 * @see things.data.ThingsPropertyViewReader
	 */
	public ThingsPropertyViewReader getMyConfigProperties() {
		return myConfigProperties;
	}	
}
