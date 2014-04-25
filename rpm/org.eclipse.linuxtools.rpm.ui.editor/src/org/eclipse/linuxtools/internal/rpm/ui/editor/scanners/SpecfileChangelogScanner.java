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
import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections;
import org.eclipse.linuxtools.internal.rpm.ui.editor.detectors.KeywordWordDetector;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.AuthorEmailRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.VersionReleaseRule;
import org.eclipse.swt.SWT;
/**
 * This class is used specifically of the syntax coloring of the %changelog
 * section of a spec file, which has completely different syntax than
 * the rest of the file.
 *
 */
public class SpecfileChangelogScanner extends RuleBasedScanner {

    private IToken fLastToken;

    public SpecfileChangelogScanner(ColorManager manager) {
        super();
        IToken sectionToken = new Token(new TextAttribute(manager
                .getColor(ISpecfileColorConstants.SECTIONS), null, SWT.ITALIC));

        IToken authorEmail = new Token(new TextAttribute(manager
                .getColor(ISpecfileColorConstants.AUTHOR_MAIL), null, SWT.NONE));

        IToken versionRelease = new Token(new TextAttribute(manager
                .getColor(ISpecfileColorConstants.VER_REL), null, SWT.NONE));

        List<IRule> rules = new ArrayList<>();

        // %prep, %build, ...
        WordRule wordRule = new WordRule(new KeywordWordDetector(), Token.UNDEFINED);
        wordRule.addWord(RpmSections.CHANGELOG_SECTION, sectionToken);
        rules.add(wordRule);

        AuthorEmailRule emailRule= new AuthorEmailRule(authorEmail);
        rules.add(emailRule);

        VersionReleaseRule verRelRule = new VersionReleaseRule(versionRelease, authorEmail, this);
        rules.add(verRelRule);

        IRule[] result = new IRule[rules.size()];
        rules.toArray(result);
        setRules(result);
    }

    public IToken getLastToken (){
        return fLastToken;
    }

    @Override
    public IToken nextToken() {
        fLastToken = super.nextToken();
        return fLastToken;
    }
}
