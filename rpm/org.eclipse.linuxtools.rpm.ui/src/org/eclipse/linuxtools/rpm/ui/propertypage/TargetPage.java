/*
 * (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.ui.propertypage;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.ui.dialogs.PropertyPage;

public class TargetPage extends PropertyPage {
	
	private static final String RPM_ARCH = Messages.getString("TargetPage.Architecture"); //$NON-NLS-1$

	private static final String RPM_PLATFORM = Messages.getString("TargetPage.Platform"); //$NON-NLS-1$

	private static final String RPM_OS = Messages.getString("TargetPage.OS"); //$NON-NLS-1$

	private static final String RPM_HOST = Messages.getString("TargetPage.BuildHost"); //$NON-NLS-1$

	private static final String RPM_TIME = Messages.getString("TargetPage.BuildTime"); //$NON-NLS-1$

	private static final int NAME_FIELD_WIDTH = 20;

	private static final int ARCH_FIELD_WIDTH = 8;

	private static final int PLATFORM_FIELD_WIDTH = 20;

	private static final int OS_FIELD_WIDTH = 10;

	private static final int HOST_FIELD_WIDTH = 40;

	private static final int TIME_FIELD_WIDTH = 35;

	private Label rpm_nameText;

	private Label rpm_archText;

	private Label rpm_platformText;

	private Label rpm_osText;

	private Label rpm_hostText;

	private Label rpm_timeText;

	/**
	 * Constructor for RPMPropertyPage.
	 */
	public TargetPage() {
		super();
	}

	private void addTargetFields(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// RPM labels and text fields setup

		Label rpmArchLabel = new Label(composite, SWT.NONE);
		rpmArchLabel.setText(RPM_ARCH);
		rpm_archText = new Label(composite, SWT.HORIZONTAL);
		GridData gdArch = new GridData();
		gdArch.widthHint = convertWidthInCharsToPixels(ARCH_FIELD_WIDTH);
		rpm_archText.setLayoutData(gdArch);

		Label rpmPlatformLabel = new Label(composite, SWT.NONE);
		rpmPlatformLabel.setText(RPM_PLATFORM);
		rpm_platformText = new Label(composite, SWT.HORIZONTAL);
		GridData gdPlatform = new GridData();
		gdPlatform.widthHint = convertWidthInCharsToPixels(PLATFORM_FIELD_WIDTH);
		rpm_platformText.setLayoutData(gdPlatform);

		Label rpmOSLabel = new Label(composite, SWT.NONE);
		rpmOSLabel.setText(RPM_OS);
		rpm_osText = new Label(composite, SWT.HORIZONTAL);
		GridData gdOS = new GridData();
		gdOS.widthHint = convertWidthInCharsToPixels(OS_FIELD_WIDTH);
		rpm_osText.setLayoutData(gdOS);

		Label rpmHostLabel = new Label(composite, SWT.NONE);
		rpmHostLabel.setText(RPM_HOST);
		rpm_hostText = new Label(composite, SWT.HORIZONTAL);
		GridData gdHost = new GridData();
		gdHost.widthHint = convertWidthInCharsToPixels(HOST_FIELD_WIDTH);
		rpm_hostText.setLayoutData(gdHost);

		Label rpmTimeLabel = new Label(composite, SWT.NONE);
		rpmTimeLabel.setText(RPM_TIME);
		rpm_timeText = new Label(composite, SWT.HORIZONTAL);
		GridData gdTime = new GridData();
		gdTime.widthHint = convertWidthInCharsToPixels(TIME_FIELD_WIDTH);
		rpm_timeText.setLayoutData(gdTime);

		// Populate RPM text fields
		try {
			IFile rpmFile = (IFile) getElement();
			String rpm_path = ((IResource) getElement()).getRawLocation()
			.toString();
			String rpm_arch = RPMQuery.getArch(rpmFile);
			rpm_archText.setText(rpm_arch);
			String rpm_platform = RPMQuery.getPlatform(rpmFile);
			rpm_platformText.setText(rpm_platform);
			String rpm_os = RPMQuery.getOS(rpmFile);
			rpm_osText.setText(rpm_os);
			String rpm_host = RPMQuery.getBuildHost(rpmFile);
			rpm_hostText.setText(rpm_host);
			String rpm_time = RPMQuery.getBuildTime(rpmFile);
			rpm_timeText.setText(rpm_time);
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

		addTargetFields(composite);
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