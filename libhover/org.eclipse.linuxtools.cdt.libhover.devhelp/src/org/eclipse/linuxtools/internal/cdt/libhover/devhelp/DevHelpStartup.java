/*******************************************************************************
 * Copyright (c) 2015, 2022 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Red Hat Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

public class DevHelpStartup implements IStartup {

    private Job k;
	
	@Override
	public void earlyStartup() {
        k = new DevHelpGenerateJob(false) ;
        k.schedule();
        DevHelpPlugin.getDefault().setJob(k);
    };

}
