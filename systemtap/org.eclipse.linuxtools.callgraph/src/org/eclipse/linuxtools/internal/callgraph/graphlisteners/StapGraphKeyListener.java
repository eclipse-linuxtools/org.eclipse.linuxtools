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
