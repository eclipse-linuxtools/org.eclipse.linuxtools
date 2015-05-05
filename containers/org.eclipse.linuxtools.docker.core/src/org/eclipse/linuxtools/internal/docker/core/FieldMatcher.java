/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.linuxtools.docker.core.IFieldMatcher;

public class FieldMatcher implements IFieldMatcher {

	private String rule;
	private Pattern pattern;

	public FieldMatcher(String rule) throws PatternSyntaxException {
		this.rule = rule;
		String regexRule = transform(rule);
		pattern = Pattern.compile(regexRule);
	}

	private String transform(String rule) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < rule.length(); ++i) {
			char ch = rule.charAt(i);
			if (Character.isLetterOrDigit(ch))
				buffer.append(ch);
			else if (ch == '*') {
				buffer.append(".*?");
			} else {
				buffer.append('\\');
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}

	@Override
	public boolean matches(String input) {
		Matcher m = pattern.matcher(input);
		return m.matches();
	}

	@Override
	public String toString() {
		return rule;
	}

}
