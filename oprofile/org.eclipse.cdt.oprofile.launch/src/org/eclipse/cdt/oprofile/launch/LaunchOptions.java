/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.launch;

import java.io.File;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.cdt.oprofile.core.OprofileDaemonOptions;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * This class wraps OProfile's global launch options for the
 * Eclipse launcher facility.
 * @author Keith Seitz <keiths@redhat.com>
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
		config.setAttribute(LaunchPlugin.ATTR_KERNEL_IMAGE_FILE, _options.getKernelImageFile());
		config.setAttribute(LaunchPlugin.ATTR_VERBOSE_LOGGING, _options.getVerboseLogging());
		config.setAttribute(LaunchPlugin.ATTR_SEPARATE_SAMPLES, _options.getSeparateProfilesMask());
	}
	
	/**
	 * Loads this object with the global options in the given launch
	 * configuration
	 * @param config	the launch configuration
	 */
	public void loadConfiguration(ILaunchConfiguration config) {
		try {
			_options.setKernelImageFile(config.getAttribute(LaunchPlugin.ATTR_KERNEL_IMAGE_FILE, Oprofile.getKernelImageFile()));
			_options.setVerboseLogging(config.getAttribute(LaunchPlugin.ATTR_VERBOSE_LOGGING, false));
			_options.setSeparateProfilesMask(config.getAttribute(LaunchPlugin.ATTR_SEPARATE_SAMPLES, OprofileDaemonOptions.SEPARATE_NONE));
		} catch (CoreException e) {
		}
	}
	
	/**
	 * Method getKernelImageFile.
	 * @return the kernel image file
	 */
	public String getKernelImageFile() {
		return _options.getKernelImageFile();
	}
	
	/**
	 * Method getProcessIdFilter().
	 * @return the process id filter in use
	 *
	public String getProcessIdFilter() {
		return _processIdFilter;
	}*/
	
	/**
	 * Method getProcessGroupFilter.
	 * @return the process group filter in use
	 *
	public String getProcessGroupFilter() {
		return _processGroupFilter;
	}*/
	

	/**
	 * Method getVerboseLogging.
	 * @return whether to be verbose in the daemon log
	 */
	public boolean getVerboseLogging() {
		return _options.getVerboseLogging();
	}
	
	/**
	 * Method getSeparateSamples.
	 * @return whether and how to separate samples for each distinct application
	 */
	public int getSeparateSamples() {
		return _options.getSeparateProfilesMask();
	}
	
	/**
	 * Sets the kernel image file
	 * @param image	the kernel image file
	 */
	public void setKernelImageFile(String image) {
		_options.setKernelImageFile(image);
	}
		
	/**
	 * Sets whether to enable verbose logging in the daemon log
	 * @param b	whether to enable verbose logging
	 */
	public void setVerboseLogging(boolean b) {
		_options.setVerboseLogging(b);
	}
	
	/**
	 * Sets whether/how to collect separate samples for each distinct application
	 * @param how	one of SEPARATE_NONE, SEPARATE_LIBRARY, SEPARATE_KERNEL, SEPARATE_ALL
	 */
	public void setSeparateSamples(int how) {
		_options.setSeparateProfilesMask(how);
	}
	
	/**
	 * Get the daemon launch options
	 * @return the OprofileDaemonOption
	 */
	public OprofileDaemonOptions getOprofileDaemonOptions() {
		return _options;
	}
}
