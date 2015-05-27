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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImagePushPage extends WizardPage {

	private final static String NAME = "ImagePush.name"; //$NON-NLS-1$
	private final static String TITLE = "ImagePush.title"; //$NON-NLS-1$
	private final static String DESC = "ImagePush.desc"; //$NON-NLS-1$
	private final static String NAME_LABEL = "ImagePushName.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImagePushName.toolTip"; //$NON-NLS-1$
	private Text nameText;
	private Combo nameCombo;
	private IDockerImage image;

	private String tag;

	public ImagePushPage() {
		this(null);
	}

	public ImagePushPage(IDockerImage image) {
		super(WizardMessages.getString(NAME));
		this.image = image;
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getImageTag() {
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

		String name = null;

		if (nameText != null) {
			name = nameText.getText();
		} else {
			name = nameCombo.getText();
		}

		if (name.length() == 0) {
			complete = false;
		}

		if (!error) {
			setErrorMessage(null);
			tag = name;
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

		Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(WizardMessages.getString(NAME_LABEL));

		// If we are given an image, use its tags for choices in a combo box,
		// otherwise,
		// allow the user to enter an existing tag.
		Control c = null;
		if (image == null || image.repoTags().size() == 0) {
			nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
			nameText.addModifyListener(Listener);
			nameText.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));
			c = nameText;
		} else {
			nameCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
			nameCombo.addModifyListener(Listener);
			nameCombo.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));
			List<String> repoTags = image.repoTags();
			nameCombo.setItems(repoTags.toArray(new String[0]));
			nameCombo.setText(repoTags.get(0));
			c = nameCombo;
		}

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2;
		if (nameText != null)
			p2 = nameText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		else
			p2 = nameCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering);
		f.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering);
		f.left = new FormAttachment(nameLabel, 5);
		f.right = new FormAttachment(100);
		c.setLayoutData(f);

		setControl(container);
		setPageComplete(nameText == null);
	}

}
