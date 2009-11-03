package org.eclipse.linuxtools.internal.cdt.autotools.ui.wizards;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.linuxtools.cdt.autotools.core.AutotoolsNewProjectNature;
import org.eclipse.linuxtools.internal.cdt.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.swt.widgets.Composite;

public class AutotoolsBuildWizardHandler extends MBSWizardHandler {
	public AutotoolsBuildWizardHandler(Composite p, IWizard w) {
		super(AutotoolsWizardMessages.getResourceString("AutotoolsBuildWizard.0"), p, w); //$NON-NLS-1$
	}

	public AutotoolsBuildWizardHandler(IProjectType pt, Composite parent, IWizard wizard) {
		super(pt, parent, wizard);
	}

	@Override
	public void convertProject(IProject proj, IProgressMonitor monitor) throws CoreException {
	    super.convertProject(proj, monitor);
		AutotoolsNewProjectNature.addAutotoolsNature(proj, monitor);
		
		// For each IConfiguration, create a corresponding Autotools Configuration
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(proj);
		IConfiguration[] cfgs = info.getManagedProject().getConfigurations();
		for (int i = 0; i < cfgs.length; ++i) {
			IConfiguration cfg = cfgs[i];
			AutotoolsConfigurationManager.getInstance().getConfiguration(proj, cfg.getName(), true);
		}
		AutotoolsConfigurationManager.getInstance().saveConfigs(proj.getName());
	}
}
