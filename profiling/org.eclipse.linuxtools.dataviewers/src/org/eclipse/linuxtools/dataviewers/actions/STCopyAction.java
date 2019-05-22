/*******************************************************************************
 * Copyright (c) 2009, 2018 STMicroelectronics and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
     *
     * @param stViewer
     *            the stViewer to expand
     */
    public STCopyAction(final AbstractSTViewer stViewer) {
        super(STDataViewersMessages.copyToAction_title, PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
        this.stViewer = stViewer;
    }

    @Override
    public void run() {
        Clipboard cb = new Clipboard(Display.getDefault());
        IStructuredSelection selections = stViewer.getViewer().getStructuredSelection();
        Iterator<?> iterator = selections.iterator();
        StringBuilder sb = new StringBuilder();

        while (iterator.hasNext()) {
            Object obj = iterator.next();
            boolean needTab = false;
            for (ISTDataViewersField field : stViewer.getAllFields()) {
                if (needTab) {
                    sb.append("\t");
                }
                needTab = true;
                if (field.getValue(obj) != null) {
                    sb.append(field.getValue(obj));
                }
            }
            sb.append("\n");
        }
        cb.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }
}
