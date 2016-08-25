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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLink;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerLinksCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerPortMappingsCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolumesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainersCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerImagesCategory;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.LoadingStub;
import org.eclipse.swt.custom.StyledText;

/**
 * @author xcoulon
 *
 */
public class LabelProviderUtils {

	public static final String CREATION_DATE_PATTERN = "yyyy-MM-dd";

	public static final String FINISHED_DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.sssz";

	/**
	 * @param elements
	 *            the {@link List} to reduce
	 * @return each {@link Entry} element of the given {@link Map} has only one
	 *         item, an empty {@link String} if the given list is empty,
	 *         otherwise the given list itself.
	 * 
	 */
	public static <K, V> Object reduce(final Map<K, List<V>> elements) {
		if (elements == null || elements.isEmpty()) {
			return "";
		} else {
			final Map<Object, Object> result = new HashMap<>();
			for (Entry<K, List<V>> entry : elements.entrySet()) {
				result.put(entry.getKey(), reduce(entry.getValue()));
			}
			return result;
		}
	}

	/**
	 * Attempts to flatten the given {@link Set}.
	 * 
	 * @return the first element of the given {@link Set} if it has only one
	 *         item, an empty {@link String} if the given set is empty,
	 *         otherwise the given set itself as array.
	 * @param elements
	 *            the {@link Set} to analyze
	 */
	public static Object reduce(final Set<?> elements) {
		if (elements == null || elements.isEmpty()) {
			return "";
		} else if (elements.size() == 1) {
			return elements.toArray()[0];
		} else {
			return elements.toArray();
		}
	}

	/**
	 * Attempts to flatten the give {@link List}.
	 * 
	 * @return the first element of the given {@link List} has only one item, an
	 *         empty {@link String} if the given list is empty, otherwise the
	 *         given list itself.
	 * @param elements
	 *            the {@link List} to analyze
	 */
	public static Object reduce(final List<?> elements) {
		if (elements == null || elements.isEmpty()) {
			return "";
		} else if (elements.size() == 1) {
			return elements.get(0);
		} else {
			return elements;
		}
	}

	public static String toCreatedDate(final long created) {
		return toDate(created, CREATION_DATE_PATTERN);
	}

	public static String toCreatedDate(final Date created) {
		return toDate(created, CREATION_DATE_PATTERN);
	}

	public static String toFinishedDate(final Date finished) {
		return toDate(finished, FINISHED_DATE_PATTERN);
	}

	public static String toDate(final Long date, final String pattern) {
		final long millis = Long.valueOf(date).longValue() * 1000;
		final Date d = new Date(millis);
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern); // $NON-NLS-1$
		return sdf.format(d);
	}

	public static String toDate(final Date date, final String pattern) {
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern); // $NON-NLS-1$
		return sdf.format(date);
	}

	public static String containerPortMappingToString(
			final IDockerPortMapping portMapping) {
		final StringBuilder mappingBuffer = new StringBuilder();
		if (portMapping.getIp() != null && portMapping.getPublicPort() != 0) {
			mappingBuffer.append(portMapping.getIp()).append(':') // $NON-NLS-1$
					.append(portMapping.getPublicPort()).append("->");
		}
		mappingBuffer.append(portMapping.getPrivatePort()).append('/') // $NON-NLS-1$
				.append(portMapping.getType());
		return mappingBuffer.toString();
	}

	public static String toString(final Object value) {
		if (value == null) {
			return "null"; //$NON-NLS-1$
		}
		if (value instanceof IDockerPortBinding) {
			final IDockerPortBinding binding = (IDockerPortBinding) value;
			final String hostIp = (binding.hostIp() == null
					|| binding.hostIp().isEmpty()) ? "<unspecified>" //$NON-NLS-1$
							: binding.hostIp();
			final String hostPort = (binding.hostPort() == null
					|| binding.hostPort().isEmpty()) ? "<unspecified>" //$NON-NLS-1$
							: binding.hostPort();
			return new StringBuilder().append(hostIp).append(':')
					.append(hostPort).toString();
		}
		return value.toString();
	}

	/**
	 * @param element
	 *            the element whose {@link StyledText} needs to be provided
	 * @return the {@link StyledText} for the given {@code element}, or
	 *         <code>null</code> if the type of element is not supported.
	 */
	public static StyledString getStyledText(final Object element) {
		// when the text to display is in the properties view title bar, the
		// given element
		// is the TreeSelection from the contributing view (the Docker Explorer
		// View)
		if (element instanceof IStructuredSelection) {
			return getStyledText(
					((IStructuredSelection) element).getFirstElement());
		}
		if (element instanceof IDockerConnection) {
			return getStyledText((IDockerConnection) element);
		} else if (element instanceof DockerImagesCategory) {
			return new StyledString(
					DVMessages.getString("DockerImagesCategory.label")); //$NON-NLS-1$
		} else if (element instanceof DockerContainersCategory) {
			return new StyledString(
					DVMessages.getString("DockerContainersCategory.label")); //$NON-NLS-1$
		} else if (element instanceof IDockerContainer) {
			return getStyledText((IDockerContainer) element);
		} else if (element instanceof IDockerImage) {
			return LabelProviderUtils.getStyledText((IDockerImage) element);
		} else if (element instanceof DockerContainerPortMappingsCategory) {
			return new StyledString(DVMessages
					.getString("DockerContainerPortMappingsCategory.label")); //$NON-NLS-1$
		} else if (element instanceof IDockerPortMapping) {
			return getStyledText((IDockerPortMapping) element);
		} else if (element instanceof DockerContainerVolumesCategory) {
			return new StyledString(DVMessages
					.getString("DockerContainerVolumesCategory.label")); //$NON-NLS-1$
		} else if (element instanceof DockerContainerVolume) {
			return getStyledText((DockerContainerVolume) element);
		} else if (element instanceof DockerContainerLinksCategory) {
			return new StyledString(
					DVMessages.getString("DockerContainerLinksCategory.label")); //$NON-NLS-1$
		} else if (element instanceof DockerContainerLink) {
			return getStyledText((DockerContainerLink) element);
		} else if (element instanceof String) {
			return new StyledString((String) element);
		} else if (element instanceof LoadingStub) {
			return new StyledString(DVMessages.getString("Loading.label")); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * @param containerLink
	 *            the {@link DockerContainerLink} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(
			final DockerContainerLink containerLink) {
		final StyledString styledString = new StyledString(
				containerLink.getContainerName()).append(
						" (" + containerLink.getContainerAlias() + ")", //$NON-NLS-1$ //$NON-NLS-2$
						StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	/**
	 * @param containerVolume
	 *            the {@link DockerContainerVolume} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(
			final DockerContainerVolume containerVolume) {
		final StyledString styledString = new StyledString();
		final String hostPath = containerVolume.getHostPath();
		if (containerVolume.getHostPath() != null
				&& containerVolume.getContainerPath() != null) {
			styledString.append(hostPath).append(" -> ") //$NON-NLS-1$
					.append(containerVolume.getContainerPath());
		} else if (containerVolume.getHostPath() == null
				&& containerVolume.getContainerPath() != null) {
			styledString.append(containerVolume.getContainerPath());
		}
		if (containerVolume.getFlags() != null) {
			styledString.append(" (" + containerVolume.getFlags() + ")", //$NON-NLS-1$ //$NON-NLS-2$
					StyledString.QUALIFIER_STYLER);
		}
		return styledString;
	}

	/**
	 * @param mapping
	 *            the {@link IDockerPortMapping} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(final IDockerPortMapping mapping) {
		final StyledString styledString = new StyledString();
		styledString.append(mapping.getIp() + ":" + mapping.getPublicPort()) //$NON-NLS-1$
				.append(" -> ") //$NON-NLS-1$
				.append(Integer.toString(mapping.getPrivatePort()))
				.append(" (" + mapping.getType() + ")", //$NON-NLS-1$ //$NON-NLS-2$
						StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	/**
	 * @param dockerContainer
	 *            the {@link IDockerContainer} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(
			final IDockerContainer dockerContainer) {
		final StyledString styledString = new StyledString();
		styledString.append(dockerContainer.name()).append(
				" (" + dockerContainer.image() + ")", //$NON-NLS-1$ //$NON-NLS-2$
				StyledString.QUALIFIER_STYLER);
		return styledString;
	}

	/**
	 * @param connection
	 *            the {@link IDockerConnection} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(
			final IDockerConnection connection) {
		final String connectionName = (connection.getName() != null
				&& !connection.getName().isEmpty()) ? connection.getName()
						: DVMessages.getString("Connection.unnamed"); //$NON-NLS-1$
		final StyledString styledString = new StyledString();
		styledString.append(connectionName);
		if (connection.getUri() != null && !connection.getUri().isEmpty()) {
			styledString.append(" (" + connection.getUri() + //$NON-NLS-1$
					")", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		}
		return styledString;
	}

	/**
	 * @param dockerImage
	 *            the {@link IDockerImage} to process
	 * @return the {@link StyledString} to be displayed.
	 */
	public static StyledString getStyledText(final IDockerImage dockerImage) {
		final StyledString result = new StyledString(dockerImage.repo());
		if (!dockerImage.tags().isEmpty()) {
			final List<String> tags = new ArrayList<>(dockerImage.tags());
			Collections.sort(tags);
			result.append(":");
			result.append(tags.stream().collect(Collectors.joining(", ")), //$NON-NLS-1$
					StyledString.COUNTER_STYLER);
		}
		// TODO: remove the cast to 'DockerImage' once the 'shortId()'
		// method is in the public API
		result.append(" (", StyledString.QUALIFIER_STYLER) //$NON-NLS-1$
				.append(((DockerImage) dockerImage).shortId(),
						StyledString.QUALIFIER_STYLER)
				.append(')', StyledString.QUALIFIER_STYLER); // $NON-NLS-1$
		return result;
	}

}
