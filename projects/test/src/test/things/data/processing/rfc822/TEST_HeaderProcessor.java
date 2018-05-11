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
package test.things.data.processing.rfc822;

import java.io.ByteArrayInputStream;

import things.common.ThingsConstants;
import things.common.tools.BitBucketOutputStream;
import things.testing.unit.Test;

/**
 * TEST the HeaderProcessor tool.<br>
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <p>
 * <i>Version History</i>
 * <pre>EPG - Initial - 28 JAN 07
 * </pre> 
 */
public class TEST_HeaderProcessor extends Test {
	
	// TESTS
	public final static String SETUP = "Setup";
	public final static String BASIC_STRING = "Basic from String";
	public final static String BASIC_SEPARATION = "Header separation";	
	public final static String BASIC_FOLD = "Basic Folding";	
	public final static String BASIC_MESSAGE = "Basic from a Message";
		
	// Configuration
	
	// Test values
	public final static String HEADER_BASIC = 
		"Da: Bo" + ThingsConstants.CR + ThingsConstants.LF +
		"Fa: Qo" + ThingsConstants.CR + ThingsConstants.LF +
		ThingsConstants.CR + ThingsConstants.LF;
	
	// Test values
	public final static String HEADER_SEPARATION = 
		"Da: Bo" + ThingsConstants.CR + ThingsConstants.LF + ThingsConstants.CR + ThingsConstants.LF +
		"Fa: Qo" + ThingsConstants.CR + ThingsConstants.LF;
	
	public final static String HEADER_FOLD = 
		"Da: Bo" + ThingsConstants.CR + ThingsConstants.LF +
		" Mo" + ThingsConstants.CR + ThingsConstants.LF +
		ThingsConstants.CR + ThingsConstants.LF;
	
	public final static String MESSAGE_BASIC = 
		"Da: Bo Do Bo" + ThingsConstants.CR + ThingsConstants.LF +
		"Fa: Qo " + ThingsConstants.CR + ThingsConstants.LF +
		" Mo"  + ThingsConstants.CR + ThingsConstants.LF +
		"Suubjeect: asdf asdf asdf" + ThingsConstants.CR + ThingsConstants.LF + 
		ThingsConstants.CR + ThingsConstants.LF +
		"Subject: This is really body and won't match." + ThingsConstants.CR + ThingsConstants.LF +
		" Bork Bork Bork"  + ThingsConstants.CR + ThingsConstants.LF;

	public void test_prepare() throws Throwable {
		SET_LONG_NAME("things.data.processing.rfc822.TEST_HeaderProcessor");
		DECLARE(SETUP);
		DECLARE(BASIC_STRING);		
		DECLARE(BASIC_FOLD);	
		DECLARE(BASIC_SEPARATION);				
		DECLARE(BASIC_MESSAGE);
	}

	public void test_execute() throws Throwable {     
	    
		TestHeaderProcessor processor = null;
		
		// == SETUP =========================================================================================================================
		try {
			
			processor = new TestHeaderProcessor();
					
			PASS(SETUP);
		} catch (Throwable e) {
		    ABORT(SETUP,e.getMessage());
		}
		
		// == BASIC STRING =========================================================================================================================
		try {
			
			// process
			processor.process("noid", new ByteArrayInputStream(HEADER_BASIC.getBytes()), new BitBucketOutputStream());
			
			// check
			String stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_DA_STRING);
			if (stuff==null) PUNT("DA not matched.");
			if ( !stuff.equals(" Bo\r\n") )  PUNT("DA is not ' Bo<cr><lf>'.  Instead, it is '" + stuff + "'");
			
			stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_FA_STRING);
			if (stuff==null) PUNT("FA not matched.");
			if ( !stuff.equals(" Qo\r\n\r\n") )  PUNT("FA is not ' Qo<cr><lf><cr><lf>'.  Instead, it is '" + stuff + "'");
			
			stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_SUBJECT_STRING);
			if (stuff!=null) PUNT("SUBJECT should not be matched, because it isn't in the header.");	
			stuff = processor.unmatchedValues.getProperty(TestHeaderProcessor.HEADER_SUBJECT_STRING);
			if (stuff!=null) PUNT("SUBJECT should not be unmatched, because it isn't in the header.");	
			
			PASS(BASIC_STRING);
//		} catch (TestLocalException t) {
//		    
		} catch (Throwable e) {
		    FAIL(BASIC_STRING,e.getMessage());
		}
		
		// == BASIC FOLD =========================================================================================================================
		try {
			
			// process
			processor.process("noid", new ByteArrayInputStream(HEADER_FOLD.getBytes()), new BitBucketOutputStream());
			
			// check
			String stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_DA_STRING);
			if (stuff==null) PUNT("DA not matched.");
			if ( stuff.indexOf("Mo") < 0 )  PUNT("DA does not contain 'Mo'");
					
			PASS(BASIC_FOLD);
//		} catch (TestLocalException t) {
//		    
		} catch (Throwable e) {
		    ABORT(BASIC_FOLD,e.getMessage());  // The rest will fail because of folding problems.
		}
		
		// == HEADER SEPARATION =========================================================================================================================
		try {
			
			// process
			processor.process("noid", new ByteArrayInputStream(HEADER_SEPARATION.getBytes()), new BitBucketOutputStream());
			
			// check
			String stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_DA_STRING);
			if (stuff==null) PUNT("DA not matched.");
			if ( stuff.indexOf("Bo") < 0 )  PUNT("DA does not contain 'Bo'");
			
			stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_FA_STRING);			
			if (stuff!=null) PUNT("FA matched when it should be body.");			
					
			PASS(BASIC_SEPARATION);
//		} catch (TestLocalException t) {
//		    
		} catch (Throwable e) {
		    ABORT(BASIC_SEPARATION,e.getMessage());  // The rest will fail because of folding problems.
		}
		
		// == BASIC MESSAGE =========================================================================================================================
		try {
			
			// process
			processor.process("noid", new ByteArrayInputStream(MESSAGE_BASIC.getBytes()), new BitBucketOutputStream());
			
			// check
			String stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_DA_STRING);
			if (stuff==null) PUNT("DA not matched.");
			if ( !stuff.equals(" Bo Do Bo\r\n") )  PUNT("DA is not ' Bo Do Bo<cr><lf>'.  Instead, it is '" + stuff + "'");

			stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_FA_STRING);
			if (stuff==null) PUNT("FA not matched.");
			if ( stuff.indexOf("Mo") < 0 )  PUNT("FA does not contain 'Mo'");
			
			stuff = processor.unmatchedValues.getProperty("Suubjeect:");
			if (stuff==null) PUNT("'Suubjeect:' unmatched not found.");	
			
			stuff = processor.matchedValues.getProperty(TestHeaderProcessor.HEADER_SUBJECT_STRING);
			if (stuff!=null) PUNT("SUBJECT should not be matched, because it is body.");	
			stuff = processor.unmatchedValues.getProperty(TestHeaderProcessor.HEADER_SUBJECT_STRING);
			if (stuff!=null) PUNT("SUBJECT should not be unmatched, because it is body.");	
					
			PASS(BASIC_MESSAGE);
//		} catch (TestLocalException t) {
//		    
		} catch (Throwable e) {
		    FAIL(BASIC_MESSAGE,e.getMessage());
		}
		
	}  // end test_execute()

}
