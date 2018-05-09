/*******************************************************************************
 * Copyright (c) 2009, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *    Red Hat Inc. - modified to test Helgrind launch short-cut
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.helgrind.tests;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.internal.valgrind.helgrind.HelgrindLaunchShortcut;

public class ValgrindTestHelgrindLaunchShortcut extends HelgrindLaunchShortcut {

    private ILaunchConfiguration config;

    @Override
    public void launch(IBinary bin, String mode) {
        config = findLaunchConfiguration(bin, mode);
    }

    public ILaunchConfiguration getConfig() {
        return config;
    }
}
