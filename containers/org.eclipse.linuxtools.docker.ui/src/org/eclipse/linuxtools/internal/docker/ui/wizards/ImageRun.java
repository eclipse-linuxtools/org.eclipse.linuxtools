/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig.Builder;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerPortBinding;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ContainerLinkModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ExposedPortModel;

/**
 * Wizard to 'docker run' a given {@link IDockerImage}.
 * 
 * @author xcoulon
 *
 */
public class ImageRun extends Wizard {

	private final ImageRunSelectionPage imageRunSelectionPage;
	private final ImageRunResourceVolumesVariablesPage imageRunResourceVolumesPage;

	/**
	 * Constructor when an {@link IDockerConnection} has been selected to run an
	 * {@link IDockerImage}.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} pointing to a specific Docker
	 *            daemon/host.
	 * @throws DockerException
	 */
	public ImageRun(final IDockerConnection connection) throws DockerException {
		super();
		setWindowTitle("Run a Docker Image");
		setNeedsProgressMonitor(true);
		this.imageRunSelectionPage = new ImageRunSelectionPage(connection);
		this.imageRunResourceVolumesPage = new ImageRunResourceVolumesVariablesPage(
				connection);
	}

	/**
	 * Full constructor with a selected {@link IDockerImage} to run.
	 * 
	 * @param selectedImage
	 *            the {@link IDockerImage} to use to fill the wizard pages
	 * @throws DockerException
	 */
	public ImageRun(final IDockerImage selectedImage) throws DockerException {
		setWindowTitle("Run a Docker Image");
		setNeedsProgressMonitor(true);
		this.imageRunSelectionPage = new ImageRunSelectionPage(selectedImage);
		this.imageRunResourceVolumesPage = new ImageRunResourceVolumesVariablesPage(
				selectedImage.getConnection());
	}

	@Override
	public void addPages() {
		addPage(imageRunSelectionPage);
		addPage(imageRunResourceVolumesPage);
	}

	@Override
	public IWizardPage getNextPage(final IWizardPage page) {
		if (page.equals(imageRunSelectionPage)) {
			imageRunResourceVolumesPage.getModel().setSelectedImage(
					imageRunSelectionPage.getModel().getSelectedImage());
		}
		return super.getNextPage(page);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	public String getDockerContainerName() {
		return this.imageRunSelectionPage.getModel().getContainerName();
	}

	@SuppressWarnings("unchecked")
	public IDockerHostConfig getDockerHostConfig() {
		final ImageRunSelectionModel selectionModel = this.imageRunSelectionPage
				.getModel();
		final ImageRunResourceVolumesVariablesModel resourcesModel = this.imageRunResourceVolumesPage
				.getModel();

		final DockerHostConfig.Builder hostConfigBuilder = new DockerHostConfig.Builder();
		if (selectionModel.isPublishAllPorts()) {
			hostConfigBuilder.publishAllPorts(true);
		} else {
			final Map<String, List<IDockerPortBinding>> portBindings = new HashMap<>();
			for (Iterator<ExposedPortModel> iterator = selectionModel
					.getExposedPorts().iterator(); iterator.hasNext();) {
				final ExposedPortModel exposedPort = iterator.next();
				// only selected Ports in the CheckboxTableViewer are exposed.
				if (!selectionModel.getSelectedPorts().contains(exposedPort)) {
					continue;
				}
				final DockerPortBinding portBinding = new DockerPortBinding(
						exposedPort.getHostAddress(),
						exposedPort.getHostPort());
				portBindings.put(
						exposedPort.getContainerPort()
								+ exposedPort.getPortType(),
						Arrays.<IDockerPortBinding> asList(portBinding));
			}
			hostConfigBuilder.portBindings(portBindings);
		}
		// container links
		final List<String> links = new ArrayList<>();
		for (Iterator<ContainerLinkModel> iterator = selectionModel.getLinks()
				.iterator(); iterator.hasNext();) {
			final ContainerLinkModel link = iterator.next();
			links.add(link.getContainerName() + ':' + link.getContainerAlias());
		}
		hostConfigBuilder.links(links);

		// data volumes
		final List<String> volumesFrom = new ArrayList<>();
		for (Iterator<DataVolumeModel> iterator = resourcesModel
				.getDataVolumes().iterator(); iterator.hasNext();) {
			final DataVolumeModel dataVolume = iterator.next();
			// only data volumes selected in the CheckBoxTableViewer are
			// included.
			if (!resourcesModel.getSelectedDataVolumes().contains(dataVolume)) {
				continue;
			}

			switch (dataVolume.getMountType()) {
			case CONTAINER:
				volumesFrom.add(dataVolume.getContainerMount());
				break;
			default:
				break;

			}
		}
		hostConfigBuilder.volumesFrom(volumesFrom);

		return hostConfigBuilder.build();
	}

	@SuppressWarnings("unchecked")
	public DockerContainerConfig getDockerContainerConfig() {
		final ImageRunSelectionModel selectionModel = this.imageRunSelectionPage
				.getModel();
		final ImageRunResourceVolumesVariablesModel resourcesModel = this.imageRunResourceVolumesPage
				.getModel();

		final Builder config = new DockerContainerConfig.Builder()
				.cmd(selectionModel.getCommand())
				.entryPoint(selectionModel.getEntrypoint())
				.image(selectionModel.getSelectedImageName())
				.tty(selectionModel.isAllocatePseudoTTY())
				.openStdin(selectionModel.isInteractiveMode());
		if (resourcesModel.isEnableResourceLimitations()) {
			config.memory(resourcesModel.getMemory());
			config.cpuShares((long) resourcesModel.getCpuShareWeight());
		}
		// environment variables
		final List<String> environmentVariables = new ArrayList<>();
		for (Iterator<EnvironmentVariableModel> iterator = resourcesModel
				.getEnvironmentVariables().iterator(); iterator.hasNext();) {
			final EnvironmentVariableModel var = iterator.next();
			environmentVariables.add(var.getName() + "=" + var.getValue());
		}
		config.env(environmentVariables);

		// container data volumes
		final Set<String> volumes = new HashSet<>();
		for (Iterator<DataVolumeModel> iterator = resourcesModel
				.getDataVolumes().iterator(); iterator.hasNext();) {
			final DataVolumeModel dataVolume = iterator.next();
			// only data volumes selected in the CheckBoxTableViewer are
			// included.
			if (!resourcesModel.getSelectedDataVolumes().contains(dataVolume)) {
				continue;
			}
			switch (dataVolume.getMountType()) {
			case CONTAINER:
				// different way to configure 'volumes-from'
				break;
			case HOST_FILE_SYSTEM:
				if (dataVolume.isReadOnly()) {
					volumes.add(dataVolume.getContainerPath() + ':'
							+ dataVolume.getHostPathMount() + ':' + "ro");
				} else {
					volumes.add(dataVolume.getContainerPath() + ':'
							+ dataVolume.getHostPathMount());
				}
				break;
			case NONE:
				volumes.add(dataVolume.getContainerPath());
				break;
			default:
				break;

			}
		}
		config.volumes(volumes);

		return config.build();
	}

}
