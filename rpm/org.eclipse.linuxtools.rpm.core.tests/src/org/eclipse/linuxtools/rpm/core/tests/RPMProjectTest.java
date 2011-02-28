/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.core.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.RPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;
import org.osgi.framework.FrameworkUtil;

public class RPMProjectTest extends TestCase {

	IWorkspace workspace;
	IWorkspaceRoot root;
	NullProgressMonitor monitor;
	String pluginRoot;

	final String file_sep = System.getProperty("file.separator"); //$NON-NLS-1$

	/*
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		IWorkspaceDescription desc;
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null) {
			fail("Workspace was not setup");
		}
		if (root == null) {
			fail("Workspace root was not setup");
		}
		desc = workspace.getDescription();
		desc.setAutoBuilding(false);
		workspace.setDescription(desc);
	}

	public void testImportHelloWorld() throws Exception {
		// Create a project for the test
		IProject testProject = root.getProject("testHelloWorld");
		testProject.create(monitor);
		testProject.open(monitor);

		// Instantiate an RPMProject
		RPMProject rpmProject = new RPMProject(testProject);

		// Find the test SRPM and install it
		URL url = FileLocator.find(FrameworkUtil.getBundle(RPMProjectTest.class), new Path(
				"resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
						"helloworld-2-2.src.rpm"), null);
		if (url == null) {
			fail("Unable to find resource" + file_sep + "srpms" + file_sep
					+ "helloworld-2-2.src.rpm");
		}
		File foo = new File(FileLocator.toFileURL(url).getPath());
		rpmProject.importSourceRPM(foo);

		// Make sure the original SRPM got copied into the workspace
		IFile srpm = rpmProject.getConfiguration().getSrpmsFolder().getFile(
				"helloworld-2-2.src.rpm");
		assertTrue(srpm.exists());
		assertNotNull(rpmProject.getProject()
				.getPersistentProperty(
						new QualifiedName(RPMCorePlugin.ID,
								IRPMConstants.SRPM_PROPERTY)));

		// Make sure everything got installed properly
		IFile spec = rpmProject.getConfiguration().getSpecsFolder().getFile(
				"helloworld.spec");
		assertTrue(spec.exists());
		IFile sourceBall = rpmProject.getConfiguration().getSourcesFolder()
				.getFile("helloworld-2.tar.bz2");
		assertTrue(sourceBall.exists());

		// Make sure we got the spec file
		IFile specFile = rpmProject.getSpecFile();
		assertTrue(specFile != null);
		assertNotNull(rpmProject.getProject().getPersistentProperty(
				new QualifiedName(RPMCorePlugin.ID,
						IRPMConstants.SPEC_FILE_PROPERTY)));

		// Make sure the RPM nature was added
		assertTrue(rpmProject.getProject().hasNature(
				RPMProjectNature.RPM_NATURE_ID));

		// Clean up
		testProject.delete(true, false, monitor);
	}

	public void testBuildPrepHelloWorld() throws Exception {
		// Create a project for the test
		IProject testProject = root.getProject("testBuildPrepHelloWorld");
		testProject.create(monitor);
		testProject.open(monitor);

		// Instantiate an RPMProject
		RPMProject rpmProject = new RPMProject(testProject);

		// Find the test SRPM, install, and build-prep it
		URL url = FileLocator.find(FrameworkUtil.getBundle(RPMProjectTest.class), new Path(
				"resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
						"helloworld-2-2.src.rpm"), null);
		if (url == null) {
			fail("Unable to find resource" + file_sep + "srpms" + file_sep
					+ "helloworld-2-2.src.rpm");
		}
		File foo = new File(FileLocator.toFileURL(url).getPath());
		rpmProject.importSourceRPM(foo);
		rpmProject.buildPrep();

		// Make sure we got everything in the build directory
		IFolder builddir = rpmProject.getConfiguration().getBuildFolder();
		IFolder helloworldFolder = builddir.getFolder("helloworld-2");
		assertTrue(helloworldFolder.exists());

		// Clean up
		testProject.delete(true, false, monitor);
	}

	public void testBuildSourceRPMHelloWorld() throws Exception {
		// Create a project for the test
		IProject testProject = root.getProject("testBuildSourceRPMHelloWorld1");
		testProject.create(monitor);
		testProject.open(monitor);

		// Instantiate an RPMProject
		RPMProject rpmProject = new RPMProject(testProject);

		// Find the test SRPM and install it
		URL url = FileLocator.find(FrameworkUtil.getBundle(RPMProjectTest.class), new Path(
				"resources" + file_sep + "srpms" + file_sep + //$NON-NLS-1$ //$NON-NLS-2$
						"helloworld-2-2.src.rpm"), null);
		if (url == null) {
			fail("Unable to find resource" + file_sep + "srpms" + file_sep
					+ "helloworld-2-2.src.rpm");
		}
		File foo = new File(FileLocator.toFileURL(url).getPath());
		rpmProject.importSourceRPM(foo);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rpmProject.buildSourceRPM(bos);

		IFile foo2 = rpmProject.getConfiguration().getSrpmsFolder().getFile(
				"helloworld-2-2.src.rpm");
		assertTrue(foo2.exists());

		testProject.delete(true, false, null);
	}
}
