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
package things.thinger.service.actor;

import things.common.PuntException;

/**
 * The actor disposition.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 12 MAY 07
 * </pre> 
 */
public class ActorDisposition extends ActorMessage {

	/**
	 * Serialization version number.
	 */
	static final long serialVersionUID=1;
	
	// ================================================================================================
	// = FIELDS
	
	/**
	 * Dispositions.
	 */
	public final static int DISPOSITION_NONE = 0;
	public final static int DISPOSITION_QUIT = 1;
	public final static int DISPOSITION_ACK = 2;
	public final static int DISPOSITION_NACK = 3;
	
	/**
	 * The parameter name.
	 */
	public  int	myDisposition;
	
	// ================================================================================================
	// = DATA
	private int sourceSequence;
	
	// ================================================================================================
	// = METHODS
	
	/**
	 * Constructor.
	 * @param disposition
	 * @param seq
	 */
	public ActorDisposition(int		disposition, int	seq) {
		super(0);
		myDisposition = disposition;
		sourceSequence = seq;
	}
	
	/**
	 * Fix any disposition work.
	 * @throws Throwable
	 */
	public void fixType() throws Throwable {
		
		switch (myDisposition) {
		case DISPOSITION_NONE:
			this.setType(ActorConstants.MESSAGE_TYPE_NONE);
			break;
			
		case DISPOSITION_QUIT:
			this.setType(ActorConstants.MESSAGE_TYPE_QUIT);
			break;
			
		case DISPOSITION_ACK:
			this.setType(ActorConstants.MESSAGE_TYPE_ACK);
			this.addParamter(ActorConstants.PARAMETER_ACK_SEQUENCE,Integer.toString(sourceSequence));
			break;
			
		case DISPOSITION_NACK:
			this.setType(ActorConstants.MESSAGE_TYPE_NACK);
			this.addParamter(ActorConstants.PARAMETER_ACK_SEQUENCE,Integer.toString(sourceSequence));			
			break;
			
		default:
			throw new Exception("Software bug.  Unknown disposition type.  disposition=" + myDisposition);
		}
	}
	
	/**
	 * This will set the disposition to a NAK and throw a PuntException.  It will set PARAMETER_RESPONSE as the message.
	 * @param message The message.
	 * @throws Throwable, but it will almost always be a PuntException
	 */
	public void fail(String message) throws Throwable {
		this.failSoft(message);
		throw new PuntException("Punt for " + message);
	}
		
	/**
	 * This will set the disposition to a NAK but will not punt.  It will set PARAMETER_RESPONSE as the message.
	 * @param message The message.
	 */
	public void failSoft(String message) throws Throwable {
		this.myDisposition = DISPOSITION_NACK;
		this.addParamter(ActorConstants.PARAMETER_RESPONSE, message);
	}
	
	/**
	 * It's ok.
	 */
	public void ok() throws Exception {
		this.myDisposition = DISPOSITION_ACK;
	}
	
	/**
	 * It's ok.  It will set PARAMETER_RESPONSE as the message.
	 * @param message The message.
	 */
	public void ok(String message) throws Throwable {
		this.ok();
		this.addParamter(ActorConstants.PARAMETER_RESPONSE, message);
	}
	
	
}