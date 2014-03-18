/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;


/**
 * Builds the chart with axis.
 *
 * @author Qi Liang
 */
public abstract class AbstractChartWithoutAxisBuilder extends AbstractChartBuilder {

    /**
     * Constructor.
     */

    public AbstractChartWithoutAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
    	 super(adapter, parent, style, title);
	}
}
