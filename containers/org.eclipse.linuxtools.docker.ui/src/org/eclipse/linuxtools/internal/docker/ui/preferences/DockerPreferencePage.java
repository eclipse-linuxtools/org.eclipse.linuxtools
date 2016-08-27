/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DockerPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private static final String REFRESH_TIME_MSG = "RefreshTime.label"; //$NON-NLS-1$
	private static final String RESTART_WAIT_TIME_MSG = "RestartWaitTime.label"; //$NON-NLS-1$

	private static final String INVALID_REFRESH_TIME_MSG = "RefreshTime.invalid.label"; //$NON-NLS-1$
	private static final String INVALID_RESTART_WAIT_TIME_MSG = "RestartWaitTime.invalid.label"; //$NON-NLS-1$
	
	private IntegerFieldEditor refreshTimeField;
	private IntegerFieldEditor restartWaitTimeField;

	public DockerPreferencePage() {
		super();
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(final IWorkbench workbench) {
	}

	/**
	 * Saves the current values in the {@link PreferenceStore}.
	 */
	private void savePreferences() {
		if (this.refreshTimeField != null) {
			this.refreshTimeField.store();
		}
		if (this.restartWaitTimeField != null) {
			this.restartWaitTimeField.store();
		}
	}

	@Override
	public boolean performOk() {
		savePreferences();
		return true;
	}
	

	@Override
	protected void performApply() {
		savePreferences();
		super.performApply();
	}
	
	@Override
	protected Control createContents(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(container);
		createContainerRefreshContainer(container);
		return container;
	}
	
	/**
	 * Create a container for the refresh rate property
	 * @param parent the parent container
	 */
	private void createContainerRefreshContainer(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1,  1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().margins(0, 0).spacing(10, 2).applyTo(container);
		refreshTimeField = new IntegerFieldEditor(
				PreferenceConstants.REFRESH_TIME,
				PreferenceMessages.getString(REFRESH_TIME_MSG), container);
		refreshTimeField.setPreferenceStore(getPreferenceStore());
		refreshTimeField
				.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		refreshTimeField.setValidRange(5, 200);
		refreshTimeField.setPage(this);
		refreshTimeField
				.setErrorMessage(PreferenceMessages.getString(INVALID_REFRESH_TIME_MSG));
		refreshTimeField.showErrorMessage();
		refreshTimeField.load();
		// If the preference changes, alert the Refresh Manager
		refreshTimeField
				.setPropertyChangeListener(event -> {
					if (event.getSource().equals(refreshTimeField)) {
						if (refreshTimeField.isValid()) {
							DockerContainerRefreshManager.getInstance()
									.setRefreshTime(
											refreshTimeField.getIntValue());
							setErrorMessage(null);
						} else {
							setErrorMessage(refreshTimeField.getErrorMessage());
						}
						setValid(refreshTimeField.isValid());
					}
				});
		restartWaitTimeField = new IntegerFieldEditor(
				PreferenceConstants.RESTART_WAIT_TIME,
				PreferenceMessages.getString(RESTART_WAIT_TIME_MSG), container);
		restartWaitTimeField.setPreferenceStore(getPreferenceStore());
		restartWaitTimeField
				.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		restartWaitTimeField.setValidRange(0, Integer.MAX_VALUE);
		restartWaitTimeField.setPage(this);
		restartWaitTimeField.setErrorMessage(
				PreferenceMessages.getString(INVALID_RESTART_WAIT_TIME_MSG));
		restartWaitTimeField.showErrorMessage();
		restartWaitTimeField.load();
		// If the preference changes, alert the Refresh Manager
		restartWaitTimeField.setPropertyChangeListener(event -> {
			if (event.getSource().equals(restartWaitTimeField)) {
				if (restartWaitTimeField.isValid()) {
					DockerContainerRefreshManager.getInstance()
							.setRefreshTime(restartWaitTimeField.getIntValue());
					setErrorMessage(null);
				} else {
					setErrorMessage(restartWaitTimeField.getErrorMessage());
				}
				setValid(restartWaitTimeField.isValid());
			}
		});
	}
	
}