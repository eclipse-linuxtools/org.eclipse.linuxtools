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

import java.util.LinkedList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.RunScriptChartHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.preferences.ConsoleLogPreferenceConstants;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptHandler;

public class SystemTapScriptLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	static final String CONFIGURATION_TYPE = "org.eclipse.linuxtools.systemtap.ui.ide.SystemTapLaunchConfigurationType"; //$NON-NLS-1$

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		IPreferenceStore preferenceStore = ConsoleLogPlugin.getDefault().getPreferenceStore();

		RunScriptHandler action;

		boolean runWithChart = configuration.getAttribute(SystemTapScriptGraphOptionsTab.RUN_WITH_CHART, false);
		if (runWithChart){
			IDataSet dataSet = SystemTapScriptGraphOptionsTab.createDataset(configuration);
			IDataSetParser parser = SystemTapScriptGraphOptionsTab.createDatasetParser(configuration);
			LinkedList<GraphData> graphs = SystemTapScriptGraphOptionsTab.createGraphsFromConfiguration(configuration);
			action = new RunScriptChartHandler(parser, dataSet, graphs);
		}else{
			action = new RunScriptHandler();
		}

		// Path
		String path = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, ""); //$NON-NLS-1$
		if (!path.isEmpty()){
			action.setPath(new Path(path));
		}

		// User Name
		String userName = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_NAME_ATTR, ""); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.SCP_USER, userName);

		// User Password
		String password = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_PASS_ATTR, ""); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.SCP_PASSWORD, password);

		// Run locally and/or as current user.
		boolean runAsCurrentUser = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.CURRENT_USER_ATTR, true);
		boolean runLocal = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.LOCAL_HOST_ATTR, true);
		action.setLocalScript(runLocal && runAsCurrentUser);

		// Host Name.
		String hostName = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.HOST_NAME_ATTR, "localhost"); //$NON-NLS-1$
		preferenceStore.setValue(ConsoleLogPreferenceConstants.HOST_NAME, hostName);

		String value = configuration.getAttribute(IDEPreferenceConstants.STAP_CMD_OPTION[IDEPreferenceConstants.KEY], ""); //$NON-NLS-1$
		if (!value.isEmpty()){
			action.addComandLineOptions(IDEPreferenceConstants.STAP_CMD_OPTION[IDEPreferenceConstants.FLAG] + " " + value); //$NON-NLS-1$
		}

		// Add command line options
		for(int i=0; i<IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS.length; i++) {
			boolean flag = configuration.getAttribute(
							IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.KEY],
							false);
			if (flag){
				action.addComandLineOptions(IDEPreferenceConstants.STAP_BOOLEAN_OPTIONS[i][IDEPreferenceConstants.FLAG]);
			}
		}

		for(int i=0; i<IDEPreferenceConstants.STAP_STRING_OPTIONS.length; i++) {
			value = configuration.getAttribute(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.KEY],""); //$NON-NLS-1$
			if (!value.isEmpty()){
				action.addComandLineOptions(IDEPreferenceConstants.STAP_STRING_OPTIONS[i][IDEPreferenceConstants.FLAG] + " " + value); //$NON-NLS-1$
			}
		}

		value = configuration.getAttribute(SystemTapScriptOptionsTab.MISC_COMMANDLINE_OPTIONS,""); //$NON-NLS-1$
		if (!value.isEmpty()){
			action.addComandLineOptions(value);
		}

		action.execute(null);
	}

}
