/*******************************************************************************
 * Copyright (c) 2006, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Kyu Lee <klee@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.ui.PlatformUI;

/**
 * Recognizes GNU format changelog. Can be configured to return different types
 * of tokens.
 */
public class GNUElementScanner extends RuleBasedScanner {

	/**
	 * Build a scanner for syntax highlighting.
	 *
	 */
	public GNUElementScanner() {
		ColorRegistry colorRegistry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		IToken file = new Token(new TextAttribute(colorRegistry.get(IChangeLogColorConstants.FILE_NAME)));

		IToken func = new Token(new TextAttribute(colorRegistry.get(IChangeLogColorConstants.FUNC_NAME)));

		IToken email = new Token(new TextAttribute(colorRegistry.get(IChangeLogColorConstants.EMAIL)));

		IToken other = new Token(new TextAttribute(colorRegistry.get(IChangeLogColorConstants.TEXT)));

		IRule[] rules = new IRule[3];

		// Add rule for file path
		rules[0] = new GNUFileEntryRule(file);

		// function
		rules[1] = new SingleLineRule("(", ")", func); // $NON-NLS-1$ //
														// $NON-NLS-2$
		// email
		rules[2] = new SingleLineRule("<", ">\n", email); // $NON-NLS-1$ //
															// $NON-NLS-2$

		setDefaultReturnToken(other);

		setRules(rules);
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
