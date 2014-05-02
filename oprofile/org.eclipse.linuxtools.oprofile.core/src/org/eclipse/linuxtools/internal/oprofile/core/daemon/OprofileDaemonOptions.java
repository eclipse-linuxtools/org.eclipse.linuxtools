/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.daemon;


/**
 * This class represents the global launch options for the
 * OProfile daemon.
 */
public class OprofileDaemonOptions {
    /**
     *  Kernel image file
     */
    private String kernelImageFile;

    /**
     *  How to separate profiles (mask)
     */
    private int separateProfiles;

    /**
     *  The image to profile
     */
    private String binaryImage;

    /**
     * How many calls down to profile
     */
    private int callgraphDepth;

    /**
     * Sample separation options. Determines how oprofiled will group
     *   samples for binaries which isn't the main binary being profiled.
     *
     * Currently only properly support:
     *   -none: ignore all other binaries
     *   -library: include shared library samples
     *   -kernel: include kernel module samples (which implicitly includes library)
     *
     *   the others probably wouldn't show nicely in the view
     */
    public static final int SEPARATE_NONE = 0;
    public static final int SEPARATE_LIBRARY = 1;
    public static final int SEPARATE_KERNEL = 2;
    public static final int SEPARATE_THREAD = 4;
    public static final int SEPARATE_CPU = 8;

    /**
     * Constructor
     */
    public OprofileDaemonOptions() {
        //defaults
        kernelImageFile = ""; //$NON-NLS-1$
        separateProfiles = SEPARATE_NONE;
        binaryImage = ""; //$NON-NLS-1$
        callgraphDepth = 0;
    }

    /**
     * Get the kernel image file
     * @return the kernel image file
     */
    public String getKernelImageFile() {
        return kernelImageFile;
    }

    /**
     * Set the kernel image file
     * @param image the kernel image
     */
    public void setKernelImageFile(String image) {
        kernelImageFile = image;
    }

    /**
     * Get daemon profile separation mask
     * @return mask of options
     */
    public int getSeparateProfilesMask() {
        return separateProfiles;
    }

    /**
     * Set daemon profile separation mask
     * @param mask the new separation mask
     */
    public void setSeparateProfilesMask(int mask) {
        separateProfiles = mask;
    }

    /**
     * Get the path to the binary image being profiled.
     * @return full path to the binary
     */
    public String getBinaryImage() {
        return binaryImage;
    }

    /**
     * Sets the path of the binary image to profile.
     * @param image full path to the binary
     */
    public void setBinaryImage(String image) {
        this.binaryImage = image;
    }

    /**
     * Get the call depth value.
     * @return integer amount of calls down to profile
     */
    public int getCallgraphDepth() {
        return callgraphDepth;
    }
}
