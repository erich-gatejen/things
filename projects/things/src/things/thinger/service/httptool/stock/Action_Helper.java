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
package things.thinger.service.httptool.stock;

import java.util.HashMap;
import java.util.List;

import things.common.ThingsException;
import things.common.help.HelpItem;
import things.common.help.HelpProperty;
import things.common.help.HelpUser;
import things.common.help.Helper;
import things.data.NVImmutable;
import things.thinger.service.httptool.Action;
import things.thinger.service.httptool.ActionResult;
import things.thinger.service.httptool.CommonTagsParams;

/**
 * A simple help page displayer.
 * <p>
 * PARAM_HELPER__FOR = Gives a name for what is using the Helper.  This is for visual naming purposes only.
 * PARAM_HELPER__NAME = Loadable class for the Helper.
 * PARAM_HELPER__PAGE = OPTIONAL.  If specified it will merge with this page instead of the registered default.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 13 Sep 08
 * </pre> 
 */
public class Action_Helper extends Action {

	// =================================================================================================
	// == DATA
	
	// -- Global style choices.  I know I'm being naughty by leaving these open like this.  These will me used as class=
	public static String helper__Table = "he_table";
	public static String helper__Banner = "he_banner";
	public static String helper__BannerReference = "he_bannerref";
	public static String helper__Info = "he_info";
	public static String helper__Column1 = "he_tdc1";
	public static String helper__Column2 = "he_tdc2";
	public static String helper__Column3 = "he_tdc3";
	public static String helper__Column2_value = "he_tdv2";
	public static String helper__Column3_value = "he_tdv3";
	public static String helper__Column1_header = "he_tdc1h";
	public static String helper__Column2_header = "he_tdc2h";
	public static String helper__Column3_header = "he_tdc3h";
	
	// =================================================================================================
	// == ABSTRACT METHODS
	
	/**
	 * The implementation.  Assume the DATA is available.
	 */
	protected ActionResult process() throws Throwable {
		
		// Try to give them whatever page they want.
		ActionResult result = new ActionResult(ActionResult.Type.PAGE);
		
		String page = parameters.getProperty(CommonTagsParams.PARAM_HELPER__PAGE);
		String forText = parameters.getProperty(CommonTagsParams.PARAM_HELPER__FOR);
		String className = parameters.getProperty(CommonTagsParams.PARAM_HELPER__NAME);	
		HelpActionItem item = get(forText, className);
		
		if (item==null) {
			// No help.
			result.setPageResult(StockConstants.PAGE_ERROR);
			tags.setProperty(CommonTagsParams.TAG_ERROR_DESCRIPTION, "Help does not exist");
			tags.setProperty(CommonTagsParams.TAG_ERROR_MESSAGE, "There is no help defined for " + className + "\r\n.  This may be a bug with the class.  If it implements Helpful but does not have a default constructor, it must have a public static public static instance of Helper named 'helper', like such:\r\n" + " public static Helper helper;");
			
		} else {
			if ((page==null)||(page.trim().length()<1)) {
				result.setPageResult(StockConstants.ACTION_HELPER__PAGE);	
			} else {
				result.setPageResult(page.trim());
			}
			tags.setProperty(CommonTagsParams.TAG_HELPER__FOR, item.forText);
			tags.setProperty(CommonTagsParams.TAG_HELPER__NAME, item.name);
			tags.setProperty(CommonTagsParams.TAG_HELPER__HELP, item.help);
		}
		
		return result;
	}
	
	// =================================================================================================
	// == METHODS
	
	/**
	 * Cached help items, so we don't have to constantly render them.  I don't think this will be a memory problem, but if
	 * it is then all you need is a simple eviction.
	 */
	private static HashMap<String, HelpActionItem> items = new  HashMap<String,HelpActionItem>();
	
	
	/**
	 * Get help.  It'll get it from the cache if available, otherwise it will build it from the class.
	 * @param forText who is this for?  This is used only for rendering.
	 * @param helperClassName what class is providing the help through a Helper?
	 * @return the help.
	 * @throws Throwable for any problem.
	 */
	private static synchronized HelpActionItem get(String forText, String helperClassName) throws Throwable {
		
		// Cached?
		String id = forText + helperClassName;
		HelpActionItem result = items.get(id);
		if (result==null) {
			
			// No.  Build it.
			Helper helper = Helper.getHelper(helperClassName);
			if (helper==null) {
				
				// Help does not exist.
				result = null;
				
			} else {
				
				result = new HelpActionItem();
				result.forText = forText;
				result.name = helper.getUser();
				
				StringBuffer bs = new StringBuffer();
				express(helper, bs, 0);

				result.help = bs.toString();
			}
	
			// Cache it
			items.put(id, result);
		}
		
		return result;	
	}
	
	/**
	 * Express the help in HTML into a string buffer.
	 * @param helper the helper to express.
	 * @param sb the string buffer in which to express.
	 * @param level the level of processing.  Call with with 0.  It is used with recursion when help refers to other help.
	 * @throws Throwable
	 */
	private static void express(Helper helper, StringBuffer sb, int level) throws Throwable {	
		String title;
		String info;
		
		// Table open and set spacing
		sb.append("<table class=\""+ helper__Table + "\" border=\"0\" cellspacing=\"0\" cellpadding=\"3\">\r\n");	
		sb.append("<tr><td width=\"17%\"></td><td width=\"8%\"></td><td width=\"75%\"></td></tr>\r\n");
		
		// See if we have a User
		List<HelpItem> helps = helper.getHelp();
		HelpItem item = null;
		if (helps.size()>0) item = helps.remove(0);
		if ((item!=null)&&(item instanceof HelpUser)) {
			
			// Extract the title and info.
			title = ((HelpUser)item).title;
			info = ((HelpUser)item).help;
			
			// Ditch the item
			if (helps.size()>0) item = helps.remove(0);
			else item = null;
			
		} else {
			
			// Otherwise get it from the overall helper.
			title = helper.getUser();
			info = helper.information();
		}
		
		// Banner and info
		sb.append("<tr><td colspan=\"3\" class=\"");
		if (level>0) {
			sb.append(helper__Banner);
		} else {
			sb.append(helper__BannerReference);
		}
		sb.append("\"><div align=\"center\">");
		sb.append(title);
		sb.append("</div></td></tr>\r\n");
		sb.append("<tr><td colspan=\"3\" class=\"" + helper__Info + "\">");
		if (info!=null) sb.append(info);
		else sb.append("&nbsp;");
		sb.append("</td></tr>");
		
		// Header
		 sb.append("<tr><td class=\"" + helper__Column1_header + "\">name<td class=\"" + helper__Column2_header + "\">&nbsp;<td class=\"" + helper__Column3_header + "\">information or value</td></tr>\r\n");

		// Help items, indigenous and inherited
		while (item != null) {
		
			 if (item instanceof HelpUser) {
				 // NOOP.  Disregard.
				 
			 } else if (item instanceof HelpProperty) { 
				 // express with required or optional
				 sb.append("<tr><td class=\"" + helper__Column1 + "\">");
				 sb.append(item.name);
				 sb.append("</td>");
				 sb.append("<td class=\"" + helper__Column2 + "\">");
				 if (((HelpProperty)item).required) sb.append("REQUIRED");
				 else sb.append("optional");
				 sb.append("</td><td class=\"" + helper__Column3 + "\">");
				 sb.append(item.help);			 
				 sb.append("</td></tr>\r\n");				 
				 
				 // express options, if any
				 if ( ((HelpProperty)item).values != null ) {
					 for ( NVImmutable nv : ((HelpProperty)item).values) { 
						 sb.append("<tr><td>&nbsp;</td><td class=\"" + helper__Column2_value  + "\">value=</td><td class=\"" + helper__Column3_value + "\">");
						 sb.append(nv.getName());
						 sb.append(" : ");
						 sb.append(nv.getValue());					 
						 sb.append("</td></tr>\r\n");					 
					 }
				 }
				 
			 } else if (item instanceof HelpItem) { 
				 // Simple expression of the base class
				 sb.append("<tr><td class=\"" + helper__Column1 + "\">");
				 sb.append(item.name);
				 sb.append("</td>");
				 sb.append("<td class=\"" + helper__Column2 + "\"></td><td class=\"" + helper__Column3 + "\">");
				 sb.append(item.help);			 
				 sb.append("</td></tr>\r\n");
				 	 
			 } else {
				 ThingsException.softwareProblem("Unimplemented HelpItem class.  " + item.getClass().getCanonicalName());
			 }
			 
			 // Iterate.
			 if (helps.size()>0) item = helps.remove(0); 
			 else item = null;
		}
		 
		// Table close
		sb.append("</table>\r\n");
		 
		// Recurse for references.  Each will get their own table.
		for (Helper references : helper.getReferences()) {
			express(references, sb, level+1);
		}	 
	}
	
}
