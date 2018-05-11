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
package things.thinger.service.httptool;

/**
 * The manage the head fields:<br>
 * Content-Length<br>
 * Content-MD5<br>
 * ETag<br>
 * Last-Modified<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 16 NOV 07
 * </pre> 
 */
public class Head  {

	// =================================================================================================
	// == DATA
	private String 	contentLength;
	private String 	md5;
	private String 	etag;
	private String  lastModified;
	
	// =================================================================================================
	// == METHODS

	/**
	 * Set the content length.  If unset or set as null, it will not be rendered.
	 * @param contentLength the value of the Content-Length field.
	 */
	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}
	
	/**
	 * Set the MD3.  If unset or set as null, it will not be rendered.
	 * @param md5 the value of the Content-MD5  field.
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	/**
	 * Set the ETag.  If unset or set as null, it will not be rendered.
	 * @param etag the value of the ETag field.
	 */
	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	/**
	 * Set the Last-Modified.  If unset or set as null, it will not be rendered.
	 * @param lastModified the value of the Last-Modified field.
	 */
	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}
	
	/**
	 * Add whichever headers are set to the result.
	 * @param result the result.
	 */
	public void addHeadersToResult(ActionResult result) {
		if (contentLength!=null) {
			result.addHeader("Content-Length", contentLength);
		} 
		if (md5!=null) {
			result.addHeader("Content-MD5", md5);
		}
		if (etag!=null) {
			result.addHeader("ETag", etag);
		}
		if (lastModified!=null) {
			result.addHeader("Last-Modified", lastModified);
		}
	}
	
	/**
	 * Render the fields as HTTP headers.  Content-Length will always be rendered, even if just 0.
	 * @return the headers.
	 */
	public String renderFields() {
		StringBuffer result = new StringBuffer();
		if (contentLength!=null) {
			result.append("Content-Length: ");
			result.append(contentLength);
			result.append("\r\n");
		} else {
			result.append("Content-Length: 0\r\n");
		}
		if (md5!=null) {
			result.append("Content-MD5: ");
			result.append(md5);
			result.append("\r\n");
		}
		if (etag!=null) {
			result.append("ETag: ");
			result.append(etag);
			result.append("\r\n");
		}
		if (lastModified!=null) {
			result.append("Last-Modified: ");
			result.append(lastModified);
			result.append("\r\n");
		}
		return result.toString();
	}
	
}
