/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import org.eclipse.debug.ui.actions.ContextualLaunchAction;

public class ProfileContextualLaunchAction extends ContextualLaunchAction {

    public ProfileContextualLaunchAction() {
        super(ProfileLaunchPlugin.LAUNCH_MODE);
    }
}
