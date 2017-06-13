/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.internal.launching.StandardVMRunner;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.ui.launch.ContainerLauncher;
import org.eclipse.linuxtools.docker.ui.launch.IContainerLaunchListener;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.osgi.util.NLS;

public class ContainerVMRunner extends StandardVMRunner {

	private String ipAddress = null;
	private boolean isListening = false;

	public ContainerVMRunner(IVMInstall vmInstance) {
		super(vmInstance);
	}

	@Override
	protected Process exec(String[] cmdLine, File workingDirectory) throws CoreException {
		return exec(cmdLine, workingDirectory, null);
	}

	@Override
	protected Process exec(String[] cmdLine, File workingDirectory, String[] envp) throws CoreException {
		String connectionUri = DockerConnectionManager.getInstance().getFirstConnection().getUri();
		String command = String.join(" ", cmdLine); //$NON-NLS-1$
		String newWD = workingDirectory.getAbsolutePath();

		// classpath has already been converted if on Windows
		String [] classPath = extractClassPathFromCommand(cmdLine);

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			newWD = UnixFile.convertDOSPathToUnixPath(workingDirectory.getAbsolutePath());
		}

		ContainerLauncher launch = new ContainerLauncher();
		int port = ((ContainerVMInstall)fVMInstance).getPort();
		String [] portMap = port != -1
				? new String [] {String.valueOf(port) + ':' + String.valueOf(port)}
				: new String [0];
		launch.launch("org.eclipse.linuxtools.jdt.docker.launcher", new JavaAppInContainerLaunchListener(), connectionUri, //$NON-NLS-1$
				fVMInstance.getId(), command, null, newWD, Arrays.asList(classPath),
				System.getenv(), null,
				Arrays.asList(portMap),
				false, true, true);

		return null;
	}

	@Override
	protected String constructProgramString(VMRunnerConfiguration config) throws CoreException {

		// Look for the user-specified java executable command
		String command= null;
		Map<String, Object> map= config.getVMSpecificAttributesMap();
		if (map != null) {
			command = (String) map.get(IJavaLaunchConfigurationConstants.ATTR_JAVA_COMMAND);
		}

		// If no java command was specified, use default executable
		if (command == null) {
			File exe = null;
			if (fVMInstance instanceof ContainerVMInstall) {
				exe = ((ContainerVMInstall)fVMInstance).getJavaExecutable();
			}
			if (exe == null) {
				abort(NLS.bind(Messages.ContainerVMRunner_Unable_to_locate_executable_for__0__1, new String[]{fVMInstance.getName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			} else {
				return exe.getAbsolutePath();
			}
		}

		// Build the path to the java executable.  First try 'bin', and if that
		// doesn't exist, try 'jre/bin'
		String installLocation = fVMInstance.getInstallLocation().getAbsolutePath() + UnixFile.separatorChar;
		File exe = new UnixFile(installLocation + "bin" + UnixFile.separatorChar + command); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new UnixFile(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)){
			return exe.getAbsolutePath();
		}
		exe = new UnixFile(installLocation + "jre" + UnixFile.separatorChar + "bin" + UnixFile.separatorChar + command); //$NON-NLS-1$ //$NON-NLS-2$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}
		exe = new UnixFile(exe.getAbsolutePath() + ".exe"); //$NON-NLS-1$
		if (fileExists(exe)) {
			return exe.getAbsolutePath();
		}

		// not found
		abort(NLS.bind(Messages.ContainerVMRunner_Specified_executable__0__does_not_exist_for__1__4, new String[]{command, fVMInstance.getName()}), null, IJavaLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		// NOTE: an exception will be thrown - null cannot be returned
		return null;
	}

	@Override
	protected boolean fileExists(File file) {
		DockerConnection conn = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
		ImageQuery q = new ImageQuery(conn, fVMInstance.getId());
		try {
			return q.isFile(file);
		} finally {
			q.destroy();
		}
	}

	public String getIPAddress() {
		return ipAddress;
	}

	public boolean isListening() {
		return isListening;
	}

	private String [] extractClassPathFromCommand (String [] cmd) {
		int i = 0;
		while (!"-classpath".equals(cmd[i])) {
			i++;
		}
		String [] classPath = (cmd.length > i + 1) ? cmd[i+1].split(UnixFile.pathSeparator) : new String[0];
		return classPath;
	}

	private class JavaAppInContainerLaunchListener implements IContainerLaunchListener {

		@Override
		public void newOutput(String output) {
			if (output.contains("dt_socket")) { //$NON-NLS-1$
				isListening = true;
			}
		}

		@Override
		public void done() {}

		@Override
		public void containerInfo(IDockerContainerInfo info) {
			ipAddress = info.networkSettings().ipAddress();
		}
	}

}
