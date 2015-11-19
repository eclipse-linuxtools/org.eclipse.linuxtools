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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.internal.docker.core.DockerPortMapping;
import org.eclipse.swt.widgets.Display;

/**
 * {@link ITreeContentProvider} for the {@link DockerExplorerView}
 *
 */
public class DockerExplorerContentProvider implements ITreeContentProvider {

	private final Object[] EMPTY = new Object[0];
	
	private TreeViewer viewer;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		this.viewer = (TreeViewer)viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof DockerConnectionManager) {
			final DockerConnectionManager connectionManager = (DockerConnectionManager) inputElement;
			return connectionManager.getConnections();
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		if (parentElement instanceof IDockerConnection) {
			final IDockerConnection dockerConnection = (IDockerConnection) parentElement;
			return new Object[] { new DockerImagesCategory(dockerConnection),
					new DockerContainersCategory(dockerConnection) };
		} else if (parentElement instanceof DockerContainersCategory) {
			final DockerContainersCategory containersCategory = (DockerContainersCategory) parentElement;
			final IDockerConnection connection = containersCategory.getConnection();
			if(connection.isContainersLoaded()) {
				return connection.getContainers().toArray();
			}
			loadContainers(containersCategory);
			return new Object[] { new LoadingStub(containersCategory) };
		} else if (parentElement instanceof DockerImagesCategory) {
			final DockerImagesCategory imagesCategory = (DockerImagesCategory) parentElement;
			final IDockerConnection connection = imagesCategory.getConnection();
			if(connection.isImagesLoaded()) {
				return connection.getImages().toArray();
			}
			loadImages(imagesCategory);
			return new Object[] { new LoadingStub(imagesCategory) };
		} else if (parentElement instanceof IDockerContainer) {
			final IDockerContainer container = (IDockerContainer) parentElement;
			if (container.isInfoLoaded()) {
				final IDockerContainerInfo info = container.info();
				final IDockerNetworkSettings networkSettings = info
						.networkSettings();
				final IDockerHostConfig hostConfig = info.hostConfig();
				return new Object[] {
						new DockerContainerPortMappingsCategory(container,
								networkSettings.ports()),
						new DockerContainerVolumesCategory(container,
								hostConfig.binds()),
						new DockerContainerLinksCategory(container,
								hostConfig.links()) };
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
	 * Call the {@link IDockerConnection#getContainers(boolean)} in a background job to avoid blocking the UI.
	 * @param containersCategory the selected {@link DockerContainersCategory}
	 */
	private void loadContainers(final DockerContainersCategory containersCategory) {
		final Job loadContainersJob = new Job(
				DVMessages.getString("ContainersLoadJob.msg")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				containersCategory.getConnection().getContainers(true);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == DockerExplorerView.class;
			}
		};
		loadContainersJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				event.getResult();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTarget(containersCategory);
					}
				});
			}
		});
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
		// only retain expanded tree paths if container was expanded
		final TreePath[] expandedTreePaths = this.viewer.getExpandedState(
				container) ? this.viewer.getExpandedTreePaths() : null;
		final Job loadContainersJob = new Job(
				DVMessages.getString("ContainerInfoLoadJob.msg")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				container.info(true);
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				event.getResult();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						refreshTarget(container);
						if (expandedTreePaths != null) {
							viewer.setExpandedTreePaths(expandedTreePaths);
						}
					}
				});
			}
		});
		loadContainersJob.schedule();
	}

	/**
	 * Call the {@link IDockerConnection#getImages(boolean)} in a background job to avoid blocking the UI.
	 * @param imagesCategory the selected {@link DockerImagesCategory}
	 */
	private void loadImages(final DockerImagesCategory imagesCategory) {
		final Job loadImagesJob = new Job(
				DVMessages.getString("ImagesLoadJob.msg")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				imagesCategory.getConnection().getImages(true);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family == DockerExplorerView.class;
			}

		};
		loadImagesJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(final IJobChangeEvent event) {
				refreshTarget(imagesCategory);
			}
		});
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
	 * Refresh the whole content tree for the <strong>given target node and all
	 * its subelements</strong>.
	 * 
	 * @param target
	 *            the node to refresh
	 */
	private void refreshTarget(final Object target) {
		// this piece of code must run in an async manner to avoid reentrant
		// call while viewer is busy.
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null) {
					final TreePath[] treePaths = viewer.getExpandedTreePaths();
					viewer.refresh(target, true);
					viewer.setExpandedTreePaths(treePaths);
				}
			}
		});
	}

	/**
	 * Wrapper node to display {@link IDockerImage} of a given
	 * {@link IDockerConnection}
	 */
	public static class DockerImagesCategory {

		private final IDockerConnection connection;

		/**
		 * @param container
		 *            - Docker container
		 */
		public DockerImagesCategory(final IDockerConnection connection) {
			this.connection = connection;
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
	public static class DockerContainersCategory {

		private final IDockerConnection connection;

		/**
		 * @param container
		 *            - Docker container
		 */
		public DockerContainersCategory(final IDockerConnection connection) {
			this.connection = connection;
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
	public static class DockerContainerPortMappingsCategory {

		private final IDockerContainer container;

		private final Map<String, List<IDockerPortBinding>> bindings;

		/**
		 * @param container
		 * @param bindings
		 *            - the container bindings
		 */
		public DockerContainerPortMappingsCategory(
				final IDockerContainer container,
				final Map<String, List<IDockerPortBinding>> bindings) {
			this.container = container;
			this.bindings = bindings;
		}

		public List<IDockerPortMapping> getPortMappings() {
			final List<IDockerPortMapping> portMappings = new ArrayList<>();
			if (bindings != null) {
				for (Entry<String, List<IDockerPortBinding>> entry : bindings
						.entrySet()) {
					// internal port is in the following form: "8080/tcp"
					final String[] source = entry.getKey().split("/");
					final int privatePort = Integer.parseInt(source[0]);
					final String type = source[1];
					for (IDockerPortBinding portBinding : entry.getValue()) {
						portMappings.add(new DockerPortMapping(privatePort,
								Integer.parseInt(portBinding.hostPort()), type,
								portBinding.hostIp()));
					}
				}
			}
			Collections.sort(portMappings);
			return portMappings;
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
	public static class DockerContainerLinksCategory {

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
					this.links.add(new DockerContainerLink(link));
				}
			}
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((container == null) ? 0 : container.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
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

	public static class DockerContainerLink {

		private final String containerName;

		private final String containerAlias;

		/**
		 * Constructor.
		 * 
		 * @param linkValue
		 *            the bind value provided by the {@link IDockerHostConfig}.
		 */
		public DockerContainerLink(final String linkValue) {
			// format: "container_name:containerAlias"
			final String[] args = linkValue.split(":");
			this.containerName = getDisplayableContainerName(args[0]);
			this.containerAlias = args.length > 0
					? getDisplayableContainerAlias(args[1]) : null;
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
	public static class DockerContainerVolumesCategory {

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
					this.volumes.add(new DockerContainerVolume(volume));
				}
			}
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

	public static class DockerContainerVolume {

		private final String hostPath;

		private final String containerPath;

		private final String flags;

		/**
		 * @param volume
		 *            the volume value provided by the {@link IDockerHostConfig}
		 *            .
		 * @return a {@link DockerContainerVolume}
		 */
		public DockerContainerVolume(final String volume) {
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

}
