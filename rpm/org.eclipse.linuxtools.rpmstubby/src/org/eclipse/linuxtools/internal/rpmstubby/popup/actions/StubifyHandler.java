/*******************************************************************************
 * Copyright (c) 2011, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpmstubby.popup.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
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
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof IStructuredSelection) {
            for (Object element : ((IStructuredSelection) selection).toList()) {
                if (element instanceof IFile) {
                    featureFile = (IFile) element;
                } else if (element instanceof IAdaptable) {
                    featureFile = ((IAdaptable) element)
                            .getAdapter(IFile.class);
                }
                if (featureFile != null) {
                    Generator generator = new Generator(getInputType());
                    generator.generate(featureFile);
                }
            }
        }
        return null;
    }

}
