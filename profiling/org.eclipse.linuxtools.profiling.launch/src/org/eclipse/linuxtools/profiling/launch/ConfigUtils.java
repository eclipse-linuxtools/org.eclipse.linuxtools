package org.eclipse.linuxtools.profiling.launch;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.profiling.launch.ProfileLaunchPlugin;

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
	public boolean getCopyExecutable()
			throws CoreException {
		boolean copyExecutable = config.getAttribute(
				RemoteProxyCMainTab.ATTR_ENABLE_COPY_FROM_EXE, false);
		return copyExecutable;
	}

	/**
	 * Get the absolute path of the executable to copy from. If the executable is
	 * on a remote machine, this is the path to the executable on that machine.
	 *
	 * @throws CoreException
	 */
	public String getCopyFromExecutablePath()
			throws CoreException {
		String executablePath = config.getAttribute(
				RemoteProxyCMainTab.ATTR_COPY_FROM_EXE_NAME, EMPTY_STRING);
		return executablePath;
	}

	/**
	 * Get the absolute path of the executable to launch. If the executable is
	 * on a remote machine, this is the path to the executable on that machine.
	 *
	 * @throws CoreException
	 */
	public String getExecutablePath()
			throws CoreException {
		String executablePath = config.getAttribute(
				ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EMPTY_STRING);
		return executablePath;
	}

	/**
	 * Verify the validity of executable path. If the executable is to be
	 * copied, then no additional verification is required. Otherwise, the path
	 * must point to an existing file.
	 *
	 * @param monitor
	 *            progress monitor
	 * @return IPath representing path to the executable (either local or
	 *         remote)
	 * @throws CoreException
	 *             if the resource can't be found or the monitor was canceled.
	 * @since 5.0
	 */
	public IPath verifyExecutablePath(
			IProgressMonitor monitor) throws CoreException {
		String executablePath = getExecutablePath();
		URI executableURI;

		try {
			executableURI = new URI(executablePath);
		} catch (URISyntaxException e) {
			return null;
		}
		RemoteConnection rc = new RemoteConnection(executableURI);
		IFileStore fs = rc.getRmtFileProxy().getResource(executableURI.getPath());
		IFileInfo fi = fs.fetchInfo();

		if (fi.exists())
			return Path.fromOSString(executableURI.getPath());
		else
			return null;
	}

	/**
	 * Get the working directory path for the application launch
	 *
	 * @return The working directory.
	 * @throws CoreException
	 * @since 5.0
	 */
	public String getWorkingDirectory()
			throws CoreException {
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
	 * Verify that the project exists prior to the launch.
	 *
	 * @return The existing project.
	 * @throws CoreException
	 */
	protected IProject verifyProject(ILaunchConfiguration configuration) throws CoreException {
		String proName = getProjectName(configuration);
		if (proName == null) {
			throw new CoreException(new Status(IStatus.ERROR, ProfileLaunchPlugin.PLUGIN_ID,
					"Messages.AbstractParallelLaunchConfigurationDelegate_Project_not_specified")); //$NON-NLS-1$
		}

		IProject project = getProject(proName);
		if (project == null || !project.exists() || !project.isOpen()) {
			throw new CoreException(new Status(IStatus.ERROR, ProfileLaunchPlugin.PLUGIN_ID,
					"Messages.AbstractParallelLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_project")); //$NON-NLS-1$
		}

		return project;
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

	/**
	 * Get the name of the project
	 *
	 * @return The name of the project.
	 * @throws CoreException
	 */
	public String getProjectName()
			throws CoreException {
		return getProjectName(config);
	}

	public static String getProjectName(ILaunchConfiguration configuration)
			throws CoreException {
		return configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}


}
