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
package org.eclipse.linuxtools.dataviewers.abstractviewers;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;

/**
 * This comparator is used to export the data of a STViewer to CSV format file
 */

public class STDataViewersCSVExporter {

    private final AbstractSTViewer stViewer;

    private PrintStream ps = null;

    private ISTDataViewersField[] fields = null;

    private List<Object> expandedElts = null;

    private Comparator<Object> comparator;

    private Object input = null;

    private IContentProvider contentProvider = null;

    private IDialogSettings state = null;

    private IProgressMonitor monitor;

    private String filePath = null;

    private String csvSeparator = null;

    private String csvChildMarker = null;

    private String csvLastChildMarker = null;

    private String csvChildLink = null;

    private String csvNoChildLink = null;

    private String csvLeafMarker = null;

    private String csvNodeMarker = null;

    private boolean expandAll = false;

    private boolean showHiddenColumns = false;

    private boolean exportTreePrefix = false;

    /*
     * It creates a new instance of exporter
     *
     * @param stViewer
     */
    public STDataViewersCSVExporter(AbstractSTViewer stViewer) {
        this.stViewer = stViewer;
        this.ps = System.out;
        this.state = stViewer.getViewerSettings()
                .getSection(STDataViewersCSVExporterConstants.TAG_SECTION_CSV_EXPORTER);
        restoreState();
    }

    /*
     * Gets from the .setting the export parameters
     */
    public void restoreState() {
        if (state == null) {
            resetState();
            return;
        }

        try {
            filePath = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_OUTPUT_FILE_PATH);
            if (filePath == null) {
                resetState();
                return;
            }

            csvSeparator = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_SEPARATOR);
            if (csvSeparator == null) {
                resetState();
                return;
            }

            csvChildMarker = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_CHILD_MARKER);
            if (csvChildMarker == null) {
                resetState();
                return;
            }

            csvLastChildMarker = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_LAST_CHILD_MARKER);
            if (csvLastChildMarker == null) {
                resetState();
                return;
            }

            csvChildLink = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_CHILD_LINK);
            if (csvChildLink == null) {
                resetState();
                return;
            }

            csvNoChildLink = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_NO_CHILD_LINK);
            if (csvNoChildLink == null) {
                resetState();
                return;
            }

            csvLeafMarker = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_LEAF_MARKER);
            if (csvLeafMarker == null) {
                resetState();
                return;
            }

            csvNodeMarker = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_NODE_MARKER);
            if (csvNodeMarker == null) {
                resetState();
                return;
            }

            String temp = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_EXPAND_ALL);
            if (temp == null) {
                resetState();
                return;
            } else {
                expandAll = Boolean.parseBoolean(temp);
            }

            temp = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_SHOW_HIDDEN_COLUMNS);
            if (temp == null) {
                resetState();
                return;
            } else {
                showHiddenColumns = Boolean.parseBoolean(temp);
            }

            if (isTreeViewerExporter()) {
                temp = state.get(STDataViewersCSVExporterConstants.TAG_EXPORTER_TREE_PREFIX);
                if (temp == null) {
                    resetState();
                    return;
                } else {
                    exportTreePrefix = Boolean.parseBoolean(temp);
                }
            } else {
                exportTreePrefix = false;
            }

        } catch (NumberFormatException nfe) {
            IStatus s = new Status(IStatus.WARNING, STDataViewersActivator.PLUGIN_ID,
                    "Invalid parameter, resetting configuration!\n" + nfe.getMessage(), nfe);
            logStatus(s);
            resetState();
        }

    }

    /*
     * Save into the .setting the export parameters
     */
    public void saveState() {
        if (state == null) {
            state = stViewer.getViewerSettings().addNewSection(
                    STDataViewersCSVExporterConstants.TAG_SECTION_CSV_EXPORTER);
        }

        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_OUTPUT_FILE_PATH, filePath);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_SEPARATOR, csvSeparator);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_CHILD_MARKER, csvChildMarker);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_LAST_CHILD_MARKER, csvLastChildMarker);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_CHILD_LINK, csvChildLink);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_NO_CHILD_LINK, csvNoChildLink);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_LEAF_MARKER, csvLeafMarker);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_NODE_MARKER, csvNodeMarker);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_EXPAND_ALL, expandAll);
        state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_SHOW_HIDDEN_COLUMNS, showHiddenColumns);
        if (isTreeViewerExporter()) {
            state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_TREE_PREFIX, exportTreePrefix);
        } else {
            state.put(STDataViewersCSVExporterConstants.TAG_EXPORTER_TREE_PREFIX, false);
        }
    }

    /*
     * Sets to default the parameters used during the export
     */
    public void resetState() {
        filePath = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_OUTPUT_FILE_PATH;
        csvSeparator = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_SEPARATOR;
        csvChildMarker = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_CHILD_MARKER;
        csvLastChildMarker = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_LAST_CHILD_MARKER;
        csvChildLink = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_CHILD_LINK;
        csvNoChildLink = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_NO_CHILD_LINK;
        csvLeafMarker = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_LEAF_MARKER;
        csvNodeMarker = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_NODE_MARKER;
        expandAll = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_EXPAND_ALL;
        showHiddenColumns = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_SHOW_HIDDEN_COLUMNS;

        if (isTreeViewerExporter()) {
            exportTreePrefix = STDataViewersCSVExporterConstants.DEFAULT_EXPORTER_TREE_PREFIX;
        } else {
            exportTreePrefix = false;
        }
    }

    private void logStatus(IStatus s) {
        STDataViewersActivator.getDefault().getLog().log(s);
    }

    public void exportTo(String filePath) {
        setFilePath(filePath);
        export();
    }

    public void exportTo(String filePath, IProgressMonitor monitor) {
        setFilePath(filePath);
        export(monitor);
    }

    public void export() {
        export(new NullProgressMonitor());
    }

    public void export(IProgressMonitor monitor) {

        // monitoring
        this.monitor = monitor;

        monitor.beginTask("Exporting to CSV (file: " + getFilePath() + ")", IProgressMonitor.UNKNOWN);

        // save this configuration
        saveState();

        // init the printStream
        initPrintStream(filePath);

        // get the viewer's parameters
        monitor.subTask("collecting viewer's parameters...");
        collectViewerParameters();

        // start dumping to csv file
        dumpColumnHeaders();

        if (input != null) {
            if (contentProvider instanceof ITreeContentProvider) {
                ITreeContentProvider treeContentProvider = (ITreeContentProvider) contentProvider;
                Object[] topElements = treeContentProvider.getElements(input);

                if (topElements != null) {

                    // monitoring
                    int work = topElements.length;
                    for (Object element : topElements) {
                        if (treeContentProvider.hasChildren(element)) {
                            work += treeContentProvider.getChildren(element).length;
                        }
                    }
                    monitor.beginTask("Exporting to CSV (file: " + getFilePath() + ")", work);
                    monitor.subTask("exporting tree data...");

                    // monitoring stuff
                    int tempWork = 0;
                    int workFloor = topElements.length / 100;
                    if (workFloor == 0) {
                        workFloor = 1;
                    }

                    // exporting tree
                    Arrays.sort(topElements, comparator);
                    for (int i = 0; i < topElements.length; i++) {
                        dumpTreeData(treeContentProvider, topElements[i], "", i == topElements.length - 1, true);

                        // monitoring
                        tempWork++;
                        if (tempWork >= workFloor) {
                            if (monitor.isCanceled()) {
                                ps.close();
                                return;
                            }
                            monitor.worked(tempWork);
                            tempWork = 0;
                        }
                    }

                    // monitoring
                    monitor.worked(tempWork);
                }

            } else if (contentProvider instanceof IStructuredContentProvider) {
                IStructuredContentProvider structContentProvider = (IStructuredContentProvider) contentProvider;
                Object[] topElements = structContentProvider.getElements(input);

                if (topElements != null) {

                    // monitoring
                    monitor.beginTask("Exporting to CSV (file: " + getFilePath() + ")", topElements.length);
                    monitor.subTask("exporting table data...");

                    // monitoring stuff
                    int tempWork = 0;
                    int workFloor = topElements.length / 100;
                    if (workFloor == 0) {
                        workFloor = 1;
                    }

                    // exporting table
                    Arrays.sort(topElements, comparator);
                    for (int n = 0; n < topElements.length; n++) {
                        if (n < topElements.length - 1) {
                            dumpNodeData(topElements[n], csvChildMarker);
                        } else {
                            dumpNodeData(topElements[n], csvLastChildMarker);
                        }

                        // monitoring
                        tempWork++;
                        if (tempWork >= workFloor) {
                            if (monitor.isCanceled()) {
                                ps.close();
                                return;
                            }
                            monitor.worked(tempWork);
                            tempWork = 0;
                        }
                    }

                    // monitoring
                    monitor.worked(tempWork);
                }
            }
        }

        // close the stream
        ps.close();

        // end monitoring
        monitor.done();

        IFile c = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(getFilePath()));
        if (c != null) {
            try {
                c.refreshLocal(1, new NullProgressMonitor());
            } catch (CoreException e) {
                STDataViewersActivator.getDefault().getLog().log(e.getStatus());
            }
        }

    }

    private void initPrintStream(String filePath) {
        try {
            File outputFile = new File(filePath);

            if (outputFile.exists())
                outputFile.delete();

            outputFile.createNewFile();

            this.ps = new PrintStream(outputFile);

        } catch (IOException e) {
            Status s = new Status(IStatus.ERROR, STDataViewersActivator.PLUGIN_ID,
                    "Invalid file! Dumping to stdout...\n" + e.getMessage(), e);
            logStatus(s);
            this.ps = System.out;
        } catch (NullPointerException e) {
            Status s = new Status(IStatus.ERROR, STDataViewersActivator.PLUGIN_ID,
                    "File has not been set! Dumping to stdout...\n" + e.getMessage(), e);
            logStatus(s);
            this.ps = System.out;
        }
    }

    private void collectViewerParameters() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
			public void run() {
                // getting columns with the right order
                Item[] unOrdColumns = stViewer.getColumns();
                Item[] columns = new Item[unOrdColumns.length];
                int[] columnOrder = stViewer.getColumnOrder();
                for (int i = 0; i < unOrdColumns.length; i++) {
                    columns[i] = unOrdColumns[columnOrder[i]];
                }

                // getting fields mapping
                // (before removing hidden columns)
                Map<Item, ISTDataViewersField> fieldsMap = new HashMap<>();
                for (Item column : columns) {
                    fieldsMap.put(column, (ISTDataViewersField) column.getData());
                }

                // creating a GUI-thread-independent comparator
                comparator = new CSVDataComparator(stViewer.getTableSorter(), fieldsMap);

                // getting only visible columns, using the column order
                if (!showHiddenColumns) {
                    int[] columnsState = stViewer.getHideShowManager().getColumnsState();
                    List<Item> enabledColumns = new ArrayList<>();
                    for (int i = 0; i < columnsState.length; i++) {
                        if (columnsState[columnOrder[i]] == STDataViewersHideShowManager.STATE_SHOWN) {
                            enabledColumns.add(columns[i]);
                        }
                    }
                    columns = enabledColumns.toArray(new Item[enabledColumns.size()]);
                }

                // collecting fields from columns (ordered & shown)
                fields = new ISTDataViewersField[columns.length];
                for (int i = 0; i < columns.length; i++) {
                    fields[i] = (ISTDataViewersField) columns[i].getData();
                }

                // getting input object
                input = stViewer.getViewer().getInput();

                // getting content provider
                contentProvider = stViewer.getViewer().getContentProvider();

                // getting expanded elements if necessary
                expandedElts = new ArrayList<>();
                if (!expandAll && stViewer.getViewer() instanceof TreeViewer) {
                    TreeViewer tv = (TreeViewer) stViewer.getViewer();
                    expandedElts = Arrays.asList(tv.getExpandedElements());
                }
            }
        });
    }

    private void dumpColumnHeaders() {
        printPrefix("Hierarchy");
        for (int i = 0; i < fields.length; i++) {
            ps.print(fields[i].getColumnHeaderText());
            printSeparator(i, fields.length);
        }
    }

    private void dumpTreeData(ITreeContentProvider tcp, Object element, String prefix, boolean isLastChild,
            boolean monitoring) {
        String childMarker = isLastChild ? csvLastChildMarker : csvChildMarker;

        boolean isLeaf = !tcp.hasChildren(element);
        String leafMarker = (isLeaf ? csvLeafMarker : csvNodeMarker);
        dumpNodeData(element, prefix + childMarker + leafMarker);

        if ((expandAll || expandedElts.contains(element)) && !isLeaf) {
            Object[] children = tcp.getChildren(element);
            if (children != null) {

                // monitoring stuff
                int tempWork = 0;
                int workFloor = children.length / 100;
                if (workFloor == 0) {
                    workFloor = 1;
                }

                // exporting children
                Arrays.sort(children, comparator);
                for (int i = 0; i < children.length; i++) {
                    String prefixAdd = isLastChild ? csvNoChildLink : csvChildLink;
                    dumpTreeData(tcp, children[i], prefix + prefixAdd, i == children.length - 1, false);

                    // monitoring
                    if (monitor != null && monitoring) {
                        tempWork++;
                        if (tempWork >= workFloor) {
                            monitor.worked(tempWork);
                            tempWork = 0;
                        }
                    }
                }

                // monitoring
                if (monitor != null && monitoring && tempWork > 0) {
                    monitor.worked(tempWork);
                }
            }
        }
    }

    private void dumpNodeData(Object element, String prefix) {
        for (int i = 0; i < fields.length; i++) {
            printNode(i, prefix, getText(i, element));
            printSeparator(i, fields.length);
        }
    }

    private void printNode(int col, String prefix, String text) {
        if (col == 0) {
            printPrefix(prefix);
        }
        ps.print(text);
    }

    private void printPrefix(String prefix) {
        if (exportTreePrefix) {
            ps.print(prefix);
            ps.print(csvSeparator);
        }
    }

    private void printSeparator(int i, int length) {
        if (i == length - 1) {
            ps.print("\n");
        } else {
            ps.print(csvSeparator);
        }
    }

    private String getText(int i, Object obj) {
        return fields[i].getValue(obj);
    }

    public String getFilePath() {
        return filePath;
    }

    public String getCSVSeparator() {
        return csvSeparator;
    }

    public String getCSVChildMarker() {
        return csvChildMarker;
    }

    public String getCSVLastChildMarker() {
        return csvLastChildMarker;
    }

    public String getCSVChildLink() {
        return csvChildLink;
    }

    public String getCSVNoChildLink() {
        return csvNoChildLink;
    }

    public String getCSVLeafMarker() {
        return csvLeafMarker;
    }

    public String getCSVNodeMarker() {
        return csvNodeMarker;
    }

    public boolean getExpandAll() {
        return expandAll;
    }

    public boolean getShowHiddenColumns() {
        return showHiddenColumns;
    }

    public boolean getExportTreePrefix() {
        return exportTreePrefix;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setCSVSeparator(String separator) {
        csvSeparator = separator;
    }

    public void setCSVChildMarker(String childMarker) {
        csvChildMarker = childMarker;
    }

    public void setCSVLastChildMarker(String lastChildMarker) {
        csvLastChildMarker = lastChildMarker;
    }

    public void setCSVChildLink(String childLink) {
        csvChildLink = childLink;
    }

    public void setCSVNoChildLink(String noChildLink) {
        csvNoChildLink = noChildLink;
    }

    public void setCSVLeafMarker(String leafMarker) {
        csvLeafMarker = leafMarker;
    }

    public void setCSVNodeMarker(String nodeMarker) {
        csvNodeMarker = nodeMarker;
    }

    public void setExpandAll(boolean expandAll) {
        this.expandAll = expandAll;
    }

    public void setShowHiddenColumns(boolean showHiddenColumns) {
        this.showHiddenColumns = showHiddenColumns;
    }

    public void setExportTreePrefix(boolean exportTreePrefix) {
        if (isTreeViewerExporter()) {
            this.exportTreePrefix = exportTreePrefix;
        } else {
            this.exportTreePrefix = false;
        }
    }

    private static class CSVDataComparator extends STDataViewersComparator {

        private final Map<Item, ISTDataViewersField> fieldsMap;

        public CSVDataComparator(STDataViewersComparator other, Map<Item, ISTDataViewersField> fieldsMap) {
            super(other);
            this.fieldsMap = fieldsMap;
        }

        @Override
		protected ISTDataViewersField getField(Item column) {
            return fieldsMap.get(column);
        }

    }

    public boolean isTreeViewerExporter() {
        return stViewer instanceof AbstractSTTreeViewer;
    }
}
