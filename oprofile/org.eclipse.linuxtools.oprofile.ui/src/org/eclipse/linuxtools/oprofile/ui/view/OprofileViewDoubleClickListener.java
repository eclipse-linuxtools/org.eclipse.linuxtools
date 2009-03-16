/*******************************************************************************
 * Copyright (c) 2004, 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *    Keith Seitz <keiths@redhat.com> - SaveSessionValidator code 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.view;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.oprofile.core.OpcontrolException;
import org.eclipse.linuxtools.oprofile.core.Oprofile;
import org.eclipse.linuxtools.oprofile.core.OprofileCorePlugin;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.oprofile.ui.OprofileUiPlugin;
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
		IUiModelElement element = (IUiModelElement)((TreeSelection)((TreeViewer)event.getSource()).getSelection()).getFirstElement();;
		
		if (element instanceof UiModelEvent) {
//			UiModelEvent event = (UiModelEvent)element;

		} else if (element instanceof UiModelSession) {
			UiModelSession session = (UiModelSession)element;
			
			if (session.isDefaultSession()) {
				//the following code was originially written by Keith Seitz
				InputDialog dialog = new InputDialog(OprofileUiPlugin.getActiveWorkbenchShell(),
						OprofileUiMessages.getString("savedialog.title"),    //$NON-NLS-1$
						OprofileUiMessages.getString("savedialog.message"),    //$NON-NLS-1$
						OprofileUiMessages.getString("savedialog.initial"),   //$NON-NLS-1$
						new SaveSessionValidator());
				
				int result = dialog.open();
				if (result == Window.OK) {
					try {
						OprofileCorePlugin.getDefault().getOpcontrolProvider().saveSession(dialog.getValue());
						OprofileUiPlugin.getDefault().getOprofileView().refreshView();
					} catch (OpcontrolException oe) {
						OprofileCorePlugin.showErrorDialog("opcontrolProvider", oe); //$NON-NLS-1$
					}
				}
			}
		} else if (element instanceof UiModelImage) {
//			UiModelImage image = (UiModelImage)element;
	
		} else if (element instanceof UiModelSymbol) {
			/* disable this.. for binary section such as .plt, 
			 * this will open the binary in an editor = bad */ 

			//jump to 1st line in the file
//			UiModelSymbol symbol = (UiModelSymbol)element;
//			String fileName = symbol.getFileName();
//			
//			if (fileName.length() > 0) {
//				try {
//					ProfileUIUtils.openEditorAndSelect(fileName, 1);
//				} catch (PartInitException e) {
//					e.printStackTrace();
//				} catch (BadLocationException e) {
//					e.printStackTrace();
//				}
//			}
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
	
	//Original author: Keith Seitz <keiths@redhat.com>
	private class SaveSessionValidator implements IInputValidator {
		public String isValid(String newText) {
			// Sanity check
			if (newText.length() == 0) {
				return ""; //$NON-NLS-1$
			}
			
			// Cannot contain invalid characters
			int index = newText.indexOf('/');
			if (index == -1) {
				index = newText.indexOf('\\');
			}
			
			if (index != -1) {
				String format = OprofileUiMessages.getString("savedialog.validator.invalidChar"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText.substring(index, index + 1), newText };
				return MessageFormat.format(format, fmtArgs);
			}
				
			// Cannot contain whitespace
			if (newText.contains(" ") || newText.contains("\t")) { //$NON-NLS-1$ //$NON-NLS-2$
				String format = OprofileUiMessages.getString("savedialog.validator.containsWhitespace"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText };
				return MessageFormat.format(format, fmtArgs);
			}
			
			// Must not already exist (opcontrol doesn't allow it)
			File file = new File(Oprofile.getDefaultSamplesDirectory(), newText);
			if (file.exists()) {
				String format = OprofileUiMessages.getString("savedialog.validator.exists"); //$NON-NLS-1$
				Object[] fmtArgs = new Object[] { newText };
				return MessageFormat.format(format, fmtArgs);
			}

			// Everything OK
			return null;
		}
	};
}
