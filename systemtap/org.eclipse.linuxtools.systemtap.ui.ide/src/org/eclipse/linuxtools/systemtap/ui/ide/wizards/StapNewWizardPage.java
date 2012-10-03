/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial Wizard and related API
 *******************************************************************************/
package org.eclipse.linuxtools.systemtap.ui.ide.wizards;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class StapNewWizardPage extends WizardPage {
	private Text fileText;
	
	private Text containerText;

	private ISelection selection;
	
	private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.eclipse.linuxtools.systemtap.ui.ide.wizards.stap_strings");

	/**
	 * Constructor for StapNewWizardPage.
	 * 
	 * @param pageName
	 */
	public StapNewWizardPage(ISelection selection) {
		super(resourceBundle.getString("StapNewWizardPage.WizardPage"));
		setTitle(resourceBundle.getString("StapNewWizardPage.Title"));
		setDescription(resourceBundle.getString("StapNewWizardPage.setDescription"));
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		Label label = new Label(container, SWT.NULL);
		label.setText(resourceBundle.getString("StapNewWizardPage.ScriptName")); //$NON-NLS-1$

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		label = new Label(container, SWT.NULL); // XXX just create a new layout with different width
		
		label = new Label(container, SWT.NULL);
		label.setText(resourceBundle.getString("StapNewWizardPage.Directory")); //$NON-NLS-1$

		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText(resourceBundle.getString("StapNewWizardPage.Browse"));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			}
		}
		fileText.setText(".stp");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		try {
			dialog.open();
			String result = dialog.getFilterPath();
			containerText.setText(result);
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IPath container = Path.fromOSString(getContainerName());
		String fileName = getFileName();
		container.isValidPath(getContainerName());
		if (fileName.length() == 0 || fileName.equals(".stp")) {
			updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus1"));
			return;
		}
		if (getContainerName().length() == 0) {
			updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus2"));
			return;
		}
		if (container == null
				|| !container.isValidPath(getContainerName())) {
			updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus3"));
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus4"));
			return;
		}
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("stp") == false) {
				updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus.5"));
				return;
			}
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}
}