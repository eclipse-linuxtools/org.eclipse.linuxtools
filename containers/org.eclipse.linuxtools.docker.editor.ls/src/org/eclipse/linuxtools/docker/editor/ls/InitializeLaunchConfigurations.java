/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.docker.editor.ls;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wildwebdeveloper.embedder.node.NodeJSManager;

public class InitializeLaunchConfigurations {

	private static boolean alreadyWarned;

	public static String getNodeJsLocation() {

		File nodeJsRuntime = NodeJSManager.getNodeJsLocation();
		if (nodeJsRuntime != null) {
			return nodeJsRuntime.getAbsolutePath();
		}

		String res = null;
		String[] command = new String[] { "/bin/bash", "-c", "which node" };
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
			command = new String[] { "cmd", "/c", "where node" };
		}

		BufferedReader reader = null;
		try {
			Process p = Runtime.getRuntime().exec(command);
			reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			res = reader.readLine();
		} catch (IOException e) {
			// try other defaults
		}

		// Try default install path as last resort
		if (res == null && Platform.getOS().equals(Platform.OS_MACOSX)) {
			res = "/usr/local/bin/node";
		}

		if (res != null && Files.exists(Paths.get(res))) {
			return res;
		} else if (!alreadyWarned) {
			warnNodeJSMissing();
			alreadyWarned = true;
		}

		return null;
	}

	private static void warnNodeJSMissing() {
		Display.getDefault().asyncExec(() -> {
			MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Missing node.js",
					"Could not find node.js. This will result in editors missing key features.\n"
							+ "Please make sure node.js is installed and that your PATH environement variable contains the location to the `node` executable.");
		});
	}

}
