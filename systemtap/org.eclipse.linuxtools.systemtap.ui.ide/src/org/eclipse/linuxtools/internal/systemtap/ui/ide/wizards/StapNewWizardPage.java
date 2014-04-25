/*******************************************************************************
 * Copyright (c) 2012 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - Initial Wizard and related API
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.wizards;

import java.util.ResourceBundle;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class StapNewWizardPage extends WizardPage {
    private Text fileText;

    private Text containerText;

    private ISelection selection;

    private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("org.eclipse.linuxtools.internal.systemtap.ui.ide.wizards.stap_strings"); //$NON-NLS-1$

    /**
     * Constructor for StapNewWizardPage.
     *
     * @param pageName
     */
    public StapNewWizardPage(ISelection selection) {
        super(resourceBundle.getString("StapNewWizardPage.WizardPage")); //$NON-NLS-1$
        setTitle(resourceBundle.getString("StapNewWizardPage.Title")); //$NON-NLS-1$
        setDescription(resourceBundle.getString("StapNewWizardPage.setDescription")); //$NON-NLS-1$
        this.selection = selection;
    }

    /**
     * @see WizardPage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;

        Label label = new Label(container, SWT.NULL);
        label.setText(resourceBundle.getString("StapNewWizardPage.ScriptName")); //$NON-NLS-1$

        fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        fileText.setLayoutData(gd);
        fileText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        new Label(container, SWT.NULL); // XXX just create a new layout with different width

        label = new Label(container, SWT.NULL);
        label.setText(resourceBundle.getString("StapNewWizardPage.Project")); //$NON-NLS-1$

        containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        containerText.setLayoutData(gd);
        containerText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });

        Button button = new Button(container, SWT.PUSH);
        button.setText(resourceBundle.getString("StapNewWizardPage.Browse")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        initialize();
        dialogChanged();
        setControl(container);
    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */

    private void initialize() {
        if (selection != null && selection.isEmpty() == false
                && selection instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection) selection;
            if (ssel.size() > 1) {
                return;
            }
            Object obj = ssel.getFirstElement();
            if (obj instanceof IResource) {
                IContainer container;
                if (obj instanceof IContainer) {
                    container = (IContainer) obj;
                } else {
                    container = ((IResource) obj).getParent();
                }
                containerText.setText(container.getFullPath().toString());
            }
        }
        fileText.setText(".stp"); //$NON-NLS-1$
    }

    /**
     * Uses the standard container selection dialog to choose the new value for
     * the container field.
     */
    private void handleBrowse() {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(
                getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, resourceBundle.getString("StapNewWizardPage.SelectProjectMessage")); //$NON-NLS-1$
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            if (result.length == 1) {
                containerText.setText(((Path) result[0]).toString());
            }
        }
    }

    /**
     * Ensures that both text fields are set.
     */

    private void dialogChanged() {
        IPath container = Path.fromOSString(getContainerName());
        String fileName = getFileName();
        if (fileName.length() == 0 || fileName.equals(".stp")) { //$NON-NLS-1$
            updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus1")); //$NON-NLS-1$
            return;
        }
        if (getContainerName().length() == 0) {
            updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus2")); //$NON-NLS-1$
            return;
        }
        if (container == null
                || !container.isValidPath(getContainerName())) {
            updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus3")); //$NON-NLS-1$
            return;
        }
        if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
            updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus4")); //$NON-NLS-1$
            return;
        }
        int dotLoc = fileName.lastIndexOf('.');
        if (dotLoc != -1) {
            String ext = fileName.substring(dotLoc + 1);
            if (ext.equalsIgnoreCase("stp") == false) { //$NON-NLS-1$
                updateStatus(resourceBundle.getString("StapNewWizardPage.UpdateStatus.5")); //$NON-NLS-1$
                return;
            }
        }
        updateStatus(null);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getContainerName() {
        return containerText.getText();
    }

    public String getFileName() {
        return fileText.getText();
    }
}
