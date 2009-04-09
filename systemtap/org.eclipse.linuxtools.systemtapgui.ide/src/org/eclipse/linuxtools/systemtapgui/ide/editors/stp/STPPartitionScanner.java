/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtapgui.ide.editors.stp;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.systemtapgui.logging.LogManager;


public class STPPartitionScanner extends RuleBasedPartitionScanner {
	public final static String STP_DEFAULT = "__stp_default";
	public final static String STP_COMMENT = "__stp_comment";
	public final static String STP_EMBEDDEDC = "__stp_embeddedc"; 
	public final static String STP_EMBEDDED = "__stp_embedded";
	
	/**
	 * Sets up the STP Partition Scanner, specifies multi line rules to govern the use of comments and
	 * embedded C code in the editor.
	 */
	public STPPartitionScanner() {
		LogManager.logDebug("Start STPPartitionScanner:", this);
		IToken stpComment = new Token(STP_COMMENT);
		IToken embeddedC = new Token(STP_EMBEDDEDC);
		IToken embedded = new Token(STP_EMBEDDED);
		
		IPredicateRule[] rules = new IPredicateRule[3];

		rules[0] = new MultiLineRule("/*", "*/", stpComment);
		rules[1] = new MultiLineRule("%{", "%}", embeddedC); 
		rules[2] = new MultiLineRule("%(", "%)", embedded); 
		
		setPredicateRules(rules);
		LogManager.logDebug("End STPPartitionScanner", this);
	}
}
