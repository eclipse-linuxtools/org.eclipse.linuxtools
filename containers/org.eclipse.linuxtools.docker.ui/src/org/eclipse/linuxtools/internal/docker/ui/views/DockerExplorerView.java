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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener;
import org.eclipse.linuxtools.docker.core.IDockerConnectionManagerListener2;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.docker.core.IDockerImageListener;
import org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils;
import org.eclipse.linuxtools.internal.docker.ui.wizards.NewDockerConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * {@link CommonNavigator} that display a tree of available
 * {@link IDockerConnection}s and for each one, the {@link IDockerContainer}s
 * and {@link IDockerImage}s under separate categories.
 *
 */
public class DockerExplorerView extends CommonNavigator implements
		IDockerConnectionManagerListener2, ITabbedPropertySheetPageContributor {

	private static final String NO_CONNECTION_LABEL = "NoConnection.label"; //$NON-NLS-1$

	/** the id of the {@link DockerExplorerView}. */
	public static final String VIEW_ID = "org.eclipse.linuxtools.docker.ui.dockerExplorerView";
	
	private Control connectionsPane;
	private Control explanationsPane;
	private Control currentPane;
	private PageBook pageBook;
	private Map<IDockerConnection, ContainersRefresher> containersRefreshers = new HashMap<>();
	private Map<IDockerConnection, ImagesRefresher> imagesRefreshers = new HashMap<>();

	/** the search text widget. to filter containers and images. */
	private Text search;

	private ViewerFilter containersAndImagesSearchFilter;

	@Override
	protected Object getInitialInput() {
		return DockerConnectionManager.getInstance();
	}

	@Override
	protected CommonViewer createCommonViewer(final Composite parent) {
		final CommonViewer viewer = super.createCommonViewer(parent);
		setLinkingEnabled(false);
		return viewer;
	}

	@Override
	public String getContributorId() {
		return getSite().getId();
	}
	
	@Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (IPropertySheetPage.class.isAssignableFrom(adapter)) {
            return new TabbedPropertySheetPage(this, true);
        }
        return super.getAdapter(adapter);
    }

	@Override
	public void dispose() {
		// remove all ContainersRefresher instance registered on the Docker
		// connections
		for (Iterator<Entry<IDockerConnection, ContainersRefresher>> iterator = containersRefreshers
				.entrySet().iterator(); iterator.hasNext();) {
			final Entry<IDockerConnection, ContainersRefresher> entry = iterator
					.next();
			entry.getKey().removeContainerListener(entry.getValue());
			iterator.remove();
		}
		// remove all ImagesRefresher instance registered on the Docker
		// connections
		for (Iterator<Entry<IDockerConnection, ImagesRefresher>> iterator = imagesRefreshers
				.entrySet().iterator(); iterator.hasNext();) {
			final Entry<IDockerConnection, ImagesRefresher> entry = iterator
					.next();
			entry.getKey().removeImageListener(entry.getValue());
			iterator.remove();
		}
		DockerConnectionManager.getInstance().removeConnectionManagerListener(
				this);

		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {
		final FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		this.pageBook = new PageBook(parent, SWT.NONE);
		this.connectionsPane = createConnectionsPane(pageBook, toolkit);
		this.explanationsPane = createExplanationPane(pageBook, toolkit);
		showConnectionsOrExplanations();
		this.containersAndImagesSearchFilter = getContainersAndImagesSearchFilter();
		getCommonViewer().addFilter(containersAndImagesSearchFilter);
		DockerConnectionManager.getInstance()
				.addConnectionManagerListener(this);
	}

	/**
	 * @return a {@link ViewerFilter} that will retain {@link IDockerContainer}
	 *         and {@link IDockerImage} that match the content of the
	 *         {@link DockerExplorerView#search} text widget.
	 */
	private ViewerFilter getContainersAndImagesSearchFilter() {
		return new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				// filtering Docker containers
				if (element instanceof IDockerContainer
						|| element instanceof IDockerImage) {
					return element.toString().contains(
							DockerExplorerView.this.search.getText());
				}
				// any other element makes it through the filter (i.e., it is
				// displayed)
				return true;
			}
		};
	}

	private Control createConnectionsPane(final PageBook pageBook,
			final FormToolkit toolkit) {
		final Form form = toolkit.createForm(pageBook);
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5)
				.applyTo(container);
		this.search = new Text(container, SWT.SEARCH | SWT.ICON_SEARCH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, false).applyTo(search);
		search.addModifyListener(onSearch());
		super.createPartControl(container);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL)
				.grab(true, true).applyTo(getCommonViewer().getControl());
		if (DockerConnectionManager.getInstance().getConnections().length > 0) {
			IDockerConnection conn = DockerConnectionManager.getInstance().getConnections()[0];
			getCommonViewer().setSelection(new StructuredSelection(conn));
		}
		return form;
	}

	/**
	 * Filters {@link IDockerContainer} and {@link IDockerImage} using the input
	 * text in the search widget of this view.
	 * 
	 * @return
	 */
	private ModifyListener onSearch() {
		return new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				final CommonViewer viewer = DockerExplorerView.this
						.getCommonViewer();
				final TreePath[] treePaths = viewer.getExpandedTreePaths();
				for (TreePath treePath : treePaths) {
					final Object lastSegment = treePath.getLastSegment();
					if (lastSegment instanceof DockerExplorerContentProvider.DockerContainersCategory
							|| lastSegment instanceof DockerExplorerContentProvider.DockerImagesCategory) {
						viewer.refresh(lastSegment);
						viewer.expandToLevel(treePath,
								AbstractTreeViewer.ALL_LEVELS);
					}
				}
			}
		};
	}

	private Control createExplanationPane(final PageBook pageBook,
			final FormToolkit toolkit) {
		final Form form = toolkit.createForm(pageBook);
		final Composite container = form.getBody();
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 5)
				.applyTo(container);
		final Link link = new Link(container, SWT.NONE);
		link.setText(DVMessages.getString(NO_CONNECTION_LABEL));
		link.setBackground(pageBook.getDisplay().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL)
				.grab(true, false).applyTo(link);
		link.addSelectionListener(onExplanationClicked());
		return form;
	}

	private SelectionAdapter onExplanationClicked() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				CommandUtils.openWizard(new NewDockerConnection(),
						PlatformUI.getWorkbench().getModalDialogShellProvider()
								.getShell());
			}
		};
	}

	/**
	 * Shows the {@link DockerExplorerView#explanationsPane} or the
	 * {@link DockerExplorerView#connectionsPane} depending on the number of
	 * connections in the {@link DockerConnectionManager}.
	 */
	public void showConnectionsOrExplanations() {
		if (DockerConnectionManager.getInstance().getConnections().length < 1) {
			pageBook.showPage(explanationsPane);
			this.currentPane = explanationsPane;
		} else {
			pageBook.showPage(connectionsPane);
			this.currentPane = connectionsPane;
			registerListeners();
		}
	}

	/**
	 * @return <code>true</code> if the current panel is the one containing a
	 *         {@link TreeViewer} of {@link IDockerConnection}s,
	 *         <code>false</code> otherwise.
	 */
	public boolean isShowingConnectionsPane() {
		return this.currentPane == connectionsPane;
	}

	@Override
	@Deprecated
	public void changeEvent(final int type) {
		// method kept for backward compatibility
	}

	@Override
	public void changeEvent(final IDockerConnection connection,
			final int type) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				showConnectionsOrExplanations();
				switch (type) {
				case IDockerConnectionManagerListener.ADD_EVENT:
					registerListeners(connection);
					getCommonViewer().refresh();
					getCommonViewer()
							.setSelection(new StructuredSelection(connection));
					break;
				case IDockerConnectionManagerListener.REMOVE_EVENT:
					unregisterListeners(connection);
					getCommonViewer().refresh();
					// move viewer selection to the first connection or set to
					// null if
					// no other connection exists
					final IDockerConnection[] connections = DockerConnectionManager
							.getInstance().getConnections();
					if (connections.length > 0) {
						getCommonViewer().setSelection(
								new StructuredSelection(connections[0]), true);
					} else {
						getCommonViewer().setSelection(null);
					}
					break;
				}
			}
		});
	}

	private void registerListeners() {
		for (IDockerConnection connection : DockerConnectionManager
				.getInstance().getConnections()) {
			registerListeners(connection);
		}
	}

	private void registerListeners(final IDockerConnection connection) {
		if (!containersRefreshers.containsKey(connection)) {
			final ContainersRefresher refresher = new ContainersRefresher();
			connection.addContainerListener(refresher);
			containersRefreshers.put(connection, refresher);
		}
		if (!imagesRefreshers.containsKey(connection)) {
			final ImagesRefresher refresher = new ImagesRefresher();
			connection.addImageListener(refresher);
			imagesRefreshers.put(connection, refresher);
		}
	}

	private void unregisterListeners(final IDockerConnection connection) {
		if (containersRefreshers.containsKey(connection)) {
			final ContainersRefresher refresher = containersRefreshers
					.get(connection);
			connection.removeContainerListener(refresher);
			containersRefreshers.remove(connection);
		}
		if (imagesRefreshers.containsKey(connection)) {
			final ImagesRefresher refresher = imagesRefreshers.get(connection);
			connection.removeImageListener(refresher);
			imagesRefreshers.remove(connection);
		}
	}

	private void refresh(final IDockerConnection connection) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (getCommonViewer().getTree() != null
						&& !getCommonViewer().getTree().isDisposed()) {
					// following is to force Container property testers
					// to run again after list is updated. They won't do so by
					// default.
					final ISelection selection = getCommonViewer().getSelection();
					getCommonViewer().refresh(connection, true);
					if (selection != null) {
						getCommonViewer().setSelection(selection, false);
					}
				}
			}
		});
	}

	class ContainersRefresher implements IDockerContainerListener {

		@Override
		public void listChanged(final IDockerConnection connection,
				final List<IDockerContainer> containers) {
			refresh(connection);
		}
	}

	class ImagesRefresher implements IDockerImageListener {

		@Override
		public void listChanged(final IDockerConnection connection,
				final List<IDockerImage> images) {
			refresh(connection);
		}

	}

}
