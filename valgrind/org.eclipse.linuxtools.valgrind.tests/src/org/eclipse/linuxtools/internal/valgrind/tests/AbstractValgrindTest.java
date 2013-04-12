/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.internal.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.profiling.tests.AbstractTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public abstract class AbstractValgrindTest extends AbstractTest {

	private static final String TEMPLATE_PREFIX = "template_"; //$NON-NLS-1$
	private static final FileFilter TEMPLATE_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return pathname.getName().startsWith(TEMPLATE_PREFIX) && !pathname.isHidden();
		}
	};
	private static final FileFilter NOT_TEMPLATE_FILTER = new FileFilter() {
		public boolean accept(File pathname) {
			return !pathname.getName().startsWith(TEMPLATE_PREFIX) && !pathname.isHidden();
		}
	};

	private static final String SEARCH_STRING_WS = "XXXXXXXXXXXX"; //$NON-NLS-1$
	private static final String SEARCH_STRING_BL = "YYYYYYYYYYYY"; //$NON-NLS-1$

	private List<ILaunch> launches;

	@Override
	protected void setUp() throws Exception {
		launches = new ArrayList<ILaunch>();

		// Substitute Valgrind command line interaction
		ValgrindLaunchPlugin.getDefault().setValgrindCommand(getValgrindCommand());

		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		if (launches.size() > 0) {
			lm.removeLaunches(launches.toArray(new ILaunch[launches.size()]));
			launches.clear();
		}
		// Delete the Launch Configurations
		ILaunchConfiguration[] configs = lm.getLaunchConfigurations();
		for (ILaunchConfiguration config : configs) {
			config.delete();
		}
		super.tearDown();
	}

	@Override
	protected ILaunchConfigurationType getLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);
	}

	protected ILaunch doLaunch(ILaunchConfiguration config, String testName) throws Exception {
		ILaunch launch;
		IPath pathToFiles = getPathToFiles(testName);

		if (!ValgrindTestsPlugin.RUN_VALGRIND) {
			bindLocation(pathToFiles);
		}

		ILaunchConfigurationWorkingCopy wc = config.getWorkingCopy();
		wc.setAttribute(LaunchConfigurationConstants.ATTR_INTERNAL_OUTPUT_DIR, pathToFiles.toOSString());
		wc.doSave();

		ValgrindTestLaunchDelegate delegate = new ValgrindTestLaunchDelegate();
		launch = new Launch(config, ILaunchManager.PROFILE_MODE, null);

		DebugPlugin.getDefault().getLaunchManager().addLaunch(launch);
		launches.add(launch);
		delegate.launch(config, ILaunchManager.PROFILE_MODE, launch, null);

		if (ValgrindTestsPlugin.GENERATE_FILES) {
			unbindLocation(pathToFiles);
		}
		return launch;
	}

	protected IPath getPathToFiles(String testName) throws URISyntaxException,
			IOException {
		URL location = FileLocator.find(getBundle(), new Path("valgrindFiles"), null); //$NON-NLS-1$
		File file = new File(FileLocator.toFileURL(location).toURI());
		IPath pathToFiles = new Path(file.getCanonicalPath()).append(testName);
		return pathToFiles;
	}

	private void unbindLocation(IPath pathToFiles) throws IOException {
		String bundleLoc = FileLocator.getBundleFile(getBundle()).getCanonicalPath();
		String workspaceLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		File testDir = pathToFiles.toFile();
		for (File log : testDir.listFiles(NOT_TEMPLATE_FILTER)) {
			File template = new File(testDir, TEMPLATE_PREFIX + log.getName());
			replaceLocation(log, template, new String[] { bundleLoc, workspaceLoc }, new String[] { SEARCH_STRING_BL , SEARCH_STRING_WS });
		}
	}

	private void bindLocation(IPath pathToFiles) throws IOException {
		String bundleLoc = FileLocator.getBundleFile(getBundle()).getCanonicalPath();
		String workspaceLoc = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		File testDir = pathToFiles.toFile();
		for (File template : testDir.listFiles(TEMPLATE_FILTER)) {
			String name = template.getName().replace(TEMPLATE_PREFIX, ""); //$NON-NLS-1$
			File log = new File(testDir, name.substring(name.indexOf(TEMPLATE_PREFIX) + 1));
			replaceLocation(template, log, new String[] { SEARCH_STRING_BL, SEARCH_STRING_WS }, new String[] { bundleLoc, workspaceLoc });
		}
	}

	private void replaceLocation(File oldFile, File newFile, String[] from, String[] to) {
		if (oldFile.isFile()) {
			BufferedReader br = null;
			PrintWriter pw = null;
			try {
				br = new BufferedReader(new FileReader(oldFile));
				pw = new PrintWriter(new FileWriter(newFile));

				String line;
				while ((line = br.readLine()) != null) {
					for (int i = 0; i < from.length; i++) {
						line = line.replaceAll(from[i], to[i]);
					}
					pw.println(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (pw != null) {
					pw.close();
				}
			}
		}
	}

	@Override
	protected void setProfileAttributes(ILaunchConfigurationWorkingCopy wc) throws CoreException {
		ILaunchConfigurationTab tab = new ValgrindOptionsTab();
		tab.setDefaults(wc);
		tab = ValgrindLaunchPlugin.getDefault().getToolPage(getToolID());
		tab.setDefaults(wc);
		wc.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, getToolID());
	}

	protected ICProject createProjectAndBuild(String projname) throws Exception {
		return createProjectAndBuild(getBundle(), projname);
	}

	protected Bundle getBundle(){
		return FrameworkUtil.getBundle(this.getClass());
	}

	protected abstract String getToolID();

	private ValgrindCommand getValgrindCommand() {
		if (!ValgrindTestsPlugin.RUN_VALGRIND) {
			return new ValgrindStubCommand();
		}
		else {
			return new ValgrindCommand();
		}
	}

}