/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.osgi.util.NLS;

/**
 * @since 2.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.messages"; //$NON-NLS-1$
	public static String ScriptRunAction_InvalidScriptTitle;
	public static String ScriptRunAction_InvalidScriptTMessage;
	public static String TempFileAction_errorDialogTitle;
	public static String RunScriptAction_alreadyRunningDialogTitle;
	public static String RunScriptAction_alreadyRunningDialogMessage;
	public static String RunScriptChartAction_couldNotSwitchToGraphicPerspective;
	public static String ProbeAliasAction_SelectEditor;
	public static String ProbeAliasAction_DialogTitle;
	public static String ProbeAliasAction_AskBeforeAddMessage;
	public static String ProbeAliasAction_AskBeforeAddYes;
	public static String ProbeAliasAction_AskBeforeAddCancel;
	public static String ProbeAliasAction_AskBeforeAddAnother;
	public static String NewFileAction_OtherFile;
	public static String DataSetFileExtension;
	public static String ExportDataSetAction_DialogTitle;
	public static String ImportDataSetAction_DialogTitle;
	public static String ImportDataSetAction_FileInvalid;
	public static String ImportDataSetAction_FileNotFound;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
