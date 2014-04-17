package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 1.1
 */
public class ConfigUtils {

	private final static String EMPTY_STRING = ""; //$NON-NLS-1$

	private ILaunchConfiguration config;

	public ConfigUtils(ILaunchConfiguration config) {
		this.config = config;
	}

	/**
	 * Get if the executable shall be copied to remote target before launch.
	 *
	 * @return To copy executable or not.
	 * @throws CoreException
	 */
	public boolean getCopyExecutable()	throws CoreException {
		return config.getAttribute(
				RemoteProxyCMainTab.ATTR_ENABLE_COPY_FROM_EXE, false);
	}

	/**
	 * Get the absolute path of the executable to copy from. If the executable is
	 * on a remote machine, this is the path to the executable on that machine.
	 * @return The path to the executable.
	 *
	 * @throws CoreException
	 */
	public String getCopyFromExecutablePath()
			throws CoreException {
		return config.getAttribute(
				RemoteProxyCMainTab.ATTR_COPY_FROM_EXE_NAME, EMPTY_STRING);
	}

	/**
	 * Get the absolute path of the executable to launch. If the executable is
	 * on a remote machine, this is the path to the executable on that machine.
	 * @return The path to the executable to launch.
	 *
	 * @throws CoreException
	 */
	public String getExecutablePath()
			throws CoreException {
		return config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
	}

	/**
	 * Get the working directory path for the application launch
	 *
	 * @return The working directory.
	 * @throws CoreException
	 * @since 5.0
	 */
	public String getWorkingDirectory()	throws CoreException {
		String workingDirectory = config.getAttribute(
				RemoteProxyCMainTab.ATTR_REMOTE_WORKING_DIRECTORY_NAME, EMPTY_STRING);
		if (workingDirectory.length() == 0)
			return null;
		URI workingDirectoryURI;
		try {
			workingDirectoryURI = new URI(workingDirectory);
		} catch (URISyntaxException e) {
			// Just treat it as unusable.
			return null;
		}
		return workingDirectoryURI.getPath();
	}

	/**
	 * Get the IProject object from the project name.
	 *
	 * @param project
	 *            name of the project
	 * @return IProject resource
	 */
	public static IProject getProject(String project) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(project);
	}

	public IProject getProject() throws CoreException {
        return getProject(getProjectName());
    }

	/**
	 * Get the name of the project
	 *
	 * @return The name of the project.
	 * @throws CoreException
	 */
	public String getProjectName() throws CoreException {
		return getProjectName(config);
	}

	public static String getProjectName(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}


}
