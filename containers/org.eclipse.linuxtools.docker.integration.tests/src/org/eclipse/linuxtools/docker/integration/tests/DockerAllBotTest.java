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

package org.eclipse.linuxtools.docker.integration.tests;


import org.eclipse.linuxtools.docker.integration.tests.connection.AddConnectionTest;
import org.eclipse.linuxtools.docker.integration.tests.container.ContainerLogTest;
import org.eclipse.linuxtools.docker.integration.tests.container.DockerContainerTest;
import org.eclipse.linuxtools.docker.integration.tests.container.ExposePortTest;
import org.eclipse.linuxtools.docker.integration.tests.container.LabelsTest;
import org.eclipse.linuxtools.docker.integration.tests.container.LinkContainersTest;
import org.eclipse.linuxtools.docker.integration.tests.container.NetworkModeTest;
import org.eclipse.linuxtools.docker.integration.tests.container.PrivilegedModeTest;
import org.eclipse.linuxtools.docker.integration.tests.container.UnconfinedTest;
import org.eclipse.linuxtools.docker.integration.tests.container.VariablesTest;
import org.eclipse.linuxtools.docker.integration.tests.container.VolumeMountTest;
import org.eclipse.linuxtools.docker.integration.tests.image.BuildImageTest;
import org.eclipse.linuxtools.docker.integration.tests.image.DeleteImagesAfter;
import org.eclipse.linuxtools.docker.integration.tests.image.EditDockerFileTest;
import org.eclipse.linuxtools.docker.integration.tests.image.HierarchyViewTest;
import org.eclipse.linuxtools.docker.integration.tests.image.ImageTagTest;
import org.eclipse.linuxtools.docker.integration.tests.image.PullImageTest;
import org.eclipse.linuxtools.docker.integration.tests.image.PushImageTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.ComposeTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.ContainerTabTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.DifferentRegistryTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.ImageTabTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.LaunchDockerImageTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.PerspectiveTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.PropertiesViewTest;
import org.eclipse.linuxtools.docker.integration.tests.ui.SearchDialogTest;
import org.eclipse.reddeer.junit.runner.RedDeerSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * This is a RedDeer test case for an eclipse application.
 * 
 * @author jkopriva@redhat.com
 */

@RunWith(RedDeerSuite.class)
@Suite.SuiteClasses({
	PerspectiveTest.class, 
	AddConnectionTest.class,
	BuildImageTest.class,
	PullImageTest.class,
	DockerContainerTest.class,
	ExposePortTest.class,
	ImageTabTest.class,
	ContainerTabTest.class,
	VolumeMountTest.class,
	PrivilegedModeTest.class,
	UnconfinedTest.class,
	VariablesTest.class,
	LinkContainersTest.class,
	DifferentRegistryTest.class,
	SearchDialogTest.class,
	ImageTagTest.class,
	LabelsTest.class,
	HierarchyViewTest.class,
	PropertiesViewTest.class,
	PushImageTest.class,
	LaunchDockerImageTest.class,
	ComposeTest.class,
	ContainerLogTest.class,
	NetworkModeTest.class,
	EditDockerFileTest.class,
	
	DeleteImagesAfter.class
})
public class DockerAllBotTest {

}