/*******************************************************************************
 * Copyright (c) 2013 Kalray.eu
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Xavier Raynaud <xavier.raynaud@kalray.eu> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.dataviewers.charts;

import org.eclipse.osgi.util.NLS;

/**
 *
 * @author Xavier Raynaud <xavier.raynaud@kalray.eu>
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.dataviewers.charts.messages"; //$NON-NLS-1$

    public static String ChartConstants_ALL_FILES;
    public static String ChartConstants_BAR_GRAPH;
    public static String ChartConstants_CHART_BUILDER;
    public static String ChartConstants_CONFIRM_OVERWRITE_MSG;
    public static String ChartConstants_CONFIRM_OVERWRITE_TITLE;
    public static String ChartConstants_CREATE_CHART;
    public static String ChartConstants_CREATE_NEW_CHART_FROM_SELECTION;
    public static String ChartConstants_DESELECT_ALL;
    public static String ChartConstants_ERROR_SAVING_CHART;
    public static String ChartConstants_ERROR_SAVING_CHART_MESSAGE;
    public static String ChartConstants_NO_COLUMN_SELECTED;
    public static String ChartConstants_PIE_CHART;
    public static String ChartConstants_SAVE_CHART_AS;
    public static String ChartConstants_SAVE_CHART_DIALOG_TEXT;
    public static String ChartConstants_SELECT_ALL;
    public static String ChartConstants_SELECT_COLUMNS_TO_SHOW;
    public static String ChartConstants_SELECT_YOUR_CHART_TYPE;
    public static String ChartConstants_VERTICAL_BARS;
    public static String ChartConstants_ERROR_CHART_DISPOSED;
    public static String ChartConstants_ERROR_CHART_CLOSED;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
