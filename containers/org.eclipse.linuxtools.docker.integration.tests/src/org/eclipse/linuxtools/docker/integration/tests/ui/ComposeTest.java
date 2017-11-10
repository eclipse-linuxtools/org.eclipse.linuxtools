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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.integration.tests.image.AbstractImageBotTest;
import org.eclipse.linuxtools.docker.integration.tests.mock.MockUtils;
import org.eclipse.linuxtools.docker.reddeer.preferences.DockerComposePreferencePage;
import org.eclipse.linuxtools.docker.reddeer.ui.DockerImagesTab;
import org.eclipse.linuxtools.docker.reddeer.utils.BrowserContentsCheck;
import org.eclipse.linuxtools.internal.docker.core.DockerCompose;
import org.eclipse.linuxtools.internal.docker.core.ProcessLauncher;
import org.eclipse.linuxtools.internal.docker.ui.testutils.CustomMatchers;
import org.eclipse.reddeer.common.matcher.RegexMatcher;
import org.eclipse.reddeer.common.wait.TimePeriod;
import org.eclipse.reddeer.common.wait.WaitWhile;
import org.eclipse.reddeer.core.exception.CoreLayerException;
import org.eclipse.reddeer.core.matcher.WithTextMatcher;
import org.eclipse.reddeer.eclipse.condition.ConsoleHasNoChange;
import org.eclipse.reddeer.eclipse.jdt.ui.packageview.PackageExplorerPart;
import org.eclipse.reddeer.eclipse.ui.browser.WebBrowserView;
import org.eclipse.reddeer.jface.preference.PreferenceDialog;
import org.eclipse.reddeer.swt.impl.button.FinishButton;
import org.eclipse.reddeer.swt.impl.button.OkButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.combo.LabeledCombo;
import org.eclipse.reddeer.swt.impl.menu.ContextMenuItem;
import org.eclipse.reddeer.swt.impl.menu.ShellMenu;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.workbench.core.condition.JobIsRunning;
import org.eclipse.reddeer.workbench.ui.dialogs.WorkbenchPreferenceDialog;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 *
 * @author jkopriva@redhat.com
 *
 */

public class ComposeTest extends AbstractImageBotTest {

	private static final String FILE_DOCKER_COMPOSE = "docker-compose.yml";
	private static final String SYSPROP_DOCKER_COMPOSE_PATH = "dockerComposePath";
	private static final String PATH_TEST_COMPOSE = "resources/test-compose";
	private static final String PROJECT_TEST_COMPOSE = "test-compose";
	private static final String IMAGE_NAME = "test_compose";
	private static final String URL = "http://0.0.0.0:5000/";
	private String dockerComposePath = System.getProperty(SYSPROP_DOCKER_COMPOSE_PATH);

	// for Mockito
	private CountDownLatch latch;

	@Before
	public void before() throws DockerException, InterruptedException {
		if (!mockitoIsUsed()) {
			org.junit.Assume.assumeTrue(!StringUtils.isBlank(this.dockerComposePath));
			assertTrue(
					"Please provide -D" + SYSPROP_DOCKER_COMPOSE_PATH
					+ "=<path to docker-compose binary> in your launch parameters.",
					!StringUtils.isBlank(this.dockerComposePath));
		}
		deleteAllConnections();
		if (mockitoIsUsed()) {
			MockUtils.createDockerMockConnection(DEFAULT_CONNECTION_NAME);
			// configure the 'docker-compose up' mocks with a CountDownLatch to
			// simulate a long-running process
			final ProcessLauncher mockProcessLauncher = Mockito.mock(ProcessLauncher.class, Mockito.RETURNS_DEEP_STUBS);
			DockerCompose.getInstance().setProcessLauncher(mockProcessLauncher);
			setupDockerComposeUpMockProcess(mockProcessLauncher);
			// configure the 'docker-compose stop' mocks which release the
			// CountDownLatch to halt the long-running process
			setupDockerComposeStopMockProcess(mockProcessLauncher);
			// Create temp file for DockerCompose
			@SuppressWarnings("unused")
			File dockerComposeTmpFile = null;
			String tempDir = System.getProperty("java.io.tmpdir");
			dockerComposeTmpFile = new File(tempDir, "docker-compose");
			this.dockerComposePath = tempDir;
		}

	}

	@Test
	public void testCompose() {
		// Set up Docker Compose location
		PreferenceDialog dialog = new WorkbenchPreferenceDialog();
		DockerComposePreferencePage composePreference = new DockerComposePreferencePage(dialog);
		dialog.open();
		dialog.select(composePreference);
		composePreference.setPathToDockerCompose(this.dockerComposePath);
		composePreference.apply();
		new PushButton("Apply and Close").click();

		// Build Image
		DockerImagesTab imagesTab = openDockerImagesTab();
		buildImage(IMAGE_NAME, PATH_TEST_COMPOSE, imagesTab);
		assertConsoleSuccess();

		// Import resource folder
		importProject(PATH_TEST_COMPOSE);

		// Run Docker Compose
		runDockerCompose(PROJECT_TEST_COMPOSE, FILE_DOCKER_COMPOSE);

		// Check if application is running
		WebBrowserView browserView = new WebBrowserView();
		browserView.open();
		//Skip browser contents check, if mockito is used
		if (!mockitoIsUsed()) {
			browserView.openPageURL(URL);
			BrowserContentsCheck.checkBrowserForErrorPage(browserView, URL);
		}

	}

	private void runDockerCompose(String project, String projectFile) {
		PackageExplorerPart pe = new PackageExplorerPart();
		pe.open();
		pe.getProject(project).getProjectItem(projectFile).select();
		@SuppressWarnings("unchecked")
		ContextMenuItem contextMenu = new ContextMenuItem(new WithTextMatcher("Run As"),
				new RegexMatcher(".*Docker Compose"));
		contextMenu.select();
		new OkButton().click();
		try {
			new DefaultShell("Docker Compose");
			new PushButton("Apply and Close").click();
			fail("Docker Compose has not been found! Is it installed and the path is correct?");
		} catch (CoreLayerException ex) {
		}
		new WaitWhile(new JobIsRunning(), TimePeriod.VERY_LONG);
		new WaitWhile(new ConsoleHasNoChange());
	}

	private void importProject(String path) {
		new ShellMenu().getItem("File", "Open Projects from File System...").select();
		new LabeledCombo("Import source:").setText(path);
		new FinishButton().click();
		new WaitWhile(new JobIsRunning());
	}

	private void setupDockerComposeUpMockProcess(final ProcessLauncher mockProcessLauncher)
			throws DockerException, InterruptedException {
		final Process mockDockerComposeUpProcess = Mockito.mock(Process.class);
		Mockito.when(mockDockerComposeUpProcess.getInputStream())
				.thenReturn(new ByteArrayInputStream("up!\n".getBytes()));
		Mockito.when(mockDockerComposeUpProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
		Mockito.when(mockDockerComposeUpProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());
		Mockito.when(mockProcessLauncher.processBuilder(Matchers.anyString(),
				Matchers.eq(DockerCompose.getDockerComposeCommandName()), CustomMatchers.arrayContains("up"))
				.workingDir(Matchers.anyString()).start()).thenReturn(mockDockerComposeUpProcess);
		latch = new CountDownLatch(1);
		Mockito.when(mockDockerComposeUpProcess.waitFor()).then(invocation -> {
			latch.await(5, TimeUnit.SECONDS);
			return 0;
		});
	}

	private void setupDockerComposeStopMockProcess(final ProcessLauncher mockProcessLauncher)
			throws DockerException, InterruptedException {
		final Process mockDockerComposeStopProcess = Mockito.mock(Process.class);
		Mockito.when(mockDockerComposeStopProcess.getInputStream())
				.thenReturn(new ByteArrayInputStream("stop\n".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
		Mockito.when(mockDockerComposeStopProcess.getOutputStream()).thenReturn(new ByteArrayOutputStream());

		Mockito.when(mockProcessLauncher.processBuilder(Matchers.anyString(),
				Matchers.eq(DockerCompose.getDockerComposeCommandName()), CustomMatchers.arrayContains("stop"))
				.workingDir(Matchers.anyString()).start()).thenReturn(mockDockerComposeStopProcess);
		Mockito.when(mockDockerComposeStopProcess.waitFor()).then(invocation -> {
			latch.countDown();
			return 0;
		});
	}

	@Override
	@After
	public void after() {
		deleteImageContainerAfter("testcompose_web_1", "testcompose_redis_1", "testcompose_web", "test_compose",
				"python:2.7", "redis");
		cleanUpWorkspace();
	}

}