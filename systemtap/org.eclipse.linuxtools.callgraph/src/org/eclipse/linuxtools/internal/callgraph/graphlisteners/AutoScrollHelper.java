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

import org.eclipse.linuxtools.internal.callgraph.StapGraph;

public class AutoScrollHelper {

    public static void scrollUp(StapGraph graph) {

        if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_LEVEL) {
            int parent = graph.getNodeData(graph.getRootVisibleNodeNumber()).parent;
            if (graph.isCollapseMode()) {
                parent = graph.getNodeData(graph.getRootVisibleNodeNumber()).collapsedParent;
            }

            if (graph.getNodeData(parent) != null && graph.getNodeData(parent).levelOfRecursion > 0 ) {
                int animMode = graph.getAnimationMode();
                graph.draw(graph.getDrawMode(), StapGraph.CONSTANT_ANIMATION_FASTEST, parent);
                graph.setAnimationMode(animMode);
            }
        } else if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE) {
            graph.shrinkTree();
        }
    }

    public static void scrollDown(StapGraph graph) {
        if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_LEVEL) {
            if (graph.getTopLevel() + graph.levelBuffer <
                    graph.getLowestLevelOfNodesAdded()) {
                int newLevel = graph.getTopLevel() + 1;
                if (graph.levels.get(newLevel).get(0) == null) {
                    return;
                }

                graph.setTopLevelTo(newLevel);
                int animMode = graph.getAnimationMode();
                graph.setAnimationMode(StapGraph.CONSTANT_ANIMATION_FASTEST);
                graph.draw(graph.getDrawMode(), StapGraph.CONSTANT_ANIMATION_FASTEST,
                        graph.levels.get(newLevel).get(0));
                graph.setAnimationMode(animMode);
            }
        } else if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE) {
            graph.extendTree();
        }

    }


}
