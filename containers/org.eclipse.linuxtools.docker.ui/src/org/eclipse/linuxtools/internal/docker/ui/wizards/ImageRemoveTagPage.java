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

import java.util.List;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ImageRemoveTagPage extends WizardPage {

	private final static String NAME = "ImageRemoveTag.name"; //$NON-NLS-1$
	private final static String TITLE = "ImageRemoveTag.title"; //$NON-NLS-1$
	private final static String DESC = "ImageRemoveTag.desc"; //$NON-NLS-1$
	private final static String REMOVE_TAG_LABEL = "ImageRemoveTagName.label"; //$NON-NLS-1$
	private final static String REMOVE_TAG_TOOLTIP = "ImageRemoveTagName.toolTip"; //$NON-NLS-1$

	private Combo tagCombo;
	private IDockerImage image;

	public ImageRemoveTagPage(IDockerImage image) {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
		this.image = image;
	}

	public String getTag() {
		return tagCombo.getText();
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
		repoLabel.setText(WizardMessages.getString(REMOVE_TAG_LABEL));

		tagCombo = new Combo(container, SWT.BORDER | SWT.READ_ONLY);
		tagCombo.setToolTipText(WizardMessages.getString(REMOVE_TAG_TOOLTIP));

		// Set up combo with repoTags that can be removed
		List<String> repoTags = image.repoTags();
		tagCombo.setItems(repoTags.toArray(new String[0]));
		tagCombo.select(0);

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = tagCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
		tagCombo.setLayoutData(f);

		setControl(container);
		setPageComplete(true);
	}

}
