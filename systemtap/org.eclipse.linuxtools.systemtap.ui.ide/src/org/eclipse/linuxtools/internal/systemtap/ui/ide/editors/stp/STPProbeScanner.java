/*******************************************************************************
 * Copyright (c) 2008, 2013 Phil Muldoon <pkmuldoon@picobot.org>.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API and implementation. 
 *    Red Hat Inc. - modification for use in probe scanning
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.editors.stp;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class STPProbeScanner extends RuleBasedPartitionScanner {
	public final static String STP_STRING = "__stp_string"; //$NON-NLS-1$
	public final static String STP_PROBE = "__stp_probe"; //$NON-NLS-1$
	public final static String STP_CONDITIONAL = "__stp_conditional"; //$NON-NLS-1$
	public final static String STP_COMMENT = "__stp_comment"; //$NON-NLS-1$
	
	public final static String STP_PROBE_PARTITIONING = "__stp_probe_partitioning"; //$NON-NLS-1$

	public static String[] STP_PROBE_PARTITION_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, 
		STP_COMMENT, STP_STRING, STP_PROBE, STP_CONDITIONAL};
	
	public STPProbeScanner() {
		
		IToken stpString = new Token(STP_STRING);
		IToken stpProbe = new Token(STP_PROBE);
		IToken stpComment = new Token(STP_COMMENT);
		IToken stpConditional = new Token(STP_CONDITIONAL);

        setPredicateRules(new IPredicateRule[] {
        		new EndOfLineRule("//", stpComment), //$NON-NLS-1$
        		new MultiLineRule("/*", "*/", stpComment),  //$NON-NLS-1$//$NON-NLS-2$
                new EndOfLineRule("#",  stpComment), //$NON-NLS-1$   
                new EndOfLineRule("#if", stpConditional), //$NON-NLS-1$
                new EndOfLineRule("#else", stpConditional), //$NON-NLS-1$
                new EndOfLineRule("#endif", stpConditional), //$NON-NLS-1$
                new EndOfLineRule("#define", stpConditional), //$NON-NLS-1$
        		new SingleLineRule("\"", "\"", stpString, '\\', false, true), //$NON-NLS-1$ //$NON-NLS-2$
        		new SingleLineRule("'", "'", stpString, '\\'), //$NON-NLS-1$ //$NON-NLS-2$
                new MultiLineRule("probe", "}", stpProbe), //$NON-NLS-1$ //$NON-NLS-2$
             });

	}
}
