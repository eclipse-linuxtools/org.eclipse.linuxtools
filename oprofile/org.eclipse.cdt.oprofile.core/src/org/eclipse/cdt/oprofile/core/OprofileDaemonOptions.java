/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/
package org.eclipse.cdt.oprofile.core;

/**
 * This class represents the global launch options for the
 * OProfile daemon.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class OprofileDaemonOptions {
	// Kernel image file
	private String _kernelImageFile;
	
	// Enable verbose logging?
	private boolean _verboseLogging;
	
	// How to separate profiles (mask)
	private int _separateProfiles;
	
	/*
	 * Separate profile masks
	 */
	
	/**
	 * No profile separation
	 */
	public static final int SEPARATE_NONE = 0;
	
	/**
	 * Separate shared library profiles per-application
	 */
	public static final int SEPARATE_LIBRARY = 1;
	
	/**
	 * Separate shared library and kernel profiles per-application
	 */
	public static final int SEPARATE_KERNEL = 2;
	
	/**
	 * Per-thread/process profiles
	 */
	public static final int SEPARATE_THREAD = 4;
	
	/**
	 * Per-CPU profiles
	 */
	public static final int SEPARATE_CPU = 8;
	
	/**
	 * Separate all profiles
	 */
	public static final int SEPARATE_ALL = 512;
	
	public OprofileDaemonOptions() {
		_kernelImageFile = Oprofile.getKernelImageFile();
		_verboseLogging = false;
		_separateProfiles = SEPARATE_NONE;	
	}
	
	/**
	 * Get the kernel image file
	 * @return the kernel image file
	 */
	public String getKernelImageFile() {
		return _kernelImageFile;
	}
	
	/**
	 * Set the kernel image file
	 * @param image the kernel image
	 */
	public void setKernelImageFile(String image) {
		_kernelImageFile = image;
	}

	/**
	 * Get daemon verbose logging
	 * @return whether verbose logging is enabled
	 */
	public boolean getVerboseLogging() {
		return _verboseLogging;
	}
	
	/**
	 * Set daemon verbose logging
	 * @param logging whether to enable verbose logging
	 */
	public void setVerboseLogging(boolean logging) {
		_verboseLogging = logging;
	}

	/**
	 * Get daemon profile separation mask
	 * @return mask of options
	 */
	public int getSeparateProfilesMask() {
		return _separateProfiles;
	}
	
	/**
	 * Set daemon profile separation mask
	 * @param mask the new separation mask
	 */
	public void setSeparateProfilesMask(int mask) {
		_separateProfiles = mask;
	}
}
