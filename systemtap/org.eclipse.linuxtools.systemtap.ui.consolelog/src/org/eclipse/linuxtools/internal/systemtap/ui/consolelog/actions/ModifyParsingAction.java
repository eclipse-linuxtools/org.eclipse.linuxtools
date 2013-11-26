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

package org.eclipse.linuxtools.internal.systemtap.ui.consolelog.actions;

import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.structures.runnable.Command;
import org.eclipse.linuxtools.systemtap.ui.consolelog.ChartStreamDaemon2;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.ConsoleLogPlugin;
import org.eclipse.linuxtools.systemtap.ui.consolelog.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorEditor;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.ChartStreamDaemon;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * The action to allow users to change the parsing expression while a script is actively running.
 * @author Ryan Morse
 * @since 2.0
 * @deprecated
 * TODO By 3.0, this action is to either be removed or made compatible with
 * the graphing API in place.
 */
@Deprecated
public class ModifyParsingAction extends ConsoleAction {

	public ModifyParsingAction(ScriptConsole fConsole) {
		super(fConsole,
				Platform.getBundle(ConsoleLogPlugin.PLUGIN_ID).getEntry("icons/actions/regEx.gif"), //$NON-NLS-1$
				Localization.getString("ModifyParsingAction_name"), //$NON-NLS-1$
				Localization.getString("ModifyParsingAction_desc")); //$NON-NLS-1$
	}

	/**
	 * Prompts the user for a new regular expression to use in parsing the stap output for
	 * the <code>DataSet</code>. If the user enters a new parsing expression then it will
	 * get the active <code>ScriptConsole</code> and from that the active <code>LoggedCommand</code>.
	 * Finally, it will dispose of the old <code>ChartStreamDaemon2</code> and add an new
	 * one in its place.
	 */
	@Override
	public void run() {
		DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, console.getName());
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();

		IDataSetParser parser = wizard.getParser();
		IDataSet dataSet = wizard.getDataSet();

		if(null != parser && null != dataSet) {
			Command cmd = console.getCommand();

			ArrayList<IGobblerListener> listeners = cmd.getInputStreamListeners();
			ChartStreamDaemon2 daemon = null;
			if(null != listeners) {
				for(int i=0; i<listeners.size(); i++) {
					if(listeners.get(i) instanceof ChartStreamDaemon) {
						daemon = (ChartStreamDaemon2)listeners.get(i);
						break;
					}
				}
			}
			if(null == daemon) {
				daemon = new ChartStreamDaemon2(console, dataSet, parser);
				cmd.addInputStreamListener(daemon);
			} else
				daemon.setParser(dataSet, parser);

			IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorEditor.ID);
			GraphSelectorEditor graphSelector = ((GraphSelectorEditor)ivp);
			String name = console.getName();
			graphSelector.createScriptSet(name.substring(name.lastIndexOf('/')+1), dataSet);
		}

		wizard.dispose();
	}

}