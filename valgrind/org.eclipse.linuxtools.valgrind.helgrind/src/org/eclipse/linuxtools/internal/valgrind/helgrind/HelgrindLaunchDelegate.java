/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.helgrind;


import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.valgrind.launch.IValgrindLaunchDelegate;
import org.eclipse.linuxtools.valgrind.ui.IValgrindToolView;
import org.osgi.framework.Version;

public class HelgrindLaunchDelegate implements IValgrindLaunchDelegate {
	private static final String EQUALS = "="; //$NON-NLS-1$
	private static final String NO = "no"; //$NON-NLS-1$
	private static final String YES = "yes"; //$NON-NLS-1$

	public void handleLaunch(ILaunchConfiguration config, ILaunch launch, IPath outDir, IProgressMonitor monitor) throws CoreException {
	}
	
	public String[] getCommandArray(ILaunchConfiguration config, Version ver, IPath logDir) throws CoreException {
		ArrayList<String> opts = new ArrayList<String>();

		opts.add(HelgrindCommandConstants.OPT_TRACK_LOCKORDERS + EQUALS + (config.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_LOCKORDERS, HelgrindLaunchConstants.DEFAULT_HELGRIND_LOCKORDERS) ? YES : NO));
		opts.add(HelgrindCommandConstants.OPT_HISTORY_LEVEL + EQUALS + config.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_HISTORYLEVEL, HelgrindLaunchConstants.DEFAULT_HELGRIND_HISTORYLEVEL));
		opts.add(HelgrindCommandConstants.OPT_CONFLICT_CACHE_SIZE + EQUALS + config.getAttribute(HelgrindLaunchConstants.ATTR_HELGRIND_CACHESIZE, HelgrindLaunchConstants.DEFAULT_HELGRIND_CACHESIZE));
		return opts.toArray(new String[opts.size()]);
	}

	public void initializeView(IValgrindToolView view, String contentDescription, IProgressMonitor monitor)
			throws CoreException {
	}
}
