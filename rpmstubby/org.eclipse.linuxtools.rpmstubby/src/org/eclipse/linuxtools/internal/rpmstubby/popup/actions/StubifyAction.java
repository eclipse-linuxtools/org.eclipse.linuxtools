/*******************************************************************************
 * Copyright (c) 2006, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Base class for the different stubify actions.
 *
 */
public abstract class StubifyAction extends AbstractHandler implements IObjectActionDelegate  {

	protected ISelection selection;

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public abstract void run(IAction action);

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		IStructuredSelection structuredSelection = null;
		ISelectionProvider provider = targetPart.getSite()
				.getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof IStructuredSelection)
				structuredSelection = (IStructuredSelection) selection;
		}
		structuredSelection = StructuredSelection.EMPTY;
		this.selection = (ISelection) structuredSelection.getFirstElement();
	}


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}
}
