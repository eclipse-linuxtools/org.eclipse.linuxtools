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

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;

/**
 * This class wraps OProfile's global launch options for the
 * Eclipse launcher facility.
 */
public class LaunchOptions {
	// The launch options for the daemon
	private OprofileDaemonOptions _options;

	public LaunchOptions() {
		_options = new OprofileDaemonOptions();
	}
	
	/**
	 * Determines whether the global oprofile options represented by this
	 * object are valid
	 * @return whether the options are valid
	 */
	public boolean isValid() {
		// The only point of contention is whether the specified vmlinux *file* exists.
		String fn = _options.getKernelImageFile();
		if (fn != null && fn.length() > 0) {
			File file = new File(_options.getKernelImageFile());
			return (file.exists() && file.isFile());
		}
		
		return true;
	}
	
	/**
	 * Saves the global options of this object into the specified launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void saveConfiguration(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(OprofileLaunchPlugin.ATTR_KERNEL_IMAGE_FILE, _options.getKernelImageFile());
		config.setAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, _options.getSeparateProfilesMask());
	}
	
	/**
	 * Loads this object with the global options in the given launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config) {
		try {
			_options.setKernelImageFile(config.getAttribute(OprofileLaunchPlugin.ATTR_KERNEL_IMAGE_FILE, "")); //$NON-NLS-1$
			_options.setSeparateProfilesMask(config.getAttribute(OprofileLaunchPlugin.ATTR_SEPARATE_SAMPLES, OprofileDaemonOptions.SEPARATE_NONE));
		} catch (CoreException e) {
		}
	}
	
	/**
	 * Get the daemon launch options
	 * @return the OprofileDaemonOption
	 */
	public OprofileDaemonOptions getOprofileDaemonOptions() {
		return _options;
	}
	
	/**
	 * Method getKernelImageFile.
	 * @return the kernel image file
	 */
	public String getKernelImageFile() {
		return _options.getKernelImageFile();
	}
	
	/**
	 * Sets the kernel image file
	 * @param image	the kernel image file
	 */
	public void setKernelImageFile(String image) {
		_options.setKernelImageFile(image);
	}

	/**
	 * Method getSeparateSamples.
	 * @return whether and how to separate samples for each distinct application
	 */
	public int getSeparateSamples() {
		return _options.getSeparateProfilesMask();
	}

	/**
	 * Sets whether/how to collect separate samples for each distinct application
	 * @param how	one of SEPARATE_{NONE, LIBRARY, KERNEL, THREAD, CPU}
	 */
	public void setSeparateSamples(int how) {
		_options.setSeparateProfilesMask(how);
	}
	
	/**
	 * Returns the path of the binary to profile.
	 * @return the full path to the binary being profile
	 */
	public String getBinaryImage() {
		return _options.getBinaryImage();
	}

	/**
	 * Sets the binary to profile in this launch.
	 * @param _image string of the full path to the binary
	 */
	public void setBinaryImage(String _image) {
		_options.setBinaryImage(_image);
	}
}
