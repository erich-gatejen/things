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
package test.things.common.impl;

import java.io.File;

import test.things.ThingsTestSuite;
import things.data.ThingsPropertyTree;
import things.data.ThingsPropertyView;
import things.data.impl.FileAccessor;
import things.data.impl.ThingsPropertyTreeBASIC;
import things.data.impl.ThingsPropertyTrunkIO;
import things.testing.unit.Test;

/**
 * TEST a ThingsPropertyTree BASIC implementation.  Tests the following:<br>
 * things.common.impl.ThingsPropertyTreeBASIC<br>
 * things.common.impl.ThingsPropertyTrunkFile<br>
 * things.common.impl.ThingsPropertyViewBASIC<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 29 NOV 04
 * </pre> 
 */
public class TEST_ThingsPropertyTreeBASIC extends Test {
	
	//private NamedTree   theTree;
	public final static String CONSTRUCT_TRUNKFILE = "Construct ThingsPropertyTrunkFile";
	public final static String CONSTRUCT_PROPERTY_TREE = "Construct ThingsPropertyTreeBASIC";
	public final static String CHECK_ROOT_TREE = "Check root branch";	
	public final static String CHECK_BRANCH_TREE = "Check branch tree";	
	public final static String CHECK_COPY_TREE = "Check copy tree";	
	public final static String CHECK_GRAFT = "Check copy and graft";	
	
	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.common.impl.ThingsPropertyTreeBASIC");
	    DECLARE(CONSTRUCT_TRUNKFILE);
	    DECLARE(CONSTRUCT_PROPERTY_TREE);
	    DECLARE(CHECK_ROOT_TREE);
	    DECLARE(CHECK_BRANCH_TREE);
	    DECLARE(CHECK_COPY_TREE);
	    DECLARE(CHECK_GRAFT);
	}

	public void test_execute() throws Throwable {
		
	    String my_prop = new String(properties.getProperty(ThingsTestSuite.REQUIRED_PROP_ROOT_DIR) + "/common/impl/prop_test1.prop");
	    ThingsPropertyTrunkIO  tio = null;
	    ThingsPropertyTree tree = null;
	        
		// Construct ThingsPropertyTrunkFile
		try {
		    tio = new ThingsPropertyTrunkIO();
		    tio.init(my_prop, new FileAccessor(new File(my_prop)));
			PASS(CONSTRUCT_TRUNKFILE,"OK");
		} catch (Throwable e) {
		    ABORT(CONSTRUCT_TRUNKFILE,e.getMessage());
		}

		// Construct ThingsPropertyTreeBASIC
		try {
		    tree = new ThingsPropertyTreeBASIC();
		    tree.init(tio);
		    tree.load();
			PASS(CONSTRUCT_PROPERTY_TREE,"OK");
		} catch (Throwable e) {
		    ABORT(CONSTRUCT_PROPERTY_TREE,e.getMessage());
		}		
		
		// Check root view
		ThingsPropertyView rootView1 = null;
		ThingsPropertyView rootView2 = null;		
		try {
		    rootView1 = tree.getBranch("");
		    rootView2 = tree.getBranch(null);			// this is allowed and should yield the root
		    if (!(rootView1.getProperty("root2").contentEquals(rootView2.getProperty("root2")))) 
		        PUNT("rootView1 does not equal rootView2");
		    if (rootView1.getProperty("root3").indexOf("dsf") < 1) 
		        PUNT("rootView1 did not concatenate slash lines properly");
		    if (!rootView1.getProperty("level1.level2.3").contentEquals("3"))
		        PUNT("rootView1 failed on level1.level2.3");		    
		    if (!rootView1.getProperty("level1.another2.level3").contentEquals("level3"))
		        PUNT("rootView1 failed on level1.another2.level3");   
		    PASS(CHECK_ROOT_TREE,"OK");	
		} catch (Throwable e) {
		    ABORT(CHECK_ROOT_TREE,e.getMessage());
		}		
		
		// Check branch view	
		ThingsPropertyView branchView1 = null;	
		ThingsPropertyView branchView2 = null;	
		try {
		    branchView1 = tree.getBranch("level1");
		    branchView2 = tree.getBranch("level1.another2");
		    if (!branchView1.getProperty("1").contentEquals("1"))
		        PUNT("branchView1 failed on (level1.)1");		    
		    if (!(rootView1.getProperty("level1.2").contentEquals(branchView1.getProperty("2")))) 
		        PUNT("rootView1(level1.2) does not equal branchView1(2)");
		    if (!branchView1.getProperty("another2.level3").contentEquals("level3"))
		        PUNT("branchView1 failed on (level1.)another2.level3");		   		    
		    if (!branchView2.getProperty("level3").contentEquals("level3"))
		        PUNT("branchView1 failed on (level1.another2.)level3");	
		    if (!branchView2.getProperty("level3.1").contentEquals("1"))
		        PUNT("branchView1 failed on (level1.another2.)level3.1");			    
			PASS(CHECK_BRANCH_TREE,"OK");	
		} catch (Throwable e) {
		    ABORT(CHECK_BRANCH_TREE,e.getMessage());
		}				

		// Check copy tree 
		ThingsPropertyTree copyTree1;	
		ThingsPropertyView copyView1;	
		ThingsPropertyView copyView2;
		try {
		    copyTree1 = tree.copyBranch("level1");
		    copyView1 = copyTree1.getRoot();
		    copyView2 = copyTree1.getBranch("level2");
		    if (!copyView1.getProperty("1").contentEquals("1"))
		        PUNT("copyView1 failed on (level1.)1");		    
		    if (!(copyView1.getProperty("level2.2").contentEquals(copyView2.getProperty("2")))) 
		        PUNT("copyView1(level1.2) does not equal copyView2(2)");		    
			PASS(CHECK_COPY_TREE,"OK");	
		} catch (Throwable e) {
		    ABORT(CHECK_COPY_TREE,e.getMessage());
		}		
		
		// Check copy tree 
		try {
			ThingsPropertyView rootView = tree.getRoot();
			 
			//Stock it
			rootView.setProperty("source1.branch1.item1", "item1");
			rootView.setProperty("source1.branch1.item2", "item2");
			rootView.setProperty("source1.branch2.item3", "item3");
			rootView.setProperty("source1.branch2.second1.item4", "item4");		
			rootView.setProperty("source1.branch2.item5", "item5");
			rootView.setProperty("source2.item6", "item6");

			// Grafts
			tree.copyAndGraftBranch("source1", "graft.dest1");
			tree.copyAndGraftBranch("source2", "graft.dest2");
			
			// Check it
		    if (!rootView.getProperty("graft.dest1.branch1.item1").contentEquals("item1"))
		        PUNT("copyAndGraftBranch failed on graft.dest1.branch1.item1");		  
		    if (!rootView.getProperty("graft.dest1.branch1.item2").contentEquals("item2"))
		        PUNT("copyAndGraftBranch failed on graft.dest1.branch1.item2");		
		    if (!rootView.getProperty("graft.dest1.branch2.item3").contentEquals("item3"))
		        PUNT("copyAndGraftBranch failed on graft.dest1.branch2.item3");		
		    if (!rootView.getProperty("graft.dest1.branch2.second1.item4").contentEquals("item4"))
		        PUNT("copyAndGraftBranch failed on graft.dest1.branch2.second1.item4");		
		    if (!rootView.getProperty("graft.dest1.branch2.item5").contentEquals("item5"))
		        PUNT("copyAndGraftBranch failed on graft.dest1.branch2.item5");		
		    if (!rootView.getProperty("graft.dest2.item6").contentEquals("item6"))
		        PUNT("copyAndGraftBranch failed on graft.dest2.item6");			    
		    
			PASS(CHECK_GRAFT,"OK");	
		} catch (Throwable e) {
			if (e instanceof NullPointerException) ABORT(CHECK_GRAFT,"One of the properties is null");
		    ABORT(CHECK_GRAFT,e.getMessage());
		}	
		
	}
}
