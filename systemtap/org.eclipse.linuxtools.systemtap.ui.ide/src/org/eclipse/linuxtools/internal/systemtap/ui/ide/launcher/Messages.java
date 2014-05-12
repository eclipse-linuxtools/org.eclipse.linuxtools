/*******************************************************************************
 * Copyright (c) 2012 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Sami Wagiaalla
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.systemtap.ui.ide.launcher.messages"; //$NON-NLS-1$

    public static String SystemTapScriptGraphOptionsTab_cantInitializeTab;
    public static String SystemTapScriptGraphOptionsTab_graphOutputRun;
    public static String SystemTapScriptGraphOptionsTab_graphOutput;
    public static String SystemTapScriptGraphOptionsTab_outputLabel;
    public static String SystemTapScriptGraphOptionsTab_emptyGroup;
    public static String SystemTapScriptGraphOptionsTab_graphingTitle;
    public static String SystemTapScriptGraphOptionsTab_deletedGraphData;
    public static String SystemTapScriptGraphOptionsTab_noGroups;
    public static String SystemTapScriptGraphOptionsTab_badGraphID;

    public static String SystemTapScriptGraphOptionsTab_AddGraphButton;
    public static String SystemTapScriptGraphOptionsTab_AddGraphButtonToolTip;
    public static String SystemTapScriptGraphOptionsTab_DuplicateGraphButton;
    public static String SystemTapScriptGraphOptionsTab_DuplicateGraphButtonToolTip;
    public static String SystemTapScriptGraphOptionsTab_EditGraphButton;
    public static String SystemTapScriptGraphOptionsTab_EditGraphButtonToolTip;

    public static String SystemTapScriptGraphOptionsTab_columnTitle;
    public static String SystemTapScriptGraphOptionsTab_extractedValueLabel;
    public static String SystemTapScriptGraphOptionsTab_columnShiftUp;
    public static String SystemTapScriptGraphOptionsTab_columnShiftDown;
    public static String SystemTapScriptGraphOptionsTab_defaultColumnTitleBase;
    public static String SystemTapScriptGraphOptionsTab_RemoveGraphButton;
    public static String SystemTapScriptGraphOptionsTab_RemoveGraphButtonToolTip;
    public static String SystemTapScriptGraphOptionsTab_invalidGraph;
    public static String SystemTapScriptGraphOptionsTab_invalidGraphID;

    public static String SystemTapScriptGraphOptionsTab_regexErrorMsgFormat;
    public static String SystemTapScriptGraphOptionsTab_regexLabel;
    public static String SystemTapScriptGraphOptionsTab_regexTooltip;
    public static String SystemTapScriptGraphOptionsTab_regexAddNew;
    public static String SystemTapScriptGraphOptionsTab_regexRemove;
    public static String SystemTapScriptGraphOptionsTab_graphSetTitleBase;

    public static String SystemTapScriptGraphOptionsTab_removeRegexTitle;
    public static String SystemTapScriptGraphOptionsTab_removeRegexAsk;

    public static String SystemTapScriptGraphOptionsTab_sampleOutputLabel;
    public static String SystemTapScriptGraphOptionsTab_sampleOutputTooltip;
    public static String SystemTapScriptGraphOptionsTab_sampleOutputNoMatch;
    public static String SystemTapScriptGraphOptionsTab_sampleOutputIsEmpty;

    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsButton;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsTooltip;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsTitle;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsMessage;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsErrorTitle;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsError;
    public static String SystemTapScriptGraphOptionsTab_generateFromPrintsEmpty;

    public static String SystemTapScriptLaunchConfigurationTab_script;
    public static String SystemTapScriptLaunchConfigurationTab_browse;
    public static String SystemTapScriptLaunchConfigurationTab_currentUser;
    public static String SystemTapScriptLaunchConfigurationTab_username;
    public static String SystemTapScriptLaunchConfigurationTab_password;
    public static String SystemTapScriptLaunchConfigurationTab_user;
    public static String SystemTapScriptLaunchConfigurationTab_host;
    public static String SystemTapScriptLaunchConfigurationTab_useDefaultPort;
    public static String SystemTapScriptLaunchConfigurationTab_port;
    public static String SystemTapScriptLaunchConfigurationTab_runLocally;
    public static String SystemTapScriptLaunchConfigurationTab_hostname;
    public static String SystemTapScriptLaunchConfigurationTab_general;
    public static String SystemTapScriptLaunchConfigurationTab_tabName;
    public static String SystemTapScriptLaunchConfigurationTab_selectScript;

    public static String SystemTapScriptLaunchConfigurationTab_errorInitializingTab;
    public static String SystemTapScriptLaunchConfigurationTab_options;
    public static String SystemTapScriptLaunchConfigurationTab_runWithChart;
    public static String SystemTapScriptLaunchShortcut_couldNotFindConfig;
    public static String SystemTapScriptLaunchShortcut_couldNotLaunchScript;
    public static String SystemTapScriptLaunchConfigurationTab_fileNotFound;
    public static String SystemTapScriptLaunchConfigurationTab_fileNotStp;

    public static String SystemTapScriptOptionsTab_selectExec;
    public static String SystemTapScriptOptionsTab_targetExec;
    public static String SystemTapScriptOptionsTab_otherOptions;
    public static String SystemTapScriptOptionsTab_dyninst;
    public static String SystemTapScriptOptionsTab_dyninstError;
    public static String SystemTapScriptOptionsTab_pidError;
    public static String SystemTapScriptOptionsTab_initializeConfigurationFailed;
    public static String SystemTapScriptOptionsTab_targetToolTip;

    public static String SystemTapScriptLaunchError_graph;
    public static String SystemTapScriptLaunchError_fileNotFound;
    public static String SystemTapScriptLaunchError_fileNotStp;
    public static String SystemTapScriptLaunchError_waitForConsoles;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
