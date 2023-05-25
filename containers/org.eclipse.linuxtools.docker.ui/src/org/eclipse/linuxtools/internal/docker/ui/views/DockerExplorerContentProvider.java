/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerContainer;
import org.eclipse.linuxtools.internal.docker.core.DockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerPortMapping;
import org.eclipse.swt.widgets.Display;

/**
 * {@link ITreeContentProvider} for the {@link DockerExplorerView}
 *
 */
public class DockerExplorerContentProvider implements ITreeContentProvider {

	private final Object[] EMPTY = new Object[0];

	private Map<IDockerConnection, Job> openRetryJobs = new HashMap<>();

	private TreeViewer viewer;

	@Override
	public void dispose() {
		Collection<Job> jobs = Collections.emptyList();
		synchronized (openRetryJobs) {
			// make copy of jobs list to avoid ConcurrentModificationException
			// because the jobs remove themselves from openRetryJobs
			// as part of success and failure
			jobs = new ArrayList<>(openRetryJobs.values());
		}
		for (Job job : jobs) {
			LoadingJob loadingJob = (LoadingJob) job;
			IProgressMonitor monitor = loadingJob.getMonitor();
			monitor.setCanceled(true);
			job.cancel();
			try {
				job.join();
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		this.viewer = (TreeViewer) viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof DockerConnectionManager connectionManager) {
			return connectionManager.getConnections();
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IDockerConnection connection) {
			// check the connection availability before returning the
			// 'containers' and 'images' child nodes.
			if (connection.isOpen()) {
				return new Object[] { new DockerImagesCategory(connection),
						new DockerContainersCategory(connection) };
			} else if (connection
					.getState() == EnumDockerConnectionState.UNKNOWN) {
				open(connection);
				return new Object[] { new LoadingStub(connection) };
			} else if (connection
					.getState() == EnumDockerConnectionState.CLOSED) {
				synchronized (openRetryJobs) {
					Job job = openRetryJobs.get(connection);
					if (job == null) {
						openRetry(connection);
					}
				}
				return new Object[] { new LoadingStub(connection) };
			}
			return new Object[0];
		} else if (parentElement instanceof DockerContainersCategory containersCategory) {
			final IDockerConnection connection = containersCategory.getConnection();
			if(connection.isContainersLoaded()) {
				return connection.getContainers().toArray();
			}
			loadContainers(containersCategory);
			return new Object[] { new LoadingStub(containersCategory) };
		} else if (parentElement instanceof DockerImagesCategory imagesCategory) {
			final IDockerConnection connection = imagesCategory.getConnection();
			if(connection.isImagesLoaded()) {
				// here we duplicate the images to show one org/repo with all
				// its tags per node in the tree
				final List<IDockerImage> allImages = connection.getImages();
				final List<IDockerImage> explorerImages = splitImageTagsByRepo(
						allImages);
				return explorerImages.toArray();
			}
			loadImages(imagesCategory);
			return new Object[] { new LoadingStub(imagesCategory) };
		} else if (parentElement instanceof IDockerContainer) {
			final DockerContainer container = (DockerContainer) parentElement;
			if (container.isInfoLoaded()) {
				final IDockerContainerInfo info = container.info();
				final IDockerNetworkSettings networkSettings = (info != null)
						? info.networkSettings() : null;
				final IDockerHostConfig hostConfig = (info != null)
						? info.hostConfig() : null;
				return new Object[] {
						new DockerContainerPortMappingsCategory(container,
								(networkSettings != null)
										? networkSettings.ports()
										: Collections
												.<String, List<IDockerPortBinding>> emptyMap()),
						new DockerContainerVolumesCategory(container,
								(hostConfig != null) ? hostConfig.binds()
										: Collections.<String> emptyList()),
						new DockerContainerLinksCategory(container,
								(hostConfig != null) ? hostConfig.links()
										: Collections.<String> emptyList()) };
			}
			loadContainerInfo(container);
			return new Object[] { new LoadingStub(container) };
		} else if (parentElement instanceof DockerContainerLinksCategory) {
			final DockerContainerLinksCategory linksCategory = (DockerContainerLinksCategory) parentElement;
			return linksCategory.getLinks().toArray();
		} else
			if (parentElement instanceof DockerContainerPortMappingsCategory) {
			final DockerContainerPortMappingsCategory portMappingsCategory = (DockerContainerPortMappingsCategory) parentElement;
			return portMappingsCategory.getPortMappings().toArray();

		} else if (parentElement instanceof DockerContainerVolumesCategory) {
			final DockerContainerVolumesCategory volumesCategory = (DockerContainerVolumesCategory) parentElement;
			return volumesCategory.getVolumes().toArray();
		}
		return EMPTY;
	}

	/**
	 * Iterates on the given {@code images} and duplicates the elements that
	 * have multiple repositories
	 * 
	 * @param images
	 *            the {@link List} of {@link IDockerImage} to process
	 * @return the resulting {@link List} containing duplicate
	 *         {@link IDockerImage} when the source had multiple repositories.
	 */
	public static List<IDockerImage> splitImageTagsByRepo(
			final List<IDockerImage> images) {
		return images.stream()
				.flatMap(image -> DockerImage.duplicateImageByRepo(image))
				.toList();
	}

	/**
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background
	 * job to avoid blocking the UI.
	 * 
	 * @param connection
	 *            the connection to ping
	 */
	private void open(final IDockerConnection connection) {
		final Job pingJob = new LoadingJob(DVMessages.getString("PingJob.msg"), //$NON-NLS-1$
				connection) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					connection.open(true);
					connection.ping();
					return Status.OK_STATUS;
				} catch (DockerException e) {
					Activator.logWarningMessage(DVMessages.getFormattedString(
										"PingJobError.msg.withExplanation", //$NON-NLS-1$
										connection.getName(), e.getMessage()));
					return Status.CANCEL_STATUS;
				}
			}
		};
		pingJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#open(boolean)} in a background job to
	 * continually retry opening the connection and avoid blocking the UI.
	 * 
	 * @param connection
	 *            the connection to open/ping
	 */
	private void openRetry(final IDockerConnection connection) {
		final Job openRetryJob = new LoadingJob(
				DVMessages.getFormattedString("PingJob2.msg", //$NON-NLS-1$
						connection.getName(), connection.getUri()),
				connection) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				setMonitor(monitor);
				long totalSleep = 0;
				long sleepTime = 3000; // 3 second default
				for (;;) {
					try {
						// check if Connection is removed or cancelled
						if (monitor.isCanceled() || DockerConnectionManager
								.getInstance()
								.getConnectionByUri(
										connection.getUri()) == null) {
							synchronized (openRetryJobs) {
								openRetryJobs.remove(connection);
							}
							return Status.CANCEL_STATUS;
						}
						connection.open(true);
						connection.ping();
						synchronized (openRetryJobs) {
							openRetryJobs.remove(connection);
						}
						return Status.OK_STATUS;
					} catch (DockerException e) {
						// ignore
					}
					try {
						Thread.sleep(sleepTime);
						totalSleep += sleepTime;
						// if we have tried for over 5 minutes, switch to the
						// container refresh rate which defaults to 15 seconds.
						// This should slow down the interference of connections
						// we never use.
						if (totalSleep > 300000) {
							totalSleep = 0; // prevent a future overflow
							sleepTime = Platform.getPreferencesService()
									.getLong("org.eclipse.linuxtools.docker.ui", //$NON-NLS-1$
											"containerRefreshTime", 15000, //$NON-NLS-1$
											null);
						}
					} catch (InterruptedException e) {
						synchronized (openRetryJobs) {
							openRetryJobs.remove(connection);
						}
						return Status.CANCEL_STATUS;
					}
				}
			}
		};
		synchronized (openRetryJobs) {
			openRetryJobs.put(connection, openRetryJob);
		}
		openRetryJob.setSystem(true);
		openRetryJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background
	 * job to avoid blocking the UI.
	 * 
	 * @param containersCategory
	 *            the selected {@link DockerContainersCategory}
	 */
	private void loadContainers(
			final DockerContainersCategory containersCategory) {
		final Job loadContainersJob = new LoadingJob(
				DVMessages.getString("ContainersLoadJob.msg"), //$NON-NLS-1$
				containersCategory) {
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				containersCategory.getConnection().getContainers(true);
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background
	 * job to avoid blocking the UI.
	 * 
	 * @param container
	 *            the selected {@link DockerContainersCategory}
	 */
	private void loadContainerInfo(final IDockerContainer container) {
		final Job loadContainersJob = new LoadingJob(
				DVMessages.getString("ContainerInfoLoadJob.msg"), container) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				((DockerContainer) container).info(true);
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#getImages(boolean)} in a background job
	 * to avoid blocking the UI.
	 * 
	 * @param imagesCategory
	 *            the selected {@link DockerImagesCategory}
	 */
	private void loadImages(final DockerImagesCategory imagesCategory) {
		final Job loadImagesJob = new LoadingJob(
				DVMessages.getString("ImagesLoadJob.msg"), imagesCategory) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				imagesCategory.getConnection().getImages(true);
				return Status.OK_STATUS;
			}
		};
		loadImagesJob.schedule();
	}

	@Override
	public Object getParent(final Object element) {
		if (element instanceof DockerImagesCategory) {
			return ((DockerImagesCategory) element).getConnection();
		} else if (element instanceof DockerContainersCategory) {
			return ((DockerContainersCategory) element).getConnection();
		}
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		// We want to automate enabling a connection.
		// If the connection is closed (meaning we tried to open
		// and failed), then kick off a retry job.
		// Don't start a retry job if one is already running.
		if (element instanceof IDockerConnection connection) {
			if (connection.getState() != EnumDockerConnectionState.ESTABLISHED) {
				Job openRetryJob = null;
				synchronized (openRetryJobs) {
					openRetryJob = openRetryJobs.get(connection);
				}
				if (openRetryJob == null) {
					openRetry(connection);
				}
			}
		}
		return (element instanceof IDockerConnection
				|| element instanceof DockerContainersCategory
				|| element instanceof DockerImagesCategory
				|| element instanceof IDockerContainer
				|| (element instanceof DockerContainerLinksCategory
						&& !((DockerContainerLinksCategory) element).getLinks()
								.isEmpty())
				|| (element instanceof DockerContainerPortMappingsCategory
						&& !((DockerContainerPortMappingsCategory) element)
								.getPortMappings().isEmpty())
				|| (element instanceof DockerContainerVolumesCategory
						&& !((DockerContainerVolumesCategory) element)
								.getVolumes().isEmpty()));
	}



	/**
	 * Wrapper node to display {@link IDockerImage} of a given
	 * {@link IDockerConnection}
	 */
	public static class DockerImagesCategory implements IAdaptable {

		private final IDockerConnection connection;

		/**
		 * @param container
		 *            - Docker container
		 */
		public DockerImagesCategory(final IDockerConnection connection) {
			this.connection = connection;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getConnection();
			}
			return null;
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((connection == null) ? 0 : connection.hashCode());
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
			DockerImagesCategory other = (DockerImagesCategory) obj;
			if (connection == null) {
				if (other.connection != null)
					return false;
			} else if (!connection.equals(other.connection))
				return false;
			return true;
		}

	}

	/**
	 * Wrapper node to display {@link IDockerContainer} of a given
	 * {@link IDockerConnection}
	 */
	public static class DockerContainersCategory implements IAdaptable {

		private final IDockerConnection connection;

		/**
		 * @param container
		 *            - Docker container
		 */
		public DockerContainersCategory(final IDockerConnection connection) {
			this.connection = connection;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getConnection();
			}
			return null;
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((connection == null) ? 0 : connection.hashCode());
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
			DockerContainersCategory other = (DockerContainersCategory) obj;
			if (connection == null) {
				if (other.connection != null)
					return false;
			} else if (!connection.equals(other.connection))
				return false;
			return true;
		}
	}

	/**
	 * Wrapper node to display {@link IDockerPortMapping} of a given
	 * {@link IDockerContainer}
	 */
	public static class DockerContainerPortMappingsCategory
			implements IAdaptable {

		private final IDockerContainer container;

		private final List<IDockerPortMapping> portMappings;

		/**
		 * @param container
		 * @param bindings
		 *            - the container bindings
		 */
		public DockerContainerPortMappingsCategory(
				final IDockerContainer container,
				final Map<String, List<IDockerPortBinding>> bindings) {
			this.container = container;
			this.portMappings = new ArrayList<>();
			if (bindings != null) {
				for (Entry<String, List<IDockerPortBinding>> entry : bindings
						.entrySet()) {
					// internal port is in the following form: "8080/tcp"
					final String[] source = entry.getKey().split("/");
					final int privatePort = Integer.parseInt(source[0]);
					final String type = source[1];
					for (IDockerPortBinding portBinding : entry.getValue()) {
						portMappings.add(
								new DockerPortMapping(container, privatePort,
										Integer.parseInt(
												portBinding.hostPort()),
										type, portBinding.hostIp()));
					}
				}
			}
			Collections.sort(portMappings,
					(portMapping,
							otherPortMapping) -> portMapping.getPrivatePort()
									- otherPortMapping.getPrivatePort());
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getContainer().getConnection();
			}
			return null;
		}

		public IDockerContainer getContainer() {
			return container;
		}

		public List<IDockerPortMapping> getPortMappings() {
			return this.portMappings;
		}

		@Override
		public String toString() {
			return "Port mappings for " + this.container.name();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((container == null) ? 0 : container.hashCode());
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
			DockerContainerPortMappingsCategory other = (DockerContainerPortMappingsCategory) obj;
			if (container == null) {
				if (other.container != null)
					return false;
			} else if (!container.equals(other.container))
				return false;
			return true;
		}
	}

	/**
	 * Wrapper node to display the {@link DockerContainerLink} of a given
	 * {@link IDockerContainer}
	 */
	public static class DockerContainerLinksCategory implements IAdaptable {

		private final IDockerContainer container;

		private final List<DockerContainerLink> links;

		/**
		 * Constructor.
		 * 
		 * @param container
		 * 
		 * @param links
		 *            - the container links
		 */
		public DockerContainerLinksCategory(final IDockerContainer container,
				final List<String> links) {
			this.container = container;
			this.links = new ArrayList<>();
			if (links != null) {
				for (String link : links) {
					this.links.add(new DockerContainerLink(container, link));
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getContainer().getConnection();
			}
			return null;
		}

		public IDockerContainer getContainer() {
			return container;
		}

		public List<DockerContainerLink> getLinks() {
			if (this.links == null) {
				return Collections.emptyList();
			}
			return this.links;
		}

		@Override
		public String toString() {
			return "Container links for " + this.container.name();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((container == null) ? 0 : container.hashCode());
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
			DockerContainerLinksCategory other = (DockerContainerLinksCategory) obj;
			if (container == null) {
				if (other.container != null)
					return false;
			} else if (!container.equals(other.container))
				return false;
			return true;
		}

	}

	public static class DockerContainerLink implements IAdaptable {

		private final IDockerContainer container;

		private final String containerName;

		private final String containerAlias;

		/**
		 * Constructor.
		 * 
		 * @param linkValue
		 *            the bind value provided by the {@link IDockerHostConfig}.
		 */
		public DockerContainerLink(final IDockerContainer container,
				final String linkValue) {
			this.container = container;
			// format: "container_name:containerAlias"
			final String[] args = linkValue.split(":");
			this.containerName = getDisplayableContainerName(args[0]);
			this.containerAlias = args.length > 0
					? getDisplayableContainerAlias(args[1]) : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getContainer().getConnection();
			}
			return null;
		}

		public IDockerContainer getContainer() {
			return container;
		}

		/**
		 * Removes the heading "/" i(if found) in the given container name
		 * 
		 * @param containerName
		 * @return a displayable container name
		 */
		private String getDisplayableContainerName(final String containerName) {
			return containerName.startsWith("/") ? containerName.substring(1)
					: containerName;
		}

		/**
		 * Removes the heading "/" i(if found) in the given container name
		 * 
		 * @param containerName
		 * @return a displayable container name
		 */
		private String getDisplayableContainerAlias(
				final String containerAlias) {
			final String[] containerAliasSplit = containerAlias.split("/");
			if (containerAliasSplit.length > 1) {
				return containerAliasSplit[2];
			}
			return null;
		}

		public String getContainerName() {
			return containerName;
		}

		public String getContainerAlias() {
			return containerAlias;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((containerAlias == null) ? 0
					: containerAlias.hashCode());
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
			DockerContainerLink other = (DockerContainerLink) obj;
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

	/**
	 * Wrapper node to display {@link DockerContainerVolume} of a given
	 * {@link IDockerContainer}
	 */
	public static class DockerContainerVolumesCategory implements IAdaptable {

		private final IDockerContainer container;

		private final List<DockerContainerVolume> volumes;

		/**
		 * Constructor.
		 * 
		 * @param container
		 * 
		 * @param volumes
		 *            - the parent Docker container
		 */
		public DockerContainerVolumesCategory(final IDockerContainer container,
				final List<String> volumes) {
			this.container = container;
			this.volumes = new ArrayList<>();
			if (volumes != null) {
				for (String volume : volumes) {
					this.volumes
							.add(new DockerContainerVolume(container, volume));
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getContainer().getConnection();
			}
			return null;
		}

		public IDockerContainer getContainer() {
			return container;
		}

		public List<DockerContainerVolume> getVolumes() {
			if (this.volumes == null) {
				return Collections.emptyList();
			}
			return volumes;
		}

		@Override
		public String toString() {
			return "Volumes for " + this.container.name();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((container == null) ? 0 : container.hashCode());
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
			DockerContainerVolumesCategory other = (DockerContainerVolumesCategory) obj;
			if (container == null) {
				if (other.container != null)
					return false;
			} else if (!container.equals(other.container))
				return false;
			return true;
		}

	}

	public static class DockerContainerVolume implements IAdaptable {

		private final IDockerContainer container;

		private final String hostPath;

		private final String containerPath;

		private final String flags;

		/**
		 * @param container
		 * @param volume
		 *            the volume value provided by the {@link IDockerHostConfig}
		 *            .
		 * @return a {@link DockerContainerVolume}
		 */
		public DockerContainerVolume(final IDockerContainer container,
				final String volume) {
			this.container = container;
			// (1) "container_path" to create a new volume for the container
			// (2) "host_path:container_path" to bind-mount a host path into the
			// container
			// (3) "host_path:container_path:ro" to make the bind-mount
			// read-only
			// inside the container.
			final String[] args = volume.split(":");
			// on case (1), hostPath is null
			this.hostPath = args.length > 1 ? args[0] : null;
			// on case (1), containerPath is the first (and only) arg, otherwise
			// it's the second one.
			this.containerPath = args.length > 1 ? args[1] : args[0];
			// flags exists on case (3) only
			this.flags = args.length > 2 ? args[2] : null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAdapter(final Class<T> adapter) {
			if (adapter.equals(IDockerConnection.class)) {
				return (T) getContainer().getConnection();
			}
			return null;
		}

		public IDockerContainer getContainer() {
			return container;
		}

		public String getHostPath() {
			return hostPath;
		}

		public String getContainerPath() {
			return containerPath;
		}

		public String getFlags() {
			return flags;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((containerPath == null) ? 0 : containerPath.hashCode());
			result = prime * result + ((flags == null) ? 0 : flags.hashCode());
			result = prime * result
					+ ((hostPath == null) ? 0 : hostPath.hashCode());
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
			DockerContainerVolume other = (DockerContainerVolume) obj;
			if (containerPath == null) {
				if (other.containerPath != null)
					return false;
			} else if (!containerPath.equals(other.containerPath))
				return false;
			if (flags == null) {
				if (other.flags != null)
					return false;
			} else if (!flags.equals(other.flags))
				return false;
			if (hostPath == null) {
				if (other.hostPath != null)
					return false;
			} else if (!hostPath.equals(other.hostPath))
				return false;
			return true;
		}

	}

	/**
	 * Node to indicate that a job is running and loading data.
	 */
	public static class LoadingStub {

		private final Object element;

		public LoadingStub(final Object element) {
			this.element = element;
		}

		public Object getElement() {
			return element;
		}
	}

	private abstract class LoadingJob extends Job {

		private IProgressMonitor monitor;

		public LoadingJob(final String name, final Object target) {
			super(name);
			this.addJobChangeListener(new JobChangeAdapter() {

				@Override
				public void done(final IJobChangeEvent event) {
					refreshTarget(target);
				}
			});
		}

		public IProgressMonitor getMonitor() {
			return monitor;
		}

		public void setMonitor(IProgressMonitor monitor) {
			this.monitor = monitor;
		}

		@Override
		public boolean belongsTo(Object family) {
			return DockerExplorerView.class.equals(family);
		}

		/**
		 * Refresh the whole content tree for the <strong>given target node and
		 * all its subelements</strong>.
		 * 
		 * @param target
		 *            the node to refresh
		 */
		private void refreshTarget(final Object target) {
			// this piece of code must run in an async manner to avoid reentrant
			// call while viewer is busy.
			Display.getDefault().asyncExec(() -> {
				if (viewer != null && !viewer.getControl().isDisposed()) {
					final TreePath[] treePaths = viewer.getExpandedTreePaths();
					viewer.refresh(target, true);
					viewer.setExpandedTreePaths(treePaths);
				}
			});
		}
	}

}
