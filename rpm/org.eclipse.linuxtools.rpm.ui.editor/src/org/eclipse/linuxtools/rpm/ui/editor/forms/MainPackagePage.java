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
package org.eclipse.linuxtools.rpm.ui.editor.forms;

import org.eclipse.linuxtools.rpm.ui.editor.RpmTags;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
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
	SpecfileParser parser;
	SpecfileFormEditor editor;

	public MainPackagePage(SpecfileFormEditor editor, Specfile specfile) {
		super(editor, "Overview", "Overview");
		this.editor = editor;
		this.specfile = specfile;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		form.setText("Main Package information");
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 5;
		layout.numColumns = 2;
		RowLayout rowLayout = new RowLayout();
		rowLayout.type = SWT.VERTICAL;
		form.getBody().setLayout(rowLayout);
		layout.numColumns = 2;
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		final Section section = toolkit.createSection(form.getBody(),
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
						| ExpandableComposite.EXPANDED);
		section.setText("Main package information");
		section.setLayout(new GridLayout());
		Composite client2 = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = gridLayout.marginHeight = 5;
		gridLayout.numColumns = 2;
		client2.setLayout(gridLayout);
		new RpmTagText(client2, RpmTags.NAME, specfile);
		new RpmTagText(client2, RpmTags.VERSION, specfile);
		new RpmTagText(client2, RpmTags.RELEASE, specfile);
		new RpmTagText(client2, RpmTags.URL, specfile);
		new RpmTagText(client2, RpmTags.LICENSE, specfile);
		new RpmTagText(client2, RpmTags.GROUP, specfile);
		new RpmTagText(client2, RpmTags.EPOCH, specfile);
		new RpmTagText(client2, RpmTags.BUILD_ROOT, specfile);
		new RpmTagText(client2, RpmTags.BUILD_ARCH, specfile);
		new RpmTagText(client2, RpmTags.SUMMARY, specfile, SWT.MULTI);
		section.setClient(client2);
		toolkit.paintBordersFor(client2);
		toolkit.paintBordersFor(section);
		// subpackages
		final Section packagesSection = toolkit.createSection(form.getBody(),
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
						| ExpandableComposite.EXPANDED);
		packagesSection.setText("Subpackages");
		packagesSection.setLayout(rowLayout);
		Composite packagesClient = toolkit.createComposite(packagesSection);
		packagesClient.setLayout(rowLayout);
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
			Composite packageClient = toolkit.createComposite(packageSection);
			packageClient.setLayout(gridLayout);
			new RpmTagText(packageClient, RpmTags.SUMMARY, specfile, specfilePackage, SWT.MULTI);
			new RpmTagText(packageClient, RpmTags.GROUP, specfile, specfilePackage, SWT.MULTI);
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
