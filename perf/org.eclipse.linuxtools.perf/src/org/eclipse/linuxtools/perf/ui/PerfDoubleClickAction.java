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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.perf.PerfPlugin;
import org.eclipse.linuxtools.perf.model.PMDso;
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
			PMLineRef line = (PMLineRef) obj;
			PMFile file = (PMFile) ((PMSymbol) line.getParent()).getParent();
			try {
				ProfileUIUtils.openEditorAndSelect(file.getPath(), Integer.parseInt(line.getName()));
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof PMFile) {
			PMFile file = (PMFile)obj;
			try {
				ProfileUIUtils.openEditorAndSelect(file.getName(), 1);
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof PMSymbol) {
			PMSymbol sym = (PMSymbol)obj;
			PMFile file = (PMFile) sym.getParent();
			PMDso dso = (PMDso) file.getParent();

			if (file.getName().equals(PerfPlugin.STRINGS_UnfiledSymbols)) 
				return; //Don't try to do anything if we don't know where or what the symbol is.

			String binaryPath = dso.getPath();
			ICProject project;
			try {
				project = ProfileUIUtils.findCProjectWithAbsolutePath(binaryPath);
				HashMap<String, int[]> map = ProfileUIUtils.findFunctionsInProject(project, sym.getFunctionName(), -1, file.getPath(), true);
				boolean bFound = false;
				for (String loc : map.keySet()) {
					ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					bFound = true;
				}
				if (!bFound) {
					ProfileUIUtils.openEditorAndSelect(file.getPath(), 1);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

}
