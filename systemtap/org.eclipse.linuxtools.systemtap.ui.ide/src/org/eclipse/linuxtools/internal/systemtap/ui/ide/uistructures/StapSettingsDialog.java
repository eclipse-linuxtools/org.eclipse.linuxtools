/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.ide.uistructures;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.ide.preferences.IDEPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * A dialog box displayed to prompt the user for additional arguments to pass to stap when the
 * user presses the Run with Options button.
 * @author Ryan Morse
 *
 */
public class StapSettingsDialog extends Dialog {
	public StapSettingsDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(Localization.getString("StapSettingsDialog.StapOptions")); //$NON-NLS-1$
		shell.setSize(new org.eclipse.swt.graphics.Point(640,170 + ((1+checkBox.length)>>1) + 50*((1+text.length)>>1)));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = (Composite) super.createDialogArea(parent);

		//Check boxes
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		cmpChkBoxes = new Composite(comp, SWT.NONE);
		cmpChkBoxes.setLayout(gridLayout);
		cmpChkBoxes.setBounds(new Rectangle(100,5,460,30*((1+checkBox.length)>>1)));

		int i;
		for(i=0; i<IDEPreferenceConstants.P_STAP.length - IDEPreferenceConstants.P_STAP_OPTS.length; i++) {
			checkBox[i] = new Button(cmpChkBoxes, SWT.CHECK);
			checkBox[i].setText(IDEPreferenceConstants.P_STAP[i][0] + IDEPreferenceConstants.P_STAP[i][1]);
			checkBox[i].setBackground(cmpChkBoxes.getBackground());
		}

		//Labels and Text fields
		cmpTxtBoxes = new Composite(comp, SWT.NONE);
		cmpTxtBoxes.setBounds(new Rectangle(5,5+30*((1+checkBox.length)>>1),620,50*((1+text.length)>>1)));

		for(int j=0; j<IDEPreferenceConstants.P_STAP_OPTS.length; i++, j++) {
			label[j] = new Label(cmpTxtBoxes, SWT.NONE);
			label[j].setBounds(new Rectangle(320*(j/5),50*(j%5),300,17));
			label[j].setText(IDEPreferenceConstants.P_STAP[i][0] + IDEPreferenceConstants.P_STAP[i][1]);
			label[j].setBackground(cmpChkBoxes.getBackground());
			text[j] = new Text(cmpTxtBoxes, SWT.BORDER);
			text[j].setBounds(new Rectangle(320*(j/5),20+50*(j%5),300,27));

			if("-v".equals(IDEPreferenceConstants.P_STAP[i][0])) { //$NON-NLS-1$
				text[j].addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if('v' != e.character) {
							e.doit = false;
						}
					}
				});
			} else if("-p NUM".equals(IDEPreferenceConstants.P_STAP[i][0])) { //$NON-NLS-1$
				text[j].addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if(32 <= e.character && 126 >= e.character) {
							if('1' > e.character || '5' < e.character)
								e.doit = false;
							else if(0 < text[1].getText().length())
								e.doit = false;
						}
					}
				});
			} else if("-s NUM".equals(IDEPreferenceConstants.P_STAP[i][0])) { //$NON-NLS-1$
				text[j].addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						if(32 <= e.character && 126 >= e.character) {
							if(!Character.isDigit(e.character))
								e.doit = false;
						}
					}
				});
			}
		}

		return comp;
	}

	@Override
	protected void okPressed() {
		cmdOpts = new boolean[checkBox.length];
		cmdOptVals = new String[text.length];

		for (int i = 0; i < cmdOpts.length; i++)
			cmdOpts[i] = checkBox[i].getSelection();

		for (int i = 0; i < cmdOptVals.length; i++)
			cmdOptVals[i] = text[i].getText();

		super.okPressed();
	}

	public boolean[] getStapOpts() {
		return cmdOpts;
	}

	public String[] getStapOptVals() {
		return cmdOptVals;
	}

	public void dispose() {
		cmdOpts = null;
		cmdOptVals = null;
		cmpChkBoxes.dispose();
		cmpTxtBoxes.dispose();
		for(int i=0; i<checkBox.length; i++)
			checkBox[i].dispose();
		checkBox = null;
		for(int i=0; i<label.length; i++) {
			label[i].dispose();
			text[i].dispose();
		}
		label = null;
		text = null;
	}

	//private static String[] tapsets = null;
	private static boolean[] cmdOpts = null;
	private static String[] cmdOptVals = null;

	private Composite cmpChkBoxes = null;
	private Composite cmpTxtBoxes = null;
	private Button checkBox[] = new Button[IDEPreferenceConstants.P_STAP.length - IDEPreferenceConstants.P_STAP_OPTS.length];
	private Label label[] = new Label[IDEPreferenceConstants.P_STAP_OPTS.length];
	private Text text[] = new Text[IDEPreferenceConstants.P_STAP_OPTS.length];
}
