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
package org.eclipse.linuxtools.rpmstubby.popup.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.rpmstubby.SpecfilePomWriter;

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

}
