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

package org.eclipse.linuxtools.systemtap.ui.systemtapgui;

import org.eclipse.linuxtools.internal.systemtap.ui.systemtapgui.views.WelcomeView;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;



public class Perspective implements IPerspectiveFactory {
	public static String ID = "org.eclipse.linuxtools.systemtap.ui.systemtapgui.Perspective";

	/**
	 * Sets options in the IPageLayout object such as editorAreaVisible and the Welcome View.
	 *
	 * @param layout The IPageLayout object to set options on.
	 */
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		layout.addStandaloneView(WelcomeView.ID, false, IPageLayout.TOP, 1.00f, editorArea);
		layout.getViewLayout(WelcomeView.ID).setCloseable(false);
		layout.addShowViewShortcut(WelcomeView.ID);

		layout.addPerspectiveShortcut(ID);

	}
}
