/*******************************************************************************
 * Copyright (c) 2006 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

/**
 * Recognizes GNU format changelog. Can be configured to return different types
 * of tokens.
 *
 * @author klee (Kyu Lee)
 */
public class GNUElementScanner extends RuleBasedScanner {

    public static final String FILE_NAME = "_file_name"; // $NON-NLS-1$
    public static final String TEXT = "_text_content"; // $NON-NLS-1$
    public static final String EMAIL = "_author_email"; // $NON-NLS-1$
    public static final String DATE = "_entry_date"; // $NON-NLS-1$
    public static final String AUTHOR = "_author_name"; // $NON-NLS-1$
    public static final String FUNC_NAME = "_function_name"; // $NON-NLS-1$
    public static final String OTHER = "_other"; // $NON-NLS-1$
    /**
     * Build a scanner for syntax highlighting.
     *
     * @param manager Color scheme to use.
     */
    public GNUElementScanner(ColorManager manager) {
        IToken file = new Token(new TextAttribute(manager
                .getColor(IChangeLogColorConstants.FILE_NAME)));

        IToken func = new Token(new TextAttribute(manager
                .getColor(IChangeLogColorConstants.FUNC_NAME)));

        IToken email = new Token(new TextAttribute(manager
                .getColor(IChangeLogColorConstants.EMAIL)));

        IToken other = new Token(new TextAttribute(manager
                .getColor(IChangeLogColorConstants.TEXT)));

        IRule[] rules = new IRule[3];

        // Add rule for file path
        rules[0] = new GNUFileEntryRule(file);

        // function
        rules[1] = new SingleLineRule("(", ")", func); // $NON-NLS-1$ // $NON-NLS-2$
        // email
        rules[2] = new SingleLineRule("<", ">\n", email); // $NON-NLS-1$ // $NON-NLS-2$

        setDefaultReturnToken(other);

        setRules(rules);
    }

    /**
     * Build a scanner for hyperlink.
     *
     */
    public GNUElementScanner() {
        IToken file = new Token(FILE_NAME);

        IToken func = new Token(FUNC_NAME);

        IToken email = new Token(EMAIL);

        IToken other = new Token(OTHER);

        IRule[] rules = new IRule[3];

        // Add rule for file path
        rules[0] = new GNUFileEntryRule(file);

        // function
        rules[1] = new SingleLineRule("(", "):", func); // $NON-NLS-1$ // $NON-NLS-2$
        // email
        rules[2]= new SingleLineRule("<", ">", email); // $NON-NLS-1$ // $NON-NLS-2$

        setDefaultReturnToken(other);

        setRules(rules);
    }

    /**
     * Get the file offset.
     *
     * @return the file offset.
     */
    public int getOffset() {
        return fOffset;
    }

    /**
     * Get the default token.
     *
     * @return the default token.
     */
    public IToken getDefaultToken() {
        return fDefaultReturnToken;
    }
}
