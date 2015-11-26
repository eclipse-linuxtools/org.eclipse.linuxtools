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

package org.eclipse.linuxtools.internal.vagrant.ui.views;

import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.vagrant.core.IVagrantBox;
import org.eclipse.linuxtools.vagrant.core.IVagrantBoxListener;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.linuxtools.vagrant.core.IVagrantVM;
import org.eclipse.linuxtools.vagrant.core.VagrantService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;

public class VagrantBoxView extends ViewPart implements IVagrantBoxListener {

	public static final String VIEW_ID = "org.eclipse.linuxtools.vagrant.ui.vagrantBoxView"; //$NON-NLS-1$

	private final static String DaemonMissing = "ViewerDaemonMissing.msg"; //$NON-NLS-1$

	private Form form;
	private Text search;
	private TableViewer viewer;
	private IVagrantConnection connection;

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		connection.removeBoxListener(this);
		super.dispose();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	@Override
	public void createPartControl(final Composite parent) {
		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createForm(parent);
		form.setText(DVMessages.getString(DaemonMissing));
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
		createTableViewer(container);
		hookContextMenu();
	}

	private void createTableViewer(final Composite container) {
		search = new Text(container, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(search);
		search.addModifyListener(onSearch());
		Composite tableArea = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,  0).applyTo(tableArea);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(tableArea);

		final TableColumnLayout tableLayout = new TableColumnLayout();
		tableArea.setLayout(tableLayout);
		this.viewer = new TableViewer(tableArea, SWT.FULL_SELECTION | SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL);
		this.viewer.setContentProvider(new VagrantBoxContentProvider());
		final Table table = viewer.getTable();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.applyTo(table);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// 'NAME' column
		final TableViewerColumn idColumn = createColumn(DVMessages
				.getString("NAME")); //$NON-NLS-1$
		setLayout(idColumn, tableLayout, 150);
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVagrantBox) {
					return ((IVagrantBox) element).getName();
				}
				return super.getText(element);
			}
		});
		// 'PROVIDER' column
		final TableViewerColumn tagsColumn = createColumn(DVMessages
				.getString("PROVIDER")); //$NON-NLS-1$
		setLayout(tagsColumn, tableLayout, 150);
		tagsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVagrantBox) {
					return ((IVagrantBox) element).getProvider();
				}
				return super.getText(element);
			}
		});
		// 'VERSION' column
		final TableViewerColumn creationDateColumn = createColumn(DVMessages
				.getString("VERSION")); //$NON-NLS-1$
		setLayout(creationDateColumn, tableLayout, 150);
		creationDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IVagrantBox) {
					return ((IVagrantBox) element).getVersion().toString();
				}
				return super.getText(element);
			}
		});
		// comparator
		final VagrantBoxComparator comparator = new VagrantBoxComparator(this.viewer);
		viewer.setComparator(comparator);
		// apply search filter
		this.viewer.addFilter(getImagesFilter());
		setConnection(VagrantService.getInstance());
		connection.addBoxListener(this);
		// get the current selection in the tableviewer
		getSite().setSelectionProvider(viewer);
	}

	private TableViewerColumn createColumn(final String title) {
		final TableViewerColumn propertyColumn = new TableViewerColumn(viewer,
				SWT.BORDER);
		propertyColumn.getColumn().setText(title);
		propertyColumn.getColumn().addSelectionListener(onColumnSelected());
		return propertyColumn;
	}

	private SelectionListener onColumnSelected() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				final TableColumn sortColumn = (TableColumn) e.getSource();
				final VagrantBoxComparator comparator = (VagrantBoxComparator) viewer.getComparator();
				comparator.setColumn(sortColumn);
				viewer.refresh();
			}
		};
	}

	private void setLayout(final TableViewerColumn viewerColumn, final TableColumnLayout tableLayout, final int weight) {
		tableLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(weight, true));
	}

	/**
	 * Filters {@link IVagrantVM} and {@link IVagrantBox} using the input text in the search widget of this view.
	 * @return
	 */
	private ModifyListener onSearch() {
		return e -> VagrantBoxView.this.viewer.refresh();
	}

	/**
	 * @return a {@link ViewerFilter} that will retain {@link IVagrantVM} that match the
	 * content of the {@link VagrantBoxView#search} text widget.
	 */
	private ViewerFilter getImagesFilter() {
		return new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IVagrantBox) {
					return element.toString().contains(VagrantBoxView.this.search.getText());
				}
				return false;
			}
		};
	}

	@Override
	public void listChanged(final IVagrantConnection connection,
			final List<IVagrantBox> images) {
		Display.getDefault().asyncExec(() -> {
			// remember the current selection before the viewer is refreshed
			final ISelection currentSelection = VagrantBoxView.this.viewer
					.getSelection();
			VagrantBoxView.this.viewer.refresh();
			// restore the selection
			if (currentSelection != null) {
				VagrantBoxView.this.viewer.setSelection(currentSelection);
				}
			refreshViewTitle();
			});
	}

	private void refreshViewTitle() {
		if (this.viewer == null || this.viewer.getControl().isDisposed()
				|| this.form == null
				|| this.connection == null) {
			return;
		} else if (!this.connection.isBoxesLoaded()) {
			form.setText(connection.getName());
		} else {
			this.form.setText(DVMessages.getFormattedString(
					"VagrantBoxViewTitle.all.msg", connection.getName(), //$NON-NLS-1$
					Integer.toString(connection.getBoxes().size())));
		}
	}

	/**
	 * @return the {@link IVagrantConnection} used to display the current {@link IVagrantVM}
	 */
	public IVagrantConnection getConnection() {
		return connection;
	}

	public void setConnection(IVagrantConnection conn) {
		this.connection = conn;
		if (conn != null) {
			viewer.setInput(conn);
			refreshViewTitle();
		} else {
			viewer.setInput(new IVagrantBox[0]);
			form.setText(DVMessages.getString(DaemonMissing));
		}
	}

	/**
	 * @return the current selection
	 */
	public ISelection getSelection() {
		if(this.viewer != null) {
			return this.viewer.getSelection();
		}
		return null;
	}

	/**
	 * @return the {@link TableViewer} showing the containers
	 */
	public TableViewer getViewer() {
		return this.viewer;
	}

}
