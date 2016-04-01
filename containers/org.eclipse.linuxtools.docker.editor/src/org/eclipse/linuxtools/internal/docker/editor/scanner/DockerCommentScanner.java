/*******************************************************************************
 * Copyright (c) 2015-2016 Open Analytics NV and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.editor.scanner;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.docker.editor.syntax.SyntaxColors;

public class DockerCommentScanner extends RuleBasedScanner {

	public static final String COMMENT_SEQUENCE = "#";

	public DockerCommentScanner() {
		IToken token = new Token(new TextAttribute(SyntaxColors.getCommentColor()));

		IRule[] rules = new IRule[1];
		rules[0] = new EndOfLineRule(COMMENT_SEQUENCE, token) {
			@Override
			public IToken evaluate(ICharacterScanner scanner, boolean resume) {
				if (getColumn() > 0)
					return Token.UNDEFINED;
				return super.evaluate(scanner, resume);
			}
		};

		setRules(rules);
	}
}
