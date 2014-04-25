/*******************************************************************************
 * Copyright (c) 2008, 2009 Phil Muldoon <pkmuldoon@picobot.org>.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pkmuldoon@picobot.org> - initial API.
 *    Red Hat - modifications for use with Valgrind plugins.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

public class SuppressionsPartitionScanner extends RuleBasedPartitionScanner {
    public static final String SUPP_TOOL = "__supp_tool"; //$NON-NLS-1$
    public static final String SUPP_TYPE = "__supp_type"; //$NON-NLS-1$
    public static final String SUPP_CONTEXT = "__supp_context"; //$NON-NLS-1$
    public static final String SUPP_COMMENT = "__supp_comment"; //$NON-NLS-1$

    public static final String[] SUPP_CONTENT_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, SUPP_TOOL, SUPP_TYPE, SUPP_CONTEXT, SUPP_COMMENT };

    public SuppressionsPartitionScanner() {
        IToken commentToken = new Token(SUPP_COMMENT);

        setPredicateRules(new IPredicateRule[] {
                new EndOfLineRule("#", commentToken) //$NON-NLS-1$
        });
    }

}
