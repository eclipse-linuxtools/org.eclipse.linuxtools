/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.linuxtools.rpm.core.utils.DownloadJob;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.FrameworkUtil;

/**
 * Tests for the Download and Prepare sources command from RPM UI Editor
 *
 */
public class DownloadPrepareSourcesTest {

	static IWorkspace workspace;
	static IWorkspaceRoot root;
	static NullProgressMonitor monitor;
	String pluginRoot;
	static IProject testProject;

	final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$

	/**
	 * Prepare the workspace
	 *
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
	}

	/**
	 * Create the test project before starting each test
	 *
	 * @throws CoreException
	 */
	@Before
	public void setUpBeforeTests() throws CoreException {
		testProject = root.getProject("testHelloWorld");
		testProject.create(monitor);
		testProject.open(monitor);
	}

	/**
	 * If the test project exists, delete it and its contents
	 * (used to start clean and fresh for each test case)
	 *
	 * @throws CoreException
	 */
	@After
	public void cleanUpAfterTests() throws CoreException {
		if (testProject != null && testProject.exists()) {
			testProject.delete(true, true, monitor);
		}
	}

	/**
	 * Testing downloading sources using RPMBuild layout
	 *
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void layoutRPMBuildDownloadSourcesTest() throws CoreException,
			IOException, InterruptedException {
		RPMProject rpmProject = importSrpm(testProject,
				RPMProjectLayout.RPMBUILD);
		assertNotNull(rpmProject);

		downloadFile(rpmProject);

		checkDownloadedFile(rpmProject, RPMProjectLayout.RPMBUILD);
	}

	/**
	 * Test downloading sources using FLAT layout
	 *
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void layoutFLATDownloadSourcesTest() throws CoreException,
			IOException, InterruptedException {
		RPMProject rpmProject = importSrpm(testProject, RPMProjectLayout.FLAT);
		assertNotNull(rpmProject);

		downloadFile(rpmProject);

		checkDownloadedFile(rpmProject, RPMProjectLayout.FLAT);
	}

	/**
	 * Test preparing sources using RPMBuild layout
	 *
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void layoutRPMBuildPrepareSourcesTest() throws CoreException,
			IOException, InterruptedException {
		RPMProject rpmProject = importSrpm(testProject,
				RPMProjectLayout.RPMBUILD);
		assertNotNull(rpmProject);

		downloadFile(rpmProject);

		checkDownloadedFile(rpmProject, RPMProjectLayout.RPMBUILD);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IStatus is = rpmProject.buildPrep(bos);
		assertTrue(is.isOK());

		checkPreparedSources(rpmProject, RPMProjectLayout.RPMBUILD);
	}

	/**
	 * Test preparing sources using FLAT layout
	 *
	 * @throws CoreException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void layoutFLATPrepareSourcesTest() throws CoreException,
			IOException, InterruptedException {
		RPMProject rpmProject = importSrpm(testProject,
				RPMProjectLayout.FLAT);
		assertNotNull(rpmProject);

		downloadFile(rpmProject);

		checkDownloadedFile(rpmProject, RPMProjectLayout.FLAT);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IStatus is = rpmProject.buildPrep(bos);
		assertTrue(is.isOK());

		checkPreparedSources(rpmProject, RPMProjectLayout.FLAT);
	}

	/**
	 * Check if the file was downloaded correctly based on project layout
	 *
	 * @param project The RPM project
	 * @param layout The layout of the RPM project (RPMBuild or FLAT)
	 * @throws CoreException
	 */
	public void checkDownloadedFile(RPMProject project, RPMProjectLayout layout)
			throws CoreException {
		IContainer sourcesFolder = project.getConfiguration()
				.getSourcesFolder();
		assertNotNull(sourcesFolder);
		switch (layout) {
		case RPMBUILD:
			assertNotNull(sourcesFolder.getParent().findMember("SOURCES"));
			assertEquals(sourcesFolder.members().length, 1);
			// check if the file exists under SOURCES folder
			assertNotNull(sourcesFolder
					.findMember(new Path("hello-2.8.tar.gz")));
			break;
		case FLAT:
			// 4 = "hello-2.8.tar.gz" + ".project" + "hello-2.8-1.fc19.src.rpm" + "hello.spec"
			assertEquals(sourcesFolder.members().length, 4);
			assertNotNull(sourcesFolder
					.findMember(new Path("hello-2.8.tar.gz")));
			break;
		}
	}

	/**
	 * Check if the source was prepared correctly based on project layout
	 *
	 * @param project The RPM project
	 * @param layout The layout of the RPM project (RPMBuild or FLAT)
	 * @throws CoreException
	 */
	public void checkPreparedSources(RPMProject project, RPMProjectLayout layout)
			throws CoreException {
		IContainer buildFolder = project.getConfiguration().getBuildFolder();
		IFolder helloBuildFolder = null;
		assertNotNull(buildFolder);
		switch (layout) {
		case RPMBUILD:
			assertNotNull(buildFolder.getParent().findMember("BUILD"));
			assertEquals(buildFolder.members().length, 1);
			// check if the file exists under BUILD folder
			helloBuildFolder = buildFolder.getFolder(new Path("hello-2.8"));
			assertTrue(helloBuildFolder.exists());
			// there should be some stuff within hello-2.8/ folder
			assertTrue(helloBuildFolder.members().length >= 1);
			break;
		case FLAT:
			// 4 = "hello-2.8.tar.gz" + ".project" + "hello-2.8-1.fc19.src.rpm" + "hello.spec" + "hello-2.8/"
			assertEquals(buildFolder.members().length, 5);
			helloBuildFolder = buildFolder.getFolder(new Path("hello-2.8"));
			assertTrue(helloBuildFolder.exists());
			// there should be some stuff within hello-2.8/ folder
			assertTrue(helloBuildFolder.members().length >= 1);
			break;
		}
	}

	/**
	 * Download and also test if the file was downloaded correctly
	 *
	 * @param project The RPM project
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void downloadFile(RPMProject project) throws IOException,
			InterruptedException {
		// connect to the URL
		URL url = new URL("http://ftp.gnu.org/gnu/hello/hello-2.8.tar.gz");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_OK);

		// download the file
		String filename = "hello-2.8.tar.gz";
		IFile file = project.getConfiguration().getSourcesFolder()
				.getFile(new Path(filename));
		Job downloadJob = new DownloadJob(file, connection);
		downloadJob.setUser(true);
		downloadJob.schedule();
		downloadJob.join();
		assertEquals(downloadJob.getResult(),Status.OK_STATUS);
	}

	/**
	 * Import the SRPM into the project (make .spec accessible)
	 *
	 * @param testProject The test project
	 * @param layout layout The layout of the RPM project (RPMBuild or FLAT)
	 * @return The RPM project
	 * @throws CoreException
	 * @throws IOException
	 */
	private RPMProject importSrpm(IProject testProject, RPMProjectLayout layout)
			throws CoreException, IOException {
		// Instantiate an RPMProject
		RPMProject rpmProject = new RPMProject(testProject, layout);

		// Find the test SRPM and install it
		URL url = FileLocator.find(FrameworkUtil
				.getBundle(RPMProjectTest.class), new Path(
				"resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
						"hello-2.8-1.fc19.src.rpm"), null);
		File foo = new File(FileLocator.toFileURL(url).getPath());

		// import the SRPM into the RPMProject
		rpmProject.importSourceRPM(foo);
		return rpmProject;
	}
}
