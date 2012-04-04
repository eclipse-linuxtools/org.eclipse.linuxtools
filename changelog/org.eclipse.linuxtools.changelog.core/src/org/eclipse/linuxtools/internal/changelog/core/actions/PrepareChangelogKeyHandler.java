/*******************************************************************************
 * Copyright (c) 2006-2008 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.changelog.core.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.changelog.core.Messages;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author klee
 *
 */
public class PrepareChangelogKeyHandler extends ChangeLogAction implements IHandler, IWorkbenchWindowActionDelegate {
	
	private IWorkbenchWindow window;
	
	public PrepareChangelogKeyHandler() {
		super();
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IStructuredSelection tempResult = null;
		
		// try getting currently selected project
		try {
			IWorkbenchPage ref = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			IWorkbenchPart part = HandlerUtil.getActivePart(event);
			if (part instanceof IEditorPart) {
				// If we are in an editor, check if the file being edited is an IResource
				// that belongs to a project in the workspace
				IEditorPart editorPart = (IEditorPart)part;
				IEditorInput input = editorPart.getEditorInput();
				IResource r = (IResource)input.getAdapter(IResource.class);
				if (r != null) {
					// We have an IResource to work with, so create a selection we can use
					// in PrepareChangeLogAction
					tempResult = new StructuredSelection(r);
				}
			} else {
				// Otherwise, our view is not an editor, see if we have an IResource or something
				// that will lead us to an IResource
				ISelection selected = ref.getSelection();
				if (selected instanceof IStructuredSelection) {
					IResource r = null;
					IStructuredSelection iss = (IStructuredSelection)selected;
					Object o = ((IStructuredSelection)selected).getFirstElement();
					if (o instanceof ISynchronizeModelElement) {
						r = ((ISynchronizeModelElement)o).getResource();
					} else if (o instanceof IAdaptable) {
						r = (IResource)((IAdaptable)o).getAdapter(IResource.class);
					}
					if (r != null)
						tempResult = iss;
				}
			}
			if (tempResult == null) {
			    // We don't have an obvious project match in the current active view.  
				// Let's search all open views for the Synchronize View which is our first
				// choice to fall back on.
				for (IViewReference view: ref.getViewReferences()) {
					if (view.getId().equals("org.eclipse.team.sync.views.SynchronizeView")) { // $NON-NLS-1$
						IViewPart v = view.getView(false);
						ISelection s = null;
						ISelectionProvider sp = v.getViewSite().getSelectionProvider();
						if (sp != null) {
							s = sp.getSelection();
						}
						if (s != null && s instanceof IStructuredSelection) {
							IStructuredSelection ss = (IStructuredSelection)s;
							Object element = ss.getFirstElement();
							IResource r = null;
							if (element instanceof ISynchronizeModelElement) {
								r = ((ISynchronizeModelElement)element).getResource();
							} else if (element instanceof IAdaptable) {
								r = (IResource)((IAdaptable)element).getAdapter(IResource.class);
							}

							if (r != null) {
								tempResult = ss;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			tempResult = null;
		}

		// If we can't find the project directly, let the user know.
		if (tempResult == null) {
			MessageDialog.openInformation(getActiveWorkbenchShell(), Messages.getString("ChangeLog.PrepareChangeLog"), // $NON-NLS-1$, 
					Messages.getString("PrepareChangeLog.InfoNoProjectFound")); // $NON-NLS-1$
			return null;
		} 

		final IStructuredSelection result = tempResult;
		Action exampleAction = new PrepareChangeLogAction() {
			@Override
			public void run() {
				setSelection(result);
				doRun();
			}
		};

		exampleAction.run();

		return null;
	}

	public void addHandlerListener(IHandlerListener handlerListener) {

	}

	public void dispose() {
	}
	
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}
	
	/**
	 * Returns active shell.
	 */
	protected Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public void run(IAction action) {

		try {
			execute(null);
		} catch (ExecutionException e) {
			reportErr(Messages.getString("PrepareChangeLog.ErrExecuteFailed"), e); // $NON-NLS-1$
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {

	}
	
}
