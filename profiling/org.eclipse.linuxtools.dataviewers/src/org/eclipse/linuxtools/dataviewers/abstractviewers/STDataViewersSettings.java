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
package org.eclipse.linuxtools.dataviewers.abstractviewers;

/*
 * This class contains the setting tags used to save a STViewer section
 */
public class STDataViewersSettings {

    // Viewer settings
    public static final String TAG_SECTION_VIEWER_STATE = "viewer_state_section"; //$NON-NLS-1$
    public static final String TAG_VIEWER_STATE_COLUMN_WIDTH_ = "column_width_"; //$NON-NLS-1$
    public static final String TAG_VIEWER_STATE_COLUMN_ORDER = "column_order"; //$NON-NLS-1$
    public static final String TAG_VIEWER_STATE_VERTICAL_POSITION = "vertical_position"; //$NON-NLS-1$
    public static final String TAG_VIEWER_STATE_HORIZONTAL_POSITION = "horizontal_position"; //$NON-NLS-1$
    // Hide/show columns settings
    public static final String TAG_SECTION_HIDESHOW = "hide_show_section"; //$NON-NLS-1$
    public static final String TAG_HIDE_SHOW_COLUMN_STATE_ = "column_state_"; //$NON-NLS-1$
    public static final String TAG_HIDE_SHOW_COLUMN_WIDTH_ = "column_width_"; //$NON-NLS-1$
    // Sorter
    public static final String TAG_SECTION_SORTER = "sorter_section"; //$NON-NLS-1$
    public static final String TAG_SORTER_PRIORITY_ = "sort_priority_"; //$NON-NLS-1$
    public static final String TAG_SORTER_DIRECTION_ = "sort_direction_"; //$NON-NLS-1$
}
