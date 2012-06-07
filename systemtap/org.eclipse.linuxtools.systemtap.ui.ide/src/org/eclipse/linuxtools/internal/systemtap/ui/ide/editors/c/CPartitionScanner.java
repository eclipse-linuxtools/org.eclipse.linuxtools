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

package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.c;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;


/**
 * This class is the Partition Scanner used by the CEditor. It is responsible for
 * detecting a multiline C comment and partitioning it to type <code>C_COMMENT</code>
 * so that the <code>CScanner</code> can highlight multiline comments.
 * @author Henry Hughes
 * @author Ryan Morse
 * @see org.eclipse.jface.text.rules.RuleBasedPartitionScanner
 */
public class CPartitionScanner extends RuleBasedPartitionScanner {
	public final static String C_DEFAULT = "__stp_default";
	public final static String C_COMMENT = "__stp_comment";
	
	public CPartitionScanner() {
		LogManager.logDebug("Start CPartitionScanner:", this); //$NON-NLS-1$
		IToken stpComment = new Token(C_COMMENT);
		IPredicateRule[] rules = new IPredicateRule[1];
		rules[0] = new MultiLineRule("/*", "*/", stpComment);
		setPredicateRules(rules);
		LogManager.logDebug("End CPartitionScanner:", this); //$NON-NLS-1$
	}
}
