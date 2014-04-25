/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat - initial API and implementation
 *    Alphonse Van Assche
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.scanners;

import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.BUILD_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CHANGELOG_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.CLEAN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.FILES_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.INSTALL_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTTRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POSTUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.POST_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREP_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRETRANS_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PREUN_SECTION;
import static org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections.PRE_SECTION;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmSections;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.CommentRule;
import org.eclipse.linuxtools.internal.rpm.ui.editor.rules.SectionRule;

public class SpecfilePartitionScanner extends RuleBasedPartitionScanner {

    public static final String SPEC_PREP = "__spec_prep"; //$NON-NLS-1$
    public static final String SPEC_SCRIPT = "__spec_script"; //$NON-NLS-1$
    public static final String SPEC_FILES = "__spec_files"; //$NON-NLS-1$
    public static final String SPEC_CHANGELOG = "__spec_changelog"; //$NON-NLS-1$
    public static final String SPEC_PACKAGES = "__spec_packages"; //$NON-NLS-1$
    public static final String SPEC_GROUP = "__spec_group"; //$NON-NLS-1$
    public static final String SPEC_FILE_PARTITIONING = "___spec_partitioning"; //$NON-NLS-1$

    public static final String[] SPEC_PARTITION_TYPES = { IDocument.DEFAULT_CONTENT_TYPE, SPEC_PREP, SPEC_SCRIPT,
            SPEC_FILES, SPEC_CHANGELOG, SPEC_PACKAGES, SPEC_GROUP};

    /** All possible headers for sections of the type SPEC_SCRIPT */
    private static final String[] SECTION_HEADERS = { BUILD_SECTION, INSTALL_SECTION,
        PRETRANS_SECTION, PRE_SECTION, PREUN_SECTION, POST_SECTION, POSTUN_SECTION,
        POSTTRANS_SECTION, CLEAN_SECTION};

    /** All possible headers for section that can come after sections of the type SPEC_SCRIPT */
    private static final String[] SECTION_ENDING_HEADERS = { BUILD_SECTION, INSTALL_SECTION,
        PRETRANS_SECTION, PRE_SECTION, PREUN_SECTION, POST_SECTION, POSTUN_SECTION, POSTTRANS_SECTION,
        CLEAN_SECTION, FILES_SECTION};

    public SpecfilePartitionScanner() {
        super();

        IToken specPrep = new Token(SPEC_PREP);
        IToken specScript = new Token(SPEC_SCRIPT);
        IToken specFiles = new Token(SPEC_FILES);
        IToken specChangelog = new Token(SPEC_CHANGELOG);
        IToken specPackages = new Token(SPEC_PACKAGES);
        IToken specGroup = new Token(SPEC_GROUP);

        List<IRule> rules = new ArrayList<>();

        // RPM packages
        for (String packageTag : SpecfilePackagesScanner.PACKAGES_TAGS) {
            rules.add(new SingleLineRule(packageTag, "", specPackages, (char)0 , true));         //$NON-NLS-1$
        }

        // %prep
        rules.add(new SectionRule(PREP_SECTION, new String[] { BUILD_SECTION }, specPrep));

        // %changelog
        rules.add(new MultiLineRule(RpmSections.CHANGELOG_SECTION, "", specChangelog, (char)0 , true)); //$NON-NLS-1$

        // "%build", "%install", "%pre", "%preun", "%post", "%postun"
        for (String sectionHeader : SECTION_HEADERS) {
            rules.add(new SectionRule(sectionHeader, SECTION_ENDING_HEADERS, specScript));
        }

        // comments
        rules.add(new CommentRule(specScript));

        // group tag
        rules.add(new EndOfLineRule("Group:", specGroup)); //$NON-NLS-1$


        // %files
        rules.add(new SectionRule(FILES_SECTION, new String[] { FILES_SECTION,
                CHANGELOG_SECTION }, specFiles));

        IPredicateRule[] result= new IPredicateRule[rules.size()];
        rules.toArray(result);
        setPredicateRules(result);
    }
}
