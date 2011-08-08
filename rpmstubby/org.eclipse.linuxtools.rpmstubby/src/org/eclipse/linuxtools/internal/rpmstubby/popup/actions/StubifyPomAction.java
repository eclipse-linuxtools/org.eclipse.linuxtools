/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import java.util.Iterator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.internal.rpmstubby.SpecfilePomWriter;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Action handling stybifying RPM spec file from a Maven pom.xml file.
 * 
 */
public class StubifyPomAction extends StubifyAction {

	@Override
	public void run(IAction action) {
		IFile pomFile = null;
		StructuredSelection structuredSelection = (StructuredSelection) selection;
		for (Iterator<?> selectionIter = structuredSelection.iterator(); selectionIter
				.hasNext();) {
			Object selected = selectionIter.next();
			if (selected instanceof IFile) {
				pomFile = (IFile) selected;
			} else {
				// FIXME: error
			}
		}
		SpecfilePomWriter specfileWriter = new SpecfilePomWriter();
		specfileWriter.write(pomFile);
	}

	public Object execute(ExecutionEvent event) {
		IFile featureFile = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection).toList()) {
				if (element instanceof IFile) {
					featureFile = (IFile) element;
				} else if (element instanceof IAdaptable) {
					featureFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
				}
				if (featureFile != null) {
					SpecfilePomWriter specfileWriter = new SpecfilePomWriter();
					specfileWriter.write(featureFile);
				}
			}
			// StructuredSelection structuredSelection = (StructuredSelection)
			// selection;
			// for (Iterator<?> selectionIter = structuredSelection.iterator();
			// selectionIter
			// .hasNext();) {
			// Object selected = selectionIter.next();
			// if (selected instanceof IProject) {
			// featureFile = ((IProject) selected).getFile(new Path(
			// "/feature.xml"));
			// } else if (selected instanceof IFile) {
			// featureFile = (IFile) selected;
			// } else {
			// // FIXME: error
			// }
			// }

		}
		return null;
	}

}
