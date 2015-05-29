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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImageBuildPage extends WizardPage {

	private final static String NAME = "ImageBuild.name"; //$NON-NLS-1$
	private final static String TITLE = "ImageBuild.title"; //$NON-NLS-1$
	private final static String DESC = "ImageBuild.desc"; //$NON-NLS-1$
	private final static String NAME_LABEL = "ImageBuildName.label"; //$NON-NLS-1$
	private final static String NAME_TOOLTIP = "ImageBuildName.toolTip"; //$NON-NLS-1$
	private final static String DIRECTORY_LABEL = "ImageBuildDirectory.label"; //$NON-NLS-1$
	private final static String DIRECTORY_TOOLTIP = "ImageBuildDirectory.toolTip"; //$NON-NLS-1$
	private final static String BROWSE_LABEL = "BrowseButton.label"; //$NON-NLS-1$
	private final static String EDIT_LABEL = "EditButton.label"; //$NON-NLS-1$
	private final static String NONEXISTENT_DIRECTORY = "ErrorNonexistentDirectory.msg"; //$NON-NLS-1$
	private final static String INVALID_DIRECTORY = "ErrorInvalidDirectory.msg"; //$NON-NLS-1$
	private final static String INVALID_ID = "ErrorInvalidImageId.msg"; //$NON-NLS-1$
	private final static String NO_DOCKER_FILE = "ErrorNoDockerFile.msg"; //$NON-NLS-1$

	private Text nameText;
	private Text directoryText;
	private Button editButton;

	public ImageBuildPage() {
		super(WizardMessages.getString(NAME));
		setDescription(WizardMessages.getString(DESC));
		setTitle(WizardMessages.getString(TITLE));
		setImageDescriptor(SWTImagesFactory.DESC_WIZARD);
	}

	public String getImageName() {
		return nameText.getText();
	}

	public String getDirectory() {
		return directoryText.getText();
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

		String name = nameText.getText();

		if (name.length() > 0 && name.charAt(name.length() - 1) == ':') { //$NON-NLS-1$
			//				&& (tag.length() > 0) || tag.contains(":")) { //$NON-NLS-1$
			setErrorMessage(WizardMessages.getString(INVALID_ID));
			error = true;
		} else {
			if (name.contains(":")) { //$NON-NLS-$
				if (name.substring(name.indexOf(":") + 1).contains(":")) { //$NON-NLS-1$ //$NON-NLS-2$
					setErrorMessage(WizardMessages.getString(INVALID_ID));
					error = true;
				}
			}
		}

		if (!error) {
			String dir = directoryText.getText();
			if (dir.length() == 0) {
				editButton.setEnabled(false);
				complete = false;
			} else {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(
						new Path(dir));
				IFileInfo info = fileStore.fetchInfo();
				if (!info.exists()) {
					error = true;
					setErrorMessage(WizardMessages
							.getString(NONEXISTENT_DIRECTORY));
				} else if (!info.isDirectory()) {
					error = true;
					setErrorMessage(WizardMessages.getString(INVALID_DIRECTORY));
				} else {
					editButton.setEnabled(true);
					IFileStore dockerStore = fileStore.getChild("Dockerfile"); //$NON-NLS-1$
					if (!dockerStore.fetchInfo().exists()) {
						complete = false;
						setMessage(WizardMessages.getString(NO_DOCKER_FILE),
								IMessageProvider.INFORMATION);
					} else {
						setMessage(null, IMessageProvider.INFORMATION);
					}

				}
			}
		}

		if (!error) {
			setErrorMessage(null);
		} else {
			editButton.setEnabled(false);
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

		nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
		nameText.addModifyListener(Listener);
		nameText.setToolTipText(WizardMessages.getString(NAME_TOOLTIP));

		Label dirLabel = new Label(container, SWT.NULL);
		dirLabel.setText(WizardMessages.getString(DIRECTORY_LABEL));

		directoryText = new Text(container, SWT.BORDER | SWT.SINGLE);
		directoryText.addModifyListener(Listener);
		directoryText.setToolTipText(WizardMessages
				.getString(DIRECTORY_TOOLTIP));

		Button browse = new Button(container, SWT.NULL);
		browse.setText(WizardMessages.getString(BROWSE_LABEL));
		browse.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog d = new DirectoryDialog(container.getShell());
				String k = d.open();
				if (k != null)
					directoryText.setText(k);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore for now
			}

		});

		editButton = new Button(container, SWT.NULL);
		editButton.setText(WizardMessages.getString(EDIT_LABEL));
		editButton.setEnabled(false);
		editButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String dir = directoryText.getText();
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(
						new Path(dir).append("Dockerfile")); //$NON-NLS-1$
				DockerfileEditDialog d = new DockerfileEditDialog(
						container.getShell(),
						fileStore);
				d.open();
				validate();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore for now
			}

		});

		Point p1 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p2 = directoryText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point p3 = browse.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int centering = (p2.y - p1.y + 1) / 2;
		int centering2 = (p3.y - p2.y + 1) / 2;

		FormData f = new FormData();
		f.top = new FormAttachment(0);
		label.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering + centering2);
		f.left = new FormAttachment(0, 0);
		nameLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(label, 11 + centering2);
		f.left = new FormAttachment(dirLabel, 5);
		f.right = new FormAttachment(browse, -10);
		nameText.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(nameLabel, 11 + centering + centering2);
		f.left = new FormAttachment(0, 0);
		dirLabel.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(nameLabel, 11);
		f.right = new FormAttachment(100);
		editButton.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(nameLabel, 11);
		f.right = new FormAttachment(editButton, -10);
		browse.setLayoutData(f);

		f = new FormData();
		f.top = new FormAttachment(nameLabel, 11 + centering2);
		f.left = new FormAttachment(dirLabel, 5);
		f.right = new FormAttachment(browse, -10);
		directoryText.setLayoutData(f);

		setControl(container);
		setPageComplete(false);
	}
}
