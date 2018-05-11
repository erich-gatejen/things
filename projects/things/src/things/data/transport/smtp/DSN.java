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
package things.data.transport.smtp;

import things.common.ThingsCodes;
import things.common.ThingsException;
import things.common.ThingsNamespace;


/**
 * Represents a DSN feature.
 * <p>
 * For textual representation in the format RET`ENVID`ORCPT`bS`bF`bD`bN 
 * <pre>
 * This:
 * MAIL FROM:<erich@things.gatejen.org> RET=HDRS ENVID=QQ123456
 * 250 <erich@things.gatejen.org> Sender ok
 * RCPT TO:<vadrick@things.gatejen.org> NOTIFY=SUCCESS,DELAY ORCPT=rfc822;vadrick@things.gatejen.org
 * 250 <vadrick@things.gatejen.org> Recipient ok
 * 
 * Would become:
 * HDRS`QQ123456`vadrick@things.gatejen.org`TRUE`f`f`f
 * 
 * Fields:
 * RET = Return type.
 * ENVID = Correlation id.
 * ORCPT = Original recipient
 * bS = if 'TRUE,', request SUCCESS notification.  Any other value is ignored.
 * bF = if 'TRUE,', request FAILURE notification.  Any other value is ignored.
 * bD = if 'TRUE,', request DELAY notification.  Any other value is ignored.
 * bN = if 'TRUE,', request that you NEVER get notification.  Any other value is ignored.
 * </pre>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Added by request - 1 JUN 09
 * </pre> 
 */
public class DSN {
	
		public String RET;
		public String ENVID;
		public String ORCPT;
		public boolean notifySuccess;		// SUCCESS
		public boolean notifyFailure;		// FAILURE
		public boolean notifyDelay;			// DELAY
		public boolean notifyNever; 		// NEVER
		
		/**
		 * DSN as a comma separated field: RET`ENVID`ORCPT`bS`bF`bD`bN 
		 * @param dsn the textual DSN.
		 * @return the parsed DSN or null if the DSN string is null or empty.
		 * @throws Throwable for any format problems.
		 */
		public static DSN parse(String dsn) throws Throwable {
			if ((dsn==null)||(dsn.trim().length()<1)) return null;
			DSN result = new DSN();
			try {
				String[] fields = dsn.split("`");
				if (fields.length < 7) throw new Exception("Not enough fields");
				if (fields[0].trim().length()>0) result.RET=fields[0].trim();
				if (fields[1].trim().length()>0) result.ENVID=fields[1].trim();
				if (fields[2].trim().length()>0) result.ORCPT=fields[2].trim();
				if (fields[3].trim().equalsIgnoreCase("TRUE")) result.notifySuccess = true;
				if (fields[4].trim().equalsIgnoreCase("TRUE")) result.notifyFailure = true;
				if (fields[5].trim().equalsIgnoreCase("TRUE")) result.notifyDelay = true;
				if (fields[6].trim().equalsIgnoreCase("TRUE")) result.notifyNever = true;
				
			} catch (Throwable t) {
				throw new ThingsException("Bad DSN", ThingsCodes.BAD_DSN, t, ThingsNamespace.ATTR_DATA_ARGUMENT, dsn);
			}
			
			return result;
		}
		
		/**
		 * Render the DSN command.  Brute force, please.
		 * @return the DSN command as a string.
		 */
		public String renderFrom() {
			StringBuffer result = new StringBuffer();
			int first = 0;
			
			if (RET!=null) {
				result.append("RET=" + RET);
				first=1;
			}
			if (ENVID!=null) { 
				if (first==1) result.append(" ");
				result.append("ENVID=" + ENVID);
			}
			return result.toString();
		}
		
		/**
		 * Render the DSN command.  Brute force, please.
		 * @return the recipient as a string.
		 */
		public String renderRecipient() {
			StringBuffer result = new StringBuffer();
			int first = 0;
			
			if (notifySuccess) {
				result.append("NOTIFY=SUCCESS");
				first=1;
			}
			if (notifyFailure) {
				if (first<1) {
					result.append("NOTIFY=FAILURE");
					first=1;
				} else {
					result.append(",FAILURE");
				}
			}
			if (notifyDelay) {
				if (first<1) {
					result.append("NOTIFY=DELAY");
					first=1;
				} else {
					result.append(",DELAY");
				}
			}
			if (notifyNever) {
				if (first<1) {
					result.append("NOTIFY=NEVER");
					first=1;
				} else {
					result.append(",NEVER");
				}
			}
			if (ORCPT!=null) {
				if (first>0) result.append(" ");
				result.append("ORCPT=rfc822;" + ORCPT);
			}
			
			return result.toString();
		}
		
}