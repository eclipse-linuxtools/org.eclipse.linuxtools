/*******************************************************************************
 * Copyright (c) 2010 Elliott Baron
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@fedoraproject.org> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.internal.valgrind.launch.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.linuxtools.profiling.launch.remote.RemoteTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ValgrindRemoteTab extends RemoteTab {
	
	private Text destDirText;
	private Text tmpDirText;
	private Text valgrindLocText;
	private boolean isInitializing;

	public ValgrindRemoteTab() {
		super(Messages.ValgrindRemoteTab_tab_name);
	}
	
	@Override
	protected void localCreateControl(Composite top) {

		Composite pathTop = new Composite(top, SWT.NONE);
		pathTop.setLayout(new GridLayout(2, false));
		pathTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Valgrind location
		Label valgrindLocLabel = new Label(pathTop, SWT.NONE);
		valgrindLocLabel.setText(Messages.ValgrindRemoteTab_label_location_VG);

		valgrindLocText = new Text(pathTop, SWT.BORDER);
		valgrindLocText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		valgrindLocText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		// Destination directory
		Label destDirLabel = new Label(pathTop, SWT.NONE);
		destDirLabel.setText(Messages.ValgrindRemoteTab_label_dest_wd);

		destDirText = new Text(pathTop, SWT.BORDER);
		destDirText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		destDirText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Label tmpDirLabel = new Label(pathTop, SWT.NONE);
		tmpDirLabel.setText(Messages.ValgrindRemoteTab_label_tmp_dir);

		tmpDirText = new Text(pathTop, SWT.BORDER);
		tmpDirText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		tmpDirText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC);
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_DESTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR);
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR);
	}

	@Override
	public void localInitializeFrom(ILaunchConfiguration configuration) throws CoreException {

		String valgrindLoc = configuration.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_VALGRINDLOC);
		if (valgrindLoc != null) {
			valgrindLocText.setText(valgrindLoc);
		}

		String destDir = configuration.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_DESTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_DESTDIR);
		if (destDir != null) {
			destDirText.setText(destDir);
		}

		String tmpDir = configuration.getAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, ValgrindRemoteLaunchConstants.DEFAULT_REMOTE_OUTPUTDIR);
		if (tmpDir != null) {
			tmpDirText.setText(tmpDir);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_VALGRINDLOC, valgrindLocText.getText());
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_DESTDIR, destDirText.getText());
		configuration.setAttribute(ValgrindRemoteLaunchConstants.ATTR_REMOTE_OUTPUTDIR, tmpDirText.getText());
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid = super.isValid(launchConfig);
		if (valid) {
			valid = valgrindLocText.getText().length() > 0;
			if (valid) {
				valid = destDirText.getText().length() > 0;
				if (valid) {
					valid = tmpDirText.getText().length() > 0;
					if (!valid) {
						setErrorMessage(Messages.ValgrindRemoteTab_error_tmp_dir);
					}
				}
				else {
					setErrorMessage(Messages.ValgrindRemoteTab_error_dest_wd);
				}
			}
			else {
				setErrorMessage(Messages.ValgrindRemoteTab_error_location_VG);
			}
		}
		else {
			setErrorMessage(Messages.ValgrindRemoteTab_error_peer);
		}
		
		return valid;
	}

	@Override
	public String getName() {
		return Messages.ValgrindRemoteTab_tab_name;
	}

	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}		
	}
}
