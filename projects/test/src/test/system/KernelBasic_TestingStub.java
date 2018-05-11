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
package test.system;

import things.common.StringPoster;
import things.common.ThingsException;
import things.data.ThingsPropertyTree;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.thinger.kernel.basic.KernelBasic;

/**
 * A startup for a KernelBasic that is meant for testing.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 11 NOV 06
 * </pre> 
 */
public class KernelBasic_TestingStub extends Thread {

	// =====================================================================================================================
	// =====================================================================================================================
	// PUBLIC DATA
	public KernelBasic kernel;
	public ThingsException outputException;
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PRIVATE DATA
	private StringPoster logger;
	private ThingsPropertyTree properties;
	
	// STATIC FIELDS
	// There will only be one stub -- ever.
	private static KernelBasic_TestingStub kernelStub = null;
	
	// =====================================================================================================================
	// =====================================================================================================================
	// BUILD IT

	/**
	 * Build the stub.
	 * @param propertyFilePath properties for the server.
	 */
	public void init(String propertyFilePath, StringPoster bootstrapLogger) throws ThingsException {

		// Make sure we haven;t done this.
		if (kernelStub != null) ThingsException.softwareProblem("KernelBasic_TestingStub already init()'d.");
		
		// handle arguments
		if (propertyFilePath == null) {
			ThingsException.softwareProblem("propertyFilePath is null.");
		}
		if (bootstrapLogger == null) {
			ThingsException.softwareProblem("bootstrapLogger is null.");
		}
		
		try {
			
			// Configuration
			properties = ThingsPropertyTreeBASIC.getExpedientFromFile(propertyFilePath);
			
			// Logger
			logger = bootstrapLogger;
			
			// Run the bootstrap
			kernel = new KernelBasic();
			kernelStub = this;
			
		} catch (ThingsException te) {
			throw te;
		} catch (Throwable e) {
			throw new ThingsException("KernelBasic_TestingStub failed to an exception", e);
		}
	}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// RUN
	public void run() {

		try {			
			kernel.bootstrap(true, logger, properties);
			
		} catch (ThingsException te) {
			outputException = te;
		} catch (Throwable e) {
			outputException = new ThingsException("KernelBasic_TestingStub failed to an exception", e);
		}
	}		
	
	// =====================================================================================================================
	// =====================================================================================================================
	// METHODS
	
	/**
	 * Get the stub.
	 * @return the stub or null if it hasn't been initialized.
	 */
	public static KernelBasic_TestingStub getStub() {
		return kernelStub;
	}
	
	/**
	 * Get the SSI from the stub.
	 * @return the SSI or null if it hasn't been initialized.
	 */
	//public static SuperSystemInterface getSSI() {
	//	return kernelStub.kernel.;
	//}
	
	// =====================================================================================================================
	// =====================================================================================================================
	// PRIVATE METHODS
	


}
