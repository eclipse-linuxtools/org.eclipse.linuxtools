package org.eclipse.linuxtools.internal.oprofile.remote.launch.launching;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.launching.OprofileLaunchConfigurationDelegate;
import org.eclipse.linuxtools.profiling.launch.ProxyLaunchMessages;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyCMainTab;


/**
 *  @since 1.1
 */
public class OprofileRemoteLaunchConfigurationDelegate extends OprofileLaunchConfigurationDelegate {
    private final static String EMPTY_STRING = ""; //$NON-NLS-1$

    @Override
    protected IProject getProject() {
        String name = null;
        try {
            name = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING);
        } catch (CoreException e) {
            return null;
        }
        if (name == null) {
            return null;
        }

        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    @Override
    protected IPath getExePath(ILaunchConfiguration config)
            throws CoreException {
        String pathString = config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
        URI uri;
        try {
            uri = new URI(pathString);
        } catch (URISyntaxException e) {
            throw new CoreException(new Status(IStatus.ERROR, OprofileCorePlugin.getId() ,
                    ProxyLaunchMessages.uri_of_executable_is_invalid));
        }
        IPath path = new Path(uri.getPath());

        return path;
    }

    @Override
    protected URI oprofileWorkingDirURI(ILaunchConfiguration config) throws CoreException {
        String workingDirString = config.getAttribute(RemoteProxyCMainTab.ATTR_REMOTE_WORKING_DIRECTORY_NAME, EMPTY_STRING);
        if(workingDirString.equals(EMPTY_STRING)){
            return getProject().getLocationURI();
        } else {
            URI workingDirUri;
            try {
                workingDirUri = new URI(workingDirString);
            } catch (URISyntaxException e) {
                Status status = new Status(
                        IStatus.ERROR,
                        OprofileCorePlugin.getId(),
                        OprofileLaunchMessages
                                .getString("oprofilelaunch.error.invalidworkingdir.status_message")); //$NON-NLS-1$
                throw new CoreException(status);
            }
            return workingDirUri;
        }
    }

}
