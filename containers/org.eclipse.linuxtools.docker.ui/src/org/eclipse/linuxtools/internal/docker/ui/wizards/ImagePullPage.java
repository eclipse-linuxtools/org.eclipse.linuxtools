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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.wizards.ImageSearch;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
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
public class ImagePullPage extends WizardPage {

	private final ImagePullPageModel model;
	private final DataBindingContext dbc;
	private final IDockerConnection connection;

	private static final String REGISTRY_HOST = "[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*"; //$NON-NLS-1$
	private static final String REGISTRY_PORT = "[0-9]+"; //$NON-NLS-1$
	private static final String REPOSITORY = "[a-z0-9]+([._-][a-z0-9]+)*"; //$NON-NLS-1$
	private static final String NAME = "[a-z0-9]+([._-][a-z0-9]+)*"; //$NON-NLS-1$
	private static final String TAG = "[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*"; //$NON-NLS-1$
	private static final Pattern imageNamePattern = Pattern.compile("(" //$NON-NLS-1$
			+ REGISTRY_HOST + "(\\:" + REGISTRY_PORT + ")?/)?" + "(" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			+ REPOSITORY + "/)?" + NAME + "(?<tag>\\:" + TAG + ")?"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	public ImagePullPage(final IDockerConnection connection) {
		super("ImagePullPage", //$NON-NLS-1$
				WizardMessages.getString("ImagePull.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		this.model = new ImagePullPageModel();
		this.dbc = new DataBindingContext();
		this.connection = connection;
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public String getImageName() {
		final Matcher matcher = imageNamePattern
				.matcher(this.model.getImageName());
		// Matcher#matches() must be called before any attempt to access a given
		// named capturing-group.
		if (matcher.matches() && matcher.group("tag") == null) { //$NON-NLS-1$
			return this.model.getImageName() + ":latest"; //$NON-NLS-1$
		}

		return this.model.getImageName();
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		// Image name
		final Label imageNameLabel = new Label(container, SWT.NONE);
		imageNameLabel
				.setText(WizardMessages.getString("ImagePull.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(imageNameLabel);

		final Text imageNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(imageNameText);
		imageNameText.setToolTipText(
				WizardMessages.getString("ImagePull.name.tooltip")); //$NON-NLS-1$
		// binding
		final IObservableValue imgeNameObservable = BeanProperties
				.value(ImagePullPageModel.class, ImagePullPageModel.IMAGE_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(imageNameText),
				imgeNameObservable, new UpdateValueStrategy()
						.setAfterConvertValidator(new ImageNameValidator()),
				null);
		// search
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("ImagePull.search.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchImage());

		// setup validation support
		WizardPageSupport.create(this, dbc);
		setControl(container);
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
						ImagePullPage.this.model.getImageName());
				final boolean completed = CommandUtils
						.openWizard(imageSearchWizard, getShell());
				if (completed) {
					model.setImageName(imageSearchWizard.getSelectedImage());
				}
			}
		};
	}

	/**
	 * Validates that the image name matches
	 * [REGISTRY_HOST[:REGISTRY_PORT]/]IMAGE_NAME[:TAG]
	 */
	public static class ImageNameValidator implements IValidator {

		@Override
		public IStatus validate(final Object value) {
			final String imageName = (String) value;
			if (imageName.isEmpty()) {
				return ValidationStatus
						.cancel(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
			}
			final Matcher matcher = imageNamePattern.matcher(imageName);
			if (!matcher.matches()) {
				return ValidationStatus.warning(WizardMessages
						.getString("ImagePull.name.invalidformat.msg")); //$NON-NLS-1$
			} else if (matcher.group("tag") == null) { //$NON-NLS-1$
				return ValidationStatus.warning(
						WizardMessages.getString("ImagePull.assumeLatest.msg")); //$NON-NLS-1$

			}
			return Status.OK_STATUS;
		}

	}

}
