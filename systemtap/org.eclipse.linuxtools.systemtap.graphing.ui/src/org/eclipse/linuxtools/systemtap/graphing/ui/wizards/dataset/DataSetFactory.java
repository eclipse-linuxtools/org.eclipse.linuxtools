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

package org.eclipse.linuxtools.systemtap.graphing.ui.wizards.dataset;

import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.FilteredRowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.row.RowDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.table.FilteredTableDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.table.TableDataSet;
import org.eclipse.linuxtools.systemtap.graphing.ui.datadisplay.DataGrid;
import org.eclipse.swt.widgets.Composite;

public final class DataSetFactory {
    public static IDataSet createDataSet(String id, String[] labels) {
        if(id.equals(RowDataSet.ID)) {
            return new RowDataSet(labels);
        } else if(id.equals(TableDataSet.ID)) {
            return new TableDataSet(labels);
        }
        return null;
    }

    public static IFilteredDataSet createFilteredDataSet(String id, String[] labels) {
        if(id.equals(RowDataSet.ID)) {
            return new FilteredRowDataSet(labels);
        } else if(id.equals(TableDataSet.ID)) {
            return new FilteredTableDataSet(labels);
        }
        return new FilteredRowDataSet(labels);
    }

    /**
     * Returns DataGrid instance containing the given data set as a sibling of the composite.
     * @param composite The parent composite.
     * @param set The data set.
     * @return The DataGrid instance created.
     * @since 3.0 set must be a IFilteredDataSet.
     */
    public static DataGrid getDataGrid(Composite composite, IFilteredDataSet set) {
        if(set instanceof RowDataSet || set instanceof TableDataSet) {
            return new DataGrid(composite, set);
        }

        return null;
    }

}
