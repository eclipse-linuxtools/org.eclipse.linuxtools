/*******************************************************************************
 * Copyright (c) 2010, 2011 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *    Red Hat Inc. - rewrite to use RemoteConnection class
 *    Corey Ashford <cjashfor@us.ibm.com> - Modified for use with an RDT-based
 *                                          RemoteConnection class.
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.linuxtools.internal.valgrind.ui.ValgrindViewPart;
import org.eclipse.linuxtools.profiling.launch.ConfigUtils;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteConnection;
import org.eclipse.linuxtools.profiling.launch.RemoteConnectionException;
import org.eclipse.linuxtools.tools.launch.core.factory.RuntimeProcessFactory;
import org.eclipse.linuxtools.valgrind.core.IValgrindMessage;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Version;

/**
 * @since 1.1
 */
public class ValgrindRemoteProxyLaunchDelegate extends ValgrindLaunchConfigurationDelegate {

	private static final String VALGRIND_CMD = "valgrind"; //$NON-NLS-1$

	private ConfigUtils configUtils;

	public ValgrindRemoteProxyLaunchDelegate() {
		super();
	}

	private final static String VERSION_OPT = "--version";

	private String whichVersion(IProject project) {
		String cmdArray[] = new String[2];
		cmdArray[0] = VALGRIND_CMD;
		cmdArray[1] = VERSION_OPT;

		try {

			Process p = RuntimeProcessFactory.getFactory().exec(cmdArray, project);

			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String commandOutput = stdout.readLine();
			stdout.close();

			return commandOutput;
		} catch (IOException e) {
			return null;
		}
	}

	private static final String VERSION_PREFIX = "valgrind-";
	private static final char VERSION_DELIMITER = '-';
	private static final Version MIN_VER = ValgrindLaunchPlugin.VER_3_3_0;

	private Version getValgrindVersion(IProject project) throws CoreException {
		Version valgrindVersion;
		String verString = whichVersion(project);

		if (verString == null || verString.equals(EMPTY_STRING)){
			throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"))); //$NON-NLS-1$
		}

		verString = verString.replace(VERSION_PREFIX, ""); //$NON-NLS-1$

		if (verString.indexOf(VERSION_DELIMITER) > 0) {
			verString = verString.substring(0, verString.indexOf(VERSION_DELIMITER));
		}
		if (verString.length() > 0) {
			valgrindVersion = Version.parseVersion(verString);
		}
		else {
			throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, Messages.getString("ValgrindLaunchPlugin.Couldn't_determine_version"))); //$NON-NLS-1$
		}

		// check for minimum supported version
		if (valgrindVersion.compareTo(MIN_VER) < 0) {
			throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, NLS.bind(Messages.getString("ValgrindLaunchPlugin.Error_min_version"), valgrindVersion.toString(), MIN_VER.toString()))); //$NON-NLS-1$
		}
		return valgrindVersion;
	}


	@Override
	public void launch(final ILaunchConfiguration config, String mode,
			final ILaunch launch, IProgressMonitor m) throws CoreException {
		if (m == null) {
			m = new NullProgressMonitor();
		}

		// Clear process as we wait on it to be instantiated
		process = null;

		SubMonitor monitor = SubMonitor.convert(m,
				Messages.getString("ValgrindRemoteLaunchDelegate.task_name"), 10);
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}

		this.config = config;
		this.launch = launch;
		try {
			// remove any output from previous run
			ValgrindUIPlugin.getDefault().resetView();
			// reset stored launch data
			getPlugin().setCurrentLaunchConfiguration(null);
			getPlugin().setCurrentLaunch(null);

			this.configUtils = new ConfigUtils(config);
			IProject project = ConfigUtils.getProject(configUtils.getProjectName());
			URI exeURI = new URI(configUtils.getExecutablePath());
			RemoteConnection exeRC = new RemoteConnection(exeURI);
			monitor.worked(1);
			String valgrindPathString = RuntimeProcessFactory.getFactory().whichCommand(VALGRIND_CMD, project);
			IPath valgrindFullPath = Path.fromOSString(valgrindPathString);
			boolean copyExecutable = configUtils.getCopyExecutable();
			if (copyExecutable) {
				URI copyExeURI = new URI(configUtils.getCopyFromExecutablePath());
				RemoteConnection copyExeRC = new RemoteConnection(copyExeURI);
				IRemoteFileProxy copyExeRFP = copyExeRC.getRmtFileProxy();
				IFileStore copyExeFS = copyExeRFP.getResource(copyExeURI.getPath());
				IRemoteFileProxy exeRFP = exeRC.getRmtFileProxy();
				IFileStore exeFS = exeRFP.getResource(exeURI.getPath());
				IFileInfo exeFI = exeFS.fetchInfo();
				if (exeFI.isDirectory()) {
					// Assume the user wants to copy the file to the given directory, using
					// the same filename as the "copy from" executable.
					IPath copyExePath = Path.fromOSString(copyExeURI.getPath());
					IPath newExePath = Path.fromOSString(exeURI.getPath()).append(copyExePath.lastSegment());
					// update the exeURI with the new path.
					exeURI = new URI(exeURI.getScheme(), exeURI.getAuthority(), newExePath.toString(), exeURI.getQuery(), exeURI.getFragment());
					exeFS = exeRFP.getResource(exeURI.getPath());
				}
				copyExeFS.copy(exeFS, EFS.OVERWRITE | EFS.SHALLOW, new SubProgressMonitor(monitor, 1));
				// Note: assume that we don't need to create a new exeRC since the
				// scheme and authority remain the same between the original exeURI and the new one.
			}
			valgrindVersion = getValgrindVersion(project);
			IPath remoteBinFile = Path.fromOSString(exeURI.getPath());
			String configWorkingDir = configUtils.getWorkingDirectory();
			IFileStore workingDir;
			if(configWorkingDir == null){
				// If no working directory was provided, use the directory containing the
				// the executable as the working directory.
				IPath workingDirPath = remoteBinFile.removeLastSegments(1);
				IRemoteFileProxy workingDirRFP = exeRC.getRmtFileProxy();
				workingDir = workingDirRFP.getResource(workingDirPath.toOSString());
			} else {
				URI workingDirURI = new URI(configUtils.getWorkingDirectory());
				RemoteConnection workingDirRC = new RemoteConnection(workingDirURI);
				IRemoteFileProxy workingDirRFP = workingDirRC.getRmtFileProxy();
				workingDir = workingDirRFP.getResource(workingDirURI.getPath());
			}

			IPath remoteLogDir = Path.fromOSString("/tmp/");
			outputPath = remoteLogDir.append("eclipse-valgrind-" + System.currentTimeMillis()); //$NON-NLS-1$

			exeRC.createFolder(outputPath, new SubProgressMonitor(monitor, 1));

			// create/empty local output directory
			IValgrindOutputDirectoryProvider provider = getPlugin().getOutputDirectoryProvider();
			IPath localOutputDir = null;
			try {
				localOutputDir = provider.getOutputPath();
				createDirectory(localOutputDir);
			} catch (IOException e2) {
				throw new CoreException(new Status(IStatus.ERROR, ValgrindLaunchPlugin.PLUGIN_ID, IStatus.OK,
						Messages.getString(""), e2));
			}

			// tool that was launched
			toolID = getTool(config);
			// ask tool extension for arguments
			dynamicDelegate = getDynamicDelegate(toolID);

			String[] valgrindArgs = getValgrindArgumentsArray(config);
			String[] executableArgs = getProgramArgumentsArray(config);
			String[] allArgs = new String[executableArgs.length + valgrindArgs.length + 2];

			int idx = 0;
			allArgs[idx++] = VALGRIND_CMD;
			for (String valgrindArg : valgrindArgs) {
				allArgs[idx++] = valgrindArg;
			}
			allArgs[idx++] = remoteBinFile.toOSString();
			for (String executableArg : executableArgs) {
				allArgs[idx++] = executableArg;
			}

			Process p = RuntimeProcessFactory.getFactory().exec(allArgs, new String[0], workingDir, project);

			int state = p.waitFor();

			if (state != IRemoteCommandLauncher.OK) {
				abort("valgrind launcher exited with status " + state + ". See IRemoteCommandLauncher for details.\n",
						null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}


			if (p.exitValue() != 0) {
				String line = null;

				StringBuilder valgrindOutSB = new StringBuilder();
				BufferedReader valgrindOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
				while((line = valgrindOut.readLine()) != null ){
					valgrindOutSB.append(line);
				}

				StringBuilder valgrindErrSB = new StringBuilder();
				BufferedReader valgrindErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				while((line = valgrindErr.readLine()) != null ){
					valgrindErrSB.append(line);
				}

				abort("Standard ouput: " + valgrindOutSB.toString() +
						"\nStandard error: " + valgrindErrSB.toString(),
						null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}

			// move remote log files to local directory
			exeRC.download(outputPath, localOutputDir, new SubProgressMonitor(monitor, 1));

			// remove remote log dir and all files under it
			exeRC.delete(outputPath, new SubProgressMonitor(monitor, 1));

			// store these for use by other classes
			getPlugin().setCurrentLaunchConfiguration(config);
			getPlugin().setCurrentLaunch(launch);

			// parse Valgrind logs
			IValgrindMessage[] messages = parseLogs(localOutputDir);

			// create launch summary string to distinguish this launch
			launchStr = createLaunchStr(valgrindFullPath);

			// create view
			ValgrindUIPlugin.getDefault().createView(launchStr, toolID);
			// set log messages
			ValgrindViewPart view = ValgrindUIPlugin.getDefault().getView();
			view.setMessages(messages);
			monitor.worked(1);

			// pass off control to extender
			dynamicDelegate.handleLaunch(config, launch, localOutputDir, monitor.newChild(2));

			// initialize tool-specific part of view
			dynamicDelegate.initializeView(view.getDynamicView(), launchStr, monitor.newChild(1));

			// refresh view
			ValgrindUIPlugin.getDefault().refreshView();

			// show view
			ValgrindUIPlugin.getDefault().showView();
			monitor.worked(1);

		} catch (URISyntaxException e) {
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (IOException e) {
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (RemoteConnectionException e) {
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} catch (InterruptedException e) {
			abort(e.getLocalizedMessage(), null, ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
		} finally {
			monitor.done();
			m.done();
		}
	}

	protected String createLaunchStr(IPath valgrindPath) throws CoreException {
		String projectName = configUtils.getProjectName();
		IProject project = ConfigUtils.getProject(projectName);
		URI projectURI = project.getLocationURI();
		
		String host = projectURI.getHost();
		
		// Host might be null since it's not needed for a well-formed URI. Try authority instead
		if(host == null){
			host = projectURI.getAuthority();
		}
		
		// If authority is also null, use a generic name
		String location;
		
		if(host == null){
			location = "remote host";
		} else {
			location = projectURI.getScheme() + "://" + host;
		}

		return config.getName()
				+ " [" + getPlugin().getToolName(toolID) + "]" + " " + valgrindPath.toString() + " on " + location; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	protected String getPluginID() {
		return ValgrindLaunchPlugin.PLUGIN_ID;
	}

	public void onError(Throwable t) {
		// for now do nothing
	}
}
