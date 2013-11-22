/*******************************************************************************
 * Copyright (c) 2004,2008,2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile.OprofileProject;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;

/**
 * This class wraps OProfile's global launch options for the
 * Eclipse launcher facility.
 */
public class LaunchOptions {
	// The launch options for the daemon
	private OprofileDaemonOptions options;
	private String oprofileComboText;
	private int executionsNumber;

	public LaunchOptions() {
		options = new OprofileDaemonOptions();
		oprofileComboText = OprofileProject.OPERF_BINARY;
		executionsNumber = 1;
	}

	/**
	 * Determines whether the global oprofile options represented by this
	 * object are valid
	 * @return whether the options are valid
	 */
	public boolean isValid() {
		IRemoteFileProxy proxy = null;
		try {
			proxy = RemoteProxyManager.getInstance().getFileProxy(getOprofileProject());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		// The only point of contention is whether the specified vmlinux *file* exists.
		String fn = options.getKernelImageFile();
		if (fn != null && fn.length() > 0) {
			IFileStore fileStore = proxy.getResource(options.getKernelImageFile());
			return (fileStore.fetchInfo().exists() && !fileStore.fetchInfo().isDirectory());
		}

		return true;
	}

	/**
	 * Get project to profile
	 * @return IProject project to profile
	 */
	protected IProject getOprofileProject(){
		return Oprofile.OprofileProject.getProject();
	}

	/**
	 * Saves the global options of this object into the specified launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(OprofileLaunchPlugin.ATTR_KERNEL_IMAGE_FILE, options.getKernelImageFile());
		config.setAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, options.getSeparateProfilesMask());
		config.setAttribute(OprofileLaunchPlugin.ATTR_EXECUTIONS_NUMBER, getExecutionsNumber());
		try {
			if (config.getType().getIdentifier().equals("org.eclipse.linuxtools.oprofile.launch.oprofile.manual")) { //$NON-NLS-1$
				config.setAttribute(OprofileLaunchPlugin.ATTR_OPROFILE_COMBO_TEXT, OprofileProject.OPCONTROL_BINARY);
				OprofileProject.setProfilingBinary(OprofileProject.OPCONTROL_BINARY);
			} else {
				config.setAttribute(OprofileLaunchPlugin.ATTR_OPROFILE_COMBO_TEXT, getOprofileComboText());
				OprofileProject.setProfilingBinary(getOprofileComboText());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads this object with the global options in the given launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config) {
		try {
			options.setKernelImageFile(config.getAttribute(OprofileLaunchPlugin.ATTR_KERNEL_IMAGE_FILE, "")); //$NON-NLS-1$
			options.setSeparateProfilesMask(config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, OprofileDaemonOptions.SEPARATE_NONE));
			setExecutionsNumber(config.getAttribute(OprofileLaunchPlugin.ATTR_EXECUTIONS_NUMBER, 1));
			setOprofileComboText(config.getAttribute(OprofileLaunchPlugin.ATTR_OPROFILE_COMBO_TEXT, OprofileProject.OPERF_BINARY));
		} catch (CoreException e) {
		}
	}

	/**
	 * Get the daemon launch options
	 * @return the OprofileDaemonOption
	 */
	public OprofileDaemonOptions getOprofileDaemonOptions() {
		return options;
	}

	/**
	 * Method getKernelImageFile.
	 * @return the kernel image file
	 */
	public String getKernelImageFile() {
		return options.getKernelImageFile();
	}

	/**
	 * Sets the kernel image file
	 * @param image	the kernel image file
	 */
	public void setKernelImageFile(String image) {
		options.setKernelImageFile(image);
	}

	/**
	 * Method getSeparateSamples.
	 * @return whether and how to separate samples for each distinct application
	 */
	public int getSeparateSamples() {
		return options.getSeparateProfilesMask();
	}

	/**
	 * Sets whether/how to collect separate samples for each distinct application
	 * @param how	one of SEPARATE_{NONE, LIBRARY, KERNEL, THREAD, CPU}
	 */
	public void setSeparateSamples(int how) {
		options.setSeparateProfilesMask(how);
	}

	/**
	 * Returns the path of the binary to profile.
	 * @return the full path to the binary being profile
	 */
	public String getBinaryImage() {
		return options.getBinaryImage();
	}

	/**
	 * Sets the binary to profile in this launch.
	 * @param image string of the full path to the binary
	 */
	public void setBinaryImage(String image) {
		options.setBinaryImage(image);
	}

	public int getExecutionsNumber() {
		return executionsNumber;
	}

	public void setExecutionsNumber(int executionsNumber) {
		this.executionsNumber = executionsNumber;
	}

	/**
	 * Gets OProfile combo box text regarding the binary
	 * opcontrol or operf
	 * @since 2.1
	 */
	public String getOprofileComboText() {
		return oprofileComboText;
	}

	/**
	 * Sets OProfile combo box text regarding the binary
	 * opcontrol or operf
	 * @since 2.1
	 */
	public void setOprofileComboText(String oprofileComboText) {
		this.oprofileComboText = oprofileComboText;
	}

}
