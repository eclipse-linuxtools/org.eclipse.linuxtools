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

import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class AutoScrollSelectionListener extends SelectionAdapter{
    public static final int AUTO_SCROLL_UP = 0;
    public static final int AUTO_SCROLL_DOWN = 1;
    private final int type;
    private final StapGraph graph;

    public AutoScrollSelectionListener(int type, StapGraph g) {
        this.type = type;
        this.graph = g;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if (type == AUTO_SCROLL_UP) {
            AutoScrollHelper.scrollUp(graph);
        }
        if (type == AUTO_SCROLL_DOWN) {
            AutoScrollHelper.scrollDown(graph);
        }
    }

}
