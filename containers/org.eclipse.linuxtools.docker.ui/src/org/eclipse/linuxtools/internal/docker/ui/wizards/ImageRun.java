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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
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
		setWindowTitle(WizardMessages.getString("ImageRun.title")); //$NON-NLS-1$
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
		setWindowTitle(WizardMessages.getString("ImageRun.title")); //$NON-NLS-1$
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
		final List<String> binds = new ArrayList<>();
		for (Iterator<DataVolumeModel> iterator = resourcesModel
				.getDataVolumes().iterator(); iterator.hasNext();) {
			final DataVolumeModel dataVolume = iterator.next();
			// only data volumes selected in the CheckBoxTableViewer are
			// included.
			if (!resourcesModel.getSelectedDataVolumes().contains(dataVolume)) {
				continue;
			}

			switch (dataVolume.getMountType()) {
			case HOST_FILE_SYSTEM:
				String bind = convertToUnixPath(dataVolume.getHostPathMount())
						+ ':' + dataVolume.getContainerPath();
				if (dataVolume.isReadOnly()) {
					bind += ':' + "ro";
				}
				binds.add(bind);
				break;
			case CONTAINER:
				volumesFrom.add(dataVolume.getContainerMount());
				break;
			default:
				break;

			}
		}
		hostConfigBuilder.binds(binds);
		hostConfigBuilder.volumesFrom(volumesFrom);

		return hostConfigBuilder.build();
	}

	private String convertToUnixPath(String path) {
		String unixPath = path;

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// replace backslashes with slashes
			unixPath = unixPath.replaceAll("\\\\", "/");

			// replace "C:/" with "/c/"
			Matcher m = Pattern.compile("([a-zA-Z]):/").matcher(unixPath);
			if (m.find()) {
				StringBuffer b = new StringBuffer();
				b.append('/');
				m.appendReplacement(b, m.group(1).toLowerCase());
				b.append('/');
				m.appendTail(b);
				unixPath = b.toString();
			}
		}

		return unixPath;
	}

	@SuppressWarnings("unchecked")
	public DockerContainerConfig getDockerContainerConfig() {
		final ImageRunSelectionModel selectionModel = this.imageRunSelectionPage
				.getModel();
		final ImageRunResourceVolumesVariablesModel resourcesModel = this.imageRunResourceVolumesPage
				.getModel();

		final Builder config = new DockerContainerConfig.Builder()
				.cmd(getCmdList(selectionModel.getCommand()))
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
			environmentVariables.add(var.getName() + "=" + var.getValue()); //$NON-NLS-1$
		}
		config.env(environmentVariables);
		return config.build();
	}

	// Create a proper command list after handling quotation.
	private List<String> getCmdList(String s) {
		ArrayList<String> list = new ArrayList<>();
		int length = s.length();
		boolean insideQuote1 = false; // single-quote
		boolean insideQuote2 = false; // double-quote
		boolean escaped = false;
		StringBuffer buffer = new StringBuffer();
		// Parse the string and break it up into chunks that are
		// separated by white-space or are quoted. Ignore characters
		// that have been escaped, including the escape character.
		for (int i = 0; i < length; ++i) {
			char c = s.charAt(i);
			if (escaped) {
				buffer.append(c);
				escaped = false;
			}
			switch (c) {
			case '\'':
				if (!insideQuote2)
					insideQuote1 = insideQuote1 ^ true;
				else
					buffer.append(c);
				break;
			case '\"':
				if (!insideQuote1)
					insideQuote2 = insideQuote2 ^ true;
				else
					buffer.append(c);
				break;
			case '\\':
				escaped = true;
				break;
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				if (insideQuote1 || insideQuote2)
					buffer.append(c);
				else {
					String item = buffer.toString();
					buffer.setLength(0);
					if (item.length() > 0)
						list.add(item);
				}
				break;
			default:
				buffer.append(c);
				break;
			}
		}
		// add last item of string that will be in the buffer
		String item = buffer.toString();
		if (item.length() > 0)
			list.add(item);
		return list;
	}

}
