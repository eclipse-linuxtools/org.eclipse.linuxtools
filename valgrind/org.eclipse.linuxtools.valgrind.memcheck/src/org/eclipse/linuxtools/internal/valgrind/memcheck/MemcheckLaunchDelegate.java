/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.memcheck;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.osgi.framework.Version;

public class MemcheckLaunchDelegate implements IValgrindLaunchDelegate {
	private static final Version VER_3_4_0 = new Version(3, 4, 0);
	private static final Version VER_3_6_0 = new Version(3, 6, 0);
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String NO = "no"; //$NON-NLS-1$
	private static final String YES = "yes"; //$NON-NLS-1$
	private static final String HEX = "0x"; //$NON-NLS-1$

	@Override
	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IPath outDir, IProgressMonitor monitor) throws CoreException {
	}
	
	@Override
	public String[] getCommandArray(ILaunchConfiguration config, Version ver, IPath logDir) throws CoreException {
		ArrayList<String> opts = new ArrayList<>();
		
		opts.add(MemcheckCommandConstants.OPT_LEAKCHECK + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKCHECK) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_SHOWREACH + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_LEAKRES + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES));
		opts.add(MemcheckCommandConstants.OPT_FREELIST + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST));
		opts.add(MemcheckCommandConstants.OPT_GCCWORK + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_PARTIAL + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_UNDEF + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF) ? YES : NO));
		if (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_BOOL)) {
			opts.add(MemcheckCommandConstants.OPT_ALIGNMENT + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_VAL));
		}
		
		// VG >= 3.4.0
		if (ver == null || ver.compareTo(VER_3_4_0) >= 0) {
			if (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS) != MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS)
				opts.add(MemcheckCommandConstants.OPT_TRACKORIGINS + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS) ? YES : NO));
		}
		
		// VG >= 3.6.0
		if (ver == null || ver.compareTo(VER_3_6_0) >= 0) {
			if (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_POSSIBLY_LOST_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL) != MemcheckLaunchConstants.DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL)
				opts.add(MemcheckCommandConstants.OPT_SHOW_POSSIBLY_LOST + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_POSSIBLY_LOST_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL) ? YES : NO));
		}
		
		if (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_MALLOCFILL_BOOL)) {
			opts.add(MemcheckCommandConstants.OPT_MALLOCFILL + EQUALS + HEX + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_MALLOCFILL_VAL));
		}

		if (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREEFILL_BOOL)) {
			opts.add(MemcheckCommandConstants.OPT_FREEFILL + EQUALS + HEX + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREEFILL_VAL));
		}
		List<String> ignoreRangesFns = config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_IGNORE_RANGES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_IGNORE_RANGES);
		for (String func : ignoreRangesFns) {
			opts.add(MemcheckCommandConstants.OPT_IGNORERANGES + EQUALS + func);
		}
		return opts.toArray(new String[opts.size()]);
	}

	@Override
	public void initializeView(IValgrindToolView view, String contentDescription, IProgressMonitor monitor)
			throws CoreException {
	}
}
