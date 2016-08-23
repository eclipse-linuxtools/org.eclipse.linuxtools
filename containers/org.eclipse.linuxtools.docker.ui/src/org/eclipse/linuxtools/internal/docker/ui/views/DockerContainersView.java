/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc. and others.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.DockerException;
import org.eclipse.linuxtools.docker.core.EnumDockerConnectionState;
import org.eclipse.linuxtools.docker.core.EnumDockerStatus;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener2;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.SWTImagesFactory;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.propertytesters.ContainerPropertyTester;
import org.eclipse.swt.SWT;
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
		ITabbedPropertySheetPageContributor, IDockerConnectionManagerListener2 {

	private static final String TOGGLE_STATE = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private static final String SHOW_ALL_CONTAINERS_COMMAND_ID = "org.eclipse.linuxtools.docker.ui.commands.showAllContainers"; //$NON-NLS-1$
	private static final String SHOW_ALL_CONTAINERS_PREFERENCE = "showAllContainers"; //$NON-NLS-1$
	private static final String FILTER_CONTAINERS_COMMAND_ID = "org.eclipse.linuxtools.docker.ui.commands.filterContainersWithLabels"; //$NON-NLS-1$
	private static final String FILTER_WITH_LABELS_PREFERENCE = "filterWithLabels"; //$NON-NLS-1$
	private static final String CONTAINER_FILTER_LABELS = "containerFilterLabels"; //$NON-NLS-1$

	/** Id of the view. */
	public static final String VIEW_ID = "org.eclipse.linuxtools.docker.ui.dockerContainersView";

	private final static String NoConnectionSelected = "ViewerNoConnectionSelected.msg"; //$NON-NLS-1$
	private final static String ConnectionNotAvailable = "ViewerConnectionNotAvailable.msg"; //$NON-NLS-1$
	private final static String ViewAllTitle = "ContainersViewTitle.all.msg"; //$NON-NLS-1$
	private final static String ViewFilteredTitle = "ContainersViewTitle.filtered.msg"; //$NON-NLS-1$

	private Form form;
	private Text search;
	private TableViewer viewer;
	private IDockerConnection connection;
	private final HideStoppedContainersViewerFilter hideStoppedContainersViewerFilter = new HideStoppedContainersViewerFilter();
	private final ContainersWithLabelsViewerFilter containersWithLabelsViewerFilter = new ContainersWithLabelsViewerFilter();

	private final Image STARTED_IMAGE = SWTImagesFactory.DESC_CONTAINER_STARTED
			.createImage();
	private final Image PAUSED_IMAGE = SWTImagesFactory.DESC_CONTAINER_PAUSED
			.createImage();
	private final Image STOPPED_IMAGE = SWTImagesFactory.DESC_CONTAINER_STOPPED
			.createImage();

	private IAction pauseAction, unpauseAction, startAction, stopAction, killAction, removeAction;

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		// remove this listener instance registered on the Docker connection
		if (connection != null) {
			connection.removeContainerListener(this);
		}
		// stop tracking selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(DockerExplorerView.VIEW_ID, this);
		DockerConnectionManager.getInstance()
				.removeConnectionManagerListener(this);
		STARTED_IMAGE.dispose();
		PAUSED_IMAGE.dispose();
		STOPPED_IMAGE.dispose();
		super.dispose();
	}

	@Override
	public String getContributorId() {
		return DockerExplorerView.VIEW_ID;
	}

	/**
	 * @return the title of the form inside the view
	 */
	public String getFormTitle() {
		return this.form.getText();
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
		form.setText(DVMessages.getString(NoConnectionSelected));
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0, 0).applyTo(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);
		createTableViewer(container);
		getSite().registerContextMenu(new MenuManager(), null);
		// track selection changes in the Docker Explorer view (only)
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(DockerExplorerView.VIEW_ID, this);
		hookContextMenu();
		hookToolBarItems();

		// Look at stored preference to determine if all containers should be
		// shown or just running/paused containers. By default, only show
		// running/paused containers
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		boolean showAll = preferences.getBoolean(SHOW_ALL_CONTAINERS_PREFERENCE,
				true);
		showAllContainers(showAll);
		final ICommandService service = getViewSite().getWorkbenchWindow()
				.getService(ICommandService.class);
		service.getCommand(SHOW_ALL_CONTAINERS_COMMAND_ID)
				.getState(TOGGLE_STATE).setValue(showAll);
		service.refreshElements(SHOW_ALL_CONTAINERS_COMMAND_ID, null);
		boolean filterByLabels = preferences
				.getBoolean(FILTER_WITH_LABELS_PREFERENCE, false);
		showContainersWithLabels(filterByLabels);
		service.getCommand(FILTER_CONTAINERS_COMMAND_ID).getState(TOGGLE_STATE)
				.setValue(filterByLabels);
		service.refreshElements(FILTER_CONTAINERS_COMMAND_ID, null);
		DockerConnectionManager.getInstance()
				.addConnectionManagerListener(this);
		// On startup, this view might get set up after the Docker Explorer View
		// and so we won't get the notification when it chooses the connection.
		// Find out if it has a selection and set our connection appropriately.
		ISelection selection = getSite().getWorkbenchWindow()
				.getSelectionService().getSelection(DockerExplorerView.VIEW_ID);
		if (selection != null)
			selectionChanged(null, selection);
	}
	
	private void createTableViewer(final Composite container) {
		this.search = new Text(container, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(search);
		search.addModifyListener(onSearch());
		Composite tableArea = new Composite(container, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(0,  0).applyTo(tableArea);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(tableArea);
		
		final TableColumnLayout tableLayout = new TableColumnLayout();
		tableArea.setLayout(tableLayout);
		this.viewer = new TableViewer(tableArea, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		this.viewer.setContentProvider(new DockerContainersContentProvider());
		final Table table = this.viewer.getTable();
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
						return STARTED_IMAGE;
					} else if (containerStatus == EnumDockerStatus.PAUSED) {
						return PAUSED_IMAGE;
					} else {
						return STOPPED_IMAGE;
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
					return LabelProviderUtils.toCreatedDate(((IDockerContainer)element).created());
				}
				return super.getText(element);
			}
		});
		// 'Command' column
		final TableViewerColumn commandColumn = createColumn(
				DVMessages.getString("COMMAND")); //$NON-NLS-1$
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
						ports.append(LabelProviderUtils.containerPortMappingToString(portMapping));
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
		this.viewer.setComparator(comparator);
		// apply search filter
		this.viewer.setFilters(getContainersFilter());
		setConnection(CommandUtils.getCurrentConnection(null));
		this.viewer.addSelectionChangedListener(onContainerSelection());
		// get the current selection in the tableviewer
		getSite().setSelectionProvider(this.viewer);
	}

	/**
	 * Some ToolBar items are depend on each other's state as enablement
	 * criteria. They must be created programmatically so the state of other
	 * buttons may be changed.
	 */
	private void hookToolBarItems() {
		IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
		pauseAction = createAction(DVMessages.getString("DockerContainersView.pause.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.pauseContainers", //$NON-NLS-1$
				SWTImagesFactory.DESC_PAUSE);
		unpauseAction = createAction(DVMessages.getString("DockerContainersView.unpause.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.unpauseContainers", //$NON-NLS-1$
				SWTImagesFactory.DESC_RESUME);
		startAction = createAction(DVMessages.getString("DockerContainersView.start.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.startContainers", // $NON-NLS-1
				SWTImagesFactory.DESC_START);
		stopAction = createAction(DVMessages.getString("DockerContainersView.stop.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.stopContainers", //$NON-NLS-1$
				SWTImagesFactory.DESC_STOP);
		killAction = createAction(DVMessages.getString("DockerContainersView.kill.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.killContainers", //$NON-NLS-1$
				SWTImagesFactory.DESC_KILL);
		removeAction = createAction(DVMessages.getString("DockerContainersView.remove.label"), //$NON-NLS-1$
				"org.eclipse.linuxtools.docker.ui.commands.removeContainers", //$NON-NLS-1$
				SWTImagesFactory.DESC_REMOVE);

		mgr.add(startAction);
		mgr.add(pauseAction);
		mgr.add(unpauseAction);
		mgr.add(stopAction);
		mgr.add(killAction);
		mgr.add(removeAction);
	}

	private IAction createAction(String label, final String id, ImageDescriptor img) {
		IAction ret = new Action(label, img) {
			@Override
			public void run() {
				ISelection sel = getSelection();
				if (sel instanceof StructuredSelection) {
					CommandUtils.execute(id, (StructuredSelection) sel);
				}
			}
		};
		ret.setEnabled(false);
		return ret;
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
		return e -> {
			if (DockerContainersView.this.viewer != null) {
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

	private void updateToolBarItemEnablement(IStructuredSelection sel) {
		pauseAction.setEnabled(ContainerPropertyTester.isRunning(sel));
		unpauseAction.setEnabled(ContainerPropertyTester.isPaused(sel));
		startAction.setEnabled(ContainerPropertyTester.isStopped(sel));
		stopAction.setEnabled(ContainerPropertyTester.isAnyRunning(sel));
		killAction.setEnabled(ContainerPropertyTester.isAnyRunning(sel));
		removeAction.setEnabled(ContainerPropertyTester.isStopped(sel));
	}

	private ISelectionChangedListener onContainerSelection() {
		return event -> {
			ISelection s = event.getSelection();
			if (s instanceof StructuredSelection) {
				StructuredSelection ss = (StructuredSelection) s;
				updateToolBarItemEnablement(ss);
			}
		};
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		final ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty()) {
			setConnection(null);
			return;
		}
		final Object firstSegment = treeSelection.getPaths()[0].getFirstSegment();
		if (firstSegment instanceof IDockerConnection) {
			final IDockerConnection connection = (IDockerConnection) firstSegment;
			setConnection(connection);
			setLabelFilterIds();
		}
	}
	
	private void setLabelFilterIds() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		boolean filterLabels = preferences.getBoolean(FILTER_WITH_LABELS_PREFERENCE, Boolean.FALSE);
		if (filterLabels) {
			String filterLabelString = preferences
					.get(CONTAINER_FILTER_LABELS, ""); //$NON-NLS-1$
			if (filterLabelString.isEmpty()) {
				containersWithLabelsViewerFilter.setIds(null);
			} else {
				String[] labels = filterLabelString.split("\u00a0"); //$NON-NLS-1$
				LinkedHashMap<String, String> labelMap = new LinkedHashMap<>();
				for (String label : labels) {
					if (label.length() > 1) {
						String[] tokens = label.split("="); //$NON-NLS-1$
						String key = tokens[0];
						String value = ""; //$NON-NLS-1$
						if (tokens.length > 1)
							value = tokens[1];
						labelMap.put(key, value);
					}
				}
				Set<String> filterIds = new HashSet<>();
				try {
					filterIds = ((DockerConnection) connection)
							.getContainerIdsWithLabels(labelMap);
				} catch (DockerException e) {
					Activator.log(e);
				}
				containersWithLabelsViewerFilter.setIds(filterIds);
			}
		}
	}

	@Override
	public void listChanged(final IDockerConnection connection,
			final List<IDockerContainer> containers) {
		if (connection.getName().equals(connection.getName())) {
			Display.getDefault().asyncExec(() -> {
				if (DockerContainersView.this.viewer != null
						&& !DockerContainersView.this.viewer.getTable()
								.isDisposed()) {
					setLabelFilterIds();
					DockerContainersView.this.viewer.refresh();
					refreshViewTitle();
					updateToolBarItemEnablement(DockerContainersView.this.viewer
							.getStructuredSelection());
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

	/**
	 * Sets the active connection
	 * 
	 * @param connection
	 *            the active connection
	 */
	public void setConnection(final IDockerConnection connection) {
		if (connection != null && connection.equals(this.connection)) {
			return;
		}
		// remove 'this' as listener on the previous connection (if applicable)
		if (this.connection != null) {
			this.connection.removeContainerListener(this);
		}
		this.connection = connection;
		if (this.viewer != null && this.connection != null) {
			final Job refreshJob = new Job(
					DVMessages.getString("ContainersRefresh.msg")) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					connection.getContainers(true);
					setLabelFilterIds();
					connection.addContainerListener(DockerContainersView.this);
					Display.getDefault().asyncExec(() -> {
						viewer.setInput(connection);
						refreshViewTitle();
					});
					return Status.OK_STATUS;
				}
			};
			refreshJob.schedule();
		} else if (this.viewer != null) {
			viewer.setInput(new IDockerContainer[0]);
			form.setText(DVMessages.getString(NoConnectionSelected));
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
		if (DockerContainersView.this.viewer == null) {
			return;
		}
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

	/**
	 * Activates {@link ContainersWithLabelsViewerFilter} if the given
	 * {@code enabled} argument is <code>false</code>, deactivates the filter
	 * otherwise.
	 * 
	 * @param enabled
	 *            the argument to enable/disable the filter.
	 */
	public void showContainersWithLabels(boolean enabled) {
		if (DockerContainersView.this.viewer == null) {
			return;
		}
		if (enabled) {
			this.viewer.addFilter(containersWithLabelsViewerFilter);
		} else {
			final List<ViewerFilter> filters = new ArrayList<>(
					Arrays.asList(this.viewer.getFilters()));

			// remove filters and make sure there is no duplicate in the list of
			// filters
			for (Iterator<ViewerFilter> iterator = filters.iterator(); iterator
					.hasNext();) {
				ViewerFilter viewerFilter = iterator.next();
				if (viewerFilter.equals(containersWithLabelsViewerFilter)) {
					iterator.remove();
				}
			}
			this.viewer.setFilters(filters.toArray(new ViewerFilter[0]));
		}
		// Save enablement across sessions using a preference variable.
		IEclipsePreferences preferences = InstanceScope.INSTANCE
				.getNode(Activator.PLUGIN_ID);
		preferences.putBoolean(FILTER_WITH_LABELS_PREFERENCE, enabled);
		refreshViewTitle();
	}

	private void refreshViewTitle() {
		if (this.viewer == null || this.viewer.getControl().isDisposed()
				|| this.form == null
				|| this.connection == null) {
			return;
		} else if (this.connection.getState() == EnumDockerConnectionState.CLOSED) {
			this.form.setText(
					DVMessages.getFormattedString(ConnectionNotAvailable,
					connection.getName()));
			this.form.setEnabled(false);
		} else if (!this.connection.isContainersLoaded()) {
			this.form.setText(connection.getName());
			this.form.setEnabled(false);
		} else {
			int itemCount = viewer.getTable().getItemCount();
			int containersSize = connection.getContainers().size();
			if (itemCount < containersSize) {
				this.form.setText(DVMessages.getFormattedString(
						ViewFilteredTitle, connection.getName(),
						Integer.toString(itemCount),
						Integer.toString(containersSize)));
			} else {
				this.form.setText(DVMessages.getFormattedString(ViewAllTitle,
						new String[] { connection.getName(), Integer.toString(
								containersSize) }));
			}
			this.form.setEnabled(true);
		}
	}

	@Override
	public void changeEvent(int type) {
		// do nothing, this method has been deprecated
	}

	@Override
	public void changeEvent(final IDockerConnection connection,
			final int type) {
		if (type == IDockerConnectionManagerListener.UPDATE_SETTINGS_EVENT) {
			final Job refreshJob = new Job(
					DVMessages.getString("ContainersRefresh.msg")) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					connection.getContainers(true);
					return Status.OK_STATUS;
				}
			};
			refreshJob.addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					Display.getDefault().asyncExec(() -> refreshViewTitle());
				}

			});
			refreshJob.schedule();
		} else if (type == IDockerConnectionManagerListener.RENAME_EVENT) {
			Display.getDefault().asyncExec(() -> refreshViewTitle());
		}
	}

}
