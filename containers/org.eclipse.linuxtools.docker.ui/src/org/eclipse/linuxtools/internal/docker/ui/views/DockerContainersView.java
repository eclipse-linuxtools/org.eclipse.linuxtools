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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
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
 * @author xcoulon
 *
 */
public class DockerContainersView extends ViewPart implements
		IDockerContainerListener, ISelectionListener,
		ITabbedPropertySheetPageContributor {

	private static final String TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String SHOW_ALL_CONTAINERS_COMMAND_ID = "org.eclipse.linuxtools.docker.ui.commands.showAllContainers"; //$NON-NLS-1$
	private static final String SHOW_ALL_CONTAINERS_PREFERENCE = "showAllContainers"; //$NON-NLS-1$

	public static final String VIEW_ID = "org.eclipse.linuxtools.docker.ui.dockerContainersView";

	private final static String DaemonMissing = "ViewerDaemonMissing.msg"; //$NON-NLS-1$
	private final static String ViewAllTitle = "ContainersViewTitle.all.msg"; //$NON-NLS-1$
	private final static String ViewFilteredTitle = "ContainersViewTitle.filtered.msg"; //$NON-NLS-1$

	private Form form;
	private Text search;
	private TableViewer viewer;
	private IDockerConnection connection;
	private final HideStoppedContainersViewerFilter hideStoppedContainersViewerFilter = new HideStoppedContainersViewerFilter();

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		// stop tracking selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(DockerExplorerView.VIEW_ID, this);
		super.dispose();
	}

	@Override
	public String getContributorId() {
		return DockerExplorerView.VIEW_ID;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAdapter(final Class<T> adapter) {
		if (adapter == IPropertySheetPage.class) {
			return (T) new TabbedPropertySheetPage(this, true);
		}
		return super.getAdapter(adapter);
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
		getSite().registerContextMenu(new MenuManager(), null);
		// track selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(DockerExplorerView.VIEW_ID, this);
		hookContextMenu();

		// Look at stored preference to determine if all containers should be
		// shown or just running/paused containers. By default, only show
		// running/paused containers
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		boolean showAll = preferences.getBoolean(SHOW_ALL_CONTAINERS_PREFERENCE,
				false);
		showAllContainers(showAll);
		final ICommandService service = getViewSite().getWorkbenchWindow()
				.getService(ICommandService.class);
		service.getCommand(SHOW_ALL_CONTAINERS_COMMAND_ID)
				.getState(TOGGLE_STATE).setValue(showAll);
		service.refreshElements(SHOW_ALL_CONTAINERS_COMMAND_ID, null);
	}
	
	private void createTableViewer(final Composite container) {
		search = new Text(container, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(search);
		search.addModifyListener(onSearch());
		Composite tableArea = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,  0).applyTo(tableArea);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tableArea);
		
		final TableColumnLayout tableLayout = new TableColumnLayout();
		tableArea.setLayout(tableLayout);
		this.viewer = new TableViewer(tableArea, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.viewer.setContentProvider(new DockerContainersContentProvider());
		final Table table = viewer.getTable();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,  0).applyTo(table);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(table);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		// 'Name' column
		final TableViewerColumn nameColumn = createColumn(DVMessages.getString("NAME")); //$NON-NLS-1$
		setLayout(nameColumn, tableLayout, 150);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					return ((IDockerContainer)element).name();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IDockerContainer) {
					final IDockerContainer container = (IDockerContainer) element;
					final EnumDockerStatus containerStatus = EnumDockerStatus
							.fromStatusMessage(container.status());
					if (containerStatus == EnumDockerStatus.RUNNING) {
						return SWTImagesFactory.DESC_CONTAINER_STARTED
								.createImage();
					} else if (containerStatus == EnumDockerStatus.PAUSED) {
						return SWTImagesFactory.DESC_CONTAINER_PAUSED
								.createImage();
					} else {
						return SWTImagesFactory.DESC_CONTAINER_STOPPED
								.createImage();
					}
				}
				return super.getImage(element);
			}
		});
		// 'Image' column
		final TableViewerColumn imageColumn = createColumn(DVMessages.getString("IMAGE")); //$NON-NLS-1$
		setLayout(imageColumn, tableLayout, 150);
		imageColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					return ((IDockerContainer)element).image();
				}
				return super.getText(element);
			}
		});
		// 'Creation Date' column
		final TableViewerColumn creationDateColumn = createColumn(DVMessages.getString("CREATED")); //$NON-NLS-1$
		setLayout(creationDateColumn, tableLayout, 150);
		creationDateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					return LabelUtils.toCreatedDate(((IDockerContainer)element).created());
				}
				return super.getText(element);
			}
		});
		// 'Command' column
		final TableViewerColumn commandColumn = createColumn(DVMessages.getString("COMMAND")); //$NON-NLS-1$
		setLayout(commandColumn, tableLayout, 150);
		commandColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					return ((IDockerContainer)element).command();
				}
				return super.getText(element);
			}
		});
		// 'Ports' column
		final TableViewerColumn portsColumn = createColumn(DVMessages.getString("PORTS")); //$NON-NLS-1$
		setLayout(portsColumn, tableLayout, 150);
		portsColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					final StringBuilder ports = new StringBuilder();
					for (Iterator<IDockerPortMapping> iterator = ((IDockerContainer) element)
							.ports().iterator(); iterator.hasNext();) {
						final IDockerPortMapping portMapping = iterator.next();
						ports.append(LabelUtils.containerPortMappingToString(portMapping));
						if(iterator.hasNext()) {
							ports.append(", ");
						}
					}
					return ports.toString();
				}
				return super.getText(element);
			}
		});
		// 'Status' column
		final TableViewerColumn statusColumn = createColumn(DVMessages.getString("STATUS")); //$NON-NLS-1$ 
		setLayout(statusColumn, tableLayout, 150);
		statusColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(final Object element) {
				if (element instanceof IDockerContainer) {
					return ((IDockerContainer)element).status();
				}
				return super.getText(element);
			}
		});
		// comparator
		final DockerContainersComparator comparator = new DockerContainersComparator(this.viewer);
		comparator.setColumn(creationDateColumn.getColumn());
		// Set column a second time so we reverse the order and default to most
		// currently created containers first
		comparator.setColumn(creationDateColumn.getColumn());
		viewer.setComparator(comparator);
		// apply search filter
		this.viewer.addFilter(getContainersFilter());
		IDockerConnection[] connections = DockerConnectionManager.getInstance()
				.getConnections();
		if (connections.length > 0) {
			setConnection(connections[0]);
			connection.addContainerListener(this);
		}
		// get the current selection in the tableviewer
		getSite().setSelectionProvider(viewer);
	}

	private TableViewerColumn createColumn(final String title) {
		final TableViewerColumn propertyColumn = new TableViewerColumn(viewer, SWT.BORDER);
		propertyColumn.getColumn().setText(title);
		propertyColumn.getColumn().addSelectionListener(onColumnSelected());
		return propertyColumn;
	}
	
	private SelectionListener onColumnSelected() {
		return new SelectionAdapter() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				final TableColumn sortColumn = (TableColumn) e.getSource();
				final DockerContainersComparator comparator = (DockerContainersComparator) viewer
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
				DockerContainersView.this.viewer.refresh();
			}
		};
	}
	
	/**
	 * @return a {@link ViewerFilter} that will retain {@link IDockerContainer} that match the 
	 * content of the {@link DockerContainerView#search} text widget.
	 */
	private ViewerFilter getContainersFilter() {
		return new ViewerFilter() {
			
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				// filtering Docker containers
				if(element instanceof IDockerContainer) {
					return element.toString().contains(DockerContainersView.this.search.getText());
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
			this.connection.removeContainerListener(this);
		}
		final Object firstSegment = treeSelection.getPaths()[0].getFirstSegment();
		if(firstSegment instanceof IDockerConnection) {
			setConnection((IDockerConnection) firstSegment);
			this.connection.addContainerListener(this);
		}
	}
	
	@Override
	public void listChanged(final IDockerConnection connection,
			final List<IDockerContainer> containers) {
		if (connection.getName().equals(connection.getName())) {
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					// remember the current selection before the viewer is
					// refreshed
					final ISelection currentSelection = DockerContainersView.this.viewer.getSelection();
					DockerContainersView.this.viewer.refresh();
					// restore the selection
					if (currentSelection != null) {
						DockerContainersView.this.viewer
								.setSelection(currentSelection);
					}
					refreshViewTitle();
				}
			});
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
			refreshViewTitle();
		} else {
			viewer.setInput(new IDockerContainer[0]);
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
	 * Activates {@link HideStoppedContainersViewerFilter} if the given {@code enabled} argument is <code>false</code>, deactivates the filter otherwise. 
	 * @param enabled the argument to enable/disable the filter.
	 */
	public void showAllContainers(boolean enabled) {
		if(!enabled) {
			this.viewer.addFilter(hideStoppedContainersViewerFilter);

		} else {
			final List<ViewerFilter> filters = new ArrayList<>(
					Arrays.asList(this.viewer.getFilters()));

			// remove filters and make sure there is no duplicate in the list of
			// filters
			for (Iterator<ViewerFilter> iterator = filters.iterator(); iterator
					.hasNext();) {
				ViewerFilter viewerFilter = iterator.next();
				if (viewerFilter.equals(hideStoppedContainersViewerFilter)) {
					iterator.remove();
				}
			}
			this.viewer.setFilters(filters.toArray(new ViewerFilter[0]));
		}
		// Save enablement across sessions using a preference variable.
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		preferences.putBoolean(SHOW_ALL_CONTAINERS_PREFERENCE, enabled);
		refreshViewTitle();
	}

	private void refreshViewTitle() {
		if (this.viewer == null || this.viewer.getControl().isDisposed()
				|| this.form == null
				|| this.connection == null) {
			return;
		} else if (!this.connection.isContainersLoaded()) {
			form.setText(connection.getName());
		} else {
			final List<ViewerFilter> filters = Arrays
					.asList(this.viewer.getFilters());
			if (filters.contains(hideStoppedContainersViewerFilter)) {
				this.form.setText(DVMessages.getFormattedString(
						ViewFilteredTitle, connection.getName(),
						Integer.toString(viewer.getTable().getItemCount()),
						Integer.toString(connection.getContainers().size())));
			} else {
				this.form.setText(DVMessages.getFormattedString(ViewAllTitle,
						new String[] { connection.getName(), Integer.toString(
								connection.getContainers().size()) }));
			}
		}
	}

}
