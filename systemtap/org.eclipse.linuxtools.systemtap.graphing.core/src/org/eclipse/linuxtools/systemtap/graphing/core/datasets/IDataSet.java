/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.core.datasets;

import java.io.File;


public interface IDataSet {
    String[] getTitles();
    String getID();
    boolean writeToFile(File file);
    boolean readFromFile(File file);

    int getRowCount();
    int getColCount();

    Object[] getRow(int row);
    Object[] getColumn(int col);
    Object[] getColumn(int col, int start, int end);

    void setData(IDataEntry entry);
    boolean remove(IDataEntry entry);

    int COL_ROW_NUM = -1;
}
