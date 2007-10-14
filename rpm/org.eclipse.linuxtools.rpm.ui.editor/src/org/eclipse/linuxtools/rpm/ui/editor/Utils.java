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
import java.io.IOException;
import java.io.InputStream;

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

}
