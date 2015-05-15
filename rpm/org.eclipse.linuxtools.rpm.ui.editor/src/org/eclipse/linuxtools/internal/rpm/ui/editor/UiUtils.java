/*******************************************************************************
 * Copyright (c) 2007, 2013 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfilePackage;

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
        if (!exists && Utils.fileExist("/usr/bin/rpmdev-setuptree")) { //$NON-NLS-1$
            org.eclipse.linuxtools.rpm.core.utils.Utils.runCommandToInputStream("rpmdev-setuptree"); //$NON-NLS-1$
        }

        // Check RPM tool preference.
        String currentRpmTool = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
        if (!Utils.fileExist("/usr/bin/yum")) { //$NON-NLS-1$
            if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_YUM)) {
                Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
            }
        } else if (!Utils.fileExist("/usr/bin/urpmq")) { //$NON-NLS-1$
            if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_URPM)) {
                Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
            }
        }
    }

    /**
     * Resolve defines for a given string. Defines in the string that are not found
     * or have some other error will remain unchanged in the returned string.
     *
     * @param specfile The specfile containing the string to resolve.
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
                    workingString = workingString.replaceAll("\\%\\{"+variable+"\\}", define.getStringValue()); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            return workingString;
        } catch (Exception e) {
            return stringToResolve;
        }
    }

    public static String getPackageDefineId(SpecfileDefine define, SpecfilePackage rpmPackage){
        return getPackageDefineId(define.getName(),rpmPackage);
    }

    public static String getPackageDefineId(String defineName, SpecfilePackage rpmPackage){
        return defineName.toLowerCase() + ':' + rpmPackage.getPackageName();
    }
}
