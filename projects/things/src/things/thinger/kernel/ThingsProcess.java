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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import things.common.StringPoster;
import things.common.ThingsException;
import things.common.Verbose;
import things.common.WhoAmI;
import things.common.tools.Rendezvous;
import things.common.tools.StoplightMonitor;
import things.data.NV;
import things.data.NVImmutable;
import things.data.Data.Type;
import things.thing.Metrics;
import things.thing.RESULT;
import things.thinger.SystemException;
import things.thinger.SystemNamespace;
import things.thinger.SystemSuperInterface;
import things.thinger.io.Logger;

/**
 * The abstract base class for all manageable processes.  All processes are interruptible, so be sure they catch it.
 * <p>
 * You will need to implement ProcessInterface and Verbose interfaces.
 * <p>
 * You should fix() the process first.  Then init() it (this will start the thread).  Then release() it.   At that point, 
 * normal state management will work.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>
 * EPG - Adapted - 6 SEP 04
 * EPG - Added control interface - 26 JUN 05
 * </pre> 
 *
 */
public abstract class ThingsProcess extends Thread implements ProcessInterface, ControlInterface, ResourceListener, Verbose {

	/**
	 * Constants
	 */
	public static final String DEFAULT_NAME = "unnamed";

	/**
	 * Start time.
	 */
	private long startTime;

	/**
	 * Process name
	 */
	//private String name;

	/**
	 * Current state
	 */
	private ThingsState thingsState;
	
	/**
	 * Private state monitor.
	 */
	private Lock stateMonitor;

	/**
	 * System Super Interface
	 * 
	 * @see things.thinger.SystemSuperInterface
	 */
	public SystemSuperInterface ssi;

	/**
	 * The Kernel imposed ID. Off limits.
	 */
	private WhoAmI i_am = null;
	
	/**
	 * Verbose poster for just Process internal action logging.
	 */
	protected StringPoster myPostLogger;
	
	/**
	 * Start up staging.  This is so we can separate construction from run. 
	 */
	private StoplightMonitor startupStages;
	
	/**
	 * Startup Rendezvous
	 */
	private Rendezvous<Throwable> startupRendezvous;

	/**
	 * The internal result.  Subclasses can choose to change this, but otherwise it'll be INCONCLUSIVE.
	 */
	protected RESULT internalResult;
	
	/**
	 * Default Constructor.
	 */
	public ThingsProcess() {
		super();
		thingsState = ThingsState.STATE_CONSTRUCTION;
		stateMonitor = new ReentrantLock();
		startupStages = new StoplightMonitor();
		startupRendezvous = new Rendezvous<Throwable>();
		startupStages.turnRed();
	}

	// ***************************************************************************************************************	
	// ***************************************************************************************************************
	// * ABSTRACT METHODS

	/**
	 * This is the entry point for the actual processing.  It's ok to let interrupted exceptions leave.  It'll be the
	 * kernel or process that did it.
	 * @throws things.thinger.SystemException
	 */
	public abstract void executeThingsProcess() throws SystemException, InterruptedException;

	/**
	 * Complete construction. This will be called when the process is initialized.
	 * @throws things.thinger.SystemException
	 */
	public abstract void constructThingsProcess() throws SystemException;

	/**
	 * Destroy. This will be called when the Process is finalizing.
	 * @throws things.thinger.SystemException
	 */
	public abstract void destructThingsProcess() throws SystemException;

	/**
	 * Get process name.  It does not have to be a unique ID. 
	 * @return the name as a String
	 */
	public abstract String getProcessName();
	
	// ***************************************************************************************************************
	// ***************************************************************************************************************
	// * SERVICES

	/**
	 * Fix the process to an ID.  This is the FIRST thing you should do.  The init() will fail without it.
	 * @param you
	 *            the kernel imposed ID.  The underlying thread will be named by a String representation of this id.
	 * @throws things.thinger.SystemException
	 */
	final public synchronized void fix(WhoAmI you) throws SystemException {
		if (you == null) throw new SystemException("Cannot fix a process with a null id.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		// this.setName(you.toString());  Maybe do this.  Going to leave it to the subclasses right now.
		i_am = you;
		this.setName(you.toString());
		try {
			internalResult = new RESULT(new Metrics(1, 0, 0, 1, 0, 0, 1, 0, 0), Type.INCONCLUSIVE, you, you);
		} catch (Throwable t) {
			throw new SystemException("Fatal spurious exception while constructing process.", ThingsException.KERNEL_FAULT_PROCESS_ILL_CONSTRUCTED, t);
		}
	}
	
	/**
	 * Complete initialization. It will chain the initialization to the
	 * constructThingsProcess() method.  You must fix() the process first.
	 * @param theSSI
	 *            a supersystem interface
	 * @throws throwable
	 */
	final public synchronized void init(SystemSuperInterface theSSI)
			throws Throwable {
			
		// Validate
		if (i_am == null) throw new SystemException("Process cannot be init() before it is fix()'d.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		if (theSSI == null) throw new SystemException("Process cannot be init() with a null SystemSuperInterface.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		ssi = theSSI;
		
		// Start the process
		this.start();
		
		// Let the init run and wait for it to Rendezvous on the init result.  If there is an exception, throw it!
		startupStages.turnGreen();
		Throwable resultThrowable = startupRendezvous.enter();
		if (resultThrowable != null) throw new SystemException("Process init()failed.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED, resultThrowable);
	}
	
	/**
	 * Release the process for execution.  It will blow up only if it hasn't been init()'d.
	 */
	final public synchronized void release()
			throws SystemException {
			
		// Validate
		if (i_am == null) throw new SystemException("Process cannot be release()'d before it is fix()'d and init()'d.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		if (ssi == null) throw new SystemException("Process cannot be release()'d before it is init()'d.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		if (thingsState != ThingsState.STATE_NEW)  throw new SystemException("Process not ready to start.", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
		
		// Let the init run and wait for it to Rendezvous on the init result.  If there is an exception, throw it!
		startupStages.turnGreen();
	}

	/**
	 * Get start time from epoch.
	 * @return Returns the start time.
	 */
	final public long getStartTime() {
		return startTime;
	}
	
	/**
	 * @return Returns the ID.
	 */
	final public WhoAmI getProcessId() {
		return i_am;
	}
	
	// ***************************************************************************************************************
	// ***************************************************************************************************************
	// * STATE MANAGEMENT

	/**
	 * Get the process state.
	 * <p>
	 * @return the state
	 * @throws things.thinger.SystemException
	 */
	public ThingsState getThingsState() throws SystemException {
		return thingsState;
	}
	
	/**
	 * Request that the process is paused. It will return true if it is already
	 * paused (but not if a request is just pending). The request will be
	 * ignored if the current state cannot be paused. Only a RUNNING process
	 * will respond to a pause. There is no count on this.
	 * <p>
	 * @return true if the process is already paused, else false
	 * @throws things.thinger.SystemException
	 */
	public boolean requestPause() throws SystemException {
		boolean result = false;
		try {
			if (myPostLogger!=null)myPostLogger.postit("Pause requested on " + this.i_am.toString());
			
			KernelTools.lockOnTimer(stateMonitor);
			if (thingsState == ThingsState.STATE_PAUSED)
				result = true;
			else if (thingsState == ThingsState.STATE_RUNNING)
				thingsState = ThingsState.STATE_PAUSE_REQUESTED;
		} catch (SystemException ke) {
			throw ke;
		} finally {
			KernelTools.unlockCarefree(stateMonitor);
		}
		return result;
	}

	/**
	 * Allow it to resume. If the process is not in a state that can be run, it
	 * will ignore this release. It will not override a halt request.
	 * @throws things.thinger.SystemException
	 */
	public void releasePause() throws SystemException {
		try {
			KernelTools.lockOnTimer(stateMonitor);

			if ((thingsState == ThingsState.STATE_PAUSED) || (thingsState == ThingsState.STATE_PAUSE_REQUESTED)) {
				thingsState = ThingsState.STATE_RUNNING;
			}
			synchronized(stateMonitor) {
				stateMonitor.notifyAll(); // Should be locked
			}
		} catch (SystemException ke) {
			throw ke;
		} finally {
			KernelTools.unlockCarefree(stateMonitor);
		}
	}

	/**
	 * Request halt. It will interrupt a pause.  It will ultimately cause a PANIC
	 * to propagate through the process user.  If the process isn't running or
	 * paused, it will be ignored.
	 * @throws things.thinger.SystemException
	 */
	public void requestHalt() throws SystemException {
		try {
			KernelTools.lockOnTimer(stateMonitor);

			if (thingsState == ThingsState.STATE_PAUSED) {
				thingsState = ThingsState.STATE_HALT;
				stateMonitor.notifyAll();
			} else if (thingsState == ThingsState.STATE_RUNNING) {
				thingsState = ThingsState.STATE_HALT_REQUESTED;
			}
		} catch (SystemException ke) {
			throw ke;
		} finally {
			KernelTools.unlockCarefree(stateMonitor);
		}
	}
	
	/**
	 * Force halt.
	 * @throws things.thinger.SystemException
	 */
	public void forceHalt() throws SystemException {
		try {
			KernelTools.lockOnTimer(stateMonitor);

			if (thingsState == ThingsState.STATE_PAUSED) {
				thingsState = ThingsState.STATE_HALT;
				stateMonitor.notifyAll();
			} else {
				thingsState = ThingsState.STATE_HALT;
				// TODO: Make sure this is enforced and the process didn't just eat it.
				this.interrupt();
			}
		} catch (SystemException ke) {
			throw ke;
		} finally {
			KernelTools.unlockCarefree(stateMonitor);
		}
	}
		
	/**
	 * Get the latest result.  Overrides the basic implementation in the ThingsProcess.
	 * @return the latest or the last result for the thread.  It is completely up to the implementation how to implement this.
	 * @throws ThingsException for whatever reason. It may come from the THING itself.
	 */
	public RESULT getResult() throws ThingsException {
		return internalResult;
	}
		
	/**
	 * This thread will accept a pause request.
	 * @throws things.thinger.SystemException
	 */
	public void acceptPause() throws SystemException {
		// Must be very careful or this will deadlock.
		try {
			KernelTools.lockOnTimer(stateMonitor);
			if (thingsState == ThingsState.STATE_PAUSE_REQUESTED) {
				// pause is on
				thingsState = ThingsState.STATE_PAUSED;
				KernelTools.unlockCarefree(stateMonitor);
				// TINY room for a race condition here
				while (thingsState == ThingsState.STATE_PAUSED) {
					synchronized(stateMonitor) {
						stateMonitor.wait();
					}
					if (thingsState == ThingsState.STATE_HALT) throw new SystemException("Halt ordered while paused", SystemException.PANIC_PROCESS_RESPONDING_TO_HALT_OK);
				}
			} 
			
		} catch (IllegalMonitorStateException ime) {
			// Frick. This means the race condition is more than TINY
			throw new SystemException(
					"Bug in Process.acceptPause().  IllegalMonitorStateException means there is a race condition.",
					SystemException.PANIC_THINGER_BUG);
		} catch (InterruptedException ie) {
			// Abandon the state. Assume another thread is barging in.
		} catch (SystemException ke) {
			throw ke;
		} finally {
			// Make sure we've unlocked!
			KernelTools.unlockCarefree(stateMonitor);
		}
	}

	/**
	 * This will accept the halt if a halt is pending.  THIS WILL THROW A 
	 * SystemException.PANIC_PROCESS_RESPONDING_TO_HALT_OK if the halt is accepted!
	 * @throws things.thinger.SystemException
	 */
	public void acceptHalt() throws SystemException {
		// Must be very careful or this will deadlock.
		try {
			KernelTools.lockOnTimer(stateMonitor);
			if (thingsState == ThingsState.STATE_HALT_REQUESTED) {
				// pause is on
				thingsState = ThingsState.STATE_HALT;
				// TINY room for a race condition here
				throw new SystemException("Halt ordered and accepted.", SystemException.PANIC_PROCESS_RESPONDING_TO_HALT_OK);
			}
		} catch (SystemException ke) {
			throw ke;
		} finally {
			KernelTools.unlockCarefree(stateMonitor);
		}
	}
	
	/**
	 * Get the state.
	 * @return Returns the state.
	 */
	public ThingsState getCurrentState() {
		return thingsState;
	}
	
	/**
	 * Get the state as a numeric.
	 * @return Returns the state numeric.
	 */
	public int getCurrentStateNumeric() {
		return thingsState.getNumeric();
	}
	
	// --- STATE CHANGE ----------------------------------------
	
	// ***************************************************************************************************************
	// ***************************************************************************************************************
	// * INTERNAL

	/**
	 * RUN implementation. Do not call directly.  There are two phases--init and run.
	 */
	public void run() {
		
		// =====================================================================================================================
		// =====================================================================================================================
		// == INIT 
		
		// Hold until we've been told to go.
		startupStages.stoplight();
		
		// Process it.  We will assume the result is A-OK.
		try {

			// Fixed?
			if (i_am == null) throw new SystemException("Process was not fix()'d before init().", SystemException.SYSTEM_FAULT_PROCESS_INIT_FAILED);
			
			// Log.
			if (myPostLogger!=null)myPostLogger.postit("Process Init() " + i_am.toString() + " started.");
			
			// No lock required. If for some strange reason the state is
			// anything but CONSTRUCTION, then someone is a very bad programmer.
			if (thingsState != ThingsState.STATE_CONSTRUCTION) {
				synchronized(stateMonitor) {
						stateMonitor.notifyAll();
				}
				throw new SystemException(
						"Process state modified before init().  The init() method must be run before any interaction with the Process, including a start().",
						SystemException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
			}
			thingsState = ThingsState.STATE_FOUL;
			
			// Chain the construction
			if (myPostLogger!=null)myPostLogger.postit("Process Init() " + i_am.toString() + " chaining construction.");
			this.constructThingsProcess();
			
			// Done
			thingsState = ThingsState.STATE_NEW;
			if (myPostLogger!=null)myPostLogger.postit("Process Init() " + i_am.toString() + " completed.");
			
		} catch (Throwable ee) {
			// Tell the initializing process that it didn't go well.  And give up.
			thingsState = ThingsState.STATE_INVALID;
			try {
				startupRendezvous.meet(ee);
			} catch (Throwable r) {
				// Doesn't matter at this point.
			}
			return;
		}
		// Tell the initializing process that it worked ok.
		startupStages.turnRed();
		try {
			startupRendezvous.meet(null);
		} catch (Throwable t) {
			// Doesn't matter at this pojnt.
		}
		
		// =====================================================================================================================
		// ====================================================================================================================
		// == RUN
		
		// Our death notice.
		ThingsException deathNotice = null;
		
		// Hold until we've been told to go.
		startupStages.stoplight();
		
		try {
			if (myPostLogger!=null)myPostLogger.postit("Process " + i_am.toString() + " starting run.");
			
			// Change the state to running.
			stateMonitor.lock();
			if (thingsState != ThingsState.STATE_NEW) {
				throw new SystemException(
						"Process started before init() complete or some other thread has locked the state.",
						SystemException.SYSTEM_FAULT_SOFTWARE_PROBLEM);
			}
			thingsState = ThingsState.STATE_RUNNING;

			// Tag start time
			startTime = System.currentTimeMillis();
			if (myPostLogger!=null)myPostLogger.postit("Process " + i_am.toString() + " start time set as " + startTime);
			
			// Run it!
			KernelTools.unlockCarefree(stateMonitor);
			this.executeThingsProcess();
			
			// Last one out turn off the lights...
			stateMonitor.lock();
			if (myPostLogger!=null)myPostLogger.postit("Process " + i_am.toString() + " execution complete.  Quitting");
			thingsState = ThingsState.STATE_DONE;
			KernelTools.unlockCarefree(stateMonitor);

		} catch (InterruptedException ie) {
			this.screech("Process was interrupted.  Killing it.");
			thingsState = ThingsState.STATE_KILLED;
		} catch (SystemException see) {
			if (see.numeric == SystemException.PANIC_PROCESS_RESPONDING_TO_HALT_OK) {
				// If this was a HALT request, ignore the exception
				thingsState = ThingsState.STATE_DONE;
			} else {
				deathNotice = see;
				thingsState = ThingsState.STATE_FOUL;	
			}
//		} catch (ThingsException tee) {
//			deathNotice = tee;
//			thingsState = ThingsState.STATE_FOUL;
		} catch (Throwable ee) {
			if (myPostLogger!=null)myPostLogger.postit("Throwable in " + i_am.toString() + " starting run.");
			deathNotice = new SystemException(
						"Process exited to unhandle exception.  message=" + ee.getMessage(),
						SystemException.SYSTEM_FAULT_PROCESS_UNHANDLED, ee);
			thingsState = ThingsState.STATE_FOUL;
		} 

		// Clean up.  The Kernel might do this, but why chance it?
		try {
			Logger myLogger = ssi.getSystemLogger();
			
			if (deathNotice != null) myLogger.error(deathNotice.getMessage(), deathNotice.numeric, (NVImmutable[])deathNotice.getAttributesNVMulti(new NV(SystemNamespace.ATTR_PLATFORM_TRACE, deathNotice.toStringComplex())));
			myLogger.flush();
		} catch (Throwable tt) {
			// Best effort, so don't care.
		}
	
		// == NO ADDITIONS PAST THIS!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		ssi.deathNotice(deathNotice);
	}

	// ================================================================================================================
	// == VERBOSE INTERFACE
	
	/**
	 * Turn on the verbose mode.  It will test the poster and will throw a ThingsException
	 * if it has a problem.
	 * @param poster StringPoster where to put the debug info
	 * @throws ThingsException
	 */  
    public void verboseOn(StringPoster poster) throws ThingsException {
    	myPostLogger = poster;
    }
    
	/**
	 * Turn off the verbose mode.
	 */
	public void verboseOff() {
		myPostLogger = null;
	}
	
	/**
	 * Post a verbose message if verbose mode is on.  It will never throw an exception.  The implementation may find a 
	 * way to report exceptions.
	 * @param message The message.
	 */
	public void screech(String	message){
		if (myPostLogger != null) {
			myPostLogger.postit(message);
			myPostLogger.flush();
		}
	}

	/**
	 * Is it set to verbose?
	 * @return true if it is verbose, otherwise false.
	 */
	public boolean isVerbose() {
		 if (myPostLogger==null) return false;
		 return true;
	}
	
	// ================================================================================================================
	// == FINALIZER
	
	/**
	 * Force finalization.  This should be done by 
	 */
	public void forceFinalize() throws SystemException {
		try {
			this.finalize();
		} catch (Throwable t) {
			if (t instanceof SystemException ) {
				throw new SystemException("Processes finalization failed.", SystemException.PROCESS_ERROR_FINALIZATION_FAILED, (SystemException)t);
			}
			throw new SystemException("Processes finalization failed.", SystemException.PROCESS_ERROR_FINALIZATION_FAILED, t);
		}
		
	}
	
	/**
	 * finalizer.
	 */
	protected void finalize() throws Throwable {
		if (finalizeproc == true)
			return;
		
		// Destruction process
		if (myPostLogger!=null)myPostLogger.postit("Process " + i_am.toString() + " destruction started.");
		this.destructThingsProcess();
		if (myPostLogger!=null)myPostLogger.postit("Process " + i_am.toString() + " destruction complete.");
		
		super.finalize();
	}
	private boolean finalizeproc = false;

}
