package org.eclipse.linuxtools.systemtap.ui.editor;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.editor.actions.file.OpenRecentFileAction;
import org.eclipse.ui.IActionBars;



public class RecentFileMenuManager {
	private RecentFileMenuManager() {
		addedBars = new ArrayList<IActionBars>();
		for(int i=0; i<MAX_RECENT_FILES; i++)
			fileActions[i] = new OpenRecentFileAction(i);
	}
	
	public static RecentFileMenuManager getInstance() {
		return manager;
	}
	
	public void registerActionBar(IActionBars bars) {
		addedBars.add(bars);
		
		for(int i=0; i<MAX_RECENT_FILES; i++)
			bars.setGlobalActionHandler(
					"org.eclipse.linuxtools.systemtap.ui.editor.actions.file.openRecentFile" + i, //$NON-NLS-1$
					fileActions[i]);
		bars.updateActionBars();
	}
	
	public static void update() {
		for(int i=0; i<MAX_RECENT_FILES; i++)
			fileActions[i].update();
		for(int i=0; i<addedBars.size(); i++)
			addedBars.get(i).updateActionBars();
	}
	
	public static final int MAX_RECENT_FILES = 4;
	private static OpenRecentFileAction[] fileActions = new OpenRecentFileAction[MAX_RECENT_FILES];
	private static RecentFileMenuManager manager = new RecentFileMenuManager();
	private static ArrayList<IActionBars> addedBars;
}
