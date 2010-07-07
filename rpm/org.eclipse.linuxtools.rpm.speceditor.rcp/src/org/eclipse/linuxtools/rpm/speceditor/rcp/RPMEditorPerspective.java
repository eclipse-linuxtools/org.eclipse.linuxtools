/*******************************************************************************
 * Copyright (c) 2000, 2003, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.speceditor.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


public class RPMEditorPerspective implements IPerspectiveFactory {

    public RPMEditorPerspective() {
    }

    public void createInitialLayout(IPageLayout layout) {
		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT,
				0.80f, IPageLayout.ID_EDITOR_AREA);
		layout.addPlaceholder("org.eclipse.ui.console.ConsoleView", IPageLayout.BOTTOM, 0.65f, IPageLayout.ID_EDITOR_AREA);
    }
}
