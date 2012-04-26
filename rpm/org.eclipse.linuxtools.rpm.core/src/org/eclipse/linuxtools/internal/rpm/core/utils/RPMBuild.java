/*******************************************************************************
 * Copyright (c) 2005-2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.profiling.launch.IRemoteCommandLauncher;
import org.eclipse.linuxtools.profiling.launch.IRemoteProxyManager;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
import org.eclipse.linuxtools.rpm.core.IProjectConfiguration;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


/**
 * A utility class for executing rpmbuild commands.
 * 
 */
public class RPMBuild {

	private static final String DEFINE = "--define"; //$NON-NLS-1$

	private String[] macroDefines;

	private String rpmBuildCmd;

	private String buildFolder;
	
	/**
	 * Constructs a new object.
	 * 
	 * @param config the RPM configuration to use
	 */
	public RPMBuild(IProjectConfiguration config) {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(IRPMConstants.RPM_CORE_ID);
		rpmBuildCmd = node.get(IRPMConstants.RPMBUILD_CMD, ""); //$NON-NLS-1$
		if (config.getSourcesFolder().getLocation()==null) {
			buildFolder = config.getBuildFolder().getLocationURI().toString();
			String fullRemoteDirectory = config.getSourcesFolder().getLocationURI().toString();
			String host = config.getSourcesFolder().getLocationURI().getHost();
			int startIndex = fullRemoteDirectory.indexOf(host)+host.length();
			String remoteDirectory = fullRemoteDirectory.substring(startIndex);
			String[] tmpMacroDefines = {
					//					rpmBuildCmd,
					"-v", //$NON-NLS-1$
					DEFINE, "_sourcedir " + //$NON-NLS-1$
					remoteDirectory + "/" + IRPMConstants.SOURCES_FOLDER, //$NON-NLS-1$
					DEFINE, "_srcrpmdir " + //$NON-NLS-1$
					remoteDirectory + "/" + IRPMConstants.SRPMS_FOLDER, //$NON-NLS-1$
					DEFINE, "_builddir " + //$NON-NLS-1$
					remoteDirectory + "/" + IRPMConstants.BUILD_FOLDER, //$NON-NLS-1$
					DEFINE, "_rpmdir " + //$NON-NLS-1$
					remoteDirectory + "/" + IRPMConstants.RPMS_FOLDER, //$NON-NLS-1$
					DEFINE, "_specdir " + //$NON-NLS-1$
					remoteDirectory + "/" + IRPMConstants.SPECS_FOLDER, }; //$NON-NLS-1$
			this.macroDefines = tmpMacroDefines;
		} else {
			String[] tmpMacroDefines = {
					rpmBuildCmd,
					"-v", //$NON-NLS-1$
					DEFINE, "_sourcedir " //$NON-NLS-1$
					+ config.getBuildFolder().getLocation().toOSString() + "/" + IRPMConstants.SOURCES_FOLDER, //$NON-NLS-1$
					DEFINE, "_srcrpmdir " + //$NON-NLS-1$
					config.getBuildFolder().getLocation().toOSString() + "/" + IRPMConstants.SRPMS_FOLDER, //$NON-NLS-1$
					DEFINE, "_builddir " + //$NON-NLS-1$
					config.getBuildFolder().getLocation().toOSString(),
					DEFINE, "_rpmdir " + //$NON-NLS-1$
					config.getBuildFolder().getLocation().toOSString() + "/" + IRPMConstants.RPMS_FOLDER, //$NON-NLS-1$
					DEFINE, "_specdir " + //$NON-NLS-1$
					config.getBuildFolder().getLocation().toOSString() + "/" + IRPMConstants.SPECS_FOLDER}; //$NON-NLS-1$
			this.macroDefines = tmpMacroDefines;
		}
	}

	/**
	 * Prepares the sources for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @throws CoreException If the operation fails.
	 */
	public void buildPrep(IResource specFile, OutputStream outStream) throws CoreException {
		build(specFile, outStream, "-bp"); //$NON-NLS-1$
	}


	/**
	 * Builds a binary RPM for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException if the operation fails
	 */
	public int buildBinary(IResource specFile, OutputStream outStream) throws CoreException {
		return build(specFile, outStream, "-bb"); //$NON-NLS-1$
	}

	/**
	 * Builds both a binary and source RPM for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException if the operation fails
	 */
	public int buildAll(IResource specFile, OutputStream outStream) throws CoreException {
		return build(specFile, outStream, "-ba"); //$NON-NLS-1$
	}


	/**
	 * Builds a source RPM for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException if the operation fails
	 */

	public int buildSource(IResource specFile, OutputStream outStream) throws CoreException {
		return build(specFile, outStream, "-bs"); //$NON-NLS-1$
	}

	/**
	 * 
	 * Operations for actually running rpmbuild binary
	 * 
	 * @param specFile The specfile used by rpmbuild
	 * @param outStream The stream to write the output to.
	 * @param buildParameter rpmbuild parameters
	 * @return The return code of the build job.
	 * @throws CoreException if the operation fails
	 * @since 0.4
	 */
	public int build(IResource specFile, OutputStream outStream, String buildParameter) throws CoreException {
		List<String> command = new ArrayList<String>();
		IRemoteProxyManager rmtProxyMgr;
		IRemoteCommandLauncher rmtCmdLauncher = null;
		command.addAll(Arrays.asList(macroDefines));
		command.add(buildParameter);
		String remoteSpec = ""; //$NON-NLS-1$
		if (specFile.getLocation()==null) {
			rmtProxyMgr = RemoteProxyManager.getInstance();
			try {
				rmtCmdLauncher = rmtProxyMgr.getLauncher(new URI(buildFolder));
			} catch (URISyntaxException e1) {
				throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
						e1.getMessage(), e1));

			}
			String host = specFile.getLocationURI().getHost();
			int startIndex = buildFolder.indexOf(host)+host.length();
			remoteSpec = specFile.getLocationURI().toString().substring(startIndex);
			command.add(remoteSpec);
			String empty[] = new String[0];
			Process pProxy = rmtCmdLauncher.execute(Path.fromOSString(rpmBuildCmd), command.toArray(new String[command.size()]), empty, null, new NullProgressMonitor());
			MessageConsole console = new MessageConsole("rpmbuild", null); //$NON-NLS-1$
			console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
			MessageConsoleStream stream = console.newMessageStream();

			if (pProxy != null){
				BufferedReader error = new BufferedReader(new InputStreamReader(pProxy.getErrorStream()));
				String err;
				try {
					err = error.readLine();
					while(err != null){
						stream.println(err);
						err = error.readLine();
					}
					error.close();
					BufferedReader reader = new BufferedReader(new InputStreamReader(pProxy.getInputStream()));
					String readLine = reader.readLine();
					while (readLine!=null) {
						stream.println(readLine);
						readLine=reader.readLine();
					}
					reader.close();
				} catch (IOException e) {
					throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
							e.getMessage(), e));
				} 
			}
			return Status.OK_STATUS.getCode();

		} else{
			command.add(specFile.getLocation().toString());
			try {
				return Utils.runCommand(outStream, command
						.toArray(new String[command.size()]));
			} catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
						e.getMessage(), e));
			}
		}
	}

}
