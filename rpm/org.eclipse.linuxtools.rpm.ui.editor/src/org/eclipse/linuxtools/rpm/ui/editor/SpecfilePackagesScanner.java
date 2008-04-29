/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

/**
 * This class is used specifically for syntax coloring of the Requires 
 * and BuildRquires, etc... tags of a spec file.
 * 
 */
public class SpecfilePackagesScanner extends RuleBasedScanner {

	private IToken fLastToken;

	protected static final String[] PACKAGES_TAGS = { "BuildRequires", "BuildConflicts",
			"BuildPreReq", "Requires", "Requires(post)", "Requires(postun)",
			"Requires(pre)", "Requires(preun)", "Requires(hint)", "Conflicts", "Obsoletes", "Prereq" };

	public SpecfilePackagesScanner(ColorManager manager) {
		IToken packageToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.PACKAGES), null, SWT.NONE));

		IToken tagToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.TAGS)));

		IToken macroToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.MACROS)));

		List<IRule> rules = new ArrayList<IRule>();

		// %{ .... }
		rules.add(new SingleLineRule("%{", "}", macroToken));

		// BuildRequires:, ...
		WordRule wordRule = new WordRule(new TagWordDetector(), Token.UNDEFINED);
		for (int i = 0; i < PACKAGES_TAGS.length; i++)
			wordRule.addWord(PACKAGES_TAGS[i] + ":", tagToken);
		rules.add(wordRule);

		// RPM packages
		wordRule = new WordRule(new PackageWordDetector(), Token.UNDEFINED);
		List<String[]> rpmPackages = Activator.getDefault().getRpmPackageList()
				.getProposals("");
		Iterator<String[]> iterator = rpmPackages.iterator();
		String[] item;
		char[] startWith = {' ', '\t', ',', ':'};
		while (iterator.hasNext()) {
			item = iterator.next();
			// FIXME Perhaps, that can slow down the scanning?
			for (int i = 0; i < startWith.length; i++) {
				wordRule.addWord(startWith[i] + item[0], packageToken);
			}
		}
		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}

	protected IToken getLastToken() {
		return fLastToken;
	}

	public IToken nextToken() {
		fLastToken = super.nextToken();
		return fLastToken;
	}
}
