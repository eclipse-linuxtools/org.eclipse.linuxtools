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
package org.eclipse.linuxtools.valgrind.launch;

import java.io.IOException;

import org.eclipse.core.runtime.IPath;

/**
 * Interface consulted by Launch Delegate for a directory
 * to hold output files.
 */
public interface IValgrindOutputDirectoryProvider {

	/**
	 * Obtains a directory to store Valgrind output files.
	 * @return the absolute path to this directory
	 */
	public abstract IPath getOutputPath() throws IOException;

}