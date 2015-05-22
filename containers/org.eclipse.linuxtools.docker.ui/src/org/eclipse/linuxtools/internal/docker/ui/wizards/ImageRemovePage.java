/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.ArrayList;
import java.util.List;

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

public class ImageRemovePage extends WizardPage {

	private final static String NAME = "ImageRemove.name"; //$NON-NLS-1$
	private final static String TITLE = "ImageRemove.title"; //$NON-NLS-1$
	private final static String DESC = "ImageRemove.desc"; //$NON-NLS-1$
	private final static String NAME_LABEL = "ImageRemoveName.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImageRemoveName.toolTip"; //$NON-NLS-1$
	private final static String NAME_EMPTY_RULE = "ErrorRemoveNameEmpty.msg"; //$NON-NLS-1$
	private final static String INVALID_ID = "ErrorRemoveInvalidImageId.msg"; //$NON-NLS-1$

	private Text nameText;

	private List<String> images = new ArrayList<>();

	public ImageRemovePage() {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public List<String> getImageNames() {
		return images;
	}

	private ModifyListener Listener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			// TODO Auto-generated method stub
			validate();
		}
	};

	private void validate() {
		boolean complete = true;
		boolean error = false;

		String nameField = nameText.getText().trim();
		ArrayList<String> validNames = new ArrayList<>();

		if (nameField.length() == 0) {
			setErrorMessage(WizardMessages.getString(NAME_EMPTY_RULE));
			error = true;
		}

		String[] names = nameField.split("\\s+"); //$NON-NLS-1$
		for (String name : names) {
			if (name.length() > 0) {
				if (name.charAt(name.length() - 1) == ':') { //$NON-NLS-1$
					setErrorMessage(WizardMessages.getString(INVALID_ID));
					error = true;
				} else {
					if (name.contains(":")) { //$NON-NLS-$
						if (name.substring(name.indexOf(":") + 1).contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
							setErrorMessage(WizardMessages
									.getString(INVALID_ID));
							error = true;
						}
					}
				}

				validNames.add(name);
			}
		}

		if (!error) {
			setErrorMessage(null);
			images = validNames;
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
		repoLabel.setText(WizardMessages.getString(NAME_LABEL));

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		nameText.addModifyListener(Listener);
		nameText.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = nameText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering);
		f.left = new FormAttachment(0, 0);
		repoLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering);
		f.left = new FormAttachment(repoLabel, 5);
		f.right = new FormAttachment(100);
		nameText.setLayoutData(f);

		setControl(container);
		setPageComplete(false);
	}

}
