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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ContainerCommitPage extends WizardPage {

	private final static String NAME = "ContainerCommit.name"; //$NON-NLS-1$
	private final static String TITLE = "ContainerCommit.title"; //$NON-NLS-1$
	private final static String DESC = "ContainerCommit.desc"; //$NON-NLS-1$
	private final static String COMMIT_LABEL = "ContainerCommit.label"; //$NON-NLS-1$
	private final static String NAME_LABEL = "Name.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImageName.toolTip"; //$NON-NLS-1$
	private final static String AUTHOR_LABEL = "Author.label"; //$NON-NLS-1$
	private final static String COMMENT_LABEL = "Comment.label"; //$NON-NLS-1$

	private final static String INVALID_REPO_ID = "ErrorInvalidRepo.msg"; //$NON-NLS-1$

	private String repo;
	private String tag;

	private Text nameText;
	private Text authorText;
	private Text commentText;

	public ContainerCommitPage(String container) {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getFormattedString(DESC,
				container.substring(0, 8)));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getRepo() {
		return repo;
	}

	public String getTag() {
		return tag;
	}

	public String getAuthor() {
		return authorText.getText();
	}

	public String getComment() {
		return commentText.getText();
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

		if (nameText.getText().length() == 0)
			complete = false;
		if (!error) {
			String[] tokens = nameText.getText().split(":"); //$NON-NLS-1$
			if (tokens.length == 2) {
				repo = tokens[0];
				tag = tokens[1];
				setErrorMessage(null);
			} else if (tokens.length == 1) {
				repo = tokens[0];
				tag = ""; //$NON-NLS-1$
				setErrorMessage(null);
			} else {
				setErrorMessage(WizardMessages.getString(INVALID_REPO_ID));
			}
		}
		setPageComplete(complete && !error);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		composite.setLayout(layout);

		Label label = new Label(composite, SWT.NULL);
		label.setText(WizardMessages.getString(COMMIT_LABEL));

		Label nameLabel = new Label(composite, SWT.NULL);
		nameLabel.setText(WizardMessages.getString(NAME_LABEL));

		nameText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		nameText.addModifyListener(Listener);
		nameText.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));

		Label authorLabel = new Label(composite, SWT.NULL);
		authorLabel.setText(WizardMessages.getString(AUTHOR_LABEL));

		authorText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		authorText.addModifyListener(Listener);

		Label commentLabel = new Label(composite, SWT.NULL);
		commentLabel.setText(WizardMessages.getString(COMMENT_LABEL));

		commentText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		commentText.addModifyListener(Listener);

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = nameText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		Control prevControl = label;
		Control longestLabel = commentLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		nameText.setLayoutData(f);

		prevControl = nameLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		authorLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		authorText.setLayoutData(f);

		prevControl = authorLabel;

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11 + centering);
		f.left = new FormAttachment(0, 0);
		commentLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(prevControl, 11);
		f.left = new FormAttachment(longestLabel, 5);
		f.right = new FormAttachment(100);
		commentText.setLayoutData(f);

		prevControl = commentLabel;

		setControl(composite);
		setPageComplete(false);
	}

}
