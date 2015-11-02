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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
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

	public ImageTagPage() {
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
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		final Label repoLabel = new Label(container, SWT.NULL);
		repoLabel.setText(WizardMessages.getString(TAG_LABEL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(repoLabel);

		tagText = new Text(container, SWT.BORDER | SWT.SINGLE);
		tagText.addModifyListener(Listener);
		tagText.setToolTipText(WizardMessages.getString(TAG_TOOLTIP));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(tagText);

		setControl(container);
		setPageComplete(false);
	}

}
