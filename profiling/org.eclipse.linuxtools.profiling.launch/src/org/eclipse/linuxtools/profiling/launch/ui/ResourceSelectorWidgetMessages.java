/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/package org.eclipse.linuxtools.profiling.launch.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class ResourceSelectorWidgetMessages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.profiling.launch.ui.messages";//$NON-NLS-1$

    public static String uriLabelText;
    public static String browseLabelText;

    public static String FileSystemSelectionArea_unrecognized_scheme;

    public static String FileSystemSelectionArea_exception_while_creating_runnable_class;

    public static String FileSystemSelectionArea_found_multiple_default_extensions;
    public static String fileSystemSelectionText;

    public static String ResourceSelectorWidget_getSelectorProxy_returned_null;
    public static String ResourceSelectorWidget_invalid_location;
    public static String ResourceSelectorWidget_select;
    public static String ResourceSelectorWidget_unrecognize_resourceType;
    public static String ResourceSelectorWidget_unrecognized_resourceType;

    static {
        // load message values from bundle file
        NLS.initializeMessages(BUNDLE_NAME, ResourceSelectorWidgetMessages.class);
    }

}
