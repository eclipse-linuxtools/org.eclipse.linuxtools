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

package org.eclipse.linuxtools.internal.docker.ui.views;


import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLink;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLinksCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerPortMappingsCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolumesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerImagesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.LoadingStub;
import org.eclipse.swt.graphics.Image;

/**
 * @author xcoulon
 *
 */
public class DockerExplorerLabelProvider implements IStyledLabelProvider, ILabelProvider {

	private static final String UNNAMED_CONNECTION = "Connection.unnamed"; //$NON-NLS-1$
	private Image OPEN_CONNECTION_IMAGE = SWTImagesFactory.DESC_REPOSITORY_MIDDLE
			.createImage();
	private Image UNOPEN_CONNECTION_IMAGE = SWTImagesFactory.DESC_REPOSITORY_MIDDLED
			.createImage();
	private Image CATEGORY_IMAGE = SWTImagesFactory.DESC_DB_GROUP.createImage();
	private Image IMAGE_IMAGE = SWTImagesFactory.DESC_IMAGE.createImage();
	private Image STARTED_CONTAINER_IMAGE = SWTImagesFactory.DESC_CONTAINER_STARTED
			.createImage();
	private Image PAUSED_CONTAINER_IMAGE = SWTImagesFactory.DESC_CONTAINER_PAUSED
			.createImage();
	private Image STOPPED_CONTAINER_IMAGE = SWTImagesFactory.DESC_CONTAINER_STOPPED
			.createImage();
	private Image CONTAINER_LINK_IMAGE = SWTImagesFactory.DESC_CONTAINER_LINK
			.createImage();
	private Image CONTAINER_VOLUME_IMAGE = SWTImagesFactory.DESC_CONTAINER_VOLUME
			.createImage();
	private Image CONTAINER_PORT_IMAGE = SWTImagesFactory.DESC_CONTAINER_PORT
			.createImage();
	private Image LOADING_IMAGE = SWTImagesFactory.DESC_SYSTEM_PROCESS
			.createImage();

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
		OPEN_CONNECTION_IMAGE.dispose();
		UNOPEN_CONNECTION_IMAGE.dispose();
		CATEGORY_IMAGE.dispose();
		IMAGE_IMAGE.dispose();
		STARTED_CONTAINER_IMAGE.dispose();
		PAUSED_CONTAINER_IMAGE.dispose();
		STOPPED_CONTAINER_IMAGE.dispose();
		CONTAINER_LINK_IMAGE.dispose();
		CONTAINER_VOLUME_IMAGE.dispose();
		CONTAINER_PORT_IMAGE.dispose();
		LOADING_IMAGE.dispose();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(final Object element) {
		if(element instanceof IDockerConnection) {
			if (((IDockerConnection) element).isOpen()) {
				return OPEN_CONNECTION_IMAGE;
			} else {
				return UNOPEN_CONNECTION_IMAGE;
			}
		} else if(element instanceof DockerImagesCategory) {
			return CATEGORY_IMAGE;
		} else if(element instanceof DockerContainersCategory) {
			return CATEGORY_IMAGE;
		} else if(element instanceof IDockerImage) {
			return IMAGE_IMAGE;
		} else if(element instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) element;
			final EnumDockerStatus containerStatus = EnumDockerStatus
					.fromStatusMessage(container.status());
			if (containerStatus == EnumDockerStatus.RUNNING) {
				return STARTED_CONTAINER_IMAGE;
			} else if (containerStatus == EnumDockerStatus.PAUSED) {
				return PAUSED_CONTAINER_IMAGE;
			} else {
				return STOPPED_CONTAINER_IMAGE;
			}
		} else if (element instanceof DockerContainerLinksCategory
				|| element instanceof DockerContainerLink) {
			return CONTAINER_LINK_IMAGE;
		} else if (element instanceof DockerContainerVolumesCategory
				|| element instanceof DockerContainerVolume) {
			return CONTAINER_VOLUME_IMAGE;
		} else if (element instanceof DockerContainerPortMappingsCategory
				|| element instanceof IDockerPortMapping) {
			return CONTAINER_PORT_IMAGE;
		} else if(element instanceof LoadingStub) {
			return LOADING_IMAGE;
		}
		return null;
	}

	@Override
	public String getText(final Object element) {
		final StyledString styledText = getStyledText(element);
		if(styledText != null) {
			return styledText.getString();
		}
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		// when the text to display is in the properties view title bar, the given element
		// is the TreeSelection from the contributing view (the Docker Explorer View)
		if(element instanceof IStructuredSelection) {
			return getStyledText(((IStructuredSelection)element).getFirstElement());
		}
		if(element instanceof IDockerConnection) {
			final IDockerConnection connection = (IDockerConnection) element;
			final String connectionName = (connection.getName() != null
					&& !connection.getName().isEmpty())
					? connection.getName()
					: DVMessages.getString(UNNAMED_CONNECTION);
			final StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append(connectionName);
			if (connection.getUri() != null && !connection.getUri().isEmpty()) {
				messageBuilder.append(" (").append(connection.getUri()) //$NON-NLS-1$
						.append(")"); //$NON-NLS-1$
			}
			final String message = messageBuilder.toString();
			final StyledString styledString = new StyledString(message);
			styledString.setStyle(connectionName.length(), message.length() - connectionName.length(), StyledString.QUALIFIER_STYLER);
			return styledString;
		} else if(element instanceof DockerImagesCategory) {
			return new StyledString(
					DVMessages.getString("DockerImagesCategory.label")); //$NON-NLS-1$
		} else if(element instanceof DockerContainersCategory) {
			return new StyledString(
					DVMessages.getString("DockerContainersCategory.label")); //$NON-NLS-1$
		} else if (element instanceof IDockerContainer) {
			final IDockerContainer dockerContainer = (IDockerContainer) element;
			final String containerName = dockerContainer.name();
			final String image = dockerContainer.image();
			final String message = containerName + " (" + image + ")";
			final StyledString styledString = new StyledString(message);
			styledString.setStyle(containerName.length(),
					message.length() - containerName.length(),
					StyledString.QUALIFIER_STYLER);
			return styledString;
		} else if (element instanceof IDockerImage) {
			return LabelProviderUtils.getStyleString((IDockerImage) element);
		} else if (element instanceof DockerContainerPortMappingsCategory) {
			return new StyledString(DVMessages
					.getString("DockerContainerPortMappingsCategory.label")); //$NON-NLS-1$
		} else if (element instanceof IDockerPortMapping) {
			final IDockerPortMapping mapping = (IDockerPortMapping) element;
			final String hostMapping = mapping.getIp() + ":"
					+ mapping.getPublicPort() + " -> ";
			final String containerMapping = Integer
					.toString(mapping.getPrivatePort());
			final String mappingType = " (" + mapping.getType() + ")";
			final StyledString styledString = new StyledString(
					hostMapping + containerMapping + mappingType);
			styledString.setStyle(
					hostMapping.length() + containerMapping.length(),
					mappingType.length(), StyledString.QUALIFIER_STYLER);
			return styledString;
		} else if (element instanceof DockerContainerVolumesCategory) {
			return new StyledString(DVMessages
					.getString("DockerContainerVolumesCategory.label")); //$NON-NLS-1$
		} else if (element instanceof DockerContainerVolume) {
			final DockerContainerVolume containerVolume = (DockerContainerVolume) element;
			final String hostPath = containerVolume.getHostPath();
			final StyledString styledString = new StyledString();
			if (containerVolume.getHostPath() != null
					&& containerVolume.getContainerPath() != null) {
				styledString.append(hostPath).append(" -> ")
						.append(containerVolume.getContainerPath());
			} else if (containerVolume.getHostPath() == null
					&& containerVolume.getContainerPath() != null) {
				styledString.append(containerVolume.getContainerPath());
			}
			if (containerVolume.getFlags() != null) {
				final int offset = styledString.length();
				styledString.append(" (" + containerVolume.getFlags() + ")");
				styledString.setStyle(offset, styledString.length() - offset,
						StyledString.QUALIFIER_STYLER);
			}
			return styledString;
		} else if (element instanceof DockerContainerLinksCategory) {
			return new StyledString(
					DVMessages.getString("DockerContainerLinksCategory.label")); //$NON-NLS-1$
		} else if (element instanceof DockerContainerLink) {
			final DockerContainerLink containerLink = (DockerContainerLink) element;
			final String containerName = containerLink.getContainerName();
			final String containerAlias = " ("
					+ containerLink.getContainerAlias() + ")";
			final StyledString styledString = new StyledString(
					containerName + containerAlias);
			styledString.setStyle(containerName.length(),
					containerAlias.length(), StyledString.QUALIFIER_STYLER);
			return styledString;
		} else if (element instanceof String) {
			return new StyledString((String) element);
		}

		else if (element instanceof LoadingStub) {
			return new StyledString(DVMessages.getString("Loading.label"));
		}
		return null;
	}

}
