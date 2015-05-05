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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DockerfileEditDialog extends Dialog {

	private final static String SAVE_LABEL = "SaveButton.label"; //$NON-NLS-1$
	private final static String CANCEL_LABEL = "CancelButton.label"; //$NON-NLS-1$

	private Composite dialogArea;
	private Text textArea;

	private IFileStore dockerFile;

	protected DockerfileEditDialog(Shell parentShell, IFileStore dockerFile) {
		super(parentShell);
		this.dockerFile = dockerFile;
		setShellStyle(getShellStyle() | SWT.MAX | SWT.RESIZE);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		String fileName = dockerFile.toString();
		newShell.setText(shortenText(fileName, newShell));
	}

	@Override
	protected Point getInitialSize() {
		return new Point(500, 375);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// TODO Auto-generated method stub
		createButton(parent, IDialogConstants.OK_ID,
				WizardMessages.getString(SAVE_LABEL), true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				WizardMessages.getString(CANCEL_LABEL), false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		dialogArea = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		dialogArea.setLayout(gridLayout);

		textArea = new Text(dialogArea, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.LEFT);
		textArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		StringBuffer b = null;
		if (dockerFile.fetchInfo().exists()) {
			try {
				b = getFileContents();
			} catch (IOException e) {
				Activator.log(e);
			}
			if (b != null)
				textArea.setText(b.toString());
		}

		return dialogArea;
	}

	private StringBuffer getFileContents() throws IOException {
		InputStream is = null;
		StringBuffer b = new StringBuffer();
		try {
			is = new BufferedInputStream(dockerFile.openInputStream(EFS.NONE,
					null));
			byte[] c = new byte[1024];
			while (is.read(c) != -1) {
				b.append(new String(c));
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				is.close();
		}
		return b;
	}

	private void writeFileContents() throws IOException {
		try {
			BufferedOutputStream output = new BufferedOutputStream(
					dockerFile.openOutputStream(EFS.NONE, null));
			byte[] bytes = textArea.getText().getBytes();
			if (bytes.length > 0)
				output.write(bytes);
			output.close();
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void okPressed() {
		try {
			writeFileContents();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.okPressed();
	}
}
