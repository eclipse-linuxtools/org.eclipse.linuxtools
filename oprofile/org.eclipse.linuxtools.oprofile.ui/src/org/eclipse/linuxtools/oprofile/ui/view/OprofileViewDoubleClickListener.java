/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

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

	@Override
	public void doubleClick(DoubleClickEvent event) {
		IUiModelElement element = (IUiModelElement)((TreeSelection)((TreeViewer)event.getSource()).getSelection()).getFirstElement();;
		
		if (element instanceof UiModelEvent) {
//			UiModelEvent event = (UiModelEvent)element;

		} else if (element instanceof UiModelSession) {
//			UiModelSession session = (UiModelSession)element;
			
		} else if (element instanceof UiModelImage) {
//			UiModelImage image = (UiModelImage)element;
	
		} else if (element instanceof UiModelSymbol) {
//			UiModelSymbol symbol = (UiModelSymbol)element;
			
		} else if (element instanceof UiModelSample) {
			UiModelSample sample = (UiModelSample)element;
			String fileName = ((UiModelSymbol)sample.getParent()).getFileName();
			int line = sample.getLine();
			
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
