/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard.structures;

import java.io.File;
import java.io.FileFilter;

/**
 * This is a simple file filter that will exclude anything other then a Dashboard module and
 * a folder.
 * @author Ryan Morse
 */
public class DashboardModuleFileFilter implements FileFilter {
	@Override
	public boolean accept(File f) {
		return f.isDirectory() || f.getName().toLowerCase().endsWith(DashboardModuleExtension);
	}
	
	public String getDescription() {
		return ".dash files"; //$NON-NLS-1$
	}
	
	public static final String DashboardModuleExtension = ".dash"; //$NON-NLS-1$
}
