/********************************************************************************
 * Copyright (c) 2008-2010 Motorola Inc. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributor:
 * Otavio Ferranti (Motorola)
 *
 * Contributors:
 * Otavio Ferranti - Eldorado Research Institute - Bug 255255 [tml][proctools] Add extension points
 * Daniel Pastore (Eldorado) - [289870] Moving and renaming Tml to Sequoyah 
 ********************************************************************************/

package org.eclipse.linuxtools.sequoyah.device.ui;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.linuxtools.sequoyah.device.tools.ITool;
import org.eclipse.linuxtools.sequoyah.device.utilities.ProtocolDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


/**
 * @author Otavio Ferranti
 */
public class DialogConnect extends TitleAreaDialog {

	final private String WINDOW_TITLE = Messages.OpenConnectionDialog_Window_Title;
	final private String WINDOW_MESSAGE = Messages.OpenConnectionDialog_Window_Message; 
	final private String LABEL_HOST = Messages.OpenConnectionDialog_Label_Host;
	final private String LABEL_PORT = Messages.OpenConnectionDialog_Label_Port;
	final private String LABEL_PROTOCOL = Messages.OpenConnectionDialog_Label_Protocol;

	private Text hostText;
	private Text portText;
	private Combo protocolCombo;

	private ITool tool = null;

	private List <ProtocolDescriptor> pdList = null;
	
	/**
	 * The constructor.
	 * @param parent
	 */
	public DialogConnect(Shell parent, ITool tool) {
		super(parent);
		this.tool = tool;
		this.pdList = tool.getProtocolsDescriptors();
	}

	/**
	 * 
	 */
	private int getProtocolDefaultPort(String name) {
		int retVal = -1;
		for (ProtocolDescriptor pd : this.pdList) {
			if(pd.getName().equalsIgnoreCase(name)) {
				retVal = pd.getDefaultPort();;
				break;
			};
		}
		return retVal;
	}

	/**
	 * 
	 */
	private ProtocolDescriptor getProcotolDescriptor(String name) {
		ProtocolDescriptor retVal = null;
		for (ProtocolDescriptor pd : this.pdList) {
			if(pd.getName().equalsIgnoreCase(name)) {
				retVal = pd;
				break;
			};
		}
		return retVal;
	}

	/**
	 * 
	 */
	private String[] getProcotolsNames() {
		List<String> aux = new LinkedList<String>();
		for (ProtocolDescriptor pd : this.pdList) {
			aux.add(pd.getName());
		}
		String[] retVal = new String[1];
		retVal = aux.toArray(retVal);
		return retVal;
	}

	/**
	 * 
	 */
	private void updatePortToDefault() {
		String selection = protocolCombo.getText();
		int port = this.getProtocolDefaultPort(selection);
		portText.setText(new Integer(port).toString());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#getInitialSize()
	 */
	protected Point getInitialSize() {
		return super.getInitialSize();
	}
	
	protected Control createDialogArea(Composite parent) {

		setTitle(WINDOW_TITLE);
		setMessage(WINDOW_MESSAGE);
		
		Composite dialogArea = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		
		gridLayout.marginLeft = 7;
		gridLayout.marginRight = 7;
		
		dialogArea.setLayout(gridLayout);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		dialogArea.setFont(parent.getFont());
		
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
	
		Label hostLabel = new Label(dialogArea, SWT.NULL);
		hostLabel.setText(LABEL_HOST);
		hostText = new Text(dialogArea, SWT.BORDER);
		hostText.setLayoutData(gridData);
		
		Label portLabel = new Label(dialogArea, SWT.NULL);
		portLabel.setText(LABEL_PORT);
		portText = new Text(dialogArea, SWT.BORDER);
		portText.setLayoutData(gridData);
				
		Label protocolLabel = new Label(dialogArea, SWT.NULL);
		protocolLabel.setText(LABEL_PROTOCOL);
		
		protocolCombo = new Combo(dialogArea, SWT.READ_ONLY);
		protocolCombo.setItems(this.getProcotolsNames());
		protocolCombo.select(0);
		updatePortToDefault();
		
		protocolCombo.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				updatePortToDefault();
			}
		});
		
		return dialogArea;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		tool.disconnect();
		tool.connect(hostText.getText(),
							new Integer(portText.getText()),
							this.getProcotolDescriptor(protocolCombo.getText()));
		super.okPressed();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		super.cancelPressed();
	}
}
