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
package org.eclipse.linuxtools.changelog.core.editors;

import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.*;

/**
 * Recognizes GNU format changelog. Can be configured to return different types
 * of tokens.
 * 
 * @author klee (Kyu Lee)
 */
public class GNUElementScanner extends RuleBasedScanner {

	public static final String FILE_NAME = "_file_name";

	public static final String TEXT = "_text_content";

	public static final String EMAIL = "_author_email";

	public static final String DATE = "_entry_date";

	public static final String AUTHOR = "_author_name";

	public static final String FUNC_NAME = "_function_name";

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

		IRule[] rules = new IRule[4];

		// Add rule for file path
		rules[0] = new SingleLineRule("* ", " ", file, '\0', true);
		rules[1] = new SingleLineRule("* ", ":", file);

		// function
		rules[2] = new SingleLineRule("(", "):", func);
		// email
		rules[3] = new SingleLineRule("<", ">\n", email);
		setRules(rules);
	}

	/**
	 * Build a scanner for hyperlink.
	 * 
	 */
	public GNUElementScanner() {
		IToken file = new Token(new String(FILE_NAME));

		IToken func = new Token(new String(FUNC_NAME));

		IToken email = new Token(new String(EMAIL));

		IRule[] rules = new IRule[4];

		// Add rule for file path
		rules[0] = new SingleLineRule("* ", " ", file, '\0', true);
		rules[1] = new SingleLineRule("* ", ":", file);

		// function
		rules[2] = new SingleLineRule("(", "):", func);
		// email
		rules[3] = new SingleLineRule("<", ">", email);

		setRules(rules);
	}

}
