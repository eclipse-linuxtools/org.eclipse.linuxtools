/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences;

public class IDEPreferenceConstants {
    //ide
    public static final String P_STORED_TREE = "UseStoredTreePreference"; //$NON-NLS-1$
    public static final String P_REMOTE_PROBES = "RemoteProbes"; //$NON-NLS-1$

    //ide.path
    public static final String P_KERNEL_SOURCE = "KernelSourcePreference"; //$NON-NLS-1$
    public static final String P_EXCLUDED_KERNEL_SOURCE = "ExcludedKernelSource"; //$NON-NLS-1$
    public static final String P_REMOTE_LOCAL_KERNEL_SOURCE = "RemoteLocalKernelSource"; //$NON-NLS-1$

    //ide.stap.tapsets
    public static final String P_TAPSETS = "TapsetPreference"; //$NON-NLS-1$

    public static final int FLAG = 0;
    public static final int LABEL = 1;
    public static final int KEY = 2;
    public static final int TOOLTIP = 3;

    public static final String[][] STAP_BOOLEAN_OPTIONS  = new String[][] {
        {"-k", "Keep temporary directory", "kStapPreference", "Keep the temporary directory after all processing.  This may  be useful in order to examine the generated C code, or to reuse the compiled kernel object."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-u", "Unoptimized translation", "uStapPreference", "Unoptimized mode.  Disable unused code elision  during  elabora‐tion."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-b", "Bulk (relayfs) mode", "bStapPreference", "Use bulk mode (percpu files) for kernel-to-user data transfer."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-t", "Benchmarking timing information", "tStapPreference", "Collect timing information on the number of times probe executes and average amount of time spent in each probe-point. Also shows the derivation for each probe-point."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-v", "Increase verbosity", "vStapPreference", "Increase verbosity for all passes."}, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"--runtime=dyninst", "Use dyninst", "dyninstStapPreference", "Dyninst mode allows you to probe userspace processes without root access. This mode requires a -c COMMAND or a -x PID"}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public static final String[][] STAP_STRING_OPTIONS = new String[][] {
        {"-p", "Stop at pass", "pStapPreference", "Stop after the given pass number. The passes are numbered 1-5: parse, elaborate, translate, compile, run."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-s", "Buffer size", "sStapPreference", "The size of the buffer in megabytes used for kernel-to-user data transfer.  On a multiprocessor in bulk mode, this is a per-processor amount."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-R", "Runtime directory", "RStapPreference", "Look for the systemtap runtime sources in the given directory."},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-r", "Kernel release", "rStapPreference", "Specify a kernel version to use"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-o", "Output file", "oStapPreference", "Send standard output to the given file"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        {"-x", "Target PID", "xStapPreference", "Set target() to the given PID. This allows scripts to be written that filter on a specific process."}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    public static final String[] STAP_CMD_OPTION = new String[] {"-c", "CMD run CMD under systemtap", "cStapPreference", "start the probes, run CMD, and exit when it finishes"};  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

}
