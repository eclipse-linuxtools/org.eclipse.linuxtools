/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;

public interface IDockerConnection {

	public void addContainerListener(IDockerContainerListener listener);

	public void removeContainerListener(IDockerContainerListener listener);

	/**
	 * Get the list of {@link IDockerContainer} of the remote Docker daemon.
	 * 
	 * @return an unmodifiable list of {@link IDockerContainer} or
	 *         {@link Collections#emptyList()} if no container exists yet. see
	 *         {@link IDockerConnection#getContainers(boolean)}
	 */
	public List<IDockerContainer> getContainers();

	/**
	 * Get the list of {@link IDockerContainer} of the remote Docker daemon.
	 * 
	 * @param force
	 *            {@code true} to force a new retrieval of the list of
	 *            {@link IDockerContainer}, {@code false} to use the cached
	 *            list.
	 * @return an unmodifiable list of {@link IDockerContainer} or
	 *         {@link Collections#emptyList()} if no container exists yet.
	 */
	public List<IDockerContainer> getContainers(final boolean force);
	
	/**
	 * @return Boolean flag to indicate if the list of {@link IDockerContainer}
	 *         has already been loaded ({@code true}) or not ({@code false}).
	 */
	public boolean isContainersLoaded();

	/**
	 * @return the {@link IDockerContainer} identified by the given {@code id} or <code>null</code> if none was found.
	 * @param id the {@link IDockerContainer} id
	 */
	public IDockerContainer getContainer(final String id);

	/**
	 * @return the {@link IDockerContainerInfo} for the {@link IDockerContainer} identified by the given {@code id} or <code>null</code> if none was found.
	 * @param id the {@link IDockerContainer} id
	 */
	public IDockerContainerInfo getContainerInfo(final String id);

	/**
	 * @return the {@link IDockerImageInfo} for the {@link IDockerImage}
	 *         identified by the given {@code id} or <code>null</code> if none
	 *         was found.
	 * @param id
	 *            the {@link IDockerImage} id
	 */
	public IDockerImageInfo getImageInfo(final String id);

	public void addImageListener(IDockerImageListener listener);

	public void removeImageListener(IDockerImageListener listener);

	/**
	 * Get the list of {@link IDockerImage} of the remote Docker daemon.
	 * 
	 * @return an unmodifiable list of {@link IDockerImage} or
	 *         {@link Collections#emptyList()} if no container exists yet.
	 * @see IDockerConnection#getImages(boolean)
	 */
	public List<IDockerImage> getImages();

	/**
	 * Checks if an entry in the current list of {@link IDockerImage} exists
	 * with the same <code>name</code> and <code>tag</code>
	 * 
	 * @param repository
	 *            the repository of the {@link IDockerImage} to find
	 * @param tag
	 *            the tag of the {@link IDockerImage} to find
	 * @return <code>true</code> if an {@link IDockerImage} was found,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasImage(String repository, String tag);

	/**
	 * @return Boolean flag to indicate if the list of {@link IDockerImage} has
	 *         already been loaded ({@code true}) or not ({@code false}).
	 */
	public boolean isImagesLoaded();

	/**
	 * Get the list of {@link IDockerImage} of the remote Docker daemon.
	 * 
	 * @param force
	 *            {@code true} to force a new retrieval of the list of
	 *            {@link IDockerImage}, {@code false} to use the cached list.
	 * @return an unmodifiable list of {@link IDockerImage} or
	 *         {@link Collections#emptyList()} if no container exists yet.
	 */
	public List<IDockerImage> getImages(final boolean force);

	public String getName();

	public String getUri();

	public String getUsername();

	public String getTcpCertPath();

	/**
	 * Checks if the connection is open
	 * @return {@code true} if connection is open, {@code false} otherwise.
	 */
	public boolean isOpen();

	/**
	 * Opens the connection to the Docker daemon.
	 * 
	 * @param registerContainerRefreshManager
	 *            {@code true} if the {@link DockerContainerRefreshManager}
	 *            should be associated with the Docker client to auto-refresh
	 *            the list of containers, {@code false} otherwise (eg: wheh the
	 *            connection should just be tested with a call to
	 *            {@link IDockerConnection#ping()}
	 * @throws DockerException
	 *             generic exception
	 */
	public void open(boolean registerContainerRefreshManager) throws DockerException;

	/**
	 * Send a ping message to the Docker daemon to check if the connection
	 * works.
	 * 
	 * @throws DockerException
	 *             generic exception
	 */
	public void ping() throws DockerException;

	/**
	 * Closes the connection.
	 */
	public void close();

	/**
	 * @return the {@link IDockerConnectionInfo} associated with this {@link IDockerConnection}
	 * @throws DockerException if info retrieval failed
	 */
	public IDockerConnectionInfo getInfo() throws DockerException;

	/**
	 * Retrieves/refreshes the {@link IDockerImage} on the Docker daemon and
	 * applies 'dangling' and 'intermediate' flags on each of them. Also
	 * notifies {@link IDockerConnection} listeners with the list of Images.
	 * 
	 * @return the {@link List} of existing {@link IDockerImage}
	 * @throws DockerException
	 *             If listing images failed.
	 */
	public List<IDockerImage> listImages() throws DockerException;

	void pullImage(String id, IDockerProgressHandler handler) throws DockerException, InterruptedException;

	public List<IDockerImageSearchResult> searchImages(final String term) throws DockerException;
	
	void pushImage(String name, IDockerProgressHandler handler) throws DockerException, InterruptedException;

	void tagImage(String name, String newTag) throws DockerException, InterruptedException;

	String buildImage(IPath path, IDockerProgressHandler handler)
			throws DockerException, InterruptedException;

	String buildImage(IPath path, String name, IDockerProgressHandler handler)
			throws DockerException, InterruptedException;

	@Deprecated
	String createContainer(IDockerContainerConfig c) throws DockerException,
			InterruptedException;

	@Deprecated
	String createContainer(final IDockerContainerConfig c,
			final String containerName) throws DockerException,
			InterruptedException;

	String createContainer(IDockerContainerConfig c, IDockerHostConfig hc)
			throws DockerException, InterruptedException;

	public String createContainer(final IDockerContainerConfig config,
			final IDockerHostConfig hc, final String containerName)
					throws DockerException, InterruptedException;

	void stopContainer(String id) throws DockerException, InterruptedException;

	void killContainer(String id) throws DockerException, InterruptedException;

	void pauseContainer(String id) throws DockerException, InterruptedException;

	void unpauseContainer(String id, OutputStream stream)
			throws DockerException, InterruptedException;

	void removeContainer(String id)
			throws DockerException, InterruptedException;

	@Deprecated
	void startContainer(String id, IDockerHostConfig config, OutputStream stream)
			throws DockerException, InterruptedException;

	@Deprecated
	void startContainer(String id, String loggingId, IDockerHostConfig config,
			OutputStream stream)
			throws DockerException, InterruptedException;

	void startContainer(String id, OutputStream stream)
			throws DockerException, InterruptedException;

	void startContainer(String id, String loggingId, OutputStream stream)
			throws DockerException, InterruptedException;

	void commitContainer(String id, String repo, String tag, String comment,
			String author) throws DockerException;

	void stopLoggingThread(String id);

	void logContainer(String id, OutputStream stream)
			throws DockerException, InterruptedException;

	void removeImage(String name) throws DockerException, InterruptedException;

	void removeTag(String tag) throws DockerException, InterruptedException;


}
