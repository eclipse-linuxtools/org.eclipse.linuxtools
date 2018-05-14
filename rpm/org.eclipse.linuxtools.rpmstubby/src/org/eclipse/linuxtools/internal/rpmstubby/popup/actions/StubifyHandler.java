/*******************************************************************************
 * Copyright (c) 2011, 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.rpmstubby.Generator;
import org.eclipse.linuxtools.rpmstubby.InputType;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Common functionality for all the stubify commands.
 *
 */
public abstract class StubifyHandler extends AbstractHandler {

	protected abstract InputType getInputType();

	@Override
	public Object execute(ExecutionEvent event) {

		IFile featureFile = null;
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		for (Object element : selection.toList()) {
			if (element instanceof IFile) {
				featureFile = (IFile) element;
			} else if (element instanceof IAdaptable) {
				featureFile = ((IAdaptable) element).getAdapter(IFile.class);
			}
			if (featureFile != null) {
				Generator generator = new Generator(getInputType());
				generator.generate(featureFile);
			}
		}
		return null;
	}

}
