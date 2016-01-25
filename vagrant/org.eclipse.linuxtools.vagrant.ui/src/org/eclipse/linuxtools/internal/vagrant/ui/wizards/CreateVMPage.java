/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
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
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.MultiValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.vagrant.core.EnvironmentsManager;
import org.eclipse.linuxtools.internal.vagrant.ui.SWTImagesFactory;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CreateVMPage extends WizardPage {

	private final CreateVMPageModel model;
	private final DataBindingContext dbc;

	private CreateVMAdvancedComposite advanced;

	public CreateVMPage(IVagrantBox box) {
		super("createVMPage", //$NON-NLS-1$
				WizardMessages.getString("CreateVM.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("CreateVM.desc")); //$NON-NLS-1$
		this.model = new CreateVMPageModel();
		if (box != null) {
			this.model.setBoxRef(box.getName());
		}
		this.dbc = new DataBindingContext();
	}

	@Override
	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	public String getVMName() {
		return model.getVFileMode() ? null : this.model.getVMName();
	}

	public String getBoxReference() {
		return model.getVFileMode() ? null : this.model.getBoxRef();
	}

	public String getVMFile() {
		return model.getVFileMode() ? this.model.getVMFile() : null;
	}

	public Map<String, String> getEnvironment() {
		return model.getEnvironment();
	}

	@Override
	public void createControl(Composite parent) {
		ScrolledComposite scrollTop = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);

		final Composite container = new Composite(scrollTop, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).margins(6, 6)
				.applyTo(container);

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
		final IObservableValue<String> vmmNameObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(vmNameText),
				vmmNameObservable);

		// Box name
		final Label boxRefLabel = new Label(container, SWT.NONE);
		boxRefLabel
				.setText(WizardMessages.getString("CreateVMPage.boxRef.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxRefLabel);

		final Text boxRefText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(boxRefText);
		boxRefText.setToolTipText(
				WizardMessages.getString("CreateVMPage.boxRef.tooltip")); //$NON-NLS-1$
		// Box Name binding
		final IObservableValue<String> boxRefObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.BOX_REF)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxRefText),
				boxRefObservable);

		// Box name search
		final Button boxSearchButton = new Button(container, SWT.NONE);
		boxSearchButton
				.setText(WizardMessages.getString("CreateVMPage.search.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxSearchButton);
		boxSearchButton.addSelectionListener(onSearchImage());

		// VM File Checkbox
		final Button customVMFileButton = new Button(container, SWT.CHECK);
		customVMFileButton
				.setText(WizardMessages.getString("CreateVMPage.File.CheckBox")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1).applyTo(customVMFileButton);
		final IObservableValue<String> customVMFileObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.V_FILE_MODE)
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
		final IObservableValue<String> boxLocObservable = BeanProperties
				.value(CreateVMPageModel.class, CreateVMPageModel.VM_FILE)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxLocText),
				boxLocObservable);
		boxLocText.addModifyListener(e -> vmFileChanged(boxLocText.getText()));

		// Vagrantfile search
		final Button vgFilesearchButton = new Button(container, SWT.NONE);
		vgFilesearchButton
				.setText(WizardMessages.getString("CreateVMPage.search.label")); //$NON-NLS-1$
		vgFilesearchButton.setEnabled(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(vgFilesearchButton);
		vgFilesearchButton.addSelectionListener(onSearchImage());

		customVMFileButton.addSelectionListener(
				onCheckCustomVMFile(vmNameText, boxRefText, boxLocText, vgFilesearchButton, boxSearchButton));

		dbc.addValidationStatusProvider(new CreateVMValidationStatusProvider(vmmNameObservable,
				boxRefObservable, boxLocObservable));

		advanced = new CreateVMAdvancedComposite(
				container, scrollTop, model);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1)
				.grab(true, false).applyTo(advanced);

		// setup validation support
		WizardPageSupport.create(this, dbc);

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		setControl(container);
	}

	private SelectionListener onCheckCustomVMFile(Text vmNameText,
			Text boxRefText, Text boxLocText, Button searchButton,
			Button boxSearchButton) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.widget instanceof Button) {
					Button bt = (Button) e.widget;
					if (bt.getSelection()) {
						vmNameText.setEnabled(false);
						boxRefText.setEnabled(false);
						boxSearchButton.setEnabled(false);
						searchButton.setEnabled(true);
						boxLocText.setEnabled(true);
						model.setVFileMode(true);
					} else {
						vmNameText.setEnabled(true);
						boxRefText.setEnabled(true);
						boxSearchButton.setEnabled(true);
						searchButton.setEnabled(false);
						boxLocText.setEnabled(false);
						model.setVFileMode(false);
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
					if (location.endsWith("box")) { //$NON-NLS-1$
						model.setBoxRef(location);
					} else {
						model.setVMFile(location);
						vmFileChanged(location);
					}
				}
			}
		};
	}

	private void vmFileChanged(String vagrantFile) {
		IPath folder = new Path(vagrantFile).removeLastSegments(1);
		Map<String, String> existingEnv = EnvironmentsManager.getSingleton()
				.getEnvironment(folder.toFile());
		if (existingEnv != null) {
			model.setEnvironment(existingEnv);
			advanced.refresh();
		}
	}

	private class CreateVMValidationStatusProvider extends MultiValidator {

		private IObservableValue<String> vmNameOb, boxRefOb, boxLocOb;

		public CreateVMValidationStatusProvider(
				IObservableValue<String> vmNameOb,
				IObservableValue<String> boxRefOb,
				IObservableValue<String> boxLocOb) {
			this.vmNameOb = vmNameOb;
			this.boxRefOb = boxRefOb;
			this.boxLocOb = boxLocOb;
		}

		@Override
		public IObservableList<String> getTargets() {
			// Work around for NPE triggered by DialogPageSupport.dispose()
			return new WritableList<>();
		}

		@Override
		protected IStatus validate() {
			String vmName = vmNameOb.getValue();
			String boxRef = boxRefOb.getValue();
			String boxLoc = boxLocOb.getValue();
			if (!model.getVFileMode()) {
				if (vmName == null || vmName.isEmpty()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.emptyVMName")); //$NON-NLS-1$
				} else if (boxRef == null || boxRef.isEmpty()) {
					return ValidationStatus.error(WizardMessages
							.getString("CreateVMPage.emptyBoxRef")); //$NON-NLS-1$
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
