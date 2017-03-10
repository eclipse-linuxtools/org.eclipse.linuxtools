/*******************************************************************************
 * Copyright (c) 2007, 2017 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.PackageWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.TagWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.MacroRule;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;

/**
 * This class is specifically used for syntax coloring of Requires,
 * BuildRquires, etc... tags of a specfile.
 *
 */
public class SpecfilePackagesScanner extends RuleBasedScanner {

	protected static final String[] PACKAGES_TAGS = { "BuildRequires", "BuildConflicts", //$NON-NLS-1$ //$NON-NLS-2$
			"BuildPreReq", "Enhances", "Recommends", "Requires", "Requires(post)", "Requires(postun)", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"Requires(pre)", "Requires(preun)", "Requires(hint)", "Conflicts", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"Obsoletes", "Prereq", "Suggests", "Supplements" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	public SpecfilePackagesScanner() {
		super();
		ColorRegistry colors = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();
		IToken packageToken = new Token(
				new TextAttribute(colors.get(ISpecfileColorConstants.PACKAGES), null, SWT.NONE));

		IToken tagToken = new Token(new TextAttribute(colors.get(ISpecfileColorConstants.TAGS)));

		IToken macroToken = new Token(new TextAttribute(colors.get(ISpecfileColorConstants.MACROS)));

		List<IRule> rules = new ArrayList<>();

		rules.add(new MacroRule(macroToken));

		// BuildRequires:, ...
		WordRule wordRule = new WordRule(new TagWordDetector(), Token.UNDEFINED);
		for (String packageTag : PACKAGES_TAGS) {
			wordRule.addWord(packageTag + ":", tagToken); //$NON-NLS-1$
		}
		rules.add(wordRule);

		// RPM packages
		wordRule = new WordRule(new PackageWordDetector(), Token.UNDEFINED);
		List<String[]> rpmPackages = Activator.getDefault().getRpmPackageList().getProposals(""); //$NON-NLS-1$
		char[] startWith = { ' ', '\t', ',', ':' };
		for (String[] item : rpmPackages) {
			// FIXME Perhaps, that can slow down the scanning?
			for (char startChar : startWith) {
				wordRule.addWord(startChar + item[0], packageToken);
			}
		}
		rules.add(wordRule);

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
		setDefaultReturnToken(new Token(new TextAttribute(colors.get(ISpecfileColorConstants.DEFAULT))));
	}
}
