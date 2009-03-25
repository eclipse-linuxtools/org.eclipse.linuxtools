package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizardPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ValgrindTestExportWizardPage extends ValgrindExportWizardPage {

	protected ValgrindTestExportWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public CheckboxTableViewer getViewer() {
		return viewer;
	}
	
	public Text getDestText() {
		return destText;
	}	

	public Button getSelectAllButton() {
		return selectAllButton;
	}
	
	public Button getDeselectAllButton() {
		return deselectAllButton;
	}
	
	@Override
	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindTestLaunchPlugin.getDefault();
	}
}
