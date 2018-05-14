/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.ui;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.junit.Test;

public class PerspectiveTest {

	@Test
	public void testDockerExplorerViewPresent() {
		new DockerExplorerView().open();
	}

	@Test
	public void testDockerImagesTabPresent() {
		DockerImagesTab tab = new DockerImagesTab();
		tab.open();
	}

	@Test
	public void testDockerContainersTabPresent() {
		DockerContainersTab tab = new DockerContainersTab();
		tab.open();
	}

}
