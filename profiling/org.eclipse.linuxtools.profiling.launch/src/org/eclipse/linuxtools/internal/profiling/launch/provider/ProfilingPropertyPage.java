/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
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
package org.eclipse.linuxtools.internal.profiling.launch.provider;

import org.eclipse.cdt.ui.newui.AbstractPage;

public class ProfilingPropertyPage extends AbstractPage {

    @Override
    protected boolean showsConfig() { return false;    }

    @Override
    protected boolean isSingle() {
        return false;
    }

}
