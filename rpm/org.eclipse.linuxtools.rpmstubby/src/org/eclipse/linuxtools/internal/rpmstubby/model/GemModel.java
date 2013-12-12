/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - ruby implementation (B#350066)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.rpmstubby.StubbyLog;
import org.eclipse.linuxtools.internal.rpmstubby.parser.CommonMetaData;
import org.eclipse.linuxtools.internal.rpmstubby.parser.RubyGemParser;

/**
 * Gives easy access to data from the ruby gemspec file.
 *
 */
public class GemModel {

	private static final String FIX_ME = "#FIXME";

	private static final String SUMMARY = "summary";
	private static final String RUBYGEMS_VERSION = "rubygems_version";
	private static final String REQUIRE_PATHS = "require_paths";
	private static final String DEPENDENCY = "add_dependency";
	private static final String DEVELOPMENT_DEPENDENCY = "add_development_dependency";
	private static final String RUNTIME_DEPENDENCY = "add_runtime_dependency";
	private static final String HOMEPAGE = "homepage";

	/**
	 * Options in the gemspec file that are supposed to only have 1 value
	 *
	 */
	private static final String[] SINGLE_VALUES = { CommonMetaData.DESCRIPTION,
			CommonMetaData.NAME, CommonMetaData.VERSION,
			CommonMetaData.LICENSE, SUMMARY, RUBYGEMS_VERSION, HOMEPAGE };

	private RubyGemParser rubyGemParser;

	/**
	 * Ruby gemspec file
	 *
	 * @param file
	 *            The Ruby gemspec file
	 */
	public GemModel(IFile file) {
		try {
			rubyGemParser = new RubyGemParser(file);
		} catch (IOException e) {
			StubbyLog.logError(e);
		} catch (CoreException e) {
			StubbyLog.logError(e);
		}
	}

	/**
	 * Get the value from one of the gemspec attributes. If the value is empty, it
	 * will return #FIXME
	 *
	 * @param attr
	 *            The gemspec attribute to get value of
	 * @return The value of the option
	 */
	private String getValue(String attr) {
		List<String> list = rubyGemParser.getValueList(attr);
		String rc = "";
		boolean single = false;

		for (String str : SINGLE_VALUES) {
			if (str.equals(attr)) {
				single = true;
				break;
			}
		}

		if (!list.isEmpty() && single) {
			rc = list.get(0);
		} else {
			rc = FIX_ME;
		}

		return rc;
	}

	/**
	 * Get the install requires in the gemspec file
	 *
	 * @param key
	 *            Either the dependencies or the development dependencies option
	 * @return A list of the values taken from the gemspec
	 */
	public List<String> getDependencies(String key) {
		List<String> rc = new ArrayList<String>();
		List<String> temp = rubyGemParser.getValueList(key);

		if (!temp.isEmpty()) {
			for (String tmp : temp) {
				rc.add(tmp
						.replaceFirst("(?:%q|%Q)(?:([\\W])([^\\W]+)[\\W])",
								"$2").replaceAll("(\"|'|\\[|\\])", "")
						.replaceAll(",", ""));
			}
		}
		return rc;
	}

	/**
	 * Get the values from the add_dependency option
	 *
	 * @return The values within add_dependency
	 */
	public List<String> getInstallRequiresList() {
		List<String> rc = getDependencies(DEPENDENCY);
		rc.addAll(getDependencies(RUNTIME_DEPENDENCY));
		return getDependencies(DEPENDENCY);
	}

	/**
	 * Get the values from the add_development_dependency option
	 *
	 * @return The values within add_development_dependency
	 */
	public List<String> getBuildRequiresList() {
		return getDependencies(DEVELOPMENT_DEPENDENCY);
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
	 * The simple package name with "rubygem-" prepended to make better RPM
	 * package name.
	 *
	 * @return The package
	 */
	public String getPackageName() {
		String simpleName = getSimplePackageName();
		if (simpleName.startsWith("rubygem-")) {
			return simpleName;
		}
		return "rubygem-"+simpleName;
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
	 * Returns the RubyGems version
	 *
	 * @return The RubyGems version
	 */
	public String getGemVersion() {
		String version = getValue(RUBYGEMS_VERSION);

		if (!hasDigits(version)) {
			version = FIX_ME;
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
		return getValue(SUMMARY);
	}

	/**
	 * Returns the license
	 *
	 * @return The license
	 */
	public String getLicense() {
		String license = getValue("license");
		List<String> list = rubyGemParser.getValueList("licenses");

		if (license.equals(FIX_ME) && !list.isEmpty()) {
			license = "";
			for (String str : list) {
				license = license.concat(str + ", ");
				license = license.replaceAll("(\"|')", "");
			}
			license = license.substring(0, license.length() - 2);
		}

		return license;
	}

	/**
	 * Returns the url
	 *
	 * @return The url
	 */
	public String getURL() {
		return getValue(HOMEPAGE);
	}

	/**
	 * Returns the description
	 *
	 * @return The description
	 */
	public String getDescription() {
		return getValue(CommonMetaData.DESCRIPTION);
	}

	/**
	 * Return the require paths
	 *
	 * @return The require paths
	 */
	public List<String> getRequirePaths() {
		List<String> rc = new ArrayList<String>();
		List<String> temp = rubyGemParser.getValueList(REQUIRE_PATHS);

		if (!temp.isEmpty()) {
			for (String tmp : temp) {
				rc.add(tmp
						.replaceFirst("(?:%q|%Q)(?:([\\W])([^\\W]+)[\\W])",
								"$2").replaceAll("(\"|'|\\[|\\])", "")
						.replaceAll(",", ""));
			}
		}

		return rc;
	}
}
