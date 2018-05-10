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

import org.eclipse.linuxtools.internal.callgraph.CallgraphView;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

/**
 * StapGraph key listener
 */
public class StapGraphKeyListener extends KeyAdapter {
    private CallgraphView callgraphView;

    public StapGraphKeyListener(StapGraph g) {
        callgraphView = g.getCallgraphView();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.stateMask != SWT.SHIFT) {
            return;
        }

        //TODO: Use accelerator in menu actions instead of this hard-coded stuff
        if (e.character == 'R') {
            callgraphView.getViewRefresh().run();
        }else if (e.character == 'C') {
            callgraphView.getModeCollapsednodes().run();
        } else if (e.character == 'N') {
            callgraphView.getGotoNext().run();
        } else if (e.character == 'P') {
            callgraphView.getGotoPrevious().run();
        } else if (e.character == 'L') {
            callgraphView.getGotoLast().run();
        } else if (e.character == 'D') {
            callgraphView.getPlay().run();
        }
    }

}
