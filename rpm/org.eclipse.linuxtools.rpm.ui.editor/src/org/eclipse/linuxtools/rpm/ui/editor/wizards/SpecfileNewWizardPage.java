/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Red Hat Inc. - ongoging maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.rpm.ui.editor.wizards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.linuxtools.internal.rpm.ui.editor.SpecfileLog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class SpecfileNewWizardPage extends WizardPage {

    private static final String NAME = "package_name"; //$NON-NLS-1$

    private static final String VERSION = "1.0"; //$NON-NLS-1$

    private static final String SUMMARY = "Summary of the package"; //$NON-NLS-1$

    private static final String GROUP = "Amusements/Games"; //$NON-NLS-1$

    private static final String LICENSE = "GPL"; //$NON-NLS-1$

    private static final String URL = "http://"; //$NON-NLS-1$

    private static final String SOURCE0 = "archive_name-%{version}"; //$NON-NLS-1$

    private Text projectText;

    private Text nameText;

    private Text versionText;

    private Text summaryText;

    private Combo groupCombo;

    private Text licenseText;

    private Text urlText;

    private Text source0Text;

    private GridData gd;

    private ISelection selection;

    private String selectedTemplate = "minimal"; //$NON-NLS-1$

    private String content;

    /**
     * Constructor for SpecfileNewWizardPage.
     *
     * @param selection
     *            The selection to put the new spec file in.
     */
    public SpecfileNewWizardPage(ISelection selection) {
        super("wizardPage"); //$NON-NLS-1$
        setTitle(Messages.SpecfileNewWizardPage_9);
        setDescription(Messages.SpecfileNewWizardPage_10);
        this.selection = selection;
    }

    /**
     * @see WizardPage#createControl(Composite)
     */
    @Override
    public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 3;
        layout.verticalSpacing = 9;

        // Project
        Label label = new Label(container, SWT.NULL);
        label.setText(Messages.SpecfileNewWizardPage_11);
        projectText = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        projectText.setLayoutData(gd);
        projectText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        Button button = new Button(container, SWT.PUSH);
        button.setText(Messages.SpecfileNewWizardPage_12);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });

        // Template to use
        label = new Label(container, SWT.NULL);
        label.setText(Messages.SpecfileNewWizardPage_13);
        Combo templateCombo = new Combo(container, SWT.NULL);
        try {
            populateTemplateCombo(templateCombo);
        } catch (CoreException e2) {
            SpecfileLog.logError(e2);
        }
        // empty label for the last row.
        label = new Label(container, SWT.NULL);
        templateCombo.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                selectedTemplate = ((Combo) e.getSource()).getText();
                InputStream inputStream = runRpmdevNewSpec(selectedTemplate);
                LineNumberReader reader = new LineNumberReader(
                        new InputStreamReader(inputStream));
                String line;
                try {
                    content = ""; //$NON-NLS-1$
                    setDefaultValues();
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("Name:")) { //$NON-NLS-1$
                            setTemplateTagValue(nameText, line);
                        }
                        if (line.startsWith("Version:")) { //$NON-NLS-1$
                            setTemplateTagValue(versionText, line);
                        }
                        if (line.startsWith("Summary:")) { //$NON-NLS-1$
                            setTemplateTagValue(summaryText, line);
                        }
                        if (line.startsWith("Group:")) { //$NON-NLS-1$
                            String[] items = line.split(":", 2); //$NON-NLS-1$
                            String value = items[1].trim();
                            if (!value.equals("")) {//$NON-NLS-1$
                                groupCombo.setText(value);
                            }
                        }
                        if (line.startsWith("License:")) { //$NON-NLS-1$
                            setTemplateTagValue(licenseText, line);
                        }
                        if (line.startsWith("URL:")) { //$NON-NLS-1$
                            setTemplateTagValue(urlText, line);
                        }
                        if (line.startsWith("Source0:")) { //$NON-NLS-1$
                            setTemplateTagValue(source0Text, line);
                        }
                        content += line + '\n';
                    }
                } catch (IOException e1) {
                    SpecfileLog.logError(e1);
                }
            }
        });

        // Package Name
        nameText = setTextItem(container, Messages.SpecfileNewWizardPage_14);

        // Package Version
        versionText = setTextItem(container, Messages.SpecfileNewWizardPage_15);

        // Package Summary
        summaryText = setTextItem(container, Messages.SpecfileNewWizardPage_16);

        // Package Group
        label = new Label(container, SWT.NULL);
        label.setText(Messages.SpecfileNewWizardPage_17);
        groupCombo = new Combo(container, SWT.NULL);
        populateGroupCombo(groupCombo);
        // empty label for the last row.
        new Label(container, SWT.NULL);

        // Package License
        licenseText = setTextItem(container, Messages.SpecfileNewWizardPage_18);

        // Package URL
        urlText = setTextItem(container, Messages.SpecfileNewWizardPage_19);

        // Package Source0
        source0Text = setTextItem(container, Messages.SpecfileNewWizardPage_20);

        initialize();
        dialogChanged();
        setControl(container);
    }

    private Text setTextItem(Composite container, String textLabel) {
        Label label = new Label(container, SWT.NULL);
        label.setText(textLabel);
        Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
        text.setLayoutData(gd);
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                dialogChanged();
            }
        });
        // empty label for the last row.
        new Label(container, SWT.NULL);
        return text;
    }

    private void setTemplateTagValue(Text text, String line) {
        String[] items = line.split(":", 2); //$NON-NLS-1$
        String value = items[1].trim();
        if (!value.equals("")) { //$NON-NLS-1$
            text.setText(value);
        }
    }

    public String getProjectName() {
        return projectText.getText();
    }

    public String getFileName() {
        return nameText.getText() + ".spec"; //$NON-NLS-1$
    }

    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    public String getContent() {
        InputStream inputStream = runRpmdevNewSpec(selectedTemplate);
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(
                inputStream));
        String line;
        try {
            content = ""; //$NON-NLS-1$
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Name:")) { //$NON-NLS-1$
                    line = "Name:" + "           " + nameText.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("Version:")) { //$NON-NLS-1$
                    line = "Version:" + "        " + versionText.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("Summary:")) { //$NON-NLS-1$
                    line = "Summary:" + "        " + summaryText.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("Group:")) { //$NON-NLS-1$
                    line = "Group:" + "          " + groupCombo.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("License:")) { //$NON-NLS-1$
                    line = "License:" + "        " + licenseText.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("URL:")) { //$NON-NLS-1$
                    line = "URL:" + "            " + urlText.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (line.startsWith("Source0:")) { //$NON-NLS-1$
                    line = "Source0:" + "        " + source0Text.getText(); //$NON-NLS-1$ //$NON-NLS-2$
                }
                content += line + '\n';
            }
        } catch (IOException e1) {
            SpecfileLog.logError(e1);
        }
        return content;
    }

    /**
     * Tests if the current workbench selection is a suitable container to use.
     */
    private void initialize() {
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
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
                projectText.setText(container.getFullPath().toString());
            }
        }
        setDefaultValues();
    }

    /**
     * Uses the standard container selection dialog to choose the new value for
     * the container field.
     */
    private void handleBrowse() {
        ContainerSelectionDialog dialog = new ContainerSelectionDialog(
                getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
                Messages.SpecfileNewWizardPage_21);
        if (dialog.open() == Window.OK) {
            Object[] result = dialog.getResult();
            if (result.length == 1) {
                projectText.setText(((Path) result[0]).toString());
            }
        }
    }

    /**
     * Ensures that both text fields are set.
     */
    private void dialogChanged() {
        IResource container = ResourcesPlugin.getWorkspace().getRoot()
                .findMember(new Path(getProjectName()));
        String fileName = getFileName();
        if (getProjectName().length() == 0) {
            updateStatus(Messages.SpecfileNewWizardPage_22);
            return;
        }
        if (container == null
                || (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
            updateStatus(Messages.SpecfileNewWizardPage_23);
            return;
        }
        if (!container.isAccessible()) {
            updateStatus(Messages.SpecfileNewWizardPage_24);
            return;
        }
        if (fileName.length() == 0) {
            updateStatus(Messages.SpecfileNewWizardPage_25);
            return;
        }

        /*
         * Current RPM doc content (4.4.2): Names must not include whitespace
         * and may include a hyphen '-' (unlike version and releasetags). Names
         * should not include any numeric operators ('<', '>','=') as future
         * versions of rpm may need to reserve characters other than '-'.
         */
        String packageName = nameText.getText();
        if (packageName.indexOf(' ') != -1 || packageName.indexOf('<') != -1
                || packageName.indexOf('>') != -1 || packageName.indexOf('=') != -1) {
            updateStatus(Messages.SpecfileNewWizardPage_26
                    + Messages.SpecfileNewWizardPage_27);
            return;
        }

        if (versionText.getText().indexOf("-") > -1) { //$NON-NLS-1$
            updateStatus(Messages.SpecfileNewWizardPage_28);
            return;
        }

        updateStatus(null);
    }

    protected void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    private void setDefaultValues() {
        nameText.setText(NAME);
        versionText.setText(VERSION);
        summaryText.setText(SUMMARY);
        groupCombo.setText(GROUP);
        licenseText.setText(LICENSE);
        urlText.setText(URL);
        source0Text.setText(SOURCE0);
    }

    private void populateTemplateCombo(Combo templateCombo)
            throws CoreException {
        // get a list of all files in a directory
        File dir = new File("/etc/rpmdevtools"); //$NON-NLS-1$
        String[] files = dir.list();
        if (dir.exists()) {
            String templateCSV = ""; //$NON-NLS-1$
            for (String file : files) {
                if (file.startsWith("spectemplate-")) { //$NON-NLS-1$
                    templateCSV += file.split("-", 2)[1].replaceAll("\\.spec", //$NON-NLS-1$ //$NON-NLS-2$
                            "") //$NON-NLS-1$
                            + ","; //$NON-NLS-1$
                }
            }
            String[] templates = templateCSV.split(","); //$NON-NLS-1$
            for (String template : templates) {
                templateCombo.add(template);
            }
            templateCombo.setText(selectedTemplate);
        } else {
            throwCoreException(Messages.SpecfileNewWizardPage_29);
        }
    }

    private void populateGroupCombo(Combo groupsCombo) {
        List<String> rpmGroups = Activator.getDefault().getRpmGroups();
        for (String rpmGroup : rpmGroups) {
            groupsCombo.add(rpmGroup);
        }
    }

    private BufferedInputStream runRpmdevNewSpec(String template) {
        BufferedInputStream in = null;
        // Here we assuming that the rpmdevtools package is installed.
        try {
            in = org.eclipse.linuxtools.rpm.core.utils.Utils
                    .runCommandToInputStream(
                            "rpmdev-newspec", "-o", "-", "-t", template); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        } catch (IOException e) {
            // FIXME: rpmdev-newspec is not in the system $PATH, what should we
            // do here?.
            SpecfileLog.logError(e);
        }
        return in;
    }

    private void throwCoreException(String message) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                IStatus.OK, message, null);
        throw new CoreException(status);
    }

    public Text getNameText() {
        return nameText;
    }

    public Text getProjectText() {
        return projectText;
    }

    public Text getVersionText() {
        return versionText;
    }

    public Text getUrlText() {
        return urlText;
    }

    public Text getLicenseText() {
        return licenseText;
    }

    public Text getSourceText() {
        return source0Text;
    }

    public Text getSummaryText() {
        return summaryText;
    }

}