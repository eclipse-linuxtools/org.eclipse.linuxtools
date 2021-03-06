/*******************************************************************************
 * Copyright (c) 2007, 2021 Alphonse Van Assche and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.FrameworkUtil;

/*
 * TODO Refract existing code to use the bellow methods so that we can easily
 * switch the way that we do some common operation.
 *
 */

public class UiUtils {

	public static void pluginSanityCheck() throws IOException {
		boolean exists = (new File(PreferenceConstants.RPMMACRO_FILE)).exists();
		// Check if ~/.rpmmacros exist, if the file don't exist we create
		// it with the appropriate command.
		if (!exists && fileExists("/usr/bin/rpmdev-setuptree")) { //$NON-NLS-1$
			org.eclipse.linuxtools.rpm.core.utils.Utils.runCommandToInputStream("rpmdev-setuptree"); //$NON-NLS-1$
		}

		// Check RPM tool preference.
		IPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE,
				FrameworkUtil.getBundle(UiUtils.class).getSymbolicName());
		String currentRpmTool = store.getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
		if (!fileExists("/usr/bin/yum")) { //$NON-NLS-1$
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_YUM)) {
				store.setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
			}
		} else if (!fileExists("/usr/bin/urpmq")) { //$NON-NLS-1$
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_URPM)) {
				store.setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
			}
		}
	}

	/**
	 * Resolve defines for a given string. Defines in the string that are not found
	 * or have some other error will remain unchanged in the returned string.
	 *
	 * @param specfile        The specfile containing the string to resolve.
	 * @param stringToResolve The string to resolve.
	 * @return The resolved string.
	 */
	public static String resolveDefines(Specfile specfile, String stringToResolve) {
		String workingString = stringToResolve;
		SpecfileDefine define;
		try {
			Pattern variablePattern = Pattern.compile("%\\{(\\S+?)\\}"); //$NON-NLS-1$
			Matcher variableMatcher = variablePattern.matcher(stringToResolve);
			Set<String> variablesFound = new HashSet<>();
			while (variableMatcher.find()) {
				String variable = variableMatcher.group(1);
				if (variablesFound.contains(variable)) {
					continue;
				}
				define = specfile.getDefine(variable);
				if (define != null && !stringToResolve.equals(define.getUnresolvedStringValue())) {
					workingString = workingString.replaceAll("\\%\\{" + variable + "\\}", define.getStringValue()); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			return workingString;
		} catch (Exception e) {
			return stringToResolve;
		}
	}

	public static String getPackageDefineId(SpecfileDefine define, SpecfilePackage rpmPackage) {
		return getPackageDefineId(define.getName(), rpmPackage);
	}

	public static String getPackageDefineId(String defineName, SpecfilePackage rpmPackage) {
		return defineName.toLowerCase() + ':' + rpmPackage.getPackageName();
	}

	/**
	 * Detects if we're running under Flatpak.
	 *
	 * @return Returns true in case we're running under Flatpak, otherwise - false
	 */
	public static boolean isFlatpak() {
		return (System.getenv("FLATPAK_SANDBOX_DIR") != null); //$NON-NLS-1$
	}

	/**
	 * Flatpak Sandbox mapping path
	 */
	public static final String SANDBOX_MAPPING_PATHNAME = "/var/run/host"; //$NON-NLS-1$

	/**
	 * Detects if a file exists under its normal path or, in case of Flatpak, in
	 * Sandbox mapped path
	 *
	 * @param absPath A file path to check
	 *
	 * @return Returns true a file exists, otherwise - false
	 */
	public static boolean fileExists(String absPath) {
		if (Files.exists(Paths.get(absPath)))
			return true;

		return (isFlatpak() && Files.exists(Paths.get(SANDBOX_MAPPING_PATHNAME + absPath)));
	}
}
