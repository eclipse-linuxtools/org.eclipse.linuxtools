/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.charts.provider;

import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;

/**
 * The interface used by the charts to get a <code>Number</code> data from a field.
 * 
 */
@Deprecated
public interface IChartField extends ISTDataViewersField {

    /**
     * Returns the number to display in the chart for this object.
     *
     * @param obj The field to retrieve from.
     * @return the corresponding number value
     */
    Number getNumber(Object obj);
}
