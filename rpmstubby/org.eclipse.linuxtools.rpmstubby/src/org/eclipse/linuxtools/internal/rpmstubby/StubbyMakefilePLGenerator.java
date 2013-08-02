/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - perl implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.linuxtools.internal.rpmstubby.model.PerlModel;

/**
 * Generator for RPM specfile from perl Makefile.PL.
 *
 */
public class StubbyMakefilePLGenerator extends AbstractGenerator {

	private PerlModel model;

	/**
	 * Creates the generator by parsing the Makefile.PL file.
	 *
	 * @param makefile
	 *            The Makefile.PL file to generate specfile for.
	 */
	public StubbyMakefilePLGenerator(IFile makefile) {
		parse(makefile);
		specfileName = model.getPackageName().toLowerCase() + ".spec";
		projectName = makefile.getProject().getName();
	}

	/**
	 * Creates the model which contains the information
	 *
	 * @param makefile The Makefile.PL file
	 */
	private void parse(IFile makefile) {
		model = new PerlModel(makefile);
	}

	/**
	 * Generates a RPM specfile based on the parsed data from the Makefile.PL file.
	 *
	 * @return The generated Makefile.PL.
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
		buffer.append("License:        GPL+ or Artistic\n");
		buffer.append("URL:            "+model.getURL() + "\n"); // e.g., Net-XMPP/ for Net::XMPP
		buffer.append("Source0:        #FIXME\n\n");
		buffer.append("BuildArch:      noarch\n");
		generateRequires(buffer);
		buffer.append("%{?perl_default_filter}\n\n\n");
		buffer.append("%description\n" + model.getDescription() + "\n\n\n");
		generatePrepSection(buffer);
		generateBuildSection(buffer);
		generateInstallSection(buffer);
		generateFilesSections(buffer);
		generateChangelog(buffer);

		return buffer.toString();
	}

	/**
	 * Generate requires
	 *
	 * @param buffer Buffer to write content to
	 */
	private void generateRequires(StringBuilder buffer) {
		buffer.append("BuildRequires:  perl(ExtUtils::MakeMaker)\n");
		List<String> requires = model.getInstallRequires() ;
		if (!requires.isEmpty()) {
			for (String str : requires) {
				buffer.append("BuildRequires:  " + str + "\n");
			}
		}
		buffer.append("Requires:  	    perl(:MODULE_COMPAT_%(eval \"`%{__perl} -V:version`\"; echo $version))\n\n");
	}

	/**
	 * Generate prep
	 *
	 * @param buffer Buffer to write content to
	 */
	private static void generatePrepSection(StringBuilder buffer) {
		buffer.append("%prep\n");
		buffer.append("%setup -q #You may need to update this according to your Source0\n\n\n");
	}

	/**
	 * Generate build
	 *
	 * @param buffer Buffer to write content to
	 */
	private static void generateBuildSection(StringBuilder buffer) {
		buffer.append("%build\n");
		buffer.append("%{__perl} Makefile.PL INSTALLDIRS=vendor\n\n\n");
	}

	/**
	 * Generate install
	 *
	 * @param buffer Buffer to write content to
	 */
	private static void generateInstallSection(StringBuilder buffer) {
		buffer.append("%install\n");
		buffer.append("make pure_install PERL_INSTALL_ROOT=$RPM_BUILD_ROOT\n\n\n");
	}

	/**
	 * Generate files
	 *
	 * @param buffer Buffer to write content to
	 */
	private static void generateFilesSections(StringBuilder buffer) {
		buffer.append("%files\n");
		buffer.append("%{perl_vendorlib}/*\n\n\n");
	}
}