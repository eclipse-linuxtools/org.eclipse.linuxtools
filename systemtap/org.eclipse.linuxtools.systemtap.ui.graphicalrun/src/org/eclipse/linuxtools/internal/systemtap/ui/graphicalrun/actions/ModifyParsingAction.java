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

package org.eclipse.linuxtools.internal.systemtap.ui.graphicalrun.actions;

import java.util.ArrayList;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.systemtap.ui.consolelog.actions.ConsoleAction;
import org.eclipse.linuxtools.systemtap.ui.consolelog.structures.ScriptConsole;
import org.eclipse.linuxtools.systemtap.ui.editor.PathEditorInput;
import org.eclipse.linuxtools.systemtap.ui.graphicalrun.structures.ChartStreamDaemon2;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingConstants;
import org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective;
import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSetParser;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.ChartStreamDaemon;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.dataset.DataSetWizard;
import org.eclipse.linuxtools.systemtap.ui.ide.IDEPerspective;
import org.eclipse.linuxtools.systemtap.ui.structures.listeners.IGobblerListener;
import org.eclipse.linuxtools.systemtap.ui.structures.runnable.LoggedCommand;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * The action to allow users to change the parsing expression while a script is activly running.
 * @author Ryan Morse
 */
public class ModifyParsingAction extends ConsoleAction {
	/**
	 * Prompts the user for a new regular expression to use in parsing the stap output for
	 * the <code>DataSet</code>. If the user enters a new parsing expression then it will
	 * get the active <code>ScriptConsole</code> and from that the active <code>LoggedCommand</code>.
	 * Finally, it will dispose of the old <code>ChartStreamDaemon2</code> and add an new
	 * one in its place.
	 */
	@Override
	public void run() {
		DataSetWizard wizard = new DataSetWizard(GraphingConstants.DataSetMetaData, getFilePath());
		IWorkbench workbench = PlatformUI.getWorkbench();
		wizard.init(workbench, null);
		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();

		IDataSetParser parser = wizard.getParser();
		IDataSet dataSet = wizard.getDataSet();

		if(null != parser && null != dataSet) {
			ScriptConsole console = super.getActive();
			LoggedCommand cmd = console.getCommand();

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
			
			IViewPart ivp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(GraphSelectorView.ID);
			GraphSelectorView graphSelector = ((GraphSelectorView)ivp);
			String name = console.getName();
			graphSelector.createScriptSet(name.substring(name.lastIndexOf('/')+1), dataSet);
		}
		
		wizard.dispose();
	}
	
	/**
	 * Gets the file location of the file open in the editor window.
	 * @return The path of the file in the active editor window.
	 */
	private String getFilePath() {
		try {
			IWorkbenchPage p = PlatformUI.getWorkbench().showPerspective(IDEPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			IEditorPart ed = p.getActiveEditor();
			PlatformUI.getWorkbench().showPerspective(GraphingPerspective.ID, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			return ((PathEditorInput)ed.getEditorInput()).getPath().toString();
		} catch(WorkbenchException we) {}
		return null;
	}
}