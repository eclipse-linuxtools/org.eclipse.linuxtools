/*******************************************************************************
 * Copyright (c) 2017, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;

public class JavaAppInContainerLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(NLS.bind("{0}...", new String[]{configuration.getName()}), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		String connectionURI = configuration.getAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
		String imageID = configuration.getAttribute(JavaLaunchConfigurationConstants.IMAGE_ID, (String) null);
		List<String> extraDirs = configuration.getAttribute(JavaLaunchConfigurationConstants.DIRS, Arrays.asList(new String [0]));

		try {
			DockerConnection conn = (DockerConnection) DockerConnectionManager.getInstance().getConnectionByUri(connectionURI);
			if (conn == null) {
				Display.getDefault().asyncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell()
					, Messages.JavaAppInContainerLaunchDelegate_connection_not_found_title
					, Messages.bind(Messages.JavaAppInContainerLaunchDelegate_connection_not_found_text, connectionURI)));
				return;
			} else if (!conn.isOpen()) {
				try {
					conn.open(false);
				} catch (DockerException e) {
				}

				if (!conn.isOpen()) {
					Display.getDefault().asyncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell()
					, Messages.JavaAppInContainerLaunchDelegate_connection_not_active_title
					, Messages.bind(Messages.JavaAppInContainerLaunchDelegate_connection_not_active_text, connectionURI)));
					return;
				}
			}

			IDockerImage img = conn.getImage(imageID);
			if (img == null) {
				Display.getDefault().asyncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell()
					, Messages.JavaAppInContainerLaunchDelegate_image_not_found_title
					, Messages.bind(Messages.JavaAppInContainerLaunchDelegate_image_not_found_text, imageID)));
				return;
			}

			// randomized port between 1025 and 65535
			int port = ILaunchManager.DEBUG_MODE.equals(mode)
					? (int)((65535 - 1025) * Math.random()) + 1025
					: -1;

			monitor.subTask(Messages.JavaAppInContainerLaunchDelegate_Verifying_launch_attributes____1);

			String mainTypeName = verifyMainTypeName(configuration);
			IVMInstall vm = new ContainerVMInstall(configuration, img, port);
			ContainerVMRunner runner = new ContainerVMRunner(vm);

			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null) {
				workingDirName = workingDir.getAbsolutePath();
			}

			runner.setAdditionalDirectories(extraDirs);

			// Environment variables
			String[] envp= getEnvironment(configuration);

			// Program & VM arguments
			String pgmArgs = getProgramArguments(configuration);
			String vmArgs = getVMArguments(configuration);
			ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);

			// VM-specific attributes
			Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);

			// Classpath
			String[] classpath = getClasspath(configuration);
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				for (int i = 0; i < classpath.length; i++) {
					classpath[i] = UnixFile.convertDOSPathToUnixPath(classpath[i]);
				}
			}

			// Bug 522333 :to be used for modulepath only for 4.7.*
			String[][] paths = getClasspathAndModulepath(configuration);

			// Create VM config
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
			runConfig.setEnvironment(envp);

			List<String> finalVMArgs = new ArrayList<> (Arrays.asList(execArgs.getVMArgumentsArray()));

			// See org.eclipse.jdt.internal.launching.StandardVMDebugger#run()
			if (ILaunchManager.DEBUG_MODE.equals(mode)) {
				double version = getJavaVersion(conn, img);
				if (version < 1.5) {
					finalVMArgs.add("-Xdebug"); //$NON-NLS-1$
					finalVMArgs.add("-Xnoagent"); //$NON-NLS-1$
				}

				//check if java 1.4 or greater
				if (version < 1.4) {
					finalVMArgs.add("-Djava.compiler=NONE"); //$NON-NLS-1$
				}
				if (version < 1.5) {
					finalVMArgs.add("-Xrunjdwp:transport=dt_socket,server=y,address=" + port); //$NON-NLS-1$
				} else if (version < 9) {
					finalVMArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,address=" + port); //$NON-NLS-1$
				} else {
					finalVMArgs.add("-agentlib:jdwp=transport=dt_socket,server=y,address=*:" + port); //$NON-NLS-1$
				}
			}

			runConfig.setVMArguments(finalVMArgs.toArray(new String [0]));
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);

			try {
				IJavaProject proj = JavaRuntime.getJavaProject(configuration);
				if (proj != null) {
					IModuleDescription module = proj == null ? null : proj.getModuleDescription();
					String modName = module == null ? null : module.getElementName();
					if (modName != null) {
						runConfig.setModuleDescription(modName);
					}
				}
			} catch (CoreException e) {
				// Not a java Project so no need to set module description
			}

			if (!JavaRuntime.isModularConfiguration(configuration)) {
				// Bootpath
				runConfig.setBootClassPath(getBootpath(configuration));
			} else {
				// module path
				runConfig.setModulepath(paths[1]);
				if (!configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_MODULE_CLI_OPTIONS, true)) {
					runConfig.setOverrideDependencies(configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MODULE_CLI_OPTIONS, "")); //$NON-NLS-1$
				} else {
					runConfig.setOverrideDependencies(getModuleCLIOptions(configuration));
				}
			}

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			// stop in main
			prepareStopInMain(configuration);

			// done the verification phase
			monitor.worked(1);

			monitor.subTask(Messages.JavaAppInContainerLaunchDelegate_Creating_source_locator____2);
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// Launch the configuration - 1 unit of work
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			if (ILaunchManager.DEBUG_MODE.equals(mode)) {
				while (runner.getIPAddress() == null || !runner.isListening()) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}

				IDockerContainerInfo info = runner.getContainerInfo();
				String configName = info.name().startsWith("/") ? info.name().substring(1) : info.name(); //$NON-NLS-1$

				ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
				ILaunchConfigurationType type = manager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_REMOTE_JAVA_APPLICATION);
				ILaunchConfiguration cfgForAttach = type.newInstance(null, configName);
				ILaunchConfigurationWorkingCopy wc = cfgForAttach.getWorkingCopy();

				String ip = runner.getIPAddress();
				// The container has an IP and is listening
				// Can we reach it ? Or is it on a different network.
				if (!isListening(ip, port)) {
					// If the daemon is reachable via TCP it should forward traffic.
					if (conn.getSettings() instanceof TCPConnectionSettings) {
						ip = ((TCPConnectionSettings)conn.getSettings()).getAddr();
						if (!isListening(ip, port)) {
							// Try to find some network interface that's listening
							ip = getIPAddressListening(port);
							if (!isListening(ip, port)) {
								ip = null;
							}
						}
					} else {
						ip = null;
					}
				}

				if (ip == null) {
					String imageName = conn.getImage(imageID).repoTags().get(0);
					Display.getDefault().asyncExec(() -> MessageDialog.openError(Display.getDefault().getActiveShell()
							, Messages.JavaAppInContainerLaunchDelegate_session_unreachable_title
							, Messages.bind(Messages.JavaAppInContainerLaunchDelegate_session_unreachable_text, new Object [] {imageName, imageID, runner.getIPAddress()})));
					return;
				}

				Map<String, String> map = new HashMap<> ();
				map.put("hostname", ip); //$NON-NLS-1$
				map.put("port", String.valueOf(port)); //$NON-NLS-1$
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CONNECT_MAP, map);
				String projectName = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
				wc.doSave();
				DebugUITools.launch(cfgForAttach, ILaunchManager.DEBUG_MODE);
			}
		}
		finally {
			monitor.done();
		}
	}

	private double getJavaVersion(DockerConnection conn, IDockerImage img) {
		ImageQuery q = new ImageQuery(conn, img.id());
		double res = q.getJavaVersion();
		q.destroy();
		return res;
	}

	private boolean isListening (String addr, int port) {
		try (Socket s = new Socket())
		{
			InetSocketAddress host = addr != null
					? new InetSocketAddress(addr, port)
					: new InetSocketAddress(InetAddress.getByName(null), port);
			s.connect(host, 1000);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private String getIPAddressListening (int port) {
		Enumeration<NetworkInterface> ifaces;
		try {
			ifaces = NetworkInterface.getNetworkInterfaces();
			while (ifaces.hasMoreElements()) {
				NetworkInterface iface = ifaces.nextElement();
				if (iface.isUp()) {
					Enumeration<InetAddress> addrs = iface.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress addr = addrs.nextElement();
						if (addr instanceof Inet4Address
								&& isListening(addr.getHostAddress(), port)) {
							return addr.getHostAddress();
						}
					}
				}
			}
		} catch (SocketException e) {
		}

		return null;
	}

}
