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

package org.eclipse.linuxtools.systemtap.ui.graphing;

import org.eclipse.linuxtools.systemtap.ui.graphing.views.GraphSelectorView;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;



/**
 * The <code>GraphingPerspective</code> class defines the layout of the Graphing perspective
 * in the application.
 * @see org.eclipse.ui.IPerspectiveFactory
 * @author Ryan Morse
 */
public class GraphingPerspective implements IPerspectiveFactory {
	public static String ID = "org.eclipse.linuxtools.systemtap.ui.graphing.GraphingPerspective"; //$NON-NLS-1$

	@Override
	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addStandaloneView(GraphSelectorView.ID, false, IPageLayout.TOP, .75f, editorArea);

		layout.getViewLayout(GraphSelectorView.ID).setCloseable(false);

		//Add all perspectives to the MainMenu. Window->Show View
		layout.addShowViewShortcut(GraphSelectorView.ID);

		//Add a link to the perspective in the MainMenu.  Window->Open Perspective
		layout.addPerspectiveShortcut(ID);
	}
}
