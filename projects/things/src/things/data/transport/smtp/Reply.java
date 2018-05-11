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


/**
 * An SMTP reply.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Adapted from another project - 23 NOV 06
 * </pre> 
 */
public class Reply {
		public int code;  // Default to aborted.
		public String text;
		public boolean broken = false;
		public long size = 0;
		
		public Reply() {
			code = SMTPClient.ACTION_ABORTED;
			text = "Nothing";
		}
		public Reply(int code, String text) {
			this.code = code;
			this.text = text;
		}
}