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

package org.eclipse.linuxtools.docker.core;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * An interface for Docker registries.
 */
public interface IDockerRegistry {

	/**
	 * @return the {@link List} of available {@link IDockerImageSearchResult}
	 *         for the given repository on this IDockerRegistry.
	 * @param repository
	 *            the image repository
	 * @throws InterruptedException
	 * @throws DockerException
	 * @throws ExecutionException
	 */
	List<IRepositoryTag> getTags(String repository)
			throws InterruptedException, ExecutionException, DockerException;

}
