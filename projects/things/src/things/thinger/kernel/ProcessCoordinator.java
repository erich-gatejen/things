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

import java.util.Collection;
import java.util.HashMap;

import things.common.WhoAmI;
import things.data.ThingsPropertyView;
import things.data.ThingsPropertyViewReader;
import things.data.tables.Table;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;

/**
 * A process coordinator.  This is mostly a helper for the Kernel.    
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Initial - 13FEB06
 * EPG - Change to use Clearance - 1 Dec 06
 * </pre> 
 */
public class ProcessCoordinator {

	// ======================================================================================================
	// DATA 

	private HashMap<String, PCB>	processes;
	private HashMap<String, PCB>	processesByOrganicId;
	
	@SuppressWarnings("unused")
	private WhoAmI					myId;	// Not used right now.

	// ======================================================================================================
	// METHODS
	
	/**
	 * Constructor.  Create the coordinator.
	 * @param id The ID of the ResourceCoordinator.
	 * @see things.common.WhoAmI
	 */
	public ProcessCoordinator(WhoAmI	id) throws SystemException {
		if (id==null) SystemException.softwareProblem("ProcessCoordinator constructed with a null id.");
		processes = new HashMap<String, PCB>();
		processesByOrganicId = new HashMap<String, PCB>();
		myId = id;
	}
	
	/**
	 * Register process.
	 * @param aProcess The process to register.
	 * @param processClearance The clearance level.  This will be immutable.
	 * @param localProperties properties visible to this process.
	 * @param configProperties configuration properties visible to this process.
	 * @throws things.thinger.SystemException 
	 * @return a PCB container for the process.
	 * @see things.data.ThingsPropertyView
	 *  @see things.data.ThingsPropertyViewReader
	 */
	public PCB registerProcess(ThingsProcess	aProcess, Clearance		processClearance, ThingsPropertyView localProperties, ThingsPropertyViewReader configProperties) throws  SystemException {
		
		PCB candidatePCB = null;
		
		// Validation step 1 - make sure we can get an ID.
		WhoAmI processID = aProcess.getProcessId();
		if (processID == null) throw new SystemException("Cannot register a process since it has a null id.",SystemException.KERNEL_FAULT_PROCESS_REGISTRATION_BAD);
	
		// Register it
		synchronized(processes) {
			
			// Make sure it is uniquely named.
			if (processes.containsKey(processID.toString())) 
				 throw new SystemException("Cannot register a process since another process has already been given the same ID (named).",SystemException.KERNEL_FAULT_PROCESS_ALREADY_NAMED,SystemNamespace.ATTR_PROCESS_ID,processID.toString());
			
			// Init it and put it under management.
			try {
				candidatePCB = new PCB(aProcess, processClearance, localProperties, configProperties);
				processes.put(processID.toString(),candidatePCB);
				
				// do numeric 
				String organicId = candidatePCB.getIdentityString();
				if (processesByOrganicId.containsKey(organicId)) {
					throw new SystemException("Process numeric is already in use.",SystemException.PANIC_KERNEL_ID_ALREADY_USED,SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId.toString());
				}
				
				// Ok.  Save it.
				processesByOrganicId.put(organicId,candidatePCB);
				
			} catch (Throwable t) {
				 throw new SystemException("Cannot register a process due to a fault.", SystemException.KERNEL_FAULT_PROCESS_REGISTRATION_BAD, t, SystemNamespace.ATTR_PROCESS_ID, processID.toString(), SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
			}

		}
		return candidatePCB;
	}
	
	/**
	 * Register a process as a ready PCB.  For consistancy sake, it is best to use the other registerProcess
	 * @param aPCB The process to register.
	 * @param processClearance The clearance level.  This will be immutable.
	 * @throws things.thinger.SystemException 
	 */
	public void registerProcess(PCB	aPCB, Clearance		processClearance) throws  SystemException {
		
		
		// Validation step 1 - make sure we can get an ID.
		WhoAmI processID = aPCB.getProcess().getProcessId();
		if (processID == null) throw new SystemException("Cannot register a process since it has a null id.",SystemException.KERNEL_FAULT_PROCESS_REGISTRATION_BAD);
	
		// Register it
		synchronized(processes) {
			
			// Make sure it is uniquely named.
			if (processes.containsKey(processID.toString())) 
				 throw new SystemException("Cannot register a process since another process has already been given the same ID (named).",SystemException.KERNEL_FAULT_PROCESS_ALREADY_NAMED,SystemNamespace.ATTR_PROCESS_ID,processID.toString());
			
			// Init it and put it under management.
			try {
				processes.put(processID.toString(),aPCB);
				
				// do numeric 
				String organicId = aPCB.getIdentityString();
				if (processesByOrganicId.containsKey(organicId)) {
					throw new SystemException("Process numeric is already in use.",SystemException.PANIC_KERNEL_ID_ALREADY_USED,SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId.toString());
				}
				
				// Ok.  Save it.
				processesByOrganicId.put(organicId,aPCB);
				
			} catch (Throwable t) {
				 throw new SystemException("Cannot register a process due to a fault.", SystemException.KERNEL_FAULT_PROCESS_REGISTRATION_BAD, t, SystemNamespace.ATTR_PROCESS_ID, processID.toString(), SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
			}

		}
	}
	
	/**
	 * Get the processes registered with the coordinator. 
	 * @return a collection of PCBs
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.PCB
	 */
	public Collection<PCB> getProcesses() throws SystemException {
	
		// Find and return it.
		return processes.values();
	}	
	
	/**
	 * Get a process by WhoAmID.  
	 * @param id  The WhoAmI ID of the process.
	 * @return the PCB for the process.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.PCB
	 */
	public PCB getProcess(WhoAmI	id) throws SystemException {
	
		// Validate id
		if (id == null) throw new SystemException("Process id cannot be null for getProcess(WhoAmI).",SystemException.PROCESS_ERROR_ID_NULL);
		
		// Find and return it.
		return getProcess(id.toString());
	}	
	
	/**
	 * Get a process by String ID.  This will usually be the same as WhoAmI.String(), but it isn't guaranteed.
	 * It will throw a SystemException.PROCESS_ERROR_NOT_FOUND if it isn't found.  
	 * @param id  The String representation of the ID.
	 * @return the PCB for the process.  It will never return a null.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.PCB
	 */
	public PCB getProcess(String	id) throws SystemException {
	
		PCB result = null;
		
		// Validate id
		if ( id == null) throw new SystemException("Process id cannot be null for getProcess(String).",SystemException.PROCESS_ERROR_ID_NULL);
		
		// Find and return it.
		synchronized(processes) {
			if (processes.containsKey(id)) {
				result = processes.get(id);
			} else {
				throw new SystemException("Process id(String) not registered.",SystemException.PROCESS_ERROR_NOT_FOUND,SystemNamespace.ATTR_PROCESS_ID,id);
			}
		}
		
		// Don't ever allow nulls.
		if (result==null) throw new SystemException("Process id(String) yielded a null object.  This is bad.",SystemException.KERNEL_FAULT_PROCESS_MANAGEMENT,SystemNamespace.ATTR_PROCESS_ID,id);
		
		return result;
	}	
	
	/**
	 * Remove a processes by String ID.  This will usually be the same as WhoAmI.String(), but it isn't guaranteed.  If the processes is
	 * not registered, it will quietly return.
	 * @param id  The WhoAmI ID of the process.
	 */
	public void removeProcess(WhoAmI	id) {
		
		// Use the local
		removeProcess(id.toString());
	}	

	
	/**
	 * Remove a processes by String ID.  This will usually be the same as WhoAmI.String(), but it isn't guaranteed.  If the processes is
	 * not registered, it will quietly return.
	 * @param id  The String representation of the ID.
	 */
	public void removeProcess(String	id) {
			
		// Validate id
		if ( id == null) return;
		
		// Find and remove it.
		synchronized(processes) {
			if (processes.containsKey(id)) {
				processes.remove(id);
			}
		}
	}	
	
	/**
	 * Get a process by its organic ID.  
	 * @param organicId  the organic ID.
	 * @return the PCB for the process.
	 * @throws things.thinger.SystemException
	 * @see things.thinger.kernel.PCB
	 */
	public PCB getProcessOrganic(String	organicId) throws SystemException {
		PCB result = null;
		
		// Validate id
		if ( organicId == null) throw new SystemException("Process organicId cannot be null for getProcess().",SystemException.PROCESS_ERROR_ID_NULL);
		
		// Find and return it.
		synchronized(processes) {
			if (processesByOrganicId.containsKey(organicId)) {
				result = processesByOrganicId.get(organicId);
			} else {
				throw new SystemException("Process organicId not registered.",SystemException.PROCESS_ERROR_NOT_FOUND,SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
			}
		}
		return result;
	}	
	
	/**
	 * Get a process table.  You provide the table and it will fill it.<br>
	 * 
	 * @param target A table to full.
	 * @throws things.thinger.SystemException
	 */
	public void  dumpProcessTable(Table<String> target) throws SystemException {
	
		// Validate tablle
		if (target == null) throw new SystemException("The target cannot be null for dumpProcessTable().",SystemException.PROCESS_ERROR_BAD_CALL);
		
		// Stuff the headers.
		target.setHeaders(PROCESS_TABLE_ENTRY1_TAG, PROCESS_TABLE_ENTRY2_ID, PROCESS_TABLE_ENTRY3_CLEARANCE, PROCESS_TABLE_ENTRY4_RUNTIME, PROCESS_TABLE_ENTRY5_NAME, PROCESS_TABLE_ENTRY6_STATE,PROCESS_TABLE_ENTRY7_STATE_NUMERIC);
		
		try {
			// Iterate the processes and fill in the table
			ThingsProcess currentProcess;
			for (PCB p : processes.values()) {
				currentProcess = p.getProcess();
				target.append(currentProcess.getProcessId().toTag(),currentProcess.getProcessId().toString(),p.getClearance().toString(),
						Long.toString(System.currentTimeMillis()-currentProcess.getStartTime()), currentProcess.getProcessName(),
						currentProcess.getCurrentState().getText(), Integer.toString(currentProcess.getCurrentStateNumeric())
						);
			}
			
		// General fault if this fails, which would be a bad thing.
		} catch (Throwable t) {
			throw new SystemException("Spurious exception while dumping process table.", SystemException.KERNEL_FAULT_PROCESS_MANAGEMENT, t,
					SystemNamespace.ATTR_PLATFORM_MESSAGE, t.getMessage());
		}
	}	
	
	 public final static String PROCESS_TABLE_ENTRY1_TAG = "tag";
	 public final static String PROCESS_TABLE_ENTRY2_ID = "id";	 
	 public final static String PROCESS_TABLE_ENTRY3_CLEARANCE = "clearance";
	 public final static String PROCESS_TABLE_ENTRY4_RUNTIME = "runtime";
	 public final static String PROCESS_TABLE_ENTRY5_NAME = "name";
	 public final static String PROCESS_TABLE_ENTRY6_STATE = "state";
	 public final static String PROCESS_TABLE_ENTRY7_STATE_NUMERIC = "state.numeric";
	 
	/**
	 * Get the calling processes ID.
	 * @return The ID of the calling process.
	 */
	public WhoAmI callerId() throws SystemException {
		
		// Find the process id
		String organicId = PCB.getCallerIdentityString();
		if (organicId == null) throw new SystemException("Process not recognized (during callerId).  It is null.",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED);
		
		// Find the PCB and extract the process id.
		if (processesByOrganicId.containsKey(organicId)) {
			// TODO wrap in exception handling.
			return processesByOrganicId.get(organicId).getProcess().getProcessId();
		} else {
			throw new SystemException("Process not recognized (during checkCredentials).",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED,
					SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
		}
	}
	 
	/**
	 * Check the clearance. 
	 * @param requiredLevel The level required by the calling thread to allow the operation.
	 * @return true if this processes can pass teh clearance check, otherwise false.
	 */
	public boolean checkClearance(Clearance	requiredLevel) throws SystemException {
		String organicId = null;
		boolean result = false;
		
		try {
			
			// Find the process id
			organicId = PCB.getCallerIdentityString();
			if (organicId == null) throw new SystemException("Process not recognized (during checkClearance).  It is null.",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED);
			
			// Find the PCB
			PCB candidateProcess;
			if (processesByOrganicId.containsKey(organicId)) {
				candidateProcess = processesByOrganicId.get(organicId);
			} else {
				throw new SystemException("Process not recognized (during checkClearance).",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED,
						SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
			}
			
			// Check credential
			result = candidateProcess.getClearance().pass(requiredLevel);
			
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Process checkClearance check failed due to error.",SystemException.KERNEL_FAULT_PROCESS_CREDENTIAL_FAULT,
					SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
		}
		return result;
	} 
	
	/**
	 * Require Clearance.  Failure will throw an exception.
	 * @param requiredLevel The level required by the calling thread to allow the operation.
	 * @param operationName The name of the operation.  This is used to identify an error should the clearance fail.
	 * @return The ID of the calling process.
	 */
	public WhoAmI requireClearance(Clearance	requiredLevel, String 	operationName) throws SystemException {
		
		String organicId = null;
		WhoAmI result = null;

		try {
			
			// Find the process id
			organicId = PCB.getCallerIdentityString();
			if (organicId == null) throw new SystemException("Process not recognized (during requireClearance).  It is null.",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED);
			
			// Find the PCB
			PCB candidateProcess;
			if (processesByOrganicId.containsKey(organicId)) {
				candidateProcess = processesByOrganicId.get(organicId);
			} else {
				throw new SystemException("Process not recognized (during requireClearance).",SystemException.KERNEL_FAULT_PROCESS_NOT_RECOGNIZED,
						SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
			}
			
			// Check credential
			if (candidateProcess.getClearance().pass(requiredLevel)) {
				result = candidateProcess.getProcess().getProcessId();
			} else {
				throw new SystemException("Process failed credential check.", SystemException.PROCESS_ERROR_CREDENTIAL_FAILED,
											SystemNamespace.ATTR_CLEARANCE_REQUIRED, requiredLevel.name(), SystemNamespace.ATTR_CLEARANCE_ACTUAL, candidateProcess.getClearance().name(),
											SystemNamespace.ATTR_SYSTEM_OPERATION_NAME, operationName);
			}
			
		} catch (SystemException se) {
			throw se;
		} catch (Throwable t) {
			throw new SystemException("Process credential check failed due to error.",SystemException.KERNEL_FAULT_PROCESS_CREDENTIAL_FAULT,
					SystemNamespace.ATTR_PROCESS_ID_ORGANIC,organicId);
		}
		
		// Return it
		return result;
	}
	
}
