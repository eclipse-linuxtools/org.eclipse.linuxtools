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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.swt.SWT;

public class SuppressionsElementScanner extends BufferedRuleBasedScanner {

    public static final String MEMCHECK = "Memcheck"; //$NON-NLS-1$
    public static final String[] MEMCHECK_SUPP_TYPES = new String[] {
            "Value0", "Value1", "Value2", "Value4", "Value8", "Value16", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
            "Cond", //$NON-NLS-1$
            "Addr1", "Addr2", "Addr4", "Addr8", "Addr16", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            "Jump", //$NON-NLS-1$
            "Param", //$NON-NLS-1$
            "Free", //$NON-NLS-1$
            "Overlap", //$NON-NLS-1$
            "Leak" //$NON-NLS-1$
    };
    public static final String[] CONTEXTS = new String[] { "obj", "fun" //$NON-NLS-1$ //$NON-NLS-2$
    };

    public SuppressionsElementScanner(ColorManager colorManager) {
        String[] tools = { MEMCHECK };
        Map<String, List<String>> kinds = new HashMap<>();
        kinds.put(MEMCHECK, Arrays.asList(MEMCHECK_SUPP_TYPES));

        IToken defaultToken = new Token(new TextAttribute(colorManager
                .getColor(ISuppressionsColorConstants.DEFAULT)));
        IToken toolToken = new Token(new TextAttribute(colorManager
                .getColor(ISuppressionsColorConstants.TOOL), null, SWT.BOLD));
        IToken suppKindToken = new Token(new TextAttribute(colorManager
                .getColor(ISuppressionsColorConstants.SUPP_TYPE)));
        IToken contextToken = new Token(new TextAttribute(colorManager
                .getColor(ISuppressionsColorConstants.CONTEXT), null, SWT.BOLD));
        IToken commentToken = new Token(new TextAttribute(colorManager
                .getColor(ISuppressionsColorConstants.COMMENT)));

        setDefaultReturnToken(defaultToken);
        setRules(new IRule[] {
                new EndOfLineRule("#", commentToken), //$NON-NLS-1$
                new SuppressionToolRule(tools, toolToken),
                new SuppressionKindRule(kinds, suppKindToken),
                new SuppressionToolRule(CONTEXTS, contextToken),
                new WhitespaceRule(new IWhitespaceDetector() {
                    @Override
                    public boolean isWhitespace(char c) {
                        return Character.isWhitespace(c);
                    }
                }) });
    }
}
