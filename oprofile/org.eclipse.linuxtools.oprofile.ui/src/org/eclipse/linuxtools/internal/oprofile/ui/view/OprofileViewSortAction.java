package org.eclipse.linuxtools.internal.oprofile.ui.view;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiMessages;
import org.eclipse.linuxtools.internal.oprofile.ui.OprofileUiPlugin;
import org.eclipse.linuxtools.oprofile.ui.model.UiModelRoot;

/**
 *
 * Action handler for tree sorting.
 * tree can be sort by session,event,Lib,function and line number.
 * @since 3.0
 *
 */
public class OprofileViewSortAction extends Action {

	public static Map<UiModelRoot.SORT_TYPE, String> sortTypeMap = new HashMap<>();
	static{
		sortTypeMap.put(UiModelRoot.SORT_TYPE.DEFAULT, OprofileUiMessages.getString("view.actions.default.label")); //$NON-NLS-1$
		sortTypeMap.put(UiModelRoot.SORT_TYPE.SESSION, OprofileUiMessages.getString("view.actions.session.label")); //$NON-NLS-1$
		sortTypeMap.put(UiModelRoot.SORT_TYPE.EVENT, OprofileUiMessages.getString("view.actions.event.label")); //$NON-NLS-1$
		sortTypeMap.put(UiModelRoot.SORT_TYPE.LIB, OprofileUiMessages.getString("view.actions.lib.label")); //$NON-NLS-1$
		sortTypeMap.put(UiModelRoot.SORT_TYPE.FUNCTION, OprofileUiMessages.getString("view.actions.function.label")); //$NON-NLS-1$
		sortTypeMap.put(UiModelRoot.SORT_TYPE.LINE_NO, OprofileUiMessages.getString("view.actions.line.label")); //$NON-NLS-1$
	}
	private UiModelRoot.SORT_TYPE sortType;

	public OprofileViewSortAction(UiModelRoot.SORT_TYPE sortType, String text) {
		super(text);
		this.sortType = sortType;
	}

	@Override
	public void run() {
		UiModelRoot.setSortingType(sortType);
		OprofileUiPlugin.getDefault().getOprofileView().refreshView();
	}

}
