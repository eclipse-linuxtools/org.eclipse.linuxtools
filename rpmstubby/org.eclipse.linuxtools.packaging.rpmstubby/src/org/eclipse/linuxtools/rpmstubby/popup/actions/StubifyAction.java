/*******************************************************************************
 * Copyright (c) 2006 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpmstubby.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.rpmstubby.SpecfileWriter;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class StubifyAction implements IObjectActionDelegate {

	private ISelection selection;
	private IFile featureFile = null;

	/**
	 * Constructor for StubifyAction.
	 */
	public StubifyAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		IStructuredSelection structuredSelection = null;
		ISelectionProvider provider= targetPart.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection= provider.getSelection();
			if (selection instanceof IStructuredSelection)
				structuredSelection = (IStructuredSelection)selection;
		}
		structuredSelection = StructuredSelection.EMPTY;
		this.selection = (ISelection) structuredSelection.getFirstElement();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		for (Iterator selectionIter = structuredSelection.iterator(); selectionIter.hasNext();) {
			Object selected = (Object) selectionIter.next();
			if (selected instanceof IProject) {
				featureFile = ((IProject) selected).getFile(new Path("/feature.xml"));
			} else if (selected instanceof IFile) {
				featureFile = (IFile) selected;
			} else {
				// FIXME:  error
			}
		}
		SpecfileWriter specfileWriter = new SpecfileWriter();
		specfileWriter.write(featureFile);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
}
