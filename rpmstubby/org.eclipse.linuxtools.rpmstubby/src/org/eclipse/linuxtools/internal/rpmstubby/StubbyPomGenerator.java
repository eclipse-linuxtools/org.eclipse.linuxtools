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
		for (Map.Entry<String, String> entry : model.getDependencies()
				.entrySet()) {
			buffer.append("Requires: mvn(" + entry.getKey() + ":"
					+ entry.getValue() + ")\n");
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
		buffer.append("install -m 644 target/%{name}-%{version}.jar   %{buildroot}%{_javadir}/%{name}.jar\n\n");

		buffer.append("# poms\n");
		buffer.append("install -d -m 755 %{buildroot}%{_mavenpomdir}\n");
		buffer.append("install -pm 644 pom.xml \\\n");
		buffer.append("    %{buildroot}%{_mavenpomdir}/JPP.%{name}.pom\n\n");

		buffer.append("%add_maven_depmap JPP.%{name}.pom %{name}.jar\n\n");

		buffer.append("# javadoc\n");
		buffer.append("install -d -m 0755 %{buildroot}%{_javadocdir}/%{name}\n");
		buffer.append("cp -pr target/site/api*/* %{buildroot}%{_javadocdir}/%{name}/\n");
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

}
