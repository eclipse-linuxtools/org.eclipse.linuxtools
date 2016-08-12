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

package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 */
public class ImagePullPage extends ImagePullPushPage<ImagePullPageModel> {

	private final IDockerConnection connection;

	/**
	 * Constructor.
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} to use to pull the image
	 */
	public ImagePullPage(final IDockerConnection connection) {
		super("ImagePullPage", //$NON-NLS-1$
				WizardMessages.getString("ImagePull.label"),
				new ImagePullPageModel());
		setMessage(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		this.connection = connection;
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	/**
	 * @return the tag to select/apply on the image
	 */
	public String getSelectedImageName() {
		return getModel().getSelectedImageName();
	}

	/**
	 * @return the target {@link IRegistry} on which to push the image
	 */
	public IRegistry getSelectedRegistryAccount() {
		return getModel().getSelectedRegistry();
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		// registry selection
		super.createRegistrySelectionControls(container);

		// image name selection
		createImageNameSelectionControls(container);

		// setup validation support
		WizardPageSupport.create(this, dbc);
		setControl(container);
	}

	@SuppressWarnings("unchecked")
	void createImageNameSelectionControls(final Composite parent) {
		// Image name
		final Label imageNameLabel = new Label(parent, SWT.NONE);
		imageNameLabel.setText(
				WizardMessages.getString("ImagePullPushPage.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageNameLabel);

		final Text imageNameText = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(imageNameText);
		imageNameText.setToolTipText(
				WizardMessages.getString("ImagePull.name.tooltip")); //$NON-NLS-1$

		// search
		final Button searchButton = new Button(parent, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("ImagePull.search.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchImage());

		// binding
		final IObservableValue<String> imgeNameObservable = BeanProperties
				.value(ImagePullPushPageModel.class,
						ImagePullPushPageModel.SELECTED_IMAGE_NAME)
				.observe(getModel());
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(imageNameText),
				imgeNameObservable, new UpdateValueStrategy()
						.setAfterConvertValidator(new ImageNameValidator()),
				null);
	}

	/**
	 * Opens the {@link ImageSearch} dialog with current image name pre-filled.
	 * 
	 * @return
	 */
	private SelectionListener onSearchImage() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final ImageSearch imageSearchWizard = new ImageSearch(
						ImagePullPage.this.connection,
						ImagePullPage.this.getModel().getSelectedImageName(),
						ImagePullPage.this.getModel()
								.getSelectedRegistry());
				final boolean completed = CommandUtils
						.openWizard(imageSearchWizard, getShell());
				if (completed) {
					ImagePullPage.this.getModel().setSelectedImageName(
							imageSearchWizard.getSelectedImage());
				}
			}
		};
	}

}
