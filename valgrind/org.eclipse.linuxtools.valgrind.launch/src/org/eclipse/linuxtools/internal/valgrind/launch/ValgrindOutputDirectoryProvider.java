/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider;

public class ValgrindOutputDirectoryProvider implements IValgrindOutputDirectoryProvider {
	protected IPath outputPath;

	public ValgrindOutputDirectoryProvider() {
		outputPath = ValgrindLaunchPlugin.getDefault().getStateLocation();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.valgrind.launch.IValgrindOutputDirectoryProvider#getOutputPath()
	 */
	@Override
	public IPath getOutputPath() {
		return outputPath;
	}

}
