package org.eclipse.linuxtools.systemtap.local.callgraph.graphlisteners;

import org.eclipse.linuxtools.systemtap.local.callgraph.StapGraph;

public class AutoScrollHelper {

	
	
	public static void scrollUp(StapGraph graph) {

		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_BOX) {
			int parent = graph.getNodeData(graph.getRootVisibleNodeNumber()).caller;
			if (graph.isCollapseMode())
				parent = graph.getNodeData(graph.getRootVisibleNodeNumber()).collapsedCaller;
			
			if (graph.getNodeData(parent).levelOfRecursion > 0 ) {
				int animMode = graph.getAnimationMode();
				graph.draw(graph.getDrawMode(), StapGraph.CONSTANT_ANIMATION_FASTEST, parent);
				graph.setAnimationMode(animMode);
			}
		} else if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_TREE) {
			graph.shrinkTree();
		}
	}
	
	public static void scrollDown(StapGraph graph) {
		if (graph.getDrawMode() == StapGraph.CONSTANT_DRAWMODE_BOX) {
			if (graph.getTopLevel() + StapGraph.levelBuffer < 
					graph.getLowestLevelOfNodesAdded()) {
				int newLevel = graph.getTopLevel() + 1;
				if (graph.levels.get(newLevel).get(0) == null)
					return;
				
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
