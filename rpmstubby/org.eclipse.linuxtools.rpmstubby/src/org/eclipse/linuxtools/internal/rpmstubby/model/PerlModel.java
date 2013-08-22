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
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;
import org.eclipse.linuxtools.internal.rpmstubby.parser.CommonMetaData;
import org.eclipse.linuxtools.internal.rpmstubby.parser.PerlMakefileParser;

/**
 * Gives easy access to data from the perl Makefile.PL file.
 *
 */
public class PerlModel {

	private static final String FIX_ME = "#FIXME";

	private static final String LICENSE_REQ = "GPL+ or Artistic";
	private static final String CPAN_URL = "http://search.cpan.org/dist/";

	private static final String ABSTRACT = "abstract";
	private static final String REQUIRES = "prereq_pm";

	private PerlMakefileParser perlMakefileParser;

	/**
	 * Perl makefile
	 *
	 * @param file
	 *            The Perl makefile
	 */
	public PerlModel(IFile file) {
		try {
			perlMakefileParser = new PerlMakefileParser(file);
		} catch (IOException e) {
			StubbyLog.logError(e);
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}

	/**
	 * Get the value from one of the makefile attributes. If the value is empty, it
	 * will return #FIXME
	 *
	 * @param key
	 *            The makefile attribute to get value of
	 * @return The value of the option
	 */
	private String getValue(String key) {
		String rc = perlMakefileParser.getValue(key);
		if (rc.isEmpty()) {
			rc = FIX_ME;
		}
		return rc;
	}

	/**
	 * Get the install requires in the makefile
	 *
	 * @return A list of the values taken from the makefile
	 */
	public List<String> getInstallRequires() {
		List<String> rc = new ArrayList<String>();
		List<String> temp = perlMakefileParser.getValueList(REQUIRES);
		String ver = "";
		if (!temp.isEmpty()) {
			for (String str : temp) {
				ver = str.substring(str.indexOf("=>")+2, str.length());
				str = str.substring(0, str.indexOf("=>"));
				str = str.replaceAll("(\\S+)", "perl($1)");
				if (!ver.isEmpty() && hasDigits(ver)) {
					str = str.concat(">= " + ver);
				}
				rc.add(str);
				ver = "";
			}
		}
		return rc;
	}

	/**
	 * Returns the package name.
	 *
	 * @return The package name.
	 */
	public String getSimplePackageName() {
		String rc = "";
		rc = getValue(CommonMetaData.NAME);
		if (!rc.equals(FIX_ME)) {
			rc = rc.replaceAll("::", "-");
		}
		return rc;
	}

	/**
	 * The simple package name with "perl-" prepended to make better RPM
	 * package name.
	 *
	 * @return The package
	 */
	public String getPackageName() {
		String simpleName = getSimplePackageName();
		if (simpleName.startsWith("perl-")) {
			return simpleName;
		}
		return "perl-"+simpleName;
	}

	/**
	 * Returns the version
	 *
	 * @return The version
	 */
	public String getVersion() {
		String version = getValue(CommonMetaData.VERSION);
		if (!hasDigits(version)) {
			version = "1 " + FIX_ME;
		}
		return version;
	}

	/**
	 * Utility method to try and see if a string contains digits within it
	 *
	 * @param str
	 *            The string to check if it has digits
	 * @return True if string contains digits
	 */
	public boolean hasDigits(String str) {
		return str.matches(".*\\d.*");
	}

	/**
	 * Returns the summary
	 *
	 * @return The package summary
	 */
	public String getSummary() {
		return getValue(ABSTRACT);
	}

	/**
	 * Returns the license
	 *
	 * @return The license
	 */
	public String getLicense() {
		return LICENSE_REQ;
	}

	/**
	 * Returns the url
	 *
	 * @return The url
	 */
	public String getURL() {
		return CPAN_URL+getSimplePackageName();
	}

	/**
	 * Returns the description
	 *
	 * @return The description
	 */
	public String getDescription() {
		return getValue(ABSTRACT);
	}
}
