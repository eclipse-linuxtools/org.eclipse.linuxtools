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
package org.eclipse.linuxtools.valgrind.memcheck;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchConfigurationDelegate;

public class MemcheckLaunchDelegate extends ValgrindLaunchConfigurationDelegate implements IValgrindLaunchDelegate {

	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IProgressMonitor monitor) throws CoreException {
	}
	
	public String[] getCommandArray(ILaunchConfiguration config) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();
		//opts.add(CommandLineConstants.OPT_XML + EQUALS + YES);
		
		opts.add(MemcheckCommandConstants.OPT_LEAKCHECK + EQUALS + YES);
		opts.add(MemcheckCommandConstants.OPT_SHOWREACH + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_LEAKRES + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES));
		opts.add(MemcheckCommandConstants.OPT_FREELIST + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST));
		opts.add(MemcheckCommandConstants.OPT_GCCWORK + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_PARTIAL + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_UNDEF + EQUALS + (config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF) ? YES : NO));
		opts.add(MemcheckCommandConstants.OPT_ALIGNMENT + EQUALS + config.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT));

		return opts.toArray(new String[opts.size()]);
	}
}
