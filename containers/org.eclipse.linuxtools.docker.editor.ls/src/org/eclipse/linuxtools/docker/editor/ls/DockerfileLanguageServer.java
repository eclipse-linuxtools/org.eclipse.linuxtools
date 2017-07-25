/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.linuxtools.docker.editor.ls;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;

public class DockerfileLanguageServer extends ProcessStreamConnectionProvider {

	final static String PATH = "/language-server/node_modules/dockerfile-language-server-nodejs/lib/server.js"; //$NON-NLS-1$

	public DockerfileLanguageServer() {
		List<String> command = new ArrayList<> ();
		try {
			URL url = FileLocator.toFileURL(getClass().getResource(PATH));
			String resourcePath = new File (url.getPath()).getAbsolutePath();
			String nodePath = InitializeLaunchConfigurations.getNodeJsLocation();
			if (nodePath != null) {
				command.add(nodePath);
				command.add(resourcePath);
				command.add("--stdio"); //$NON-NLS-1$
				setCommands(command);
				setWorkingDirectory(System.getProperty("user.dir")); //$NON-NLS-1$
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
