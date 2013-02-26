/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.dataviewers.charts;

/**
 * A class that handles the constants used by the charts.
 * 
 * <br/>
 * <br/>
 * It contains:
 * <ul>
 * <li>labels used by the chart dialog.</li>
 * <li>keys and default values used by the dialog settings.</li>
 * <li>predefined colors used by the charts.</li>
 * </ul>
 * 
 */
public class ChartConstants {

    /** The section name of the viewer's dialog settings where the chart dialog save its state */
    public static final String TAG_SECTION_CHARTS_STATE = "charts_section";
    /**
     * The key used by the column buttons to save their state. For example the button i will use the key
     * <code>TAG_COLUMN_BUTTON_+i</code>
     */
    public static final String TAG_COLUMN_BUTTON_ = "COLUMN_BUTTON_";
    /** The key used by the bar graph button to save its state */
    public static final String TAG_BAR_GRAPH_BUTTON = "BAR_GRAPH_BUTTON";
    /** The key used by the vertical bars button to save its state */
    public static final String TAG_VERTICAL_BARS_BUTTON = "VERTICAL_BARS_BUTTON";

    /** The default value of the column buttons */
    public static final boolean DEFAULT_COLUMN_BUTTON = true;
    /** The default value of the bar graph button */
    public static final boolean DEFAULT_BAR_GRAPH_BUTTON = true;
    /** The default value of the vertical bars button */
    public static final boolean DEFAULT_VERTICAL_BARS_BUTTON = false;

    /** The section name of the "save chart as image" action dialog settings */
    public static final String TAG_SECTION_CHARTS_SAVEACTION_STATE = "charts_saveasimg_section";
    /** The key used by the file dialog to save its file name */
    public static final String TAG_IMG_FILE_NAME = "IMG_FILE_NAME";
    /** The key used by the file dialog to save its filter path */
    public static final String TAG_IMG_FILTER_PATH = "IMG_FILTER_PATH";

    /** The default value of the file dialog file name */
    public static final String DEFAULT_IMG_FILE_NAME = ".";
    /** The default value of the file dialog filter path */
    public static final String DEFAULT_IMG_FILTER_PATH = ".";

    /** Image extension for jpg format */
    public static final String EXT_JPG = ".jpg";
    /** Image extension for jpeg format */
    public static final String EXT_JPEG = ".jpeg";
    /** Image extension for png format */
    public static final String EXT_PNG = ".png";
    /** Image extension for gif format */
    public static final String EXT_GIF = ".gif";
    /** Image extension for svg format */
    public static final String EXT_SVG = ".svg";
    /** The file extensions provided by the "save chart as image" file dialog */
    public static final String[] saveAsImageExt = { "*" + EXT_JPG, "*" + EXT_JPEG, "*" + EXT_PNG, "*" + EXT_GIF, "*.*" };
    /** The names associated to the files extensions provided by the "save chart as image" file dialog */
    public static final String[] saveAsImageExtNames = { "JPEG (*.jpg)", "JPEG (*.jpeg)", "PNG (*.png)", "GIF (*.gif)",
            "All Files (*.*)" };
}
