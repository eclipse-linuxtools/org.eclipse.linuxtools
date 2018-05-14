/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.rules;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileSpecialSymbols;

/**
 * A rule for matching comment line. Starts with # and ends at the end of line.
 *
 */
public class CommentRule extends EndOfLineRule {
	/**
	 * Creates a EndofLineRule starting with the comment("#") symbol.
	 *
	 * @param token The token to look into.
	 */
	public CommentRule(IToken token) {
		super(ISpecfileSpecialSymbols.COMMENT_START, token);
	}

}
