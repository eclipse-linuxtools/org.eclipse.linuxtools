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

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.IDEPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.RunScriptChartHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.actions.RunScriptHandler;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphing.core.datasets.IFilteredDataSet;
import org.eclipse.linuxtools.systemtap.graphing.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.structures.process.SystemTapRuntimeProcessFactory;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.RemoteScriptOptions;

public class SystemTapScriptLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate {

	static final String CONFIGURATION_TYPE = "org.eclipse.linuxtools.systemtap.ui.ide.SystemTapLaunchConfigurationType"; //$NON-NLS-1$

	private IProject[] scriptProject;

	/**
	 * Keep a reference to the target running script's parent project, so only that project
	 * will be saved when the script is run.
	 */
	@Override
	protected IProject[] getBuildOrder(ILaunchConfiguration configuration, String mode) {
		return scriptProject;
	}

	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) {
		return new SystemTapScriptLaunch(configuration, mode);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		// Force the configuration to use the proper Process Factory.
		if (!configuration.getAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, "").equals(SystemTapRuntimeProcessFactory.PROCESS_FACTORY_ID)) { //$NON-NLS-1$
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, SystemTapRuntimeProcessFactory.PROCESS_FACTORY_ID);
			wc.doSave();
		}
		// Find the parent project of the target script.
		IPath path = Path.fromOSString(configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, (String)null));
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
		scriptProject = file == null ? null : new IProject[]{file.getProject()};

		// Only save the target script's project if a project is found.
		if (scriptProject != null) {
			return super.preLaunchCheck(configuration, mode, monitor);
		}
		return true;
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		// Wait for other stap launches' consoles to be initiated before starting a new launch.
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		for (ILaunch olaunch : manager.getLaunches()) {
			if (olaunch.equals(launch)) {
				continue;
			}
			if (olaunch instanceof SystemTapScriptLaunch && ((SystemTapScriptLaunch) olaunch).getConsole() == null) {
				throw new CoreException(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID,
						Messages.SystemTapScriptLaunchError_waitForConsoles));
			}
		}

		if (!SystemTapScriptGraphOptionsTab.isValidLaunch(configuration)) {
			throw new CoreException(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID, Messages.SystemTapScriptLaunchError_graph));
		}

		RunScriptHandler action;

		boolean runWithChart = configuration.getAttribute(SystemTapScriptGraphOptionsTab.RUN_WITH_CHART, false);
		// If runWithChart is true there must be at least one graph, but this isn't guaranteed
		// to be true for outdated Launch Configurations. So for safety, make sure there are graphs.
		int numGraphs = configuration.getAttribute(SystemTapScriptGraphOptionsTab.NUMBER_OF_REGEXS, 0);
		if (runWithChart && numGraphs > 0){
			List<IDataSetParser> parsers = SystemTapScriptGraphOptionsTab.createDatasetParsers(configuration);
			List<IFilteredDataSet> dataSets = SystemTapScriptGraphOptionsTab.createDataset(configuration);
			List<String> names = SystemTapScriptGraphOptionsTab.createDatasetNames(configuration);
			List<LinkedList<GraphData>> graphs = SystemTapScriptGraphOptionsTab.createGraphsFromConfiguration(configuration);
			action = new RunScriptChartHandler(parsers, dataSets, names, graphs);
		}else{
			action = new RunScriptHandler();
		}

		// Path
		IPath scriptPath = new Path(configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.SCRIPT_PATH_ATTR, "")); //$NON-NLS-1$
		if (!scriptPath.toFile().exists()) {
			throw new CoreException(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.SystemTapScriptLaunchError_fileNotFound, scriptPath.toString())));
		}
		String extension = scriptPath.getFileExtension();
		if (extension == null || !extension.equals("stp")) { //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.SystemTapScriptLaunchError_fileNotStp, scriptPath.toString())));
		}
		action.setPath(scriptPath);

		// Run locally and/or as current user.
		boolean runAsCurrentUser = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.CURRENT_USER_ATTR, true);
		boolean runLocal = configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.LOCAL_HOST_ATTR, true);

		action.setRemoteScriptOptions(runLocal && runAsCurrentUser ? null : new RemoteScriptOptions(
						configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_NAME_ATTR, ""), //$NON-NLS-1$
						configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.USER_PASS_ATTR, ""), //$NON-NLS-1$
						configuration.getAttribute(SystemTapScriptLaunchConfigurationTab.HOST_NAME_ATTR, "localhost"))); //$NON-NLS-1$

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

		action.setLaunch((SystemTapScriptLaunch) launch);
		try {
			action.execute(null);
		} catch (ExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, IDEPlugin.PLUGIN_ID, e.getMessage()));
		}
	}

}
