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
