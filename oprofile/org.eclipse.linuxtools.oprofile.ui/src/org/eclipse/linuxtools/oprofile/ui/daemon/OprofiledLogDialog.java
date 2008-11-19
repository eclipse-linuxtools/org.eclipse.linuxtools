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

package org.eclipse.linuxtools.oprofile.ui.daemon;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A custom dialog box to display the oprofiled log file.
 * The content is grabbed by a {@link LogReader}.
 * Used in <code>OprofiledLogActionDelegate</code>.
 * 
 * @author Kent Sebastian	<ksebasti@redhat.com>
 */
public class OprofiledLogDialog extends MessageDialog {
	//string to contain the log file
	String textContent = null;
	
	final int GRID_WIDTH = 350;
	final int GRID_HEIGHT = 400;
	
	public OprofiledLogDialog (Shell parentShell, String dialogMessage) {
		super(parentShell, "OProfiled Log", null, null, MessageDialog.NONE, new String[] { IDialogConstants.OK_LABEL }, 0);
		textContent = dialogMessage;
	}
	
	@Override
    protected Control createCustomArea(Composite parent) {
		Composite area = new Composite(parent, 0);
		Layout layout = new GridLayout(1, true);
		GridData gd = new GridData(GRID_WIDTH, GRID_HEIGHT);
		
		area.setLayout(layout);
		area.setLayoutData(gd);
		
		Text txt = new Text(area, SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		txt.setText(textContent);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		
        return area;
    }
}
