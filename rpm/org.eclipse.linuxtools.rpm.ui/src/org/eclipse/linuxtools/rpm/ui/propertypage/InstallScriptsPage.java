/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.linuxtools.rpm.core.utils.RPMQuery;
import org.eclipse.linuxtools.rpm.ui.util.ExceptionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class InstallScriptsPage extends PropertyPage {

	private static final String RPM_PRE_INSTALL = Messages.getString("InstallScriptsPage.PreinstallScript"); //$NON-NLS-1$

	private static final String RPM_POST_INSTALL = Messages.getString("InstallScriptsPage.PostinstallScript"); //$NON-NLS-1$

	private static final String RPM_PRE_UNINSTALL = Messages.getString("InstallScriptsPage.PreuninstallScript"); //$NON-NLS-1$

	private static final String RPM_POST_UNINSTALL = Messages.getString("InstallScriptsPage.PostuninstallScript"); //$NON-NLS-1$

	private static final int SCRIPT_ENTRIES_FIELD_WIDTH = 80;

	private static final int SCRIPT_ENTRIES_FIELD_HEIGHT = 20;

	private Text rpm_PreInstallText;

	private Text rpm_PostInstallText;

	private Text rpm_PreUnInstallText;

	private Text rpm_PostUnInstallText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public InstallScriptsPage() {
		super();
	}
	
	private void addScriptFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmPreInstallLabel = new Label(composite, SWT.NONE);
		rpmPreInstallLabel.setText(RPM_PRE_INSTALL);
		rpm_PreInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreInst = new GridData();
		gdPreInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreInstallText.setLayoutData(gdPreInst);

		Label rpmPostInstallLabel = new Label(composite, SWT.NONE);
		rpmPostInstallLabel.setText(RPM_POST_INSTALL);
		rpm_PostInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostInst = new GridData();
		gdPostInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostInstallText.setLayoutData(gdPostInst);

		Label rpmPreUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPreUnInstallLabel.setText(RPM_PRE_UNINSTALL);
		rpm_PreUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPreUnInst = new GridData();
		gdPreUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPreUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PreUnInstallText.setLayoutData(gdPreUnInst);

		Label rpmPostUnInstallLabel = new Label(composite, SWT.NONE);
		rpmPostUnInstallLabel.setText(RPM_POST_UNINSTALL);
		rpm_PostUnInstallText = new Text(composite, SWT.MULTI | SWT.BORDER
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
		GridData gdPostUnInst = new GridData();
		gdPostUnInst.widthHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_WIDTH);
		gdPostUnInst.heightHint = convertWidthInCharsToPixels(SCRIPT_ENTRIES_FIELD_HEIGHT);
		rpm_PostUnInstallText.setLayoutData(gdPostUnInst);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpm_PreInstall = RPMQuery.getPreInstallScript(rpmFile);
			rpm_PreInstallText.setText(rpm_PreInstall);
			
			String rpm_PostInstall = RPMQuery.getPostInstallScript(rpmFile);
			rpm_PostInstallText.setText(rpm_PostInstall);
			
			String rpm_PreUnInstall = RPMQuery.getPreUninstallScript(rpmFile);
			rpm_PreUnInstallText.setText(rpm_PreUnInstall);
			
			String rpm_PostUnInstall = RPMQuery.getPostUninstallScript(rpmFile);
			rpm_PostUnInstallText.setText(rpm_PostUnInstall);
		} catch(CoreException e) {
			ExceptionHandler.handle(e, getShell(),
					Messages.getString("ErrorDialog.title"), e.getMessage());
		}

	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addScriptFields(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {

	}

	public boolean performOk() {

		return true;
	}

}