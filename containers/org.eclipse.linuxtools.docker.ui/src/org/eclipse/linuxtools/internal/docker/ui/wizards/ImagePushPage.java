/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * {@link WizardPage} to push an image to a registry.
 */
public class ImagePushPage extends ImagePullPushPage<ImagePushPageModel> {

	/**
	 * Constructor
	 * 
	 * @param image
	 *            the {@link IDockerImage} to push.
	 * @param selectedImageName
	 *            the default image name/tag
	 */
	public ImagePushPage(final IDockerImage image,
			final String selectedImageName) {
		super(WizardMessages.getString("ImagePush.name"), //$NON-NLS-1$
				WizardMessages.getString("ImagePush.title"), //$NON-NLS-1$
				new ImagePushPageModel(image, selectedImageName));
	}

	@Override
	public void dispose() {
		this.dbc.dispose();
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

	/**
	 * @return flag to indicate if the 'force' option should be used when
	 *         tagging the image.
	 */
	public boolean isForceTagging() {
		return getModel().isForceTagging();
	}

	/**
	 * @return flag to indicate if the tagged image should be kept upon
	 *         completion.
	 */
	public boolean isKeepTaggedImage() {
		return getModel().isKeepTaggedImage();
	}


	@SuppressWarnings("unchecked")
	@Override
	public void createControl(final Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(container);

		// registry selection
		final IObservableValue<IRegistry> registryAccountObservable = super.createRegistrySelectionControls(
				container);
		// image selection
		final IObservableValue<String> imageNameObservable = createImageSelectionControls(
				container);

		// force tagging
		final Button forceTaggingButton = new Button(container, SWT.CHECK);
		forceTaggingButton.setText(WizardMessages
				.getString("ImagePushPage.forcetagging.label")); //$NON-NLS-1$ );
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(forceTaggingButton);
		dbc.bindValue(WidgetProperties.selection().observe(forceTaggingButton),
				BeanProperties.value(ImagePushPageModel.class,
						ImagePushPageModel.FORCE_TAGGING).observe(getModel()));

		// keep tagged image upon completion
		final Button keepTaggedImageButton = new Button(container, SWT.CHECK);
		keepTaggedImageButton.setText(WizardMessages
				.getString("ImagePushPage.keeptaggedimage.label")); //$NON-NLS-1$ );
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(keepTaggedImageButton);
		dbc.bindValue(
				WidgetProperties.selection().observe(keepTaggedImageButton),
				BeanProperties
						.value(ImagePushPageModel.class,
								ImagePushPageModel.KEEP_TAGGED_IMAGE)
						.observe(getModel()));

		// setup validation support
		WizardPageSupport.create(this, dbc);
		dbc.addValidationStatusProvider(getModel().new ImagePushValidator(
				imageNameObservable, registryAccountObservable));
		setControl(container);
	}

	@SuppressWarnings("unchecked")
	private IObservableValue<String> createImageSelectionControls(
			final Composite container) {
		final Label nameLabel = new Label(container, SWT.NULL);
		nameLabel.setText(
				WizardMessages.getString("ImagePullPushPage.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(nameLabel);
		final Combo imageNameCombo = new Combo(container, SWT.DROP_DOWN);
		imageNameCombo.setToolTipText(
				WizardMessages.getString("ImagePushName.toolTip")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(imageNameCombo);
		final ComboViewer imageNameComboViewer = new ComboViewer(
				imageNameCombo);
		imageNameComboViewer.setContentProvider(new ArrayContentProvider());
		imageNameComboViewer.setInput(getModel().getImage().repoTags());
		// binding must take place after the input is set, so that default
		// repo/name can be selected.
		final IObservableValue<String> imageNameObservable = BeanProperties
				.value(ImagePushPageModel.class,
						ImagePullPushPageModel.SELECTED_IMAGE_NAME)
				.observe(getModel());
		dbc.bindValue(WidgetProperties.selection().observe(imageNameCombo),
				imageNameObservable);
		// filler for the last column
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(new Label(container, SWT.NONE));
		return imageNameObservable;
	}


}
