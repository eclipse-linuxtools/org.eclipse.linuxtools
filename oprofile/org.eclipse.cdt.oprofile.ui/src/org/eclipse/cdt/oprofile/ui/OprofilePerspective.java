/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

/**
 * This class defines the profile perspective.
 * @author keiths
 *
 */
public class OprofilePerspective implements IPerspectiveFactory {

	/**
	 * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(IPageLayout)
	 */
	public void createInitialLayout(IPageLayout layout) {
		_defineLayout(layout);
		_defineActions(layout);
	}

	 // Defines the initial actions for a page
	private void _defineActions(IPageLayout layout)
	{
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet("org.eclipse.cdt.oprofile.ui.contribution.set"); //$NON-NLS-1$
	}
	
	 // Defines the initial layout for a page
	private void _defineLayout(IPageLayout layout)
	{
		String editorArea = layout.getEditorArea();		

		IFolderLayout folder1 = layout.createFolder("bottom", IPageLayout.BOTTOM, (float) 0.75, editorArea); //$NON-NLS-1$
		folder1.addView(IPageLayout.ID_TASK_LIST);
		//folder1.addView(OprofilePlugin.ID_VIEW_LOG);
		folder1.addView(OprofilePlugin.ID_VIEW_SAMPLE);
		folder1.addView(OprofilePlugin.ID_VIEW_DAEMON);

		IFolderLayout folder2 = layout.createFolder("topLeft", IPageLayout.LEFT, (float) 0.25, editorArea); //$NON-NLS-1$
		//folder2.addView(OprofilePlugin.ID_VIEW_PROJECT_PROFILE);
		folder2.addView(OprofilePlugin.ID_VIEW_SYSTEM_PROFILE);
		folder2.addView(IPageLayout.ID_RES_NAV);
		folder2.addView(IPageLayout.ID_BOOKMARKS);
		
		// 2003-05-28 keiths: This is really lame, but I don't know what else to do...
		// Focus the SampleView and the SystemProfileView by default
		IWorkbenchPage page = OprofilePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (page != null)
		{
			try
			{
				page.showView(OprofilePlugin.ID_VIEW_SYSTEM_PROFILE);
				page.showView(OprofilePlugin.ID_VIEW_SAMPLE);
			}
			catch (PartInitException pie)
			{
				// Couldn't show the views. The user will have to manually select them.
			};
		}
	}
}
