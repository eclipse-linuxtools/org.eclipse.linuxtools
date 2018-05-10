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

public interface IDataEntry {
    int getRowCount();
    int getColCount();
    Object get(String key, int col);
    Object[] getRow(int row);
    Object[] getRow(String key);
    Object[] getColumn(int col);
    Object[][] getData();
    IDataEntry copy();
    void putRow(int row, Object[] data);
    boolean remove(int row);
}
