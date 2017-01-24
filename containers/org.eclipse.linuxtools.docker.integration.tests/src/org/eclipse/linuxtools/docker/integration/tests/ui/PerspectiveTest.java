/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.integration.tests.ui;

import org.eclipse.linuxtools.docker.reddeer.ui.DockerContainersTab;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerExplorerView;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.junit.Test;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */
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
