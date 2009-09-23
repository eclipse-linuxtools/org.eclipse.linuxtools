package org.eclipse.linuxtools.systemtap.local.callgraph.tests;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.systemtap.local.callgraph.CallgraphView;
import org.eclipse.linuxtools.systemtap.local.callgraph.GraphUIJob;
import org.eclipse.linuxtools.systemtap.local.callgraph.StapGraph;
import org.eclipse.linuxtools.systemtap.local.callgraph.StapGraphParser;
import org.eclipse.linuxtools.systemtap.local.callgraph.graphlisteners.StapGraphKeyListener;
import org.eclipse.linuxtools.systemtap.local.callgraph.graphlisteners.StapGraphMouseListener;
import org.eclipse.linuxtools.systemtap.local.callgraph.graphlisteners.StapGraphMouseWheelListener;
import org.eclipse.zest.core.widgets.GraphItem;


public class MouseListenerTest extends TestCase{

	public void test() {
		StapGraphParser parse = new StapGraphParser();
		parse.setFile(Activator.PLUGIN_LOCATION + "eag.graph");
		parse.testRun(new NullProgressMonitor());

		CallgraphView.forceDisplay();

		GraphUIJob j = new GraphUIJob("Test Graph UI Job", parse);
		j.runInUIThread(new NullProgressMonitor());
		
		
		StapGraphMouseListener mListener = CallgraphView.getGraph().getMouseListener();
		StapGraphKeyListener kListener = CallgraphView.getGraph().getKeyListener();
		StapGraphMouseWheelListener mwListener = CallgraphView.getGraph().getMouseWheelListener();
		
		StapGraph g = (StapGraph) CallgraphView.getGraph();
		g.setProject(parse.project);
		
		
		GraphItem[] nodes = {g.getNode(g.getFirstUsefulNode())}; 
		g.setSelection(nodes);
		
		
		System.out.println(mListener.controlDoubleClick());
		mListener.mouseDownEvent(0, 0);
		g.draw(StapGraph.CONSTANT_DRAWMODE_TREE, StapGraph.CONSTANT_ANIMATION_FASTEST,
				g.getFirstUsefulNode());
		mListener.mouseUpEvent();
		
		System.out.println("TEST");
		
	}
}
