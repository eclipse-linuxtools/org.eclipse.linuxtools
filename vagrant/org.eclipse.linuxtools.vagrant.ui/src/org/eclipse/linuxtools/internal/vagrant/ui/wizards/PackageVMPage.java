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

import java.nio.file.Files;
import java.nio.file.Paths;

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
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class PackageVMPage extends WizardPage {

	private final PackageVMPageModel model;
	private final DataBindingContext dbc;

	public PackageVMPage() {
		super("PackageVMPage", //$NON-NLS-1$
				WizardMessages.getString("PackageVMPage.label"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("PackageVMPage.desc")); //$NON-NLS-1$
		this.model = new PackageVMPageModel();
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

	public String getBoxFolder() {
		return this.model.getBoxFolder();
	}

	@Override
	public void createControl(Composite parent) {
		ScrolledComposite scrollTop = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);

		final Composite container = new Composite(scrollTop, SWT.NONE);
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
				.value(PackageVMPageModel.class, PackageVMPageModel.BOX_NAME)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxNameText),
				boxNameObservable, new UpdateValueStrategy(), null);

		// Box folder
		final Label boxFolderLabel = new Label(container, SWT.NONE);
		boxFolderLabel.setText(WizardMessages.getString("PackageVMPage.folder.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(boxFolderLabel);

		final Text boxFolderText = new Text(container, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(boxFolderText);
		boxFolderText.setToolTipText(
				WizardMessages.getString("PackageVMPage.folder.desc")); //$NON-NLS-1$
		// Folder binding
		final IObservableValue<String> boxFolderObservable = BeanProperties
				.value(PackageVMPageModel.class, PackageVMPageModel.BOX_FOLDER)
				.observe(model);
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(boxFolderText),
				boxFolderObservable, new UpdateValueStrategy(), null);
		// search
		final Button searchButton = new Button(container, SWT.NONE);
		searchButton
				.setText(WizardMessages.getString("ImagePull.search.label")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchButton);
		searchButton.addSelectionListener(onSearchFolder());

		dbc.addValidationStatusProvider(new PackageVMValidationStatusProvider(
				boxNameObservable, boxFolderObservable));

		// setup validation support
		WizardPageSupport.create(this, dbc);

		scrollTop.setContent(container);
		Point point = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrollTop.setSize(point);
		scrollTop.setMinSize(point);
		setControl(container);
	}

	/**
	 * Opens the folder selection dialog.
	 * 
	 * @return
	 */
	private SelectionListener onSearchFolder() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				DirectoryDialog fd = new DirectoryDialog(getShell());
				String location = fd.open();
				if (location != null && !location.isEmpty()) {
					model.setBoxFolder(location);
				}
			}
		};
	}

	private class PackageVMValidationStatusProvider extends MultiValidator {

		private IObservableValue<String> boxNameOb, boxFolderOb;

		public PackageVMValidationStatusProvider(
				IObservableValue<String> boxNameOb,
				IObservableValue<String> boxFolderOb) {
			this.boxNameOb = boxNameOb;
			this.boxFolderOb = boxFolderOb;
		}

		@Override
		public IObservableList<String> getTargets() {
			// Work around for NPE triggered by DialogPageSupport.dispose()
			return new WritableList<>();
		}

		@Override
		protected IStatus validate() {
			String boxName = boxNameOb.getValue();
			String boxFolder = boxFolderOb.getValue();
			if (boxName == null || boxName.isEmpty()) {
				return ValidationStatus.error(
						WizardMessages.getString("AddBoxPage.emptyBoxName")); //$NON-NLS-1$
			} else if (boxFolder == null || boxFolder.isEmpty()) {
				return ValidationStatus.error(
						WizardMessages.getString("PackageVMPage.folder.empty")); //$NON-NLS-1$
			} else if (!Paths.get(boxFolder).toFile().exists()) {
				return ValidationStatus.error(WizardMessages
						.getString("PackageVMPage.folder.not.exist")); //$NON-NLS-1$
			} else if (!Files.isWritable(Paths.get(boxFolder))) {
				return ValidationStatus.error(WizardMessages
						.getString("PackageVMPage.folder.not.writeable")); //$NON-NLS-1$
			}
			return ValidationStatus.ok();
		}
	}

}
