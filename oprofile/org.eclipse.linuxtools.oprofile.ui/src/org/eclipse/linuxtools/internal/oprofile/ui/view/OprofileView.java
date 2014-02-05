/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.internal.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * The view for the OProfile plugin. Shows the elements gathered by the data model
 *   in a tree viewer, parsed by the ui model (in the model package). The hierarchy
 *   (as it is displayed) looks like:
 *
 *   UiModelRoot (not shown in the view)
 *   \_ UiModelEvent
 *   \_ ...
 *   \_ UiModelEvent
 *      \_ UiModelSession
 *      \_ ...
 *      \_ UiModelSession
 *         \_ UiModelImage
 *         |  \_ UiModelSymbol
 *         |  \_ ...
 *         |  \_ UiModelSymbol
 *         |     \_ UiModelSample
 *         |     \_ ...
 *         |     \_ UiModelSample
 *         \_ UiModelDependent
 *            \_ UiModelImage
 *            |  \_ ... (see above)
 *            \_ ...
 *
 * The refreshView() function takes care of launching the data model parsing and
 *   ui model parsing in a separate thread.
 */
public class OprofileView extends ViewPart implements ISelectionChangedListener {
	private TreeViewer viewer;
	private IAction deleteSessionAction;
	private IAction saveDefaultSessionAction;

	@Override
	public void createPartControl(Composite parent) {
		createTreeViewer(parent);
		createActionMenu();

		OprofileUiPlugin.getDefault().setOprofileView(this);
	}

	private void createTreeViewer(Composite parent) {
		viewer = new TreeViewer(parent, SWT.SINGLE);
		viewer.setContentProvider(new OprofileViewContentProvider());
		viewer.setLabelProvider(new OprofileViewLabelProvider());
		viewer.addDoubleClickListener(new OprofileViewDoubleClickListener());
		viewer.addSelectionChangedListener(this);
	}

	private void createActionMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();

		manager.add(new OprofileViewLogReaderAction());
		manager.add(new OprofileViewRefreshAction());
		saveDefaultSessionAction = new OprofileViewSaveDefaultSessionAction();
		manager.add(saveDefaultSessionAction);
		deleteSessionAction = new OprofileViewDeleteSessionAction(getTreeViewer());
		manager.add(deleteSessionAction);

		MenuManager sortMenu = new MenuManager(
				OprofileUiMessages.getString("view.menu.sortby.label")); //$NON-NLS-1$

		for (UiModelRoot.SORT_TYPE s : UiModelRoot.SORT_TYPE.values()) {
			sortMenu.add(new OprofileViewSortAction(s,
					OprofileViewSortAction.sortTypeMap.get(s)));
		}
		manager.add(sortMenu);

	}

	private TreeViewer getTreeViewer() {
		return viewer;
	}

	/**
	 * Extremely convoluted way of getting the running and parsing to happen in
	 *   a separate thread, with a progress monitor. In most cases and on fast
	 *   machines this will probably only be a blip.
	 */
	public void refreshView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(OprofileUiPlugin.ID_OPROFILE_VIEW);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		IRunnableWithProgress refreshRunner = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(OprofileUiMessages.getString("view.dialog.parsing.text"), 2); //$NON-NLS-1$

				OpModelRoot dataModelRoot = OpModelRoot.getDefault();
				dataModelRoot.refreshModel();
				monitor.worked(1);

				final UiModelRoot UiRoot = UiModelRoot.getDefault();
				UiRoot.refreshModel();

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						OprofileUiPlugin.getDefault().getOprofileView().getTreeViewer().setInput(UiRoot);
					}
				});
				monitor.worked(1);
				monitor.done();
			}
		};

		ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
		try {
			dialog.run(true, false, refreshRunner);
		} catch (InvocationTargetException|InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		TreeSelection tsl = (TreeSelection) viewer.getSelection();
		if (tsl.getFirstElement() instanceof UiModelSession) {
			if (!deleteSessionAction.isEnabled()) {
				deleteSessionAction.setEnabled(true);
			}

			if (((UiModelSession) tsl.getFirstElement()).isDefaultSession()) {
				if (!saveDefaultSessionAction.isEnabled()) {
					saveDefaultSessionAction.setEnabled(true);
				}
			}
		} else {
			deleteSessionAction.setEnabled(false);
			saveDefaultSessionAction.setEnabled(false);

		}

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

	@Override
	public void dispose() {
		super.dispose();
		viewer.removeSelectionChangedListener(this);
	}
}
