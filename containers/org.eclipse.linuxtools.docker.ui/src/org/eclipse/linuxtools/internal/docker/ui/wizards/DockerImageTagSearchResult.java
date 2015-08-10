/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.linuxtools.docker.core.IRepositoryTag;

/**
 * A single result of when searching for tags for a specific repository on a
 * registry
 */
public class DockerImageTagSearchResult {

	/** repository name of the image. */
	private final String repository;

	/** the tag for this specific image. */
	private final String name;

	/** the corresponding image layer. */
	private final String layer;

	/**
	 * boolean marker to indicate if this image has already been pulled on the
	 * selected Docker daemon.
	 */
	private final boolean resolved;

	public DockerImageTagSearchResult(final String repository, final IRepositoryTag repositoryTag, final boolean resolved) {
		this.repository = repository;
		this.name = repositoryTag.getName();
		this.layer = repositoryTag.getLayer();
		this.resolved = resolved;
	}

	/**
	 * @return the repository name
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * @return Name of the tag.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The corresponding image layer for this specific tag
	 */
	public String getLayer() {
		return this.layer;
	}

	/**
	 * @return a {@link Boolean} to indicate if the corresponding image with the
	 *         given tag has already been pulled, i.e, the exact same
	 *         repository/tag exists on the current Docker daemon.
	 */
	public boolean isResolved() {
		return this.resolved;
	}

}