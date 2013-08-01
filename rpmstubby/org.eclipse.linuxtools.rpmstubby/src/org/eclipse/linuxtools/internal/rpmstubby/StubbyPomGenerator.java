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

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.model.PomModel;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Generator for RPM specfile from maven pom.xml.
 *
 */
public class StubbyPomGenerator extends AbstractGenerator {

	private PomModel model;

	/**
	 * Creates the generator by parsing the pom.xml file.
	 *
	 * @param pomFile
	 *            The pom.xml file to generate specfile for.
	 */
	public StubbyPomGenerator(IFile pomFile) {
		parse(pomFile);
		specfileName = model.getPackageName().toLowerCase() + ".spec";
		projectName = pomFile.getProject().getName();
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
	@Override
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
		for (Map.Entry<String, String> entry : model.getDependencies()
				.entrySet()) {
			buffer.append("BuildRequires: mvn(" + entry.getKey() + ":"
					+ entry.getValue() + ")\n");
		}
		buffer.append("BuildRequires: maven-local\n");
	}

	private static void generateJavadocSubpackage(StringBuilder buffer) {
		buffer.append("%package javadoc\n");
		buffer.append("Group:          Documentation\n");
		buffer.append("Summary:        Javadoc for %{name}\n\n");
		buffer.append("%description javadoc\n");
		buffer.append("API documentation for %{name}.\n\n");

	}

	private static void generateInstallSection(StringBuilder buffer) {
		buffer.append("%install\n");
		buffer.append("%mvn_install\n\n");
	}

	private static void generateFilesSections(StringBuilder buffer) {
		buffer.append("%files -f .mfiles\n");
		buffer.append("%dir %{_javadir}/%{name}\n\n");
		buffer.append("%files javadoc -f .mfiles-javadoc\n\n");
	}

	private static void generatePrepSection(StringBuilder buffer) {
		buffer.append("\n%prep\n");
		buffer.append("%setup -q #You may need to update this according to your Source0\n\n");
	}

	private static void generateBuildSection(StringBuilder buffer) {
		buffer.append("%build\n");
		buffer.append("%mvn_build\n\n");
	}

}