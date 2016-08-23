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
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerCompose;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * The {@link IWorkbenchPreferencePage} for the Docker Compose settings.
 */
public class DockerComposePreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * The {@link DirectoryFieldEditor} to select the installation directory for
	 * the {@code docker-compose} command.
	 */
	private DirectoryFieldEditor dockerComposeInstallDir;

	/**
	 * Constructor.
	 */
	public DockerComposePreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(PreferenceMessages.getString("DockerCompose.message")); //$NON-NLS-1$
	}

	@Override
	public void init(final IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		// installation directory for docker-compose
		this.dockerComposeInstallDir = new CustomDirectoryFieldEditor(
				PreferenceConstants.DOCKER_COMPOSE_INSTALLATION_DIRECTORY,
				PreferenceMessages.getString("DockerComposePath.label"), //$NON-NLS-1$
				getFieldEditorParent()) {
			@Override
			protected boolean checkState() {
				if (isEmptyStringAllowed()
						&& !this.getStringValue().isEmpty()) {
					final boolean validPath = super.checkState();
					if (!validPath) {
						return false;
					}
					if (!DockerCompose.getInstance()
							.checkPathToDockerCompose(this.getStringValue())) {
						setWarningMessage(NLS.bind(
								org.eclipse.linuxtools.docker.core.Messages.Docker_Compose_Command_Not_Found,
								this.getStringValue()));
						return true;
					}
				}
				setMessage("");
				return true;
			}
		};
		addField(this.dockerComposeInstallDir);
		this.dockerComposeInstallDir.setPreferenceStore(getPreferenceStore());
		// allow empty value if docker-machine is not installed
		this.dockerComposeInstallDir.setEmptyStringAllowed(true);
		this.dockerComposeInstallDir
				.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		this.dockerComposeInstallDir.setPage(this);
		this.dockerComposeInstallDir.setErrorMessage(
				PreferenceMessages
						.getString("DockerComposePath.invalid.label")); //$NON-NLS-1$
		this.dockerComposeInstallDir.showErrorMessage();
		this.dockerComposeInstallDir.load();
	}

	private void setWarningMessage(final String message) {
		super.setMessage(message, IMessageProvider.WARNING);
	}

	/**
	 * Subclass of the {@link DirectoryFieldEditor} but with the
	 * {@link StringFieldEditor#VALIDATE_ON_KEY_STROKE} validation strategy.
	 */
	private static class CustomDirectoryFieldEditor
			extends DirectoryFieldEditor {

		public CustomDirectoryFieldEditor(String name, String labelText,
				Composite parent) {
			init(name, labelText);
			setErrorMessage(JFaceResources
					.getString("DirectoryFieldEditor.errorMessage"));//$NON-NLS-1$
			setChangeButtonText(JFaceResources.getString("openBrowse"));//$NON-NLS-1$
			setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
			createControl(parent);
		}
		
		

	}
}