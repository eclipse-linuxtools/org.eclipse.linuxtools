/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui.preferences;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.vagrant.ui.Activator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class VagrantPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * The {@link DirectoryFieldEditor} to select the installation directory for
	 * the Docker-Machine command.
	 */
	private DirectoryFieldEditor vagrantPath;

	public VagrantPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(Messages.getString("VagrantPreferencePage.preference.title")); //$NON-NLS-1$
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		this.vagrantPath = new CustomDirectoryFieldEditor(
				PreferenceInitializer.VAGRANT_PATH,
				Messages.getString("VagrantPreferencePage.tool.path.label"), //$NON-NLS-1$
				getFieldEditorParent());
		addField(this.vagrantPath);
		this.vagrantPath.setEmptyStringAllowed(true);
		this.vagrantPath.load();
	}

	/**
	 * Subclass of the {@link DirectoryFieldEditor} but with the
	 * {@link StringFieldEditor#VALIDATE_ON_KEY_STROKE} validation strategy.
	 */
	private static class CustomDirectoryFieldEditor extends DirectoryFieldEditor {

		public CustomDirectoryFieldEditor(String name, String labelText, Composite parent) {
			init(name, labelText);
	        setErrorMessage(JFaceResources.getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
	        setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
			setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
	        createControl(parent);
		}
	}
}
