/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
