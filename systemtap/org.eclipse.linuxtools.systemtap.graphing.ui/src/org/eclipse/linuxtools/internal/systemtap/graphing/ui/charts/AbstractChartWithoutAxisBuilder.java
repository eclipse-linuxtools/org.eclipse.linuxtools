/*******************************************************************************
 * Copyright (c) 2010-2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Red Hat Inc. - ongoing maintenance
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.linuxtools.systemtap.graphing.core.adapters.IAdapter;
import org.eclipse.linuxtools.systemtap.graphing.ui.charts.AbstractChartBuilder;
import org.eclipse.swt.widgets.Composite;


/**
 * A {@link AbstractChartBuilder} for building a chart without axes.
 * @author Qi Liang
 */
public abstract class AbstractChartWithoutAxisBuilder extends AbstractChartBuilder {

    /**
     * Constructs a builder for a chart with no axes and associates it to one data set.
     * @param adapter An {@link IAdapter} for reading from the chart's data set.
     * @param parent The parent {@link Composite} that will contain this chart builder.
     * @param style The style of the chart to construct.
     * @param title The title of the chart to construct.
     */
    public AbstractChartWithoutAxisBuilder(IAdapter adapter, Composite parent, int style, String title) {
         super(adapter, parent, style, title);
    }
}
