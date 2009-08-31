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
package org.eclipse.linuxtools.systemtap.localgui.tests;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.systemtap.localgui.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.systemtap.localgui.graphing.StapGraphParser;
import org.eclipse.linuxtools.systemtap.localgui.graphing.SystemTapView;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchConfigurationDelegate;
import org.eclipse.linuxtools.systemtap.localgui.launch.SystemTapLaunchShortcut;
import org.eclipse.swt.widgets.Shell;

public class SystemTapGraphTest extends TestCase {	
	
	
/*	public void testLaunch() {
		System.out.println("\n\nLaunching SystemTapGraphTest");

		LaunchStapGraph launch = new LaunchStapGraph();
		launch.launch(bin, mode);
		checkScript(launch);
	}*/
	
	public void testGraphLoading() {
		System.out.println("Testing graph loading");

		StapGraphParser parse = new StapGraphParser("Test StapParser", Activator.PLUGIN_LOCATION+"graph_data_output.graph");
		parse.testRun(new NullProgressMonitor());
		
		SystemTapView.forceDisplay();
		SystemTapView.maximizeOrRefresh(true);
		
		
		//TODO: Figure out how to make the graph display at the same time as the dialog
		SystemTapUIErrorMessages testRadial = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
				"Now checking graph for correctness. Please press OK, then maximize the SystemTap view.");
		testRadial.schedule();

		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
				"Check the radial view. Does the radial view look correct? Press OK if correct."));

		
		
		
		
		 //* To test:
		 //*
		 //* All transitions from (drawMode A, animMode A, collapseMode A, zoom A) to (drawMode B, animMode B, collapseMode B, zoom B) 
		 
		
		//Transition: (dRadial, aSlow, cTrue, zDefault)-->(dTree, same) 
		SystemTapUIErrorMessages testTree = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
			"Now checking Tree View. Please press OK, then select Tree View from the drop-down menu, press OK if correct.");
		testTree.schedule();
		
		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
		"Select Tree View from the drop-down menu. Does the Tree View look correct? Press OK to continue."));
		
		//Transition: (dTree, aSlow, cTrue, zDefault)-->(dAgg, same)
		SystemTapUIErrorMessages testAggregate = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
		"Now checking Aggregate View. Please press OK, then select Aggregate View from the drop-down menu, press OK to continue.");
		testAggregate.schedule();
	
		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
		"Press OK, then Select Aggregate View from the drop-down menu. Does the Aggregate View look correct? Press OK to continue."));
		
		//Transition: (dAgg, aSlow, cTrue, zDefault)-->(dBox, same)
		SystemTapUIErrorMessages testBox = new SystemTapUIErrorMessages("Test graph", "Opening graph", 
		"Now checking Box View. Please press OK, then select Box View from the drop-down menu, press OK to continue.");
		testBox.schedule();
	
		assertEquals(true, MessageDialog.openConfirm(new Shell(), "Check Graph", 
		"Press OK, then Select Box View from the drop-down menu. Does the Box View look correct? Press OK to continue."));
	}
	
	
	public void checkScript(SystemTapLaunchShortcut launch) {
		//Check that script is set properly
		File f = new File (launch.getScriptPath());
		if (!f.exists())
			fail();
	}
	
	public void checkLaunchConfiguration(String checkString, SystemTapLaunchShortcut launch) {
		//Check that the configuration was properly set
		ILaunchConfiguration config = launch.getConfig();
		SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
		try {
			del.launch(config, "profile", null, null);
			assertEquals(del.getCommand(), checkString);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
}
