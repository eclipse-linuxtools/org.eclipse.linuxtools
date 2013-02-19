/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.profiling.launch.ui;

import java.net.URI;

import org.eclipse.swt.widgets.Shell;

/**
 * @since 2.0
 */
public interface IRemoteResourceSelectorProxy {
	public URI selectFile(String scheme, String initialPath, String prompt, Shell shell);
	public URI selectDirectory(String scheme, String initialPath, String prompt, Shell shell);
}
