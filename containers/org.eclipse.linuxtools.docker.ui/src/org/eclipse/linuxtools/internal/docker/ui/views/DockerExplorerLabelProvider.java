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


import java.util.Iterator;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerImagesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.LoadingStub;
import org.eclipse.swt.graphics.Image;

/**
 * @author xcoulon
 *
 */
public class DockerExplorerLabelProvider implements IStyledLabelProvider, ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
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
			return Activator.getImageDescriptor("icons/repository-middle.gif").createImage();
		} else if(element instanceof DockerImagesCategory) {
			return Activator.getImageDescriptor("icons/dbgroup_obj.gif").createImage();
		} else if(element instanceof DockerContainersCategory) {
			return Activator.getImageDescriptor("icons/dbgroup_obj.gif").createImage();
		} else if(element instanceof IDockerImage) {
			return Activator.getImageDescriptor("icons/image.png").createImage();
		} else if(element instanceof IDockerContainer) {
			return Activator.getImageDescriptor("icons/container.png").createImage();
		} else if(element instanceof LoadingStub) {
			return Activator.getImageDescriptor("icons/systemprocess.gif").createImage();
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
			final String message = connection.getName() + " (" + connection.getUri() + ")";
			final StyledString styledString = new StyledString(message);
			styledString.setStyle(connection.getName().length(), message.length() - connection.getName().length(), StyledString.QUALIFIER_STYLER);
			return styledString;
		} else if(element instanceof DockerImagesCategory) {
			return new StyledString("Images");
		} else if(element instanceof DockerContainersCategory) {
			return new StyledString("Containers");
		} else {
			if(element instanceof IDockerContainer) {
				final IDockerContainer dockerContainer = (IDockerContainer)element;
				final String containerName = dockerContainer.name();
				final String image = dockerContainer.image();
				final String message = containerName + " (" + image + ")";
				final StyledString styledString = new StyledString(message);
				styledString.setStyle(containerName.length(), message.length() - containerName.length(), StyledString.QUALIFIER_STYLER);
				return styledString;
			} else if(element instanceof IDockerImage) {
				final IDockerImage dockerImage = (IDockerImage)element;
				final String imageShortId = dockerImage.id().substring(0, 12);
				final StringBuilder messageBuilder = new StringBuilder(dockerImage.repo());
				final int startTags = messageBuilder.length();
				if(!dockerImage.tags().isEmpty()) {
					messageBuilder.append(": ");
					for(Iterator<String> tagIterator = dockerImage.tags().iterator(); tagIterator.hasNext();) {
						messageBuilder.append(tagIterator.next());
						if(tagIterator.hasNext()) {
							messageBuilder.append(", ");
						}
					}
				}
				final int startImageId = messageBuilder.length();
				messageBuilder.append(" (").append(imageShortId).append(')');
				final String message = messageBuilder.toString();
				final StyledString styledString = new StyledString(message);
				// styled tags
				styledString.setStyle(startTags, startImageId - startTags, StyledString.COUNTER_STYLER);
				// styled image id
				styledString.setStyle(startImageId, message.length() - startImageId, StyledString.QUALIFIER_STYLER);
				return styledString;
			} else if(element instanceof LoadingStub) {
				return new StyledString("Loading...");
			}
		}
		return null;
	}

}
