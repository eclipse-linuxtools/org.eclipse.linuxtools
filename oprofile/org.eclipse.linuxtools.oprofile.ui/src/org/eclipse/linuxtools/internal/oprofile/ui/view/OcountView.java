/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.ui.view;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;

/**
 * Simple View to show text output of OCount
 * @author jjohnstn
 *
 */
public class OcountView extends ViewPart {

	String text = ""; //$NON-NLS-1$
	TextViewer viewer;

	@Override
	public void createPartControl(Composite parent) {
        viewer = new TextViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        viewer.setEditable(false);
        viewer.getTextWidget().setFont(JFaceResources.getFont(IDebugUIConstants.PREF_DETAIL_PANE_FONT));
        Control control=viewer.getControl();
        GridData gd=new GridData(GridData.FILL_BOTH);
        control.setLayoutData(gd);
        Document d = new Document(text);
        viewer.setDocument(d);
        viewer.refresh();
        OprofileUiPlugin.getDefault().setOcountView(this);
	}

	public void setText(String text) {
        Document d = new Document(text);
        viewer.setDocument(d);
        viewer.refresh();
	}

	/**
	 * Get the Text Viewer
	 *
	 * @return the text viewer for this View
	 */
	public TextViewer getViewer() {
		return viewer;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
