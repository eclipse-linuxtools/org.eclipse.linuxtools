/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui.editor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.rpm.ui.editor.parser.Specfile;
import org.eclipse.linuxtools.rpm.ui.editor.parser.SpecfileDefine;
import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;

/*
 * TODO Refract existing code to use the bellow methods so that we can easily 
 * switch the way that we do some common operation.
 *
 */

public class Utils {
	
	public static void pluginSanityCheck() throws IOException {
		boolean exists = (new File(PreferenceConstants.RPMMACRO_FILE)).exists();
	    // Check if ~/.rpmmacros exist, if the file don't exist we create 
		// it with the appropriate command.
		if (!exists) {
	    	runCommandToInputStream("rpmdev-setuptree"); //$NON-NLS-1$
	    }
		
		// Check RPM tool preference.
		String currentRpmTool = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
		if (!fileExist("/usr/bin/yum")) { //$NON-NLS-1$
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_YUM))
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
		} else if (!fileExist("/usr/bin/urpmq")) { //$NON-NLS-1$
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_URPM))
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
		}
	}
	
	public static boolean fileExist(String cmdPath) {
		return new File(cmdPath).exists();
	}
	
	public static BufferedInputStream runCommandToInputStream(String... command) throws IOException {
		BufferedInputStream in = null;
		Process child = new ProcessBuilder(command).start();
		in = new BufferedInputStream(child.getInputStream());
		return in;
	}
	
	public static String runCommandToString(String... command) throws IOException {
		BufferedInputStream in = runCommandToInputStream(command);
		return inputStreamToString(in);
	}
	
	public static String inputStreamToString(InputStream stream) throws IOException {
		String retStr = ""; //$NON-NLS-1$
		int c;
		while ((c = stream.read()) != -1) {
			retStr += ((char) c);
		}
		stream.close();
		return retStr;		
	}
	
	public static void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}
	
	
	/**
	 * Resolve defines for a give URL string, if a define is not found or if
	 * there is some other error, the original string is returned.
	 * 
	 * @param string To resolve
	 * @return resolved URL String
	 */
	public static String resolveDefines(Specfile specfile, String string) {
		String originalUrlString= string;
		SpecfileDefine define;
		try {
			Pattern variablePattern= Pattern.compile("%\\{(\\S+?)\\}"); //$NON-NLS-1$
			Matcher variableMatcher= variablePattern.matcher(string);
			while (variableMatcher.find()) {
				define= specfile.getDefine(variableMatcher.group(1));
				string= string.replaceAll(variableMatcher.group(1), define.getStringValue());
			}
			if (!string.equals(originalUrlString))
				string= string.replaceAll("\\%\\{|\\}", ""); //$NON-NLS-1$ //$NON-NLS-2$
			return string;
		} catch (Exception e) {
			return originalUrlString;
		}
	}
	
}
