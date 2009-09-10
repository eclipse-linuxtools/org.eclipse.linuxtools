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
package org.eclipse.linuxtools.systemtap.local.tests;

import junit.framework.TestCase;

import org.eclipse.linuxtools.systemtap.local.launch.SystemTapLaunchShortcut;


public class LaunchShortcutsTest extends TestCase{
	SystemTapLaunchShortcut shcut = new SystemTapLaunchShortcut();
//	private IBinary bin = null;
//	private String mode = "profile";

	/**
	 * Checks that the scripts are correct/exist and that the expected 
	 * command is sent.
	 */
	
//	
//	public void testCallGraph() {
//		
//		LaunchStapGraph launch = new LaunchStapGraph();
//		launch.launch(bin, mode);
//		
//		checkScript(launch);
//		
//		String dirPath = launch.getDirPath();
//		//Since we're passing in a null argument, the names of files 
//		//tried by the launch shortcut 
//		
//		checkLaunchConfiguration(PluginConstants.STAP_PATH + " -o " +
//								PluginConstants.STAP_GRAPH_DEFAULT_IO_PATH,
//								launch.getConfig());
//
//		killStap();
//		
//		
//		//SECOND LAUNCH FOR A SLIGHTLY DIFFERENT TEST
//		//ONLY TO INCREASE CODE COVERAGE
//		//FIRST TIME USE 0MS, then USE VALUE >0MS
//		launch = new LaunchStapGraph();
//		
//		launch.launch(bin, mode);
//		
//		checkScript(launch);
//		
//		dirPath = launch.getDirPath();
//		//Since we're passing in a null argument, the names of files 
//		//tried by the launch shortcut 
//		
//		checkLaunchConfiguration(PluginConstants.STAP_PATH + " -c '" +
//								dirPath + "' " + dirPath + "Callgraph.stp",
//								launch.getConfig());
//
//		killStap();
//
//	}
//	
//	public void testFunctionCount() {
//		LaunchFunctionCount launch = new LaunchFunctionCount();
//		launch.launch(bin, mode);
//		
//		checkScript(launch);
//		
//		String dirPath = launch.getDirPath();
//		checkLaunchConfiguration(PluginConstants.STAP_PATH + " -c '" +
//				dirPath + "' " + launch.getScriptPath() + " " + dirPath, 
//				launch);
//
//		killStap();
//	}
//	
//	public void testSyscallAll() {
//		LaunchSyscallAll launch = new LaunchSyscallAll();
//		launch.launch(bin, mode);
//		
//		checkScript(launch);
//		checkLaunchConfiguration(PluginConstants.STAP_PATH + " -c '" + 
//				launch.getDirPath() + "' " + launch.getScriptPath(), 
//				launch);
//		
//		killStap();
//	}
	
//	
//	public void testFileIOMonitor() {
//		LaunchFileIOMonitor launch = new LaunchFileIOMonitor();
//		launch.launchIOTrace(mode);
//		
//		checkScript(launch);
//		checkLaunchConfiguration(PluginConstants.STAP_PATH + " " + launch.getScriptPath()
//				+ " " + launch.getArguments(), launch);
//		
//		killStap();
//	}
//	
//	
//	public void testWizard() {
//				
//		ISelection sel = null;
//		LaunchWizard launch = new LaunchWizard();
//		launch.launch(sel, mode);
//		
//		while(!launch.isCompleted()) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		checkScript(launch);
//		
//		
//		if (launch.getArguments().length() > 0)
//			checkLaunchConfiguration(PluginConstants.STAP_PATH + " -c '" + 
//					launch.getBinaryPath() + "' " + launch.getScriptPath() + " " + launch.getArguments(),
//					launch);
//		
//		else 
//			checkLaunchConfiguration(PluginConstants.STAP_PATH + " -c '" + 
//					launch.getBinaryPath() + "' " + launch.getScriptPath(),
//					launch);
//		
//		killStap();
//	}
//	
//	public void checkScript(SystemTapLaunchShortcut launch) {
//		//Check that script was created
//		File f = new File (launch.getScriptPath());
//		if (!f.exists())
//			fail();
//	}
//	
//	public void checkLaunchConfiguration(String checkString, ILaunchConfiguration config) {
//		//Check that the configuration was properly set
//		
//		try {
//			SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
//			del.launch(config, "profile", null, null);
//			assertEquals(checkString, del.getCommand());
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public void killStap() {
//		Runtime run = Runtime.getRuntime();
//		try {
//			run.exec("kill stap");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
}
