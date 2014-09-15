/*******************************************************************************
 * Copyright (c) 2011 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.test;

import org.eclipse.core.runtime.NullProgressMonitor;

public class ProgressMonitor extends NullProgressMonitor {

    private boolean done = false;

    public boolean isDone() {
        return done;
    }

    @Override
    public void done() {
        super.done();
        done = true;
    }

}
