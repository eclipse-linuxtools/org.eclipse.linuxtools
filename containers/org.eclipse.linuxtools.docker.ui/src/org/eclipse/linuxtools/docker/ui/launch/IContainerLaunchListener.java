/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
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
package org.eclipse.linuxtools.docker.ui.launch;

import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;

public interface IContainerLaunchListener extends IRunConsoleListener {

	void done(); // called when container finishes

	void containerInfo(IDockerContainerInfo info);

}
