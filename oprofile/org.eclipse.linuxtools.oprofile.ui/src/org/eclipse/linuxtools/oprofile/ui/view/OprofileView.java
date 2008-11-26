/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.oprofile.core.model.OpModelRoot;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 */
public class OprofileView extends ViewPart {
	private TreeViewer _viewer;

	@Override
	public void createPartControl(Composite parent) {
		_createTreeViewer(parent);
		_createActionMenu();

		OprofileUiPlugin.getDefault().setOprofileView(this);
	}
	
	private void _createTreeViewer(Composite parent) {
		_viewer = new TreeViewer(parent);
		_viewer.setContentProvider(new OprofileViewContentProvider());
		_viewer.setLabelProvider(new OprofileViewLabelProvider());
	}

	private void _createActionMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		final OprofileView view = this;
		manager.add(new Action("Refresh Model"){
			public void run() {
				view.refreshView();
			}
		});
	}
	
	private TreeViewer getTreeViewer() {
		return _viewer;
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
				//TODO: externalize
				monitor.beginTask("Parsing OProfile data", 2);

				OpModelRoot dataModelRoot = OpModelRoot.getDefault();
				dataModelRoot.refreshModel();
				System.out.println(dataModelRoot);	//debugging
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
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}

}
