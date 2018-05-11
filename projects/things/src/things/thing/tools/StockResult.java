/**
 * THINGS/THINGER 2004, 2005, 2006, 2007
 * Copyright Erich P Gatejen (c) 2004, 2005, 2006, 2007 ALL RIGHTS RESERVED
 * This is not to be distributed without written permission. 
 *
 * @author Erich P Gatejen
 */
package things.thing.tools;

import things.data.Data;
import things.thing.RESULT;

/**
 * Static, stock results.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 7 MAY 07
 * </pre> 
 */
public class StockResult {

	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == STATIC DATA
	
	// Cooked results
	private static RESULT waitingResult;
	private static RESULT passResult;
	private static RESULT failResult;
	private static RESULT exceptionResult;
	private static RESULT inconclusiveResult;
	private static RESULT abortResult;
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == METHODS
	
	/**
	 * Get a stock waiting.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getWaiting() {
		if (waitingResult==null) {
			try {
				waitingResult = new RESULT(Data.Type.WAITING);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getWaiting() is totally screwed.  WAITING.  Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return waitingResult;
	}
	
	/**
	 * Get a stock pass.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getPass() {
		if (passResult==null) {
			try {
				passResult = new RESULT(Data.Type.PASS);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getResult() is totally screwed.  PASS.  Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return passResult;
	}
	
	/**
	 * Get a stock fail.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getFail() {
		if (failResult==null) {
			try {
				failResult = new RESULT(Data.Type.FAIL);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getResult() is totally screwed. FAIL.   Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return failResult;
	}
	
	/**
	 * Get a stock exception.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getException() {
		if (exceptionResult==null) {
			try {
				exceptionResult = new RESULT(Data.Type.FAIL);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getResult() is totally screwed.  EXCEPTION.  Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return exceptionResult;
	}
	
	/**
	 * Get a stock inconclusive.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getInconclusive() {
		if (inconclusiveResult==null) {
			try {
				inconclusiveResult = new RESULT(Data.Type.INCONCLUSIVE);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getResult() is totally screwed.  INCONCLUSIVE.  Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return inconclusiveResult;
	}
	
	/**
	 * Get a stock abort.
	 * @return the RESULT
	 * @see things.thing.RESULT
	 * @see things.data.Data
	 */
	public static RESULT getAbort() {
		if (abortResult==null) {
			try {
				abortResult = new RESULT(Data.Type.FAIL);
			} catch (Throwable t) {
				// IF this happens, we are totally screwed.
				System.out.println("SOFTWARE DETECTED FAULT: StockResult.getResult() is totally screwed.  ABORT.  Tell Erich.  message=" + t.getMessage());
				System.exit(1);
			}
		}
		return abortResult;
	}
	
	// ====================================================================================================================================
	// ====================================================================================================================================
	// == HELPERS
	

}
