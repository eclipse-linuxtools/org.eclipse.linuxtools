/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.launch;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunResourceVolumesVariablesModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

public class RunImageResourcesTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunResourcesTab.name"; //$NON-NLS-1$

	private static final int COLUMNS = 2;

	private final DataBindingContext dbc = new DataBindingContext();
	private ImageRunResourceVolumesVariablesModel model = null;

	private Composite container;

	public RunImageResourcesTab(ImageRunResourceVolumesVariablesModel model) {
		this.model = model;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		if (model == null) {
			setErrorMessage(LaunchMessages.getString("NoConnectionError.msg"));
		} else {
			setErrorMessage(null);
			createResourceSettingsContainer(container);
		}
		setControl(container);
	}

	private void createResourceSettingsContainer(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().spacing(10, 2).applyTo(container);
		final Button enableResourceLimitationButton = new Button(container,
				SWT.CHECK);
		enableResourceLimitationButton.setText(WizardMessages.getString(
				"ImageRunResourceVolVarPage.enableLimitationButton")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(enableResourceLimitationButton);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(enableResourceLimitationButton),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.ENABLE_RESOURCE_LIMITATIONS)
						.observe(model));
		final int COLUMNS = 5;
		final int INDENT = 20;
		final Composite subContainer = new Composite(container, SWT.NONE);
		setContainer(subContainer);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(subContainer);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(subContainer);

		// specify CPU limitation
		final Label cpuPriorityLabel = new Label(subContainer, SWT.NONE);
		cpuPriorityLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.cpuPriorityLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(cpuPriorityLabel);
		final Button lowCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		lowCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.lowButton")); //$NON-NLS-1$
		lowCPULimitationButton.addSelectionListener(
				onCpuShareWeighting(ImageRunResourceVolumesVariablesModel.LOW));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(lowCPULimitationButton);
		final Button mediumCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		mediumCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.mediumButton")); //$NON-NLS-1$
		mediumCPULimitationButton.addSelectionListener(onCpuShareWeighting(
				ImageRunResourceVolumesVariablesModel.MEDIUM));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(mediumCPULimitationButton);
		final Button highCPULimitationButton = new Button(subContainer,
				SWT.RADIO);
		mediumCPULimitationButton.setSelection(true);
		highCPULimitationButton.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.highButton")); //$NON-NLS-1$
		highCPULimitationButton.addSelectionListener(onCpuShareWeighting(
				ImageRunResourceVolumesVariablesModel.HIGH));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(highCPULimitationButton);
		dbc.bindValue(
				WidgetProperties.selection()
						.observe(enableResourceLimitationButton),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.ENABLE_RESOURCE_LIMITATIONS)
						.observe(model));

		// Memory limitation
		final Label memoryLimitLabel = new Label(subContainer, SWT.NONE);
		memoryLimitLabel.setText(WizardMessages
				.getString("ImageRunResourceVolVarPage.memoryLimit")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(memoryLimitLabel);
		final Scale memoryLimitSpinner = new Scale(subContainer, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(2, 1).applyTo(memoryLimitSpinner);
		memoryLimitSpinner.setBackground(
				Display.getDefault().getSystemColor(SWT.COLOR_TRANSPARENT));
		memoryLimitSpinner.setMinimum(0);
		int maxMemory = this.model.getTotalMemory();
		memoryLimitSpinner.setMaximum(this.model.getTotalMemory());
		memoryLimitSpinner.setSelection(Math.min(512, maxMemory));
		memoryLimitSpinner.setPageIncrement(64);
		dbc.bindValue(WidgetProperties.selection().observe(memoryLimitSpinner),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.MEMORY_LIMIT)
						.observe(model));

		final Text memoryLimitValueText = new Text(subContainer, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).hint(50, SWT.DEFAULT)
				.applyTo(memoryLimitValueText);
		dbc.bindValue(
				WidgetProperties.text(SWT.Modify).observe(memoryLimitValueText),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.MEMORY_LIMIT)
						.observe(model));
		dbc.bindValue(WidgetProperties.selection().observe(memoryLimitSpinner),
				BeanProperties
						.value(ImageRunResourceVolumesVariablesModel.class,
								ImageRunResourceVolumesVariablesModel.MEMORY_LIMIT)
						.observe(model));
		final Label memoryLimitValueLabel = new Label(subContainer, SWT.NONE);
		memoryLimitValueLabel.setText("MB"); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(memoryLimitValueLabel);

		// enable/disable controls
		enableResourceLimitationButton
				.addSelectionListener(onEnableResourceLimitation(subContainer));
		toggleResourceLimitationControls(subContainer);

	}

	private void setContainer(final Composite container) {
		this.container = container;
	}

	private Composite getContainer() {
		return container;
	}

	private SelectionListener onCpuShareWeighting(final int cpuShareWeigth) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				model.setCpuShareWeight(cpuShareWeigth);
			}
		};
	}

	private SelectionListener onEnableResourceLimitation(
			final Composite container) {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				toggleResourceLimitationControls(container);
			}

		};
	}

	private void toggleResourceLimitationControls(Composite container) {
		for (Control childControl : container.getChildren()) {
			if (model.isEnableResourceLimitations()) {
				childControl.setEnabled(true);
			} else {
				childControl.setEnabled(false);
			}
			updateLaunchConfigurationDialog();
		}
	}


	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_RESOURCE);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			boolean enableLimits = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS,
					false);
			model.setEnableResourceLimitations(enableLimits);

			int cpuShareWeight = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY,
					1024);
			model.setCpuShareWeight(cpuShareWeight);

			int maxMemory = this.model.getTotalMemory();
			int memoryLimit = Math.min(512, maxMemory);
			memoryLimit = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT,
					memoryLimit);
			model.setMemoryLimit(memoryLimit);
			toggleResourceLimitationControls(getContainer());
		} catch (CoreException e) {
			Activator.logErrorMessage(
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.load.failure"), //$NON-NLS-1$
					e);
		}
		model.addPropertyChangeListener(
				new LaunchConfigurationChangeListener());
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.ENABLE_LIMITS,
				model.isEnableResourceLimitations());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.MEMORY_LIMIT,
				model.getMemoryLimit());
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.CPU_PRIORITY,
				model.getCpuShareWeight());
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

}
