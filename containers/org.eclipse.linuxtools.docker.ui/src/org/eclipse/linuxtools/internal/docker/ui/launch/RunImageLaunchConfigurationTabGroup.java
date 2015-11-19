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
package org.eclipse.linuxtools.internal.docker.ui.launch;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel;

public class RunImageLaunchConfigurationTabGroup
		extends AbstractLaunchConfigurationTabGroup {

	private ImageRunSelectionModel runSelectionModel;
	private ImageRunResourceVolumesVariablesModel runVolumesModel;

	public RunImageLaunchConfigurationTabGroup() {
	}

	public ImageRunSelectionModel getRunSelectionModel() {
		return runSelectionModel;
	}

	public ImageRunResourceVolumesVariablesModel getRunVolumesModel() {
		return runVolumesModel;
	}

	@Override
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		runSelectionModel = null;
		runVolumesModel = null;
		if (connections != null && connections.length > 0) {
			runSelectionModel = new ImageRunSelectionModel(connections[0]);
			try {
				runVolumesModel = new ImageRunResourceVolumesVariablesModel(
						connections[0]);
			} catch (DockerException e) {
				// do nothing
			}
		}
		setTabs(new AbstractLaunchConfigurationTab[] {
				new RunImageMainTab(runSelectionModel, runVolumesModel),
				new RunImageVolumesTab(runVolumesModel),
				new RunImagePortsTab(runSelectionModel),
				new RunImageLinksTab(runSelectionModel),
				new RunImageEnvironmentTab(runVolumesModel),
				new RunImageResourcesTab(runVolumesModel) });
	}

}
