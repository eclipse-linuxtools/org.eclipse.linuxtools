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
import org.eclipse.linuxtools.callgraph.CallGraphConstants;
import org.eclipse.linuxtools.callgraph.CallgraphView;
import org.eclipse.linuxtools.callgraph.StapGraphParser;
import org.eclipse.linuxtools.internal.callgraph.core.StapUIJob;
import org.eclipse.linuxtools.internal.callgraph.core.ViewFactory;

public class SystemTapGraphTest extends TestCase {	
	
	
/*	public void testLaunch() {
		System.out.println("\n\nLaunching SystemTapGraphTest");

		LaunchStapGraph launch = new LaunchStapGraph();
		launch.launch(bin, mode);
		checkScript(launch);
	}*/
	private boolean manual = false;
	
	public void testGraphLoading() throws InterruptedException {

		StapGraphParser parse = new StapGraphParser();
		parse.setSourcePath(Activator.PLUGIN_LOCATION+"eag.graph");
//		parse.setTestMode(true);
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
				System.out.println(task);
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
					String tempLocation = Activator.PLUGIN_LOCATION+"eag.graph2"; 
					File temp = new File(tempLocation);
					temp.delete();
					cView.saveData(tempLocation);
					temp.delete();
					break;
				case 15:
					StapGraphParser new_parser = new StapGraphParser();
					new_parser.setSourcePath(Activator.PLUGIN_LOCATION+"eag.graph");
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
			
//		//TODO: Figure out how to make the graph display at the same time as the dialog
//		SystemTapUIErrorMessages testRadial = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//				"Testing Graph. Press OK, then go through the list of tasks.");
//		testRadial.schedule();
//
//		testRadial.cancel();
//
//		ArrayList<String> tasks = new ArrayList<String>();
//		
//
//		tasks.add("(Manually) Maximize CallgraphView");
//		tasks.add("Refresh");
//		tasks.add("Tree View");
//		tasks.add("Aggregate View");
//		tasks.add("Box View");
//		tasks.add("Animation->Fast");
//		tasks.add("Collapse");
//		tasks.add("Uncollapse");
//		tasks.add("Radial View");
//		tasks.add("Collapse.");
//		tasks.add("(Manually) Double-click node with no children in TreeViewer");
//		tasks.add("(Manually) Expand an arrow in the TreeViewer");
//		tasks.add("(Manually) Collapse an arrow in the TreeViewer");
//		tasks.add("Save file");
//		tasks.add("Reload file");
//		tasks.add("Check Version");
//		
//		
//		final Shell sh = new Shell(SWT.SHELL_TRIM);
//		sh.setSize(450,tasks.size()*38);
//		sh.setText("Tasklist - press Finished when finished.");
//		sh.setLayout(new GridLayout(1, false));
//		sh.setAlpha(150);
//		
//		ScrolledComposite testComp = new ScrolledComposite(sh, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
//	
//		
//		Composite buttons = new Composite(testComp, SWT.NONE);
//		testComp.setContent(buttons);
//		buttons.setLayout(new GridLayout(1, false));
//	    testComp.setExpandHorizontal(true);
//	    testComp.setExpandVertical(true);
//
//	    int taskNumber = 0;
//		for (String task : tasks) {
//			taskNumber++;
//
//			
//			Button checkBox = new Button(buttons, SWT.CHECK);
//			list.add(checkBox);
//			checkBox.setText(task);
//			Action act = null;
//			switch (taskNumber) {
//			case 1:
//				break;
//			case 2:
//				act = cView.getView_refresh();
//				break;
//			case 3:
//				act = cView.getView_treeview();
//				break;
//			case 4:
//				act = cView.getView_aggregateview();
//				break;
//			case 5:
//				act = cView.getView_levelview();
//				break;
//			case 6:
//				act = cView.getAnimation_fast();
//				break;
//			case 7:
//			case 8:
//				act = cView.getMode_collapsednodes();
//				break;
//			case 9:
//				act = cView.getView_radialview();
//				break;
//			case 10:
//				act = cView.getMode_collapsednodes();
//				break;
//			case 14:
//				act = cView.getSave_file();
//				break;
//			case 15:
//				act = cView.getOpen_file();
//				break;
//			case 16:
//				act = cView.getHelp_version();
//				break;
//			default:
//				break;
//			}
//			if (act != null) {
//				ButtonSelectionListener bl = new ButtonSelectionListener(act);
//				checkBox.addSelectionListener(bl);
//			}
//			
//			
//		}
		
//		Button finish = new Button(buttons, SWT.PUSH);
//		finish.setText("Finish");
//		finish.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				
//			}
//			
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				sh.dispose();
//			}
//		});
//		
		
//		sh.open();
		
//		
//		boolean doneTasks =MessageDialog.openConfirm(new Shell(SWT.ON_TOP), "Check Graph", 
//							"Press OK if all "+ tasks.size() + " boxes in the checklist have been checked.\n" +
//									"Hit Cancel if any test fails."); 
//		assertEquals(true, doneTasks);

//		for (Button b : list) {
//			if (!b.getSelection()) {
//				fail("Task failed: " + b.getText());
//			}
//			assertEquals(true, b.getSelection());
//		}
		
		
		
		 //* To test:
		 //*
		 //* All transitions from (drawMode A, animMode A, collapseMode A, zoom A) to (drawMode B, animMode B, collapseMode B, zoom B) 
		 
		//Transition: (dRadial, aSlow, cTrue, zDefault)-->(dTree, same) 
//		SystemTapUIErrorMessages testTree = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//			"Now checking Tree View. Please press OK, then , press OK if correct.");
//		testTree.schedule();
//		
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Select Tree View from the drop-down menu. Does the Tree View look correct? Press OK to continue."));
//		
//		//Transition: (dTree, aSlow, cTrue, zDefault)-->(dAgg, same)
//		SystemTapUIErrorMessages testAggregate = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//		"Now checking Aggregate View. Please press OK, then select Aggregate View from the drop-down menu, press OK to continue.");
//		testAggregate.schedule();
//	
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Press OK, then Select Aggregate View from the drop-down menu. Does the Aggregate View look correct? Press OK to continue."));
//		
//		//Transition: (dAgg, aSlow, cTrue, zDefault)-->(dBox, same)
//		SystemTapUIErrorMessages testBox = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
//		"Now checking Box View. Please press OK, then select Box View from the drop-down menu, press OK to continue.");
//		testBox.schedule();
//	
//		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
//		"Press OK, then Select Box View from the drop-down menu. Does the Box View look correct? Press OK to continue."));
	}
	
}
