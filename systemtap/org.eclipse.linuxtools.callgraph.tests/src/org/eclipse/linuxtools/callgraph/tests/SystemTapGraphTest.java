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

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.internal.callgraph.CallgraphView;
import org.eclipse.linuxtools.internal.callgraph.StapGraphParser;
import org.eclipse.linuxtools.internal.callgraph.core.StapUIJob;
import org.eclipse.linuxtools.internal.callgraph.core.ViewFactory;

public class SystemTapGraphTest extends TestCase {	
	
	
	private boolean manual = false;
	
	public void testGraphLoading() {

		StapGraphParser parse = new StapGraphParser();
		parse.setSourcePath(Activator.getPluginLocation()+"eag.graph");
		assertEquals(Status.OK_STATUS, parse.testRun(new NullProgressMonitor(), true));
		
		StapUIJob j = new StapUIJob("Test Graph UI Job", parse, CallGraphConstants.viewID);
		j.runInUIThread(new NullProgressMonitor());
		CallgraphView cView = (CallgraphView)  ViewFactory.createView(CallGraphConstants.viewID);
		 
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
					act = cView.getView_refresh();
					break;
				case 3:
					act = cView.getView_treeview();
					break;
				case 4:
					act = cView.getView_aggregateview();
					break;
				case 5:
					act = cView.getView_levelview();
					break;
				case 6:
					act = cView.getAnimation_fast();
					break;
				case 7:
				case 8:
					act = cView.getMode_collapsednodes();
					break;
				case 9:
					act = cView.getView_radialview();
					break;
				case 10:
					act = cView.getMode_collapsednodes();
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
