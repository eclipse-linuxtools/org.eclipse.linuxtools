/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.consolelog.structures;

import java.util.Arrays;

import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.editor.SimpleEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;



/**
 * A class push data to a ScriptConsole.
 * @author Ryan Morse
 */
public class ErrorTableDisplay {
    public ErrorTableDisplay(Composite parent, String[] titles) {
        this.titles = Arrays.copyOf(titles, titles.length);
        createControl(parent);
    }

    /**
     * Creates the table for displaying error messages in.
     * @param parent The container for the new error table.
     */
    private void createControl(Composite parent) {
        table = new Table(parent, SWT.SINGLE);
        table.setHeaderVisible(true);
        table.getVerticalBar().setVisible(true);
        table.setLinesVisible(true);
        table.addMouseListener(mouseListener);

        TableColumn column;
        for(String title: titles) {
            column = new TableColumn(table, SWT.NONE);
            column.setText(title);
        }

        updateColumns();
    }

    /**
     * Clears all items from the table.
     */
    public void clear() {
        table.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                table.removeAll();
            }
        });
    }

    /**
     * Adds a new row to the table with an error icon.
     * @param row The pre-divied sections of the error message.
     */
    public void addRow(final String[] row) {
        addRow(row, AbstractUIPlugin.imageDescriptorFromPlugin(ConsoleLogPlugin.PLUGIN_ID, "icons/views/error_st_obj.gif").createImage()); //$NON-NLS-1$
    }

    /**
     * Adds a new row to the table.
     * @param row The pre-divied sections of the error message.
     * @param img The image to display with the error.
     */
    private void addRow(final String[] row, final Image img) {
        table.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                item = new TableItem(table, SWT.NULL);
                for(int i=0; i<row.length; i++) {
                    item.setText(i+1, row[i]);
                }
                item.setImage(img);
                updateColumns();
            }

        });
    }

    /**
     * Updates each of the columns in the table to ensure that the entries all fit
     * as well as possible.
     */
    private void updateColumns() {
        TableColumn[] columns = table.getColumns();
        for (TableColumn column: columns) {
            column.pack();
            column.setMoveable(true);
        }
    }

    public Control getControl() {
        return table;
    }

    /**
     * Disposes of all internal references in the class. No method should be called after this.
     */
    public void dispose() {
        if(null != table && !table.isDisposed()) {
            table.removeMouseListener(mouseListener);
            table.dispose();
            table = null;
        }

        if(titles != null) {
            for(int i=0; i<titles.length; i++) {
                titles[i] = null;
            }
        }
        titles = null;

        if(null != item) {
            item.dispose();
        }
        item = null;
    }

    /**
     * MouseListener that is used to detect when the user clicks on a row in the table.
     * When clicked it will find the line number the error occured on and then set the
     * cursor location to that location in the active editor.
     */
    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseDoubleClick(MouseEvent me) {
            String location = table.getSelection()[0].getText(4);

            if(location.length() > 0) {
                int line = 0;
                if(location.indexOf(':') > 0) {
                    String[] pos = location.split(":"); //$NON-NLS-1$
                    line = Integer.parseInt(pos[0]);
                } else {
                    line = Integer.parseInt(location);
                }

                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IEditorPart ed = page.getActiveEditor();

                if(ed instanceof SimpleEditor) {
                    SimpleEditor editor = ((SimpleEditor)ed);
                    editor.selectLine(line);
                    editor.setFocus();
                }
            }
        }
    };

    private Table table;
    private String[] titles;
    private TableItem item;
}
