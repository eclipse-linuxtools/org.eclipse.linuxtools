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
package org.eclipse.linuxtools.callgraph.tests;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.CallgraphView;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.eclipse.linuxtools.internal.callgraph.core.StapUIJob;
import org.eclipse.linuxtools.internal.callgraph.core.ViewFactory;
import org.eclipse.linuxtools.internal.callgraph.graphlisteners.StapGraphMouseListener;
import org.eclipse.zest.core.widgets.GraphItem;
import org.junit.Test;

public class MouseListenerTest {

    @Test
    public void test() {
        StapGraphParser parse = new StapGraphParser();
        parse.setSourcePath(Activator.getPluginLocation() + "eag.graph");
        parse.testRun(new NullProgressMonitor(), true);

        CallgraphView cView = (CallgraphView) ViewFactory.createView("org.eclipse.linuxtools.callgraph.callgraphview");

        StapUIJob j = new StapUIJob("Test Graph UI Job", parse,
                CallGraphConstants.VIEW_ID);
        j.runInUIThread(new NullProgressMonitor());

        StapGraphMouseListener mListener = cView.getGraph().getMouseListener();

        StapGraph g = cView.getGraph();
        g.setProject(parse.project);

        GraphItem[] nodes = { g.getNode(g.getFirstUsefulNode()) };
        g.setSelection(nodes);

        mListener.mouseDownEvent(0, 0);
        g.draw(StapGraph.CONSTANT_DRAWMODE_TREE,
                StapGraph.CONSTANT_ANIMATION_FASTEST, g.getFirstUsefulNode());
        mListener.mouseUp(null);

    }
}
