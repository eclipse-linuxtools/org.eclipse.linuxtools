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
package org.eclipse.linuxtools.systemtap.local.launch.tests;

import java.io.File;
import java.io.IOException;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.linuxtools.systemtap.local.core.SystemTapUIErrorMessages;
import org.eclipse.linuxtools.systemtap.local.launch.LaunchStapGraph;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapLaunchConfigurationDelegate;
import org.eclipse.linuxtools.systemtap.local.launch.SystemTapLaunchShortcut;
import org.osgi.framework.Bundle;




public class LaunchShortcutsTest extends AbstractStapTest{

	/**
	 * Checks that the scripts are correct/exist and that the expected 
	 * command is sent.
	 */
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		proj = createProjectAndBuild("basicTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
		super.tearDown();
	}
	
	
	public void testLaunchCallGraph() {
		try {
			SystemTapUIErrorMessages.setActive(false);
			
			LaunchStapGraph launch = new LaunchStapGraph();
			launch.setStop(true);
			
			IBinary bin = proj.getBinaryContainer().getBinaries()[0];
			launch.launch(bin, "profile");
			
			String script = launch.getScript();
			
			assert(script.contains("probe process(@1).function(\"calledOnce\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledOnce\").return{		returnFunction(probefunc())	}"));
			assert(script.contains("probe process(@1).function(\"calledTwice\").call{	callFunction(probefunc())	}	probe process(@1).function(\"calledTwice\").return{		returnFunction(probefunc())	}"));
			assert(script.contains("probe process(@1).function(\"main\").call{	callFunction(probefunc())	}	probe process(@1).function(\"main\").return{		returnFunction(probefunc())	}"));

			
			
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
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
	public void checkScript(SystemTapLaunchShortcut launch) {
		//Check that script was created
		File f = new File (launch.getScriptPath());
		if (!f.exists())
			fail();
	}
	
	public void checkLaunchConfiguration(String checkString, ILaunchConfiguration config) {
		//Check that the configuration was properly set
		
		try {
			SystemTapLaunchConfigurationDelegate del = new SystemTapLaunchConfigurationDelegate();
			del.launch(config, "profile", null, null);
			assertEquals(checkString, del.getCommand());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public void killStap() {
		Runtime run = Runtime.getRuntime();
		try {
			run.exec("kill stap");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	@Override
	protected Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

}
