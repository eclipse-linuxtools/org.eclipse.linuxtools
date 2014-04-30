/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.gcov.dialog;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.linuxtools.binutils.utils.STSymbolManager;
import org.eclipse.linuxtools.internal.gcov.Activator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * This dialog box is opened when user clicks on a gcno/gcda file. it allows the user to choose the binary file who
 * produced the gcno/gcda file.
 * @author Xavier Raynaud <xavier.raynaud@st.com>
 */
public class OpenGCDialog extends Dialog {

    /* Inputs */
    private Text binText;
    private String binValue;

    private boolean openCoverageSummary = true;

    /* error label */
    private Label errorLabel;

    /* validation boolean */
    private boolean binaryValid;

    private final String defaultValue;
    private final IPath gcFile;

    /**
     * Constructor
     * @param parentShell
     * @param binPath
     *            the path to a binary file.
     */
    public OpenGCDialog(Shell parentShell, String binPath, IPath gcFile) {
        super(parentShell);
        this.gcFile = gcFile;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.defaultValue = binPath;
    }

    /**
     * Gets the Binary file selected by the user
     * @return a path to a binary file
     */
    public String getBinaryFile() {
        return binValue;
    }

    /**
     * Gets whether the user wants a complete coverage result, or a result specific file to the given gcFile.
     */
    public boolean isCompleteCoverageResultWanted() {
        return openCoverageSummary;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control composite = super.createContents(parent);
        validateBinary();
        return composite;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.getShell().setText(Messages.OpenGCDialog_open_results);
        Composite composite = (Composite) super.createDialogArea(parent);

        /* first line */
        Group c = new Group(composite, SWT.NONE);
        c.setText(Messages.OpenGCDialog_bin_group_header);
        c.setToolTipText(Messages.OpenGCDialog_bin_group_tooltip);
        GridLayout layout = new GridLayout(2, false);
        c.setLayout(layout);
        GridData data = new GridData(GridData.FILL_BOTH);
        c.setLayoutData(data);

        Label binLabel = new Label(c, SWT.NONE);
        binLabel.setText(Messages.OpenGCDialog_bin_group_label);
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
        binBrowseWorkspaceButton.setText(Messages.OpenGCDialog_bin_browser_button_text);
        binBrowseWorkspaceButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent sev) {
                handleBrowseWorkspace(Messages.OpenGCDialog_bin_browser_handler_text, binText);
            }
        });
        Button binBrowseFileSystemButton = new Button(cbBin, SWT.PUSH);
        binBrowseFileSystemButton.setText(Messages.OpenGCDialog_bin_browser_fs_button_text);
        binBrowseFileSystemButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent sev) {
                handleBrowse(Messages.OpenGCDialog_bin_browser_handler_text, binText);
            }
        });

        Group covMode = new Group(composite, SWT.NONE);
        covMode.setText(Messages.OpenGCDialog_coverage_mode_header);
        covMode.setToolTipText(Messages.OpenGCDialog_coverage_mode_tooltip);
        GridData covModeData = new GridData(GridData.FILL_BOTH);
        covMode.setLayoutData(covModeData);
        covMode.setLayout(new GridLayout());
        Button openThisFileOnlyButton = new Button(covMode, SWT.RADIO);
        openThisFileOnlyButton.setLayoutData(new GridData());
        final Button openCoverageSummaryButton = new Button(covMode, SWT.RADIO);
        openCoverageSummaryButton.setLayoutData(new GridData());
        String cFile = gcFile.removeFileExtension().lastSegment() + ".c"; //$NON-NLS-1$

        openThisFileOnlyButton.setText(NLS.bind(Messages.OpenGCDialog_open_file_button_text, cFile));
        openCoverageSummaryButton.setText(Messages.OpenGCDialog_summ_button_text);

        openCoverageSummaryButton.setSelection(true);

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent sev) {
                openCoverageSummary = openCoverageSummaryButton.getSelection();
            }
        };
        openCoverageSummaryButton.addSelectionListener(sa);
        openThisFileOnlyButton.addSelectionListener(sa);

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
        } catch (CoreException _) {
            // do nothing: never occurs
        }

        File f = new File(binValue);
        if (f.exists()) {
            IBinaryObject binary = STSymbolManager.sharedInstance.getBinaryObject(new Path(binValue));
            if (binary == null) {
                MessageDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                        Messages.OpenGCDialog_invalid_bin_error_title,
                        NLS.bind(Messages.OpenGCDialog_invalid_bin_error_message, binText.getText()));
                return;
            }
            binaryValid = true;
            getButton(IDialogConstants.OK_ID).setEnabled(binaryValid);
            errorLabel.setText(""); //$NON-NLS-1$
        } else {
            binaryValid = false;
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            if (!binValue.isEmpty()) {
                errorLabel.setText(NLS.bind(Messages.OpenGCDialog_bin_dne_error_label, binText.getText()));
            } else {
                errorLabel.setText(Messages.OpenGCDialog_no_bin_error_label);
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

    protected void handleBrowseWorkspace(String msg, Text text) {
        ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(),
                new WorkbenchContentProvider());
        dialog.setTitle(msg);
        dialog.setMessage(msg);
        dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
        dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
        dialog.setAllowMultiple(false);
        IContainer c = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(this.gcFile);
        if (c != null)
            dialog.setInitialSelection(c.getProject());
        dialog.setValidator(new ISelectionStatusValidator() {
            @Override
            public IStatus validate(Object[] selection) {
                if (selection.length != 1) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                }
                if (!(selection[0] instanceof IFile)) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
                }
                return new Status(IStatus.OK, Activator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
            }
        });
        if (dialog.open() == IDialogConstants.OK_ID) {
            IResource resource = (IResource) dialog.getFirstResult();
            text.setText("${resource_loc:" + resource.getFullPath() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    protected void handleBrowse(String msg, Text text) {
        FileDialog dialog = new FileDialog(this.getShell(), SWT.OPEN);
        dialog.setText(msg);
        String t = text.getText();
        IStringVariableManager mgr = VariablesPlugin.getDefault().getStringVariableManager();
        try {
            t = mgr.performStringSubstitution(t, false);
        } catch (CoreException _) {
            // do nothing: never occurs
        }
        File f = new File(t);
        t = f.getParent();
        if (t == null || t.length() == 0) {
            t = this.gcFile.removeLastSegments(1).toOSString();
        }
        dialog.setFilterPath(t);
        String s = dialog.open();
        if (s != null)
            text.setText(s);
    }
}
