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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ContainerLinkDialog;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageRunSelectionModel.ContainerLinkModel;
import org.eclipse.linuxtools.internal.docker.ui.wizards.WizardMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

public class RunImageLinksTab extends AbstractLaunchConfigurationTab {

	private static final String TAB_NAME = "RunLinksTab.name"; //$NON-NLS-1$

	private static final int COLUMNS = 3;

	private ImageRunSelectionModel model = null;

	public RunImageLinksTab(ImageRunSelectionModel model) {
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
			createLinkSettingsSection(container);
		}
		setControl(container);
	}

	private TableViewerColumn createTableViewerColumn(
			final TableViewer tableViewer, final String title,
			final int width) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setWidth(width);
		return viewerColumn;
	}

	private void createLinkSettingsSection(final Composite container) {
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(3, 1)
				.applyTo(new Label(container, SWT.NONE));
		final Label linksLabel = new Label(container, SWT.NONE);
		linksLabel.setText(
				WizardMessages.getString("ImageRunSelectionPage.links"));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).span(COLUMNS, 1).applyTo(linksLabel);
		final TableViewer linksTableViewer = createLinksTable(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, true)
				.hint(200, 100).applyTo(linksTableViewer.getTable());
		// buttons
		final Composite buttonsContainers = new Composite(container, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(false, false).applyTo(buttonsContainers);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.spacing(SWT.DEFAULT, 0).applyTo(buttonsContainers);

		final Button addButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(addButton);
		addButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.addButton")); //$NON-NLS-1$
		addButton.addSelectionListener(onAddLink());
		final Button editButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(editButton);
		editButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.editButton")); //$NON-NLS-1$
		editButton.setEnabled(false);
		editButton.addSelectionListener(onEditLink(linksTableViewer));
		final Button removeButton = new Button(buttonsContainers, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP)
				.grab(true, false).applyTo(removeButton);
		removeButton.setText(
				WizardMessages.getString("ImageRunSelectionPage.remove")); //$NON-NLS-1$
		removeButton.addSelectionListener(onRemoveLinks(linksTableViewer));
		removeButton.setEnabled(false);
		ViewerSupport
				.bind(linksTableViewer, model.getLinks(),
						BeanProperties.values(ContainerLinkModel.class,
								new String[] {
										ContainerLinkModel.CONTAINER_NAME,
										ContainerLinkModel.CONTAINER_ALIAS }));
		// disable the edit and removeButton if the table is empty
		linksTableViewer.addSelectionChangedListener(
				onSelectionChanged(editButton, removeButton));

	}

	private ISelectionChangedListener onSelectionChanged(
			final Button... targetButtons) {
		return new ISelectionChangedListener() {

			@Override
			public void selectionChanged(final SelectionChangedEvent e) {
				if (e.getSelection().isEmpty()) {
					setControlsEnabled(targetButtons, false);
				} else {
					setControlsEnabled(targetButtons, true);
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private TableViewer createLinksTable(final Composite container) {
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createTableViewerColumn(tableViewer,
				WizardMessages
						.getString("ImageRunSelectionPage.containerNameColumn"), //$NON-NLS-1$
				200);
		createTableViewerColumn(tableViewer,
				WizardMessages.getString("ImageRunSelectionPage.aliasColumn"), //$NON-NLS-1$
				150);
		tableViewer.setContentProvider(new ObservableListContentProvider());
		return tableViewer;
	}

	private SelectionListener onAddLink() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final ContainerLinkDialog dialog = new ContainerLinkDialog(
						getShell(), model.getSelectedConnection());
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					model.addLink(dialog.getContainerName(),
							dialog.getContainerAlias());
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private SelectionListener onEditLink(final TableViewer linksTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IStructuredSelection selection = linksTableViewer
						.getStructuredSelection();

				final ContainerLinkModel selectedContainerLink = (ContainerLinkModel) selection
						.getFirstElement();
				final ContainerLinkDialog dialog = new ContainerLinkDialog(
						getShell(), model.getSelectedConnection(),
						selectedContainerLink);
				dialog.create();
				if (dialog.open() == IDialogConstants.OK_ID) {
					selectedContainerLink
							.setContainerName(dialog.getContainerName());
					selectedContainerLink
							.setContainerAlias(dialog.getContainerAlias());
					linksTableViewer.refresh();
					updateLaunchConfigurationDialog();
				}
			}
		};
	}

	private SelectionListener onRemoveLinks(
			final TableViewer linksTableViewer) {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final IStructuredSelection selection = linksTableViewer
						.getStructuredSelection();
				for (@SuppressWarnings("unchecked")
				Iterator<ContainerLinkModel> iterator = selection
						.iterator(); iterator.hasNext();) {
					model.removeLink(iterator.next());
				}
				updateLaunchConfigurationDialog();
			}
		};
	}

	private static void setControlsEnabled(final Control[] controls,
			final boolean enabled) {
		for (Control control : controls) {
			control.setEnabled(enabled);
		}
	}

	@Override
	public Image getImage() {
		return SWTImagesFactory.get(SWTImagesFactory.IMG_CONTAINER_LINK);
	}

	@Override
	public void setDefaults(
			final ILaunchConfigurationWorkingCopy configuration) {
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		try {
			// model needs to be recycled
			model.removeLinks();
			final List<String> containerLinks = configuration.getAttribute(
					IRunDockerImageLaunchConfigurationConstants.LINKS,
					new ArrayList<String>());
			for (String containerLink : containerLinks) {
				model.addLink(ImageRunSelectionModel.ContainerLinkModel
						.createContainerLinkModel(containerLink));
			}
		} catch (CoreException e) {
			// do nothing
		}
		// update the underlying launch config working copy on model
		// changes.
		model.addPropertyChangeListener(
				new LaunchConfigurationChangeListener());
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		ArrayList<String> linksList = new ArrayList<>();
		for (Object o : model.getLinks()) {
			ImageRunSelectionModel.ContainerLinkModel m = (ImageRunSelectionModel.ContainerLinkModel) o;
			linksList.add(m.toString());
		}
		configuration.setAttribute(
				IRunDockerImageLaunchConfigurationConstants.LINKS, linksList);
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
