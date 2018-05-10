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
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.graphlisteners;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class StapGraphMouseExitListener implements Listener{
    private StapGraphMouseMoveListener listener;

    public StapGraphMouseExitListener(StapGraphMouseMoveListener l) {
        this.listener = l;
    }

    @Override
    public void handleEvent(Event event) {
        listener.setStop(true);
    }

}
