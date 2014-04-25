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
     * Resolve defines for a give string, if a define is not found or if
     * there is some other error, the original string is returned.
     *
     * @param stringToResolve The string to resolve.
     * @return resolved string
     */
    public static String resolveDefines(Specfile specfile, String stringToResolve) {
        String originalString = stringToResolve;
        SpecfileDefine define;
        try {
            Pattern variablePattern = Pattern.compile("%\\{(\\S+?)\\}"); //$NON-NLS-1$
            Matcher variableMatcher = variablePattern.matcher(stringToResolve);
            while (variableMatcher.find()) {
                define = specfile.getDefine(variableMatcher.group(1));
                if (define != null) {
                    stringToResolve = stringToResolve.replaceAll("\\b"+variableMatcher.group(1)+"\\b", define.getStringValue()); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    return originalString;
                }
            }
            if (!stringToResolve.equals(originalString)) {
                stringToResolve = stringToResolve.replaceAll("\\%\\{|\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return stringToResolve;
        } catch (Exception e) {
            return originalString;
        }
    }

    public static String getPackageDefineId(SpecfileDefine define, SpecfilePackage rpmPackage){
        return getPackageDefineId(define.getName(),rpmPackage);
    }

    public static String getPackageDefineId(String defineName, SpecfilePackage rpmPackage){
        return defineName.toLowerCase() + ':' + rpmPackage.getPackageName();
    }
}
