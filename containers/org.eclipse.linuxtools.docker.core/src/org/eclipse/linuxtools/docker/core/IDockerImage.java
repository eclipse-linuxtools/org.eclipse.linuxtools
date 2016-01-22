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

import java.util.List;

public interface IDockerImage {

	/** the literal value for the 'latest' tag on Docker images. */
	public static final String TAG_LATEST = "latest"; //$NON-NLS-1$

	public String created();

	public String createdDate();

	public String id();

	public String parentId();

	public List<String> repoTags();
	
	/**
	 * @return the first repo/name of the Image.
	 */
	public String repo();

	/**
	 * @return all tags associated with the first repo/name of this image
	 */
	public List<String> tags();

	public Long size();

	public Long virtualSize();

	/**
	 * @return {@code true} is this is an intermediate image, i.e., it is the
	 *         parent of another image and it is not tagged (no repo/tag).
	 */
	public boolean isIntermediateImage();

	/**
	 * @return {@code true} is this is a top-level image, i.e., it is not the
	 *         parent of another image, but it has no repo/tag (they were
	 *         removed when another image was built).
	 */
	public boolean isDangling();

	/**
	 * @return the {@link IDockerConnection} associated with (or used to retrieve) this {@link IDockerImage}
	 */
	public IDockerConnection getConnection();

}
