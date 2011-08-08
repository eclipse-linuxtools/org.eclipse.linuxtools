/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.rpmstubby.model.PomModel;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Generator for RPM specfile from maven pom.xml.
 *
 */
public class StubbyPomGenerator {

	private PomModel model;

	/**
	 * Creates the generator by parsing the pom.xml file.
	 * @param pomFile The pom.xml file to generate specfile for.
	 */
	public StubbyPomGenerator(IFile pomFile) {
		parse(pomFile);
	}

	private void parse(IFile pomFile) {
		DocumentBuilderFactory docfactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docbuilder;
		try {
			docbuilder = docfactory.newDocumentBuilder();
			Document docroot = docbuilder.parse(pomFile.getContents());
			model = new PomModel(docroot);

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
		String packageName = model.getPackageName();
		buffer.append("Name:           " + packageName.toLowerCase() + "\n");
		buffer.append("Version:        " + model.getVersion() + "\n");
		buffer.append("Release:        1%{?dist}" + "\n");
		buffer.append("Summary:        " + model.getSummary() + "\n\n");
		buffer.append("Group:          Development/Libraries\n");
		buffer.append("License:        " + model.getLicense() + "\n");
		buffer.append("URL:            " + model.getURL() + "\n");
		buffer.append("Source0:        #FIXME\n");
		buffer.append("BuildArch: noarch\n\n");
		generateRequires(buffer);
		buffer.append("\n%description\n" + model.getDescription() + "\n\n");
		generateJavadocSubpackage(buffer);
		generatePrepSection(buffer);
		generateBuildSection(buffer);
		generateInstallSection(buffer);
		generateFilesSections(buffer);
		generateChangelog(buffer);

		return buffer.toString();
	}

	private void generateRequires(StringBuilder buffer) {
		for (String dependency: model.getDependencies()) {
			buffer.append("BuildRequires: "+dependency+"\n");
		}
		for (String dependency: model.getDependencies()) {
			buffer.append("Requires: "+dependency+"\n");
		}
	}

	private void generateJavadocSubpackage(StringBuilder buffer) {
		buffer.append("%package javadoc\n");
		buffer.append("Group:          Documentation\n");
		buffer.append("Summary:        Javadoc for %{name}\n");
		buffer.append("Requires:       jpackage-utils\n\n");

		buffer.append("%description javadoc\n");
		buffer.append("API documentation for %{name}.\n\n");

	}

	private void generateChangelog(StringBuilder buffer) {
		buffer.append("%changelog\n\n");
		buffer.append("#FIXME\n");
	}

	private void generateInstallSection(StringBuilder buffer) {
		buffer.append("%install\n");
		buffer.append("# jars\n");
		buffer.append("install -d -m 0755 %{buildroot}%{_javadir}\n");
		buffer
				.append("install -m 644 target/%{name}-%{version}.jar   %{buildroot}%{_javadir}/%{name}\n\n");

		buffer.append("%add_to_maven_depmap " + model.getGroupId() + " "
				+ model.getArtifactId() + " %{version} JPP "
				+ model.getArtifactId() + "\n\n");

		buffer.append("# poms\n");
		buffer
				.append("install -d -m 755 %{buildroot}%{_mavenpomdir}\n");
		buffer.append("install -pm 644 pom.xml \\\n");
		buffer
				.append("    %{buildroot}%{_mavenpomdir}/JPP.%{name}.pom\n\n");

		buffer.append("# javadoc\n");
		buffer
				.append("install -d -m 0755 %{buildroot}%{_javadocdir}/%{name}\n");
		buffer
				.append("cp -pr target/site/api*/* %{buildroot}%{_javadocdir}/%{name}/\n");
		buffer.append("rm -rf target/site/api*\n\n");
	}

	private void generateFilesSections(StringBuilder buffer) {
		buffer.append("%files\n");
		buffer.append("%{_javadir}/*\n");
		buffer.append("%{_mavenpomdir}/*\n");
		buffer.append("%{_mavendepmapfragdir}/*\n\n");

		buffer.append("%files javadoc\n");
		buffer.append("%{_javadocdir}/%{name}\n\n");
	}

	private void generatePrepSection(StringBuilder buffer) {
		buffer.append("\n%prep\n");
		buffer.append("%setup -q #You may need to update this according to your Source0\n\n");
	}

	private void generateBuildSection(StringBuilder buffer) {
		buffer.append("%build\n");
		buffer.append("mvn-rpmbuild \\\n");
		buffer.append("        -e \\\n");
		buffer.append("        install javadoc:javadoc\n\n");
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
		InputStream contentInputStream = new ByteArrayInputStream(contents
				.getBytes());
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
		StubbyPlugin.getActiveWorkbenchShell().getDisplay().asyncExec(
				new Runnable() {
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

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, StubbyPlugin.PLUGIN_ID,
				IStatus.OK, message, null);
		throw new CoreException(status);
	}
}
