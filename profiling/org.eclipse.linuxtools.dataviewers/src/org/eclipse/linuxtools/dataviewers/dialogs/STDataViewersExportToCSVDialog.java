/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.dialogs;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersCSVExporter;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersCSVExporterConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SaveAsDialog;

public class STDataViewersExportToCSVDialog extends Dialog {

    private Text previewText = null;

    private Text outputFile = null;

    private Text separatorText = null;

    private Text childLinkText = null;

    private Text noChildLinkText = null;

    private Text childMarkerText = null;

    private Text lastChildMarkerText = null;

    private Text leafMarkerText = null;

    private Text nodeMarkerText = null;

    private Label childLinkLabel = null;

    private Label noChildLinkLabel = null;

    private Label childMarkerLabel = null;

    private Label lastChildMarkerLabel = null;

    private Label leafMarkerLabel = null;

    private Label nodeMarkerLabel = null;

    private Button expandAllButton = null;

    private Button showHiddenColumnsButton = null;

    private Button exportTreePrefixButton = null;

    private Button restoreDefaults = null;

    private STDataViewersCSVExporter exporter = null;

    private ModifyListener updatePreviewModifyListener = new ModifyListener() {
        @Override
		public void modifyText(ModifyEvent e) {
            updatePreview();
        }
    };

    private SelectionListener updatePreviewSelectionListener = new SelectionListener() {
        @Override
		public void widgetDefaultSelected(SelectionEvent e) {
            widgetSelected(e);
        }

        @Override
		public void widgetSelected(SelectionEvent e) {
            updatePreview();
        }
    };

    public STDataViewersExportToCSVDialog(Shell parentShell, STDataViewersCSVExporter exporter) {
        super(parentShell);

        this.setShellStyle(this.getShellStyle() | SWT.RESIZE);

        this.exporter = exporter;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Export to CSV");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        composite.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(1, false);
        composite.setLayout(layout);

        if (exporter == null) {
            return composite;
        }

        initializeDialogUnits(composite);

        createOutputFileArea(composite);
        createCSVConfigArea(composite);

        if (exporter.isTreeViewerExporter()) {
            createTreePrefixConfigArea(composite);
        }

        createCSVPreviewArea(composite);
        createRestoreDefaultsButton(composite);

        createSeparatorLine(composite);
        Dialog.applyDialogFont(composite);

        setDefaultWidgetsValues();

        return composite;
    }

    private void createOutputFileArea(Composite composite) {
        Group outputGroup = new Group(composite, SWT.NONE);
        outputGroup.setText("Output file");
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        outputGroup.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(2, false);
        outputGroup.setLayout(layout);

        Label outputLabel = new Label(outputGroup, SWT.NONE);
        outputLabel.setText("Output file:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        outputLabel.setLayoutData(layoutData);

        outputFile = new Text(outputGroup, SWT.BORDER);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        outputFile.setLayoutData(layoutData);

        Composite browseComposite = new Composite(outputGroup, SWT.NONE);
        layoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 2, 1);
        browseComposite.setLayoutData(layoutData);
        layout = new GridLayout(2, false);
        browseComposite.setLayout(layout);

        Button browseOutputButton = new Button(browseComposite, SWT.PUSH);
        browseOutputButton.setText("File System...");
        browseOutputButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                handleBrowse();
            }
        });
        layoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
        browseOutputButton.setLayoutData(layoutData);

        Button browseOutputInWorkspaceButton = new Button(browseComposite, SWT.PUSH);
        browseOutputInWorkspaceButton.setText("Workspace...");
        browseOutputInWorkspaceButton.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent e) {
                handleBrowseWorkspace();
            }
        });
        layoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
        browseOutputInWorkspaceButton.setLayoutData(layoutData);
    }

    private void createCSVConfigArea(Composite composite) {
        Group configGroup = new Group(composite, SWT.NONE);
        configGroup.setText("CSV Configuration");
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        configGroup.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(2, false);
        configGroup.setLayout(layout);

        Label separatorLabel = new Label(configGroup, SWT.NONE);
        separatorLabel.setText("CSV Separator:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        separatorLabel.setLayoutData(layoutData);

        separatorText = new Text(configGroup, SWT.BORDER);
        separatorText.setFont(JFaceResources.getTextFont());
        separatorText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        separatorText.setLayoutData(layoutData);

        if (exporter.isTreeViewerExporter()) {
            expandAllButton = new Button(configGroup, SWT.CHECK);
            expandAllButton.setText("Expand All");
            layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
            expandAllButton.setLayoutData(layoutData);
        } else {
            new Label(configGroup, SWT.NONE).setText("          ");
        }

        showHiddenColumnsButton = new Button(configGroup, SWT.CHECK);
        showHiddenColumnsButton.setText("Export Hidden Columns");
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        showHiddenColumnsButton.setLayoutData(layoutData);
    }

    private void createTreePrefixConfigArea(Composite composite) {
        Group prefixConfigGroup = new Group(composite, SWT.NONE);
        prefixConfigGroup.setText("Tree ascii-art drawing");
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        prefixConfigGroup.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(5, false);
        prefixConfigGroup.setLayout(layout);

        exportTreePrefixButton = new Button(prefixConfigGroup, SWT.CHECK);
        exportTreePrefixButton.setText("Export ascii-art tree");
        exportTreePrefixButton.addSelectionListener(updatePreviewSelectionListener);
        exportTreePrefixButton.addSelectionListener(new SelectionListener() {
            @Override
			public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
			public void widgetSelected(SelectionEvent e) {
                boolean enabled = exportTreePrefixButton.getSelection();
                enableTreePrefixText(enabled);
            }
        });
        layoutData = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
        exportTreePrefixButton.setLayoutData(layoutData);

        Label blankLabel = new Label(prefixConfigGroup, SWT.NONE);
        blankLabel.setText("");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1);
        blankLabel.setLayoutData(layoutData);

        childLinkLabel = new Label(prefixConfigGroup, SWT.NONE);
        childLinkLabel.setText("Child Link:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        childLinkLabel.setLayoutData(layoutData);

        childLinkText = new Text(prefixConfigGroup, SWT.BORDER);
        childLinkText.setFont(JFaceResources.getTextFont());
        childLinkText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        childLinkText.setLayoutData(layoutData);

        Label separator = new Label(prefixConfigGroup, SWT.SEPARATOR | SWT.VERTICAL);
        layoutData = new GridData(SWT.CENTER, SWT.FILL, false, true, 1, 3);
        separator.setLayoutData(layoutData);

        noChildLinkLabel = new Label(prefixConfigGroup, SWT.NONE);
        noChildLinkLabel.setText("No-child Link:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        noChildLinkLabel.setLayoutData(layoutData);

        noChildLinkText = new Text(prefixConfigGroup, SWT.BORDER);
        noChildLinkText.setFont(JFaceResources.getTextFont());
        noChildLinkText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        noChildLinkText.setLayoutData(layoutData);

        childMarkerLabel = new Label(prefixConfigGroup, SWT.NONE);
        childMarkerLabel.setText("Child Marker:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        childMarkerLabel.setLayoutData(layoutData);

        childMarkerText = new Text(prefixConfigGroup, SWT.BORDER);
        childMarkerText.setFont(JFaceResources.getTextFont());
        childMarkerText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        childMarkerText.setLayoutData(layoutData);

        lastChildMarkerLabel = new Label(prefixConfigGroup, SWT.NONE);
        lastChildMarkerLabel.setText("Last child Marker:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        lastChildMarkerLabel.setLayoutData(layoutData);

        lastChildMarkerText = new Text(prefixConfigGroup, SWT.BORDER);
        lastChildMarkerText.setFont(JFaceResources.getTextFont());
        lastChildMarkerText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        lastChildMarkerText.setLayoutData(layoutData);

        leafMarkerLabel = new Label(prefixConfigGroup, SWT.NONE);
        leafMarkerLabel.setText("Leaf Marker:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        leafMarkerLabel.setLayoutData(layoutData);

        leafMarkerText = new Text(prefixConfigGroup, SWT.BORDER);
        leafMarkerText.setFont(JFaceResources.getTextFont());
        leafMarkerText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        leafMarkerText.setLayoutData(layoutData);

        nodeMarkerLabel = new Label(prefixConfigGroup, SWT.NONE);
        nodeMarkerLabel.setText("Non-leaf Marker:");
        layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        nodeMarkerLabel.setLayoutData(layoutData);

        nodeMarkerText = new Text(prefixConfigGroup, SWT.BORDER);
        nodeMarkerText.setFont(JFaceResources.getTextFont());
        nodeMarkerText.addModifyListener(updatePreviewModifyListener);
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        nodeMarkerText.setLayoutData(layoutData);
    }

    private void createCSVPreviewArea(Composite composite) {
        Group previewGroup = new Group(composite, SWT.NONE);
        previewGroup.setText("Preview");
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        previewGroup.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(1, false);
        previewGroup.setLayout(layout);

        previewText = new Text(previewGroup, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        previewText.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
        previewText.setFont(JFaceResources.getTextFont());
        layoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 8);
        previewText.setLayoutData(layoutData);
    }

    private void createRestoreDefaultsButton(Composite composite) {
        restoreDefaults = new Button(composite, SWT.NONE);
        restoreDefaults.setText("Restore Defaults");
        restoreDefaults.addSelectionListener(new SelectionListener() {
            @Override
			public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

            @Override
			public void widgetSelected(SelectionEvent e) {
                restoreDefaults();
            }
        });
        GridData layoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
        restoreDefaults.setLayoutData(layoutData);
    }

    private void restoreDefaults() {
        outputFile.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_OUTPUT_FILE_PATH);
        separatorText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_SEPARATOR);

        showHiddenColumnsButton.setSelection(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_SHOW_HIDDEN_COLUMNS);

        if (exporter.isTreeViewerExporter()) {
            expandAllButton.setSelection(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_EXPAND_ALL);
            childMarkerText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_CHILD_MARKER);
            lastChildMarkerText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_LAST_CHILD_MARKER);
            childLinkText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_CHILD_LINK);
            noChildLinkText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_NO_CHILD_LINK);
            leafMarkerText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_LEAF_MARKER);
            nodeMarkerText.setText(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_NODE_MARKER);
            exportTreePrefixButton.setSelection(STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_TREE_PREFIX);
            enableTreePrefixText(exportTreePrefixButton.getSelection());
        }

        updatePreview();
    }

    private void setDefaultWidgetsValues() {
        outputFile.setText(exporter.getFilePath());

        showHiddenColumnsButton.setSelection(exporter.getShowHiddenColumns());
        separatorText.setText(inactivateSpecialChars(exporter.getCSVSeparator()));

        if (exporter.isTreeViewerExporter()) {
            expandAllButton.setSelection(exporter.getExpandAll());

            exportTreePrefixButton.setSelection(exporter.getExportTreePrefix());
            childLinkText.setText(inactivateSpecialChars(exporter.getCSVChildLink()));
            childMarkerText.setText(inactivateSpecialChars(exporter.getCSVChildMarker()));
            lastChildMarkerText.setText(inactivateSpecialChars(exporter.getCSVLastChildMarker()));
            noChildLinkText.setText(inactivateSpecialChars(exporter.getCSVNoChildLink()));
            leafMarkerText.setText(inactivateSpecialChars(exporter.getCSVLeafMarker()));
            nodeMarkerText.setText(inactivateSpecialChars(exporter.getCSVNodeMarker()));
            enableTreePrefixText(exportTreePrefixButton.getSelection());
        }

        updatePreview();
    }

    private void updatePreview() {
        if (exporter.isTreeViewerExporter()) {
            previewText.setText(createPreview(separatorText.getText(), childMarkerText.getText(),
                    lastChildMarkerText.getText(), nodeMarkerText.getText(), leafMarkerText.getText(),
                    childLinkText.getText(), noChildLinkText.getText(), exportTreePrefixButton.getSelection()));
        } else {
            previewText.setText(createPreview(separatorText.getText(), null, null, null, null, null, null, false));
        }
    }

    public String createPreview(String Separator, String childMarker, String lastChildMarker, String nodeMarker,
            String leafMarker, String childLink, String noChildLink, boolean exportTreePrefix) {
        String preview = "";

        if (exportTreePrefix) {
            preview += "Hierarchy" + Separator;
        }

        preview += "col_1" + Separator + "col_2" + "\n";

        if (exportTreePrefix) {
            preview += childMarker + nodeMarker + Separator;
        }

        preview += "A1" + Separator + "B1" + "\n";

        if (exportTreePrefix) {
            preview += childLink + lastChildMarker + leafMarker + Separator;
        }

        preview += "A11" + Separator + "B11" + "\n";

        if (exportTreePrefix) {
            preview += lastChildMarker + nodeMarker + Separator;
        }

        preview += "A2" + Separator + "B2" + "\n";

        if (exportTreePrefix) {
            preview += noChildLink + childMarker + leafMarker + Separator;
        }

        preview += "A21" + Separator + "B21" + "\n";

        if (exportTreePrefix) {
            preview += noChildLink + childMarker + nodeMarker + Separator;
        }

        preview += "A22" + Separator + "B22" + "\n";

        if (exportTreePrefix) {
            preview += noChildLink + childLink + lastChildMarker + leafMarker + Separator;
        }

        preview += "A221" + Separator + "B221" + "\n";

        if (exportTreePrefix) {
            preview += noChildLink + lastChildMarker + leafMarker + Separator;
        }

        preview += "A23" + Separator + "B23";

        return activateSpecialChars(preview);
    }

    private void enableTreePrefixText(boolean enabled) {
        childLinkText.setEnabled(enabled);
        noChildLinkText.setEnabled(enabled);
        childMarkerText.setEnabled(enabled);
        lastChildMarkerText.setEnabled(enabled);
        leafMarkerText.setEnabled(enabled);
        nodeMarkerText.setEnabled(enabled);

        childLinkLabel.setEnabled(enabled);
        noChildLinkLabel.setEnabled(enabled);
        childMarkerLabel.setEnabled(enabled);
        lastChildMarkerLabel.setEnabled(enabled);
        leafMarkerLabel.setEnabled(enabled);
        nodeMarkerLabel.setEnabled(enabled);
    }

    @Override
    protected void okPressed() {
        File f = new File(outputFile.getText());
        if (f.exists()) {
            MessageDialog dialog = new MessageDialog(this.getShell(), "Warning: file already exists", null, "File \""
                    + f.getAbsolutePath() + "\" already exists.\n" + "Overwrite it anyway?", MessageDialog.WARNING,
                    new String[] { "OK", "Cancel" }, 1);
            if (dialog.open() > 0) {
                return;
            }
        }

        if (isDirty()) {
            saveExporterSettings();
        }
        super.okPressed();
    }

    private String activateSpecialChars(String text) {
        String result = text.replace("\\t", "\t");
        result = result.replace("\\n", "\n");

        return result;
    }

    private String inactivateSpecialChars(String text) {
        String result = text.replace("\t", "\\t");
        result = result.replace("\n", "\\n");

        return result;
    }

    private void saveExporterSettings() {
        exporter.setFilePath(outputFile.getText());

        exporter.setShowHiddenColumns(showHiddenColumnsButton.getSelection());
        exporter.setCSVSeparator(activateSpecialChars(separatorText.getText()));

        if (exporter.isTreeViewerExporter()) {
            exporter.setExpandAll(expandAllButton.getSelection());
            exporter.setExportTreePrefix(exportTreePrefixButton.getSelection());
            exporter.setCSVChildLink(activateSpecialChars(childLinkText.getText()));
            exporter.setCSVChildMarker(activateSpecialChars(childMarkerText.getText()));
            exporter.setCSVLastChildMarker(activateSpecialChars(lastChildMarkerText.getText()));
            exporter.setCSVNoChildLink(activateSpecialChars(noChildLinkText.getText()));
            exporter.setCSVLeafMarker(activateSpecialChars(leafMarkerText.getText()));
            exporter.setCSVNodeMarker(activateSpecialChars(nodeMarkerText.getText()));
        }
    }

    /**
     * @return boolean
     */
    private boolean isDirty() {
        if (exporter.isTreeViewerExporter()) {
            return !(exporter.getFilePath().equals(outputFile.getText())
                    && exporter.getCSVChildLink().equals(childLinkText.getText())
                    && exporter.getCSVChildMarker().equals(childMarkerText.getText())
                    && exporter.getCSVLastChildMarker().equals(lastChildMarkerText.getText())
                    && exporter.getCSVNoChildLink().equals(noChildLinkText.getText())
                    && exporter.getCSVSeparator().equals(separatorText.getText())
                    && exporter.getCSVLeafMarker().equals(leafMarkerText.getText())
                    && exporter.getCSVNodeMarker().equals(nodeMarkerText.getText())
                    && exporter.getExpandAll() == expandAllButton.getSelection()
                    && exporter.getShowHiddenColumns() == showHiddenColumnsButton.getSelection()
                    && exporter.getExportTreePrefix() == exportTreePrefixButton.getSelection() && exporter
                        .getExpandAll() == expandAllButton.getSelection());
        } else {
            return !(exporter.getFilePath().equals(outputFile.getText())
                    && exporter.getCSVSeparator().equals(separatorText.getText()) && exporter.getShowHiddenColumns() == showHiddenColumnsButton
                    .getSelection());
        }
    }

    private void createSeparatorLine(Composite parent) {
        Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
    }

    private void handleBrowseWorkspace() {
        SaveAsDialog dialog = new SaveAsDialog(getShell());
        dialog.setTitle("Output file");
        if (dialog.open() == IDialogConstants.OK_ID) {
            IPath p = dialog.getResult();
            IWorkspace workspace = ResourcesPlugin.getWorkspace();
            IFile file = workspace.getRoot().getFile(p);
            outputFile.setText(file.getLocation().toOSString());
        }
    }

    private void handleBrowse() {
        FileDialog dialog = new FileDialog(this.getShell(), SWT.OPEN | SWT.SAVE);
        dialog.setText("Select output file");
        dialog.setFilterExtensions(new String[] { "*.csv", "*.*" });
        String t = outputFile.getText();
        File f = new File(t);
        t = f.getParent();
        dialog.setFilterPath(t);
        String s = dialog.open();
        if (s != null)
            outputFile.setText(s);
    }

}
