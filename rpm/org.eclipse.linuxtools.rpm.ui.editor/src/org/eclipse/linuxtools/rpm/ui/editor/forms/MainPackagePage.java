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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.ui.editor.RpmTags;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

	public MainPackagePage(SpecfileFormEditor editor, IDocument document) {
		super(editor, "Overview", "Overview");
		parser = new SpecfileParser();
		specfile = parser.parse(document);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
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

		Label label = toolkit.createLabel(client2, "Name:", SWT.SINGLE);
		final Text nameText = toolkit.createText(client2, specfile.getName(),
				SWT.BORDER_SOLID);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				Text text = (Text) e.widget;
				int lineNumber = specfile.getDefine(RpmTags.NAME.toLowerCase())
						.getLineNumber();
				replaceTagValue(specfile.getDocument(), lineNumber,
						RpmTags.NAME.toLowerCase(), text.getText());
			}
		});
		label = toolkit.createLabel(client2, "Version:");
		Text text = toolkit.createText(client2, specfile.getVersion(),
				SWT.BORDER_SOLID);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label = toolkit.createLabel(client2, "Release:");
		text = toolkit.createText(client2, specfile.getRelease(),
				SWT.BORDER_SOLID);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label = toolkit.createLabel(client2, "Summary:");
		text = toolkit.createText(client2, specfile.getDefine(
				RpmTags.SUMMARY.toLowerCase()).getStringValue(),
				SWT.BORDER_SOLID);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		section.setClient(client2);

		toolkit.paintBordersFor(client2);

		toolkit.paintBordersFor(section);

	}

	private void replaceTagValue(IDocument document, int lineNumber,
			String tagName, String newValue) {
		try {
			document.replace(document.getLineOffset(lineNumber)
					+ tagName.length() + 2, newValue.length(), newValue);
			specfile = parser.parse(document); 
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
