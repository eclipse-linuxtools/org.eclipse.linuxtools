/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners;

import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.BUILD_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CHANGELOG_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CHECK_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CLEAN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.DESCRIPTION_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.FILES_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.INSTALL_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PACKAGE_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTTRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POST_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREP_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRETRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRE_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.AUTO_PROV;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.AUTO_REQ;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.AUTO_REQUIRES;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.AUTO_REQ_PROV;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.BUILD_ARCH;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.BUILD_ARCHITECTURES;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.BUILD_ROOT;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.DISTRIBUTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.EPOCH;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.EXCLUDE_ARCH;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.EXCLUDE_OS;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.EXCLUSIVE_ARCH;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.GROUP;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.ICON;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.LICENSE;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.NAME;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.PACKAGER;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.PREFIX;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.PROVIDES;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.RELEASE;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.SUMMARY;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.URL;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.VENDOR;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags.VERSION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ColorManager;
import org.eclipse.linuxtools.internal.rpm.ui.editor.ISpecfileColorConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.KeywordWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.MacroWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.PatchNumberDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.SuffixNumberDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.TagWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.CommentRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.MacroRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.StringWithEndingRule;
import org.eclipse.swt.SWT;

public class SpecfileScanner extends RuleBasedScanner {

	private static final String[] SECTIONS = { PREP_SECTION, BUILD_SECTION, INSTALL_SECTION,
		PRETRANS_SECTION, PRE_SECTION, PREUN_SECTION, POST_SECTION, FILES_SECTION, CHANGELOG_SECTION,
		PACKAGE_SECTION, DESCRIPTION_SECTION, POSTUN_SECTION, POSTTRANS_SECTION, CLEAN_SECTION,
		CHECK_SECTION };

	public static final String[] DEFINED_MACROS = {
			"%define", "%global", "%make", "%setup", "%autosetup", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"%attrib", "%defattr", "%attr", "%dir", "%config", "%docdir", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"%doc", "%lang", "%verify", "%ghost" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private static final String[] KEYWORDS = { "%if", "%ifarch", "%ifnarch", "%else", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"%endif" }; //$NON-NLS-1$

	private static final String[] TAGS = { SUMMARY, NAME, VERSION, PACKAGER, ICON,
			URL, PREFIX, GROUP, LICENSE, RELEASE, BUILD_ROOT, DISTRIBUTION,
			VENDOR, PROVIDES, EXCLUSIVE_ARCH, EXCLUDE_ARCH, EXCLUDE_OS,
			BUILD_ARCH, BUILD_ARCHITECTURES, AUTO_REQUIRES, AUTO_REQ,
			AUTO_REQ_PROV, AUTO_PROV, EPOCH };

	public SpecfileScanner(ColorManager manager) {
		super();
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

		List<IRule> rules = new ArrayList<>();

		rules.add(new CommentRule(commentToken));
		rules.add(new MacroRule( macroToken));

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
		for (String keyword : KEYWORDS) {
			wordRule.addWord(keyword, keywordToken);
		}
		rules.add(wordRule);

		// %prep, %build, ...
		wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
		for (String section : SECTIONS) {
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
