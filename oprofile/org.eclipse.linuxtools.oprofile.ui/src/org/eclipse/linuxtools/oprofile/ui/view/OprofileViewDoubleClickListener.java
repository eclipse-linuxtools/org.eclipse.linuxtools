/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

import java.util.HashMap;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.linuxtools.oprofile.ui.model.IUiModelElement;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelEvent;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelImage;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSample;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSession;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelSymbol;
import org.eclipse.linuxtools.profiling.ui.ProfileUIUtils;
import org.eclipse.ui.PartInitException;

/**
 * Listener for the oprofile view when a user double clicks on an element in the tree.
 * 
 * Different things occur based on the event:
 *   
 *   UiModelEvent 		- nothing (?)
 *   UiModelSession 	- save the session to a different name
 *   UiModelImage 		- nothing (?)
 *   UiModelSymbol		- nothing (?)
 *   UiModelSample		- go to line number in appropriate file
 */
public class OprofileViewDoubleClickListener implements IDoubleClickListener {
	public void doubleClick(DoubleClickEvent event) {
		TreeViewer tv = (TreeViewer) event.getSource();
		TreeSelection tsl = (TreeSelection) tv.getSelection();
		IUiModelElement element = (IUiModelElement) tsl.getFirstElement();
		
		if (element instanceof UiModelEvent) {
//			UiModelEvent event = (UiModelEvent)element;

		} else if (element instanceof UiModelSession) {
			/* moved into an action menu */
		} else if (element instanceof UiModelImage) {
//			UiModelImage image = (UiModelImage)element;
	
		} else if (element instanceof UiModelSymbol) {
			UiModelSymbol symbol = (UiModelSymbol)element;
			String symbolLabel = symbol.getParent().getLabelText();
			String fileName = symbol.getFileName();
			String functionName = symbol.getFunctionName();
			int numOfArgs = -1;
			HashMap<String,int[]> map;
			
			ICProject project;
			try {
				// hard coded to match "XY.PQ% in /some/arbitrary/path/to/binary"
				String absPath = symbolLabel.substring(symbolLabel.indexOf(" in ") + 4);
				project = ProfileUIUtils.findCProjectWithAbsolutePath(absPath);
				if (project == null){
					return;
				}
			} catch (CoreException e1) {
				e1.printStackTrace();
				return;
			}
			
			// detect function with arguments and narrow search accordingly
			if (functionName.matches(".*\\(.*\\)")) {
				int start = functionName.indexOf('(');
				if (functionName.contains(",")) {
					int end = functionName.indexOf(')');
					numOfArgs = functionName.substring(start, end).split(",").length;
				} else {
					numOfArgs = 1;
				}
				functionName = functionName.substring(0, start);
			}
			
			if (fileName.length() > 0 && functionName.length() > 0){
				// try and go to the function in the file
				map = ProfileUIUtils.findFunctionsInProject(project, functionName, numOfArgs, fileName);
				
				for (String loc : map.keySet()){
					try {
						ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
				
			}else if (functionName.length() > 0){
				// try to find the file name that has this function
				map = ProfileUIUtils.findFunctionsInProject(project, functionName, numOfArgs, null);
				
				for (String loc : map.keySet()){
					try {
						ProfileUIUtils.openEditorAndSelect(loc, map.get(loc)[0], map.get(loc)[1]);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}else if (fileName.length() > 0) {
				// can this ever happen ?
				// jump to 1st line in the file
				try {
					ProfileUIUtils.openEditorAndSelect(fileName, 1);
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		} else if (element instanceof UiModelSample) {
			//jump to line number in the appropriate file
			UiModelSample sample = (UiModelSample)element;
			int line = sample.getLine();
			
			//get file name from the parent sample 
			String fileName = ((UiModelSymbol)sample.getParent()).getFileName();
			
			try {
				ProfileUIUtils.openEditorAndSelect(fileName, line);
			} catch (PartInitException e) {
				e.printStackTrace();
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
	
}
