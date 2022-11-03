/*******************************************************************************
 * Copyright (c) 2011, 2022 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.devhelp.preferences;

import java.io.File;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpGenerateJob;
import org.eclipse.linuxtools.internal.cdt.libhover.devhelp.DevHelpPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class LibHoverPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    // ID of the context help for the preference page
    private static final String HELP_CONTEXT_ID = "org.eclipse.linuxtools.cdt.libhover.devhelp.prefs"; //$NON-NLS-1$

    private final static String DEVHELP_DIR = "Libhover.Devhelp.Directory"; //$NON-NLS-1$
    private final static String GENERATE = "Libhover.Devhelp.Generate.lbl"; //$NON-NLS-1$
    private final static String TITLE = "Libhover.Devhelp.Preference.title"; //$NON-NLS-1$

    private Button generateButton;

    public LibHoverPreferencePage() {
        super(GRID);
    }

    @Override
    public void init(IWorkbench workbench) {
        // Nothing to do
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return DevHelpPlugin.getDefault().getPreferenceStore();
    }

    private static class DevhelpStringFieldEditor extends DirectoryFieldEditor {
        public DevhelpStringFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
            setFilterPath(new File(DevHelpPlugin.getDefault().getPreferenceStore().getString(name)));
        }

    }

    private synchronized void regenerate() {
        generateButton.setEnabled(false);
        Job k = new DevHelpGenerateJob(true);
        k.setUser(true);
        k.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent event) {
                Display.getDefault().syncExec(() -> {
				    if (generateButton != null && !generateButton.isDisposed())
				        generateButton.setEnabled(true);
				});
            }
        });
        k.schedule();
    }

    @Override
    protected void contributeButtons(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        generateButton = new Button(parent, SWT.NONE);
        generateButton.setFont(parent.getFont());
        generateButton.setText(LibHoverMessages.getString(GENERATE));
        generateButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> regenerate()));
        GridData gd = new GridData();

        gd.horizontalAlignment = GridData.FILL;
        int widthHint = convertHorizontalDLUsToPixels(
                IDialogConstants.BUTTON_WIDTH);
        gd.widthHint = Math.max(widthHint, generateButton.computeSize(
                SWT.DEFAULT, SWT.DEFAULT, true).x);

        generateButton.setLayoutData(gd);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HELP_CONTEXT_ID);
    }

    @Override
    protected Control createContents(Composite parent) {
        Label desc = new Label(parent, SWT.NONE);
        desc.setText(LibHoverMessages.getString(TITLE));
        return super.createContents(parent);
    }

    @Override
    public void createFieldEditors() {
        addField(new DevhelpStringFieldEditor(PreferenceConstants.DEVHELP_DIRECTORY,
                LibHoverMessages.getString(DEVHELP_DIR), getFieldEditorParent()));
    }
}