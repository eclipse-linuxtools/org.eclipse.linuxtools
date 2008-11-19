///*******************************************************************************
// * Copyright (c) 2004,2008 Red Hat, Inc.
// * All rights reserved. This program and the accompanying materials
// * are made available under the terms of the Eclipse Public License v1.0
// * which accompanies this distribution, and is available at
// * http://www.eclipse.org/legal/epl-v10.html
// *
// * Contributors:
// *    Keith Seitz <keiths@redhat.com> - initial API and implementation
// *    Kent Sebastian <ksebasti@redhat.com> - 
// *******************************************************************************/ 
//
//package org.eclipse.linuxtools.oprofile.ui;
//
//import org.eclipse.debug.ui.IDebugUIConstants;
//import org.eclipse.ui.IFolderLayout;
//import org.eclipse.ui.IPageLayout;
//import org.eclipse.ui.IPerspectiveFactory;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.PartInitException;
//
///**
// * This class defines the profile perspective.
// */
//public class OprofilePerspective implements IPerspectiveFactory {
//
//	/**
//	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
//	 */
//	public void createInitialLayout(IPageLayout layout) {
//		_defineLayout(layout);
//		_defineActions(layout);
//	}
//
//	 // Defines the initial actions for a page
//	private void _defineActions(IPageLayout layout)
//	{
//		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
//		layout.addActionSet("org.eclipse.linuxtools.oprofile.ui.contribution.set"); //$NON-NLS-1$
//	}
//	
//	 // Defines the initial layout for a page
//	private void _defineLayout(IPageLayout layout)
//	{
//		String editorArea = layout.getEditorArea();		
//
//		IFolderLayout bottom = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
//		bottom.addView(OprofileUiPlugin.ID_VIEW_SAMPLE);
//		bottom.addView(IPageLayout.ID_TASK_LIST);
//
//		IFolderLayout topleft = layout.createFolder("topLeft", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
//		topleft.addView(OprofileUiPlugin.ID_VIEW_SYSTEM_PROFILE);
////		topleft.addView(IPageLayout.ID_RES_NAV);
//		
//		IFolderLayout topright = layout.createFolder("topRight", IPageLayout.RIGHT, (float) 0.75, editorArea);
//		topright.addView(IPageLayout.ID_RES_NAV);
//		
//		// 2003-05-28 keiths: This is really lame, but I don't know what else to do...
//		// Focus the SampleView and the SystemProfileView by default
//		IWorkbenchPage page = OprofileUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		if (page != null)
//		{
//			try
//			{
//				page.showView(OprofileUiPlugin.ID_VIEW_SYSTEM_PROFILE);
//				page.showView(OprofileUiPlugin.ID_VIEW_SAMPLE);
//			}
//			catch (PartInitException pie)
//			{
//				// Couldn't show the views. The user will have to manually select them.
//			};
//		}
//	}
//}
