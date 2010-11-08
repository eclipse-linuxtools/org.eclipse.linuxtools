/********************************************************************************
 * Copyright (c) 2009 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.tools.cpuload;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author Otavio Ferranti
 */
public class DialogOptions extends TitleAreaDialog {

	final private String WINDOW_TITLE = Messages.OptionsDialog_Window_Title;
	final private String WINDOW_MESSAGE = Messages.OptionsDialog_Window_Message; 
	final private String LABEL_REFRESH = Messages.OptionsDialog_Label_Refresh_Rate;
	final private String LABEL_TIMEUNIT = "ms"; //$NON-NLS-1$

	private Spinner spinner;
	
	private ITool tool = null;
	
	/**
	 * The constructor.
	 * @param parent
	 */
	public DialogOptions(Shell parent, ITool tool) {
		super(parent);
		this.tool = tool;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		setTitle(WINDOW_TITLE);
		setMessage(WINDOW_MESSAGE);
		
		Composite dialogArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(3, false);
		
		gridLayout.marginLeft = 7;
		gridLayout.marginRight = 7;
		
		dialogArea.setLayout(gridLayout);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		dialogArea.setFont(parent.getFont());
	
		Label refreshLabel = new Label(dialogArea, SWT.NULL);
		refreshLabel.setText(LABEL_REFRESH);
		
		spinner = new Spinner(dialogArea, SWT.BORDER);
		
		Label timeUnit = new Label(dialogArea, SWT.NULL);
		timeUnit.setText(LABEL_TIMEUNIT);
				
		spinner.setMinimum(0);
		spinner.setMaximum(50000);
		spinner.setSelection(tool.getRefreshDelay());
		spinner.setIncrement(100);
		spinner.setPageIncrement(500);
		spinner.pack();
		
		return dialogArea;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		return super.getInitialSize();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		tool.setRefreshDelay(spinner.getSelection());
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
	}
}
