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

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

/**
 * Set mouseDown flag to false when focus is lost (prevents the graph from
 * sticking to the mouse cursor when focus is regained)
 *
 *
 */
public class StapGraphFocusListener extends FocusAdapter{
    private StapGraphMouseMoveListener listener;

    public StapGraphFocusListener(StapGraphMouseMoveListener listener) {
        this.listener = listener;
    }

    @Override
    public void focusLost(FocusEvent e) {
        listener.setStop(true);
    }

}
