/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - python implementation (B#350065)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;
import org.eclipse.linuxtools.internal.rpmstubby.parser.CommonMetaData;
import org.eclipse.linuxtools.internal.rpmstubby.parser.PythonEggParser;
import org.eclipse.linuxtools.internal.rpmstubby.parser.ValidLicenses;

/**
 * Gives easy access to data from the python setup.py file.
 *
 */
public class EggModel {

	private static final String[] VALID_LICENSES = {
		ValidLicenses.GPL,
		ValidLicenses.ARTISTIC,
		ValidLicenses.MIT,
		ValidLicenses.APACHE,
		ValidLicenses.PUBLIC_DOMAIN,
		ValidLicenses.ZLIB,
		ValidLicenses.RICOH_SOURCE_CODE,
		ValidLicenses.VOVIDA_SOFTWARE,
		ValidLicenses.INTEL_OPEN_SOURCE,
		ValidLicenses.LGPL,
		ValidLicenses.BSD,
		ValidLicenses.QPL,
		ValidLicenses.IBM_PUBLIC,
		ValidLicenses.PHP,
		ValidLicenses.MODIFIED_CNRI_OPEN_SOURCE,
		ValidLicenses.CVW,
		ValidLicenses.PYTHON,
		ValidLicenses.SUN_INTERNET_STANDARDS_SOURCE,
		ValidLicenses.JABBER_OPEN_SOURCE
	};

	private static final String LONG_DESCRIPTION = "long_description";
	private static final String CLASSIFIERS = "classifiers";
	private static final String INSTALL_REQUIRES = "install_requires";

	private static final String FIX_ME = "#FIXME";

	private PythonEggParser pyEggParser;

	/**
	 * Python egg setup.py file
	 *
	 * @param file The Python setup.py file
	 */
	public EggModel(IFile file) {
		try {
			pyEggParser = new PythonEggParser(file);
		} catch (IOException e) {
			StubbyLog.logError(e);
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}

	/**
	 * Get the value from one of the setup options.
	 * If the value is empty, it will return #FIXME
	 * It will also return #FIX_ME if it looks like a function
	 *
	 * @param option The option from the setup(...) function to get value of
	 * @return The value of the option
	 */
	private String getValue(String option) {
		String str = pyEggParser.getValue(option);

		if (str.isEmpty() || pyEggParser.checkFunction(str)) {
			str = FIX_ME;
		}

		return str;
	}

	/**
	 * Get the values from the classifiers option and
	 * check to see if the keyword is in one of them
	 *
	 * @param keyword What to check for within the list of values in classifiers
	 * @return The value within classifiers that contains the keyword
	 */
	private String getClassifiersList(String keyword) {
		String rc = "";
		List<String> list = pyEggParser.getValueList(CLASSIFIERS);

		for (String str : list) {
			if (str.toLowerCase().contains(keyword)) {
				rc = str;
			}
		}

		return rc;
	}

	/**
	 * Get the values from the install_requires option
	 *
	 * @return The values within install_requires
	 */
	public List<String> getInstallRequiresList() {
		return pyEggParser.getValueList(INSTALL_REQUIRES);
	}

	/**
	 * Returns the package name.
	 *
	 * @return The package name.
	 */
	public String getSimplePackageName() {
		return getValue(CommonMetaData.NAME);
	}

	/**
	 * The simple package name with "python-" prepended to make better RPM package name.
	 *
	 * @return The package
	 */
	public String getPackageName() {
		String simpleName = getSimplePackageName();
		if (simpleName.startsWith("python-")) {
			return simpleName;
		}
		return "python-"+simpleName;
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
	 * Utitlity method to try and see if a string contains
	 * digits within it
	 *
	 * @param str The string to check if it has digits
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
		return getValue(LONG_DESCRIPTION);
	}

	/**
	 * Returns the license
	 *
	 * @return The license
	 */
	public String getLicense() {
		String rawLicense = getClassifiersList(CommonMetaData.LICENSE).toLowerCase();
		String license = "";

		for (String valid : VALID_LICENSES) {
			if (rawLicense.contains(valid.toLowerCase())) {
					license += valid + ", ";
			}
		}

		if (!license.isEmpty()) {
			license = license.substring(0, license.length()-2);
		} else {
			license = FIX_ME;
		}

		return license;
	}

	/**
	 * Returns the url
	 *
	 * @return The url
	 */
	public String getURL() {
		return getValue(CommonMetaData.URL);
	}

	/**
	 * Returns the description
	 *
	 * @return The description
	 */
	public String getDescription() {
		return getValue(CommonMetaData.DESCRIPTION);
	}
}
