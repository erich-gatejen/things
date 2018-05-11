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
package test.system.testtools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import things.common.WhoAmI;
import things.thinger.SystemException;
import things.thinger.io.AFileSystem;
import things.thinger.io.Logger.LEVEL;
import things.thinger.io.Logger.TYPE;
import things.thinger.kernel.basic.KernelBasic_WriterLogger;
import things.thinger.kernel.basic.KernelBasic_WriterLogger_Factory;

/**
 * The interception log factory for the kernel to replace the standard factory during testing.
 * <br>
 * It assumes that any process will have a logger forged for it by name only once!  If the kernel does another one, there could be troubles.
 * <p>
 * I'm going to synchronize everything, since the forge process is infrequent, rather than risk bugs with a fine grain.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 JUN 05
 * </pre> 
 */
public class KernelBasic_WriterLogger_NamedInterceptionFactory implements KernelBasic_WriterLogger_Factory {

	/**
	 * Initial buffer size for the interceptor.
	 */
	private final static int INTERCEPTOR_BUFFER_SIZE  = 2048;
	
	/**
	 * Create a filesystem based logger.
	 * @param owner The owner of the log.
	 * @param path The path into the filesystem for the log.
	 * @param fs a filesystem
	 * @param configuredLevel The configured logging level.
	 */
	public synchronized KernelBasic_WriterLogger forgeFileLogger(WhoAmI owner, String path, AFileSystem fs, LEVEL configuredLevel) throws SystemException {
		KernelBasic_WriterLogger candidate = null;
		
		try {
			
			// The real desination
			OutputStream outs = fs.openOutputStream(path);
			InterceptedStream finalStream = null; 
			
			// Are we waiting for an interception?
			String id = owner.toString();
			if (catalog.containsKey(id)) {
				// Interception has been set up.  Are we waiting?  
				InterceptionDescriptor existingInterception = catalog.get(id);
				if (existingInterception.state == InterceptionState.WAITING_LOGGING) {
					// Waiting to log.  Attach it.
					existingInterception.originalStream = outs;	
					InterceptedStream interceptedStream = new InterceptedStream(existingInterception.interceptedBuffer);
					existingInterception.interceptorStream = interceptedStream;
					existingInterception.state = InterceptionState.INTERCEPTED;
					finalStream = interceptedStream;
					
				} else {
					// This is bad.  It means someone set up an interceptor for this logger, it was attached, and now it is being 
					// forged again!
					SystemException.softwareProblem("BUG: forgeFileLogger called for an already existing InterceptionDescriptor that is not WAITING_LOGGING");
				}
				
			} else {
				// No interception.  So set up a descriptor.
				InterceptionDescriptor newInterception = new InterceptionDescriptor();
				InterceptedStream interceptedStream = new InterceptedStream(outs);
				newInterception.originalStream = outs;
				newInterception.interceptorStream = interceptedStream;
				newInterception.state = InterceptionState.LOGGING;
				finalStream = interceptedStream;
				
				catalog.put(id, newInterception);
			}
			
			// Build it
			candidate =  new KernelBasic_WriterLogger();
			PrintWriter pow = new PrintWriter(finalStream);	
			candidate.init(pow, owner, configuredLevel);
			candidate.init(TYPE.BROADCAST);
			
		} catch (SystemException se) {
			throw new SystemException("Could not forge a file Logger (INTERCEPTION).",SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER,se);			
		} catch (Throwable t) {
			throw new SystemException("Could not forge a file Logger (INTERCEPTION) due to spurious exception.",SystemException.KERNEL_FAULT_COULD_NOT_FORGE_LOGGER,t);
		} finally {
			this.notifyAll();  // in case someone is waiting for an interception.
		}
		
		// Done
		return candidate;
	}

	// =================================================================================================
	// == INTERCEPTION CONTROLLERS

	/**
	 * Default constructor.  It makes sure we can get the interceptor reference.  we're counting on the kernel only 
	 * having ONE factory!  Any more can this gets dicey.
	 */
	public KernelBasic_WriterLogger_NamedInterceptionFactory() {
		super();
		thisInterceptor = this;
		catalog = new HashMap<String, InterceptionDescriptor>();
	}
	
	/**
	 * Get a reference to the interceptor.
	 * @return
	 */
	public static KernelBasic_WriterLogger_NamedInterceptionFactory getInterceptor() {
		return thisInterceptor;
	}
	
	/**
	 * The static reference to the service.  We're doing this so the testing system can access.  Eventually
	 * we'll move this behind the kernel for security.
	 */
	private static KernelBasic_WriterLogger_NamedInterceptionFactory thisInterceptor;
	
	
	// =================================================================================================
	// == INTERCEPTION DATA
	
	/**
	 * Catalog of intercepted things.
	 */
	private HashMap<String, InterceptionDescriptor> 	catalog;
	
	/**
	 * Interception states.
	 */
	private enum InterceptionState {
		INTERCEPTED, LOGGING, WAITING_LOGGING
	}
	
	
	// =================================================================================================
	// == INTERCEPTION METHODS
	
	/**
	 * Intercept based on the name (whois of the process).
	 * @param name The name of the process to intercept (likely the whoami).
	 * @returns an InputStream that will get a refernce to the interception buffer (ByteArrayOutputStream) or NULL if it can't find the named logger or it cannot attach the pipe.
	 */
	public synchronized ByteArrayOutputStream intercept(String name) {
		ByteArrayOutputStream result = null;
		if (catalog.containsKey(name)) {
			// Log already created, so just attach.
			try {
				InterceptionDescriptor descriptor = catalog.get(name);
				
				// States
				switch (descriptor.state) {
				case INTERCEPTED:
				case WAITING_LOGGING:
					// Already done.
					result = descriptor.interceptedBuffer;
					break;
					
				case LOGGING:
					// Log is running, so intercept it.
					if (descriptor.interceptedBuffer == null) descriptor.interceptedBuffer = new ByteArrayOutputStream(INTERCEPTOR_BUFFER_SIZE);
					descriptor.interceptorStream.setOutputStream(descriptor.interceptedBuffer);
					descriptor.state = InterceptionState.INTERCEPTED;
					result = descriptor.interceptedBuffer;
					break;
				}
				
			} catch (Exception e) {
				// Do nothing and it will return a null
			}
		} else {
			// Log not created, so make a stub.
			InterceptionDescriptor newInterception = new InterceptionDescriptor();			
			newInterception.interceptedBuffer = new ByteArrayOutputStream(INTERCEPTOR_BUFFER_SIZE);
			result = newInterception.interceptedBuffer ;
			newInterception.state = InterceptionState.WAITING_LOGGING;
			catalog.put(name, newInterception);	
		}
		this.notifyAll();  // in case someone is waiting for an interception.
		return result;
	}
	
	/**
	 * Release an interception.  If it isn't intercepted, it'll quietly succeed anyway.  If the log was never created, it'll forget the intercept() request ever happened.
	 * @param name The name of the process to release the intercept (likely the whois).
	 * @author erich
	 */
	public synchronized void release(String name) {
		if (catalog.containsKey(name)) {
			InterceptionDescriptor descriptor = catalog.get(name);
			
			// Only bother if it is intercepted.  The flush the current (and intercepted) pipe and then swap back the original stream.
			if (descriptor.state == InterceptionState.INTERCEPTED) {
				try {
					descriptor.interceptorStream.currentOutput.flush();
				} catch (Exception e) {
					// Don't care, really.  We're abandoning it.
				}
				descriptor.interceptorStream.currentOutput = descriptor.originalStream;
				descriptor.state = InterceptionState.LOGGING;
				
			} else if (descriptor.state == InterceptionState.WAITING_LOGGING) {
				// Forget it ever happened.
				catalog.remove(name);
			}
		}
		this.notifyAll();  // in case someone is waiting for an interception.
	}
	
	/**
	 * Ask for the string value for an interceptor.  It'll return if it was never intercepted.
	 * @param name The name of the process that was (maybe) intercepted (likely the whoami).
	 * @param clear If true and possible, it will clear the interception buffer after getting the value.  It will only be possible if it was release()'d.  Otherwise, it is still best-effort.
	 * @return the String value or null
	 * @author erich
	 */
	public synchronized String requestString(String name, boolean  clear) {
		String result = null;
		if (catalog.containsKey(name)) {
			InterceptionDescriptor descriptor = catalog.get(name);
			
			// Only bother if it was intercepted 
			if (descriptor.interceptedBuffer != null) {
				result = descriptor.interceptedBuffer.toString();
				if (clear && (descriptor.state != InterceptionState.INTERCEPTED)) {
					descriptor.interceptedBuffer = null;
				}
			}
		}
		this.notifyAll();  // in case someone is waiting for an interception.
		return result;
	}
	
	/**
	 * Wait for a named interception.  Only a InterruptedException or the timeout will free this.
	 * @param name The name of the process to intercept (likely the whois).
	 * @param timeout The timeout period for each check.  Whenever the kernel forges a log or any log is intercepted (not just this one), the timer will be reset.  So, the actual timeout can be much longer.
	 * @return true if it has been intercepted, false if it timeout or was interrupted.
	 * @author erich
	 */
	public synchronized boolean waitInterception(String name, long timeout) {
		boolean result = false;
		
		try {
			while(result == false) {
				
				// Is it in the catalog and intercepted?
				if ((catalog.containsKey(name) && (catalog.get(name).state == InterceptionState.INTERCEPTED) )) {
					// Yes.  leave.
					result = true;
					
				} else {
					// No, wait for something to happen.
					this.wait(timeout);
				}
			}
		
		} catch (Throwable t) {
			// It was interrupted somehow, so let the false return.
		}
		return result;
	}
	
	
	// =================================================================================================
	// == INTERCEPTION CLASSES
	
	private class InterceptionDescriptor {
		public InterceptionState		state = InterceptionState.WAITING_LOGGING;
		public OutputStream 			originalStream;
		public InterceptedStream 		interceptorStream;
		public ByteArrayOutputStream	interceptedBuffer;
		
	}
	
	/**
	 * Inner-class for an intercepted stream.
	 * @author erich
	 */
	public class InterceptedStream extends OutputStream {
		
		// Current output stream;
		OutputStream  	currentOutput;
		
		/**
		 * Set a new output stream.
		 * @param outs
		 */
		public void setOutputStream(OutputStream  outs) {
			currentOutput = outs;
		}
		
		/**
		 * Constructor
		 * @param outs starting output stream.
		 * @param intercepted true if it is intercepted (don't let closes propigate).
		 */
		public InterceptedStream(OutputStream  outs) {
			super();
			currentOutput = outs;
		}
		
		 public void close() throws IOException {
			 currentOutput.close();
		 }
		
         public void flush() throws IOException {
        	 currentOutput.flush();	 
         }
         
         public void write(byte[] b) throws IOException {
        	currentOutput.write(b);
         }

         public void write(byte[] b, int off, int len) throws IOException {
        	 currentOutput.write(b, off, len);	 
         }

         public void write(int b) throws IOException {
        	 currentOutput.write(b);
         }     
	}
	
}
