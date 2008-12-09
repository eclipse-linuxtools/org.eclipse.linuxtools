/*******************************************************************************
 * Copyright (c) 2004 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core;


/**
 * This class represents the global launch options for the
 * OProfile daemon.
 */
public class OprofileDaemonOptions {
	// Kernel image file
	private String _kernelImageFile;
	
	// Enable verbose logging?
	private boolean _verboseLogging;
	
	// How to separate profiles (mask)
	private int _separateProfiles;
	
	// the image to profile
	private String _image;
	
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
		//defaults
//		_kernelImageFile = Oprofile.getKernelImageFile();
		_kernelImageFile = "";
		_verboseLogging = false;
		_separateProfiles = SEPARATE_NONE;	
		_image = "";
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

	public String getImage() {
		return _image;
	}

	public void setImage(String _image) {
		this._image = _image;
	}

}
