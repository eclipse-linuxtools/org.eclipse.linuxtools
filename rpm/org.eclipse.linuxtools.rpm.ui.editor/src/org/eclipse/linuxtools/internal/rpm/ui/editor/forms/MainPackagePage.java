/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor.forms;

import org.eclipse.linuxtools.internal.rpm.ui.editor.RpmTags;
import org.eclipse.linuxtools.internal.rpm.ui.editor.parser.SpecfileTag;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class MainPackagePage extends FormPage {
    private FormToolkit toolkit;
    private ScrolledForm form;
    private Specfile specfile;

    public MainPackagePage(SpecfileFormEditor editor, Specfile specfile) {
        super(editor, Messages.MainPackagePage_0, Messages.MainPackagePage_1);
        this.specfile = specfile;
    }

    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        toolkit = managedForm.getToolkit();
        form = managedForm.getForm();
        form.setText(Messages.MainPackagePage_2);
        GridLayout layout = new GridLayout();
        layout.marginWidth = layout.marginHeight = 5;
        layout.numColumns = 2;
        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.VERTICAL;
        rowLayout.justify = true;
        rowLayout.fill = true;

        form.getBody().setLayout(rowLayout);
        form.getBody().setLayoutData(rowLayout);
        layout.numColumns = 2;
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.horizontalAlignment = SWT.FILL;
        final Section mainPackageSection = toolkit.createSection(form.getBody(),
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED);
        mainPackageSection.setText(Messages.MainPackagePage_3);
        mainPackageSection.setLayout(new GridLayout());
        Composite mainPackageClient = toolkit.createComposite(mainPackageSection);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginWidth = gridLayout.marginHeight = 5;
        gridLayout.numColumns = 2;

        mainPackageClient.setLayout(gridLayout);
        new RpmTagText(mainPackageClient, RpmTags.NAME, specfile);
        new RpmTagText(mainPackageClient, RpmTags.VERSION, specfile);
        new RpmTagText(mainPackageClient, RpmTags.RELEASE, specfile);
        new RpmTagText(mainPackageClient, RpmTags.URL, specfile);
        new RpmTagText(mainPackageClient, RpmTags.LICENSE, specfile);
        new RpmTagText(mainPackageClient, RpmTags.GROUP, specfile);
        new RpmTagText(mainPackageClient, RpmTags.EPOCH, specfile);
        new RpmTagText(mainPackageClient, RpmTags.BUILD_ROOT, specfile);
        new RpmTagText(mainPackageClient, RpmTags.BUILD_ARCH, specfile);
        new RpmTagText(mainPackageClient, RpmTags.SUMMARY, specfile, SWT.MULTI);

        // BuildRequires
        final Section buildRequiresSection = toolkit.createSection(mainPackageClient,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED);
        buildRequiresSection.setText(Messages.MainPackagePage_4);
        buildRequiresSection.setLayout(rowLayout);
        buildRequiresSection.setExpanded(false);
        Composite buildRequiresClient = toolkit.createComposite(buildRequiresSection);
        buildRequiresClient.setLayout(gridLayout);
        for (SpecfileTag buildRequire: specfile.getBuildRequires()) {
            new RpmTagText(buildRequiresClient, buildRequire, specfile);
        }
        buildRequiresSection.setClient(buildRequiresClient);
        toolkit.paintBordersFor(buildRequiresClient);
        toolkit.paintBordersFor(buildRequiresSection);

        // Requires
        final Section requiresSection = toolkit.createSection(mainPackageClient,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED);
        requiresSection.setText(Messages.MainPackagePage_5);
        requiresSection.setLayout(rowLayout);
        requiresSection.setExpanded(false);
        Composite requiresClient = toolkit.createComposite(requiresSection);
        requiresClient.setLayout(gridLayout);
        requiresClient.setLayoutData(gd);
        for (SpecfileTag require: specfile.getRequires()) {
            new RpmTagText(requiresClient, require, specfile);
        }
        requiresSection.setClient(requiresClient);
        toolkit.paintBordersFor(requiresClient);
        toolkit.paintBordersFor(requiresSection);

        mainPackageSection.setClient(mainPackageClient);
        toolkit.paintBordersFor(mainPackageClient);
        toolkit.paintBordersFor(mainPackageSection);

        // subpackages
        final Section packagesSection = toolkit.createSection(form.getBody(),
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED);
        packagesSection.setText(Messages.MainPackagePage_6);
        packagesSection.setLayout(gridLayout);
        Composite packagesClient = toolkit.createComposite(packagesSection);
        packagesClient.setLayout(gridLayout);
        packagesClient.setLayoutData(gd);
        for (SpecfilePackage specfilePackage : specfile.getPackages()
                .getPackages()) {
            if (specfilePackage.isMainPackage()){
                continue;
            }
            final Section packageSection = toolkit.createSection(packagesClient,
                    ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                            | ExpandableComposite.EXPANDED);
            packageSection.setText(specfilePackage.getFullPackageName());
            packageSection.setExpanded(false);
            packageSection.setLayout(rowLayout);
            Composite packageClient = toolkit.createComposite(packageSection);
            packageClient.setLayout(gridLayout);
            packageClient.setLayoutData(gd);
            new RpmTagText(packageClient, RpmTags.SUMMARY, specfile, specfilePackage, SWT.MULTI);
            new RpmTagText(packageClient, RpmTags.GROUP, specfile, specfilePackage, SWT.MULTI);

            final Section packageRequiresSection = toolkit.createSection(packageClient,
                    ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                            | ExpandableComposite.EXPANDED);
            packageRequiresSection.setText(Messages.MainPackagePage_7);
            packageRequiresSection.setLayout(rowLayout);
            packageRequiresSection.setLayoutData(gd);
            Composite packageRequiresClient = toolkit.createComposite(packageRequiresSection);
            packageRequiresClient.setLayout(gridLayout);
            packageRequiresClient.setLayoutData(gd);
            for (SpecfileTag require: specfilePackage.getRequires()) {
                new RpmTagText(packageRequiresClient, require, specfile);
            }
            packageRequiresSection.setClient(packageRequiresClient);

            toolkit.paintBordersFor(packageRequiresClient);
            toolkit.paintBordersFor(packageRequiresSection);

            packageSection.setClient(packageClient);
            toolkit.paintBordersFor(packageClient);
            toolkit.paintBordersFor(packageSection);
        }
        packagesSection.setClient(packagesClient);
        toolkit.paintBordersFor(packagesClient);
        toolkit.paintBordersFor(packagesSection);
        managedForm.refresh();
    }
}
