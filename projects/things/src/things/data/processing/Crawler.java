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

package things.data.processing;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

import things.common.PuntException;
import things.common.ThingsException;
import things.common.ThingsNamespace;
import things.thinger.io.Logger;

/**
 * Universal crawler.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 21 OCT 06
 * </pre> 
 */
public class Crawler {

	// ========================================================================================
	// FIELDS
	private Stack<CrawlNode> crawlStack;
	private CrawlNode current;
	private Logger logger;
	private String pathroot;
	private boolean loop;
	
	private Pattern includePattern = null;
	private ArrayList<String> includeParts = null;
	private Pattern excludePattern = null;
	private ArrayList<String> excludeParts = null;
	
	private String suffix;

	// ========================================================================================
	// METHODS
	
	/**
	 * Start a crawl on the filesystem.  Log progress.  You can restart the crawl with a call to this at any time.  It will reset any matching configuration.
	 * @param pathroot the path from where to start.
	 * @param logger the logger to use.
	 * @param loop if true, just go back to the loop once we deplete.
	 * @throws Throwable for any problem.
	 */
	public void crawl(String pathroot, Logger logger, boolean loop) throws Throwable {
		if (logger==null) throw new ThingsException("Crawler failed to start due to EXCEPTION.  Null logger.", ThingsException.CRAWLER_ERROR_DURING_SETUP);
		if (pathroot==null) throw new ThingsException("Crawler failed to start due to EXCEPTION.  Null pathroot.", ThingsException.CRAWLER_ERROR_DURING_SETUP);
	
		this.logger = logger;
		this.pathroot = pathroot;
		this.loop = loop;
		
		try {
			
			// REFRESH matching
			includePattern = null;
			includeParts = null;
			excludePattern = null;
			excludeParts = null;
			
			// SETUP
			start();
			if (!current.hasNext()) throw new ThingsException("Crawl is completely empty.", ThingsException.CRAWLER_ERROR);
			logger.info("CRAWL STARTING.");

		} catch (Throwable t) {
			throw new ThingsException("Crawler failed to start due to EXCEPTION.", ThingsException.CRAWLER_ERROR_DURING_SETUP, t, ThingsNamespace.ATTR_DATA_ARGUMENT, pathroot);
		}
	}

	/**
	 * Start from the root.  This is the same as a reset.
	 * @throws Throwable
	 */
	public void start() throws Throwable {
		if (logger==null) throw new ThingsException("Crawler failed to start due to EXCEPTION.  Null logger.  You must 'crawl' before you can start/re-start.", ThingsException.CRAWLER_ERROR_DURING_SETUP);
		File root = new File(pathroot);
		if (!root.isDirectory()) {
			throw new PuntException("Crawler given a bad directory as root.");
		}
		crawlStack = new Stack<CrawlNode>();
		current = new CrawlNode(root.listFiles());
	}
	
	/**
	 * It will require every filename match the regex passed.  You may call this more than once and it will attempt to stitch them together, but I can't guarantee it.
	 * You must have called crawl at least once before setting this.
	 * @param text the regex.   It cannot be null or empty.
	 * @throws Throwable
	 */
	public void match(String text) throws Throwable {
		if (logger==null) ThingsException.softwareProblem("You must call Crawler.crawl before calling Crawler.match.");
		if ((text==null)||(text.length()<1)) ThingsException.softwareProblem("Crawler.match cannot be called with an empty text.");
		
		synchronized(logger) {		
			// Piece the include parts together.
			StringBuffer temp = new StringBuffer();
			if (includeParts==null) includeParts = new ArrayList<String>();
			includeParts.add(text);
			temp.append(text);
			if (includeParts.size() > 1) {
				for (int index = 1; index < includeParts.size() ; index++) {
					temp.append('|');  // OR
					temp.append(includeParts.get(index));
				}
			}
			includePattern = Pattern.compile(temp.toString());	
		}	
	}
	
	/**
	 * It will require every filename does NOT match the regex passed.  You may call this more than once and it will attempt to stitch them together, but I can't guarantee it.
	 * You must have called crawl at least once before setting this.
	 * @param text the regex.   It cannot be null or empty.
	 * @throws Throwable
	 */
	public void dontMatch(String text) throws Throwable {
		if (logger==null) ThingsException.softwareProblem("You must call Crawler.crawl before calling Crawler.dontMatch.");
		if ((text==null)||(text.length()<1)) ThingsException.softwareProblem("Crawler.dontMatch cannot be called with an empty text.");
		
		synchronized(logger) {		
			// Piece the include parts together.
			StringBuffer temp = new StringBuffer();
			if (excludeParts==null) excludeParts = new ArrayList<String>();
			excludeParts.add(text);
			temp.append(text);
			if (excludeParts.size() > 1) {
				for (int index = 1; index < excludeParts.size() ; index++) {
					temp.append('|');  // OR
					temp.append(excludeParts.get(index));
				}
			}
			excludePattern = Pattern.compile(temp.toString());	
		}	
	}
	
	/**
	 * Ignore any path that has this as a suffix.  You can only set one at a time.
	 * @param suffix The suffix.  Null will turn it off.  It is not case sensitive.
	 * @throws Throwable
	 */
	public void dropSuffix(String suffix) throws Throwable {
		if ((suffix!=null)&&(suffix.length()<1)) ThingsException.softwareProblem("Crawler.dropSuffix cannot be called with an empty text.");
		this.suffix = suffix.toLowerCase();
	}
	
	/**
	 * Get the next time in the crawl.
	 * @return the File node or null if all done.
	 * @throws Throwable if there was a serious problem.
	 */
	public File next() throws Throwable {
		File result = null;
		File candidate = current.getNext();

		try {
			while (result == null) {
				if (candidate == null) {
					if (crawlStack.empty()) {
						// Depleted						
						if (loop) {
							// Start or restart.
							start();
							candidate = current.getNext();
							if (candidate==null) ThingsException.softwareProblem("Crawl failed because a loop re-start() yielded nothing.");
							logger.info("CRAWL RESTARTING.");
							continue;
						} else {
							logger.info("CRAWL DONE.  Depleted current and depleted stack.");
							return null;
						}

					} else {
						current = (CrawlNode) crawlStack.pop();
						logger.debug("CRAWL Pop.");
					}
					candidate = current.getNext();

				} else if (candidate.isDirectory()) {
					crawlStack.push(current);
					logger.debug("CRAWL Push.");
					current = new CrawlNode(candidate.listFiles());
					candidate = current.getNext();

				} else if (candidate.isFile()) {
					
					boolean go = true;
					
					if (includePattern!=null) {
						go = go & includePattern.matcher(candidate.getName()).matches();
					}
					if (excludePattern!=null) {
						go = go & !(excludePattern.matcher(candidate.getName()).matches());
					}		
					if (suffix!=null) {
						if (candidate.getName().toLowerCase().endsWith(suffix)) go = false;
					}
					
					if (go)	{
						logger.info("CRAWL accepted for next File " + candidate.getAbsolutePath());
						result = candidate;
					} else {
						logger.info("CRAWL skipped for next File " + candidate.getAbsolutePath());
						candidate = current.getNext();
					}
					
				} else {
					// SHOULD NEVER HAPPEN
					candidate = current.getNext();
				}

			} // while no result

		} catch (Throwable t) {
			throw new ThingsException("Crawler failed due to EXCEPTION.", ThingsException.CRAWLER_ERROR, t);

		}
		return result;
	}

	// ========================================================================================
	// INTERNAL
	
	/**
	 * Internal crawlnode.
	 */
	class CrawlNode {
		protected int index = 0;
		protected File[] list;
		private int lastindex = 0;

		public CrawlNode(File[] il) {
			list = il;
			lastindex = list.length;
		}

		protected File getNext() {
			File result = null;
			if (index < lastindex) {
				result = list[index];
				index++;
			}
			return result;
		}
		
		protected boolean hasNext() {
			boolean result = false;
			if (index < lastindex) {
				result = true;
			}
			return result;
		}
	}

}
