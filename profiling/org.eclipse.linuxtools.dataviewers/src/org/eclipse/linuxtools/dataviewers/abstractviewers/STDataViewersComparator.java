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

import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.dataviewers.STDataViewersActivator;
import org.eclipse.swt.widgets.Item;

/**
 * This comparator is used to reorder the elements provided by the content provider.
 */
public class STDataViewersComparator extends ViewerComparator implements Comparator<Object> {

    /** Maximum number of sorters */
    public static final int MAX_DEPTH = 4;

    /** Tag for ascending direction when sorting */
    public static final int ASCENDING = 1;

    /** Tag for reverse direction when sorting */
    public static final int DESCENDING = -1;

    protected final Item[] columns;

    protected int[] priorities;

    protected int[] directions;

    /**
     * Copy Constructor
     *
     * @param other The comparator to initialize from.
     */
    public STDataViewersComparator(STDataViewersComparator other) {
        this(other.getColumns(), other.getPriorities(), other.getDirections());
    }

    /**
     * Constructor
     *
     * @param columns The columns properties of the viewer.
     */
    public STDataViewersComparator(Item[] columns) {
        this(columns, null, null);
    }

    /**
     * Constructor
     *
     * @param columns
     * @param priorities
     * @param directions
     */
    private STDataViewersComparator(Item[] columns, int[] priorities, int[] directions) {
        this.columns = columns;
        if (priorities == null || directions == null) {
            this.priorities = new int[columns.length];
            this.directions = new int[columns.length];
            resetState();
        } else {
            if (priorities.length == columns.length && directions.length == columns.length) {
                this.priorities = priorities;
                this.directions = directions;
            } else {
                STDataViewersActivator
                        .getDefault()
                        .getLog()
                        .log(new Status(IStatus.WARNING, STDataViewersActivator.PLUGIN_ID, "Invalid parameters:"
                                + " priorities and/or directions number don't match with"
                                + " viewer's columns count. Applying defaults settings."));
                resetState();
            }
        }
    }

    /**
     * Reset the priorities to the default ones
     */
    private void resetPriorites() {
        for (int i = 0; i < this.priorities.length; i++) {
            this.priorities[i] = i;
        }
    }

    /**
     * Reset the directions to the default ones
     */
    private void resetDirections() {
        for (int i = 0; i < this.directions.length; i++) {
            this.directions[i] = getField(this.columns[i]).getDefaultDirection();
        }
    }

    /**
     * Resets the directions and priorities of the sorters
     */
    public void resetState() {
        resetDirections();
        resetPriorites();
    }

    /**
     * Change the direction of the first sorter
     */
    public void reverseTopPriority() {
        directions[priorities[0]] *= -1;
    }

    /**
     * Sets the top-level sorter.
     *
     * @param column The column to make top priority.
     * @param field The field to set priority for.
     */
    public void setTopPriority(final Item column, final ISTDataViewersField field) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(column)) {
                setTopPriority(i, field);
            }
        }
    }

    /**
     * Sets the top-level sorter.
     *
     * @param priority The new top priority.
     * @param field The field to set priority for.
     */
    private void setTopPriority(final int priority, final ISTDataViewersField field) {
        if (priority < 0 || priority >= priorities.length) {
            return;
        }
        int index = -1;
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i] == priority) {
                index = i;
            }
        }
        if (index == -1) {
            resetState();
            return;
        }
        // shift the array
        for (int i = index; i > 0; i--) {
            priorities[i] = priorities[i - 1];
        }
        priorities[0] = priority;
        directions[priority] = field.getDefaultDirection();
    }

    /**
     * Changes the direction of the top-priority sorter.
     *
     * @param direction The direction of sorting - ascending or descending.
     */
    public void setTopPriorityDirection(int direction) {
        if (direction == ASCENDING || direction == DESCENDING) {
            directions[priorities[0]] = direction;
        }
    }

    /**
     * @return the direction of the top-level sorter
     */
    public int getTopPriorityDirection() {
        return directions[priorities[0]];
    }

    /**
     * Return the field at the top priority.
     *
     * @return IField
     */
    public Item getTopColumn() {
        return columns[priorities[0]];
    }

    /**
     * NOTE: defensive programming: return a copy of the array
     *
     * @return the current priorities
     */
    public int[] getPriorities() {
        int[] copy = new int[priorities.length];
        System.arraycopy(priorities, 0, copy, 0, copy.length);
        return copy;
    }

    /**
     * NOTE: defensive programming: return a copy of the array
     *
     * @return the current directions
     */
    public int[] getDirections() {
        int[] copy = new int[directions.length];
        System.arraycopy(directions, 0, copy, 0, copy.length);
        return copy;
    }

    @Override
	public int compare(Object o1, Object o2) {
        return compare(o1, o2, 0, true);
    }

    @Override
	public int compare(Viewer viewer, Object e1, Object e2) {
        return compare(e1, e2, 0, true);
    }

    /**
     * Compare obj1 and obj2 at depth. If continueSearching continue searching below depth to continue the comparison.
     *
     * @param obj1
     * @param obj2
     * @param depth
     * @param continueSearching
     * @return int
     */
    private int compare(Object obj1, Object obj2, int depth, boolean continueSearching) {
        if (depth >= priorities.length) {
            return 0;
        }

        int column = priorities[depth];
        ISTDataViewersField property = getField(columns[column]);

        int result;
        if (directions[column] >= 0) {
            result = property.compare(obj1, obj2);
        } else {
            result = property.compare(obj2, obj1);
        }

        if (result == 0 && continueSearching) {
            return compare(obj1, obj2, depth + 1, continueSearching);
        }

        return result;
    }

    protected ISTDataViewersField getField(Item column) {
        return (ISTDataViewersField) column.getData();
    }

    /**
     * @return IField[] an array of fields
     */
    public Item[] getColumns() {
        return columns;
    }

    /**
     * Saves the sort order preferences of the user in the given {@link IDialogSettings}
     *
     * @param dialogSettings The setting to save into.
     */
    public void saveState(IDialogSettings dialogSettings) {
        if (dialogSettings == null) {
            return;
        }
        IDialogSettings settings = dialogSettings.getSection(STDataViewersSettings.TAG_SECTION_SORTER);
        if (settings == null) {
            settings = dialogSettings.addNewSection(STDataViewersSettings.TAG_SECTION_SORTER);
        }
        for (int i = 0; i < priorities.length; i++) {
            settings.put(STDataViewersSettings.TAG_SORTER_PRIORITY_ + i, priorities[i]);
            settings.put(STDataViewersSettings.TAG_SORTER_DIRECTION_ + i, directions[i]);
        }
    }

    /**
     * Restore the sort order preferences of the user from the given {@link IDialogSettings}
     *
     * @param dialogSettings The settings to restore from.
     */
    public void restoreState(IDialogSettings dialogSettings) {
        if (dialogSettings == null) {
            // no settings section
            resetState();
            return;
        }
        IDialogSettings settings = dialogSettings.getSection(STDataViewersSettings.TAG_SECTION_SORTER);
        if (settings == null) {
            // no settings saved
            resetState();
            return;
        }
        try {
            for (int i = 0; i < priorities.length; i++) {
                String priority = settings.get(STDataViewersSettings.TAG_SORTER_PRIORITY_ + i);
                if (priority == null) {
                    // no priority data
                    resetState();
                    return;
                }

                int colIndex = Integer.parseInt(priority);
                // Make sure it is not old data from a different sized array
                if (colIndex < columns.length) {
                    priorities[i] = colIndex;
                } else {
                    // data from a different sized array
                    resetState();
                    return;
                }
                String direction = settings.get(STDataViewersSettings.TAG_SORTER_DIRECTION_ + i);
                if (direction == null) {
                    // no direction data
                    resetState();
                    return;
                }
                directions[i] = Integer.parseInt(direction);
            }
        } catch (NumberFormatException e) {
            // invalid entry
            resetState();
            return;
        }
    }

}
