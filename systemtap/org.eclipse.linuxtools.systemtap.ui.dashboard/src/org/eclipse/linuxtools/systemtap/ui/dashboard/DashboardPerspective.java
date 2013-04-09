/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.dashboard;


import org.eclipse.linuxtools.internal.systemtap.ui.dashboard.views.DashboardView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.ActiveModuleBrowserView;
import org.eclipse.linuxtools.systemtap.ui.dashboard.views.DashboardModuleBrowserView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * The <code>DashboardPerspective</code> class defines the layout of the Dashboard perspective
 * in the application.
 * @see org.eclipse.ui.IPerspectiveFactory
 * @author Ryan Morse
 */
public class DashboardPerspective implements IPerspectiveFactory {
	public static final String ID = "org.eclipse.linuxtools.systemtap.ui.dashboard.DashboardPerspective"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {
		//Don't display the editor
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		//Create the left hand tabed view
		IFolderLayout browsers = layout.createFolder("browsers", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		browsers.addPlaceholder(DashboardModuleBrowserView.ID + ":*"); //$NON-NLS-1$
		browsers.addView(DashboardModuleBrowserView.ID);


		IFolderLayout browsers2 = layout.createFolder("browsers2", IPageLayout.BOTTOM, 0.5f, "browsers"); //$NON-NLS-1$ //$NON-NLS-2$
		browsers2.addPlaceholder(ActiveModuleBrowserView.ID + ":*"); //$NON-NLS-1$
		browsers2.addView(ActiveModuleBrowserView.ID);

		layout.getViewLayout(DashboardModuleBrowserView.ID).setCloseable(false);
		layout.getViewLayout(ActiveModuleBrowserView.ID).setCloseable(false);

		//Add the graph content view.
		layout.addStandaloneView(DashboardView.ID, false, IPageLayout.TOP, 1.0f, editorArea);

		layout.getViewLayout(DashboardView.ID).setCloseable(false);
		layout.getViewLayout(DashboardView.ID).setMoveable(false);

		//Add all perspectives to the MainMenu. Window->Show View
		layout.addShowViewShortcut(DashboardView.ID);
		layout.addShowViewShortcut(DashboardModuleBrowserView.ID);
		layout.addShowViewShortcut(ActiveModuleBrowserView.ID);

		//Add a link to the perspective in the MainMenu.  Window->Open Perspective
		layout.addPerspectiveShortcut(ID);
	}
}
