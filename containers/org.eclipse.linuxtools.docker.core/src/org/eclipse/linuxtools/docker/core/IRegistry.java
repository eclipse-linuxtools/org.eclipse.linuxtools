/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
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

public interface IRegistry {

	/**
	 * @return the server address URL, including the scheme
	 */
	String getServerAddress();

	List<IDockerImageSearchResult> getImages(String term) throws DockerException;

	List<IRepositoryTag> getTags(String repository) throws DockerException;

	boolean isVersion2();

}
