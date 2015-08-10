/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 */
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;

/**
 *
 */
public class DockerImageSearchResult 
		implements IDockerImageSearchResult {

	/** the image description. */
	private final String description;
	/** official image flag. */
	private final boolean official;
	/** automated build flag */
	private final boolean automated;
	/** image name */
	private final String name;
	/** star count */
	private final int starCount;

	/**
	 * Full constructor
	 * 
	 * @param description
	 *            the image description
	 * @param official
	 *            official image flag
	 * @param automated
	 *            automated build flag
	 * @param name
	 *            image name (org/repo)
	 * @param starCount
	 *            star count
	 */
	public DockerImageSearchResult(String description, boolean official,
			boolean automated, String name, int starCount) {
		this.description = description;
		this.official = official;
		this.automated = automated;
		this.name = name;
		this.starCount = starCount;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isOfficial() {
		return official;
	}

	@Override
	public boolean isAutomated() {
		return automated;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getStarCount() {
		return starCount;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DockerImageSearchResult other = (DockerImageSearchResult) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
