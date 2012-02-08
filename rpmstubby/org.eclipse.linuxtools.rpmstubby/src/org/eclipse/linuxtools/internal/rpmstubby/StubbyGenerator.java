/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.rpmstubby.model.FeatureModel;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Generates the RPM specfile and the fetch script based on the feature and user
 * preferences.
 * 
 */
public class StubbyGenerator {

	private FeatureModel model;
	private IFile featureFile;

	/**
	 * Creates the specfile and fetch script generator for the given packages.
	 * 
	 * @param featureFile
	 *            The feature.xml file to generate from.
	 */
	public StubbyGenerator(IFile featureFile) {
		this.featureFile = featureFile;
		parse(featureFile);
	}

	private void parse(IFile featureFile) {
		DocumentBuilderFactory docfactory = DocumentBuilderFactory
				.newInstance();
		IPath featureDir = featureFile.getLocation().removeLastSegments(1);
		String featurePropertiesFile = featureDir.toOSString()
				+ "/feature.properties"; 
		Properties featureProperties = new Properties();
		try {
			featureProperties.load(new FileInputStream(featurePropertiesFile));
		} catch (FileNotFoundException e) {
			// Do nothing if the feature.properties is not found
		} catch (IOException e) {
			StubbyLog.logError(e);
		}
		DocumentBuilder docbuilder;
		try {
			docbuilder = docfactory.newDocumentBuilder();
			Document docroot = docbuilder.parse(featureFile.getContents());
			model = new FeatureModel(docroot, featureProperties);

		} catch (ParserConfigurationException e) {
			StubbyLog.logError(e);
		} catch (SAXException e) {
			StubbyLog.logError(e);
		} catch (IOException e) {
			StubbyLog.logError(e);
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}

	/**
	 * Generates a RPM specfile based on the parsed data from the pom file.
	 * 
	 * @return The generated specfile.
	 */
	public String generateSpecfile() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("%global eclipse_base   %{_libdir}/eclipse\n");
		buffer.append("%global install_loc    %{_datadir}/eclipse/dropins/"
				+ model.getSimplePackageName() + "\n\n");
		buffer.append("Name:           " + model.getPackageName().toLowerCase() + "\n");
		buffer.append("Version:        " + model.getVersion() + "\n");
		buffer.append("Release:        1%{?dist}" + "\n");
		buffer.append("Summary:        " + model.getSummary() + "\n\n");
		buffer.append("Group:          Development/Tools\n");
		buffer.append("License:        " + model.getLicense() + "\n");
		buffer.append("URL:            " + model.getURL() + "\n");
		buffer.append("Source0:        #FIXME\n");
		buffer.append("BuildArch: noarch\n\n");
		generateRequires(buffer);
		buffer.append("\n%description\n" + model.getDescription() + "\n");
		generatePrepSection(buffer);
		generateBuildSection(buffer);
		generateInstallSection(buffer);
		generateFilesSections(buffer);
		generateChangelog(buffer);

		return buffer.toString();
	}

	private void generateRequires(StringBuilder buffer) {
		buffer.append("BuildRequires: eclipse-pde >= 1:3.4.0\n");
		buffer.append("Requires: eclipse-platform >= 3.4.0\n");

	}

	private void generateInstallSection(StringBuilder buffer) {
		buffer.append("%install\n");
		buffer.append("install -d -m 755 %{buildroot}%{install_loc}\n\n");
		buffer.append("%{__unzip} -q -d %{buildroot}%{install_loc} \\\n");
		buffer.append("     build/rpmBuild/" + model.getFeatureId()
				+ ".zip \n\n");

	}

	private void generateFilesSections(StringBuilder buffer) {
		buffer.append("%files\n");
		buffer.append("%{install_loc}\n");
		String docsRoot = featureFile.getLocation().removeLastSegments(1).lastSegment();
		String[] files = featureFile.getLocation().removeLastSegments(1).toFile().list();
		for (String file :files){
			if (file.matches("(epl-.*|license)\\.html")){
				buffer.append("%doc "+docsRoot+"/"+file+"\n");
			}
		}
		buffer.append("\n");
	}

	private void generatePrepSection(StringBuilder buffer) {
		buffer.append("\n%prep\n");
		buffer.append("#FIXME Replace FIXME with the root directory name in Source0\n");
		buffer.append("%setup -q -n FIXME\n\n");
	}

	private void generateBuildSection(StringBuilder buffer) {
		buffer.append("%build\n");
		buffer.append("%{eclipse_base}/buildscripts/pdebuild -f ").append(
				model.getFeatureId());
		buffer.append("\n\n");
	}

	/**
	 * Returns the last meaningful part of the feature id before the feature
	 * substring.
	 * 
	 * @param packageName
	 *            The feature id from which to extract the name.
	 * @return The part of the feature id to be used for package name.
	 */
	public String getPackageName(String packageName) {
		String[] packageItems = packageName.split("\\.");
		String name = packageItems[packageItems.length - 1];
		if (name.equalsIgnoreCase("feature")) {
			name = packageItems[packageItems.length - 2];
		}
		return "eclipse-" + name;
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, StubbyPlugin.PLUGIN_ID,
				IStatus.OK, message, null);
		throw new CoreException(status);
	}

	private void generateChangelog(StringBuilder buffer) {
		buffer.append("%changelog\n\n");
		buffer.append("#FIXME\n");
	}

	/**
	 * Writes the given contents to a file with the given fileName in the
	 * specified project.
	 * 
	 * @param projectName
	 *            The name of the project to put the file into.
	 * @throws CoreException
	 *             Thrown when the project doesn't exist.
	 */
	public void writeContent(String projectName) throws CoreException {
		String fileName = model.getPackageName().toLowerCase() + ".spec";
		String contents = generateSpecfile();
		InputStream contentInputStream = new ByteArrayInputStream(
				contents.getBytes());
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(projectName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Project \"" + projectName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = contentInputStream;
			if (file.exists()) {
				file.setContents(stream, true, true, null);
			} else {
				file.create(stream, true, null);
			}
			stream.close();
		} catch (IOException e) {
			StubbyLog.logError(e);
		}
		StubbyPlugin.getActiveWorkbenchShell().getDisplay()
				.asyncExec(new Runnable() {
					public void run() {
						IWorkbenchPage page = PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
						try {
							IDE.openEditor(page, file, true);
						} catch (PartInitException e) {
							StubbyLog.logError(e);
						}
					}
				});
	}
}
