/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.rpmlint.resolutions;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.linuxtools.rpm.rpmlint.RpmlintLog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

abstract public class ARemoveLineResolution implements IMarkerResolution2 {
	
	public void run(IMarker marker) {
	
		// Open or activate the editor. 
		IWorkbenchPage page = PlatformUI.getWorkbench() 
		.getActiveWorkbenchWindow().getActivePage(); 
		IEditorPart part; 
		try { 
			part = IDE.openEditor(page, marker); 
		} 
		catch (PartInitException e) { 
			RpmlintLog.logError(e); 
			return; 
		} 
		// Get the editor's document. 
		if (!(part instanceof ITextEditor)) { 
			return; 
		} 
		ITextEditor editor = (ITextEditor) part; 
		// Get the document
		IDocument doc = editor.getDocumentProvider() 
		.getDocument(editor.getEditorInput()); 

		try {
			int index = doc.getLineOffset(marker.getAttribute(IMarker.LINE_NUMBER, 0));
			int lineLength = doc.getLineLength(marker.getAttribute(IMarker.LINE_NUMBER, 0));
			doc.replace(index, lineLength, ""); 
		} catch (BadLocationException e) {
			RpmlintLog.logError(e);
		}
	}
	

}
