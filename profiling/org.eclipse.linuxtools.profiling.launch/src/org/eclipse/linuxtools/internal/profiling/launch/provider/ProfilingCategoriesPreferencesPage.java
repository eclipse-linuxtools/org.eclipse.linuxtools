/*******************************************************************************
 * Copyright (c) 2012, 2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Red Hat initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.profiling.launch.provider;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.linuxtools.internal.profiling.launch.provider.launch.Messages;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class ProfilingCategoriesPreferencesPage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    @Override
    public void init(IWorkbench workbench) {
        setDescription(Messages.ProviderPreferencesPage_2);
    }

    @Override
    protected void createFieldEditors() {
        // Content for global profiling provider preferences.
    }

    @Override
    protected Control createContents(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, getHelpContextId());
        return super.createContents(parent);
    }

    /**
     * Return the help context id to use if the help button is pushed.
     *
     * @return the help context id
     */
    private String getHelpContextId() {
        return ProviderProfileConstants.PLUGIN_ID + ".profiling_categories";  //$NON-NLS-1$
    }

}