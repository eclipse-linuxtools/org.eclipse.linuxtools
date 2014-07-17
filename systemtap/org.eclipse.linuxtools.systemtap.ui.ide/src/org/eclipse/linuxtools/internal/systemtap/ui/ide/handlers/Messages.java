/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.handlers.messages"; //$NON-NLS-1$
    public static String RunScriptHandler_InvalidScriptMessage;
    public static String RunScriptHandler_AlreadyRunningDialogTitle;
    public static String RunScriptHandler_AlreadyRunningDialogMessage;
    public static String RunScriptHandler_NonLocalTitle;
    public static String RunScriptChartHandler_couldNotSwitchToGraphicPerspective;
    public static String DataSetFileExtension;
    public static String ExportDataSetAction_DialogTitle;
    public static String ImportDataSetAction_DialogTitle;
    public static String ImportDataSetAction_FileInvalid;
    public static String ImportDataSetAction_FileNotFound;
    public static String AddStapProbe_editorError;
    public static String AddStapProbe_unableToInsertProbe;
    public static String CEditor_probeInsertFailed;
    public static String CEditor_canNotProbeLine;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
