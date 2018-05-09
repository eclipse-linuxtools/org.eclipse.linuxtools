/*******************************************************************************
 * Copyright (c) 2012, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
    URI selectFile(String scheme, String initialPath, String prompt, Shell shell);
    URI selectDirectory(String scheme, String initialPath, String prompt, Shell shell);
}
