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

	public String created();

	public String createdDate();

	public String id();

	public String parentId();

	public List<String> repoTags();
	
	public String repo();

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
