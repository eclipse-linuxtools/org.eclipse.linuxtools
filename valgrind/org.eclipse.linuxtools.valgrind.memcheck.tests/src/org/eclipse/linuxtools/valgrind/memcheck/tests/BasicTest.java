/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.memcheck.tests;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.linuxtools.valgrind.launch.ValgrindOptionsTab;
import org.eclipse.linuxtools.valgrind.memcheck.MemcheckViewPart;
import org.eclipse.linuxtools.valgrind.ui.ValgrindUIPlugin;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

public class BasicTest extends TestCase {
	
	protected ICProject proj;

	@Override
	protected void setUp() throws Exception {
		proj = createProject("basicTest"); //$NON-NLS-1$
	}
	
	@Override
	protected void tearDown() throws Exception {
		deleteProject(proj);
	}
	
	public void testTest() throws Exception {
		assertNotNull(proj.getBinaryContainer().getBinaries()[0]);
	}
	
	public void testErrors() throws Exception {
		IBinary bin = proj.getBinaryContainer().getBinaries()[0];
		ILaunchConfiguration config = createConfiguration(bin);
		config.launch(ILaunchManager.PROFILE_MODE, null, true);
				
		MemcheckViewPart view = (MemcheckViewPart) ValgrindUIPlugin.getDefault().getView().getDynamicView();
		assertEquals(3, view.getErrors().length);
	}

	protected ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	protected ICProject createProject(String projname) throws CoreException, URISyntaxException,
			InvocationTargetException, InterruptedException, IOException {
		ICProject proj = CProjectHelper.createCProject(projname , "Debug"); //$NON-NLS-1$
		URL location = FileLocator.find(MemcheckTestsPlugin.getDefault().getBundle(), new Path("resources/" + projname), null); //$NON-NLS-1$
		File testDir = new File(FileLocator.toFileURL(location).toURI());
		
		ImportOperation op = new ImportOperation(proj.getProject().getFullPath(), testDir, FileSystemStructureProvider.INSTANCE, new IOverwriteQuery() {
			public String queryOverwrite(String pathString) {
				return ALL;
			}			
		});
		op.setCreateContainerStructure(false);
		op.run(null);
		proj.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		return proj;
	}
	
	protected void deleteProject(ICProject cproject) {
		CProjectHelper.delete(cproject);
	}
	
	protected ILaunchConfiguration createConfiguration(IBinary bin) {
		ILaunchConfiguration config = null;
		try {
			String projectName = bin.getResource().getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(ValgrindLaunchPlugin.LAUNCH_ID);;
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager().generateUniqueLaunchConfigurationNameFrom(bin.getElementName()));
	
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, bin.getCProject().getElementName());
			wc.setMappedResources(new IResource[] {bin.getResource(), bin.getResource().getProject()});
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);
			
			// Make launch run in foreground
			wc.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, false);
			
			ValgrindOptionsTab.setDefaultGeneralAttributes(wc);
			ILaunchConfigurationTab defaultTab = ValgrindLaunchPlugin.getDefault().getToolPage(ValgrindLaunchPlugin.TOOL_EXT_DEFAULT);
			defaultTab.setDefaults(wc);
	
			config = wc.doSave();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return config;
	}
}
