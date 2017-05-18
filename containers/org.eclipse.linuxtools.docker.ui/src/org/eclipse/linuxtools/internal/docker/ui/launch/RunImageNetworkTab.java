/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
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

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
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
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunNetworkModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RunImageNetworkTab extends AbstractLaunchConfigurationTab
		implements PropertyChangeListener {

	private static final String TAB_NAME = "RunNetworkTab.name"; //$NON-NLS-1$

	private static final int INDENT = 30;

	private static final int COLUMNS = 3;

	private final DataBindingContext dbc = new DataBindingContext();

	private final ImageRunNetworkModel model;

	private Control containerControl; // control selecting container
	private Control otherControl; // control selecting other selection

	public RunImageNetworkTab(ImageRunNetworkModel model) {
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
			createNetworkSettingsSection(container);
		}
		setControl(container);
	}

	private void createNetworkSettingsSection(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.span(COLUMNS, 1).grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().spacing(10, 2).applyTo(container);

		// specify default network
		final Label modeLabel = new Label(container, SWT.NONE);

		modeLabel.setText(WizardMessages.getString("ImageRunNetworkPage.mode")); //$NON-NLS-1$
		// $NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.grab(false, false).applyTo(modeLabel);

		final int COLUMNS = 2;
		final int INDENT = 20;
		final Composite subContainer = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.indent(INDENT, 0).span(COLUMNS, 1).grab(true, false)
				.applyTo(subContainer);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 5).applyTo(subContainer);

		modeLabel.setEnabled(true);
		final Button defaultButton = new Button(subContainer, SWT.RADIO);
		bindButton(defaultButton, ImageRunNetworkModel.DEFAULT_MODE);
		defaultButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.default")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(defaultButton);
		final Button bridgeButton = new Button(subContainer, SWT.RADIO);
		bridgeButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.bridge")); //$NON-NLS-1$
		bindButton(bridgeButton, ImageRunNetworkModel.BRIDGE_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(bridgeButton);
		final Button hostButton = new Button(subContainer, SWT.RADIO);
		hostButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.host")); //$NON-NLS-1$
		bindButton(hostButton, ImageRunNetworkModel.HOST_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(hostButton);
		final Button noneButton = new Button(subContainer, SWT.RADIO);
		noneButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.none")); // $NON-NLS-1$
		bindButton(noneButton, ImageRunNetworkModel.NONE_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1)
				.applyTo(noneButton);

		final Button containerButton = new Button(subContainer, SWT.RADIO);
		containerButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.container"));
		// $NON-NLS-1$
		bindButton(containerButton, ImageRunNetworkModel.CONTAINER_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(containerButton);

		// Container list
		final Combo containerList = new Combo(subContainer,
				SWT.DROP_DOWN | SWT.READ_ONLY);

		setContainerControl(containerList);

		containerList
				.setItems(model.getContainerNames().toArray(new String[] {}));
		if (containerList.getItemCount() > 0)
			containerList.select(0);
		containerList.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(containerList);
		dbc.bindList(WidgetProperties.items().observe(containerList),
				BeanProperties
						.list(ImageRunNetworkModel.class,
								ImageRunNetworkModel.CONTAINER_NAMES)
						.observe(model));
		dbc.bindValue(WidgetProperties.selection().observe(containerList),
				BeanProperties
						.value(ImageRunNetworkModel.class,
								ImageRunNetworkModel.SELECTED_CONTAINER)
						.observe(model));

		containerButton.addSelectionListener(
				onContainerSelection(containerButton, containerList));

		final Button otherButton = new Button(subContainer, SWT.RADIO);
		otherButton.setText(
				WizardMessages.getString("ImageRunNetworkPage.mode.other"));
		// $NON-NLS-1$
		bindButton(otherButton, ImageRunNetworkModel.OTHER_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(otherButton);

		// Container list
		final Text otherText = new Text(subContainer, SWT.SINGLE | SWT.BORDER);

		setOtherControl(otherText);

		otherText.setEnabled(false);

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(otherText);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(otherText),
				BeanProperties
						.value(ImageRunNetworkModel.class,
								ImageRunNetworkModel.OTHER_STRING)
						.observe(model));

		otherButton
				.addSelectionListener(onOtherSelection(otherButton, otherText));
	}

	private Control getContainerControl() {
		return containerControl;
	}

	private void setContainerControl(Control containerControl) {
		this.containerControl = containerControl;
	}

	private Control getOtherControl() {
		return otherControl;
	}

	private void setOtherControl(Control containerControl) {
		this.otherControl = containerControl;
	}

	/**
	 * Binds the given <code>network mode</code> value to the given
	 * {@link Button} when it is selected.
	 *
	 * @param button
	 *            the {@link Button} to bind
	 * @param strValue
	 *            the <code>network mode</code> to bind to the {@link Button}
	 * @return
	 */
	private Binding bindButton(final Button button, final String strValue) {
		return dbc.bindValue(WidgetProperties.selection().observe(button),
				BeanProperties
						.value(ImageRunNetworkModel.class,
								ImageRunNetworkModel.NETWORK_MODE)
						.observe(model),
				new UpdateValueStrategy() {
			@Override
					public Object convert(Object value) {
						if (value.equals(Boolean.TRUE)) {
							return strValue;
				}
						return null;
			}

				}, new UpdateValueStrategy() {
					@Override
					public Object convert(final Object value) {
						return value.equals(strValue);
					}
				});
	}

	private SelectionListener onContainerSelection(final Button containerButton,
			final Combo containerList) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final boolean selection = containerButton.getSelection();
			containerList.setEnabled(selection);
		});
	}

	private SelectionListener onOtherSelection(final Button otherButton,
			final Text otherText) {
		return SelectionListener.widgetSelectedAdapter(e -> {
			final boolean selection = otherButton.getSelection();
			otherText.setEnabled(selection);
		});
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_NETWORK_TAB);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

	}


	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		if (model == null)
			return;
		try {
			String networkModeString = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE,
					ImageRunNetworkModel.DEFAULT_MODE);
			if (networkModeString
					.startsWith(ImageRunNetworkModel.CONTAINER_MODE)) {
				String[] token = networkModeString.split(":");
				model.setNetworkMode(ImageRunNetworkModel.CONTAINER_MODE);
				if (token.length > 1) {
					model.setSelectedContainer(token[1]);
				}
				getContainerControl().setEnabled(true);
			} else {
				switch (networkModeString) {
				case ImageRunNetworkModel.BRIDGE_MODE:
				case ImageRunNetworkModel.HOST_MODE:
				case ImageRunNetworkModel.NONE_MODE:
				case ImageRunNetworkModel.DEFAULT_MODE:
					model.setNetworkMode(networkModeString);
					break;
				default:
					model.setNetworkMode(ImageRunNetworkModel.OTHER_MODE);
					model.setOtherString(networkModeString);
					getOtherControl().setEnabled(true);
					break;
				}
			}

			// update the underlying launch config working copy on model
			// changes.
			model.addPropertyChangeListener(
					new LaunchConfigurationChangeListener());
		} catch (CoreException e) {
			Activator.logErrorMessage(
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.load.failure"), //$NON-NLS-1$
					e);
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (model == null)
			return;
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.NETWORK_MODE,
				model.getNetworkModeString());
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		try {
			if (model == null)
				return false;
			if (model.getNetworkMode()
					.equals(ImageRunNetworkModel.CONTAINER_MODE)
					&& ((Combo) getContainerControl()).getText().isEmpty()) {
				setWarningMessage(WizardMessages
						.getString("WarningNoContainerSelected.msg")); // $NON-NLS-1
				return false;
			} else if (model.getNetworkMode()
					.equals(ImageRunNetworkModel.OTHER_MODE)
					&& ((Text) getOtherControl()).getText().isEmpty()) {
				setWarningMessage(
						WizardMessages.getString("WarningOtherFieldEmpty.msg")); // $NON-NLS-1
				return false;
			}
		} catch (Exception e) {
			Activator.log(e);
			return false;
		}
		setWarningMessage(null);
		return true;
	}

	@Override
	public String getName() {
		return LaunchMessages.getString(TAB_NAME);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// TODO Auto-generated method stub
	}

	private class LaunchConfigurationChangeListener
			implements PropertyChangeListener {

		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

}
