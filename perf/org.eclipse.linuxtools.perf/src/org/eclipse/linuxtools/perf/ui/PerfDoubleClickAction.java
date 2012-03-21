/*******************************************************************************
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thavidu Ranatunga (IBM) - Initial implementation.
 *******************************************************************************/ 
package org.eclipse.linuxtools.perf.ui;

import java.util.HashMap;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.perf.PerfPlugin;
import org.eclipse.linuxtools.perf.model.PMCommand;
import org.eclipse.linuxtools.perf.model.PMDso;
import org.eclipse.linuxtools.perf.model.PMEvent;
import org.eclipse.linuxtools.perf.model.PMFile;
import org.eclipse.linuxtools.perf.model.PMLineRef;
import org.eclipse.linuxtools.perf.model.PMSymbol;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.ui.PartInitException;

public class PerfDoubleClickAction extends Action {
	
	private TreeViewer viewer;
	
	public PerfDoubleClickAction(TreeViewer v) {
		super();
		viewer = v;
	}
	public void run() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof PMLineRef) {
			//Open in editor
			PMLineRef tmp = (PMLineRef)obj;
//			showMessage(tmp.getParent().getName().toString());
			try {
				PMFile file = (PMFile) tmp.getParent().getParent();
				ProfileUIUtils.openEditorAndSelect(file.getPath(), Integer.parseInt(tmp.getName()));
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof PMFile) {
			PMFile tmp = (PMFile)obj;
			//showMessage(tmp.getName().toString());
			try {
				ProfileUIUtils.openEditorAndSelect(tmp.getName(), 1);
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof PMSymbol) {
			PMSymbol tmpsym = (PMSymbol)obj;
			PMFile tmpfile = (PMFile)tmpsym.getParent();
			PMDso tmpdso = (PMDso)tmpfile.getParent();
			if (tmpfile.getName().equals(PerfPlugin.STRINGS_UnfiledSymbols)) 
				return; //Don't try to do anything if we don't know where or what the symbol is.
			String binaryPath = tmpdso.getPath();
			ICProject project;
			try {
				project = ProfileUIUtils.findCProjectWithAbsolutePath(binaryPath);
				HashMap<String, int[]> map = ProfileUIUtils.findFunctionsInProject(project, tmpsym.getFunctionName(), -1, tmpfile.getPath(), true);
				boolean bFound = false;
				for (String loc : map.keySet()) {
					ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					bFound = true;
				}
				if (!bFound) {
					ProfileUIUtils.openEditorAndSelect(tmpfile.getPath(), 1);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (obj instanceof PMDso || obj instanceof PMCommand || obj instanceof PMEvent) {
			//no effect
		} else {
			//Shouldn't happen...
			showMessage("Double-click detected on "+obj.toString()+" "+obj.getClass().toString());
		}
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Perf Profile View",
			message);
	}
}
