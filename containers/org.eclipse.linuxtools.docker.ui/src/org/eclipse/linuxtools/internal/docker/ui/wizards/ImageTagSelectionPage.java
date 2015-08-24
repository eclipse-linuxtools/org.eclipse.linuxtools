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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IRepositoryTag;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerHubRegistry;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.wizards.ImageSearchPage.IconColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * {@link WizardPage} to select an existing tag for a given image (repository)
 * on a remote registry. This page performs a call to the registry when it is
 * activated.
 */
public class ImageTagSelectionPage extends WizardPage {

	private final ImageSearchModel model;
	private final DataBindingContext ctx = new DataBindingContext();

	/**
	 * Default constructor.
	 */
	public ImageTagSelectionPage(final ImageSearchModel model) {
		super("ImageTagSelectionPage", //$NON-NLS-1$
				WizardMessages.getString("ImageTagSelectionPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		setMessage(WizardMessages.getString("ImageTagSelectionPage.title")); //$NON-NLS-1$
		this.model = model;
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			searchTags();
		}
	}

	private void searchTags() {
		try {
			final BlockingQueue<List<DockerImageTagSearchResult>> searchResultQueue = new ArrayBlockingQueue<>(
					1);
			ImageTagSelectionPage.this.getContainer().run(true, true,
					new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) {
							try {
								monitor.beginTask(WizardMessages.getString(
										"ImageTagSelectionPage.searchTask"), //$NON-NLS-1$
										2);
								final String selectedImageName = ImageTagSelectionPage.this.model
										.getSelectedImage()
										.getName();
								final List<IRepositoryTag> repositoryTags = new DockerHubRegistry()
										.getTags(
												selectedImageName);
								monitor.worked(1);
								final List<DockerImageTagSearchResult> searchResults = new ArrayList<>();
								final IDockerConnection connection = model.getSelectedConnection();
								for (IRepositoryTag repositoryTag : repositoryTags) {
									searchResults
											.add(new DockerImageTagSearchResult(
													selectedImageName,
													repositoryTag,
													connection.hasImage(
															selectedImageName,
															repositoryTag
																	.getName())));
								}
								monitor.worked(1);
								searchResultQueue.offer(searchResults);
							} catch (DockerException | InterruptedException
									| ExecutionException e) {
								Activator.log(e);
							}
							monitor.done();
						}
					});
			List<DockerImageTagSearchResult> res = searchResultQueue.poll(10,
					TimeUnit.SECONDS);
			final List<DockerImageTagSearchResult> searchResult = (res == null)
					? new ArrayList<DockerImageTagSearchResult>() : res;
			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					ImageTagSelectionPage.this.model
							.setImageTagSearchResult(searchResult);
					// refresh the wizard buttons
					getWizard().getContainer().updateButtons();
				}
			});
			// display a warning in the title area if the search result is empty
			if (searchResult.isEmpty()) {
				this.setMessage(
						WizardMessages.getString(
								"ImageTagSelectionPage.noTagWarning"), //$NON-NLS-1$
						WARNING);
			} else if (searchResult.size() == 1) {
				this.setMessage(
						WizardMessages.getString(
								"ImageTagSelectionPage.oneTagMatched"), //$NON-NLS-1$
						INFORMATION);
			} else {
				this.setMessage(
						WizardMessages.getFormattedString(
								"ImageTagSelectionPage.tagsMatched", //$NON-NLS-1$
								Integer.toString(searchResult.size())),
						INFORMATION);
			}
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	public DockerImageTagSearchResult getSelectedImageTag() {
		return model.getSelectedImageTag();
	}

	@Override
	public boolean isPageComplete() {
		return this.model.getSelectedImageTag() != null;
	}

	@Override
	public void dispose() {
		ctx.dispose();
		super.dispose();
	}

	@Override
	public void createControl(final Composite parent) {
		final int COLUMNS = 1;
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(container);

		// tags/layers table
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).span(COLUMNS, 1).hint(200, 200)
				.applyTo(table);
		final TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableViewerColum(tableViewer,
				WizardMessages.getString("ImageTagSelectionPage.column.tag"), //$NON-NLS-1$
				SWT.NONE, SWT.LEFT, 200,
				new RepositoryTagColumnLabelProvider());
		addTableViewerColum(tableViewer,
				WizardMessages.getString("ImageTagSelectionPage.column.layer"), //$NON-NLS-1$
				SWT.NONE, SWT.LEFT, 200,
				new RepositoryLayerColumnLabelProvider());
		addTableViewerColum(tableViewer,
				WizardMessages
						.getString("ImageTagSelectionPage.column.localcopy"), //$NON-NLS-1$
				SWT.NONE, SWT.LEFT, 75, new ImagePulledColumnLabelProvider());
		tableViewer.setContentProvider(new ObservableListContentProvider());
		// observe the viewer content
		final IObservableList observableSearchResultModel = BeanProperties
				.list(ImageSearchModel.class,
						ImageSearchModel.IMAGE_TAG_SEARCH_RESULT)
				.observe(model);
		tableViewer.setInput(observableSearchResultModel);
		// observe the viewer selection
		ctx.bindValue(ViewerProperties.singleSelection().observe(tableViewer),
				BeanProperties.value(ImageSearchModel.SELECTED_IMAGE_TAG)
						.observe(model));
		setControl(container);
	}

	private TableViewerColumn addTableViewerColum(final TableViewer tableViewer,
			final String title, final int style, final int alignment,
			final int width, final CellLabelProvider columnLabelProvider) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(
				tableViewer, style);
		final TableColumn column = viewerColumn.getColumn();
		if (title != null) {
			column.setText(title);
		}
		column.setAlignment(alignment);
		column.setWidth(width);
		viewerColumn.setLabelProvider(columnLabelProvider);
		return viewerColumn;
	}

	static class RepositoryTagColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof DockerImageTagSearchResult) {
				return ((DockerImageTagSearchResult) element).getName();
			}
			return super.getText(element);
		}
	}

	static class RepositoryLayerColumnLabelProvider
			extends ColumnLabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof DockerImageTagSearchResult) {
				return ((DockerImageTagSearchResult) element).getLayer();
			}
			return super.getText(element);
		}
	}

	static class ImagePulledColumnLabelProvider
			extends IconColumnLabelProvider {

		@Override
		boolean doPaint(final Object element) {
			return element instanceof DockerImageTagSearchResult
					&& ((DockerImageTagSearchResult) element).isResolved();
		}
	}

}
