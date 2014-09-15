/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Ferrazzutti <aferrazz@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.graphing.ui.charts.messages"; //$NON-NLS-1$
    public static String AbstractChartWithAxisBuilder_ToolTipCoords;
    public static String BarChartBuilder_LabelTrimTag;
    public static String BarChartBuilder_ToolTipCoords;
    public static String PieChartBuilder_ToolTipCoords;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
