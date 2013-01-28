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

	//ide.editor
	public static final String P_EDITOR_BACKGROUND = "EditorBackgroundPreference"; //$NON-NLS-1$
	public static final String P_SHOW_LINE_NUMBERS = "ShowLineNumbers"; //$NON-NLS-1$
	
	//ide.editor.codeassist
	public static final String P_USE_CODE_ASSIST = "UseCodeAssistPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION = "CompletionPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION_INSERT = "CompletionInsertPreference"; //$NON-NLS-1$
	public static final String P_COMPLETION_OVERWRITE = "CompletionOverwritePreference"; //$NON-NLS-1$
	public static final String P_ACTIVATION_DELAY = "ActivationDelayPreference"; //$NON-NLS-1$
	public static final String P_ACTIVATION_TRIGGER = "ActivationTriggerPreference"; //$NON-NLS-1$
	
	//ide.editor.preferenceconstants
	public static final String P_CONDITIONAL_FILTERS = "ConditionalFilters"; //$NON-NLS-1$
	
	//ide.stap.stapoptions
	public static final String[][] P_STAP = new String[][] {
		{"-k", "\tkeep temporary directory", "kStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-u", "\tunoptimized translation", "uStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-b", "\tbulk (relayfs) mode", "bStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-t", "\tbenchmarking timing information", "tStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-v", "\t\t\tincrease verbosity [0]", "vStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-p NUM", "\t\tstop after pass NUM 1-5", "pStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-s NUM", "\t\tbuffer size in megabytes", "sStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-D NM=VAL", "\temit macro definition into C code", "DStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-R DIR", "\t\tlook in DIR for runtime", "RStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-r RELEASE", "\tuse kernel RELEASE", "rStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-m MODULE", "\tset probe module name", "mStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-o FILE", "\t\tsend output to file", "oStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-c CMD", "\t\tstart the probes, run CMD, and exit when it finishes", "cStapPreference"},  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		{"-x PID", "\t\tsets target() to PID", "xStapPreference"}}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public static final String[] P_STAP_OPTS = new String[] {
		"vStapOptPreference", //$NON-NLS-1$
		"pStapOptPreference", //$NON-NLS-1$
		"sStapOptPreference", //$NON-NLS-1$
		"DStapOptPreference", //$NON-NLS-1$
		"RStapOptPreference", //$NON-NLS-1$
		"rStapOptPreference", //$NON-NLS-1$
		"mStapOptPreference", //$NON-NLS-1$
		"oStapOptPreference", //$NON-NLS-1$
		"cStapOptPreference", //$NON-NLS-1$
		"xStapOptPreference" //$NON-NLS-1$
	};
	
	//ide.editor.syntaxcoloring
	public static final String P_STP_DEFAULT_COLOR = "stpDefaultColorPreference"; //$NON-NLS-1$
	public static final String P_STP_KEYWORD_COLOR = "stpKeywordColorPreference"; //$NON-NLS-1$
	public static final String P_STP_EMBEDDED_C_COLOR = "stpEmbeddedCColorPreference"; //$NON-NLS-1$
	public static final String P_STP_EMBEDDED_COLOR = "stpEmbeddedColorPreference"; //$NON-NLS-1$
	public static final String P_STP_COMMENT_COLOR = "stpCommentColorPreference"; //$NON-NLS-1$
	public static final String P_STP_TYPE_COLOR = "stpTypeColorPreference"; //$NON-NLS-1$
	public static final String P_STP_STRING_COLOR = "stpStringColorPreference"; //$NON-NLS-1$
	public static final String P_C_DEFAULT_COLOR = "cDefaultColorPreference"; //$NON-NLS-1$
	public static final String P_C_KEYWORD_COLOR = "cKeywordColorPreference"; //$NON-NLS-1$
	public static final String P_C_COMMENT_COLOR = "cCommentColorPreference"; //$NON-NLS-1$
	public static final String P_C_PREPROCESSOR_COLOR = "cPreprocessorColorPreference"; //$NON-NLS-1$
	public static final String P_C_TYPE_COLOR = "cTypeColorPreference"; //$NON-NLS-1$
	public static final String P_C_STRING_COLOR = "cStringColorPreference"; //$NON-NLS-1$
	
}
