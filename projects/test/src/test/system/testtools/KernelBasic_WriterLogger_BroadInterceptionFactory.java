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

import things.common.WhoAmI;
import things.thinger.SystemException;
import things.thinger.io.AFileSystem;
import things.thinger.io.Logger.LEVEL;
import things.thinger.io.Logger.TYPE;
import things.thinger.kernel.basic.KernelBasic_WriterLogger;
import things.thinger.kernel.basic.KernelBasic_WriterLogger_Factory;

/**
 * The broad interceptor will intercept ALL logs and channel it into a single stream.
 * <p>
 * It has a 128k buffer.  It needs to be cleaned regularly, if you log a lot.
 * <p>
 * Note that the interceptionStream operations aren't synchronized.  So, it is possible that a few writes could 
 * sneak past a clear while getting the buffer.  This shouldn't be a problem for testing.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 20 JUL 07
 * </pre> 
 */
public class KernelBasic_WriterLogger_BroadInterceptionFactory implements KernelBasic_WriterLogger_Factory {

	/**
	 * Initial buffer size for the interceptor.
	 */
	private final static int INTERCEPTOR_BUFFER_SIZE  = 1024*128;
	
	// =================================================================================================
	// == INTERFACE
	
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
			
			// The real destination
			OutputStream outs = fs.openOutputStream(path);
			SplitInterceptedStream finalStream = new SplitInterceptedStream(outs, interceptionStream); 
			
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
	public KernelBasic_WriterLogger_BroadInterceptionFactory() {
		super();
		interceptionStream =  new ByteArrayOutputStream(INTERCEPTOR_BUFFER_SIZE);
		thisInterceptor = this;
	}
	
	/**
	 * Get a reference to the interceptor.
	 * @return
	 */
	public static KernelBasic_WriterLogger_BroadInterceptionFactory getInterceptor() {
		return thisInterceptor;
	}
	
	/**
	 * The static reference to the service.  We're doing this so the testing system can access.  Eventually
	 * we'll move this behind the kernel for security.
	 */
	private static KernelBasic_WriterLogger_BroadInterceptionFactory thisInterceptor;
	
	
	// =================================================================================================
	// == INTERCEPTION DATA

	/**
	 * Where all the intercepted data goes.
	 */
	private ByteArrayOutputStream	interceptionStream;
	
	// =================================================================================================
	// == INTERCEPTION METHODS
	
	/**
	 * Clear the current interception buffer.
	 */
	public void clear() {
		interceptionStream.reset();
	}
	
	/**
	 * Get the current interception buffer as a string.  This will clear it!  
	 * @return the contents of the buffer as a properly encoded string.
	 */
	public String get() {
		String result = interceptionStream.toString();
		interceptionStream.reset();
		return result;
	}

	
	// =================================================================================================
	// == INTERCEPTION CLASSES
	
	/**
	 * Inner-class for an intercepted stream.  This will split all write across two streams, so we can
	 * intercept EVERYTHING.  It will not close any intercept stream.
	 * @author erich
	 */
	public class SplitInterceptedStream extends OutputStream {
		
		// The streams.
		OutputStream  	real;
		OutputStream	intercept;
		
		/**
		 * Constructor
		 * @param outs starting output stream.
		 * @param intercepted true if it is intercepted (don't let closes propigate).
		 */
		public SplitInterceptedStream(OutputStream  realStream, OutputStream interceptStream) {
			super();
			real = realStream;
			intercept = interceptStream;
		}
		
		 public void close() throws IOException {
			 real.close();
		 }
		
         public void flush() throws IOException {
        	 real.flush();	 
        	 intercept.flush();
         }
         
         public void write(byte[] b) throws IOException {
        	 real.write(b);
        	 intercept.write(b);
         }

         public void write(byte[] b, int off, int len) throws IOException {
        	 real.write(b, off, len);	 
        	 intercept.write(b, off, len);	 
         }

         public void write(int b) throws IOException {
        	 real.write(b);
        	 intercept.write(b);
         }     
	}
	
}
