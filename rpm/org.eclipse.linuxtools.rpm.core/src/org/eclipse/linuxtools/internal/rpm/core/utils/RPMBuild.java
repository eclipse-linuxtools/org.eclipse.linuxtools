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

import java.io.IOException;
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

/**
 * A utility class for executing rpmbuild commands.
 *
 */
public class RPMBuild {

    private List<String> macroDefines = new ArrayList<>();

    private String rpmBuildCmd;

    private String mainFolder;

    /**
     * Constructs a new object.
     *
     * @param config
     *            the RPM configuration to use
     */
    public RPMBuild(IProjectConfiguration config) {
        IEclipsePreferences node = DefaultScope.INSTANCE
                .getNode(IRPMConstants.RPM_CORE_ID);
        if (config.getBuildFolder().getLocation() == null) {
            mainFolder = config.getSourcesFolder().getLocationURI().toString();
        }
        rpmBuildCmd = node.get(IRPMConstants.RPMBUILD_CMD, ""); //$NON-NLS-1$
        String[] tmpMacroDefines = { rpmBuildCmd, "-v" }; //$NON-NLS-1$
        macroDefines.addAll(Arrays.asList(tmpMacroDefines));
        macroDefines.addAll(config.getConfigDefines());
    }

    /**
     * Prepares the sources for a given spec file.
     *
     * @param specFile
     *            the spec file
     * @param outStream
     *            The stream to write the output to.
     * @return The return code of the build job.
     * @throws CoreException
     *             If the operation fails.
     */
    public IStatus buildPrep(IResource specFile, OutputStream outStream)
            throws CoreException {
        return build(specFile, outStream, "-bp"); //$NON-NLS-1$
    }

    /**
     * Builds a binary RPM for a given spec file.
     *
     * @param specFile
     *            the spec file
     * @param outStream
     *            The stream to write the output to.
     * @return The return code of the build job.
     * @throws CoreException
     *             if the operation fails
     */
    public IStatus buildBinary(IResource specFile, OutputStream outStream)
            throws CoreException {
        return build(specFile, outStream, "-bb"); //$NON-NLS-1$
    }

    /**
     * Builds both a binary and source RPM for a given spec file.
     *
     * @param specFile
     *            the spec file
     * @param outStream
     *            The stream to write the output to.
     * @return The return code of the build job.
     * @throws CoreException
     *             if the operation fails
     */
    public IStatus buildAll(IResource specFile, OutputStream outStream)
            throws CoreException {
        return build(specFile, outStream, "-ba"); //$NON-NLS-1$
    }

    /**
     * Builds a source RPM for a given spec file.
     *
     * @param specFile
     *            the spec file
     * @param outStream
     *            The stream to write the output to.
     * @return The return code of the build job.
     * @throws CoreException
     *             if the operation fails
     */

    public IStatus buildSource(IResource specFile, OutputStream outStream)
            throws CoreException {
        return build(specFile, outStream, "-bs"); //$NON-NLS-1$
    }

    /**
     *
     * Operations for actually running rpmbuild binary
     *
     * @param specFile
     *            The specfile used by rpmbuild
     * @param outStream
     *            The stream to write the output to.
     * @param buildParameter
     *            rpmbuild parameters
     * @return The return code of the build job.
     * @throws CoreException
     *             if the operation fails
     * @since 0.4
     */
    public IStatus build(IResource specFile, OutputStream outStream,
            String buildParameter) throws CoreException {
        if (specFile == null) {
            throw new CoreException(new Status(IStatus.ERROR,
                    IRPMConstants.RPM_CORE_ID, Messages.Specfile_not_found));
        }

        final List<String> command = new ArrayList<>();
        command.addAll(macroDefines);
        command.add(buildParameter);

        try {
            if (specFile.getLocation() == null) {
                return remoteBuild(specFile, outStream, command);
            } else {
                command.add(specFile.getLocation().toString());
                return Utils.runCommand(outStream, specFile.getProject(),
                        command.toArray(new String[command.size()]));
            }
        } catch (IOException | URISyntaxException e) {
            throw new CoreException(new Status(IStatus.ERROR,
                    IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
        }
    }

    private IStatus remoteBuild(IResource specFile, OutputStream outStream,
            List<String> command) throws CoreException, URISyntaxException {
        command.remove(0);
        IRemoteProxyManager rmtProxyMgr = RemoteProxyManager.getInstance();
        IRemoteCommandLauncher rmtCmdLauncher = null;
        rmtCmdLauncher = rmtProxyMgr.getLauncher(new URI(mainFolder));

        command.add(specFile.getLocationURI().getPath());
        String empty[] = new String[0];
        Process pProxy = rmtCmdLauncher.execute(
                Path.fromOSString(rpmBuildCmd),
                command.toArray(new String[command.size()]), empty, null,
                new NullProgressMonitor());

        if (pProxy != null) {
            return Utils.watchProcess(outStream, pProxy);
        }
        return Status.OK_STATUS;
    }

}
