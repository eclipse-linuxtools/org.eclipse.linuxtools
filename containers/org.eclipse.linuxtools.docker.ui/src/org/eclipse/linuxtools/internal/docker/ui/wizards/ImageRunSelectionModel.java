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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.runtime.Assert;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.ui.databinding.BaseDatabindingModel;

/**
 * Databinding model for the {@link ImageRunSelectionPage}
 * 
 * @author xcoulon
 *
 */
public class ImageRunSelectionModel extends BaseDatabindingModel {

	public static final String CONNECTION_NAMES = "connectionNames"; //$NON-NLS-1$

	public static final String SELECTED_CONNECTION_NAME = "selectedConnectionName"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE_NAME = "selectedImageName"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE = "selectedImage"; //$NON-NLS-1$

	public static final String SELECTED_IMAGE_NEEDS_PULLING = "selectedImageNeedsPulling"; //$NON-NLS-1$

	public static final String IMAGE_NAMES = "imageNames"; //$NON-NLS-1$

	public static final String CONTAINER_NAME = "containerName"; //$NON-NLS-1$

	public static final String COMMAND = "command"; //$NON-NLS-1$

	public static final String ENTRYPOINT = "entrypoint"; //$NON-NLS-1$

	public static final String PUBLISH_ALL_PORTS = "publishAllPorts"; //$NON-NLS-1$

	public static final String EXPOSED_PORTS = "exposedPorts"; //$NON-NLS-1$

	public static final String SELECTED_PORTS = "selectedPorts"; //$NON-NLS-1$

	public static final String LINKS = "links"; //$NON-NLS-1$

	public static final String INTERACTIVE_MODE = "interactiveMode"; //$NON-NLS-1$

	public static final String ALLOCATE_PSEUDO_TTY = "allocatePseudoTTY"; //$NON-NLS-1$

	public static final String REMOVE_WHEN_EXITS = "removeWhenExits"; //$NON-NLS-1$

	private String selectedConnectionName;

	private List<String> connectionNames;

	private Map<String, IDockerConnection> connections;

	private String selectedImageName;

	private boolean selectedImageNeedsPulling = false;

	private List<String> imageNames;

	private Map<String, IDockerImage> images;

	private String containerName;

	private String command;

	private String entrypoint;

	private boolean publishAllPorts = true;

	private final WritableList exposedPorts = new WritableList();

	private Set<ExposedPortModel> selectedPorts;

	private final WritableList links = new WritableList();

	private boolean interactiveMode = false;

	private boolean allocatePseudoTTY = false;

	private boolean removeWhenExits = false;

	public ImageRunSelectionModel(
			final IDockerConnection selectedConnection) {
		refreshConnectionNames();
		setSelectedConnectionName(selectedConnection.getName());
		refreshImageNames();
	}

	public void refreshConnectionNames() {
		this.connectionNames = new ArrayList<>();
		this.connections = new HashMap<>();
		for (IDockerConnection connection : DockerConnectionManager
				.getInstance().getConnections()) {
			String name = connection.getName();
			connections.put(name, connection);
			connectionNames.add(name);
		}
	}

	public void refreshImageNames() {
		this.imageNames = new ArrayList<>();
		this.images = new HashMap<>();
		if (getSelectedConnection() != null) {
			for (IDockerImage image : getSelectedConnection().getImages()) {
				if (!image.isIntermediateImage() && !image.isDangling()) {
					for (String tag : image.tags()) {
						final String imageName = ImageRunSelectionModel
								.getImageName(image.repo(), tag);
						images.put(imageName, image);
						imageNames.add(imageName);
					}
				}
			}
		}
	}

	public ImageRunSelectionModel(final IDockerImage selectedImage) {
		this(selectedImage.getConnection());
		if (selectedImage.tags().contains("latest")) { //$NON-NLS-1$
			setSelectedImageName(ImageRunSelectionModel
					.getImageName(selectedImage.repo(), "latest")); //$NON-NLS-1$
		} else {
			final String lastTag = selectedImage.tags()
					.get(selectedImage.tags().size() - 1);
			setSelectedImageName(ImageRunSelectionModel
					.getImageName(selectedImage.repo(), lastTag)); // $NON-NLS-1$
		}
	}

	public boolean isPublishAllPorts() {
		return publishAllPorts;
	}

	public void setPublishAllPorts(boolean publishAllPorts) {
		firePropertyChange(PUBLISH_ALL_PORTS, this.publishAllPorts,
				this.publishAllPorts = publishAllPorts);
	}

	public List<String> getConnectionNames() {
		return connectionNames;
	}

	public void setConnectionNames(final List<String> connectionNames) {
		firePropertyChange(CONNECTION_NAMES, this.connectionNames,
				this.connectionNames = connectionNames);
	}

	public String getSelectedConnectionName() {
		return selectedConnectionName;
	}

	public void setSelectedConnectionName(final String selectedConnectionName) {
		firePropertyChange(SELECTED_CONNECTION_NAME,
				this.selectedConnectionName,
				this.selectedConnectionName = selectedConnectionName);
		refreshImageNames();
	}

	public IDockerConnection getSelectedConnection() {
		return this.connections.get(selectedConnectionName);
	}

	public List<String> getImageNames() {
		return imageNames;
	}

	public void setImageNames(final List<String> imageNames) {
		firePropertyChange(IMAGE_NAMES, this.imageNames,
				this.imageNames = imageNames);
	}

	public String getSelectedImageName() {
		return selectedImageName;
	}

	public boolean isSelectedImageNeedsPulling() {
		return selectedImageNeedsPulling;
	}

	public void setSelectedImageNeedsPulling(
			final boolean selectedImageNeedsPulling) {
		firePropertyChange(SELECTED_IMAGE_NEEDS_PULLING,
				this.selectedImageNeedsPulling,
				this.selectedImageNeedsPulling = selectedImageNeedsPulling);
	}

	public void setSelectedImageName(final String selectedImageName) {
		firePropertyChange(SELECTED_IMAGE_NAME, this.selectedImageName,
				this.selectedImageName = selectedImageName);
	}

	/**
	 * @return the selected {@link IDockerImage} or <code>null</code> if none
	 *         was found.
	 */
	public IDockerImage getSelectedImage() {
		return this.images.get(selectedImageName);
	}

	public String getContainerName() {
		return containerName;
	}

	public void setContainerName(final String containerName) {
		firePropertyChange(CONTAINER_NAME, this.containerName,
				this.containerName = containerName);
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(final String command) {
		firePropertyChange(COMMAND, this.command, this.command = command);
	}

	public void setCommand(final List<String> cmdElements) {
		final StringBuilder commandBuilder = new StringBuilder();
		if (cmdElements != null) {
			for (String cmdElement : cmdElements) {
				commandBuilder.append(cmdElement).append(' ');
			}
		}
		setCommand(commandBuilder.toString());
	}

	public String getEntrypoint() {
		return entrypoint;
	}

	public void setEntrypoint(final String entrypoint) {
		firePropertyChange(ENTRYPOINT, this.entrypoint,
				this.entrypoint = entrypoint);
	}

	public void setEntrypoint(final List<String> entrypointElements) {
		final StringBuilder entrypointBuilder = new StringBuilder();
		if (entrypointElements != null) {
			for (String entrypoint : entrypointElements) {
				entrypointBuilder.append(entrypoint).append(' ');
			}
		}
		setEntrypoint(entrypointBuilder.toString());
	}

	public WritableList getExposedPorts() {
		return exposedPorts;
	}

	public void addAvailablePort(final ExposedPortModel port) {
		this.exposedPorts.add(port);
	}

	public void removeAvailablePort(final ExposedPortModel port) {
		this.exposedPorts.remove(port);
	}

	public void setExposedPorts(final List<ExposedPortModel> exposedPorts) {
		this.exposedPorts.clear();
		this.exposedPorts.addAll(exposedPorts);
		// FIXME: also add all given exposedPorts to selectedExposedPorts ?
	}

	public void addExposedPort(final ExposedPortModel exposedPort) {
		if (!this.exposedPorts.contains(exposedPort)) {
			this.exposedPorts.add(exposedPort);
		}
	}

	public void removeExposedPort(final ExposedPortModel exposedPort) {
		this.exposedPorts.remove(exposedPort);
	}

	public void removeExposedPorts() {
		this.exposedPorts.clear();
	}

	public Set<ExposedPortModel> getSelectedPorts() {
		return this.selectedPorts;
	}

	public void setSelectedPorts(final Set<ExposedPortModel> ports) {
		firePropertyChange(SELECTED_PORTS, this.selectedPorts,
				this.selectedPorts = ports);
	}

	public WritableList getLinks() {
		return links;
	}

	public void addLink(final String containerName,
			final String containerAlias) {
		addLink(new ContainerLinkModel(containerName, containerAlias));
	}

	public void addLink(final ContainerLinkModel containerLink) {
		links.add(containerLink);
	}

	public void addLink(final int index, final String containerName,
			final String containerAlias) {
		links.add(index, new ContainerLinkModel(containerName, containerAlias));
	}

	public void removeLink(final ContainerLinkModel link) {
		links.remove(link);
	}

	public void removeLinks() {
		this.links.clear();
	}

	public void setLinks(final WritableList links) {
		this.links.clear();
		this.links.addAll(links);
	}

	/**
	 * Set the container links
	 * 
	 * @param links
	 *            in the following format:
	 *            <code>&lt;containerName&gt;:&lt;containerAlias&gt;</code>
	 */
	public void setLinks(final List<String> links) {
		for (String link : links) {
			final String[] items = link.split(":");
			if (items.length == 2) {
				addLink(items[0], items[1]);
			}
		}
	}

	public static String getImageName(final String repo, final String tag) {
		return repo + ":" + tag;
	}

	public boolean isAllocatePseudoTTY() {
		return allocatePseudoTTY;
	}

	public void setAllocatePseudoTTY(boolean allocatePseudoTTY) {
		firePropertyChange(ALLOCATE_PSEUDO_TTY, this.allocatePseudoTTY,
				this.allocatePseudoTTY = allocatePseudoTTY);
	}

	public boolean isInteractiveMode() {
		return interactiveMode;
	}

	public void setInteractiveMode(boolean interactiveMode) {
		firePropertyChange(INTERACTIVE_MODE, this.interactiveMode,
				this.interactiveMode = interactiveMode);
	}

	public boolean isRemoveWhenExits() {
		return removeWhenExits;
	}

	public void setRemoveWhenExits(boolean removeWhenExits) {
		firePropertyChange(REMOVE_WHEN_EXITS, this.removeWhenExits,
				this.removeWhenExits = removeWhenExits);
	}

	public static class ExposedPortModel extends BaseDatabindingModel
			implements Comparable<ExposedPortModel> {

		private static final String SEPARATOR = ":"; //$NON-NLS-1$

		private static final String CONTAINER_TYPE_SEPARATOR = "/"; //$NON-NLS-1$

		public static final String SELECTED = "selected"; //$NON-NLS-1$

		public static final String CONTAINER_PORT = "containerPort"; //$NON-NLS-1$

		public static final String PORT_TYPE = "portType"; //$NON-NLS-1$

		public static final String HOST_ADDRESS = "hostAddress"; //$NON-NLS-1$

		public static final String HOST_PORT = "hostPort"; //$NON-NLS-1$

		private final String id = UUID.randomUUID().toString();

		private boolean selected;

		private String containerPort;

		private String portType;

		private String hostAddress;

		private String hostPort;

		/**
		 * Parses and converts the {@link List} of the given {@link String}
		 * values into a {@link List} of {@link ExposedPortModel}
		 * 
		 * @param exposedPortInfos
		 *            the input values
		 * @return the corresponding {@link ExposedPortModel}s
		 */
		public static List<ExposedPortModel> fromStrings(
				final Collection<String> exposedPortInfos) {
			final List<ExposedPortModel> exposedPorts = new ArrayList<>();
			for (String exposedPortInfo : exposedPortInfos) {
				final ExposedPortModel exposedPort = ExposedPortModel
						.fromString(exposedPortInfo);
				if (exposedPort != null) {
					exposedPorts.add(exposedPort);
				}
			}
			return exposedPorts;
		}

		/**
		 * Parse the given value and returns an instance of
		 * {@link ExposedPortModel}.
		 * 
		 * @param exposedPortInfo
		 *            the value to parse
		 * @return the corresponding {@link ExposedPortModel}
		 */
		public static ExposedPortModel fromString(
				final String exposedPortInfo) {
			final String privatePort = exposedPortInfo.substring(0,
					exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR));
			// exposed ports without host IP/port info
			final int firstColumnSeparator = exposedPortInfo.indexOf(SEPARATOR);
			if (firstColumnSeparator == -1 && exposedPortInfo
					.indexOf(CONTAINER_TYPE_SEPARATOR) != -1) {
				final String type = exposedPortInfo.substring(
						exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR)); // $NON-NLS-1$
				final ExposedPortModel exposedPort = new ExposedPortModel(
						privatePort, type, "", privatePort); // $NON-NLS-1$
				return exposedPort; // $NON-NLS-1$
			} else {
				final int secondColumnSeparator = exposedPortInfo
						.indexOf(SEPARATOR,
						firstColumnSeparator + 1);
				final String type = exposedPortInfo.substring(
						exposedPortInfo.indexOf(CONTAINER_TYPE_SEPARATOR), // $NON-NLS-1$
						firstColumnSeparator); // $NON-NLS-1$
				final String hostIP = exposedPortInfo.substring(
						firstColumnSeparator + 1, secondColumnSeparator);
				final String hostPort = exposedPortInfo
						.substring(secondColumnSeparator + 1);
				final ExposedPortModel exposedPort = new ExposedPortModel(
						privatePort, type, hostIP, hostPort); // $NON-NLS-1$
				return exposedPort; // $NON-NLS-1$
			}
		}

		/**
		 * Full constructor
		 * 
		 * @param privatePort
		 * @param portType
		 * @param hostAddress
		 * @param hostPort
		 */
		public ExposedPortModel(final String privatePort, final String type,
				final String hostAddress, final String hostPort) {
			Assert.isNotNull(privatePort,
					"Port Mapping privatePort cannot be null"); //$NON-NLS-1$
			Assert.isNotNull(type, "Port Mapping portType cannot be null"); //$NON-NLS-1$
			this.containerPort = privatePort;
			this.hostPort = hostPort;
			this.portType = type;
			this.hostAddress = hostAddress;
		}

		/**
		 * Create an ExposedPortModel from its toString output
		 * 
		 * @param stringValue
		 * @return ExposedPortModel
		 */
		static public ExposedPortModel createPortModel(String stringValue) {
			final String[] elements = stringValue.split(SEPARATOR);
			final String[] containerPortElements = elements[0]
					.split(CONTAINER_TYPE_SEPARATOR);
			ExposedPortModel model = new ExposedPortModel(
					containerPortElements[0], containerPortElements[1],
					elements[1], elements[2]);
			// check the last argument if exists otherwise assume 'true'
			model.selected = (elements.length == 4)
					? Boolean.valueOf(elements[3]) : true;
			return model;
		}

		public String getContainerPort() {
			return containerPort;
		}

		public void setContainerPort(final String containerPort) {
			firePropertyChange(CONTAINER_PORT, this.containerPort,
					this.containerPort = containerPort);
		}

		public String getPortType() {
			return portType;
		}

		public void setPortType(final String type) {
			firePropertyChange(PORT_TYPE, this.portType, this.portType = type);
		}

		public boolean getSelected() {
			return selected;
		}

		public void setSelected(final boolean selected) {
			firePropertyChange(SELECTED, this.selected,
					this.selected = selected);
		}

		public String getHostPort() {
			return hostPort;
		}

		public void setHostPort(final String hostPort) {
			firePropertyChange(HOST_PORT, this.hostPort,
					this.hostPort = hostPort);
		}

		public String getHostAddress() {
			return hostAddress;
		}

		public void setHostAddress(final String hostAddress) {
			firePropertyChange(HOST_ADDRESS, this.hostAddress,
					this.hostAddress = hostAddress);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExposedPortModel other = (ExposedPortModel) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}

		@Override
		public int compareTo(final ExposedPortModel other) {
			return this.containerPort.compareTo(other.containerPort);
		}

		// FIXME we should have a dedicated method to serialize the bean
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(containerPort + CONTAINER_TYPE_SEPARATOR + portType
					+ SEPARATOR + hostAddress + SEPARATOR + hostPort
					+ SEPARATOR + selected);
			return buffer.toString();
		}

	}

	public static class ContainerLinkModel extends BaseDatabindingModel {

		private static final String CONTAINER_SEPARATOR = ":"; //$NON-NLS-1$

		public static final String CONTAINER_NAME = "containerName"; //$NON-NLS-1$

		public static final String CONTAINER_ALIAS = "containerAlias"; //$NON-NLS-1$

		private String containerName;

		private String containerAlias;

		/**
		 * Default constructor
		 */
		public ContainerLinkModel() {
		}

		public ContainerLinkModel(final String containerName,
				final String alias) {
			this.containerName = containerName;
			this.containerAlias = alias;
		}

		public static ContainerLinkModel createContainerLinkModel(
				final String fromString) {
			String[] s = fromString.split(CONTAINER_SEPARATOR);
			return new ContainerLinkModel(s[0], s[1]);
		}

		public String getContainerName() {
			return containerName;
		}

		public void setContainerName(String containerName) {
			firePropertyChange(CONTAINER_NAME, this.containerName,
					this.containerName = containerName);
		}

		public String getContainerAlias() {
			return containerAlias;
		}

		public void setContainerAlias(String alias) {
			firePropertyChange(CONTAINER_ALIAS, this.containerAlias, this.containerAlias = alias);
		}

		@Override
		public String toString() {
			return containerName + CONTAINER_SEPARATOR + containerAlias;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((containerAlias == null) ? 0 : containerAlias.hashCode());
			result = prime * result
					+ ((containerName == null) ? 0 : containerName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ContainerLinkModel other = (ContainerLinkModel) obj;
			if (containerAlias == null) {
				if (other.containerAlias != null)
					return false;
			} else if (!containerAlias.equals(other.containerAlias))
				return false;
			if (containerName == null) {
				if (other.containerName != null)
					return false;
			} else if (!containerName.equals(other.containerName))
				return false;
			return true;
		}
	}

}
