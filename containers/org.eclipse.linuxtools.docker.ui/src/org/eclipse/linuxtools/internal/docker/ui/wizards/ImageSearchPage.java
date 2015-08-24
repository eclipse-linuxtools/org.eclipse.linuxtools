/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.IDockerImageSearchResult;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * @author xcoulon
 *
 */
public class ImageSearchPage extends WizardPage {

	private final ImageSearchModel model;
	private final DataBindingContext ctx = new DataBindingContext();

	/**
	 * Default constructor.
	 */
	public ImageSearchPage(final ImageSearchModel model) {
		super("ImageSearchPage", //$NON-NLS-1$
				WizardMessages.getString("ImageSearchPage.title"), //$NON-NLS-1$
				SWTImagesFactory.DESC_BANNER_REPOSITORY);
		this.model = model;
	}

	public IDockerImageSearchResult getSelectedImage() {
		return model.getSelectedImage();
	}

	@Override
	public void dispose() {
		ctx.dispose();
		super.dispose();
	}

	@Override
	public void createControl(final Composite parent) {
		final int COLUMNS = 3;
		final Composite container = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).span(1, 1)
				.grab(true, false).applyTo(container);
		GridLayoutFactory.fillDefaults().numColumns(COLUMNS).margins(6, 6)
				.spacing(10, 2).applyTo(container);
		// search text
		final Label searchImageLabel = new Label(container, SWT.NONE);
		searchImageLabel.setText(
				WizardMessages.getString("ImageSearchPage.imageLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchImageLabel);
		final Text searchImageText = new Text(container,
				SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).applyTo(searchImageText);
		searchImageText.addKeyListener(onKeyPressed());
		searchImageText.addTraverseListener(onSearchImageTextTraverse());
		final Button searchImageButton = new Button(container, SWT.NONE);
		searchImageButton
				.setText(WizardMessages.getString("ImageSearchPage.search")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(false, false).applyTo(searchImageButton);
		searchImageButton.addSelectionListener(onSearchImageButtonSelected());
		searchImageButton.setEnabled(!searchImageText.getText().isEmpty());
		// result table
		final Label searchResultLabel = new Label(container, SWT.NONE);
		searchResultLabel.setText(
				WizardMessages.getString("ImageSearchPage.searchResultLabel")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, false).span(COLUMNS, 1).applyTo(searchResultLabel);
		final Table table = new Table(container,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
		final TableViewer searchResultTableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		addTableViewerColum(searchResultTableViewer,
				WizardMessages.getString("ImageSearchPage.nameColumn"), //$NON-NLS-1$
				SWT.NONE,
				SWT.RIGHT, 290, new ImageNameColumnLabelProvider());
		addTableViewerColum(searchResultTableViewer,
				WizardMessages.getString("ImageSearchPage.starsColumn"), //$NON-NLS-1$
				SWT.NONE,
				SWT.RIGHT,
				70, new ImageStarsColumnLabelProvider());
		addTableViewerColum(searchResultTableViewer,
				WizardMessages.getString("ImageSearchPage.officialColumn"), //$NON-NLS-1$
				SWT.NONE,
				SWT.CENTER,
				70, new ImageOfficialColumnLabelProvider());
		addTableViewerColum(searchResultTableViewer,
				WizardMessages.getString("ImageSearchPage.automatedColumn"), //$NON-NLS-1$
				SWT.NONE,
				SWT.CENTER,
				70, new ImageAutomatedColumnLabelProvider());
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).span(COLUMNS, 1).hint(200, 100)
				.applyTo(table);
		// description text area
		final Group selectedImageDescriptionGroup = new Group(container,
				SWT.BORDER);
		selectedImageDescriptionGroup.setText(WizardMessages.getString("ImageSearchPage.descriptionGroup")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, true).span(COLUMNS, 1)
				.applyTo(selectedImageDescriptionGroup);
		GridLayoutFactory.fillDefaults().margins(6, 6).spacing(10, 2)
				.applyTo(selectedImageDescriptionGroup);
		final Label selectedImageDescription = new Label(
				selectedImageDescriptionGroup, SWT.WRAP);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
				.grab(true, true).hint(200, 100)
				.applyTo(selectedImageDescription);
		// bind the search term
		final IObservableValue observableTermModel = BeanProperties
				.value(ImageSearchModel.class, ImageSearchModel.TERM)
				.observe(model);
		final UpdateValueStrategy strategy = new UpdateValueStrategy();
		strategy.setBeforeSetValidator(new SearchTermValidator());

		final ISWTObservableValue imageSearchTextObservable = WidgetProperties
				.text(SWT.Modify).observe(searchImageText);
		ctx.bindValue(imageSearchTextObservable, observableTermModel, strategy,
				null);
		// enable/disable the search button
		imageSearchTextObservable
				.addValueChangeListener(onTermValueChanged(searchImageButton));
		// observe the viewer content
		searchResultTableViewer
				.setContentProvider(new ObservableListContentProvider());
		// observe the viewer content
		final IObservableList observableSearchResultModel = BeanProperties
				.list(ImageSearchModel.class,
						ImageSearchModel.IMAGE_SEARCH_RESULT)
				.observe(model);
		searchResultTableViewer.setInput(observableSearchResultModel);

		// observe the viewer selection
		ctx.bindValue(
				ViewerProperties.singleSelection()
						.observe(searchResultTableViewer),
				BeanProperties.value(ImageSearchModel.SELECTED_IMAGE)
						.observe(model));
		// observe the viewer selection to update the description label
		final IObservableValue observableSelectedImageDescription = PojoProperties
				.value("description", String.class) // $NON-NLS-1$
				.observeDetail(ViewerProperties.singleSelection()
						.observe(searchResultTableViewer));
		ctx.bindValue(WidgetProperties.text().observe(selectedImageDescription),
				observableSelectedImageDescription);
		searchImageText.setFocus();
		// attach the Databinding context status to this wizard page.
		WizardPageSupport.create(this, this.ctx);
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

	private IValueChangeListener onTermValueChanged(
			final Button searchImageButton) {
		return new IValueChangeListener() {

			@Override
			public void handleValueChange(final ValueChangeEvent event) {
				final String searchTerm = (String) event.getObservableValue()
						.getValue();
				final IStatus status = AggregateValidationStatus
						.getStatusMaxSeverity(
								ctx.getValidationStatusProviders());
				if (searchTerm.isEmpty() || !status.isOK()) {
					searchImageButton.setEnabled(false);
				} else {
					searchImageButton.setEnabled(true);
				}
			}
		};
	}

	private TraverseListener onSearchImageTextTraverse() {
		return new TraverseListener() {

			@Override
			public void keyTraversed(final TraverseEvent e) {
				if (e.keyCode == SWT.CR) {
					e.doit = false;
				}
			}
		};
	}

	private KeyListener onKeyPressed() {
		return new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent event) {
				final IStatus status = AggregateValidationStatus
						.getStatusMaxSeverity(
								ctx.getValidationStatusProviders());
				final String searchTerm = ImageSearchPage.this.model.getTerm();
				if (event.character == SWT.CR && !searchTerm.isEmpty()
						&& status.isOK()) {
					searchImages();
				}
			}
		};
	}

	private SelectionListener onSearchImageButtonSelected() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent event) {
				searchImages();
			}
		};
	}

	private void searchImages() {
		final String term = this.model.getTerm();
		if (term.isEmpty()) {
			return;
		}
		try {
			final BlockingQueue<List<IDockerImageSearchResult>> searchResultQueue = new ArrayBlockingQueue<>(
					1);
			ImageSearchPage.this.getContainer().run(true, true,
					new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) {
							monitor.beginTask(WizardMessages.getString(
									"ImageSearchPage.searchTask"), 1); //$NON-NLS-1$
							try {
								final List<IDockerImageSearchResult> searchResults = ImageSearchPage.this.model
										.getSelectedConnection()
										.searchImages(term);
								searchResultQueue.offer(searchResults);
							} catch (DockerException e) {
								Activator.log(e);
								searchResultQueue.offer(
										new ArrayList<IDockerImageSearchResult>());
							}
							monitor.done();
						}
					});
			List<IDockerImageSearchResult> res = searchResultQueue
					.poll(10, TimeUnit.SECONDS);
			final List<IDockerImageSearchResult> searchResult = (res == null)
					? new ArrayList<IDockerImageSearchResult>() : res;

			Display.getCurrent().asyncExec(new Runnable() {
				@Override
				public void run() {
					ImageSearchPage.this.model
							.setImageSearchResult(searchResult);
					// refresh the wizard buttons
					getWizard().getContainer().updateButtons();
				}
			});
			// display a warning in the title area if the search result is empty
			if (searchResult.isEmpty()) {
				this.setMessage(
						WizardMessages
								.getString("ImageSearchPage.noImageWarning"), //$NON-NLS-1$
						WARNING);
			} else if (searchResult.size() == 1) {
				this.setMessage(
						WizardMessages
								.getString("ImageSearchPage.oneImageMatched"), //$NON-NLS-1$
						INFORMATION);
			} else {
				this.setMessage(
						WizardMessages.getFormattedString(
								"ImageSearchPage.imagesMatched", //$NON-NLS-1$
								Integer.toString(searchResult.size())),
						INFORMATION);
			}
		} catch (InvocationTargetException | InterruptedException e) {
			Activator.log(e);
		}
	}

	@Override
	public boolean isPageComplete() {
		return this.model.getSelectedImage() != null;
	}

	static class SearchTermValidator implements IValidator {

		private static final Pattern termPattern = Pattern
				.compile("[a-z0-9]+([._-][a-z0-9]+)*"); //$NON-NLS-1$

		@Override
		public IStatus validate(final Object value) {
			final String term = (String) value;
			if (term == null || term.isEmpty()) {
				return ValidationStatus.info(WizardMessages
						.getString("ImageSearchPage.description")); //$NON-NLS-1$
			} else if (termPattern.matcher(term).matches()) {
				return Status.OK_STATUS;
			} else {
				return ValidationStatus.error(WizardMessages.getFormattedString(
						"ImageSearchPage.term.invalidformat", //$NON-NLS-1$
						termPattern.pattern()));
			}
		}

	}

	static class ImageNameColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof IDockerImageSearchResult) {
				return ((IDockerImageSearchResult) element).getName();
			}
			return super.getText(element);
		}
	}

	static class ImageStarsColumnLabelProvider extends ColumnLabelProvider {

		@Override
		public String getText(final Object element) {
			if (element instanceof IDockerImageSearchResult) {
				return Integer.toString(
						((IDockerImageSearchResult) element).getStarCount());
			}
			return super.getText(element);
		}
	}

	static class ImageOfficialColumnLabelProvider
			extends IconColumnLabelProvider {

		@Override
		boolean doPaint(final Object element) {
			return element instanceof IDockerImageSearchResult
					&& ((IDockerImageSearchResult) element).isOfficial();
		}

	}

	static class ImageAutomatedColumnLabelProvider
			extends IconColumnLabelProvider {

		@Override
		boolean doPaint(final Object element) {
			return element instanceof IDockerImageSearchResult
					&& ((IDockerImageSearchResult) element).isAutomated();
		}

	}

	static abstract class IconColumnLabelProvider
			extends StyledCellLabelProvider {

		private static final Image ICON = SWTImagesFactory.DESC_RESOLVED
				.createImage();

		@Override
		protected void measure(Event event, Object element) {
			Rectangle rectangle = ICON.getBounds();
			event.setBounds(new Rectangle(event.x, event.y,
					rectangle.width + 200, rectangle.height));

		}

		@Override
		protected void paint(final Event event, final Object element) {
			final TableColumn column = ((TableViewerColumn) getColumn())
					.getColumn();
			final int columnWidth = column.getWidth();
			if (doPaint(element)) {
				final Rectangle bounds = event.getBounds();
				event.gc.drawImage(ICON,
						bounds.x + (columnWidth - ICON.getBounds().width) / 2,
						bounds.y + 1);
			}
		}

		abstract boolean doPaint(final Object element);
	}

}
