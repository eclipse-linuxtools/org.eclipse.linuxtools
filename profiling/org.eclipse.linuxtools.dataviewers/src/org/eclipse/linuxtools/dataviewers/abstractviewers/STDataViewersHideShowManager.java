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

import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.dataviewers.listeners.STColumnSizeListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Item;

/**
 * This class is used to handle the width and visibility of all columns of a {@link AbstractSTViewer}. Width and
 * visibility of these columns are stored in a dialogSetting.
 */
public class STDataViewersHideShowManager {

    public static final int STATE_SHOWN = 1;
    public static final int STATE_HIDDEN = 0;

    private final AbstractSTViewer stViewer;
    private final int[] defaultColumnsWidth;
    private final int[] columnsWidth;
    private final int[] columnsState;

    private final HashMap<Item, STColumnSizeListener> columnsSizeListener = new HashMap<>();

    /**
     * Creates a new instance of STDataViewersHideShowManager.
     */
    public STDataViewersHideShowManager(AbstractSTViewer stViewer) {
        this.stViewer = stViewer;
        Item[] columns = stViewer.getColumns();
        this.columnsWidth = new int[columns.length];
        this.columnsState = new int[columns.length];
        this.defaultColumnsWidth = new int[columns.length];
        for (int i = 0; i < columns.length; i++) {
            ISTDataViewersField field = (ISTDataViewersField) columns[i].getData();
            columnsWidth[i] = stViewer.getColumnWidth(columns[i]);
            columnsState[i] = field.isShowingByDefault() ? STATE_SHOWN : STATE_HIDDEN;
            defaultColumnsWidth[i] = field.getPreferredWidth();
            STColumnSizeListener l = new STColumnSizeListener(this);
            columnsSizeListener.put(columns[i], l);
            columns[i].addListener(SWT.Resize, l);
            columns[i].addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    Item column = (Item) e.widget;
                    column.removeListener(SWT.Resize, columnsSizeListener.get(column));
                }
            });
        }
    }

    /**
     * Saves the column width and visibility status in the given dialogSettings
     */
    public void saveState(IDialogSettings dialogSettings) {
        // delete old settings and save new ones
        IDialogSettings settings = dialogSettings.addNewSection(STDataViewersSettings.TAG_SECTION_HIDESHOW);
        for (int i = 0; i < columnsWidth.length; i++) {
            settings.put(STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_WIDTH_ + i, columnsWidth[i]);
            settings.put(STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_STATE_ + i, columnsState[i]);
        }
    }

    /**
     * Restores the columns width and visibility using the given dialogSettings
     * @param dialogSettings
     */
    public void restoreState(IDialogSettings dialogSettings) {
        if (dialogSettings == null) {
            // no settings section
            resetState();
            return;
        }
        IDialogSettings settings = dialogSettings.getSection(STDataViewersSettings.TAG_SECTION_HIDESHOW);
        if (settings == null) {
            // no settings saved
            resetState();
            return;
        }
        try {
            for (int i = 0; i < columnsWidth.length; i++) {
                String width = settings.get(STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_WIDTH_ + i);
                if (width == null) {
                    // no width data
                    resetState();
                    return;
                }
                columnsWidth[i] = Integer.parseInt(width);
                String state = settings.get(STDataViewersSettings.TAG_HIDE_SHOW_COLUMN_STATE_ + i);
                if (state == null) {
                    // no state data
                    resetState();
                    return;
                }
                columnsState[i] = Integer.parseInt(state);
            }
        } catch (NumberFormatException nfe) {
            // invalid entry
            resetState();
            return;
        }
    }

    /**
     * It restores the original columns width and visibility
     */
    private void resetState() {
        Item[] columns = stViewer.getColumns();
        for (int i = 0; i < columns.length; i++) {
            ISTDataViewersField field = (ISTDataViewersField) columns[i].getData();
            columnsState[i] = field.isShowingByDefault() ? STATE_SHOWN : STATE_HIDDEN;
            columnsWidth[i] = defaultColumnsWidth[i];
        }
    }

    /**
     * Sets the column width
     * @param index
     *            index of column
     * @param width
     */
    public void setWidth(int index, int width) {
        if (columnsState[index] != STATE_HIDDEN) {
            columnsWidth[index] = width;
        }
        // ignore if this column is set to hidden
    }

    /**
     * Sets the state of column
     * @param index
     *            index of the column
     * @param state
     *            one of {@link #STATE_SHOWN} or {@link #STATE_HIDDEN}
     */
    public void setState(int index, int state) {
        columnsState[index] = state;
    }

    /**
     * Gets the column width
     * @param index
     *            index of the column
     * @return a column width
     */
    public int getWidth(int index) {
        return columnsWidth[index];
    }

    /**
     * Gets the column state.
     * @param index
     *            of the column
     * @return one of {@link #STATE_SHOWN} or {@link #STATE_HIDDEN}
     */
    public int getState(int index) {
        return columnsState[index];
    }

    /**
     * Gets the width of all columns
     * @return an array of width
     */
    public int[] getColumnsWidth() {
        return columnsWidth;
    }

    /**
     * Gets the status ({@link #STATE_HIDDEN} or {@link #STATE_SHOWN}) of all columns.
     * @return an array of status
     */
    public int[] getColumnsState() {
        return columnsState;
    }

    /**
     * Updates the columns width and status
     * @since 5.0
     */
    public void updateColumns() {
        Item[] columns = stViewer.getColumns();
        for (int i = columns.length; i-- > 0;) {
            Item column = columns[i];
            if (getState(i) == STDataViewersHideShowManager.STATE_HIDDEN) {
                stViewer.setColumnWidth(column, 0);
                stViewer.setColumnResizable(column, false);
            } else {
                stViewer.setColumnWidth(column, getWidth(i));
                stViewer.setColumnResizable(column, true);
            }
        }
    }

    /**
     * Gets the STViewer hooked to this Hide/Show Manager
     * @return AbstractSTViewer
     */
    public AbstractSTViewer getSTViewer() {
        return stViewer;
    }

}
