/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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
package org.eclipse.linuxtools.docker.core;

import java.util.List;

public interface IDockerImage {

	/** the literal value for the 'latest' tag on Docker images. */
	String TAG_LATEST = "latest"; //$NON-NLS-1$

	String created();

	String createdDate();

	/**
	 * @return the full image id
	 */
	String id();

	String parentId();

	List<String> repoTags();
	
	/**
	 * @return the first repo/name of the Image.
	 */
	String repo();

	/**
	 * @return all tags associated with the first repo/name of this image
	 */
	List<String> tags();

	Long size();

	Long virtualSize();

	/**
	 * @return {@code true} is this is an intermediate image, i.e., it is the
	 *         parent of another image and it is not tagged (no repo/tag).
	 */
	boolean isIntermediateImage();

	/**
	 * @return {@code true} is this is a top-level image, i.e., it is not the
	 *         parent of another image, but it has no repo/tag (they were
	 *         removed when another image was built).
	 */
	boolean isDangling();

	/**
	 * @return the {@link IDockerConnection} associated with (or used to retrieve) this {@link IDockerImage}
	 */
	IDockerConnection getConnection();

}
