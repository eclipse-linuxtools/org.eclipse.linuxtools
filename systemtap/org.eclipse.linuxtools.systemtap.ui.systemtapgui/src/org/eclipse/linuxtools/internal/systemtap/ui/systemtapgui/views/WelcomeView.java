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

package org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.views;

import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.Localization;
import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.SystemTapGUIPlugin;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;



public class WelcomeView extends ViewPart {
	public WelcomeView() {
		super();
		LogManager.logInfo("Initializing", this);
	}
	
	/**
	 * Establishes the components for the Welcome View.
	 * 
	 * @param parent Composite parent of the new object.
	 */
	public void createPartControl(Composite parent) {
		Composite cmpMain = new Composite(parent, SWT.NONE);
		cmpMain.setLayout(new FormLayout());
		cmpMain.setBackground(new Color(parent.getDisplay(), 170, 200, 255));

		Label lblTitle = new Label(cmpMain,SWT.NONE);
		lblTitle.setText(Localization.getString("WelcomeView.Welcome"));
		lblTitle.setFont(new Font(parent.getDisplay(), "Arial", 40, SWT.BOLD));
		lblTitle.setBackground(cmpMain.getBackground());
		lblTitle.setForeground(new Color(parent.getDisplay(), 0, 0, 100));
		lblTitle.setAlignment(SWT.CENTER);

		Image imgIcon = new Image(parent.getDisplay(), SystemTapGUIPlugin.getImageDescriptor("splash.bmp").getImageData());
		Label lblImage = new Label(cmpMain,SWT.NONE);
		lblImage.setBackground(cmpMain.getBackground());
		lblImage.setImage(imgIcon);
		
		lblImage.setAlignment(SWT.CENTER);
		
		
		FormData titleData = new FormData();
		titleData.left = new FormAttachment(0, 0);
		titleData.top = new FormAttachment(0, 0);
		titleData.right = new FormAttachment(100, 0);
		titleData.bottom = new FormAttachment(20, 0);
		lblTitle.setLayoutData(titleData);

		FormData iconData = new FormData();
		iconData.left = new FormAttachment(0, 0);
		iconData.top = new FormAttachment(lblTitle);
		iconData.right = new FormAttachment(100, 0);
		iconData.bottom = new FormAttachment(80, 0);
		lblImage.setLayoutData(iconData);

		
	}
	
	public void setFocus() {
	}

	public void dispose() {
		LogManager.logInfo("Disposing", this);
		super.dispose();
	}
	
	public static final String ID = "org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.views.WelcomeView";
}
