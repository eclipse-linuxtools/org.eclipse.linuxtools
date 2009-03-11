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
package org.eclipse.linuxtools.valgrind.tests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.valgrind.core.ValgrindCommand;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

public class ValgrindTestLaunchPlugin extends ValgrindLaunchPlugin {

	protected static final String VERSION_FILE = ".version"; //$NON-NLS-1$

	protected Bundle testBundle;
	protected IValgrindToolPage toolPage;
	
	private static ValgrindTestLaunchPlugin instance;

	public ValgrindTestLaunchPlugin() {
		instance = this;
	}

	public static ValgrindTestLaunchPlugin getDefault() {
		if (instance == null) {
			instance = new ValgrindTestLaunchPlugin();
		}
		return instance;
	}
	
	public void setTestBundle(Bundle testBundle) {
		this.testBundle = testBundle;
	}
	
	public void setToolPage(IValgrindToolPage toolPage) {
		this.toolPage = toolPage;
	}
	
	@Override
	public IValgrindToolPage getToolPage(String id) throws CoreException {
		return toolPage;
	}

	@Override
	protected ValgrindCommand getValgrindCommand() {
		if (!ValgrindTestsPlugin.RUN_VALGRIND) {
			return new ValgrindStubCommand(0);
		}
		else {
			return super.getValgrindCommand();
		}
	}
	
	@Override
	public Version findValgrindVersion(IPath valgrindLocation) throws CoreException {
		if (valgrindVersion == null) {
			try {
				URL location = FileLocator.find(testBundle, new Path("valgrindFiles"), null); //$NON-NLS-1$
				File file = new File(FileLocator.toFileURL(location).toURI());
				IPath versionFile = new Path(file.getCanonicalPath()).append(VERSION_FILE);

				if (!ValgrindTestsPlugin.RUN_VALGRIND) {
					valgrindVersion = readVersion(versionFile);
				}
				else {
					valgrindVersion = super.findValgrindVersion(valgrindLocation);
					if (ValgrindTestsPlugin.GENERATE_FILES) {
						writeVersion(valgrindVersion, versionFile);
					}				
				}
			} catch (URISyntaxException e) {
				throw new CoreException(new Status(IStatus.ERROR, ValgrindTestsPlugin.PLUGIN_ID, null, e));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, ValgrindTestsPlugin.PLUGIN_ID, null, e));
			}
		}
		return valgrindVersion;
	}

	private void writeVersion(Version ver, IPath versionFile) throws IOException {
		FileWriter fw = null;
		try {
			fw = new FileWriter(versionFile.toFile());
			fw.write(ver.getMajor());
			fw.write(ver.getMinor());
			fw.write(ver.getMicro());
		} finally {
			if (fw != null) {
				fw.close();
			}
		}
	}

	private Version readVersion(IPath versionFile) throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(versionFile.toFile());
			return new Version(fr.read(), fr.read(), fr.read());
		} finally {
			if (fr != null) {
				fr.close();
			}
		}
	}
}
