/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImageTagPage extends WizardPage {

	private final static String NAME = "ImageTag.name"; //$NON-NLS-1$
	private final static String TITLE = "ImageTag.title"; //$NON-NLS-1$
	private final static String DESC = "ImageTag.desc"; //$NON-NLS-1$
	private final static String TAG_LABEL = "ImageTagName.label"; //$NON-NLS-1$
	private final static String TAG_TOOLTIP = "ImageTagName.toolTip"; //$NON-NLS-1$

	private Text tagText;

	private String tag;

	public ImageTagPage(String image) {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getTag() {
		return tag;
	}

	private ModifyListener Listener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			validate();
		}
	};

	private void validate() {
		boolean complete = true;
		boolean error = false;

		String tagField = tagText.getText().trim();

		if (tagField.length() == 0) {
			complete = false;
		}

		if (!error) {
			setErrorMessage(null);
			tag = tagField;
		}
		setPageComplete(complete && !error);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		container.setLayout(layout);

		Label label = new Label(container, SWT.NULL);

		Label repoLabel = new Label(container, SWT.NULL);
		repoLabel.setText(WizardMessages.getString(TAG_LABEL));

		tagText = new Text(container, SWT.BORDER | SWT.SINGLE);
		tagText.addModifyListener(Listener);
		tagText.setToolTipText(WizardMessages.getString(TAG_TOOLTIP));

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = tagText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering);
		f.left = new FormAttachment(0, 0);
		repoLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11);
		f.left = new FormAttachment(repoLabel, 5);
		f.right = new FormAttachment(100);
		tagText.setLayoutData(f);

		setControl(container);
		setPageComplete(false);
	}

}
