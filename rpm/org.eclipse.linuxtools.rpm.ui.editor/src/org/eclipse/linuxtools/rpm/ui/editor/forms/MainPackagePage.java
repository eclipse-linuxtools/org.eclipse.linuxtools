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
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
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
		form.getBody().setLayout(layout);
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
		toolkit.createLabel(client2, RpmTags.NAME, SWT.SINGLE);
		Text proba = new Text(client2, SWT.BORDER_SOLID);
		proba.setText("proba");
		final Text nameText = toolkit.createText(client2, specfile.getName(),
				SWT.BORDER_SOLID);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				specfile.modifyDefine(RpmTags.NAME, nameText.getText());
			}
		});
		toolkit.createLabel(client2, RpmTags.VERSION);
		Text text = toolkit.createText(client2, specfile.getVersion(),
				SWT.BORDER_SOLID);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.createLabel(client2, RpmTags.RELEASE);
		text = toolkit.createText(client2, specfile.getRelease(),
				SWT.BORDER_SOLID);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setClient(client2);
		toolkit.paintBordersFor(client2);
		toolkit.paintBordersFor(section);
		managedForm.refresh();
	}
}
