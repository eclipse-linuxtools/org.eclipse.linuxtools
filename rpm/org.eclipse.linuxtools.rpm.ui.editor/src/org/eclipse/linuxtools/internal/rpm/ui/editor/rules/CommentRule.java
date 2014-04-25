/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.rules;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;

/**
 * A rule for matching comment line.
 * Starts with # and ends at the end of line.
 *
 */
public class CommentRule extends EndOfLineRule{
    /**
     * Creates a EndofLineRule starting with the comment("#") symbol.
     *
     * @param token The token to look into.
     */
    public CommentRule(IToken token) {
        super(ISpecfileSpecialSymbols.COMMENT_START, token);
    }

}
