/*******************************************************************************
 * Copyright (c) 2007 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.scanners;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.linuxtools.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.rpm.ui.editor.detectors.KeywordWordDetector;
import org.eclipse.linuxtools.rpm.ui.editor.detectors.MacroWordDetector;
import org.eclipse.linuxtools.rpm.ui.editor.detectors.PatchNumberDetector;
import org.eclipse.linuxtools.rpm.ui.editor.detectors.SuffixNumberDetector;
import org.eclipse.linuxtools.rpm.ui.editor.detectors.TagWordDetector;
import org.eclipse.linuxtools.rpm.ui.editor.rules.StringWithEndingRule;
import org.eclipse.swt.SWT;
import static org.eclipse.linuxtools.rpm.ui.editor.RpmSections.*;
import static org.eclipse.linuxtools.rpm.ui.editor.RpmTags.*;

public class SpecfileScanner extends RuleBasedScanner {

	private static String[] sections = { PREP_SECTION, BUILD_SECTION, INSTALL_SECTION,
		PRETRANS_SECTION, PRE_SECTION, PREUN_SECTION, POST_SECTION, FILES_SECTION, CHANGELOG_SECTION,
		PACKAGE_SECTION, DESCRIPTION_SECTION, POSTUN_SECTION, POSTTRANS_SECTION, CLEAN_SECTION, 
		CHECK_SECTION };

	public static String[] DEFINED_MACROS = { "%define", "%make", "%setup", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"%attrib", "%defattr", "%attr", "%dir", "%config", "%docdir", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"%doc", "%lang", "%verify", "%ghost" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private static String[] keywords = { "%if", "%ifarch", "%ifnarch", "%else", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"%endif" }; //$NON-NLS-1$

	private static String[] TAGS = { SUMMARY, NAME, VERSION, PACKAGER, ICON,
			URL, PREFIX, GROUP, LICENSE, RELEASE, BUILD_ROOT, DISTRIBUTION,
			VENDOR, PROVIDES, EXCLUSIVE_ARCH, EXCLUDE_ARCH, EXCLUDE_OS,
			BUILD_ARCH, BUILD_ARCHITECTURES, AUTO_REQUIRES, AUTO_REQ,
			AUTO_REQ_PROV, AUTO_PROV, EPOCH };

	public SpecfileScanner(ColorManager manager) {
		IToken sectionToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.SECTIONS), null, SWT.ITALIC));

		IToken macroToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.MACROS)));

		IToken keywordToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.KEYWORDS), null, SWT.BOLD));

		IToken tagToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.TAGS)));

		IToken commentToken = new Token(new TextAttribute(manager
				.getColor(ISpecfileColorConstants.COMMENT)));

		List<IRule> rules = new ArrayList<IRule>();

		// Comments
		rules.add(new EndOfLineRule("#", commentToken)); //$NON-NLS-1$

		// %{ .... }
		rules.add(new SingleLineRule("%{", "}", macroToken)); //$NON-NLS-1$ //$NON-NLS-2$

		// %define, %make, ...
		WordRule wordRule = new WordRule(new MacroWordDetector(),
				Token.UNDEFINED);
		for (String definedMacro : DEFINED_MACROS) {
			wordRule.addWord(definedMacro, macroToken);
		}
		rules.add(wordRule);

		// %patch[0-9]+[\ \t]
		rules.add(new StringWithEndingRule("%patch", new PatchNumberDetector(), //$NON-NLS-1$
				macroToken, false ));

		// %if, %else ...
		wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (String keyword : keywords) {
			wordRule.addWord(keyword, keywordToken);
		}
		rules.add(wordRule);

		// %prep, %build, ...
		wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (String section : sections) {
			wordRule.addWord(section, sectionToken);
		}
		rules.add(wordRule);

		// Name:, Summary:, ...
		wordRule = new WordRule(new TagWordDetector(), Token.UNDEFINED);
		for (String tag : TAGS) {
			wordRule.addWord(tag + ":", tagToken); //$NON-NLS-1$
		}
		rules.add(wordRule);

		// Source[0-9]*:, Patch[0-9]*:
		rules.add(new StringWithEndingRule("Source", //$NON-NLS-1$
				new SuffixNumberDetector(), tagToken, false));
		rules.add(new StringWithEndingRule("Patch", new SuffixNumberDetector(), //$NON-NLS-1$
				tagToken, false));

		IRule[] result = new IRule[rules.size()];
		rules.toArray(result);
		setRules(result);
	}
}
