/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Andrew Ferrazzutti
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphing.ui.widgets;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.systemtap.graphing.ui.widgets.messages"; //$NON-NLS-1$
    public static String GraphContinuousControl_ZoomInLabel;
    public static String GraphContinuousXControl_ZoomInTooltip;
    public static String GraphContinuousYControl_ZoomInTooltip;
    public static String GraphContinuousControl_ZoomOutLabel;
    public static String GraphContinuousXControl_ZoomOutTooltip;
    public static String GraphContinuousYControl_ZoomOutTooltip;
    public static String GraphContinuousXControl_ScaleMessage;
    public static String GraphContinuousXControl_ScrollMessage;
    public static String GraphContinuousYControl_ScaleMessage;
    public static String GraphContinuousYControl_ScrollMessage;
    public static String GraphDiscreteXControl_First;
    public static String GraphDiscreteXControl_Left;
    public static String GraphDiscreteXControl_ZoomIn;
    public static String GraphDiscreteXControl_ZoomOut;
    public static String GraphDiscreteXControl_All;
    public static String GraphDiscreteXControl_Right;
    public static String GraphDiscreteXControl_Last;

    /**
     * @since 3.1
     */
    public static String GraphCompositeTitle;
    /**
     * @since 3.1
     */
    public static String GraphCompositeLegend;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
