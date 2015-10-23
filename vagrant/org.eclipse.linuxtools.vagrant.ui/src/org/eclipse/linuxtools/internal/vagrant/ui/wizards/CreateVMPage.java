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

import java.nio.file.Paths;

import org.eclipse.core.databinding.DataBindingContext;
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
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
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

public class CreateVMPage extends WizardPage {

	private final CreateVMPageModel model;
	private final DataBindingContext dbc;

	public CreateVMPage(IVagrantBox box) {
		super("createVMPage", //$NON-NLS-1$
				WizardMessages.getString("CreateVM.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("CreateVM.desc")); //$NON-NLS-1$
		this.model = new CreateVMPageModel();
		if (box != null) {
			this.model.setBoxName(box.getName());
		}
		this.dbc = new DataBindingContext();
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public String getVMName() {
		return model.getBoxLocMode() ? null : this.model.getVMName();
	}

	public String getBoxName() {
		return model.getBoxLocMode() ? null : this.model.getBoxName();
	}

	public String getVMFile() {
		return model.getBoxLocMode() ? this.model.getVMFile() : null;
	}

	@Override
	public void createControl(Composite parent) {
		parent.setLayout(new GridLayout());
		final Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1, 1)
				.grab(true, false).applyTo(container);

		// VM Name
		final Label vmNameLabel = new Label(container, SWT.NONE);
		vmNameLabel
				.setText(WizardMessages.getString("CreateVM.name.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(vmNameLabel);

		final Text vmNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(vmNameText);
		vmNameText.setToolTipText(
				WizardMessages.getString("CreateVMPage.name.tooltip")); //$NON-NLS-1$
		// VM Name binding
		final IObservableValue vmmNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(vmNameText),
				vmmNameObservable);

		// Box name
		final Label boxNameLabel = new Label(container, SWT.NONE);
		boxNameLabel
				.setText(WizardMessages.getString("CreateVMPage.boxName.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxNameLabel);

		final Text boxNameText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(2, 1).applyTo(boxNameText);
		boxNameText.setToolTipText(
				WizardMessages.getString("CreateVMPage.boxName.tooltip")); //$NON-NLS-1$
		// Box Name binding
		final IObservableValue boxNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.BOX_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxNameText),
				boxNameObservable);

		// VM File Checkbox
		final Button customVMFileButton = new Button(container, SWT.CHECK);
		customVMFileButton
				.setText(WizardMessages.getString("CreateVMPage.File.CheckBox")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1).applyTo(customVMFileButton);
		final IObservableValue customVMFileObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.BOX_LOC_MODE)
				.observe(model);
		dbc.bindValue(WidgetProperties.selection().observe(customVMFileButton),
				customVMFileObservable);

		// VM File
		final Label boxLocLabel = new Label(container, SWT.NONE);
		boxLocLabel
				.setText(WizardMessages.getString("CreateVMPage.loc.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxLocLabel);

		final Text boxLocText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(boxLocText);
		boxLocText.setToolTipText(
				WizardMessages.getString("CreateVMPage.loc.tooltip")); //$NON-NLS-1$
		boxLocText.setEnabled(false);
		// Location binding
		final IObservableValue boxLocObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_FILE)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxLocText),
				boxLocObservable);

		// search
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("CreateVMPage.search.label")); //$NON-NLS-1$
		searchButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchImage());

		customVMFileButton.addSelectionListener(
				onCheckCustomVMFile(vmNameText, boxNameText, boxLocText, searchButton));

		dbc.addValidationStatusProvider(new CreateVMValidationStatusProvider(vmmNameObservable,
				boxNameObservable, boxLocObservable));

		// setup validation support
		WizardPageSupport.create(this, dbc);
		setControl(container);
	}

	private SelectionListener onCheckCustomVMFile(Text vmNameText,
			Text boxNameText, Text boxLocText, Button searchButton) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.widget instanceof Button) {
					Button bt = (Button) e.widget;
					if (bt.getSelection()) {
						vmNameText.setEnabled(false);
						boxNameText.setEnabled(false);
						searchButton.setEnabled(true);
						boxLocText.setEnabled(true);
						model.setBoxLocMode(true);
					} else {
						vmNameText.setEnabled(true);
						boxNameText.setEnabled(true);
						searchButton.setEnabled(false);
						boxLocText.setEnabled(false);
						model.setBoxLocMode(false);
					}
				}
			}
		};
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
					model.setVMFile(location);
				}
			}
		};
	}

	public class CreateVMValidationStatusProvider extends MultiValidator {

		private IObservableValue vmNameOb, boxNameOb, boxLocOb;

		public CreateVMValidationStatusProvider(IObservableValue vmNameOb,
				IObservableValue boxNameOb, IObservableValue boxLocOb) {
			this.vmNameOb = vmNameOb;
			this.boxNameOb = boxNameOb;
			this.boxLocOb = boxLocOb;
		}

		@Override
		public IObservableList getTargets() {
			// Work around for NPE triggered by DialogPageSupport.dispose()
			return new WritableList();
		}

		@Override
		protected IStatus validate() {
			String vmName = (String) vmNameOb.getValue();
			String boxName = (String) boxNameOb.getValue();
			String boxLoc = (String) boxLocOb.getValue();
			if (!model.getBoxLocMode()) {
				if (vmName == null || vmName.isEmpty()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.emptyVMName")); //$NON-NLS-1$
				} else if (boxName == null || boxName.isEmpty()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.emptyBoxName")); //$NON-NLS-1$
				}
			} else {
				if (boxLoc == null || boxLoc.isEmpty()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.emptyVagrantfile")); //$NON-NLS-1$
				}
				if (!Paths.get(boxLoc).toFile().canRead()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.invalidVagrantfile")); //$NON-NLS-1$
				}
			}
			return ValidationStatus.ok();
		}

	}
}
