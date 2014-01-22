/*******************************************************************************
 * Copyright (c) 2011, 2012 Red Hat Inc and Others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *     IBM Corporation - Adapting to ssh
 *******************************************************************************/
package org.eclipse.linuxtools.ssh.proxy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHCommandLauncher;
import org.eclipse.linuxtools.internal.ssh.proxy.SSHFileProxy;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteEnvProxyManager;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;

public class SSHProxyManager implements IRemoteEnvProxyManager {

	@Override
	public IRemoteFileProxy getFileProxy(URI uri) {
		return new SSHFileProxy(uri);
	}

	@Override
	public IRemoteFileProxy getFileProxy(IProject project) {
		return new SSHFileProxy(project.getLocationURI());
	}

	@Override
	public IRemoteCommandLauncher getLauncher(URI uri) {
		return new SSHCommandLauncher(uri);
	}

	@Override
	public IRemoteCommandLauncher getLauncher(IProject project) {
		return new SSHCommandLauncher(project.getLocationURI());
	}

	@Override
	public String getOS(URI uri) throws CoreException {
		SSHCommandLauncher cmdLauncher = new SSHCommandLauncher(uri);
		Process p = cmdLauncher.execute(new Path("/bin/uname"), new String[] {"-s"}, new String[0], null, null); //$NON-NLS-1$ //$NON-NLS-2$
		String os = ""; //$NON-NLS-1$
		try {
			InputStream in = p.getInputStream();
			int exit = p.waitFor();
			if (exit == 0) {
				byte bytes[] = new byte[15];
				int len;
				while ((len = in.read(bytes)) != -1)
					os = os + new String(bytes, 0, len);
				os = os.substring(0, os.indexOf("\n")); //$NON-NLS-1$
			}
		} catch (InterruptedException|IOException e) {
		}
		return os;
	}

	@Override
	public String getOS(IProject project) throws CoreException {
		URI uri = project.getLocationURI();
		return getOS(uri);
	}

	@Override
	public Map<String, String> getEnv(URI uri) throws CoreException {
		Map<String, String> env = Collections.emptyMap();
		SSHCommandLauncher cmdLauncher = new SSHCommandLauncher(uri);
		Process p = cmdLauncher.execute(new Path("/bin/env"), new String[] {}, new String[] {}, null, null); //$NON-NLS-1$
		
		String errorLine;
		try (BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()))){
			if((errorLine = error.readLine()) != null){
				throw new IOException(errorLine);
			}
			error.close();
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR, e.getMessage(), Activator.PLUGIN_ID);
			Activator.getDefault().getLog().log(status);
			return Collections.emptyMap();
		}
		/*
		 * It is common to export functions declaration in the environment so
		 *  this pattern filters out them because they get truncated
		 *  and might end up on failure.
		 *
		 * Patterns added in the env list:
		 * var=value
		 * var=value
		 *
		 * Patterns not added in the env list:
		 * var=() { something
		 *
		 * TODO: implement a parser for function declarations so that they do not need to be excluded
		 */
		Pattern variablePattern = Pattern.compile("^(.+)=([^\\(\\)\\s{].*|)$"); //$NON-NLS-1$
		Matcher m;
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				p.getInputStream()))) {
			String readLine = reader.readLine();
			while (readLine != null) {
				m = variablePattern.matcher(readLine);
				if (m.matches()) {
					env.put(m.group(1), m.group(2));
				}
				readLine = reader.readLine();
			}
		} catch (IOException e) {
			Status status = new Status(IStatus.ERROR, e.getMessage(),
					Activator.PLUGIN_ID);
			Activator.getDefault().getLog().log(status);
			return Collections.emptyMap();
		}
		return env;
	}

	@Override
	public Map<String, String> getEnv(IProject project) throws CoreException {
		URI uri = project.getLocationURI();
		return getEnv(uri);
	}
}
