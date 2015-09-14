/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DockerMachine {

	private static final String DM = "docker-machine"; //$NON-NLS-1$
	private static final String LS = "ls"; //$NON-NLS-1$
	private static final String URL = "url"; //$NON-NLS-1$
	private static final String ENV = "env"; //$NON-NLS-1$

	public static String[] getNames() {
		return call(new String[] { LS, "-q" }); //$NON-NLS-1$
	}

	public static String getHost(String name) {
		String[] res = call(new String[] { URL, name });
		return res.length == 1 ? res[0] : null;
	}

	public static String getCertPath(String name) {
		String[] res = getEnv(name);
		for (String l : res) {
			if (l.contains("DOCKER_CERT_PATH")) { //$NON-NLS-1$
				// DOCKER_CERT_PATH="/path/to/cert-folder"
				return l.split("=")[1].replace("\"", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return null;
	}

	private static String[] getEnv(String name) {
		return call(new String[] { ENV, name });
	}

	private static String[] call(String[] args) {
		List<String> result = new ArrayList<>();
		try {
			List<String> cmd = new ArrayList<>();
			cmd.add(DM);
			cmd.addAll(Arrays.asList(args));
			Process p = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
			BufferedReader buff = new BufferedReader(new InputStreamReader(p.getInputStream()));
			if (p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0) {
				String line;
				while ((line = buff.readLine()) != null) {
					result.add(line);
				}
			} else {
				return new String[0];
			}
		} catch (IOException e) {
		} catch (InterruptedException e) {
		}
		return result.toArray(new String[0]);
	}

}
