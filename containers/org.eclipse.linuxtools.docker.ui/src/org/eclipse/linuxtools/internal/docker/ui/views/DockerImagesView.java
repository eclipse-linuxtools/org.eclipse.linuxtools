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

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author jjohnstn
 *
 */
public class DockerImagesView extends ViewPart implements IDockerImageListener,
		ISelectionListener, IDockerConnectionManagerListener,
		ITabbedPropertySheetPageContributor {

	public static final String VIEW_ID = "org.eclipse.linuxtools.docker.ui.dockerImagesView";

	private static final String TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String SHOW_ALL_IMAGES_COMMAND_ID = "org.eclipse.linuxtools.docker.ui.commands.showAllImages"; //$NON-NLS-1$
	private static final String SHOW_ALL_IMAGES_PREFERENCE = "showAllImages"; //$NON-NLS-1$

	private final static String DaemonMissing = "ViewerDaemonMissing.msg"; //$NON-NLS-1$
	private final static String ViewAllTitle = "ImagesViewTitle.all.msg"; //$NON-NLS-1$
	private final static String ViewFilteredTitle = "ImagesViewTitle.filtered.msg"; //$NON-NLS-1$

	private Form form;
	private Text search;
	private TableViewer viewer;
	private IDockerConnection connection;
	private final DanglingImagesViewerFilter hideDanglingImagesFilter = new DanglingImagesViewerFilter();
	private final IntermediateImagesViewerFilter hideIntermediateImagesFilter = new IntermediateImagesViewerFilter();

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		// stop tracking selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(DockerExplorerView.VIEW_ID, this);
		DockerConnectionManager.getInstance().removeConnectionManagerListener(
				this);
		super.dispose();
	}

	@Override
	public String getContributorId() {
		return DockerExplorerView.VIEW_ID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IPropertySheetPage.class) {
			return (T) new TabbedPropertySheetPage(this, true);
		}
		return super.getAdapter(adapter);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		// menuMgr.addMenuListener(new IMenuListener() {
		// public void menuAboutToShow(IMenuManager manager) {
		// DockerContainersView.this.fillContextMenu(manager);
		// }
		// });
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
		// track selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(DockerExplorerView.VIEW_ID, this);
		DockerConnectionManager.getInstance()
				.addConnectionManagerListener(this);
		hookContextMenu();
		// Look at stored preference to determine if all images should be
		// shown or just top-level images. By default, only show
		// top-level images.
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		boolean showAll = preferences.getBoolean(SHOW_ALL_IMAGES_PREFERENCE,
				false);
		showAllImages(showAll);
		final ICommandService service = getViewSite().getWorkbenchWindow()
				.getService(ICommandService.class);
		service.getCommand(SHOW_ALL_IMAGES_COMMAND_ID).getState(TOGGLE_STATE)
				.setValue(showAll);
		service.refreshElements(SHOW_ALL_IMAGES_COMMAND_ID, null);

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
		this.viewer.setContentProvider(new DockerImagesContentProvider());
		final Table table = viewer.getTable();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0)
				.applyTo(table);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// 'Image' column
		final TableViewerColumn idColumn = createColumn(DVMessages
				.getString("ID")); //$NON-NLS-1$
		setLayout(idColumn, tableLayout, 150);
		idColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerImage) {
					return ((IDockerImage) element).id();
				}
				return super.getText(element);
			}
		});
		// 'Tags' column
		final TableViewerColumn tagsColumn = createColumn(DVMessages
				.getString("TAGS")); //$NON-NLS-1$
		setLayout(tagsColumn, tableLayout, 150);
		tagsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerImage) {
					final StringBuilder tags = new StringBuilder();
					List<String> repoTags = new ArrayList<>();
					repoTags.addAll(((IDockerImage) element).repoTags());
					Collections.sort(repoTags);
					for (Iterator<String> iterator = repoTags.iterator(); iterator
							.hasNext();) {
						final String tag = iterator.next();
						tags.append(tag);
						if (iterator.hasNext()) {
							tags.append(System.getProperty("line.separator")); //$NON-NLS-1$
						}
					}
					return tags.toString();
				}
				return super.getText(element);
			}
		});
		// 'Creation Date' column
		final TableViewerColumn creationDateColumn = createColumn(DVMessages
				.getString("CREATED")); //$NON-NLS-1$
		setLayout(creationDateColumn, tableLayout, 150);
		creationDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerImage) {
					return LabelUtils.toCreatedDate(Long
							.parseLong(((IDockerImage) element).created()));
				}
				return super.getText(element);
			}
		});
		// 'Virtual Size' column
		final TableViewerColumn virtsizeColumn = createColumn(DVMessages
				.getString("VIRTSIZE")); //$NON-NLS-1$ 
		setLayout(virtsizeColumn, tableLayout, 150);
		virtsizeColumn.setLabelProvider(new SpecialColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerImage) {
					Long size = ((IDockerImage) element).virtualSize();
					if (size <= 0)
						return "0"; //$NON-NLS-1$
					final String[] units = new String[] { "B", "kB", "MB", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"GB", "TB" }; //$NON-NLS-1$ //$NON-NLS-2$
					int digitGroups = (int) (Math.log10(size) / Math
							.log10(1000));
					return new DecimalFormat("#,##0.#").format(size //$NON-NLS-1$
							/ Math.pow(1000, digitGroups))
							+ " " + units[digitGroups];
				}
				return super.getText(element);
			}

			@Override
			public String getCompareText(final Object element) {
				// For comparison purposes, we want to use the raw long value
				// and not the shortened value with units appended.
				if (element instanceof IDockerImage) {
					return new DecimalFormat("000000000000000000000000") //$NON-NLS-1$
							.format((((IDockerImage) element).virtualSize()));
				}
				return super.getText(element);
			}
		});
		// comparator
		final DockerImagesComparator comparator = new DockerImagesComparator(
				this.viewer);
		comparator.setColumn(creationDateColumn.getColumn());
		// Set column a second time so we reverse the order and default to most
		// currently created containers first
		comparator.setColumn(creationDateColumn.getColumn());
		viewer.setComparator(comparator);
		// apply search filter
		this.viewer.addFilter(getImagesFilter());

		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		if (connections.length > 0) {
			setConnection(connections[0]);
			connection.addImageListener(this);
		}
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
				final DockerImagesComparator comparator = (DockerImagesComparator) viewer
						.getComparator();
				comparator.setColumn(sortColumn);
				viewer.refresh();
			}
		};
	}

	private void setLayout(final TableViewerColumn viewerColumn, final TableColumnLayout tableLayout, final int weight) {
		tableLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(weight, true));
	}

	/**
	 * Filters {@link IDockerContainer} and {@link IDockerImage} using the input text in the search widget of this view.
	 * @return
	 */
	private ModifyListener onSearch() {
		return new ModifyListener() {
			
			@Override
			public void modifyText(final ModifyEvent e) {
				DockerImagesView.this.viewer.refresh();
			}
		};
	}
	
	/**
	 * @return a {@link ViewerFilter} that will retain {@link IDockerContainer} that match the 
	 * content of the {@link DockerContainerView#search} text widget.
	 */
	private ViewerFilter getImagesFilter() {
		return new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				// filtering Docker images
				if (element instanceof IDockerImage) {
					return element.toString().contains(DockerImagesView.this.search.getText());
				}
				// any other element should not make it through the filter (i.e., it is not displayed)
				return false;
			}
		};
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final ITreeSelection treeSelection = (ITreeSelection) selection;
		if(treeSelection.isEmpty()) {
			return;
		}
		// remove this view as a container listener on the former select connection 
		if(this.connection != null) {
			this.connection.removeImageListener(this);
		}
		final Object firstSegment = treeSelection.getPaths()[0].getFirstSegment();
		if(firstSegment instanceof IDockerConnection) {
			setConnection((IDockerConnection) firstSegment);
			this.connection.addImageListener(this);
		}
	}
	
	@Override
	public void listChanged(final IDockerConnection connection,
			final List<IDockerImage> containers) {
		if (connection.getName().equals(connection.getName())) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// remember the current selection before the viewer is
					// refreshed
					final ISelection currentSelection = DockerImagesView.this.viewer.getSelection();
					DockerImagesView.this.viewer.refresh();
					// restore the selection
					if (currentSelection != null) {
						DockerImagesView.this.viewer
								.setSelection(currentSelection);
					}
					refreshViewTitle();
				}
			});
		}
	}
	
	private void refreshViewTitle() {
		if (this.viewer == null || this.viewer.getControl().isDisposed()
				|| this.form == null
				|| this.connection == null) {
			return;
		} else if (!this.connection.isImagesLoaded()) {
			form.setText(connection.getName());
		} else {
			final List<ViewerFilter> filters = Arrays
					.asList(this.viewer.getFilters());
			if (filters.contains(hideDanglingImagesFilter)
					|| filters.contains(hideIntermediateImagesFilter)) {
				this.form.setText(DVMessages.getFormattedString(
						ViewFilteredTitle, connection.getName(),
						Integer.toString(viewer.getTable().getItemCount()),
						Integer.toString(connection.getImages().size())));
			} else {
				this.form.setText(DVMessages.getFormattedString(ViewAllTitle,
						new String[] { connection.getName(), Integer
								.toString(connection.getImages().size()) }));

			}
		}
	}
	
	/**
	 * @return the {@link IDockerConnection} used to display the current {@link IDockerContainer}
	 */
	public IDockerConnection getConnection() {
		return connection;
	}

	public void setConnection(IDockerConnection conn) {
		this.connection = conn;
		if (conn != null) {
			viewer.setInput(conn);
			form.setText(conn.getName());
		} else {
			viewer.setInput(new IDockerImage[0]);
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

	/**
	 * Activates {@link HideDanglingImagesFilter} and
	 * {@link HideIntermediateImagesFilter} if the given {@code enabled}
	 * argument is <code>false</code>, deactivates the filter otherwise.
	 * 
	 * @param enabled
	 *            the argument to enable/disable the filter.
	 */
	public void showAllImages(boolean enabled) {
		if(!enabled) {
			this.viewer.addFilter(hideDanglingImagesFilter);
			this.viewer.addFilter(hideIntermediateImagesFilter);
		} else {
			final List<ViewerFilter> filters = new ArrayList<>(
					Arrays.asList(this.viewer.getFilters()));
			// remove filters and make sure there is no duplicate in the list of
			// filters
			for (Iterator<ViewerFilter> iterator = filters.iterator(); iterator
					.hasNext();) {
				ViewerFilter viewerFilter = iterator.next();
				if (viewerFilter.equals(hideDanglingImagesFilter)
						|| viewerFilter.equals(hideIntermediateImagesFilter)) {
					iterator.remove();
				}
			}
			this.viewer.setFilters(filters.toArray(new ViewerFilter[0]));
		}
		// Save enablement across sessions using a preference variable.
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		preferences.putBoolean(SHOW_ALL_IMAGES_PREFERENCE, enabled);
		refreshViewTitle();
	}

	@Override
	public void changeEvent(int type) {
		String currUri = null;
		int currIndex = 0;
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		if (connection != null) {
			currUri = connection.getUri();
		}
		int index = 0;
		for (int i = 0; i < connections.length; ++i) {
			if (connections[i].getUri().equals(currUri))
				index = i;
		}
		if (type == IDockerConnectionManagerListener.RENAME_EVENT) {
			index = currIndex; // no change in connection displayed
		}
		if (connections.length > 0
				&& type != IDockerConnectionManagerListener.REMOVE_EVENT) {
			setConnection(connections[index]);
		} else {
			setConnection(null);
		}
	}
	
}
