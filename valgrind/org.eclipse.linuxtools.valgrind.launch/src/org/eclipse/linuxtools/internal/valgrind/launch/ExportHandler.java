/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.launch;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

public class ExportHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                IWorkbenchWizard wizard = new ValgrindExportWizard();
                wizard.init(PlatformUI.getWorkbench(), null);

                WizardDialog dialog = new WizardDialog(parent, wizard);
                dialog.open();
            }
        });

        return null;
    }

}
