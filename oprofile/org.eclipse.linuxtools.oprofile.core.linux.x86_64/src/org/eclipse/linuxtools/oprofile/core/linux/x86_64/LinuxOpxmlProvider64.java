/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.core.linux.x86_64;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.oprofile.core.OpxmlException;
import org.eclipse.linuxtools.oprofile.core.linux.LinuxOpxmlProvider;

public class LinuxOpxmlProvider64 extends LinuxOpxmlProvider {
	private static final String PLUGIN_ID = "org.eclipse.linuxtools.oprofile.core.linux.x86_64"; //$NON-NLS-1$
	private static final String OPXML_BINARY_NAME = "opxml"; //$NON-NLS-1$
	private static final String OPXML_BINARY_PATH = "$os$"; //$NON-NLS-1$

	public LinuxOpxmlProvider64() throws OpxmlException {
		super();
	}

	@Override
	public String _getOpxmlPath() {
		String opxmlPath = null;
		URL opxmlBinaryUrl = FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path(OPXML_BINARY_PATH + Path.SEPARATOR + OPXML_BINARY_NAME), null); 
		
		if (opxmlBinaryUrl != null) {
			try {
				opxmlPath = FileLocator.toFileURL(opxmlBinaryUrl).getPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return opxmlPath;
	}
}
