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
	    	String[] command = {"rpmdev-setuptree"};
	    	runCommandToInputStream(command);
	    }
		
		// Check RPM tool preference.
		String currentRpmTool = Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_CURRENT_RPMTOOLS);
		if (!fileExist("/usr/bin/yum")) {
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_YUM))
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
		} else if (!fileExist("/usr/bin/urpmq")) {
			if (currentRpmTool.equals(PreferenceConstants.DP_RPMTOOLS_URPM))
				Activator.getDefault().getPreferenceStore().setValue(PreferenceConstants.P_CURRENT_RPMTOOLS, PreferenceConstants.DP_RPMTOOLS_RPM);
		}
	}
	
	public static boolean fileExist(String cmdPath) {
		return new File(cmdPath).exists();
	}
	
	public static BufferedInputStream runCommandToInputStream(String[] command) throws IOException {
		BufferedInputStream in = null;
		Process child = Runtime.getRuntime().exec(command);
		in = new BufferedInputStream(child.getInputStream());
		return in;
	}
	
	public static String runCommandToString(String[] command) throws IOException {
		BufferedInputStream in = runCommandToInputStream(command);
		return inputStreamToString(in);
	}
	
	public static String inputStreamToString(InputStream stream) throws IOException {
		String retStr = "";
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
}
