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

package org.eclipse.linuxtools.internal.rpm.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Custom panel to be reused by different RPM project creation and import
 * wizards. Contains the path and project type.
 *
 */
public class RPMDetailsPanel {

    private Text locationPath;
    private ComboViewer typeCombo;
    private Button defaultSettings;
    private Composite parent;

    /**
     * Initializes the panel with all the needed components and listeners.
     *
     * @param parent
     *            The composite that will handle the childrens
     */
    public RPMDetailsPanel(Composite parent) {
        this.parent = parent;
        initialize();
    }

    private void initialize() {
        defaultSettings = new Button(parent, SWT.CHECK);
        defaultSettings.setText(Messages.getString("SRPMImportPage.0")); //$NON-NLS-1$
        defaultSettings.setSelection(true);

        final Group specGrid = new Group(parent, SWT.NONE);
        defaultSettings.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (defaultSettings.getSelection()) {
                    for (Control control : specGrid.getChildren()) {
                        specGrid.setEnabled(false);
                        control.setEnabled(false);
                    }
                } else {
                    for (Control control : specGrid.getChildren()) {
                        specGrid.setEnabled(true);
                        control.setEnabled(true);
                    }
                }
            }

        });
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        specGrid.setLayout(layout);
        specGrid.setText(Messages.getString("SRPMImportPage.1")); //$NON-NLS-1$
        specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
                | GridData.HORIZONTAL_ALIGN_FILL));
        specGrid.setEnabled(false);
        Label locationLabel = new Label(specGrid, SWT.NULL);
        locationLabel.setText(Messages.getString("SRPMImportPage.2")); //$NON-NLS-1$
        locationLabel.setEnabled(false);
        locationPath = new Text(specGrid, SWT.SINGLE | SWT.BORDER);
        locationPath.setEnabled(false);
        locationPath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL
                | GridData.GRAB_HORIZONTAL));
        Button containerBrowseButton = new Button(specGrid, SWT.PUSH);
        containerBrowseButton.setText(Messages.getString("SRPMImportPage.3")); //$NON-NLS-1$
        containerBrowseButton.setLayoutData(new GridData(
                GridData.HORIZONTAL_ALIGN_FILL));
        containerBrowseButton.setEnabled(false);

        final Composite projectTypeGroup = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.numColumns = 2;
        projectTypeGroup.setLayout(layout);
        projectTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label typeLabel = new Label(projectTypeGroup, SWT.NULL);
        typeLabel.setText(Messages.getString("SRPMImportPage.4")); //$NON-NLS-1$
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        typeCombo = new ComboViewer(projectTypeGroup, SWT.READ_ONLY);
        typeCombo.getCombo().setLayoutData(gridData);
        typeCombo.setContentProvider(ArrayContentProvider.getInstance());
        typeCombo.setInput(RPMProjectLayout.values());
        typeCombo.setSelection(new StructuredSelection(RPMProjectLayout.RPMBUILD));

    }

    /**
     * Returns the selected layout if any or the default one - RPMBUILD.
     *
     * @return The selected project layout.
     */
    public RPMProjectLayout getSelectedLayout() {
        return RPMProjectLayout.valueOf(typeCombo.getCombo().getItem(
                typeCombo.getCombo().getSelectionIndex()));
    }

    /**
     * Sets the location path to given absolute path.
     *
     * @param absolutePath The path to be shown.
     */
    public void setLocationPath(String absolutePath) {
        locationPath.setText(absolutePath);

    }

    /**
     * Returns the default path or whatever is entered.
     *
     * @return The location path to use for the project.
     */
    public IPath getLocationPath() {
        return Path.fromOSString(locationPath.getText());
    }

}
