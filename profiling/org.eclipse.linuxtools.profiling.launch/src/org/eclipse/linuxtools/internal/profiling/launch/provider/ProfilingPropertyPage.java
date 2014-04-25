/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
