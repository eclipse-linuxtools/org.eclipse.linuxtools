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

package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.vagrant.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AddBoxPage extends WizardPage {

	private final AddBoxPageModel model;
	private final DataBindingContext dbc;

	public AddBoxPage() {
		super("ImagePullPage", //$NON-NLS-1$
				WizardMessages.getString("ImagePull.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("ImagePull.desc")); //$NON-NLS-1$
		this.model = new AddBoxPageModel();
		this.dbc = new DataBindingContext();
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public String getBoxName() {
		return this.model.getBoxName();
	}

	public String getBoxLoc() {
		return this.model.getBoxLoc();
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		// Box name
		final Label boxNameLabel = new Label(container, SWT.NONE);
		boxNameLabel
				.setText(WizardMessages.getString("ImagePull.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxNameLabel);

		final Text boxNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(boxNameText);
		boxNameText.setToolTipText(
				WizardMessages.getString("ImagePull.name.tooltip")); //$NON-NLS-1$
		// Name binding
		final IObservableValue<String> boxNameObservable = BeanProperties
				.value(AddBoxPageModel.class, AddBoxPageModel.BOX_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxNameText),
				boxNameObservable, new UpdateValueStrategy(), null);

		// Box location
		final Label boxLocLabel = new Label(container, SWT.NONE);
		boxLocLabel
				.setText(WizardMessages.getString("ImagePull.loc.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxLocLabel);

		final Text boxLocText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(boxLocText);
		boxLocText.setToolTipText(
				WizardMessages.getString("ImagePull.loc.tooltip")); //$NON-NLS-1$
		// Location binding
		final IObservableValue<String> imageNameObservable = BeanProperties
				.value(AddBoxPageModel.class, AddBoxPageModel.BOX_LOC)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxLocText),
				imageNameObservable, new UpdateValueStrategy(), null);
		// search
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("ImagePull.search.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchImage());

		dbc.addValidationStatusProvider(new CreateBoxValidationStatusProvider(
				boxNameObservable, imageNameObservable));

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
				FileDialog fd = new FileDialog(getShell());
				String location = fd.open();
				if (location != null && !location.isEmpty()) {
					model.setBoxLoc(location);
				}
			}
		};
	}

	private class CreateBoxValidationStatusProvider extends MultiValidator {

		private IObservableValue<String> boxNameOb, boxLocOb;

		public CreateBoxValidationStatusProvider(
				IObservableValue<String> boxNameOb,
				IObservableValue<String> boxLocOb) {
			this.boxNameOb = boxNameOb;
			this.boxLocOb = boxLocOb;
		}

		@Override
		public IObservableList<String> getTargets() {
			// Work around for NPE triggered by DialogPageSupport.dispose()
			return new WritableList<>();
		}

		@Override
		protected IStatus validate() {
			String boxName = boxNameOb.getValue();
			String boxLoc = boxLocOb.getValue();
			if (boxName == null || boxName.isEmpty()) {
				return ValidationStatus.error(
						WizardMessages
							.getString("AddBoxPage.emptyBoxName")); //$NON-NLS-1$
			} else if (boxLoc == null || boxLoc.isEmpty()) {
				return ValidationStatus.error(
						WizardMessages
							.getString("AddBoxPage.emptyBoxLoc")); //$NON-NLS-1$
				}
			return ValidationStatus.ok();
		}
	}

}
