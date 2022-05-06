/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gprof.dialog;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.linuxtools.internal.gprof.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * This dialog box is opened when user clicks on a gmon file. it alows the user to choose the binary file who produced
 * the gmon file.
 *
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 *
 */
public class OpenGmonDialog extends Dialog {

    /* Inputs */
    private Text binText;
    private String binValue;

    /* error label */
    private Label errorLabel;

    /* validation boolean */
    private boolean binaryValid;

    private final String defaultValue;
    private final IPath gmonFile;

    /**
     * Constructor
     *
     * @param parentShell
     * @param binPath
     *            the path to a binary file.
     */
    public OpenGmonDialog(Shell parentShell, String binPath, IPath gmonFile) {
        super(parentShell);
        this.gmonFile = gmonFile;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.defaultValue = binPath;
    }

    /**
     * Gets the Binary file selected by the user
     *
     * @return a path to a binary file
     */
    public String getBinaryFile() {
        return binValue;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control composite = super.createContents(parent);
        validateBinary();
        return composite;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.getShell().setText(Messages.OpenGmonDialog_GMON_BINARY_FILE);
        Composite composite = (Composite) super.createDialogArea(parent);

        /* first line */
        Group c = new Group(composite, SWT.NONE);
        c.setText(Messages.OpenGmonDialog_BINARY_FILE);
        c.setToolTipText(Messages.OpenGmonDialog_PLEASE_ENTER_BINARY_FILE_FULL_MSG);
        GridLayout layout = new GridLayout(2, false);
        c.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        c.setLayoutData(data);

        Label binLabel = new Label(c, SWT.NONE);
        binLabel.setText(Messages.OpenGmonDialog_PLEASE_ENTER_BINARY_FILE_FULL_MSG);
        data = new GridData();
        data.horizontalSpan = 2;
        binLabel.setLayoutData(data);

        binText = new Text(c, SWT.BORDER);
        binText.setText(this.defaultValue);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        binText.setLayoutData(data);
        binText.addModifyListener(new BinaryModifyListener());

        Composite cbBin = new Composite(c, SWT.NONE);
        data = new GridData(GridData.HORIZONTAL_ALIGN_END);
        cbBin.setLayoutData(data);
        cbBin.setLayout(new GridLayout(2, true));
        Button binBrowseWorkspaceButton = new Button(cbBin, SWT.PUSH);
        binBrowseWorkspaceButton.setText(Messages.OpenGmonDialog_WORKSPACE);
		binBrowseWorkspaceButton.addSelectionListener(SelectionListener
				.widgetSelectedAdapter(e -> handleBrowseWorkspace(Messages.OpenGmonDialog_OPEN_BINARY_FILE, binText)));
        Button binBrowseFileSystemButton = new Button(cbBin, SWT.PUSH);
        binBrowseFileSystemButton.setText(Messages.OpenGmonDialog_FILE_SYSTEM);
		binBrowseFileSystemButton.addSelectionListener(SelectionListener
				.widgetSelectedAdapter(e -> handleBrowse(Messages.OpenGmonDialog_OPEN_BINARY_FILE, binText)));

        /* 2sd line */
        errorLabel = new Label(composite, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        errorLabel.setLayoutData(data);
        errorLabel.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));

        c.layout();

        return composite;
    }

    private void validateBinary() {
        binValue = binText.getText();
        IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
        try {
            binValue = mgr.performStringSubstitution(binValue, false);
        } catch (CoreException e) {
            // do nothing: never occurs
        }

        File f = new File(binValue);
        if (f.exists()) {
            binaryValid = true;
            getButton(IDialogConstants.OK_ID).setEnabled(binaryValid);
            errorLabel.setText(""); //$NON-NLS-1$
        } else {
            binaryValid = false;
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            if (!binValue.equals("")) { //$NON-NLS-1$
                errorLabel.setText("\"" + binText.getText() + "\" " + Messages.OpenGmonDialog_DOES_NOT_EXIST); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                errorLabel.setText(Messages.OpenGmonDialog_PLEASE_ENTER_BINARY_FILE);
            }
            return;
        }
    }

    private class BinaryModifyListener implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            validateBinary();
        }

    }

    private void handleBrowseWorkspace(String msg, Text text) {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setTitle(msg);
        dialog.setMessage(msg);
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
        dialog.setAllowMultiple(false);
        IContainer c = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(this.gmonFile);
        if (c != null) {
            dialog.setInitialSelection(c.getProject());
        }
        dialog.setValidator(selection -> {
		    if (selection.length != 1) {
				return Status.error(""); //$NON-NLS-1$
		    }
		    if (!(selection[0] instanceof IFile)) {
				return Status.error(""); //$NON-NLS-1$
		    }
			return Status.OK_STATUS;
		});
        if (dialog.open() == IDialogConstants.OK_ID) {
            IResource resource = (IResource) dialog.getFirstResult();
            text.setText("${resource_loc:" + resource.getFullPath() + "}"); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    private void handleBrowse(String msg, Text text) {
        FileDialog dialog = new FileDialog(this.getShell(), SWT.OPEN);
        dialog.setText(msg);
        String t = text.getText();
        IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
        try {
            t = mgr.performStringSubstitution(t, false);
        } catch (CoreException e) {
            // do nothing: never occurs
        }
        File f = new File(t);
        t = f.getParent();
        if (t == null || t.length() == 0) {
            t = this.gmonFile.removeLastSegments(1).toOSString();
        }
        dialog.setFilterPath(t);
        String s = dialog.open();
        if (s != null) {
            text.setText(s);
        }
    }
}
