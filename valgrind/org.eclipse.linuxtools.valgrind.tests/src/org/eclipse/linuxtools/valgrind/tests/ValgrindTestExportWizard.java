package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.linuxtools.valgrind.launch.ExportWizardConstants;
import org.eclipse.linuxtools.valgrind.launch.Messages;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizard;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizardPage;

public class ValgrindTestExportWizard extends ValgrindExportWizard {
	
	@Override
	protected ValgrindExportWizardPage getWizardPage() {
		return new ValgrindTestExportWizardPage(Messages.getString("ValgrindExportWizard.Page_name"), ExportWizardConstants.WIZARD_TITLE, null); //$NON-NLS-1$
	}

}
