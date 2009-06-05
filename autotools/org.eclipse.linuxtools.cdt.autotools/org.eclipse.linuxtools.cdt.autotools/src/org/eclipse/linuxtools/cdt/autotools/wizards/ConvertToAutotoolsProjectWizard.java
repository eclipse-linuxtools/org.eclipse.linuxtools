/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.wizards;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.NewCProjectWizardPage;
import org.eclipse.cdt.ui.wizards.conversion.ConversionWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.cdt.autotools.AutotoolsPlugin;


/**
 * This wizard provides a method by which the user can 
 * add a C nature to a project that previously had no nature associated with it.
 */
public class ConvertToAutotoolsProjectWizard extends ConversionWizard {

	private static final String WZ_TITLE = "WizardAutotoolsProjectConversion.title"; //$NON-NLS-1$
	private static final String WZ_DESC = "WizardAutotoolsProjectConversion.description"; //$NON-NLS-1$
	private static final String PREFIX = "WizardAutotoolsConversion"; //$NON-NLS-1$
	private static final String WINDOW_TITLE = "WizardAutotoolsConversion.windowTitle"; //$NON-NLS-1$
	protected static final String CONF_TITLE = PREFIX + ".config.title";	//$NON-NLS-1$
	protected static final String CONF_DESC = PREFIX + ".config.desc";	//$NON-NLS-1$
	protected static final String MSG_SAVE = PREFIX + ".message.save";	//$NON-NLS-1$
	protected static final String OPTIONS_TITLE = PREFIX + ".options.title";	//$NON-NLS-1$
	protected static final String OPTIONS_DESC = PREFIX + ".options.desc";	//$NON-NLS-1$

	public static final String NULL_INDEXER_ID = "org.eclipse.cdt.core.nullindexer"; //$NON-NLS-1$
	
	protected CProjectPlatformPage projectConfigurationPage;
	protected NewAutotoolsProjectOptionPage optionPage;

	protected IProject curProject;
	
	/**
	 * ConvertToAutotoolsConversionWizard Wizard constructor
	 */
	public ConvertToAutotoolsProjectWizard() {
		this(getWindowTitleResource(), getWzDescriptionResource());
	}
	/**
	 * ConvertToAutotoolsConversionWizard Wizard constructor
	 * 
	 * @param title
	 * @param desc
	 */
	public ConvertToAutotoolsProjectWizard(String title, String desc) {
		super(title, desc);
	}

	/**
	 * Method getWzDescriptionResource,  allows Wizard description label value
	 * to be changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWzDescriptionResource() {
		return AutotoolsPlugin.getResourceString(WZ_DESC);
	}

	/**
	 * Method getWzTitleResource,  allows Wizard description label value
	 * to be changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWzTitleResource() {
		return AutotoolsPlugin.getResourceString(WZ_TITLE);
	}

	/**
	 * Method getWindowTitleResource, allows Wizard Title label value to be
	 * changed by subclasses
	 * 
	 * @return String
	 */
	protected static String getWindowTitleResource() {
		return AutotoolsPlugin.getResourceString(WINDOW_TITLE);
	}

	/**
	  * Method getPrefix,  allows prefix value to be changed by subclasses
	  * 
	  * @return String
	  */
	protected static String getPrefix() {
		return PREFIX;
	}

	/**
	 * Method addPages adds our Simple to C conversion Wizard page.
	 * 
	 * @see Wizard#createPages
	 */
	public void addPages() {
		addPage(mainPage = new ConvertToAutotoolsProjectWizardPage(getPrefix(), this));
		
		// Add the configuration selection page
		projectConfigurationPage = new CProjectPlatformPage(PREFIX, this);
		projectConfigurationPage.setTitle(AutotoolsPlugin.getResourceString(CONF_TITLE));
		projectConfigurationPage.setDescription(AutotoolsPlugin.getResourceString(CONF_DESC));
		addPage(projectConfigurationPage);

		// Add the options (tabbed) page
		optionPage = new NewAutotoolsProjectOptionPage(PREFIX, this);
		optionPage.setTitle(AutotoolsPlugin.getResourceString(OPTIONS_TITLE));
		optionPage.setDescription(AutotoolsPlugin.getResourceString(OPTIONS_DESC));
		addPage(optionPage);
		
		// add custom pages
		MBSCustomPageManager.init();
		
		// add stock pages
		MBSCustomPageManager.addStockPage(fMainPage, NewCProjectWizardPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(projectConfigurationPage, CProjectPlatformPage.PAGE_ID);
		MBSCustomPageManager.addStockPage(optionPage, NewAutotoolsProjectOptionPage.PAGE_ID);
	}

	public String getProjectID() {
		return ManagedBuilderCorePlugin.MANAGED_MAKE_PROJECT_ID;
	}

	public IProjectType getProjectType() {
		return projectConfigurationPage.getProjectType();
	}

	public IConfiguration[] getSelectedConfigurations() {
		return projectConfigurationPage.getSelectedConfigurations();
	}

	protected void setCurrentProject (IProject project) {
		curProject = project;
	}
	
	public IProject getProject() {
		return curProject;
	}
	
	public void applyOptions(IProject project, IProgressMonitor monitor) {
		// When applying the project options, we need to specify which
		// project because the conversion wizard allows us to convert
		// more than one project at once.  We accomplish this by setting
		// the current project we are working on.  The optionPage when
		// applying options will ask the wizard (us) to get the project which
		// we will report is the current project being converted.
		setCurrentProject(project);
    	optionPage.performApply(monitor);
    }
	
	protected void doRun(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(AutotoolsPlugin.getResourceString("WizardAutotoolsProjectConversion.monitor.convertingToMakeProject"), 2); //$NON-NLS-1$
		try {
			super.doRun(new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
		
//		// Add the ManagedProject to the project
//		IManagedProject newManagedProject = null;
//		IManagedBuildInfo info = null;
//		try {
//			project = getProjectHandle();
//			info = ManagedBuildManager.createBuildInfo(project);
//			IProjectType parent = getProjectType();
//			newManagedProject = ManagedBuildManager.createManagedProject(project, parent);
//			if (newManagedProject != null) {
//				IConfiguration [] selectedConfigs = getSelectedConfigurations();
//				for (int i = 0; i < selectedConfigs.length; i++) {
//					IConfiguration config = selectedConfigs[i];
//					int id = ManagedBuildManager.getRandomNumber();
//					IConfiguration newConfig = newManagedProject.createConfiguration(config, config.getId() + "." + id); //$NON-NLS-1$
//					newConfig.setArtifactName(newManagedProject.getDefaultArtifactName());
//				}
//				// Now add the first supported config in the list as the default
//				IConfiguration defaultCfg = null;
//				IConfiguration[] newConfigs = newManagedProject.getConfigurations();
//				for(int i = 0; i < newConfigs.length; i++) {
//					if(newConfigs[i].isSupported()){
//						defaultCfg = newConfigs[i];
//						break;
//					}
//				}
//				
//				if(defaultCfg == null && newConfigs.length > 0)
//					defaultCfg = newConfigs[0];
//				
//				if(defaultCfg != null) {
//					ManagedBuildManager.setDefaultConfiguration(project, defaultCfg);
//					ManagedBuildManager.setSelectedConfiguration(project, defaultCfg);
//				}
//				ManagedBuildManager.setNewProjectVersion(project);
//				ICDescriptor desc = null;
//				try {
//					desc = CCorePlugin.getDefault().getCProjectDescription(project, true);
//					desc.create(CCorePlugin.BUILD_SCANNER_INFO_UNIQ_ID, ManagedBuildManager.INTERFACE_IDENTITY);
//					//  TODO:  The binary parser setting is currently per-project in the rest of CDT.
//					//         In the MBS, it is per-coonfiguration.  For now, select the binary parsers of the
//					//         first configuration.
//					if (newConfigs.length > 0) {
//						IToolChain tc = newConfigs[0].getToolChain();
//						ITargetPlatform targetPlatform = tc.getTargetPlatform();
//					}
//				} catch (CoreException e) {
//					ManagedBuilderUIPlugin.log(e);
//				}
//			}
//		} catch (BuildException e) {
//			ManagedBuilderUIPlugin.log(e);
//		}
//
//		// Modify the project settings
//		if (project != null) {
//			optionPage.performApply(new SubProgressMonitor(monitor, 2));
//		}
//
//		// Save the build options
//		monitor.subTask(AutotoolsPlugin.getResourceString(MSG_SAVE));
//		if (info != null) {
//			info.setValid(true);
//			ManagedBuildManager.saveBuildInfo(project, true);
//		}
//		monitor.done();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunPrologue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunPrologue(IProgressMonitor monitor) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.NewCProjectWizard#doRunEpilogue(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void doRunEpilogue(IProgressMonitor monitor) {
		// Get my initializer to run
//		if (project == null)
//			return;
//
//		IStatus initResult = ManagedBuildManager.initBuildInfoContainer(project);
//		if (initResult.getCode() != IStatus.OK) {
//			// At this point, I can live with a failure
//			ManagedBuilderUIPlugin.log(initResult);
//		}
		
		// execute any operations specified by custom pages
		IRunnableWithProgress operations[] = MBSCustomPageManager.getOperations();
		
		if (operations != null)
		{
			for(int k = 0; k < operations.length; k++)
			{
				try {
				operations[k].run(monitor);
				} catch(InvocationTargetException e) {
					//TODO: what should we do?
				} catch(InterruptedException e) {
					//TODO: what should we do?
				}
			}
		}
	}
}
