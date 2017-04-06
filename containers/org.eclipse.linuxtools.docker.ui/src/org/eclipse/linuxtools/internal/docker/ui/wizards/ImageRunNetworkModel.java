/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
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
import java.util.Collections;
import java.util.List;

import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Databinding model for the {@link ImageRunNetworkPage}
 * 
 * @author jjohnstn
 *
 */
public class ImageRunNetworkModel extends BaseDatabindingModel {

	public static final String NETWORK_MODE = "networkMode"; //$NON-NLS-1$

	public static final String DEFAULT_MODE = ""; //$NON-NLS-1$

	public static final String BRIDGE_MODE = "bridge"; //$NON-NLS-1$

	public static final String HOST_MODE = "host"; //$NON-NLS-1$

	public static final String NONE_MODE = "none"; //$NON-NLS-1$

	public static final String OTHER_MODE = "other"; //$NON-NLS-1$

	public static final String CONTAINER_MODE = "container"; //$NON-NLS-1$

	public static final String CONTAINER_NAMES = "containerNames"; //$NON-NLS-1$

	public static final String SELECTED_CONTAINER = "selectedContainer"; //$NON-NLS-1$

	public static final String OTHER_STRING = "otherString"; //$NON-NLS-1$

	private String networkMode;

	private IDockerConnection connection;

	private String selectedContainer;

	private String otherString;

	private List<String> containerNames = new ArrayList<>();

	public ImageRunNetworkModel(
			final IDockerConnection connection) {
		this.connection = connection;
		this.networkMode = DEFAULT_MODE;
		refreshContainerNames();
	}

	public ImageRunNetworkModel(final IDockerImage selectedImage) {
		this.connection = selectedImage.getConnection();
		this.networkMode = DEFAULT_MODE;
		refreshContainerNames();
	}

	public void setConnection(final IDockerConnection connection) {
		this.connection = connection;
		refreshContainerNames();
	}

	public void refreshContainerNames() {
		final List<String> refreshedContainerNames = new ArrayList<>();
		final IDockerConnection connection = this.connection;
		if (connection != null && connection.isOpen()) {
			connection.getContainers().stream()
					.filter(container -> EnumDockerStatus.fromStatusMessage(
							container.status()) == EnumDockerStatus.RUNNING)
					.forEach(container -> {
						refreshedContainerNames.add(container.name());
					});
			Collections.sort(refreshedContainerNames);
		}
		setContainerNames(refreshedContainerNames);
	}

	public String getNetworkMode() {
		return networkMode;
	}

	public void setNetworkMode(final String networkMode) {
		if (networkMode != null) {
			firePropertyChange(NETWORK_MODE, this.networkMode,
					this.networkMode = networkMode);
		}
	}

	/**
	 * Return the string to use in the network mode option of host config
	 * 
	 * @return container:selectedContainer if container mode otherwise the mode
	 *         string
	 */
	public String getNetworkModeString() {
		String mode = getNetworkMode();
		if (CONTAINER_MODE.equals(mode)) {
			return "container:" + getSelectedContainer(); //$NON-NLS-1$
		} else if (OTHER_MODE.equals(mode)) {
			return getOtherString();
		}
		return mode;
	}

	public String getSelectedContainer() {
		return selectedContainer;
	}

	public void setSelectedContainer(String selectedContainer) {
		firePropertyChange(SELECTED_CONTAINER, this.selectedContainer,
				this.selectedContainer = selectedContainer);
	}

	public String getOtherString() {
		return otherString;
	}

	public void setOtherString(String otherString) {
		firePropertyChange(OTHER_STRING, this.otherString,
				this.otherString = otherString);
	}

	public List<String> getContainerNames() {
		return containerNames;
	}

	public void setContainerNames(List<String> refreshedContainerNames) {
		firePropertyChange(CONTAINER_NAMES, this.containerNames,
				this.containerNames = refreshedContainerNames);
	}

}
