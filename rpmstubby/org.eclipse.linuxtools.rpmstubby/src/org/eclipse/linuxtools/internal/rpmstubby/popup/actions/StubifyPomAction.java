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
/*******************************************************************************
 * Copyright (c) 2011 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.internal.rpmstubby.SpecfilePomWriter;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Action handling stybifying RPM spec file from a Maven pom.xml file.
 * 
 */
public class StubifyPomAction extends AbstractHandler {

	public Object execute(ExecutionEvent event) {
		IFile pomFile = null;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			for (Object element : ((IStructuredSelection) selection).toList()) {
				if (element instanceof IFile) {
					pomFile = (IFile) element;
				} else if (element instanceof IAdaptable) {
					pomFile = (IFile) ((IAdaptable) element)
							.getAdapter(IFile.class);
				}
				if (pomFile != null) {
					SpecfilePomWriter specfileWriter = new SpecfilePomWriter();
					specfileWriter.write(pomFile);
				}
			}
		}
		return null;
	}

}
