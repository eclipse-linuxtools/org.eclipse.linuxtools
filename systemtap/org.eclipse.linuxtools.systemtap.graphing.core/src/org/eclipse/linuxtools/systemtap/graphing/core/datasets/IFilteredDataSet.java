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

import org.eclipse.linuxtools.systemtap.graphing.core.filters.IDataSetFilter;

public interface IFilteredDataSet extends IDataSet {
    void addFilter(IDataSetFilter filter);
    IDataSetFilter[] getFilters();
    void clearFilters();
    boolean removeFilter(IDataSetFilter filter);
}
