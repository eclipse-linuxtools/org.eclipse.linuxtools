/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.actions;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ChartStreamDaemon2;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.Messages;
import org.eclipse.linuxtools.systemtap.ui.ide.actions.RunScriptHandler;
import org.eclipse.linuxtools.systemtap.ui.structures.ui.ExceptionErrorDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Action used to run the systemTap script in the active editor.  This action will start stap
 * and send the output to both the <code>ScriptConsole</code> window and a <code>DataSet</code>.
 * @author Ryan Morse
 */
public class RunScriptChartHandler extends RunScriptHandler {

	public RunScriptChartHandler() {
		super();
	}

	@Override
	protected void scriptConsoleInitialized(ScriptConsole console){
		getChartingOptions();
		console.getCommand().addInputStreamListener(new ChartStreamDaemon2(console, dataSet, parser));
		try {
			IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(GraphingPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			IViewPart ivp = p.showView(GraphSelectorView.ID);
			String name = console.getName();
			((GraphSelectorView)ivp).createScriptSet(name.substring(name.lastIndexOf('/')+1), dataSet);
		} catch(WorkbenchException we) {
			ExceptionErrorDialog.openError(Messages.RunScriptChartAction_couldNotSwitchToGraphicPerspective, we);
		}
	}

	/**
	 * This method is used to prompt the user for the parsing expression to be used in generating
	 * the <code>DataSet</code> from the scripts output.
	 */
	protected void getChartingOptions() {
		DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, getFilePath());
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		parser = wizard.getParser();

		dataSet = wizard.getDataSet();

		if(null == parser || null == dataSet)
		{
			continueRun = false;
		}
		wizard.dispose();
	}

	private IDataSet dataSet = null;
	private IDataSetParser parser = null;

}
