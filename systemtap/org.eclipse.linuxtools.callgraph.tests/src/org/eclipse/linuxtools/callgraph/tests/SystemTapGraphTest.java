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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.CallgraphView;
import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.eclipse.linuxtools.internal.callgraph.core.StapUIJob;
import org.eclipse.linuxtools.internal.callgraph.core.ViewFactory;
import org.junit.Test;

public class SystemTapGraphTest {


	private boolean manual = false;
	@Test
	public void testGraphLoading() {

		StapGraphParser parse = new StapGraphParser();
		parse.setSourcePath(Activator.getPluginLocation()+"eag.graph");
		assertEquals(Status.OK_STATUS, parse.testRun(new NullProgressMonitor(), true));

		StapUIJob j = new StapUIJob("Test Graph UI Job", parse, CallGraphConstants.VIEW_ID);
		j.runInUIThread(new NullProgressMonitor());
		CallgraphView cView = (CallgraphView)  ViewFactory.createView(CallGraphConstants.VIEW_ID);

		if (!manual) {
			ArrayList<String> tasks = new ArrayList<String>();


			tasks.add("(Manually) Maximize CallgraphView");
			tasks.add("Refresh");
			tasks.add("Tree View");
			tasks.add("Aggregate View");
			tasks.add("Box View");
			tasks.add("Animation->Fast");
			tasks.add("Collapse");
			tasks.add("Uncollapse");
			tasks.add("Radial View");
			tasks.add("Collapse.");
			tasks.add("(Manually) Double-click node with no children in TreeViewer");
			tasks.add("(Manually) Expand an arrow in the TreeViewer");
			tasks.add("(Manually) Collapse an arrow in the TreeViewer");
			tasks.add("Save file");
			tasks.add("Reload file");
			tasks.add("Maximize");


		    int taskNumber = 0;
			for (String task : tasks) {
				taskNumber++;
				Action act = null;
				switch (taskNumber) {
				case 1:
					break;
				case 2:
					act = cView.getViewRefresh();
					break;
				case 3:
					act = cView.getViewTreeview();
					break;
				case 4:
					act = cView.getViewAggregateview();
					break;
				case 5:
					act = cView.getViewLevelview();
					break;
				case 6:
					act = cView.getAnimationFast();
					break;
				case 7:
				case 8:
					act = cView.getModeCollapsednodes();
					break;
				case 9:
					act = cView.getViewRadialview();
					break;
				case 10:
					act = cView.getModeCollapsednodes();
					break;
				case 14:
					String tempLocation = Activator.getPluginLocation()+"eag.graph2";
					File temp = new File(tempLocation);
					temp.delete();
					cView.saveData(tempLocation);
					temp.delete();
					break;
				case 15:
					StapGraphParser new_parser = new StapGraphParser();
					new_parser.setSourcePath(Activator.getPluginLocation()+"eag.graph");
					new_parser.testRun(new NullProgressMonitor(), true);
					break;
				case 16:
					cView.maximizeIfUnmaximized();
					break;
				default:
					break;
				}
				if (act != null) {
					act.run();
				}
			}
			return;
		}
	}
}
