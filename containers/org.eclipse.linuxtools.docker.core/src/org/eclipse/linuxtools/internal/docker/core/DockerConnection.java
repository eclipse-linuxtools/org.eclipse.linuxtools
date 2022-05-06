/*******************************************************************************
 * Copyright (c) 2014, 2021 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.internal.docker.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ProcessingException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerContainerNotFoundException;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.DockerOpenConnectionException;
import org.eclipse.linuxtools.docker.core.DockerPingConnectionException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.EnumDockerLoggingStatus;
import org.eclipse.linuxtools.docker.core.IDockerConfParameter;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnection2;
import org.eclipse.linuxtools.docker.core.IDockerConnection3;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerChange;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerExit;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageBuildOptions;
import org.eclipse.linuxtools.docker.core.IDockerImageHierarchyNode;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;
import org.eclipse.linuxtools.docker.core.IDockerIpamConfig;
import org.eclipse.linuxtools.docker.core.IDockerNetwork;
import org.eclipse.linuxtools.docker.core.IDockerNetworkConfig;
import org.eclipse.linuxtools.docker.core.IDockerNetworkCreation;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerProgressHandler;
import org.eclipse.linuxtools.docker.core.IDockerVersion;
import org.eclipse.linuxtools.docker.core.IDockerVolume;
import org.eclipse.linuxtools.docker.core.ILogger;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.docker.core.Messages;
import org.eclipse.linuxtools.internal.docker.core.DockerImage.DockerImageQualifier;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.terminal.view.core.TerminalServiceFactory;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalService;
import org.eclipse.tm.terminal.view.core.interfaces.ITerminalServiceOutputStreamMonitorListener;
import org.eclipse.tm.terminal.view.core.interfaces.constants.ITerminalsConnectorConstants;
import org.mandas.docker.client.DockerClient;
import org.mandas.docker.client.DockerClient.AttachParameter;
import org.mandas.docker.client.DockerClient.BuildParam;
import org.mandas.docker.client.DockerClient.ExecCreateParam;
import org.mandas.docker.client.DockerClient.LogsParam;
import org.mandas.docker.client.LogStream;
import org.mandas.docker.client.exceptions.ContainerNotFoundException;
import org.mandas.docker.client.exceptions.DockerCertificateException;
import org.mandas.docker.client.exceptions.DockerTimeoutException;
import org.mandas.docker.client.messages.Container;
import org.mandas.docker.client.messages.ContainerChange;
import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.ContainerCreation;
import org.mandas.docker.client.messages.ContainerExit;
import org.mandas.docker.client.messages.ContainerInfo;
import org.mandas.docker.client.messages.ExecCreation;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.HostConfig.LxcConfParameter;
import org.mandas.docker.client.messages.Image;
import org.mandas.docker.client.messages.ImageInfo;
import org.mandas.docker.client.messages.ImageSearchResult;
import org.mandas.docker.client.messages.Info;
import org.mandas.docker.client.messages.Ipam;
import org.mandas.docker.client.messages.IpamConfig;
import org.mandas.docker.client.messages.Network;
import org.mandas.docker.client.messages.NetworkConfig;
import org.mandas.docker.client.messages.PortBinding;
import org.mandas.docker.client.messages.RegistryAuth;
import org.mandas.docker.client.messages.Version;
import org.mandas.docker.client.messages.Volume;
import org.mandas.docker.client.messages.VolumeList;

/**
 * A connection to a Docker daemon. The connection may rely on Unix Socket or TCP connection (using the REST API).
 * All low-level communication is delegated to a wrapped {@link DockerClient}.
 *
 *
 */
public class DockerConnection
		implements IDockerConnection, IDockerConnection2, IDockerConnection3,
		Closeable {

	// Builder allowing different binding modes (unix socket vs TCP connection)
	public static class Builder {

		private String name;

		public Builder name(final String name) {
			this.name = name;
			return this;
		}

		/**
		 * Creates a new {@link DockerConnection} using a Unix socket.
		 *
		 * @param unixSocketConnectionSettings
		 *            the connection settings.
		 * @return a new {@link DockerConnection}
		 */
		public DockerConnection unixSocketConnection(
				final UnixSocketConnectionSettings unixSocketConnectionSettings) {
			return new DockerConnection(name, unixSocketConnectionSettings,
					null, null);
		}

		/**
		 * Creates a {@link DockerConnection} using a TCP connection.
		 *
		 * @param tcpConnectionSettings
		 *            the {@link TCPConnectionSettings}
		 * @return a new {@link DockerConnection}
		 */
		public DockerConnection tcpConnection(
				final TCPConnectionSettings tcpConnectionSettings) {
			return new DockerConnection(name,
					tcpConnectionSettings, null,
					null);
		}

	}

	private String name;
	private IDockerConnectionSettings connectionSettings;
	@SuppressWarnings("unused")
	private IDockerConnectionInfo connectionInfo;
	private final String username;
	private boolean finalizing;
	private final Object imageLock = new Object();
	private final Object containerLock = new Object();
	private final Object actionLock = new Object();
	private final Object clientLock = new Object();
	private DockerClientFactory dockerClientFactory = new DockerClientFactory();
	private DockerClient client;

	private Map<String, Job> actionJobs;

	private Map<String, LogThread> loggingThreads = new HashMap<>();

	// containers sorted by name
	private List<IDockerContainer> containers;
	// containers indexed by id
	private Map<String, IDockerContainer> containersById = new HashMap<>();
	// flag to indicate if the state of the connection to the Docker daemon
	private EnumDockerConnectionState state = EnumDockerConnectionState.UNKNOWN;
	private List<IDockerImage> images;
	private Boolean isLocalConnection;

	ListenerList<IDockerContainerListener> containerListeners;
	ListenerList<IDockerImageListener> imageListeners;

	/**
	 * Constructor for a Unix socket based connection
	 */
	private DockerConnection(final String name,
			final UnixSocketConnectionSettings connectionSettings,
			final String username, final String password) {
		this.name = name;
		this.connectionSettings = connectionSettings;
		this.username = username;
		storePassword(connectionSettings.getPath(), username, password);
	}

	/**
	 * Constructor for a TCP-based connection
	 */
	private DockerConnection(final String name,
			final TCPConnectionSettings connectionSettings,
			final String username,
			final String password) {
		this.name = name;
		this.connectionSettings = connectionSettings;
		this.username = username;
		storePassword(connectionSettings.getHost(), username, password);
		// Add the container refresh manager to watch the containers list
		DockerContainerRefreshManager dcrm = DockerContainerRefreshManager
				.getInstance();
		addContainerListener(dcrm);
	}

	private void storePassword(String uri, String username, String passwd) {
		ISecurePreferences root = SecurePreferencesFactory.getDefault();
		String key = DockerConnection.getPreferencesKey(uri, username);
		ISecurePreferences node = root.node(key);
		try {
			if (passwd != null && !passwd.equals("")) //$NON-NLS-1$
				node.put("password", passwd, true /* encrypt */); //$NON-NLS-1$
		} catch (StorageException e) {
			Activator.log(e);
		}
	}

	public static String getPreferencesKey(String uri, String username) {
		String key = "/org/eclipse/linuxtools/docker/core/"; //$NON-NLS-1$
		key += uri + "/" + username; //$NON-NLS-1$
		return EncodingUtils.encodeSlashes(key);
	}

	@Override
	public boolean isOpen() {
		return this.client != null
				&& this.state == EnumDockerConnectionState.ESTABLISHED;
	}

	@Override
	public void open(boolean registerContainerRefreshManager)
			throws DockerException {
		// synchronized block to avoid concurrent attempts to open a connection
		// to the same Docker daemon
		synchronized (this) {
			if (this.client == null) {
				try {
					setClient(dockerClientFactory
							.getClient(this.connectionSettings));
					if (registerContainerRefreshManager) {
						// Add the container refresh manager to watch the
						// containers
						// list
						DockerContainerRefreshManager dcrm = DockerContainerRefreshManager
								.getInstance();
						addContainerListener(dcrm);
					}
				} catch (DockerCertificateException e) {
					setState(EnumDockerConnectionState.CLOSED);
					throw new DockerOpenConnectionException(
							NLS
							.bind(Messages.Open_Connection_Failure, this.name,
									this.getUri()),
							e);
				}
			}
			// then try to ping the Docker daemon to verify the connection
			ping();
		}
	}

	public DockerClient getClient() {
		return client;
	}

	public void setClient(final DockerClient client) {
		this.client = client;
		try {
			this.connectionInfo = getInfo();
		} catch (Exception e) {
			// ignore for now as this seems to occur too often and we always
			// check the value of connectionInfo before using
		}
	}
	/**
	 * Change the default {@link DockerClientFactory}
	 *
	 * @param dockerClientFactory
	 *            the new {@link DockerClientFactory} to use when opening a
	 *            connection.
	 */
	public void setDockerClientFactory(
			final DockerClientFactory dockerClientFactory) {
		this.dockerClientFactory = dockerClientFactory;
	}

	@Override
	public EnumDockerConnectionState getState() {
		return this.state;
	}

	public void setState(final EnumDockerConnectionState state) {
		EnumDockerConnectionState oldState = this.state;
		this.state = state;
		switch (state) {
		case UNKNOWN:
		case CLOSED:
			synchronized (imageLock) {
				this.images = Collections.emptyList();
			}
			synchronized (containerLock) {
				this.containers = Collections.emptyList();
				this.containersById = new HashMap<>();
			}
			notifyContainerListeners(this.containers);
			notifyImageListeners(this.images);
			if (!isFinalizing()
					&& oldState == EnumDockerConnectionState.ESTABLISHED) {
				DockerConnectionManager.instanceNotifyListeners(this,
						IDockerConnectionManagerListener.DISABLE_EVENT);
			}
			break;
		case ESTABLISHED:
			this.getContainers(true);
			this.getImages(true);
			notifyContainerListeners(this.containers);
			notifyImageListeners(this.images);
			if (oldState != EnumDockerConnectionState.ESTABLISHED) {
				DockerConnectionManager.instanceNotifyListeners(this,
						IDockerConnectionManagerListener.ENABLE_EVENT);
			}
			break;
		}
	}

	@Override
	public void ping() throws DockerException {
		try {
			// for a unix socket, if it doesn't exist (i.e. daemon not up)
			// then any request will result in stderr messages as retries will
			// occur...check for socket existence before trying the ping
			if (getState() == EnumDockerConnectionState.CLOSED
					|| getState() == EnumDockerConnectionState.UNKNOWN) {
				if (connectionSettings
						.getType() == IDockerConnectionSettings.BindingType.UNIX_SOCKET_CONNECTION) {
					// try this before pinging to avoid retry
					UnixSocketConnectionSettings settings = (UnixSocketConnectionSettings) connectionSettings;
					String path = settings.getPath();
					String socket = path.replaceAll("unix://", ""); //$NON-NLS-1$ //$NON-NLS-2$
					java.io.File f = new java.io.File(socket);
					if (!f.exists()) {
						throw new org.mandas.docker.client.exceptions.DockerException(
								NLS.bind(Messages.Docker_Daemon_No_Unix_Socket,
										socket));
					}
				}
			}
			if (this.client != null) {
				this.client.ping();
			} else {
				throw new DockerPingConnectionException(NLS.bind(
						Messages.Docker_Daemon_Ping_Failure, this.getName()));
			}
			setState(EnumDockerConnectionState.ESTABLISHED);
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException | IllegalArgumentException e) {
			setState(EnumDockerConnectionState.CLOSED);
			throw new DockerPingConnectionException(NLS.bind(
					Messages.Docker_Daemon_Ping_Failure, this.getName()), e);
		}
	}

	@Override
	protected void finalize() {
		this.finalizing = true;
		close();
	}

	@Override
	public boolean isFinalizing() {
		return this.finalizing;
	}

	@Override
	public void close() {
		// stop and remove all logging threads
		for (String key : loggingThreads.keySet().toArray(new String[0])) {
			LogThread t = loggingThreads.get(key);
			if (t != null) {
				t.kill();
			}
			loggingThreads.remove(key);
		}
		synchronized (clientLock) {
			if (this.client != null) {
				this.client.close();
				this.client = null;
			}
		}
		setState(EnumDockerConnectionState.CLOSED);
	}

	@Override
	public IDockerConnectionInfo getInfo() throws DockerException {
		if (this.client == null) {
			return null;
		}
		try {
			final Info info = this.client.info();
			final Version version = this.client.version();
			return new DockerConnectionInfo(info, version);
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			throw new DockerException(Messages.Docker_General_Info_Failure, e);
		}
	}

	@Override
	public String getName() {
		if (name != null && !name.isEmpty()) {
			return name;
		}
		return Messages.Unnamed;
	}

	@Override
	public boolean setName(final String name) {
		if (!this.name.equals(name)) {
			this.name = name;
			return true;
		}
		return false;
	}

	@Override
	public String getUri() {
		if (this.connectionSettings
				.getType() == BindingType.UNIX_SOCKET_CONNECTION) {
			return ((UnixSocketConnectionSettings) this.connectionSettings)
					.getPath();
		} else {
			return ((TCPConnectionSettings) this.connectionSettings).getHost();
		}
	}

	@Override
	public IDockerConnectionSettings getSettings() {
		return this.connectionSettings;
	}

	@Override
	public boolean setSettings(
			final IDockerConnectionSettings connectionSettings) {
		if (!this.connectionSettings.equals(connectionSettings)) {
			// make sure no other operation using the underneath client occurs
			// while switching the connection settings.
			synchronized (clientLock) {
				this.connectionSettings = connectionSettings;
				if (this.client != null) {
					this.client.close();
				}
				this.state = EnumDockerConnectionState.UNKNOWN;
				this.client = null;
				new Job(NLS.bind(Messages.Open_Connection, this.getUri())) {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							open(true);
							ping();
						} catch (DockerException e) {
							Activator.logErrorMessage(
									NLS.bind(
											Messages.Docker_Daemon_Ping_Failure,
											this.getName()),
									e);
							return Status.CANCEL_STATUS;
						}
						return Status.OK_STATUS;
					}
				};
			}
			// getContainers(true);
			// getImages(true);
			return true;
		}
		return false;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public IDockerVersion getVersion() throws DockerException {
		try {
			Version version = client.version();
			return new DockerVersion(this, version);
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			throw new DockerException(Messages.Docker_General_Info_Failure, e);
		}
	}

	public List<IDockerVolume> getVolumes() throws DockerException {
		List<IDockerVolume> volumeList = new ArrayList<>();
		try {
			VolumeList list = client
					.listVolumes();
			List<Volume> volumes = list.volumes();
			if (volumes != null) {
				for (Volume volume : volumes) {
					DockerVolume v = new DockerVolume(volume);
					volumeList.add(v);
				}
			}
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		} catch (InterruptedException e) {
			return Collections.emptyList();
		}
		return volumeList;
	}

	@Override
	public void addContainerListener(IDockerContainerListener listener) {
		if (containerListeners == null)
			containerListeners = new ListenerList<>(ListenerList.IDENTITY);
		containerListeners.add(listener);
	}

	@Override
	public void removeContainerListener(IDockerContainerListener listener) {
		if (containerListeners != null) {
			containerListeners.remove(listener);
		}
	}

	/**
	 * Get a copy of the client to use in parallel threads for long-standing
	 * operations such as logging or waiting until finished. The user of the
	 * copy should close it when the operation is complete.
	 *
	 * @return copy of client
	 * @throws DockerException
	 *             - general Docker client exception
	 * @see DockerConnection#open(boolean)
	 */
	private DockerClient getClientCopy() throws DockerException {
		try {
			return dockerClientFactory.getClient(this.connectionSettings);
		} catch (DockerCertificateException e) {
			throw new DockerException(NLS.bind(Messages.Open_Connection_Failure,
					this.name, this.getUri()));
		}
	}

	public Closeable getOperationToken() throws DockerException {
		return getClientCopy();
	}

	public void closeOperationToken(Object token) {
		DockerClient client = (DockerClient) token;
		client.close();
	}

	// TODO: we might need something more fine grained, to indicate which
	// container changed, was added or was removed, so we can refresh the UI
	// accordingly.
	public void notifyContainerListeners(List<IDockerContainer> list) {
		if (containerListeners != null) {
			for (IDockerContainerListener listener : containerListeners) {
				listener.listChanged(this, list);
			}
		}
	}

	/**
	 * @return an fixed-size list of all {@link IDockerContainerListener}
	 */
	// TODO: include in IDockerConnection API
	public List<IDockerContainerListener> getContainerListeners() {
		if (this.containerListeners == null) {
			return Collections.emptyList();
		}
		final IDockerContainerListener[] result = new IDockerContainerListener[this.containerListeners
				.size()];
		final Object[] listeners = containerListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			result[i] = (IDockerContainerListener) listeners[i];
		}
		return Arrays.asList(result);
	}

	public Job getActionJob(String id) {
		synchronized (actionLock) {
			Job j = null;
			if (actionJobs != null) {
				return actionJobs.get(id);
			}
			return j;
		}
	}

	public void registerActionJob(String id, Job j) {
		synchronized (actionLock) {
			if (actionJobs == null)
				actionJobs = new HashMap<>();
			actionJobs.put(id, j);
		}
	}

	public void removeActionJob(String id, Job j) {
		synchronized (actionLock) {
			if (actionJobs != null && actionJobs.get(id) == j)
				actionJobs.remove(id);
		}
	}

	@Override
	public List<IDockerContainer> getContainers() {
		return getContainers(false);
	}

	@Override
	public List<IDockerContainer> getContainers(final boolean force) {
		if (this.state == EnumDockerConnectionState.CLOSED) {
			return Collections.emptyList();
		} else if (this.state == EnumDockerConnectionState.UNKNOWN) {
			try {
				open(true);
				getContainers(force);
			} catch (DockerException e) {
				Activator.log(e);
				return Collections.emptyList();
			}

		} else if (!isContainersLoaded() || force) {
			try {
				return listContainers();
			} catch (DockerException e) {
				Activator.log(e);
			}
		}
		return this.containers;
	}

	@Override
	public boolean isContainersLoaded() {
		return this.containers != null;
	}

	/**
	 * Class to perform logging of a container run to a given output stream
	 * (usually a console stream).
	 *
	 */
	private class LogThread extends AbstractKillableThread implements ILogger {
		private String id;
		private DockerClient copyClient;
		private OutputStream outputStream;
		private boolean follow;

		public LogThread(String id, DockerClient copyClient, boolean follow) {
			this.id = id;
			this.copyClient = copyClient;
			this.follow = follow;
		}

		@Override
		public LogThread clone() {
			return new LogThread(id, copyClient, follow);
		}

		@Override
		public void setOutputStream(OutputStream stream) {
			outputStream = stream;
		}

		@Override
		public void execute() throws InterruptedException, IOException {
			LogStream stream = null;
			try {
				// Add timestamps to log based on user preference
				IEclipsePreferences preferences = InstanceScope.INSTANCE
						.getNode("org.eclipse.linuxtools.docker.ui"); //$NON-NLS-1$

				boolean timestamps = preferences.getBoolean(
						"logTimestamp", true); //$NON-NLS-1$


				if (timestamps)
					stream = copyClient.logs(id, LogsParam.follow(),
							LogsParam.stdout(), LogsParam.stderr(),
							LogsParam.timestamps());
				else
					stream = copyClient.logs(id, LogsParam.follow(),
							LogsParam.stdout(), LogsParam.stderr());

				// First time through, don't sleep before showing log data
				int delayTime = 100;

				do {
					Thread.sleep(delayTime);
					// Second time in loop and following, pause a second to
					// allow other threads to do meaningful work
					delayTime = 500;
					while (stream.hasNext()) {
						ByteBuffer b = stream.next().content();
						byte[] bytes = new byte[b.remaining()];
						b.get(bytes);
						if (outputStream != null)
							outputStream.write(bytes);
					}
				} while (follow && !stop);
				listContainers();
			} catch (org.mandas.docker.client.exceptions.DockerException
					| IOException e) {
				Activator.logErrorMessage(
						ProcessMessages.getString("Monitor_Logs_Exception"), e); //$NON-NLS-1$
				throw new InterruptedException();
			} catch (InterruptedException e) {
				kill = true;
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				Activator.logErrorMessage(
						ProcessMessages.getString("Monitor_Logs_Exception"), e); //$NON-NLS-1$
			} finally {
				follow = false;
				copyClient.close(); // we are done with copyClient..dispose
				if (stream != null)
					stream.close();
				if (outputStream != null)
					outputStream.close();
			}
		}
	}

	private List<IDockerContainer> listContainers()
			throws DockerException {
		final Map<String, IDockerContainer> updatedContainersById = new HashMap<>();
		List<IDockerContainer> sortedContainers;
		synchronized (containerLock) {
			try {
				final List<Container> nativeContainers = new ArrayList<>();
				synchronized (clientLock) {
					// Check that client is not null as this connection may have
					// been closed but there is an async request to update the
					// containers list left in the queue
					if (client == null) {
						// in that case the list becomes empty, which is fine is
						// there's no client.
						return Collections.emptyList();
					}
					nativeContainers.addAll(client.listContainers(
							DockerClient.ListContainersParam.allContainers()));
				}
				// We have a list of containers. Now, we translate them to our
				// own
				// core format in case we decide to change the underlying engine
				// in the future.
				for (Container nativeContainer : nativeContainers) {
					// For containers that have exited, make sure we aren't
					// tracking
					// them with a logging thread.
					if (nativeContainer.status() != null && nativeContainer
							.status().startsWith(Messages.Exited_specifier)) {
						synchronized (loggingThreads) {
							if (loggingThreads
									.containsKey(nativeContainer.id())) {
								loggingThreads.get(nativeContainer.id())
										.requestStop();
								loggingThreads.remove(nativeContainer.id());
							}
						}
					}
					// skip containers that are being removed
					if (nativeContainer.status() != null
							&& nativeContainer.status().equals(
									Messages.Removal_In_Progress_specifier)) {
						continue;
					}
					// re-use info from existing container with same id
					if (this.containers != null && this.containersById
							.containsKey(nativeContainer.id())) {
						final IDockerContainer container = this.containersById
								.get(nativeContainer.id());
						updatedContainersById.put(nativeContainer.id(),
								new DockerContainer(this, nativeContainer,
										container.info()));
					} else {
						updatedContainersById.put(nativeContainer.id(),
								new DockerContainer(this, nativeContainer));
					}
				}
			} catch (DockerTimeoutException e) {
				if (isOpen()) {
					Activator.log(Status
							.warning(Messages.Docker_Connection_Timeout, e));
				}
			} catch (org.mandas.docker.client.exceptions.DockerException
					| InterruptedException e) {
				if (isOpen() && e.getCause() != null
						&& e.getCause().getCause() != null && e.getCause()
								.getCause() instanceof ProcessingException) {
					Activator.log(Status.warning(
							Messages.List_Docker_Containers_Failure, e));
				} else {
					throw new DockerException(e);
				}
			} finally {
				this.containersById = updatedContainersById;
				sortedContainers = sort(
						updatedContainersById.values(),
						(container, otherContainer) -> container.name()
								.compareTo(otherContainer.name()));
				this.containers = sortedContainers;
			}
		}
		// perform notification outside of containerLock so we don't have a View
		// causing a deadlock
		// TODO: we should probably notify the listeners only if the containers
		// list changed.
		notifyContainerListeners(sortedContainers);
		return sortedContainers;
	}

	public Set<String> getContainerIdsWithLabels(Map<String, String> labels)
			throws DockerException {
		Set<String> labelSet = new HashSet<>();
		try {
			final List<Container> nativeContainers = new ArrayList<>();
			synchronized (clientLock) {
				// Check that client is not null as this connection may have
				// been closed but there is an async request to filter the
				// containers list left in the queue
				if (client == null) {
					// in that case the list becomes empty, which is fine is
					// there's no client.
					return Collections.emptySet();
				}
				DockerClient clientCopy = getClientCopy();
				DockerClient.ListContainersParam[] parms = new DockerClient.ListContainersParam[2];
				parms[0] = DockerClient.ListContainersParam.allContainers();
				// DockerClient doesn't support multiple labels with its
				// ListContainersParam so we have
				// to do a kludge and put in control chars ourselves and pretend
				// we have a label with no value.
				String separator = ""; //$NON-NLS-1$
				StringBuilder labelString = new StringBuilder();
				for (Entry<String, String> entry : labels.entrySet()) {
					labelString.append(separator);
					if (entry.getValue() == null || "".equals(entry.getValue())) //$NON-NLS-1$
						labelString.append(entry.getKey());
					else {
						labelString.append(
								entry.getKey() + "=" + entry.getValue()); //$NON-NLS-1$
					}
					separator = "\",\""; //$NON-NLS-1$
				}
				parms[1] = DockerClient.ListContainersParam
						.withLabel(labelString.toString());
				nativeContainers.addAll(clientCopy.listContainers(parms));
			}
			// We have a list of containers with labels. Now, we create a Set of
			// ids which contain those labels to use in filtering a list of
			// Containers
			for (Container nativeContainer : nativeContainers) {
				labelSet.add(nativeContainer.id());
			}
		} catch (DockerTimeoutException e) {
			if (isOpen()) {
				Activator.log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
						Messages.Docker_Connection_Timeout, e));
				close();
			}
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			if (isOpen() && e.getCause() != null
					&& e.getCause().getCause() != null
					&& e.getCause().getCause() instanceof ProcessingException) {
				close();
			} else {
				throw new DockerException(e);
			}
		}
		return labelSet;
	}

	/**
	 * Sorts the given values using the given comparator and returns the result
	 * in a {@link List}
	 *
	 * @param values
	 *            the values to sort
	 * @param comparator
	 *            the comparator to use
	 * @return the list of sorted values
	 */
	private <T> List<T> sort(final Collection<T> values,
			final Comparator<T> comparator) {
		final List<T> result = new ArrayList<>(values);
		Collections.sort(result, comparator);
		return result;
	}

	@Override
	public IDockerContainer getContainer(String id) {
		List<IDockerContainer> containers = getContainers();
		for (IDockerContainer container : containers) {
			if (container.id().equals(id)) {
				return container;
			}
		}
		return null;
	}

	@Override
	public IDockerContainerInfo getContainerInfo(final String id) {
		try {
			final ContainerInfo info = client.inspectContainer(id);
			return new DockerContainerInfo(info);
		} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
			Activator.logErrorMessage(
					ProcessMessages.getString("Container_Info_Exception"), e); //$NON-NLS-1$
			return null;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			Activator.log(Status.error(ProcessMessages.getFormattedString(
							"Container_Inspect_Exception", id), //$NON-NLS-1$
					e));
			return null;
		}
	}

	@Override
	public IDockerImageInfo getImageInfo(String id) {
		if (this.client == null) {
			return null;
		}
		try {
			final ImageInfo info = this.client.inspectImage(id);
			return new DockerImageInfo(info);
		} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
			Activator.logErrorMessage(
					ProcessMessages.getString("Image_Info_Exception"), e); //$NON-NLS-1$
			return null;
		} catch (org.mandas.docker.client.exceptions.ImageNotFoundException e) {
			// we might have an id which contains a registry which is invalid
			// for inspectImage() so
			// see if we can find a local image with matching tag
			IDockerImage image = getImageByTag(id);
			if (image != null) {
				return getImageInfo(image.id());
			}
			return null;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			Activator.log(Status.error(ProcessMessages.getFormattedString(
							"Image_Inspect_Exception", id), //$NON-NLS-1$
					e));
			return null;
		}
	}

	@Override
	public void addImageListener(IDockerImageListener listener) {
		if (imageListeners == null)
			imageListeners = new ListenerList<>(ListenerList.IDENTITY);
		imageListeners.add(listener);
	}

	@Override
	public void removeImageListener(IDockerImageListener listener) {
		if (imageListeners != null) {
			imageListeners.remove(listener);
		}
	}

	public void notifyImageListeners(List<IDockerImage> list) {
		if (imageListeners != null) {
			for (IDockerImageListener listener : imageListeners) {
				listener.listChanged(this, list);
			}
		}
	}

	/**
	 * @return an fixed-size list of all {@link IDockerImageListener}
	 */
	// TODO: include in IDockerConnection API
	public List<IDockerImageListener> getImageListeners() {
		if (this.imageListeners == null) {
			return Collections.emptyList();
		}
		final IDockerImageListener[] result = new IDockerImageListener[this.imageListeners
				.size()];
		final Object[] listeners = imageListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			result[i] = (IDockerImageListener) listeners[i];
		}
		return Arrays.asList(result);
	}

	@Override
	public List<IDockerImage> getImages() {
		return getImages(false);
	}

	/**
	 * @return the {@link IDockerImage} identified by the given {@code id} or
	 *         <code>null</code> if none was found.
	 * @param id
	 *            the {@link IDockerImage} id
	 */
	@Override
	public IDockerImage getImage(String id) {
		List<IDockerImage> images = getImages();
		for (IDockerImage image : images) {
			if (image.id().equals(id)) {
				return image;
			}
		}
		return null;
	}

	@Override
	public List<IDockerImage> getImages(final boolean force) {
		List<IDockerImage> latestImages;
		synchronized (imageLock) {
			latestImages = this.images;
		}
		if (this.state == EnumDockerConnectionState.CLOSED) {
			return Collections.emptyList();
		} else if (this.state == EnumDockerConnectionState.UNKNOWN) {
			try {
				open(true);
				latestImages = getImages(force);
			} catch (DockerException e) {
				Activator.log(e);
				return Collections.emptyList();
			}
		} else if (!isImagesLoaded() || force) {
			try {
				latestImages = listImages();
			} catch (DockerException e) {
				synchronized (imageLock) {
					this.images = Collections.emptyList();
				}
				Activator.log(e);
			}
		}
		return latestImages;
	}

	@Override
	public boolean isImagesLoaded() {
		return this.images != null;
	}

	// TODO: remove this method from the API
	@Override
	public List<IDockerImage> listImages() throws DockerException {
		final List<IDockerImage> tempImages = new CopyOnWriteArrayList<>();
		synchronized (imageLock) {
			try {
				final List<Image> nativeImages = new ArrayList<>();
				synchronized (clientLock) {
					// Check that client is not null as this connection may have
					// been closed but there is an async request to update the
					// containers list left in the queue
					if (client == null) {
						// in that case the list becomes empty, which is fine is
						// there's no client.
						return Collections.emptyList();
					}
					nativeImages.addAll(client.listImages(
							DockerClient.ListImagesParam.allImages()));
				}
				// We have a list of images. Now, we translate them to our own
				// core format in case we decide to change the underlying engine
				// in the future. We also look for intermediate and dangling
				// images.
				for (Image nativeImage : nativeImages) {
					final DockerImageQualifier imageQualifier = resolveQualifier(
							nativeImage, nativeImages);
					// return one IDockerImage per raw image
					final List<String> repoTags = (nativeImage
							.repoTags() != null)
									? new ArrayList<>(nativeImage.repoTags())
									: new ArrayList<>();
					Collections.sort(repoTags);
					if (repoTags.isEmpty()) {
						repoTags.add("<none>:<none>"); //$NON-NLS-1$
					}
					final String repo = DockerImage
							.extractRepo(repoTags.get(0));
					final List<String> tags = Arrays
							.asList(DockerImage.extractTag(repoTags.get(0)));
					tempImages.add(new DockerImage(this, repoTags, repo, tags,
							nativeImage.id(), nativeImage.parentId(),
							nativeImage.created(), nativeImage.size(),
							nativeImage.virtualSize(), imageQualifier));
				}
			} catch (org.mandas.docker.client.exceptions.DockerTimeoutException e) {
				if (isOpen()) {
					Activator.log(Status
							.warning(Messages.Docker_Connection_Timeout, e));
				}
			} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
				throw new DockerException(e);
			} catch (org.mandas.docker.client.exceptions.DockerException
					| InterruptedException e) {
				if (isOpen() && e.getCause() != null
						&& e.getCause().getCause() != null && e.getCause()
								.getCause() instanceof ProcessingException) {
					Activator.log(Status
							.warning(Messages.List_Docker_Images_Failure, e));
				} else {
					throw new DockerException(
							NLS.bind(Messages.List_Docker_Images_Failure,
									this.getName()),
							e);
				}
			} finally {
				this.images = tempImages;
			}
		}
		// Perform notification outside of lock so that listener doesn't cause a
		// deadlock to occur
		notifyImageListeners(tempImages);
		return tempImages;
	}

	/**
	 * Resolves the {@link DockerImageQualifier} for the given
	 * {@code nativeImage} in the context of all {@code nativeImages}
	 *
	 * @param nativeImage
	 *            the image to analyze
	 * @param nativeImages
	 *            all known images
	 * @return the corresponding {@link DockerImageQualifier}
	 */
	private static DockerImageQualifier resolveQualifier(
			final Image nativeImage, final List<Image> nativeImages) {
		final boolean hasTag = !(nativeImage.repoTags() == null
				|| (nativeImage.repoTags().size() == 1
						&& nativeImage.repoTags().contains("<none>:<none>"))); //$NON-NLS-1$
		final boolean hasChildImage = nativeImages.stream()
				.anyMatch(i -> nativeImage.id().equals(i.parentId()));
		// imtermediate image
		if (!hasTag && hasChildImage) {
			return DockerImageQualifier.INTERMEDIATE;
		}
		// dangling image
		if (!hasTag && !hasChildImage) {
			return DockerImageQualifier.DANGLING;
		}
		return DockerImageQualifier.TOP_LEVEL;
	}

	@Override
	public boolean hasImage(final String repository, final String tag) {
		for (IDockerImage image : getImages()) {
			for (String repoTag : image.repoTags()) {
				String tagExpr = (tag != null && !tag.isEmpty()) ? ":" + tag //$NON-NLS-1$
						: ""; //$NON-NLS-1$
				if (repoTag.equals(repository + tagExpr)) {
					return true;
				}
			}
		}
		return false;
	}

	public IDockerImage getImageByTag(final String tag) {
		for (IDockerImage image : getImages()) {
			for (String repoTag : image.repoTags()) {
				if (repoTag.equals(tag)) {
					return image;
				}
			}
		}
		return null;
	}

	@Override
	public IDockerProgressHandler getDefaultBuildImageProgressHandler(
			String image, int lines) {
		return new DefaultImageBuildProgressHandler(this, image, lines);
	}

	@Override
	public IDockerProgressHandler getDefaultPullImageProgressHandler(
			String image) {
		return new DefaultImagePullProgressHandler(this, image);
	}

	@Override
	public IDockerProgressHandler getDefaultPushImageProgressHandler(
			String image) {
		return new DefaultImagePushProgressHandler(this, image);
	}

	@Override
	public void pullImage(final String id, final IDockerProgressHandler handler)
			throws DockerException, InterruptedException {
		try {
			DockerProgressHandler d = new DockerProgressHandler(handler);
			client.pull(id, d);
			listImages();
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void pullImage(final String imageId, final IRegistryAccount info, final IDockerProgressHandler handler)
			throws DockerException, InterruptedException, DockerCertificateException {
		try {
			final DockerClient client = dockerClientFactory
					.getClient(this.connectionSettings, info);
			final DockerProgressHandler d = new DockerProgressHandler(handler);
			client.pull(imageId, d);
			listImages();
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public List<IDockerImageSearchResult> searchImages(final String term) throws DockerException {
		try {
			final List<ImageSearchResult> searchResults = client.searchImages(term);
			final List<IDockerImageSearchResult> results = new ArrayList<>();
			for(ImageSearchResult r : searchResults) {
				if (r.name().contains(term)) {
					results.add(new DockerImageSearchResult(r.description(),
							r.official(), r.automated(), r.name(),
							r.starCount()));
				}
			}
			return results;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void pushImage(final String name, final IDockerProgressHandler handler)
			throws DockerException, InterruptedException {
		try {
			DockerProgressHandler d = new DockerProgressHandler(handler);
			client.push(name, d);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void pushImage(final String name, final IRegistryAccount info, final IDockerProgressHandler handler)
			throws DockerException, InterruptedException {
		try {
			final DockerClient client = dockerClientFactory
					.getClient(this.connectionSettings, info);
			final DockerProgressHandler d = new DockerProgressHandler(handler);
			client.push(name, d);
		} catch (org.mandas.docker.client.exceptions.DockerException
				| DockerCertificateException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void removeImage(final String name) throws DockerException,
			InterruptedException {
		try {
			client.removeImage(name, true, false);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void removeTag(final String tag) throws DockerException,
			InterruptedException {
		try {
			client.removeImage(tag, false, false);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void tagImage(final String name, final String newTag) throws DockerException,
			InterruptedException {
		tagImage(name, newTag, false);
	}

	/**
	 * Adds a tag to an existing image while specifying the <code>force</code>
	 * flag.
	 *
	 * @param name
	 *            the image id
	 * @param newTag
	 *            the new tag to add to the given image
	 * @param force
	 *            the {@code force} flag to force the operation if
	 *            <code>true</code>.
	 * @throws DockerException
	 *             in case of underlying problem (server error)
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	// TODO: add to the API in version 3.0.0
	public void tagImage(final String name, final String newTag,
			final boolean force) throws DockerException, InterruptedException {
		try {
			client.tag(name, newTag, force);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public String buildImage(final IPath path,
			final IDockerProgressHandler handler)
					throws DockerException, InterruptedException {
		try {
			final DockerProgressHandler d = new DockerProgressHandler(handler);
			final java.nio.file.Path p = FileSystems.getDefault()
					.getPath(path.makeAbsolute().toOSString());
			String res = getClientCopy().build(p, d,
					BuildParam.create("forcerm", "true")); //$NON-NLS-1$ //$NON-NLS-2$
			return res;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| IOException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public String buildImage(final IPath path, final String name,
			final IDockerProgressHandler handler)
					throws DockerException, InterruptedException {
		try {
			DockerProgressHandler d = new DockerProgressHandler(handler);
			java.nio.file.Path p = FileSystems.getDefault().getPath(
					path.makeAbsolute().toOSString());
			String res = getClientCopy().build(p, name, d,
					BuildParam.create("forcerm", "true")); //$NON-NLS-1$ $NON-NLS-2$
			return res;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| IOException e) {
			throw new DockerException(e);
		}
	}

	/**
	 * Builds an {@link IDockerImage}
	 *
	 * @param path
	 *            path to the build context
	 * @param name
	 *            optional name and tag of the image to build
	 * @param handler
	 *            progress handler
	 * @param buildOptions
	 *            build options
	 * @return the id of the {@link IDockerImage} that was build
	 * @throws DockerException
	 *             if building image failed
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	// TODO: add this method in the public interface
	public String buildImage(final IPath path, final String name,
			final IDockerProgressHandler handler,
			final Map<String, Object> buildOptions)
					throws DockerException, InterruptedException {
		try {
			final DockerProgressHandler d = new DockerProgressHandler(handler);
			final java.nio.file.Path p = FileSystems.getDefault()
					.getPath(path.makeAbsolute().toOSString());
			String res = getClientCopy().build(p, name, d,
					getBuildParameters(buildOptions));
			return res;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| IOException e) {
			throw new DockerException(e);
		}
	}

	/**
	 * Builds an {@link IDockerImage}
	 *
	 * @param path
	 *            path to the build context
	 * @param name
	 *            optional name and tag of the image to build
	 * @param dockerFileName
	 *            name of dockerfile
	 * @param handler
	 *            progress handler
	 * @param buildOptions
	 *            build options
	 * @return the id of the {@link IDockerImage} that was build
	 * @throws DockerException
	 *             if building image failed
	 * @throws InterruptedException
	 *             if the thread was interrupted
	 */
	// TODO: add this method in the public interface
	public String buildImage(final IPath path, final String name,
			final String dockerFileName, final IDockerProgressHandler handler,
			final Map<String, Object> buildOptions)
			throws DockerException, InterruptedException {
		try {
			final DockerProgressHandler d = new DockerProgressHandler(handler);
			final java.nio.file.Path p = FileSystems.getDefault()
					.getPath(path.makeAbsolute().toOSString());
			String res = getClientCopy().build(p, name, dockerFileName, d,
					getBuildParameters(buildOptions));
			return res;
		} catch (org.mandas.docker.client.exceptions.DockerException
				| IOException e) {
			throw new DockerException(e);
		}
	}

	/**
	 * Converts the given {@link Map} of build options into an array of
	 * {@link BuildParameter} when the build options are set a value different
	 * from the default value.
	 *
	 * @param buildOptions
	 *            the build options
	 * @return an array of relevant {@link BuildParameter}, an empty array if
	 *         the given {@code buildOptions} is empty or <code>null</code>.
	 */
	private BuildParam[] getBuildParameters(
			final Map<String, Object> buildOptions) {
		if (buildOptions == null) {
			return new BuildParam[0];
		}
		final List<BuildParam> buildParameters = new ArrayList<>();
		for (Entry<String, Object> entry : buildOptions.entrySet()) {
			final Object optionName = entry.getKey();
			final Object optionValue = entry.getValue();

			if (optionName.equals(IDockerImageBuildOptions.QUIET_BUILD)
					&& optionValue.equals(true)) {
				buildParameters.add(BuildParam.create("q", "true")); //$NON-NLS-1$ $NON-NLS-2$
			} else if (optionName.equals(IDockerImageBuildOptions.NO_CACHE)
					&& optionValue.equals(true)) {
				buildParameters.add(BuildParam.create("nocache", "true")); //$NON-NLS-1$ $NON-NLS-2$
			} else if (optionName
					.equals(IDockerImageBuildOptions.RM_INTERMEDIATE_CONTAINERS)
					&& optionValue.equals(false)) {
				buildParameters.add(BuildParam.create("rm", "false")); //$NON-NLS-1$ $NON-NLS-2$
			} else if (optionName
					.equals(IDockerImageBuildOptions.FORCE_RM_INTERMEDIATE_CONTAINERS)
					&& optionValue.equals(true)) {
				buildParameters.add(BuildParam.create("forcerm", "true")); //$NON-NLS-1$ $NON-NLS-2$
			}
		}
		return buildParameters.toArray(new BuildParam[0]);
	}

	public void save() {
		// Currently we have to save all clouds instead of just this one
		DockerConnectionManager.getInstance().saveConnections();
	}

	@Override
	public String createContainer(final IDockerContainerConfig c,
			final IDockerHostConfig hc) throws DockerException,
			InterruptedException {
		return createContainer(c, hc, null);
	}

	@Override
	public String createContainer(final IDockerContainerConfig c,
			IDockerHostConfig hc,
			final String containerName)
			throws DockerException, InterruptedException {

		try {
			HostConfig.Builder hbuilder = HostConfig.builder()
					.containerIdFile(hc.containerIDFile())
					.publishAllPorts(hc.publishAllPorts())
					.privileged(hc.privileged()).networkMode(hc.networkMode())
					.readonlyRootfs(((DockerHostConfig) hc).readonlyRootfs());
			if (((DockerHostConfig) hc).tmpfs() != null) {
				hbuilder.tmpfs(Map.copyOf(((DockerHostConfig) hc).tmpfs()));
			}
			if (((DockerHostConfig) hc).capAdd() != null) {
				hbuilder.capAdd(((DockerHostConfig) hc).capAdd());
			}
			if (((DockerHostConfig) hc).capDrop() != null) {
				hbuilder.capDrop(((DockerHostConfig) hc).capDrop());
			}
			if (hc.binds() != null)
				hbuilder.binds(hc.binds());
			if (hc.dns() != null)
				hbuilder.dns(hc.dns());
			if (hc.dnsSearch() != null)
				hbuilder.dnsSearch(hc.dnsSearch());
			if (hc.links() != null)
				hbuilder.links(hc.links());
			if (hc.lxcConf() != null) {
				List<IDockerConfParameter> lxcconf = hc.lxcConf();
				ArrayList<LxcConfParameter> lxcreal = new ArrayList<>();
				for (IDockerConfParameter param : lxcconf) {
					// TODO: Fix This
				}
				hbuilder.lxcConf(lxcreal);
			}
			if (hc.portBindings() != null) {
				Map<String, List<IDockerPortBinding>> bindings = hc
						.portBindings();
				HashMap<String, List<PortBinding>> realBindings = new HashMap<>();

				for (Entry<String, List<IDockerPortBinding>> entry : bindings
						.entrySet()) {
					String key = entry.getKey();
					List<IDockerPortBinding> bindingList = entry.getValue();
					ArrayList<PortBinding> newList = new ArrayList<>();
					for (IDockerPortBinding binding : bindingList) {
						newList.add(PortBinding.of(binding.hostIp(),
								binding.hostPort()));
					}
					realBindings.put(key, newList);
				}
				hbuilder.portBindings(realBindings);
			}
			if (hc.volumesFrom() != null) {
				hbuilder.volumesFrom(hc.volumesFrom());
			}
			if (hc.securityOpt() != null) {
				hbuilder.securityOpt(hc.securityOpt());
			}
			// FIXME: add the 'memory()' method in the IDockerHostConfig
			// interface
			if (((DockerHostConfig) hc).memory() != null) {
				hbuilder.memory(((DockerHostConfig) hc).memory());
			}
			// FIXME: add the 'cpuShares()' method in the IDockerHostConfig
			// interface
			if (((DockerHostConfig) hc).cpuShares() != null
					&& ((DockerHostConfig) hc).cpuShares().longValue() > 0) {
				hbuilder.cpuShares(((DockerHostConfig) hc).cpuShares());
			}

			ContainerConfig.Builder builder = ContainerConfig.builder()
					.hostname(c.hostname()).domainname(c.domainname())
					.user(c.user()).attachStdin(c.attachStdin())
					.attachStdout(c.attachStdout())
					.attachStderr(c.attachStderr()).tty(c.tty())
					.openStdin(c.openStdin()).stdinOnce(c.stdinOnce())
					.cmd(c.cmd()).image(c.image())
					.hostConfig(hbuilder.build())
					.workingDir(c.workingDir())
					.labels(c.labels())
					.networkDisabled(c.networkDisabled());
			// For those fields that are Collections and not set, they will be null.
			// We can't use their values to set the builder's fields as they are
			// expecting non-null Collections to copy over. In those cases, we just
			// don't set those fields in the builder.
			if (c.portSpecs() != null) {
				builder = builder.portSpecs(c.portSpecs());
			}
			if (c.exposedPorts() != null) {
				builder = builder.exposedPorts(c.exposedPorts());
			}
			if (c.env() != null) {
				builder = builder.env(c.env());
			}
			if (c.volumes() != null) {
				builder = builder.volumes(c.volumes().keySet());
			}
			if (c.entrypoint() != null) {
				builder = builder.entrypoint(c.entrypoint());
			}
			if (c.onBuild() != null) {
				builder = builder.onBuild(c.onBuild());
			}

			// create container with default random name if an empty/null
			// containerName argument was passed
			final ContainerCreation creation = client
					.createContainer(builder.build(),
					(containerName != null && !containerName.isEmpty())
							? containerName : null);
			final String id = creation.id();
			// force a refresh of the current containers to include the new one
			listContainers();
			return id;
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void stopContainer(final String id) throws DockerException,
			InterruptedException {
		try {
			// stop container or kill after 10 seconds
			client.stopContainer(id, 10); // allow up to 10 seconds to stop
			synchronized (loggingThreads) {
				if (loggingThreads.containsKey(id)) {
					loggingThreads.get(id).kill();
					loggingThreads.remove(id);
				}
			}
			// list of containers needs to be updated once the given container is stopped, to reflect it new state.
			listContainers();
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void killContainer(final String id) throws DockerException,
			InterruptedException {
		try {
			// kill container
			client.killContainer(id);
			synchronized (loggingThreads) {
				if (loggingThreads.containsKey(id)) {
					loggingThreads.get(id).kill();
					loggingThreads.remove(id);
				}
			}
			listContainers(); // update container list
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
			Activator.log(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void pauseContainer(final String id) throws DockerException,
			InterruptedException {
		try {
			// pause container
			client.pauseContainer(id);
			listContainers(); // update container list
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void unpauseContainer(final String id, final OutputStream stream)
			throws DockerException, InterruptedException {
		try {
			// unpause container
			client.unpauseContainer(id);
			if (stream != null) {
				synchronized (loggingThreads) {
					LogThread t = loggingThreads.get(id);
					if (t == null || !t.isAlive()) {
						t = new LogThread(id, getClientCopy(), true);
						loggingThreads.put(id, t);
						t.setOutputStream(stream);
						t.start();
					} else {
						// we aren't going to use the stream given...close it
						try {
							stream.close();
						} catch (IOException e) {
							// do nothing...we tried to close the stream
						}
					}
				}
			}
			listContainers(); // update container list
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void removeContainer(final String id) throws DockerException,
			InterruptedException {
		try {
			// kill container
			client.removeContainer(id);
			listContainers(); // update container list
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	private String getCmdString(IDockerContainerInfo info) {
		if (info == null) {
			return "";
		}
		List<String> cmd = info.config().cmd();
		StringBuilder b = new StringBuilder();
		cmd.stream().forEach(s -> b.append(s + " "));
		b.deleteCharAt(b.length() - 1);
		return b.toString();
	}

	@Override
	public void startContainer(final String id, final OutputStream stream)
			throws DockerException, InterruptedException {
		startContainer(client, id, stream);
	}

	public void startContainer(final Closeable token, final String id,
			final OutputStream stream)
			throws DockerException, InterruptedException {
		final IDockerContainerInfo containerInfo = getContainerInfo(id);
		if (containerInfo == null) {
			throw new DockerException(DockerMessages
					.getFormattedString("DockerContainerNotFound.error", id)); //$NON-NLS-1$
		}
		try {
			// start container
			((DockerClient) token).startContainer(id);
			// Log the started container if a stream is provided
			if (stream != null && containerInfo != null
					&& containerInfo.config() != null
					&& !containerInfo.config().tty()) {
				// display logs for container
				synchronized (loggingThreads) {
					LogThread t = loggingThreads.get(id);
					if (t == null || !t.isAlive()) {
						t = new LogThread(id, getClientCopy(), true);
						loggingThreads.put(id, t);
						t.setOutputStream(stream);
						t.start();
					}
				}
			}
			// list of containers needs to be refreshed once the container started, to reflect it new state.
			listContainers();
		} catch (ContainerNotFoundException e) {
			// if we get here, it means that the command failed...the actual
			// message is buried in the throwable cause and isn't actually
			// clearly stated so report there was a problem starting the command
			throw new DockerException(DockerMessages.getFormattedString(
					"DockerStartContainer.error", getCmdString(containerInfo))); //$NON-NLS-1$
		} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
			if (e.status() != 304) {
				throw new DockerException(e);
			}
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void startContainer(String id, String loggingId, OutputStream stream)
			throws DockerException, InterruptedException {
		final IDockerContainerInfo containerInfo = getContainerInfo(id);
		if (containerInfo == null) {
			throw new DockerException(DockerMessages
					.getFormattedString("DockerContainerNotFound.error", id)); //$NON-NLS-1$
		}
		try {
			// start container with host config
			client.startContainer(id);
			// Log the started container based on user preference
			// Log the started container based on user preference
			// Log the started container based on user preference
			IEclipsePreferences preferences = InstanceScope.INSTANCE
					.getNode("org.eclipse.linuxtools.docker.ui"); //$NON-NLS-1$

			boolean autoLog = preferences.getBoolean("autoLogOnStart", true); //$NON-NLS-1$

			if (autoLog && !containerInfo.config().tty()) {
				synchronized (loggingThreads) {
					LogThread t = loggingThreads.get(loggingId);
					if (t == null || !t.isAlive()) {
						t = new LogThread(id, getClientCopy(), true);
						loggingThreads.put(loggingId, t);
						t.setOutputStream(stream);
						t.start();
					}
				}
			}
			// update container list
			listContainers();
		} catch (ContainerNotFoundException e) {
			// if we get here, it means that the command failed...the actual
			// message is buried in the throwable cause and isn't actually
			// clearly stated so report there was a problem starting the command
			throw new DockerException(DockerMessages.getFormattedString(
					"DockerStartContainer.error", getCmdString(containerInfo))); //$NON-NLS-1$
		} catch (org.mandas.docker.client.exceptions.DockerRequestException e) {
			if (e.status() != 304) {
				throw new DockerException(e);
			}
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void restartContainer(String id, int secondsToWait)
			throws DockerException, InterruptedException {
		restartContainer(id, secondsToWait, null);
	}

	public void restartContainer(final String id, int secondsToWait,
			final OutputStream stream)
			throws DockerException, InterruptedException {
		try {
			// restart container
			client.restartContainer(id, secondsToWait);
			// Log the started container if a stream is provided
			final IDockerContainerInfo containerInfo = getContainerInfo(id);
			if (stream != null && containerInfo != null
					&& containerInfo.config() != null
					&& !containerInfo.config().tty()) {
				// display logs for container
				synchronized (loggingThreads) {
					LogThread t = loggingThreads.get(id);
					if (t == null || !t.isAlive()) {
						t = new LogThread(id, getClientCopy(), true);
						loggingThreads.put(id, t);
						t.setOutputStream(stream);
						t.start();
					}
				}
			}
			// list of containers needs to be refreshed once the container
			// started, to reflect it new state.
			listContainers();
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}


	@Override
	public void commitContainer(final String id, final String repo, final String tag,
			final String comment, final String author) throws DockerException {
		ContainerInfo info;
		try {
			info = client.inspectContainer(id);
			client.commitContainer(id, repo, tag, info.config(), comment,
					author);
			// update images list
			// FIXME: are we refreshing the list of images twice ?
			listImages();
			getImages(true);
		} catch (org.mandas.docker.client.exceptions.DockerException
				| InterruptedException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public InputStream copyContainer(final String id, final String path)
			throws DockerException, InterruptedException {
		InputStream stream;
		try {
			DockerClient copy = getClientCopy();
			stream = copy.archiveContainer(id, path);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
		return stream;
	}

	public InputStream copyContainer(final Closeable token,
			final String id, final String path)
			throws DockerException, InterruptedException {
		InputStream stream;
		DockerClient clientCopy = (DockerClient) token;
		try {
			stream = clientCopy.archiveContainer(id, path);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
		return stream;
	}

	@Override
	public List<IDockerContainerChange> containerChanges(final String id)
			throws DockerException, InterruptedException {
		List<IDockerContainerChange> containerChanges = new ArrayList<>();
		try {
			DockerClient copy = getClientCopy();
			List<ContainerChange> changes = copy.inspectContainerChanges(id);
			for (ContainerChange change : changes) {
				containerChanges.add(new DockerContainerChange(change.path(),
						change.kind()));
			}
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
		return containerChanges;
	}

	public boolean isLocal() {
		if (isLocalConnection != null)
			return isLocalConnection.booleanValue();
		isLocalConnection = false;
		if (connectionSettings
				.getType() == BindingType.UNIX_SOCKET_CONNECTION) {
			isLocalConnection = true;
		} else if (connectionSettings.getType() == BindingType.TCP_CONNECTION) {
			TCPConnectionSettings settings = (TCPConnectionSettings) connectionSettings;
			try {
				InetAddress addr = InetAddress.getByName(settings.getAddr());
				if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
					isLocalConnection = true;
				} else {
					// Check if the address is defined on any interface
					try {
						isLocalConnection = NetworkInterface
								.getByInetAddress(addr) != null;
					} catch (SocketException e) {
						isLocalConnection = false;
					}
				}
			} catch (UnknownHostException e) {
				// should not happen
				Activator.log(e);
			}
		}
		return isLocalConnection.booleanValue();
	}

	@Override
	public void copyToContainer(final String directory, final String id,
			final String path)
			throws DockerException, InterruptedException, IOException {
		try {
			DockerClient copy = getClientCopy();
			java.nio.file.Path dirPath = FileSystems.getDefault()
					.getPath(directory);
			copy.copyToContainer(dirPath, id, path);
			copy.close(); /* dispose of client copy now that we are done */
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	public void copyToContainer(final Closeable token, final String directory,
			final String id, final String path)
			throws DockerException, InterruptedException, IOException {
		try {
			DockerClient copy = (DockerClient) token;
			java.nio.file.Path dirPath = FileSystems.getDefault()
					.getPath(directory);
			copy.copyToContainer(dirPath, id, path);
			copy.close(); /* dispose of client copy now that we are done */
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public int auth(IRegistryAccount cfg)
			throws DockerException, InterruptedException {
		try {
			RegistryAuth authConfig = RegistryAuth.builder()
					.username(new String(cfg.getUsername()))
					.password(cfg.getPassword() != null
							? new String(cfg.getPassword()) : null)
					.email(new String(cfg.getEmail()))
					.serverAddress(new String(cfg.getServerAddress())).build();
			return client.auth(authConfig);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	public EnumDockerLoggingStatus loggingStatus(final String id) {
		synchronized (loggingThreads) {
			LogThread t = loggingThreads.get(id);
			if (t == null)
				return EnumDockerLoggingStatus.LOGGING_NONE;
			if (t.isAlive())
				return EnumDockerLoggingStatus.LOGGING_ACTIVE;
			return EnumDockerLoggingStatus.LOGGING_COMPLETE;
		}
	}

	@Override
	public void stopLoggingThread(final String id) {
		synchronized (loggingThreads) {
			LogThread t = loggingThreads.get(id);
			if (t != null)
				t.requestStop();
		}
		while (loggingStatus(id) == EnumDockerLoggingStatus.LOGGING_ACTIVE) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Activator.log(e);
			}
		}
	}

	@Override
	public void logContainer(final String id, final OutputStream stream)
			throws DockerException, InterruptedException {
		try {
			// Figure out if we are logging a running container or not
			// Pass that info to see whether the LogThread should just terminate
			// or keep running
			synchronized (loggingThreads) {
				ContainerInfo info = client.inspectContainer(id);
				LogThread t = loggingThreads.get(id);
				if (t == null || !t.isAlive()) {
					t = new LogThread(id, getClientCopy(), info.state()
							.running());
					loggingThreads.put(id, t);
					t.setOutputStream(stream);
					t.start();
				} else {
					// we aren't going to use the stream given...close it
					try {
						stream.close();
					} catch (IOException e) {
						// do nothing...we tried to close the stream
					}
				}
			}
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void attachLog(final String id, final OutputStream out,
			final OutputStream err)
			throws DockerException, InterruptedException, IOException {
		DockerClient copyClient;
		try {
			copyClient = getClientCopy();
			LogStream stream = copyClient.logs(id, LogsParam.follow(),
					LogsParam.stdout(), LogsParam.stderr());
			stream.attach(out, err);
			stream.close();
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	public void attachLog(final Closeable token, final String id,
			final OutputStream out, final OutputStream err)
			throws DockerException, InterruptedException, IOException {
		try {
			LogStream stream = ((DockerClient) token).logs(id,
					LogsParam.follow(), LogsParam.stdout(), LogsParam.stderr());
			stream.attach(out, err);
			stream.close();
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public IDockerContainerExit waitForContainer(final String id)
			throws DockerException, InterruptedException {
		try {
			// wait for container to exit
			DockerClient copy = getClientCopy();
			ContainerExit x = copy.waitContainer(id);
			DockerContainerExit exit = new DockerContainerExit(x.statusCode());
			listContainers(); // update container list
			copy.close(); // dispose of copy now we are finished
			return exit;
		} catch (ContainerNotFoundException e) {
			throw new DockerContainerNotFoundException(e);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	public void attachCommand(final String id, final InputStream in,
			final DockerConsoleOutputStream out)
					throws DockerException {
		attachCommand(client, id, in, out, true);
	}

	public void attachCommand(final Closeable token, final String id,
			final InputStream in, final DockerConsoleOutputStream out,
			final boolean openTTY)
			throws DockerException {

		final byte[] prevCmd = new byte[1024];
		try {
			final LogStream pty_stream = ((DockerClient) token).attachContainer(
					id,
					AttachParameter.STDIN, AttachParameter.STDOUT,
					AttachParameter.STDERR, AttachParameter.STREAM,
					AttachParameter.LOGS);
			final IDockerContainerInfo info = getContainerInfo(id);
			final boolean isTtyEnabled = info.config().tty();
			final boolean isOpenStdin = info.config().openStdin();

			if (isTtyEnabled && openTTY) {
				openTerminal(pty_stream, info.name(), out);
			}

			// Data from the given input stream
			// Written to container's STDIN
			Thread t_in = new Thread(() -> {
				byte[] buff = new byte[1024];
				int n;
				try {
					WritableByteChannel pty_out = HttpHijackWorkaround
							.getOutputStream(pty_stream, getUri());
					while ((n = in.read(buff)) != -1
							&& getContainerInfo(id).state().running()) {
						synchronized (prevCmd) {
							pty_out.write(ByteBuffer.wrap(buff, 0, n));
							for (int i = 0; i < prevCmd.length; i++) {
								prevCmd[i] = buff[i];
							}
						}
						buff = new byte[1024];
					}
				} catch (Exception e) {
				}
			});

			if (!isTtyEnabled && isOpenStdin) {
				t_in.start();
			}
		} catch (Exception e) {
			throw new DockerException(e);
		}
	}

	public void attachContainerOutput(final Closeable token, final String id,
			OutputStream stdout, OutputStream stderr) throws DockerException {
		try {
			final LogStream logstream = ((DockerClient) token).attachContainer(
					id, AttachParameter.STDOUT, AttachParameter.STDERR,
					AttachParameter.STREAM);
			logstream.attach(stdout, stderr);
		} catch (Exception e) {
			throw new DockerException(e);
		}
	}

	public List<ContainerFileProxy> readContainerDirectory(final String id,
			final String path) throws DockerException {
		List<ContainerFileProxy> childList = new ArrayList<>();
		String dirListing = null;
		try {
			DockerClient copyClient = getClientCopy();
			final ExecCreation execCreation = copyClient.execCreate(id,
					new String[] { "/bin/sh", "-c", //$NON-NLS-1$ //$NON-NLS-2$
							"ls -l -F -Q " + path + "/" }, //$NON-NLS-1$
					ExecCreateParam.attachStdout(),
					ExecCreateParam.attachStderr());
			final String execId = execCreation.id();
			final LogStream pty_stream = copyClient.execStart(execId);

			try {
				dirListing = pty_stream.readFully();
			} finally {
				if (pty_stream != null)
					pty_stream.close();
				copyClient.close();
			}
		} catch (Exception e) {
			if (e.getCause() instanceof IOException) {
				// ugly hack as we often get Connection reset by peer exceptions
				// so retry
				return readContainerDirectory(id, path);
			}
			// e.printStackTrace();
			// TODO: Should probably rethrow
		}

		final String[] lines = dirListing.split("\\r?\\n");
		for (String line : lines) {
			processDirectoryLine(line, path, childList);
		}
		return childList;
	}

	private String[] tokenize(String line) {
		String regex = "(\"[^\"]*\"\\S?)|(\\S+)";
		List<String> result = new ArrayList<>();

		Matcher m = Pattern.compile(regex).matcher(line);
		while (m.find()) {
			if (m.group(1) != null) {
				result.add(m.group(1));
			} else {
				result.add(m.group(2));
			}
		}
		return result.toArray(new String[0]);
	}

	private void processDirectoryLine(String line, String path,
			List<ContainerFileProxy> childList) {
		if (line.trim().startsWith("total")) //$NON-NLS-1$
			return; // ignore the total line
		String[] token = tokenize(line);
		boolean isDirectory = token[0].startsWith("d"); //$NON-NLS-1$
		boolean isLink = token[0].startsWith("l"); //$NON-NLS-1$
		if (token.length > 8) {
			// Non-Link ends with "filename"
			// Link ends with "filename" -> "Target"
			String name = token[token.length - (isLink ? 3 : 1)];
			// remove quotes and any indicator char
			name = name.substring(1,
					name.length() - (name.endsWith("\"") ? 1 : 2));

			if (isLink) {
				String linkname = token[token.length - 1];
				if (linkname.endsWith("/")) { //$NON-NLS-1$
					isDirectory = true;
				}
				linkname = linkname.substring(1,
						linkname.length() - (linkname.endsWith("\"") ? 1 : 2));

				childList.add(new ContainerFileProxy(path, name, isDirectory,
						isLink, linkname));
			} else {
				childList.add(new ContainerFileProxy(path, name, isDirectory));
			}
		}
	}

	public void execShell(final String id) throws DockerException {
		try {
			final ExecCreation execCreation = client.execCreate(id,
					new String[] { "/bin/sh" }, //$NON-NLS-1$
					ExecCreateParam.attachStdout(),
					ExecCreateParam.attachStderr(),
					ExecCreateParam.attachStdin(),
					ExecCreateParam.tty());
			final String execId = execCreation.id();

			final LogStream pty_stream = client.execStart(execId,
					DockerClient.ExecStartParameter.TTY);
			final IDockerContainerInfo info = getContainerInfo(id);
			openTerminal(pty_stream, info.name() + " [shell]", null); //$NON-NLS-1$
		} catch (Exception e) {
			throw new DockerException(e);
		}
	}

	private class TerminalOutputMonitorListener
			implements ITerminalServiceOutputStreamMonitorListener {

		private DockerConsoleOutputStream consoleOutputStream;

		public TerminalOutputMonitorListener(DockerConsoleOutputStream out) {
			this.consoleOutputStream = out;
		}

		@Override
		public void onContentReadFromStream(byte[] byteBuffer, int bytesRead) {
			try {
				if (consoleOutputStream != null) {
					consoleOutputStream.write(byteBuffer, 0, bytesRead);
				}
			} catch (IOException e) {
				Activator.log(e);
			}
		}

	}

	private void openTerminal(LogStream pty_stream, String name,
			DockerConsoleOutputStream out) throws DockerException {
		try {
			OutputStream tout = noBlockingOutputStream(
					HttpHijackWorkaround.getOutputStream(pty_stream, getUri()));
			InputStream tin = HttpHijackWorkaround.getInputStream(pty_stream);

			TerminalOutputMonitorListener monitor = new TerminalOutputMonitorListener(out);

			// org.eclipse.tm.terminal.connector.ssh.controls.SshWizardConfigurationPanel
			Map<String, Object> properties = new HashMap<>();
			properties.put(ITerminalsConnectorConstants.PROP_DELEGATE_ID,
					"org.eclipse.tm.terminal.connector.streams.launcher.streams"); //$NON-NLS-1$
			properties.put(
					ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID,
					"org.eclipse.tm.terminal.connector.streams.StreamsConnector"); //$NON-NLS-1$
			properties.put(ITerminalsConnectorConstants.PROP_TITLE, name);
			properties.put(ITerminalsConnectorConstants.PROP_LOCAL_ECHO, false);
			properties.put(ITerminalsConnectorConstants.PROP_FORCE_NEW, true);
			properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDIN, tout);
			properties.put(ITerminalsConnectorConstants.PROP_STREAMS_STDOUT, tin);
			properties.put(ITerminalsConnectorConstants.PROP_STDERR_LISTENERS, new ITerminalServiceOutputStreamMonitorListener[] {monitor});
			properties.put(ITerminalsConnectorConstants.PROP_STDOUT_LISTENERS,
					new ITerminalServiceOutputStreamMonitorListener[] {
							monitor });
			properties.put(ITerminalsConnectorConstants.PROP_DATA, pty_stream);
			/*
			 * The JVM will call finalize() on 'pty_stream' (LogStream)
			 * since we hold no references to it (although we do hold
			 * references to one of its heavily nested fields. The
			 * LogStream overrides finalize() to close the stream being
			 * used so we must preserve a reference to it.
			 */
			properties.put("PREVENT_JVM_GC_FINALIZE", pty_stream); //$NON-NLS-1$
			// save properties to remove terminal later
			if (out != null) {
				out.setTerminalProperties(properties);
			}
			ITerminalService service = TerminalServiceFactory.getService();
			service.openConsole(properties, null);
		} catch (Exception e) {
			throw new DockerException(e);
		}
	}

	@Override
	public String getTcpCertPath() {
		if (this.connectionSettings.getType() == BindingType.TCP_CONNECTION) {
			return ((TCPConnectionSettings) this.connectionSettings)
					.getPathToCertificates();
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	public static OutputStream noBlockingOutputStream(final WritableByteChannel out) {
		return new OutputStream() {

			@Override
			public synchronized void write(int i) throws IOException {
				byte b[] = new byte[1];
				b[0] = (byte) i;
				write(b);
			}

			@Override
			public synchronized void write(byte[] b, int off, int len)
					throws IOException {
				if (len == 0) {
					return;
				}
				ByteBuffer buff = ByteBuffer.wrap(b, off, len);
				while (buff.remaining() > 0) {
					out.write(buff);
				}
			}

			@Override
			public void close() throws IOException {
				out.close();
			}
		};
	}

	@Override
	public IDockerNetworkCreation createNetwork(IDockerNetworkConfig cfg)
			throws DockerException, InterruptedException {
		try {
			Ipam.Builder ipamBuilder = Ipam.builder()
					.driver(cfg.ipam().driver());
			List<IDockerIpamConfig> idockerIpamCfgs = cfg.ipam().config();
			List<IpamConfig> ipamCfgs = new ArrayList<>();
			for (IDockerIpamConfig ipamCfg : idockerIpamCfgs) {
				ipamCfgs.add(IpamConfig.create(ipamCfg.subnet(),
						ipamCfg.ipRange(), ipamCfg.gateway()));
			}
			ipamBuilder = ipamBuilder.config(ipamCfgs);
			Ipam ipam = ipamBuilder.build();
			NetworkConfig.Builder networkConfigBuilder = NetworkConfig.builder()
					.name(cfg.name()).driver(cfg.driver()).ipam(ipam);
			networkConfigBuilder.options(cfg.options());
			NetworkConfig networkConfig = networkConfigBuilder.build();
			org.mandas.docker.client.messages.NetworkCreation creation = client
					.createNetwork(networkConfig);
			return new DockerNetworkCreation(creation);

		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public IDockerNetwork inspectNetwork(String networkId)
			throws DockerException, InterruptedException {
		try {
			Network n = client.inspectNetwork(networkId);
			return new DockerNetwork(n);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public List<IDockerNetwork> listNetworks()
			throws DockerException, InterruptedException {
		try {
			List<Network> networkList = client.listNetworks();
			ArrayList<IDockerNetwork> networks = new ArrayList<>();
			for (Network n : networkList) {
				networks.add(new DockerNetwork(n));
			}
			return networks;
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void removeNetwork(String networkId)
			throws DockerException, InterruptedException {
		try {
			client.removeNetwork(networkId);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void connectNetwork(String id, String networkId)
			throws DockerException, InterruptedException {
		try {
			client.connectToNetwork(id, networkId);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public void disconnectNetwork(String id, String networkId)
			throws DockerException, InterruptedException {
		try {
			client.disconnectFromNetwork(id, networkId);
		} catch (org.mandas.docker.client.exceptions.DockerException e) {
			throw new DockerException(e);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IDockerConnection) {
			return getSettings()
					.equals(((IDockerConnection) other).getSettings());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getSettings().hashCode();
	}

	@Override
	public IDockerImageHierarchyNode resolveImageHierarchy(
			final IDockerImage selectedImage) {
		return DockerImageHierarchyNodeUtils.resolveImageHierarchy(this.images,
				this.containers, selectedImage);
	}

	@Override
	public IDockerImageHierarchyNode resolveImageHierarchy(
			final IDockerContainer selectedContainer) {
		return DockerImageHierarchyNodeUtils.resolveImageHierarchy(this.images,
				selectedContainer);
	}

}
