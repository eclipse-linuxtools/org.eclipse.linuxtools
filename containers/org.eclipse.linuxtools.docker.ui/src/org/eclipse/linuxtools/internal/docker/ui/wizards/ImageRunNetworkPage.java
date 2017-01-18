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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A {@link WizardPage} to let the user select the CPU and memory allocation for
 * the container, as well as the volumes to mount.
 * 
 * @author jjohnstn
 *
 */
public class ImageRunNetworkPage extends WizardPage {

	private final int COLUMNS = 2;

	private final static String NETWORK_MODE = "networkMode"; //$NON-NLS-1$

	private final DataBindingContext dbc = new DataBindingContext();
	private final ImageRunNetworkModel model;
	private final ILaunchConfiguration lastLaunchConfiguration;

	private Control containerControl; // control selecting container
	private Control otherControl; // control selecting other selection

	/**
	 * Default constructor.
	 *
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 *
	 */
	public ImageRunNetworkPage(final IDockerConnection connection) {
		super("ImageRunNetworkPage", //$NON-NLS-1$
				WizardMessages.getString("ImageRunNetworkPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunNetworkModel(connection);
		this.lastLaunchConfiguration = null;
	}

	/**
	 * Default constructor.
	 *
	 * @param selectedImage
	 *            the {@link IDockerImage} to run
	 * @param connection
	 *            the {@link IDockerConnection} to use
	 * @throws DockerException
	 *             if obtaining info from the given {@link IDockerConnection}
	 *             failed
	 *
	 */
	public ImageRunNetworkPage(
			final IDockerImage selectedImage,
			final ILaunchConfiguration lastLaunchConfiguration) {
		super("ImageRunNetworkPage", //$NON-NLS-1$
				WizardMessages.getString("ImageRunNetworkPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setPageComplete(true);
		this.model = new ImageRunNetworkModel(selectedImage);
		this.lastLaunchConfiguration = lastLaunchConfiguration;
	}

	public ImageRunNetworkModel getModel() {
		return model;
	}

	@Override
	public void createControl(Composite parent) {
		final ScrolledComposite scrollTop = new ScrolledComposite(parent,
				SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);
		final Composite container = new Composite(scrollTop, SWT.NONE);
		container.setLayout(new GridLayout());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.applyTo(container);
		createNetworkModeContainer(container);
		setDefaultValues();

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		setControl(scrollTop);
	}

	@SuppressWarnings("unchecked")
	private void createNetworkModeContainer(final Composite parent) {
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
				WizardMessages.getString("ImageRunNetworkPage.mode.container")); //$NON-NLS-1$
		// $NON-NLS-1$
		bindButton(containerButton, ImageRunNetworkModel.CONTAINER_MODE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.applyTo(containerButton);


		// Container list
		final Combo containerList = new Combo(subContainer, SWT.BORDER);

		setContainerControl(containerList);

		containerList
				.setItems(model.getContainerNames().toArray(new String[] {}));
		if (containerList.getItemCount() > 0)
			containerList.select(0);
		containerList.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(containerList);
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

	@Override
	public boolean isPageComplete() {
		if (model == null)
			return false;
		if (ImageRunNetworkModel.CONTAINER_MODE.equals(model.getNetworkMode())) {
			if (((Combo)getContainerControl()).getText().isEmpty()) {
				setMessage(
						WizardMessages
								.getString("WarningNoContainerSelected.msg"), //$NON-NLS-1$
						IMessageProvider.WARNING);
				return false;
			} else if (ImageRunNetworkModel.OTHER_MODE
					.equals(model.getNetworkMode())) {
				if (((Text) getOtherControl()).getText().isEmpty()) {
					setMessage(
							WizardMessages
									.getString("WarningOtherFieldEmpty.msg"), //$NON-NLS-1$
							IMessageProvider.WARNING);
					return false;
				}
			}
		}
		setMessage(null, IMessageProvider.WARNING);
		return true;
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
	@SuppressWarnings("unchecked")
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
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final boolean selection = containerButton.getSelection();
				containerList.setEnabled(selection);
			}
		};
	}

	private SelectionListener onOtherSelection(final Button otherButton,
			final Text otherText) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final boolean selection = otherButton.getSelection();
				otherText.setEnabled(selection);
			}
		};
	}

	private void setDefaultValues() {
		try {
			if (lastLaunchConfiguration != null) {
				String networkModeString = lastLaunchConfiguration.getAttribute(
						NETWORK_MODE, ImageRunNetworkModel.DEFAULT_MODE);
				if (networkModeString
						.startsWith(ImageRunNetworkModel.CONTAINER_MODE)) {
					String[] token = networkModeString.split(":");
					model.setNetworkMode(ImageRunNetworkModel.CONTAINER_MODE);
					if (token.length > 1)
						model.setSelectedContainer(token[1]);
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
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

}
