/*******************************************************************************
 * Copyright (c) 2009 STMicroelectronics.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marzia Maugeri <marzia.maugeri@st.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.dataviewers.actions;

import java.util.Iterator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.dataviewers.abstractviewers.AbstractSTViewer;
import org.eclipse.linuxtools.dataviewers.abstractviewers.ISTDataViewersField;
import org.eclipse.linuxtools.dataviewers.abstractviewers.STDataViewersMessages;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;


public class STCopyAction extends Action {
	private final AbstractSTViewer stViewer;
	
	/**
	 * Constructor
	 * @param stViewer the stViewer to expand
	 */
	public STCopyAction(final AbstractSTViewer stViewer) {
		super(STDataViewersMessages.copyToAction_title,
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		this.stViewer = stViewer;
	}
	
	public void run() {
		Clipboard cb = new Clipboard(Display.getDefault());
		ISTDataViewersField[] fields = stViewer.getAllFields();
		IStructuredSelection selections = (IStructuredSelection)stViewer.getViewer().getSelection();
		Iterator<?> iterator = selections.iterator();
		StringBuilder sb = new StringBuilder();
			
		for(int i=0;iterator.hasNext();i++){
			Object obj = iterator.next();
			
			for(int j=0;j<fields.length;j++){
				if (fields[j].getValue(obj) == null){
					sb.append("");
				}
				else{
					sb.append(fields[j].getValue(obj));
					sb.append(" ");
				}
			}

			sb.append("\n");
	
		}
		cb.setContents(new Object[]{sb.toString()}, new Transfer[] {TextTransfer.getInstance()});
	}
}
