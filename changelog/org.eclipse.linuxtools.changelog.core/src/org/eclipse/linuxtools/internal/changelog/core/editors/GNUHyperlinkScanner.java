/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.changelog.core.editors;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

public class GNUHyperlinkScanner extends RuleBasedScanner {

	public static final String FILE_NAME = "_file_name"; // $NON-NLS-1$
	public static final String OTHER = "_other"; // $NON-NLS-1$

	/**
	 * Build a scanner for hyperlink.
	 *
	 */
	public GNUHyperlinkScanner() {
		IToken file = new Token(FILE_NAME);

		IToken other = new Token(OTHER);

		IRule[] rules = new IRule[1];

		// Add rule for file path
		rules[0] = new GNUFileEntryRule(file);

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
