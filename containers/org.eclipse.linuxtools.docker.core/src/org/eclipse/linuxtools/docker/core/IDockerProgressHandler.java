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

public interface IDockerProgressHandler {

	/**
	 * To cancel the the current operation a
	 * {@link DockerOperationCancelledException} should be thrown. This is
	 * currently only supported by pullImage().
	 *
	 * @param message
	 *            The progress message
	 * @throws DockerException
	 *             If an exception is thrown the current operation is normally
	 *             terminated
	 */
	void processMessage(IDockerProgressMessage message) throws DockerException;

}
