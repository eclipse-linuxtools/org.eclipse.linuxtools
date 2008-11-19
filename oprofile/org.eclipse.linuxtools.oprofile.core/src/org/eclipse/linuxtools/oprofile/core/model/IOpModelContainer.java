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

package org.eclipse.linuxtools.oprofile.core.model;

import java.io.File;

import org.eclipse.swt.widgets.Shell;

/**
 * An interface that all sample-containing "root" objects contain (files and sessions).
 */
public interface IOpModelContainer {
	/**
	 * Method getExecutableName().
	 *  @return the name of the executable with which this sample file corresponds.
	 */
	String getExecutableName();
	
	/**
	 * Method getFile().
	 * @return the File representing this container. This is a real File on disk.
	 */
	File getFile();
	
	/**
	 * Get any sample containers associated with this container.
	 * @param shell the shell to use for progress dialog (or <code>null</code> for none)
	 * @return any associated sample containers (never returns null)
	 */
	IOpModelContainer[] getSampleContainers(Shell shell);
	
	/**
	 * Get the samples contained in this container.
	 * @param shell the shell to use for progress dialog (or <code>null</code> for none)
	 * @return samples in this container
	 */
	OpModelSample[] getSamples(Shell shell);
	
	/**
	 * Computes the total of all samples in the implementing class.
	 * @return	the total count of all samples
	 */
	int getSampleCount();
}
